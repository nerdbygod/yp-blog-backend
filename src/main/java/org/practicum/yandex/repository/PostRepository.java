package org.practicum.yandex.repository;

import org.practicum.yandex.persistence.post.PostModel;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public interface PostRepository extends SqlRepository<BigInteger, PostModel> {
    void saveTags(final BigInteger postId, final List<BigInteger> tagIds);
    List<BigInteger> getOrInsertTags(final List<String> tagNames);
    Page<PostModel> findPaged(final long page, final long size, String query, Set<String> tags);
    void removePostTags(final BigInteger postId);
    BigInteger incrementLikes(final BigInteger postId);
}
