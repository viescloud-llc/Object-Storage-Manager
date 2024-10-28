package com.viescloud.llc.object_storage_manager.dao;

import com.viescloud.llc.object_storage_manager.model.S3FileMetaData;
import com.viescloud.llc.viesspringutils.dao.ViesUserAccessJpaRepositoryTemplate;

public interface S3FileMetaDataDao extends ObjectStorageDao<S3FileMetaData, Integer> {}
class S3FileMetaDataDaoImpl extends ViesUserAccessJpaRepositoryTemplate<S3FileMetaData> {}