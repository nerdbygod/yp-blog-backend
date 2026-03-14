package org.practicum.yandex.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostDto {
    private Long id;
    private String title;
    private String text;
    private List<String> tags;

    @Builder.Default
    private long likeCount = 0L;

    @Builder.Default
    private long commentCount = 0L;
}
