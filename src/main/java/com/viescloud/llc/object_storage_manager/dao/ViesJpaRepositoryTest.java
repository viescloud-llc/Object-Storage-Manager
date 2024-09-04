package com.viescloud.llc.object_storage_manager.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vincent.inc.viesspringutils.model.UserAccess;

@Repository
public interface ViesJpaRepositoryTest<T extends UserAccess, I> extends JpaRepository<T, I> {

    public List<T> findAllByOwnerUserId(int ownerUserId);

    /**
     * Retrieves a list of all entities that have a shared user ID matching the given ID.
     *
     * @param  id  the ID of the shared user
     * @return     a list of entities that have a shared user ID matching the given ID
     */
    @Query(value = "select t from #{#entityName} t join t.sharedUsers.userId sharedUserId where (sharedUserId = :id)")
    public List<T> findAllRelatedSharedUserId(@Param("id") int id);
}
