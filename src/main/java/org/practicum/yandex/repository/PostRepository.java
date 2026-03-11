package org.practicum.yandex.repository;

import org.practicum.yandex.persistence.post.PostModel;

import java.math.BigInteger;
import java.util.List;

public interface PostRepository extends SqlRepository<BigInteger, PostModel> {
    void saveTags(final BigInteger postId, final List<BigInteger> tagIds);
    List<BigInteger> getOrInsertTags(final List<String> tagNames);
    List<PostModel> findPaged(final int page, final int size, String query);
    void removePostTags(final BigInteger postId);
}
