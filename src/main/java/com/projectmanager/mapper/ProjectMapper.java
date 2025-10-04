package com.projectmanager.mapper;

import com.projectmanager.dto.project.ProjectCreateDto;
import com.projectmanager.dto.project.ProjectReadDto;
import com.projectmanager.dto.project.ProjectUpdateDto;
import com.projectmanager.entity.Project;
import com.projectmanager.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "steps", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toEntity(ProjectCreateDto dto);

    @Mapping(target = "userIds", source = "users", qualifiedByName = "usersToIds")
    ProjectReadDto toReadDto(Project entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "steps", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Project entity, ProjectUpdateDto dto);

    @Named("usersToIds")
    default Set<UUID> usersToIds(Set<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
    }

}
