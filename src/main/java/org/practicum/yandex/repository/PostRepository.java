package org.practicum.yandex.repository;

import org.practicum.yandex.persistence.post.PostModel;

import java.math.BigInteger;

public interface PostRepository extends SqlRepository<BigInteger, PostModel> {
}
