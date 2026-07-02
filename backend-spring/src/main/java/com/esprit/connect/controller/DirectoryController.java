package com.esprit.connect.controller;

import com.esprit.connect.dto.MentorshipRequestDTO;
import com.esprit.connect.dto.PaginatedResponse;
import com.esprit.connect.dto.UserDTO;
import com.esprit.connect.model.MentorshipRequest;
import com.esprit.connect.model.User;
import com.esprit.connect.repository.MentorshipRequestRepository;
import com.esprit.connect.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DirectoryController {

    private final UserRepository userRepository;
    private final MentorshipRequestRepository mentorshipRequestRepository;

    public DirectoryController(UserRepository userRepository,
                               MentorshipRequestRepository mentorshipRequestRepository) {
        this.userRepository = userRepository;
        this.mentorshipRequestRepository = mentorshipRequestRepository;
    }

    @GetMapping({"/directory/", "/users/"})
    public ResponseEntity<?> searchDirectory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer graduation_year,
            @RequestParam(required = false) String field_of_study,
            @RequestParam(required = false) Boolean is_mentor,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size) {

        Pageable pageable = PageRequest.of(page - 1, page_size);
        Page<User> userPage = userRepository.findAll(pageable);

        List<User> filtered = userPage.getContent().stream()
                .filter(user -> {
                    if (search != null && !search.isBlank()) {
                        String q = search.toLowerCase();
                        boolean matches = (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(q))
                                || (user.getLastName() != null && user.getLastName().toLowerCase().contains(q))
                                || (user.getUsername() != null && user.getUsername().toLowerCase().contains(q))
                                || (user.getEmail() != null && user.getEmail().toLowerCase().contains(q));
                        if (!matches) return false;
                    }
                    if (role != null && !role.isBlank()) {
                        if (user.getRole() == null || !user.getRole().name().equalsIgnoreCase(role)) return false;
                    }
                    if (graduation_year != null) {
                        if (user.getGraduationYear() == null || !user.getGraduationYear().equals(graduation_year)) return false;
                    }
                    if (field_of_study != null && !field_of_study.isBlank()) {
                        if (user.getFieldOfStudy() == null || !user.getFieldOfStudy().toLowerCase().contains(field_of_study.toLowerCase())) return false;
                    }
                    if (is_mentor != null) {
                        if (!is_mentor.equals(user.getIsMentor())) return false;
                    }
                    if (location != null && !location.isBlank()) {
                        if (user.getLocation() == null || !user.getLocation().toLowerCase().contains(location.toLowerCase())) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<UserDTO> userDTOs = filtered.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());

        PaginatedResponse<UserDTO> response = new PaginatedResponse<>();
        response.setCount(userPage.getTotalElements());
        response.setResults(userDTOs);
        if (userPage.hasNext()) {
            response.setNext("/api/directory/?page=" + (userPage.getNumber() + 2));
        }
        if (userPage.hasPrevious()) {
            response.setPrevious("/api/directory/?page=" + userPage.getNumber());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping({"/directory/{id}/", "/users/{id}/"})
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "User not found."));
        }
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @GetMapping("/directory/mentors/")
    public ResponseEntity<?> listMentors() {
        List<User> allUsers = userRepository.findAll();
        List<UserDTO> mentors = allUsers.stream()
                .filter(user -> Boolean.TRUE.equals(user.getIsMentor()))
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mentors);
    }

    @PostMapping("/mentorship/")
    public ResponseEntity<?> createMentorshipRequest(@RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Object mentorIdObj = request.get("mentor");
        if (mentorIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("mentor", "Mentor ID is required."));
        }
        Long mentorId = Long.valueOf(mentorIdObj.toString());

        User mentor = userRepository.findById(mentorId).orElse(null);
        if (mentor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Mentor not found."));
        }
        if (!Boolean.TRUE.equals(mentor.getIsMentor())) {
            return ResponseEntity.badRequest().body(Map.of("detail", "Selected user is not a mentor."));
        }
        if (mentor.getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(Map.of("detail", "You cannot request mentorship from yourself."));
        }

        String message = (String) request.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Message is required."));
        }

        MentorshipRequest mentorshipRequest = new MentorshipRequest();
        mentorshipRequest.setMentee(currentUser);
        mentorshipRequest.setMentor(mentor);
        mentorshipRequest.setMessage(message);

        MentorshipRequest saved = mentorshipRequestRepository.save(mentorshipRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(MentorshipRequestDTO.fromEntity(saved));
    }

    @PostMapping("/mentorship/{id}/accept/")
    public ResponseEntity<?> acceptMentorshipRequest(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        MentorshipRequest mentorshipRequest = mentorshipRequestRepository.findById(id).orElse(null);
        if (mentorshipRequest == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Mentorship request not found."));
        }
        if (!mentorshipRequest.getMentor().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("detail", "Only the mentor can accept this request."));
        }

        mentorshipRequest.setStatus(MentorshipRequest.Status.ACCEPTED);
        MentorshipRequest saved = mentorshipRequestRepository.save(mentorshipRequest);
        return ResponseEntity.ok(MentorshipRequestDTO.fromEntity(saved));
    }

    @PostMapping("/mentorship/{id}/reject/")
    public ResponseEntity<?> rejectMentorshipRequest(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        MentorshipRequest mentorshipRequest = mentorshipRequestRepository.findById(id).orElse(null);
        if (mentorshipRequest == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Mentorship request not found."));
        }
        if (!mentorshipRequest.getMentor().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("detail", "Only the mentor can reject this request."));
        }

        mentorshipRequest.setStatus(MentorshipRequest.Status.REJECTED);
        MentorshipRequest saved = mentorshipRequestRepository.save(mentorshipRequest);
        return ResponseEntity.ok(MentorshipRequestDTO.fromEntity(saved));
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
