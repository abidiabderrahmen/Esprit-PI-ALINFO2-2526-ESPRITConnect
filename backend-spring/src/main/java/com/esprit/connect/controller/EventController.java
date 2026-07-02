package com.esprit.connect.controller;

import com.esprit.connect.dto.EventDTO;
import com.esprit.connect.dto.PaginatedResponse;
import com.esprit.connect.dto.UserDTO;
import com.esprit.connect.model.Event;
import com.esprit.connect.model.EventRegistration;
import com.esprit.connect.model.User;
import com.esprit.connect.repository.EventRegistrationRepository;
import com.esprit.connect.repository.EventRepository;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class EventController {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserRepository userRepository;

    public EventController(EventRepository eventRepository,
                           EventRegistrationRepository eventRegistrationRepository,
                           UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/events/")
    public ResponseEntity<?> listEvents(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int page_size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, page_size);
        Page<Event> eventPage = eventRepository.findAllByOrderByDateDesc(pageable);

        List<EventDTO> eventDTOs = eventPage.getContent().stream()
                .map(event -> {
                    boolean registered = currentUser != null &&
                            eventRegistrationRepository.existsByEventIdAndParticipantId(event.getId(), currentUser.getId());
                    return EventDTO.fromEntity(event, registered);
                })
                .collect(Collectors.toList());

        PaginatedResponse<EventDTO> response = PaginatedResponse.fromPage(eventPage, eventDTOs);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/events/")
    public ResponseEntity<?> createEvent(@RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Event event = new Event();
        event.setOrganizer(currentUser);
        event.setTitle((String) request.get("title"));
        event.setDescription((String) request.get("description"));

        String eventType = (String) request.get("event_type");
        if (eventType != null) {
            try {
                event.setEventType(Event.EventType.valueOf(eventType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("event_type", "Invalid event type."));
            }
        }

        event.setLocation((String) request.get("location"));

        String dateStr = (String) request.get("date");
        if (dateStr != null) event.setDate(LocalDate.parse(dateStr));

        String timeStr = (String) request.get("time");
        if (timeStr != null) event.setTime(LocalTime.parse(timeStr));

        Object maxP = request.get("max_participants");
        if (maxP != null) event.setMaxParticipants(Integer.valueOf(maxP.toString()));

        event.setImage((String) request.get("image"));

        Object isOnline = request.get("is_online");
        if (isOnline != null) event.setIsOnline(Boolean.valueOf(isOnline.toString()));

        event.setMeetingLink((String) request.get("meeting_link"));

        Event saved = eventRepository.save(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventDTO.fromEntity(saved, false));
    }

    @GetMapping("/events/{id}/")
    public ResponseEntity<?> getEvent(@PathVariable Long id) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Event not found."));
        }

        User currentUser = getCurrentUser();
        boolean registered = currentUser != null &&
                eventRegistrationRepository.existsByEventIdAndParticipantId(id, currentUser.getId());
        return ResponseEntity.ok(EventDTO.fromEntity(event, registered));
    }

    @PutMapping("/events/{id}/")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Event not found."));
        }

        if (!event.getOrganizer().getId().equals(currentUser.getId()) && !Boolean.TRUE.equals(currentUser.getIsStaff())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("detail", "You do not have permission to edit this event."));
        }

        if (request.containsKey("title")) event.setTitle((String) request.get("title"));
        if (request.containsKey("description")) event.setDescription((String) request.get("description"));
        if (request.containsKey("event_type")) {
            try {
                event.setEventType(Event.EventType.valueOf(((String) request.get("event_type")).toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (request.containsKey("location")) event.setLocation((String) request.get("location"));
        if (request.containsKey("date")) event.setDate(LocalDate.parse((String) request.get("date")));
        if (request.containsKey("time")) event.setTime(LocalTime.parse((String) request.get("time")));
        if (request.containsKey("max_participants")) {
            event.setMaxParticipants(Integer.valueOf(request.get("max_participants").toString()));
        }
        if (request.containsKey("image")) event.setImage((String) request.get("image"));
        if (request.containsKey("is_online")) event.setIsOnline(Boolean.valueOf(request.get("is_online").toString()));
        if (request.containsKey("meeting_link")) event.setMeetingLink((String) request.get("meeting_link"));
        if (request.containsKey("is_active")) event.setIsActive(Boolean.valueOf(request.get("is_active").toString()));

        Event saved = eventRepository.save(event);
        boolean registered = eventRegistrationRepository.existsByEventIdAndParticipantId(id, currentUser.getId());
        return ResponseEntity.ok(EventDTO.fromEntity(saved, registered));
    }

    @DeleteMapping("/events/{id}/")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Event not found."));
        }

        if (!event.getOrganizer().getId().equals(currentUser.getId()) && !Boolean.TRUE.equals(currentUser.getIsStaff())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("detail", "You do not have permission to delete this event."));
        }

        eventRepository.delete(event);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events/{id}/register/")
    public ResponseEntity<?> registerForEvent(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Event not found."));
        }

        if (event.isFull()) {
            return ResponseEntity.badRequest().body(Map.of("detail", "Event is full."));
        }

        if (eventRegistrationRepository.existsByEventIdAndParticipantId(id, currentUser.getId())) {
            return ResponseEntity.badRequest().body(Map.of("detail", "You are already registered."));
        }

        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setParticipant(currentUser);
        EventRegistration saved = eventRegistrationRepository.save(registration);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "event_id", event.getId(),
                "participant_id", currentUser.getId(),
                "registered_at", saved.getRegisteredAt()
        ));
    }

    @PostMapping("/events/{id}/unregister/")
    public ResponseEntity<?> unregisterFromEvent(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        EventRegistration registration = eventRegistrationRepository
                .findByEventIdAndParticipantId(id, currentUser.getId()).orElse(null);
        if (registration == null) {
            return ResponseEntity.badRequest().body(Map.of("detail", "You are not registered for this event."));
        }

        eventRegistrationRepository.delete(registration);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{id}/participants/")
    public ResponseEntity<?> listParticipants(@PathVariable Long id) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Event not found."));
        }

        List<UserDTO> participants = event.getRegistrations().stream()
                .map(reg -> UserDTO.fromEntity(reg.getParticipant()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(participants);
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
