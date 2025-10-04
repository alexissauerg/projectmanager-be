package com.projectmanager.controller;

import com.projectmanager.dto.step.StepCreateDto;
import com.projectmanager.dto.step.StepReadDto;
import com.projectmanager.dto.step.StepUpdateDto;
import com.projectmanager.service.StepService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/steps")
public class StepController {

    private static final Logger logger = LoggerFactory.getLogger(StepController.class);

    private final StepService stepService;

    public StepController(StepService stepService) {
        this.stepService = stepService;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StepReadDto> createStep(@PathVariable UUID projectId, @Valid @RequestBody StepCreateDto dto) {
        UUID currentUserId = getCurrentUserId();
        StepReadDto createdStep = stepService.createStep(dto, projectId, currentUserId);
        logger.info("Step created with ID: {} for project {}", createdStep.getId(), projectId);
        return ResponseEntity.ok(createdStep);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StepReadDto> updateStep(@PathVariable UUID id, @Valid @RequestBody StepUpdateDto dto) {
        UUID currentUserId = getCurrentUserId();
        StepReadDto updatedStep = stepService.updateStep(id, dto, currentUserId);
        logger.info("Step updated with ID: {}", id);
        return ResponseEntity.ok(updatedStep);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteStep(@PathVariable UUID id) {
        UUID currentUserId = getCurrentUserId();
        stepService.deleteStep(id, currentUserId);
        logger.info("Step deleted with ID: {}", id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StepReadDto> getStepById(@PathVariable UUID id) {
        UUID currentUserId = getCurrentUserId();
        StepReadDto step = stepService.getStepById(id, currentUserId);
        logger.info("Step retrieved with ID: {}", id);
        return ResponseEntity.ok(step);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<StepReadDto>> getStepsByProjectId(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        UUID currentUserId = getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<StepReadDto> steps = stepService.getStepsByProjectId(projectId, pageRequest, name, currentUserId);
        logger.info("Steps retrieved for project ID: {}", projectId);
        return ResponseEntity.ok(steps);
    }

}
