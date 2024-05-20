package com.alatoo.CodeWars.dto.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {
    private Long id;
    private String author;
    private String text;
    private Double rating;
}
