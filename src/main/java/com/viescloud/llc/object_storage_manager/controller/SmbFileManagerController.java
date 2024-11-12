package com.viescloud.llc.object_storage_manager.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.viescloud.llc.object_storage_manager.model.SmbFileMetaData;
import com.viescloud.llc.object_storage_manager.service.ObjectStorageService;
import com.viescloud.llc.object_storage_manager.service.SmbFileMetaDataService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RestController
@RequestMapping("smb/v1")
public class SmbFileManagerController extends ObjectStorageController<Integer, SmbFileMetaData, SmbFileMetaDataService> {
    
    public SmbFileManagerController(ObjectStorageService<Integer, SmbFileMetaData, ?> objectStorageService) {
        super(objectStorageService);
    }

    @Override
    protected SmbFileMetaData fromMultipartFile(MultipartFile file, int userId, byte[] data, boolean publicity) {
        var metaData = SmbFileMetaData.builder()
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
