package org.practicum.yandex.repository;

import org.practicum.yandex.persistence.post.PostModel;

import java.util.List;
import java.util.Set;

public interface PostRepository extends SqlRepository<Long, PostModel> {
    void saveTags(final Long postId, final List<Long> tagIds);
    List<Long> getOrInsertTags(final List<String> tagNames);
    Page<PostModel> findPaged(final long page, final long size, String query, Set<String> tags);
    void removePostTags(final Long postId);
    Long incrementLikes(final Long postId);
}
