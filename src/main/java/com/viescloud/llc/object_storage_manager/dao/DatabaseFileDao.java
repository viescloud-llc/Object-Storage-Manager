package com.viescloud.llc.object_storage_manager.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viescloud.llc.object_storage_manager.model.DatabaseFile;

public interface DatabaseFileDao extends JpaRepository<DatabaseFile, Integer> {
    public DatabaseFile findByPath(String path);
    public boolean existsByPath(String path);
}
