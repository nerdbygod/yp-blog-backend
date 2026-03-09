package org.practicum.yandex.repository;

import java.util.List;

public interface SqlRepository<ID, T> {
    T findById(final ID id);

    List<T> findAll(final int page, final int size);

    T create(final T postModel);

    T update(final T updatedModel);

    void deleteById(final ID id);
}
