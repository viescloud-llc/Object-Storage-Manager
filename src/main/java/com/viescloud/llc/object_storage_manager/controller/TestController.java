package com.viescloud.llc.object_storage_manager.controller;

import org.springframework.web.bind.annotation.RestController;

import com.viescloud.llc.object_storage_manager.dao.S3FileMetaDataDao;
import com.viescloud.llc.object_storage_manager.model.S3FileMetaData;
import com.vincent.inc.viesspringutils.model.UserPermission;
import com.vincent.inc.viesspringutils.model.UserPermissionEnum;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class TestController {

    @Autowired
    S3FileMetaDataDao s3FileMetaDataDao;

    @GetMapping("test1")
    public List<S3FileMetaData> getMethodName() {
        for (int i = 0; i < 5; i++) {
            var data = new S3FileMetaData();
            data.setData("data" + i);
            data.setOwnerUserId(1);
            data.setSharedUsers(List.of(new UserPermission(2, List.of(UserPermissionEnum.READ)), new UserPermission(3, List.of(UserPermissionEnum.READ))));
            this.s3FileMetaDataDao.save(data);
        }

        return this.s3FileMetaDataDao.findAllRelatedSharedUserId(2);
    }
    
}
