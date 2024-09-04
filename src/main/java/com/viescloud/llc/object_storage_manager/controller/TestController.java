package com.viescloud.llc.object_storage_manager.controller;

import org.springframework.web.bind.annotation.RestController;

import com.viescloud.llc.object_storage_manager.dao.S3FileMetaDataDao;
import com.viescloud.llc.object_storage_manager.model.S3FileMetaData;
import com.vincent.inc.viesspringutils.model.UserPermission;
import com.vincent.inc.viesspringutils.model.UserPermissionEnum;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
public class TestController {

    @Autowired
    S3FileMetaDataDao s3FileMetaDataDao;

    Random random = new Random();

    @GetMapping("test1")
    public List<S3FileMetaData> test1() {
        for (int i = 0; i < 5; i++) {
            var data = new S3FileMetaData();
            data.setData("data " + random.nextInt(10000));
            data.setOwnerUserId(1);
            data.setSharedUsers(List.of(new UserPermission(2, List.of(UserPermissionEnum.READ)), new UserPermission(3, List.of(UserPermissionEnum.READ))));
            this.s3FileMetaDataDao.save(data);
        }

        return s3FileMetaDataDao.findAll();
    }

    @GetMapping("test2/{ownerUserId}")
    public List<S3FileMetaData> test2(@PathVariable int ownerUserId) {
        for (int i = 0; i < 5; i++) {
            var data = new S3FileMetaData();
            data.setData("data " + random.nextInt(10000));
            data.setOwnerUserId(ownerUserId);
            data.setSharedUsers(List.of(new UserPermission(2, List.of(UserPermissionEnum.READ)), new UserPermission(3, List.of(UserPermissionEnum.READ))));
            this.s3FileMetaDataDao.save(data);
        }

        return s3FileMetaDataDao.findAllByOwnerUserId(ownerUserId);
    }
    
    @GetMapping("test3/{relatedUserId}")
    public List<S3FileMetaData> test3(@PathVariable int relatedUserId) {
        for (int i = 0; i < 5; i++) {
            var data = new S3FileMetaData();
            data.setData("data " + random.nextInt(10000));
            data.setOwnerUserId(random.nextInt(10000));
            data.setSharedUsers(List.of(new UserPermission(relatedUserId, List.of(UserPermissionEnum.READ)), new UserPermission(random.nextInt(1000), List.of(UserPermissionEnum.READ))));
            this.s3FileMetaDataDao.save(data);
        }

        return this.s3FileMetaDataDao.findAllRelatedSharedUserId(relatedUserId);
    }
}
