package com.alatoo.CodeWars.dto.task;

import com.alatoo.CodeWars.entities.TaskFile;
import com.alatoo.CodeWars.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class TaskDetailsResponse {
    private Long id;
    private String name;
    private String description;
    private List<TaskFile> taskFiles;
    private String difficulty;
    private Integer points;
    private Integer solved;
    private User createdBy;
    private Boolean verified;
}
