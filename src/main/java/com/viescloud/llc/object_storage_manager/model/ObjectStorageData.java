package com.viescloud.llc.object_storage_manager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vincent.inc.viesspringutils.model.UserAccess;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
public abstract class ObjectStorageData extends UserAccess {
    @Column
    private String originalFilename;
    
    @Column
    private String contentType;
    
    @Column
    private Long size;
    
    @Column(unique = true)
    private String path;

    @Column(columnDefinition = "BIT(1) default false")
    private Boolean publicity;

    @JsonIgnore
    @Transient
    private byte[] data;
}
