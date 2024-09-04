package com.viescloud.llc.object_storage_manager.dao;

import com.viescloud.llc.object_storage_manager.model.S3FileMetaData;
import com.vincent.inc.viesspringutils.dao.ViesUserAccessJpaRepository;
import com.vincent.inc.viesspringutils.dao.ViesUserAccessJpaRepositoryTemplate;

public interface S3FileMetaDataDao extends ViesUserAccessJpaRepository<S3FileMetaData, Integer> {}
class S3FileMetaDataDaoImpl extends ViesUserAccessJpaRepositoryTemplate<S3FileMetaData> {}