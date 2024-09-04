package com.viescloud.llc.object_storage_manager.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.viescloud.llc.object_storage_manager.model.SmbFileMetaData;
import com.vincent.inc.viesspringutils.dao.ViesUserAccessJpaRepositoryTemplate;

public interface SmbFileMetaDataDao extends ObjectStorageDao<SmbFileMetaData, Integer> {
	@Query(value = "select * from file_meta_data f where f.path like ?1", nativeQuery = true)
	public List<SmbFileMetaData> findAllByUserId(String id);
}

class SmbFileMetaDataDaoImpl extends ViesUserAccessJpaRepositoryTemplate<SmbFileMetaData> {}