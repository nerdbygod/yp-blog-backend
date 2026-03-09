package org.practicum.yandex.repository;

import java.util.List;

public interface SqlRepository<ID, T> {
    T findById(final ID id);

    List<T> findAll();

    T save(final T postModel);

    int deleteById(final ID id);
}
