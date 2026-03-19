package org.practicum.yandex.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.practicum.yandex.controller.dto.PostDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPostListResponse {
    private List<PostDto> posts;
    boolean hasPrev;
    boolean hasNext;
    Long lastPage;
}
