package com.viescloud.llc.object_storage_manager.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.viescloud.llc.object_storage_manager.dao.S3FileMetaDataDao;
import com.viescloud.llc.object_storage_manager.model.S3FileMetaData;
import com.vincent.inc.viesspringutils.exception.HttpResponseThrowers;
import com.vincent.inc.viesspringutils.util.DatabaseCall;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class S3FileMetaDataService extends ObjectStorageService<S3FileMetaData, Integer, S3FileMetaDataDao> {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.s3.bucket}") 
    private String bucket;

    public S3FileMetaDataService(DatabaseCall<S3FileMetaData, Integer> databaseCall, S3FileMetaDataDao repositoryDao) {
        super(databaseCall, repositoryDao);
    }

    @Override
    protected S3FileMetaData newEmptyObject() {
        return new S3FileMetaData();
    }

    @Override
    protected void checkIfFileDirectoryExist(String path) {
        return;
    }

    @Override
    protected void moveFileOnStorage(String originalPath, String newPath) {
        try {
            minioClient.copyObject(
                CopyObjectArgs.builder()
                .bucket(bucket)
                .object(newPath)
                .source(
                    CopySource.builder()
                    .bucket(bucket)
                    .object(originalPath)
                    .build()
                ).build()
            );

            this.removeObjectFromStorage(originalPath);
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException ex) {
            log.error(ex.getMessage(), ex);
            HttpResponseThrowers.throwServerError("Server experience unexpected error while moving file");
        }
    }

    @Override
    protected byte[] readRawOnStorage(String path) {
        try {
            var response = this.minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(path).build());
            return response.readAllBytes();
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException ex) {
            log.error(ex.getMessage(), ex);
            return (byte[]) HttpResponseThrowers.throwServerError("Server experience unexpected error while reading file");
        }
    }

    @Override
    protected boolean isFileExistOnStorage(String path) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(path).build());
            return true;
        } catch (ErrorResponseException ex) {
            return false;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return (boolean) HttpResponseThrowers.throwServerError("Server experience unexpected error while reading file");
        }
        
    }

    @Override
    protected void writeOnStorage(byte[] data, String path) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            this.minioClient.putObject(
                PutObjectArgs.builder().bucket(bucket).object(path).stream(
                        bais, bais.available(), -1)
                    .build());
            bais.close();
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException ex) {
            log.error(ex.getMessage(), ex);
            HttpResponseThrowers.throwServerError("Server experience unexpected error while writing file");
        }
    }
    
    private void removeObjectFromStorage(String path) {
        try {
            this.minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(path).build());
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException ex) {
            log.error(ex.getMessage(), ex);
            HttpResponseThrowers.throwServerError("Server experience unexpected error when removing file");
        }
    }
}
