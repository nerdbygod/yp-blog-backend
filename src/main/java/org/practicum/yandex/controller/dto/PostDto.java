package org.practicum.yandex.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
public class PostDto {
    private BigInteger id;
    private String title;
    private String text;
    private List<String> tags;

    @Builder.Default
    private int likesCount = 0;

    @Builder.Default
    private int commentsCount = 0;
}
