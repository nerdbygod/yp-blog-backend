package org.practicum.yandex.persistence.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostModel {
    private BigInteger id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String title;
    private String content;
}
