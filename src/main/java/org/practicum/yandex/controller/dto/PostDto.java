package org.practicum.yandex.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
