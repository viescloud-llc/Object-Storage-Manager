package com.viescloud.llc.object_storage_manager.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.viescloud.llc.object_storage_manager.model.DatabaseFileMetaData;
import com.viescloud.llc.object_storage_manager.service.DatabaseFileMetaDataService;
import com.viescloud.llc.object_storage_manager.service.ObjectStorageService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("database/v1")
public class DatabaseFileMetaDataController extends ObjectStorageController<Integer, DatabaseFileMetaData, DatabaseFileMetaDataService> {

    public DatabaseFileMetaDataController(ObjectStorageService<Integer, DatabaseFileMetaData, ?> objectStorageService) {
        super(objectStorageService);
    }

    @Override
    protected DatabaseFileMetaData fromMultipartFile(MultipartFile file, int userId, byte[] data, boolean publicity) {
        var metaData = DatabaseFileMetaData.builder()
                           .originalFilename(file.getOriginalFilename())
                           .contentType(file.getContentType())
                           .size(file.getSize())
                           .ownerUserId(userId)
                           .publicity(publicity)
                           .path(String.format("/%s/%s", userId, file.getOriginalFilename()))
                           .data(data)
                           .build();

        return metaData;
    }
    
}
