package org.practicum.yandex.service.post.dto;

import lombok.Builder;
import lombok.Data;
import org.practicum.yandex.persistence.post.PostModel;

import java.util.Collection;

@Data
@Builder
public class PostDataWrapper {
    private Collection<PostModel> posts;
    private Long count;
    private boolean hasNext;
    private boolean hasPrev;
    private Long lastPage;
}
