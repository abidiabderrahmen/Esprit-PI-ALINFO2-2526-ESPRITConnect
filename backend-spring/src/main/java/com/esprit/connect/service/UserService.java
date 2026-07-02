package com.esprit.connect.service;

import com.esprit.connect.model.User;
import com.esprit.connect.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User updateProfile(Long userId, Map<String, Object> updates) {
        User user = getUserById(userId);

        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("bio")) {
            user.setBio((String) updates.get("bio"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("linkedinUrl")) {
            user.setLinkedinUrl((String) updates.get("linkedinUrl"));
        }
        if (updates.containsKey("githubUrl")) {
            user.setGithubUrl((String) updates.get("githubUrl"));
        }
        if (updates.containsKey("graduationYear")) {
            Object val = updates.get("graduationYear");
            if (val instanceof Number) {
                user.setGraduationYear(((Number) val).intValue());
            }
        }
        if (updates.containsKey("fieldOfStudy")) {
            user.setFieldOfStudy((String) updates.get("fieldOfStudy"));
        }
        if (updates.containsKey("currentPosition")) {
            user.setCurrentPosition((String) updates.get("currentPosition"));
        }
        if (updates.containsKey("companyName")) {
            user.setCompanyName((String) updates.get("companyName"));
        }
        if (updates.containsKey("location")) {
            user.setLocation((String) updates.get("location"));
        }
        if (updates.containsKey("skills")) {
            user.setSkills((String) updates.get("skills"));
        }
        if (updates.containsKey("isMentor")) {
            Object val = updates.get("isMentor");
            if (val instanceof Boolean) {
                user.setIsMentor((Boolean) val);
            }
        }
        if (updates.containsKey("avatar")) {
            user.setAvatar((String) updates.get("avatar"));
        }

        return userRepository.save(user);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> searchUsers(String query, Pageable pageable) {
        return userRepository
                .findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrSkillsContainingIgnoreCase(
                        query, query, query, query, pageable);
    }
}
