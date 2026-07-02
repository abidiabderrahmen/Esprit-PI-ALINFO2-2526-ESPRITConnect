package com.esprit.connect.controller;

import com.esprit.connect.dto.ApplicationDTO;
import com.esprit.connect.dto.OpportunityDTO;
import com.esprit.connect.dto.PaginatedResponse;
import com.esprit.connect.model.Application;
import com.esprit.connect.model.Opportunity;
import com.esprit.connect.model.User;
import com.esprit.connect.repository.ApplicationRepository;
import com.esprit.connect.repository.OpportunityRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class OpportunityController {

    private final OpportunityRepository opportunityRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public OpportunityController(OpportunityRepository opportunityRepository,
                                 ApplicationRepository applicationRepository,
                                 UserRepository userRepository) {
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/opportunities/")
    public ResponseEntity<?> listOpportunities(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int page_size) {
        Pageable pageable = PageRequest.of(page - 1, page_size);
        Page<Opportunity> oppPage = opportunityRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<OpportunityDTO> oppDTOs = oppPage.getContent().stream()
                .map(OpportunityDTO::fromEntity)
                .collect(Collectors.toList());

        PaginatedResponse<OpportunityDTO> response = PaginatedResponse.fromPage(oppPage, oppDTOs);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/opportunities/")
    public ResponseEntity<?> createOpportunity(@RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Opportunity opp = new Opportunity();
        opp.setPostedBy(currentUser);
        opp.setTitle((String) request.get("title"));
        opp.setDescription((String) request.get("description"));

        String oppType = (String) request.get("opportunity_type");
        if (oppType != null) {
            try {
                opp.setOpportunityType(Opportunity.OpportunityType.valueOf(oppType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("opportunity_type", "Invalid opportunity type."));
            }
        }

        opp.setCompanyName((String) request.get("company_name"));
        opp.setLocation((String) request.get("location"));
        opp.setSalaryRange((String) request.get("salary_range"));
        opp.setRequirements((String) request.get("requirements"));

        Object isRemote = request.get("is_remote");
        if (isRemote != null) opp.setIsRemote(Boolean.valueOf(isRemote.toString()));

        String deadline = (String) request.get("deadline");
        if (deadline != null) opp.setDeadline(LocalDate.parse(deadline));

        Opportunity saved = opportunityRepository.save(opp);
        return ResponseEntity.status(HttpStatus.CREATED).body(OpportunityDTO.fromEntity(saved));
    }

    @GetMapping("/opportunities/{id}/")
    public ResponseEntity<?> getOpportunity(@PathVariable Long id) {
        Opportunity opp = opportunityRepository.findById(id).orElse(null);
        if (opp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Opportunity not found."));
        }
        return ResponseEntity.ok(OpportunityDTO.fromEntity(opp));
    }

    @PostMapping("/applications/")
    public ResponseEntity<?> applyForOpportunity(@RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Object oppIdObj = request.get("opportunity");
        if (oppIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("opportunity", "Opportunity ID is required."));
        }
        Long opportunityId = Long.valueOf(oppIdObj.toString());

        Opportunity opp = opportunityRepository.findById(opportunityId).orElse(null);
        if (opp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Opportunity not found."));
        }

        if (applicationRepository.existsByOpportunityIdAndApplicantId(opportunityId, currentUser.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("detail", "You have already applied for this opportunity."));
        }

        String coverLetter = (String) request.get("cover_letter");
        if (coverLetter == null || coverLetter.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("cover_letter", "Cover letter is required."));
        }

        Application application = new Application();
        application.setOpportunity(opp);
        application.setApplicant(currentUser);
        application.setCoverLetter(coverLetter);

        Application saved = applicationRepository.save(application);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApplicationDTO.fromEntity(saved));
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
