package com.esprit.connect.service;

import com.esprit.connect.model.User;
import com.esprit.connect.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DirectoryService {

    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public DirectoryService(UserRepository userRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    public Page<User> getDirectory(String search, String role, Integer gradYear,
                                   String fieldOfStudy, Boolean isMentor,
                                   String location, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(buildPredicates(cb, countRoot, search, role, gradYear, fieldOfStudy, isMentor, location));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        query.where(buildPredicates(cb, root, search, role, gradYear, fieldOfStudy, isMentor, location));
        query.orderBy(cb.desc(root.get("createdAt")));

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<User> results = typedQuery.getResultList();
        return new PageImpl<>(results, pageable, total);
    }

    public List<User> getMentors() {
        return userRepository.findByIsMentorTrue();
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<User> root,
                                        String search, String role, Integer gradYear,
                                        String fieldOfStudy, Boolean isMentor, String location) {
        List<Predicate> predicates = new ArrayList<>();

        if (role != null && !role.isBlank()) {
            try {
                User.Role userRole = User.Role.valueOf(role.toUpperCase());
                predicates.add(cb.equal(root.get("role"), userRole));
            } catch (IllegalArgumentException ignored) {
            }
        } else {
            predicates.add(root.get("role").in(User.Role.ALUMNI, User.Role.STUDENT));
        }

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                    cb.like(cb.lower(root.get("username")), pattern),
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("skills")), pattern),
                    cb.like(cb.lower(root.get("companyName")), pattern)
            );
            predicates.add(searchPredicate);
        }

        if (gradYear != null) {
            predicates.add(cb.equal(root.get("graduationYear"), gradYear));
        }

        if (fieldOfStudy != null && !fieldOfStudy.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("fieldOfStudy")),
                    "%" + fieldOfStudy.toLowerCase() + "%"));
        }

        if (isMentor != null) {
            predicates.add(cb.equal(root.get("isMentor"), isMentor));
        }

        if (location != null && !location.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("location")),
                    "%" + location.toLowerCase() + "%"));
        }

        return predicates.toArray(new Predicate[0]);
    }
}
