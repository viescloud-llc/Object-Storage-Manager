package com.viescloud.llc.object_storage_manager.model;

import com.vincent.inc.viesspringutils.model.UserAccess;

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
public class S3FileMetaData extends UserAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
}
