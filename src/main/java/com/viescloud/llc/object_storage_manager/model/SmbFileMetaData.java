package com.viescloud.llc.object_storage_manager.model;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SmbFileMetaData extends ObjectStorageData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    public static SmbFileMetaData fromMultipartFile(MultipartFile file, int userId, byte[] data, boolean publicity) {
        var metaData = SmbFileMetaData.builder()
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
