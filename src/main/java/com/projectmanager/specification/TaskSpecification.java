package com.projectmanager.specification;

import com.projectmanager.entity.Task;
import com.projectmanager.entity.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class TaskSpecification {

    public static Specification<Task> hasTitleLike(String title) {
        return (root, query, cb) -> title == null ? cb.conjunction() : cb.like(root.get("title"), "%" + title + "%");
    }

    public static Specification<Task> hasProjectId(UUID projectId) {
        return (root, query, cb) -> projectId == null ? cb.conjunction() : cb.equal(root.get("step").get("project").get("id"), projectId);
    }

    public static Specification<Task> hasAssignedTo(UUID userId) {
        return (root, query, cb) -> userId == null ? cb.conjunction() : cb.equal(root.get("assignedTo").get("id"), userId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasDescriptionLike(String description) {
        return (root, query, cb) -> description == null ? cb.conjunction() : cb.like(root.get("description"), "%" + description + "%");
    }

    public static Specification<Task> hasStepId(UUID stepId) {
        return (root, query, cb) -> stepId == null ? cb.conjunction() : cb.equal(root.get("step").get("id"), stepId);
    }

}
