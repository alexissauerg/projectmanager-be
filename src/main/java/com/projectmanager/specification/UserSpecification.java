package com.projectmanager.specification;

import com.projectmanager.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UserSpecification {

    public static Specification<User> hasNameLike(String name) {
        return (root, query, cb) -> name == null ? cb.conjunction() : cb.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<User> hasEmailLike(String email) {
        return (root, query, cb) -> email == null ? cb.conjunction() : cb.like(root.get("email"), "%" + email + "%");
    }

    public static Specification<User> hasRole(String role) {
        return (root, query, cb) -> role == null ? cb.conjunction() : cb.equal(root.get("role"), role);
    }

    public static Specification<User> isEmailVerified(Boolean emailVerified) {
        return (root, query, cb) -> emailVerified == null ? cb.conjunction() : cb.equal(root.get("emailVerified"), emailVerified);
    }

    public static Specification<User> hasId(UUID id) {
        return (root, query, cb) -> id == null ? cb.conjunction() : cb.equal(root.get("id"), id);
    }

}
