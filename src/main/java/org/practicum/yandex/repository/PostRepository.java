package org.practicum.yandex.repository;

import org.practicum.yandex.persistence.post.PostModel;

import java.math.BigInteger;
import java.util.List;

public interface PostRepository extends SqlRepository<BigInteger, PostModel> {
    List<PostModel> findPaged(final int page, final int size, String query);
}
