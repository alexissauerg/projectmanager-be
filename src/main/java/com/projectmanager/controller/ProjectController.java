package com.projectmanager.controller;

import com.projectmanager.dto.project.ProjectCreateDto;
import com.projectmanager.dto.project.ProjectReadDto;
import com.projectmanager.dto.project.ProjectUpdateDto;
import com.projectmanager.service.ProjectService;
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
@RequestMapping("/api/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectReadDto> createProject(@Valid @RequestBody ProjectCreateDto dto) {
        UUID currentUserId = getCurrentUserId();
        ProjectReadDto createdProject = projectService.createProject(dto, currentUserId);
        logger.info("Project created with ID: {}", createdProject.getId());
        return ResponseEntity.ok(createdProject);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectReadDto> updateProject(@PathVariable UUID id, @Valid @RequestBody ProjectUpdateDto dto) {
        UUID currentUserId = getCurrentUserId();
        ProjectReadDto updatedProject = projectService.updateProject(id, dto, currentUserId);
        logger.info("Project updated with ID: {}", id);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        UUID currentUserId = getCurrentUserId();
        projectService.deleteProject(id, currentUserId);
        logger.info("Project deleted with ID: {}", id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectReadDto> getProjectById(@PathVariable UUID id) {
        UUID currentUserId = getCurrentUserId();
        ProjectReadDto project = projectService.getProjectById(id, currentUserId);
        logger.info("Project retrieved with ID: {}", id);
        return ResponseEntity.ok(project);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ProjectReadDto>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) UUID userId) {
        UUID currentUserId = getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ProjectReadDto> projects = projectService.getAllProjects(pageRequest, name, description, userId, currentUserId);
        logger.info("All projects retrieved for user ID: {}", currentUserId);
        return ResponseEntity.ok(projects);
    }

    @PostMapping("/{projectId}/users/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectReadDto> addUserToProject(@PathVariable UUID projectId, @PathVariable UUID userId) {
        UUID currentUserId = getCurrentUserId();
        ProjectReadDto updatedProject = projectService.addUserToProject(projectId, userId, currentUserId);
        logger.info("User {} added to project {}", userId, projectId);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}/users/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectReadDto> removeUserFromProject(@PathVariable UUID projectId, @PathVariable UUID userId) {
        UUID currentUserId = getCurrentUserId();
        ProjectReadDto updatedProject = projectService.removeUserFromProject(projectId, userId, currentUserId);
        logger.info("User {} removed from project {}", userId, projectId);
        return ResponseEntity.ok(updatedProject);
    }

}
