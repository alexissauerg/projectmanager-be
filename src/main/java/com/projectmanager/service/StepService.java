package com.projectmanager.service;

import com.projectmanager.dto.step.StepCreateDto;
import com.projectmanager.dto.step.StepReadDto;
import com.projectmanager.dto.step.StepUpdateDto;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.Step;
import com.projectmanager.exception.NotFoundException;
import com.projectmanager.exception.UnauthorizedException;
import com.projectmanager.mapper.StepMapper;
import com.projectmanager.repository.StepRepository;
import com.projectmanager.specification.StepSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StepService {

    private static final Logger logger = LoggerFactory.getLogger(StepService.class);

    private final StepRepository stepRepository;
    private final StepMapper stepMapper;
    private final ProjectService projectService;

    public StepService(StepRepository stepRepository, StepMapper stepMapper, ProjectService projectService) {
        this.stepRepository = stepRepository;
        this.stepMapper = stepMapper;
        this.projectService = projectService;
    }

    public StepReadDto createStep(StepCreateDto dto, UUID projectId, UUID currentUserId) {
        Project project = projectService.getProjectEntityById(projectId);

        if (!project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to create steps in this project");
        }

        Step step = stepMapper.toEntity(dto);
        step.setId(UUID.randomUUID());
        step.setProject(project);
        step.setDeleted(false);

        Step savedStep = stepRepository.save(step);
        logger.info("Step created with ID: {} for project {}", savedStep.getId(), projectId);

        return stepMapper.toReadDto(savedStep);
    }

    public StepReadDto updateStep(UUID id, StepUpdateDto dto, UUID currentUserId) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Step not found with ID: " + id));

        if (!step.getProject().getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to update this step");
        }

        stepMapper.updateEntity(step, dto);
        Step updatedStep = stepRepository.save(step);
        logger.info("Step updated with ID: {}", updatedStep.getId());

        return stepMapper.toReadDto(updatedStep);
    }

    public void deleteStep(UUID id, UUID currentUserId) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Step not found with ID: " + id));

        if (!step.getProject().getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to delete this step");
        }

        step.setDeleted(true);
        stepRepository.save(step);
        logger.info("Step logically deleted with ID: {}", id);
    }

    public StepReadDto getStepById(UUID id, UUID currentUserId) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Step not found with ID: " + id));

        if (!step.getProject().getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to view this step");
        }

        return stepMapper.toReadDto(step);
    }

    public Page<StepReadDto> getStepsByProjectId(UUID projectId, Pageable pageable, String name, UUID currentUserId) {
        Project project = projectService.getProjectEntityById(projectId);

        if (!project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to view steps in this project");
        }

        var spec = StepSpecification.hasProjectId(projectId)
                .and(StepSpecification.hasNameLike(name));

        return stepRepository.findAll(spec, pageable)
                .map(stepMapper::toReadDto);
    }

    public Step getStepEntityById(UUID id) {
        return stepRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Step not found with ID: " + id));
    }
}
