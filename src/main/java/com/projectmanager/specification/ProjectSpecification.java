package com.projectmanager.specification;

import com.projectmanager.entity.Project;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ProjectSpecification {

    public static Specification<Project> hasNameLike(String name) {
        return (root, query, cb) -> name == null ? cb.conjunction() : cb.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<Project> hasUserId(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction();
            }
            return cb.isMember(userId, root.get("users").get("id"));
        };
    }

    public static Specification<Project> hasDescriptionLike(String description) {
        return (root, query, cb) -> description == null ? cb.conjunction() : cb.like(root.get("description"), "%" + description + "%");
    }

}
