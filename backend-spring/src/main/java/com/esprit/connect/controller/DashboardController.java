package com.esprit.connect.controller;

import com.esprit.connect.dto.UserDTO;
import com.esprit.connect.model.User;
import com.esprit.connect.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final UserRepository userRepository;
    private final OpportunityRepository opportunityRepository;
    private final EventRepository eventRepository;
    private final PostRepository postRepository;

    public DashboardController(UserRepository userRepository,
                               OpportunityRepository opportunityRepository,
                               EventRepository eventRepository,
                               PostRepository postRepository) {
        this.userRepository = userRepository;
        this.opportunityRepository = opportunityRepository;
        this.eventRepository = eventRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/dashboard/stats/")
    public ResponseEntity<?> getDashboardStats() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }
        if (!Boolean.TRUE.equals(currentUser.getIsStaff()) &&
                currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("detail", "Admin access required."));
        }

        List<User> allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();

        Map<String, Long> usersByRole = allUsers.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getRole() != null ? u.getRole().name().toLowerCase() : "unknown",
                        Collectors.counting()
                ));

        long totalOpportunities = opportunityRepository.count();
        long activeOpportunities = opportunityRepository.findAll().stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsActive()))
                .count();

        long totalEvents = eventRepository.count();
        long activeEvents = eventRepository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()) &&
                        (e.getDate() == null || !e.getDate().isBefore(LocalDate.now())))
                .count();

        long totalPosts = postRepository.count();

        List<UserDTO> recentUsers = userRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("total_users", totalUsers);
        stats.put("users_by_role", usersByRole);
        stats.put("total_opportunities", totalOpportunities);
        stats.put("active_opportunities", activeOpportunities);
        stats.put("total_events", totalEvents);
        stats.put("active_events", activeEvents);
        stats.put("total_posts", totalPosts);
        stats.put("recent_users", recentUsers);

        return ResponseEntity.ok(stats);
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
