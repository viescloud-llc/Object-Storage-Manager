package com.viescloud.llc.object_storage_manager.service;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.springframework.integration.smb.session.SmbSession;
import org.springframework.integration.smb.session.SmbSessionFactory;
import org.springframework.stereotype.Service;
import com.viescloud.llc.object_storage_manager.dao.SmbFileMetaDataDao;
import com.viescloud.llc.object_storage_manager.model.SmbFileMetaData;
import com.viescloud.llc.viesspringutils.exception.HttpResponseThrowers;
import com.viescloud.llc.viesspringutils.repository.DatabaseCall;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmbFileMetaDataService extends ObjectStorageService<Integer, SmbFileMetaData, SmbFileMetaDataDao> {

    private final SmbSession smbSession;

    public SmbFileMetaDataService(DatabaseCall<Integer, SmbFileMetaData> databaseCall, SmbFileMetaDataDao repositoryDao,
            SmbSessionFactory smbSessionFactory) {
        super(databaseCall, repositoryDao);
        this.smbSession = smbSessionFactory.getSession();
    }

    @Override
    protected SmbFileMetaData newEmptyObject() {
        return new SmbFileMetaData();
    }

    @Override
    public void checkIfFileDirectoryExist(String path) {
        path = getDirectoryPathFromFilePath(path);
        try {
            if(!this.smbSession.isDirectory(path))
                this.smbSession.mkdir(path);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    protected void moveFileOnStorage(String originalPath, String newPath) {
        try {
            this.smbSession.rename(originalPath, newPath);
        } catch (IOException e1) {
            log.error(e1.getMessage(), e1);
            try {
                this.smbSession.remove(newPath);
            } catch (IOException e2) {
                log.error(e2.getMessage(), e2);
            }
            HttpResponseThrowers.throwServerError("Server experience unexpected error when moving file");
        }
    }

    @Override
    protected byte[] readRawOnStorage(String path) {
        try (var raw = this.smbSession.readRaw(path)) {
            var data = IOUtils.toByteArray(raw);
            return data;
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return (byte[]) HttpResponseThrowers.throwServerError("Server experience unexpected error while reading file");
        }
    }

    @Override
    protected boolean isFileExistOnStorage(String path) {
        try {
            return this.smbSession.isFile(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            HttpResponseThrowers.throwServerError("Server experience unexpected error while reading file");
            return false;
        }
    }

    @Override
    protected void writeOnStorage(byte[] data, String path) {
        try {
            this.smbSession.write(data, path);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            HttpResponseThrowers.throwServerError("Server experience unexpected error while writing file");
        }
    }

    @Override
    public void replaceOnStorage(byte[] data, String path) {
        try {
            this.smbSession.remove(path);
            this.smbSession.write(data, path);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            HttpResponseThrowers.throwServerError("Server experience unexpected error while writing file");
        }
    }
}
