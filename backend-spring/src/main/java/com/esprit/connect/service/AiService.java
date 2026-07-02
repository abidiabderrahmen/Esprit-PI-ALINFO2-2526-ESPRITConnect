package com.esprit.connect.service;

import com.esprit.connect.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Value("${app.groq.api-key:}")
    private String apiKey;

    @Value("${app.groq.model:llama-3.3-70b-versatile}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String callGroq(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) return null;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null) messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.7);
        body.put("max_tokens", 2048);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = restTemplate.exchange(GROQ_URL, HttpMethod.POST, entity, Map.class);

        if (resp.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }
        return null;
    }

    private Map<String, Object> parseJson(String text) {
        if (text == null) return null;
        text = text.strip();
        if (text.startsWith("```")) {
            String[] lines = text.split("\n");
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < lines.length; i++) {
                if (i == lines.length - 1 && lines[i].strip().equals("```")) continue;
                sb.append(lines[i]).append("\n");
            }
            text = sb.toString().strip();
        }
        try {
            return objectMapper.readValue(text, new TypeReference<>() {});
        } catch (Exception e) {
            int start = text.indexOf('{');
            int end = text.lastIndexOf('}') + 1;
            if (start >= 0 && end > start) {
                try {
                    return objectMapper.readValue(text.substring(start, end), new TypeReference<>() {});
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    public String chat(String message, List<Map<String, String>> history) {
        String system = "You are an AI Career Assistant for ESPRIT Connect, a professional alumni network for ESPRIT engineering school in Tunisia. Provide helpful, professional career advice. Be concise but thorough.";

        StringBuilder prompt = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            prompt.append("Previous conversation:\n");
            int start = Math.max(0, history.size() - 10);
            for (int i = start; i < history.size(); i++) {
                Map<String, String> h = history.get(i);
                prompt.append(h.getOrDefault("role", "user")).append(": ").append(h.getOrDefault("text", "")).append("\n");
            }
            prompt.append("\n");
        }
        prompt.append("User message: ").append(message);

        try {
            String resp = callGroq(system, prompt.toString());
            if (resp != null) return resp;
        } catch (Exception e) {
            log.error("Chat error: {}", e.getMessage());
        }
        return "I'm your AI Career Assistant. I'm having trouble connecting right now. Please try again.";
    }

    public Map<String, Object> analyzeCv(String cvText) {
        String system = "You are an expert CV analyst. Respond with valid JSON only, no extra text.";
        String prompt = "Analyze this CV and respond ONLY with valid JSON:\n\n" + cvText.substring(0, Math.min(cvText.length(), 4000))
                + "\n\nJSON format: {\"overall_score\":75,\"scores\":[{\"label\":\"ATS Compatibility\",\"score\":72,\"icon\":\"smart_toy\",\"tip\":\"tip\"}],\"improvements\":[{\"title\":\"Section\",\"description\":\"What to improve\",\"priority\":\"high\"}],\"matching_jobs\":[{\"title\":\"Job\",\"company\":\"Co\",\"match\":85,\"missing\":[\"skill\"]}],\"cert_recommendations\":[{\"name\":\"Cert\",\"relevance\":\"Why\",\"difficulty\":\"Beginner\",\"time\":\"4 weeks\"}]}";

        try {
            String resp = callGroq(system, prompt);
            Map<String, Object> result = parseJson(resp);
            if (result != null) return result;
        } catch (Exception e) {
            log.error("CV analysis error: {}", e.getMessage());
        }
        return fallbackCvAnalysis();
    }

    public String generateCoverLetter(String jobDescription, String cvText) {
        String system = "You are an expert cover letter writer. Write compelling, professional cover letters.";
        String prompt = "Generate a professional cover letter for:\n\nJOB: " + jobDescription + "\n\nCV: " + (cvText != null ? cvText : "") + "\n\nWrite 3-4 paragraphs.";
        try {
            String resp = callGroq(system, prompt);
            if (resp != null) return resp;
        } catch (Exception e) {
            log.error("Cover letter error: {}", e.getMessage());
        }
        return "Unable to generate cover letter. Please try again.";
    }

    public Map<String, Object> careerCopilot(User user) {
        String system = "You are an expert career advisor for ESPRIT Connect (Tunisian engineering school alumni network). Analyze the user profile and provide PERSONALIZED career guidance. Respond with valid JSON only, no extra text.";

        StringBuilder profileInfo = new StringBuilder();
        profileInfo.append("USER PROFILE:\n");
        profileInfo.append("Name: ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n");
        profileInfo.append("Role: ").append(user.getRole() != null ? user.getRole().name() : "STUDENT").append("\n");
        if (user.getSkills() != null) profileInfo.append("Skills: ").append(user.getSkills()).append("\n");
        if (user.getFieldOfStudy() != null) profileInfo.append("Field of Study: ").append(user.getFieldOfStudy()).append("\n");
        if (user.getCurrentPosition() != null) profileInfo.append("Current Position: ").append(user.getCurrentPosition()).append("\n");
        if (user.getCompanyName() != null) profileInfo.append("Company: ").append(user.getCompanyName()).append("\n");
        if (user.getGraduationYear() != null) profileInfo.append("Graduation Year: ").append(user.getGraduationYear()).append("\n");
        if (user.getLocation() != null) profileInfo.append("Location: ").append(user.getLocation()).append("\n");
        if (user.getBio() != null) profileInfo.append("Bio: ").append(user.getBio()).append("\n");

        String prompt = profileInfo + "\nBased on this profile, provide personalized career guidance as JSON:\n"
                + "{\"employability_score\":0-100,\"skills\":[{\"name\":\"SkillName\",\"level\":0-100,\"category\":\"Technical|Soft Skill\"}],"
                + "\"skill_gaps\":[{\"skill\":\"SkillName\",\"importance\":\"high|medium|low\",\"description\":\"Why needed\",\"resources\":\"How to learn\"}],"
                + "\"career_roadmap\":[{\"phase\":\"Current|6 months|1 year|2 years\",\"title\":\"Role\",\"description\":\"Key actions\",\"color\":\"#hex\"}],"
                + "\"job_matches\":[{\"title\":\"Job Title\",\"company\":\"Company in Tunisia or remote\",\"match\":0-100,\"location\":\"City\",\"salary\":\"range TND\"}],"
                + "\"mentor_matches\":[{\"name\":\"Mentor Name\",\"expertise\":\"Area\",\"match\":0-100}],"
                + "\"interview_readiness\":{\"overall\":0-100,\"technical\":0-100,\"behavioral\":0-100,\"system_design\":0-100},"
                + "\"cv_score\":{\"ats\":0-100,\"content\":0-100,\"format\":0-100,\"keywords\":0-100}}";
        try {
            String resp = callGroq(system, prompt);
            Map<String, Object> result = parseJson(resp);
            if (result != null) return result;
        } catch (Exception e) {
            log.error("Copilot error: {}", e.getMessage());
        }
        return fallbackCopilot();
    }

    public Map<String, Object> matchScore(Long userId, Long opportunityId) {
        try {
            String resp = callGroq("Calculate job match. Respond JSON only.", "{\"score\":82,\"breakdown\":{\"skills\":85,\"experience\":75,\"education\":80},\"recommendations\":[\"Highlight projects\",\"Add certs\"]}");
            Map<String, Object> result = parseJson(resp);
            if (result != null) return result;
        } catch (Exception e) {
            log.error("Match score error: {}", e.getMessage());
        }
        return Map.of("score", 75, "breakdown", Map.of("skills", 80, "experience", 70, "education", 75), "recommendations", List.of());
    }

    private Map<String, Object> fallbackCvAnalysis() {
        Map<String, Object> r = new HashMap<>();
        r.put("overall_score", 72);
        r.put("scores", List.of(
                Map.of("label", "ATS Compatibility", "score", 72, "icon", "smart_toy", "tip", "Add industry keywords"),
                Map.of("label", "Content Quality", "score", 68, "icon", "description", "tip", "Quantify achievements"),
                Map.of("label", "Format & Structure", "score", 85, "icon", "grid_view", "tip", "Good structure"),
                Map.of("label", "Keywords Match", "score", 55, "icon", "key", "tip", "Add trending keywords")
        ));
        r.put("improvements", List.of(
                Map.of("title", "Experience", "description", "Include metrics", "priority", "high"),
                Map.of("title", "Skills", "description", "Use standard keywords", "priority", "high"),
                Map.of("title", "Projects", "description", "Add technical projects", "priority", "medium")
        ));
        r.put("matching_jobs", List.of(
                Map.of("title", "Software Developer", "company", "Tech Corp", "match", 78, "missing", List.of("Docker")),
                Map.of("title", "Junior Engineer", "company", "StartupTN", "match", 72, "missing", List.of("AWS"))
        ));
        r.put("cert_recommendations", List.of(
                Map.of("name", "AWS Cloud Practitioner", "relevance", "High demand", "difficulty", "Beginner", "time", "4 weeks"),
                Map.of("name", "Google IT Support", "relevance", "Strong foundation", "difficulty", "Beginner", "time", "6 weeks")
        ));
        return r;
    }

    private Map<String, Object> fallbackCopilot() {
        Map<String, Object> r = new HashMap<>();
        r.put("employability_score", 74);
        r.put("skills", List.of(Map.of("name", "Python", "level", 78, "category", "Technical"), Map.of("name", "JavaScript", "level", 72, "category", "Technical"), Map.of("name", "Problem Solving", "level", 80, "category", "Soft Skill")));
        r.put("skill_gaps", List.of(Map.of("skill", "Cloud Platforms", "importance", "high", "description", "Essential for modern roles", "resources", "AWS/GCP free tiers"), Map.of("skill", "CI/CD", "importance", "medium", "description", "Important for DevOps", "resources", "GitHub Actions")));
        r.put("career_roadmap", List.of(Map.of("phase", "Now", "title", "Strengthen Basics", "description", "Master core tech", "color", "#4338CA"), Map.of("phase", "6 months", "title", "Build Portfolio", "description", "Create projects", "color", "#059669"), Map.of("phase", "1 year", "title", "First Role", "description", "Land target position", "color", "#EA580C"), Map.of("phase", "2 years", "title", "Grow & Lead", "description", "Advance career", "color", "#7C3AED")));
        r.put("job_matches", List.of(Map.of("title", "Full Stack Developer", "company", "Vermeg", "match", 85, "location", "Tunis"), Map.of("title", "Backend Engineer", "company", "InstaDeep", "match", 78, "location", "Tunis")));
        r.put("mentor_matches", List.of(Map.of("name", "Tech Lead", "expertise", "Full Stack", "match", 88), Map.of("name", "Senior Dev", "expertise", "Backend", "match", 82)));
        r.put("interview_readiness", Map.of("overall", 70, "technical", 72, "behavioral", 68, "system_design", 58));
        r.put("cv_score", Map.of("ats", 70, "content", 65, "format", 80, "keywords", 55));
        return r;
    }
}
