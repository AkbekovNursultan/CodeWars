package com.alatoo.CodeWars.dto.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewTaskRequest {
    private String name;
    private String description;
    private String difficulty;
}
