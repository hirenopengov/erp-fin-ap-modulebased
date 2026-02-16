package com.opengov.erp.ap.common.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T, ID> {

    protected abstract JpaRepository<T, ID> getRepository();

    public List<T> findAll() {
        return getRepository().findAll();
    }

    public Page<T> findAll(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }

    public T save(T entity) {
        return getRepository().save(entity);
    }

    public List<T> saveAll(Iterable<T> entities) {
        return getRepository().saveAll(entities);
    }

    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }

    public void delete(T entity) {
        getRepository().delete(entity);
    }

    public void deleteAll(Iterable<T> entities) {
        getRepository().deleteAll(entities);
    }

    public long count() {
        return getRepository().count();
    }

    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }
}
