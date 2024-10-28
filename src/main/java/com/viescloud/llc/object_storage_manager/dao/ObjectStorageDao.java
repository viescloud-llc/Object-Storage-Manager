package com.viescloud.llc.object_storage_manager.dao;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;

import com.viescloud.llc.object_storage_manager.model.ObjectStorageData;
import com.viescloud.llc.viesspringutils.dao.ViesUserAccessJpaRepository;

@NoRepositoryBean
public interface ObjectStorageDao<T extends ObjectStorageData, I> extends ViesUserAccessJpaRepository<T, I> {
    public List<T> findAllByOriginalFilename(String originalFilename);

	public T findByContentType(String contentType);
	public List<T> findAllByContentType(String contentType);

	public T findByPath(String path);
	public List<T> findAllByPath(String path);

	public T findByPublicity(boolean publicity);
	public List<T> findAllByPublicity(boolean publicity);
}
