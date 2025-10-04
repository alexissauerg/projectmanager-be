package com.projectmanager.mapper;

import com.projectmanager.dto.task.TaskCreateDto;
import com.projectmanager.dto.task.TaskReadDto;
import com.projectmanager.dto.task.TaskUpdateDto;
import com.projectmanager.entity.Task;
import com.projectmanager.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "step", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(TaskCreateDto dto);

    @Mapping(target = "assignedToId", source = "assignedTo", qualifiedByName = "userToId")
    @Mapping(target = "stepId", source = "step.id")
    TaskReadDto toReadDto(Task entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "step", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Task entity, TaskUpdateDto dto);

    @Named("userToId")
    default UUID userToId(User user) {
        return user != null ? user.getId() : null;
    }

}
