package com.esprit.connect.service;

import com.esprit.connect.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobSearchService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String FREEHIRE_BASE = "https://freehire.dev/api/v1/jobs/search";
    private static final int MAX_JOBS = 30;

    public Map<String, Object> searchRealJobs(User user, String query) {
        String skills = user.getSkills() != null ? user.getSkills().trim() : "";
        String[] skillList = skills.isEmpty() ? new String[0] :
                Arrays.stream(skills.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new);

        boolean hasSkills = skillList.length > 0;

        List<Map<String, Object>> allJobs = new ArrayList<>();
        Set<String> seenSlugs = new HashSet<>();

        // Search by explicit query first
        if (query != null && !query.isBlank()) {
            fetchAndAdd(query, allJobs, seenSlugs);
        }

        // Search by each skill
        if (hasSkills) {
            for (String skill : skillList) {
                if (allJobs.size() >= MAX_JOBS) break;
                fetchAndAdd(skill, allJobs, seenSlugs);
            }
            // Combined skills query
            if (allJobs.size() < MAX_JOBS && skillList.length >= 2) {
                String combined = String.join(" ", skillList);
                fetchAndAdd(combined, allJobs, seenSlugs);
            }
        }

        // If no results yet (no skills, no query), fetch trending
        if (allJobs.isEmpty()) {
            fetchAndAdd("developer", allJobs, seenSlugs);
        }

        // Sort by posted date (newest first)
        allJobs.sort((a, b) -> {
            String da = (String) a.getOrDefault("posted_at", "");
            String db = (String) b.getOrDefault("posted_at", "");
            return db.compareTo(da);
        });

        // Trim to max
        if (allJobs.size() > MAX_JOBS) {
            allJobs = new ArrayList<>(allJobs.subList(0, MAX_JOBS));
        }

        // Build response
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jobs", allJobs);
        result.put("total", allJobs.size());
        result.put("skills", hasSkills ? List.of(skillList) : List.of());
        result.put("has_skills", hasSkills);
        return result;
    }

    private void fetchAndAdd(String query, List<Map<String, Object>> allJobs, Set<String> seenSlugs) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = FREEHIRE_BASE + "?q=" + encoded + "&limit=15&sort=posted_at&order=desc";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "ESPRITConnect/1.0")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> body = objectMapper.readValue(response.body(),
                        new TypeReference<Map<String, Object>>() {});
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> jobs = (List<Map<String, Object>>) body.getOrDefault("data", List.of());

                for (Map<String, Object> job : jobs) {
                    String slug = (String) job.getOrDefault("public_slug", UUID.randomUUID().toString());
                    if (seenSlugs.contains(slug)) continue;
                    seenSlugs.add(slug);

                    Map<String, Object> cleaned = normalizeJob(job, query);
                    allJobs.add(cleaned);
                }
            }
        } catch (Exception e) {
            // Silently skip failed queries
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> normalizeJob(Map<String, Object> raw, String matchedQuery) {
        Map<String, Object> job = new LinkedHashMap<>();
        job.put("id", raw.getOrDefault("public_slug", ""));
        job.put("title", raw.getOrDefault("title", "Untitled Position"));
        job.put("company", raw.getOrDefault("company", "Unknown Company"));

        String location = (String) raw.getOrDefault("location", "");
        if (location == null || location.isBlank()) {
            List<Map<String, Object>> countries = (List<Map<String, Object>>) raw.getOrDefault("countries", List.of());
            if (countries != null && !countries.isEmpty()) {
                location = countries.stream()
                        .map(c -> (String) c.getOrDefault("name", ""))
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.joining(", "));
            }
            if (location.isBlank()) {
                location = "Remote";
            }
        }
        job.put("location", location);

        List<String> skills = (List<String>) raw.getOrDefault("skills", List.of());
        job.put("skills", skills != null ? skills : List.of());

        job.put("posted_at", raw.getOrDefault("posted_at", ""));
        job.put("url", raw.getOrDefault("url", ""));

        String source = (String) raw.getOrDefault("source", "web");
        job.put("source", source);

        // Extract enrichment data
        Map<String, Object> enrichment = (Map<String, Object>) raw.getOrDefault("enrichment", Map.of());
        if (enrichment != null) {
            job.put("seniority", enrichment.getOrDefault("seniority", ""));
            job.put("company_type", enrichment.getOrDefault("company_type", ""));
            job.put("company_size", enrichment.getOrDefault("company_size", ""));
        }

        // Description (strip HTML, truncate)
        String desc = (String) raw.getOrDefault("description", "");
        if (desc != null && !desc.isBlank()) {
            desc = desc.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
            if (desc.length() > 300) desc = desc.substring(0, 300) + "...";
        }
        job.put("description", desc);

        job.put("matched_query", matchedQuery);

        return job;
    }
}
