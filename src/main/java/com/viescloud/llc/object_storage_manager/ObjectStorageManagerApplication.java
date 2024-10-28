package com.viescloud.llc.object_storage_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RestController;

import com.viescloud.llc.viesspringutils.ViesApplication;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class ObjectStorageManagerApplication extends ViesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObjectStorageManagerApplication.class, args);
	}

	@Override
	public String getApplicationName() {
		return "object-storage-manager";
	}

}
