package org.practicum.yandex.controller.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest {
    private Long postId;
    private String text;
}
