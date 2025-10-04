package com.projectmanager.specification;

import com.projectmanager.entity.Step;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class StepSpecification {

    public static Specification<Step> hasNameLike(String name) {
        return (root, query, cb) -> name == null ? cb.conjunction() : cb.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<Step> hasProjectId(UUID projectId) {
        return (root, query, cb) -> projectId == null ? cb.conjunction() : cb.equal(root.get("project").get("id"), projectId);
    }

}
