package com.alatoo.CodeWars.dto.task;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchTaskRequest {
    private String name;
    private String difficulty;
    private List<String> tags;
    private Boolean isSolved;
    private String sortBy;
}
