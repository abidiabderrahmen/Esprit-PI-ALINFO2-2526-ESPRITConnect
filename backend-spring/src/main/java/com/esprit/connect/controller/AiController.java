package com.esprit.connect.controller;

import com.esprit.connect.dto.UserDTO;
import com.esprit.connect.model.*;
import com.esprit.connect.repository.*;
import com.esprit.connect.service.AiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final CvAnalysisRepository cvAnalysisRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final UserRepository userRepository;
    private final OpportunityRepository opportunityRepository;
    private final ObjectMapper objectMapper;

    public AiController(AiService aiService, CvAnalysisRepository cvAnalysisRepository,
                        ChatHistoryRepository chatHistoryRepository, UserRepository userRepository,
                        OpportunityRepository opportunityRepository, ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.cvAnalysisRepository = cvAnalysisRepository;
        this.chatHistoryRepository = chatHistoryRepository;
        this.userRepository = userRepository;
        this.opportunityRepository = opportunityRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/analyze-cv/")
    public ResponseEntity<?> analyzeCv(@RequestParam("file") MultipartFile file) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("file", "File is required."));
        }

        try {
            String cvText = extractText(file);
            if (cvText == null || cvText.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("detail", "Could not extract text from the file."));
            }

            Map<String, Object> analysis = aiService.analyzeCv(cvText);

            CvAnalysis cvAnalysis = new CvAnalysis();
            cvAnalysis.setUser(currentUser);
            cvAnalysis.setFileName(file.getOriginalFilename());
            cvAnalysis.setCvText(cvText);
            cvAnalysis.setAnalysisResult(objectMapper.writeValueAsString(analysis));
            cvAnalysisRepository.save(cvAnalysis);

            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("detail", "Error processing file: " + e.getMessage()));
        }
    }

    @PostMapping("/chat/")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        String message = (String) request.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Message is required."));
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
        if (history == null) history = new ArrayList<>();

        String response = aiService.chat(message, history);

        try {
            ChatHistory chatHistory = chatHistoryRepository.findByUserId(currentUser.getId())
                    .orElseGet(() -> {
                        ChatHistory newChat = new ChatHistory();
                        newChat.setUser(currentUser);
                        return newChat;
                    });

            List<Map<String, String>> messages = objectMapper.readValue(
                    chatHistory.getMessages(), new TypeReference<List<Map<String, String>>>() {});
            messages.add(Map.of("role", "user", "content", message));
            messages.add(Map.of("role", "assistant", "content", response));
            chatHistory.setMessages(objectMapper.writeValueAsString(messages));
            chatHistoryRepository.save(chatHistory);
        } catch (JsonProcessingException ignored) {}

        return ResponseEntity.ok(Map.of("response", response));
    }

    @PostMapping("/generate-cover-letter/")
    public ResponseEntity<?> generateCoverLetter(@RequestBody Map<String, String> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        String jobDescription = request.get("job_description");
        String cvText = request.get("cv_text");

        if (jobDescription == null || jobDescription.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("job_description", "Job description is required."));
        }
        if (cvText == null || cvText.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("cv_text", "CV text is required."));
        }

        String coverLetter = aiService.generateCoverLetter(jobDescription, cvText);
        return ResponseEntity.ok(Map.of("cover_letter", coverLetter));
    }

    @GetMapping("/career-copilot/")
    public ResponseEntity<?> careerCopilot() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Map<String, Object> result = new HashMap<>(aiService.careerCopilot(currentUser));

        List<User> mentors = userRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsMentor()) && !u.getId().equals(currentUser.getId()))
                .limit(5)
                .collect(Collectors.toList());
        if (!mentors.isEmpty()) {
            List<Map<String, Object>> mentorList = mentors.stream().map(m -> {
                Map<String, Object> mm = new HashMap<>();
                String name = (m.getFirstName() != null ? m.getFirstName() : "") + " " + (m.getLastName() != null ? m.getLastName() : "");
                mm.put("name", name.trim());
                mm.put("expertise", m.getCurrentPosition() != null ? m.getCurrentPosition() : (m.getSkills() != null ? m.getSkills() : m.getRole() != null ? m.getRole().name() : ""));
                mm.put("match", 80 + (int)(Math.random() * 15));
                mm.put("skills", m.getSkills() != null ? List.of(m.getSkills().split(",")) : List.of());
                return mm;
            }).collect(Collectors.toList());
            result.put("mentor_matches", mentorList);
        }

        List<Opportunity> opportunities = opportunityRepository.findAll().stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsActive()))
                .limit(5)
                .collect(Collectors.toList());
        if (!opportunities.isEmpty()) {
            List<Map<String, Object>> jobList = opportunities.stream().map(o -> {
                Map<String, Object> jm = new HashMap<>();
                jm.put("title", o.getTitle());
                jm.put("company", o.getCompanyName() != null ? o.getCompanyName() : "");
                jm.put("match", 70 + (int)(Math.random() * 25));
                jm.put("salary", o.getSalaryRange() != null ? o.getSalaryRange() : "");
                jm.put("location", o.getLocation() != null ? o.getLocation() : "");
                return jm;
            }).collect(Collectors.toList());
            result.put("job_matches", jobList);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/match-score/")
    public ResponseEntity<?> matchScore(@RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Object oppIdObj = request.get("opportunity_id");
        if (oppIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("opportunity_id", "Opportunity ID is required."));
        }
        Long opportunityId = Long.valueOf(oppIdObj.toString());

        Map<String, Object> result = aiService.matchScore(currentUser.getId(), opportunityId);
        return ResponseEntity.ok(result);
    }

    private String extractText(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) filename = "";
        String lower = filename.toLowerCase();

        if (lower.endsWith(".pdf")) {
            try (PDDocument document = org.apache.pdfbox.Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else if (lower.endsWith(".docx")) {
            try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
                return document.getParagraphs().stream()
                        .map(XWPFParagraph::getText)
                        .collect(Collectors.joining("\n"));
            }
        } else if (lower.endsWith(".txt")) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } else {
            throw new IllegalArgumentException("Unsupported file format. Please upload PDF, DOCX, or TXT.");
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        String username;
        if (auth.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
        } else {
            username = auth.getPrincipal().toString();
        }
        return userRepository.findByUsername(username).orElse(null);
    }
}
