package com.opengov.erp.ap.common.repository;

import com.opengov.erp.ap.common.context.TenantContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    // Base repository interface for common repository methods
    // Extend this interface for custom base repository functionality
    
    @Query("SELECT e FROM #{#entityName} e WHERE e.entityId = :entityId")
    List<T> findAllByEntityId(@Param("entityId") String entityId);
    
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.entityId = :entityId")
    Optional<T> findByIdAndEntityId(@Param("id") ID id, @Param("entityId") String entityId);
    
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.entityId = :entityId")
    long countByEntityId(@Param("entityId") String entityId);
    
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id AND e.entityId = :entityId")
    boolean existsByIdAndEntityId(@Param("id") ID id, @Param("entityId") String entityId);
}
