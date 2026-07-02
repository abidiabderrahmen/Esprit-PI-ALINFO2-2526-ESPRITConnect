package com.esprit.connect.controller;

import com.esprit.connect.dto.UserDTO;
import com.esprit.connect.model.Connection;
import com.esprit.connect.model.User;
import com.esprit.connect.repository.ConnectionRepository;
import com.esprit.connect.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    public ConnectionController(ConnectionRepository connectionRepository, UserRepository userRepository) {
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/send/")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("detail", "Authentication required."));

        Object receiverIdObj = request.get("receiver_id");
        if (receiverIdObj == null)
            return ResponseEntity.badRequest().body(Map.of("detail", "receiver_id is required."));

        Long receiverId = Long.valueOf(receiverIdObj.toString());
        if (receiverId.equals(currentUser.getId()))
            return ResponseEntity.badRequest().body(Map.of("detail", "Cannot connect with yourself."));

        User receiver = userRepository.findById(receiverId).orElse(null);
        if (receiver == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", "User not found."));

        Optional<Connection> existing = connectionRepository.findBetweenUsers(currentUser.getId(), receiverId);
        if (existing.isPresent()) {
            Connection c = existing.get();
            if (c.getStatus() == Connection.Status.ACCEPTED)
                return ResponseEntity.badRequest().body(Map.of("detail", "Already connected."));
            if (c.getStatus() == Connection.Status.PENDING)
                return ResponseEntity.badRequest().body(Map.of("detail", "Connection request already pending."));
            if (c.getStatus() == Connection.Status.REJECTED) {
                c.setStatus(Connection.Status.PENDING);
                c.setSender(currentUser);
                c.setReceiver(receiver);
                connectionRepository.save(c);
                return ResponseEntity.ok(Map.of("detail", "Connection request sent.", "status", "pending"));
            }
        }

        Connection conn = new Connection();
        conn.setSender(currentUser);
        conn.setReceiver(receiver);
        connectionRepository.save(conn);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("detail", "Connection request sent.", "status", "pending"));
    }

    @PostMapping("/{id}/accept/")
    public ResponseEntity<?> acceptRequest(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("detail", "Authentication required."));

        Connection conn = connectionRepository.findById(id).orElse(null);
        if (conn == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", "Connection request not found."));
        if (!conn.getReceiver().getId().equals(currentUser.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("detail", "Only the receiver can accept."));
        if (conn.getStatus() != Connection.Status.PENDING)
            return ResponseEntity.badRequest().body(Map.of("detail", "Request is not pending."));

        conn.setStatus(Connection.Status.ACCEPTED);
        connectionRepository.save(conn);
        return ResponseEntity.ok(Map.of("detail", "Connection accepted.", "status", "accepted"));
    }

    @PostMapping("/{id}/reject/")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("detail", "Authentication required."));

        Connection conn = connectionRepository.findById(id).orElse(null);
        if (conn == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", "Connection request not found."));
        if (!conn.getReceiver().getId().equals(currentUser.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("detail", "Only the receiver can reject."));

        conn.setStatus(Connection.Status.REJECTED);
        connectionRepository.save(conn);
        return ResponseEntity.ok(Map.of("detail", "Connection rejected.", "status", "rejected"));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<?> removeConnection(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("detail", "Authentication required."));

        Connection conn = connectionRepository.findById(id).orElse(null);
        if (conn == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", "Connection not found."));
        if (!conn.getSender().getId().equals(currentUser.getId()) && !conn.getReceiver().getId().equals(currentUser.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("detail", "Not your connection."));

        connectionRepository.delete(conn);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/")
    public ResponseEntity<?> listConnections() {
        User currentUser = getCurrentUser();
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("detail", "Authentication required."));

        List<Connection> accepted = connectionRepository.findAcceptedConnections(currentUser.getId());
        List<Map<String, Object>> result = accepted.stream().map(c -> {
            User other = c.getSender().getId().equals(currentUser.getId()) ? c.getReceiver() : c.getSender();
            Map<String, Object> m = new HashMap<>();
            m.put("connection_id", c.getId());
            m.put("user", UserDTO.fromEntity(other));
            m.put("connected_at", c.getUpdatedAt());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending/")
    public ResponseEntity<?> listPending() {
        User currentUser = getCurrentUser();
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("detail", "Authentication required."));

        List<Connection> pending = connectionRepository.findPendingForUser(currentUser.getId());
        List<Map<String, Object>> result = pending.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("sender", UserDTO.fromEntity(c.getSender()));
            m.put("created_at", c.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/status/{userId}/")
    public ResponseEntity<?> getConnectionStatus(@PathVariable Long userId) {
        User currentUser = getCurrentUser();
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("detail", "Authentication required."));

        Optional<Connection> conn = connectionRepository.findBetweenUsers(currentUser.getId(), userId);
        if (conn.isEmpty())
            return ResponseEntity.ok(Map.of("status", "none"));

        Connection c = conn.get();
        Map<String, Object> result = new HashMap<>();
        result.put("connection_id", c.getId());
        result.put("status", c.getStatus().name().toLowerCase());
        result.put("is_sender", c.getSender().getId().equals(currentUser.getId()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/count/")
    public ResponseEntity<?> connectionCount() {
        User currentUser = getCurrentUser();
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("detail", "Authentication required."));
        long count = connectionRepository.countAcceptedConnections(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) return null;
        String username;
        if (auth.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
        } else {
            username = auth.getPrincipal().toString();
        }
        return userRepository.findByUsername(username).orElse(null);
    }
}
