package com.viescloud.llc.object_storage_manager.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.viescloud.llc.object_storage_manager.model.S3FileMetaData;
import com.viescloud.llc.object_storage_manager.service.ObjectStorageService;
import com.viescloud.llc.object_storage_manager.service.S3FileMetaDataService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/s3/v1")
public class S3FileMetaDataController extends ObjectStorageController<Integer, S3FileMetaData, S3FileMetaDataService> {

    public S3FileMetaDataController(ObjectStorageService<Integer, S3FileMetaData, ?> objectStorageService) {
        super(objectStorageService);
    }

    @Override
    protected S3FileMetaData fromMultipartFile(MultipartFile file, int userId, byte[] data, boolean publicity) {
        var metaData = S3FileMetaData.builder()
                           .originalFilename(file.getOriginalFilename())
                           .contentType(file.getContentType())
                           .size(file.getSize())
                           .ownerUserId(userId)
                           .publicity(publicity)
                           .path(String.format("/%s/%s", userId, file.getOriginalFilename()))
                           .data(data)
                           .build();

        return this.objectStorageService.formatMetaData(metaData);
    }

}
