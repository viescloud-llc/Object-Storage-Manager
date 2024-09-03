package com.viescloud.llc.object_storage_manager.dao;

import com.viescloud.llc.object_storage_manager.model.S3FileMetaData;
import com.vincent.inc.viesspringutils.dao.ViesJpaRepository;

public interface S3FileMetaDataDao extends ViesJpaRepository<S3FileMetaData, Integer> {
    
}
