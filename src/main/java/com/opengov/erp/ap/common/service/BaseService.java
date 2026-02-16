package com.opengov.erp.ap.common.service;

import com.opengov.erp.ap.common.context.TenantContext;
import com.opengov.erp.ap.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T, ID> {

    protected abstract JpaRepository<T, ID> getRepository();

    protected BaseRepository<T, ID> getBaseRepository() {
        if (getRepository() instanceof BaseRepository) {
            return (BaseRepository<T, ID>) getRepository();
        }
        throw new UnsupportedOperationException("Repository must extend BaseRepository for multitenancy support");
    }

    public List<T> findAll() {
        return getBaseRepository().findAllByEntityId(TenantContext.getCurrentTenant());
    }

    public Page<T> findAll(Pageable pageable) {
        // Note: Page queries need custom implementation for entity_id filtering
        // For now, using findAll and manual pagination
        List<T> all = findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), all.size());
        return new PageImpl<>(all.subList(start, end), pageable, all.size());
    }

    public Optional<T> findById(ID id) {
        return getBaseRepository().findByIdAndEntityId(id, TenantContext.getCurrentTenant());
    }

    public T save(T entity) {
        return getRepository().save(entity);
    }

    public List<T> saveAll(Iterable<T> entities) {
        return getRepository().saveAll(entities);
    }

    public void deleteById(ID id) {
        Optional<T> entity = findById(id);
        entity.ifPresent(this::delete);
    }

    public void delete(T entity) {
        getRepository().delete(entity);
    }

    public void deleteAll(Iterable<T> entities) {
        getRepository().deleteAll(entities);
    }

    public long count() {
        return getBaseRepository().countByEntityId(TenantContext.getCurrentTenant());
    }

    public boolean existsById(ID id) {
        return getBaseRepository().existsByIdAndEntityId(id, TenantContext.getCurrentTenant());
    }
}
