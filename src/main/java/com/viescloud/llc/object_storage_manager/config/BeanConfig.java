package com.viescloud.llc.object_storage_manager.config;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.smb.session.SmbSessionFactory;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import jcifs.DialectVersion;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class BeanConfig {

    @Bean
    public SmbSessionFactory smbSessionFactory(
            @Value("${smb.host}") String host,
            @Value("${smb.port}") int port,
            @Value("${smb.domain}") String domain,
            @Value("${smb.username}") String username,
            @Value("${smb.password}") String password,
            @Value("${smb.shareAndDir}") String shareAndDir) {
        SmbSessionFactory smbSessionFactory = new SmbSessionFactory();
        smbSessionFactory.setHost(host);
        smbSessionFactory.setPort(port);
        smbSessionFactory.setDomain(domain);
        smbSessionFactory.setUsername(username);
        smbSessionFactory.setPassword(password);
        smbSessionFactory.setShareAndDir(shareAndDir);
        smbSessionFactory.setSmbMinVersion(DialectVersion.SMB210);
        smbSessionFactory.setSmbMaxVersion(DialectVersion.SMB311);
        return smbSessionFactory;
    }

    @Bean
    public MinioAsyncClient minioAsyncClient(
            @Value("${minio.s3.local.host}") String host,
            @Value("${minio.s3.username}") String username,
            @Value("${minio.s3.password}") String password,
            @Value("${minio.s3.bucket}") String bucket) {
        MinioAsyncClient client = MinioAsyncClient.builder()
                .endpoint(host)
                .credentials(username, password)
                .build();
        checkIfBucketExist(client, bucket);
        return client;
    }

    @Bean
    public MinioClient minioClient(
            @Value("${minio.s3.local.host}") String host,
            @Value("${minio.s3.username}") String username,
            @Value("${minio.s3.password}") String password,
            @Value("${minio.s3.bucket}") String bucket) {
        MinioClient client = MinioClient.builder()
                .endpoint(host)
                .credentials(username, password)
                .build();
        checkIfBucketExist(client, bucket);
        return client;
    }

    private void checkIfBucketExist(MinioAsyncClient client, String bucket) {
        try {
            CompletableFuture<Boolean> exist = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            exist.thenRun(() -> {
                try {
                    if (!exist.get()) {
                        try {
                            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                        } catch (InvalidKeyException | InsufficientDataException | InternalException
                                | NoSuchAlgorithmException | XmlParserException | IllegalArgumentException
                                | IOException ex) {
                            log.error(ex.getMessage(), ex);
                            throw new RuntimeException(ex);
                        }
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    log.error(ex.getMessage(), ex);
                    throw new RuntimeException(ex);
                }
            });
        } catch (InvalidKeyException | InsufficientDataException | InternalException | NoSuchAlgorithmException
                | XmlParserException | IllegalArgumentException | IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    private void checkIfBucketExist(MinioClient client, String bucket) {
        try {
            boolean exist = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exist)
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
    }

}
