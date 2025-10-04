package com.projectmanager.service;

import com.projectmanager.dto.project.ProjectCreateDto;
import com.projectmanager.dto.project.ProjectReadDto;
import com.projectmanager.dto.project.ProjectUpdateDto;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.User;
import com.projectmanager.exception.NotFoundException;
import com.projectmanager.exception.UnauthorizedException;
import com.projectmanager.mapper.ProjectMapper;
import com.projectmanager.repository.ProjectRepository;
import com.projectmanager.specification.ProjectSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper, UserService userService) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userService = userService;
    }

    public ProjectReadDto createProject(ProjectCreateDto dto, UUID currentUserId) {
        Project project = projectMapper.toEntity(dto);
        project.setId(UUID.randomUUID());
        project.setDeleted(false);

        User currentUser = userService.getUserEntityById(currentUserId);
        project.getUsers().add(currentUser);

        Project savedProject = projectRepository.save(project);
        logger.info("Project created with ID: {}", savedProject.getId());

        return projectMapper.toReadDto(savedProject);
    }

    public ProjectReadDto updateProject(UUID id, ProjectUpdateDto dto, UUID currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + id));

        if (!project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to update this project");
        }

        projectMapper.updateEntity(project, dto);
        Project updatedProject = projectRepository.save(project);
        logger.info("Project updated with ID: {}", updatedProject.getId());

        return projectMapper.toReadDto(updatedProject);
    }

    public void deleteProject(UUID id, UUID currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + id));

        if (!project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to delete this project");
        }

        project.setDeleted(true);
        projectRepository.save(project);
        logger.info("Project logically deleted with ID: {}", id);
    }

    public ProjectReadDto getProjectById(UUID id, UUID currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + id));

        if (!project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to view this project");
        }

        return projectMapper.toReadDto(project);
    }

    public Page<ProjectReadDto> getAllProjects(Pageable pageable, String name, String description, UUID userId, UUID currentUserId) {
        var spec = ProjectSpecification.hasNameLike(name)
                .and(ProjectSpecification.hasDescriptionLike(description))
                .and(ProjectSpecification.hasUserId(userId));

        Page<Project> projects = projectRepository.findAll(spec, pageable);
        List<ProjectReadDto> filteredProjects = projects.getContent().stream()
                .filter(project -> project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId)))
                .map(projectMapper::toReadDto)
                .collect(Collectors.toList());

        return new PageImpl<>(filteredProjects, pageable, projects.getTotalElements());
    }

    public ProjectReadDto addUserToProject(UUID projectId, UUID userId, UUID currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectId));

        if (!project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to modify this project");
        }

        User user = userService.getUserEntityById(userId);
        project.getUsers().add(user);

        Project updatedProject = projectRepository.save(project);
        logger.info("User {} added to project {}", userId, projectId);

        return projectMapper.toReadDto(updatedProject);
    }

    public ProjectReadDto removeUserFromProject(UUID projectId, UUID userId, UUID currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + projectId));

        if (!project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to modify this project");
        }

        project.getUsers().removeIf(user -> user.getId().equals(userId));
        Project updatedProject = projectRepository.save(project);
        logger.info("User {} removed from project {}", userId, projectId);

        return projectMapper.toReadDto(updatedProject);
    }

    public Project getProjectEntityById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found with ID: " + id));
    }

}
