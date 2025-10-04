package com.projectmanager.controller;

import com.projectmanager.dto.task.TaskCreateDto;
import com.projectmanager.dto.task.TaskReadDto;
import com.projectmanager.dto.task.TaskUpdateDto;
import com.projectmanager.entity.TaskStatus;
import com.projectmanager.service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskReadDto> createTask(@Valid @RequestBody TaskCreateDto dto) throws Exception {
        UUID currentUserId = getCurrentUserId();
        TaskReadDto createdTask = taskService.createTask(dto, currentUserId);
        logger.info("Task created with ID: {}", createdTask.getId());
        return ResponseEntity.ok(createdTask);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskReadDto> updateTask(@PathVariable UUID id, @Valid @RequestBody TaskUpdateDto dto) {
        UUID currentUserId = getCurrentUserId();
        TaskReadDto updatedTask = taskService.updateTask(id, dto, currentUserId);
        logger.info("Task updated with ID: {}", id);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        UUID currentUserId = getCurrentUserId();
        taskService.deleteTask(id, currentUserId);
        logger.info("Task deleted with ID: {}", id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskReadDto> getTaskById(@PathVariable UUID id) {
        UUID currentUserId = getCurrentUserId();
        TaskReadDto task = taskService.getTaskById(id, currentUserId);
        logger.info("Task retrieved with ID: {}", id);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskReadDto>> getTasksByProjectId(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) UUID stepId,
            @RequestParam(required = false) TaskStatus status) {
        UUID currentUserId = getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<TaskReadDto> tasks = taskService.getTasksByProjectId(projectId, pageRequest, title, description, assignedTo, stepId, status, currentUserId);
        logger.info("Tasks retrieved for project ID: {}", projectId);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskReadDto> updateTaskStatus(@PathVariable UUID id) {
        UUID currentUserId = getCurrentUserId();
        TaskReadDto updatedTask = taskService.updateTaskStatus(id, currentUserId);
        logger.info("Task status updated for ID: {}", id);
        return ResponseEntity.ok(updatedTask);
    }

}
