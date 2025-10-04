package com.projectmanager.service;

import com.projectmanager.dto.task.TaskCreateDto;
import com.projectmanager.dto.task.TaskReadDto;
import com.projectmanager.dto.task.TaskUpdateDto;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.Step;
import com.projectmanager.entity.Task;
import com.projectmanager.entity.TaskStatus;
import com.projectmanager.exception.BadRequestException;
import com.projectmanager.exception.NotFoundException;
import com.projectmanager.exception.UnauthorizedException;
import com.projectmanager.mapper.TaskMapper;
import com.projectmanager.repository.TaskRepository;
import com.projectmanager.specification.TaskSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final StepService stepService;
    private final UserService userService;
    private final ProjectService projectService;
    private final EmailService emailService;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, StepService stepService, 
                       UserService userService, ProjectService projectService, EmailService emailService) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.stepService = stepService;
        this.userService = userService;
        this.projectService = projectService;
        this.emailService = emailService;
    }

    public TaskReadDto createTask(TaskCreateDto dto, UUID currentUserId) throws Exception {
        Step step = stepService.getStepEntityById(dto.getStepId());
        Project project = step.getProject();

        if (!project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to create tasks in this project");
        }

        if (dto.getAssignedTo() != null && 
            !project.getUsers().stream().anyMatch(user -> user.getId().equals(dto.getAssignedTo()))) {
            throw new BadRequestException("Assigned user must be a member of the project");
        }

        Task task = taskMapper.toEntity(dto);
        task.setId(UUID.randomUUID());
        task.setStep(step);
        task.setStatus(TaskStatus.TODO);
        task.setDeleted(false);

        if (dto.getAssignedTo() != null) {
            var assignedUser = userService.getUserEntityById(dto.getAssignedTo());
            task.setAssignedTo(assignedUser);
        }

        Task savedTask = taskRepository.save(task);
        logger.info("Task created with ID: {} in project {}", savedTask.getId(), project.getId());

        if (task.getAssignedTo() != null) {
            emailService.sendTaskAssignmentEmail(task.getAssignedTo().getEmail(), task.getTitle(), project.getName());
            logger.info("Task assignment email sent to: {}", task.getAssignedTo().getEmail());
        }

        return taskMapper.toReadDto(savedTask);
    }

    public TaskReadDto updateTask(UUID id, TaskUpdateDto dto, UUID currentUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + id));

        Project project = task.getStep().getProject();

        if (!project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to update this task");
        }

        if (dto.getAssignedTo() != null && 
            !project.getUsers().stream().anyMatch(user -> user.getId().equals(dto.getAssignedTo()))) {
            throw new BadRequestException("Assigned user must be a member of the project");
        }

        taskMapper.updateEntity(task, dto);
        if (dto.getAssignedTo() != null) {
            var assignedUser = userService.getUserEntityById(dto.getAssignedTo());
            task.setAssignedTo(assignedUser);
        }

        Task updatedTask = taskRepository.save(task);
        logger.info("Task updated with ID: {}", updatedTask.getId());

        return taskMapper.toReadDto(updatedTask);
    }

    public void deleteTask(UUID id, UUID currentUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + id));

        if (!task.getStep().getProject().getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to delete this task");
        }

        task.setDeleted(true);
        taskRepository.save(task);
        logger.info("Task logically deleted with ID: {}", id);
    }

    public TaskReadDto getTaskById(UUID id, UUID currentUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + id));

        if (!task.getStep().getProject().getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to view this task");
        }

        return taskMapper.toReadDto(task);
    }

    public Page<TaskReadDto> getTasksByProjectId(UUID projectId, Pageable pageable, String title, String description, 
                                                  UUID assignedTo, UUID stepId, TaskStatus status, UUID currentUserId) {
        Project project = projectService.getProjectEntityById(projectId);

        if (!project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to view tasks in this project");
        }

        var spec = TaskSpecification.hasProjectId(projectId)
                .and(TaskSpecification.hasTitleLike(title))
                .and(TaskSpecification.hasDescriptionLike(description))
                .and(TaskSpecification.hasAssignedTo(assignedTo))
                .and(TaskSpecification.hasStepId(stepId))
                .and(TaskSpecification.hasStatus(status));

        return taskRepository.findAll(spec, pageable)
                .map(taskMapper::toReadDto);
    }

    public TaskReadDto updateTaskStatus(UUID id, UUID currentUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + id));

        Project project = task.getStep().getProject();

        if (!project.getUsers().stream().anyMatch(user -> user.getId().equals(currentUserId))) {
            throw new UnauthorizedException("You are not authorized to update this task status");
        }

        TaskStatus currentStatus = task.getStatus();
        TaskStatus nextStatus;

        switch (currentStatus) {
            case TODO:
                nextStatus = TaskStatus.IN_PROGRESS;
                break;
            case IN_PROGRESS:
                nextStatus = TaskStatus.DONE;
                break;
            case DONE:
                throw new BadRequestException("Task is already in DONE status");
            default:
                throw new BadRequestException("Invalid current task status");
        }

        task.setStatus(nextStatus);
        Task updatedTask = taskRepository.save(task);
        logger.info("Task status updated to {} for ID: {}", nextStatus, id);

        return taskMapper.toReadDto(updatedTask);
    }

    public Task getTaskEntityById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + id));
    }
}
