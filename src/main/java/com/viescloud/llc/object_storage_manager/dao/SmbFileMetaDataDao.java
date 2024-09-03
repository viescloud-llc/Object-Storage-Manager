package com.viescloud.llc.object_storage_manager.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.viescloud.llc.object_storage_manager.model.SmbFileMetaData;
import com.vincent.inc.viesspringutils.dao.ViesJpaRepository;

@Repository
public interface SmbFileMetaDataDao extends ViesJpaRepository<SmbFileMetaData, Integer> {
    public List<SmbFileMetaData> findAllByOriginalFilename(String originalFilename);

	public SmbFileMetaData findByContentType(String contentType);
	public List<SmbFileMetaData> findAllByContentType(String contentType);

	public SmbFileMetaData findByPath(String path);
	public List<SmbFileMetaData> findAllByPath(String path);

	public SmbFileMetaData findByPublicity(boolean publicity);
	public List<SmbFileMetaData> findAllByPublicity(boolean publicity);

	@Query(value = "select * from file_meta_data f where f.path like ?1", nativeQuery = true)
	public List<SmbFileMetaData> findAllByUserId(String id);
}
