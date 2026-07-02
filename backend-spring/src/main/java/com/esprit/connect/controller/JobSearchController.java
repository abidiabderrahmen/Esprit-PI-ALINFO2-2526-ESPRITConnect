package com.esprit.connect.controller;

import com.esprit.connect.model.User;
import com.esprit.connect.repository.UserRepository;
import com.esprit.connect.service.JobSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobSearchController {

    private final JobSearchService jobSearchService;
    private final UserRepository userRepository;

    public JobSearchController(JobSearchService jobSearchService, UserRepository userRepository) {
        this.jobSearchService = jobSearchService;
        this.userRepository = userRepository;
    }

    @GetMapping("/search/")
    public ResponseEntity<?> searchJobs(@RequestParam(required = false) String query) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        Map<String, Object> results = jobSearchService.searchRealJobs(currentUser, query);
        return ResponseEntity.ok(results);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }
}
