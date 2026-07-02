package com.esprit.connect.service;

import com.esprit.connect.model.Event;
import com.esprit.connect.model.EventRegistration;
import com.esprit.connect.model.User;
import com.esprit.connect.repository.EventRegistrationRepository;
import com.esprit.connect.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    public EventService(EventRepository eventRepository,
                        EventRegistrationRepository eventRegistrationRepository) {
        this.eventRepository = eventRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
    }

    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAllByOrderByDateDesc(pageable);
    }

    public Event createEvent(User organizer, Event event) {
        event.setOrganizer(organizer);
        return eventRepository.save(event);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    public Event updateEvent(Long id, User requester, Event updates) {
        Event event = getEventById(id);

        if (!event.getOrganizer().getId().equals(requester.getId())) {
            throw new RuntimeException("You can only edit your own events");
        }

        if (updates.getTitle() != null) {
            event.setTitle(updates.getTitle());
        }
        if (updates.getDescription() != null) {
            event.setDescription(updates.getDescription());
        }
        if (updates.getEventType() != null) {
            event.setEventType(updates.getEventType());
        }
        if (updates.getLocation() != null) {
            event.setLocation(updates.getLocation());
        }
        if (updates.getDate() != null) {
            event.setDate(updates.getDate());
        }
        if (updates.getTime() != null) {
            event.setTime(updates.getTime());
        }
        if (updates.getMaxParticipants() != null) {
            event.setMaxParticipants(updates.getMaxParticipants());
        }
        if (updates.getImage() != null) {
            event.setImage(updates.getImage());
        }
        if (updates.getIsOnline() != null) {
            event.setIsOnline(updates.getIsOnline());
        }
        if (updates.getMeetingLink() != null) {
            event.setMeetingLink(updates.getMeetingLink());
        }
        if (updates.getIsActive() != null) {
            event.setIsActive(updates.getIsActive());
        }

        return eventRepository.save(event);
    }

    public void deleteEvent(Long id, User requester) {
        Event event = getEventById(id);

        if (!event.getOrganizer().getId().equals(requester.getId())) {
            throw new RuntimeException("You can only delete your own events");
        }

        eventRepository.delete(event);
    }

    public EventRegistration registerForEvent(Long eventId, User participant) {
        Event event = getEventById(eventId);

        if (event.isFull()) {
            throw new RuntimeException("Event has reached maximum capacity");
        }

        if (eventRegistrationRepository.existsByEventIdAndParticipantId(eventId, participant.getId())) {
            throw new RuntimeException("You are already registered for this event");
        }

        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setParticipant(participant);
        return eventRegistrationRepository.save(registration);
    }

    public void unregisterFromEvent(Long eventId, User participant) {
        EventRegistration registration = eventRegistrationRepository
                .findByEventIdAndParticipantId(eventId, participant.getId())
                .orElseThrow(() -> new RuntimeException("You are not registered for this event"));
        eventRegistrationRepository.delete(registration);
    }

    public boolean isRegistered(Long eventId, Long userId) {
        return eventRegistrationRepository.existsByEventIdAndParticipantId(eventId, userId);
    }
}
