package org.practicum.yandex.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import org.practicum.yandex.controller.dto.PostDto;

import java.util.List;

@Data
@Builder
public class GetPostListResponse {
    private List<PostDto> posts;
    boolean hasPrev;
    boolean hasNext;
    Long lastPage;
}
