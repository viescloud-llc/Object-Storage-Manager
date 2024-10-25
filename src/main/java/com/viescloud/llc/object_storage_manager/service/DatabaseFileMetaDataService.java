package com.viescloud.llc.object_storage_manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.viescloud.llc.object_storage_manager.dao.DatabaseFileDao;
import com.viescloud.llc.object_storage_manager.dao.DatabaseFileMetaDataDao;
import com.viescloud.llc.object_storage_manager.model.DatabaseFile;
import com.viescloud.llc.object_storage_manager.model.DatabaseFileMetaData;
import com.vincent.inc.viesspringutils.util.DatabaseCall;

@Service
public class DatabaseFileMetaDataService extends ObjectStorageService<DatabaseFileMetaData, Integer, DatabaseFileMetaDataDao> {

    @Autowired
    private DatabaseFileDao databaseFileDao;

    public DatabaseFileMetaDataService(DatabaseCall<DatabaseFileMetaData, Integer> databaseCall,
            DatabaseFileMetaDataDao repositoryDao, DatabaseCall<Boolean, String> fetchFileFlagCache) {
        super(databaseCall, repositoryDao, fetchFileFlagCache);
    }

    @Override
    protected void checkIfFileDirectoryExist(String path) {
        return;
    }

    @Override
    protected void moveFileOnStorage(String originalPath, String newPath) {
        var file = this.databaseFileDao.findByPath(originalPath);
        file.setPath(newPath);
        this.databaseFileDao.saveAndFlush(file);
    }

    @Override
    protected byte[] readRawOnStorage(String path) {
        var file = this.databaseFileDao.findByPath(path);
        return file.getData();
    }

    @Override
    protected boolean isFileExistOnStorage(String path) {
        return this.databaseFileDao.existsByPath(path);
    }

    @Override
    protected void writeOnStorage(byte[] data, String path) {
        var file = new DatabaseFile();
        file.setPath(path);
        file.setData(data);
        this.databaseFileDao.saveAndFlush(file);
    }

    @Override
    protected DatabaseFileMetaData newEmptyObject() {
        return new DatabaseFileMetaData();
    }

    @Override
    public void replaceOnStorage(byte[] data, String path) {
        var file = this.databaseFileDao.findByPath(path);
        file.setData(data);
        this.databaseFileDao.saveAndFlush(file);
    }
    
}
