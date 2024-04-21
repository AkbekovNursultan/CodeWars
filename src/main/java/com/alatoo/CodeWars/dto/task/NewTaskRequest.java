package com.alatoo.CodeWars.dto.task;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NewTaskRequest {
    private String name;
    private String description;
    private String answer;
    private String difficulty;
    private List<String> hints;
}
