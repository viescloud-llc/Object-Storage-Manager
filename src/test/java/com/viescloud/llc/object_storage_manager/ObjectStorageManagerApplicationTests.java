package com.viescloud.llc.object_storage_manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.viescloud.llc.object_storage_manager.controller.S3FileMetaDataController;
import com.viescloud.llc.viesspringutils.util.WebCall;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;

import java.util.UUID;

@SpringBootTest(classes = ObjectStorageManagerApplication.class)
class ObjectStorageManagerApplicationTests {

    @Autowired
    private S3FileMetaDataController s3FileMetaDataController;

    @Autowired
    private RestTemplate restTemplate;

    // @Test
    public void test() throws IOException {
        URI Uri = URI.create("http://10.24.24.2:8385/public/Videos/videoplayback2.mp4");

        var data = WebCall.of(restTemplate, byte[].class)
                          .skipRestClientError(true)
                          .logRequest(true)
                          .get(Uri, null)
                          .orElse(null);

        assertNotNull(data);

        MultipartFile mockFile = new MockMultipartFile(
            "file",                            // Name of the form field
            UUID.randomUUID().toString() + ".mp4",                     // Original file name
            "video/mp4",                       // Content type
            data                         // File content as byte array
        );

        var response = this.s3FileMetaDataController.uploadFile(-1, mockFile, false);

        assertNotNull(response);

        var response2 = this.s3FileMetaDataController.getFileById(-1, null, null, response.getId(), null, null, null, null, null);
        assertNotNull(response2);
        assertTrue(response2.getStatusCode().is2xxSuccessful());
        assertNotNull(response2.getBody());

        Uri = URI.create("http://10.24.24.2:8385/public/Videos/videoplayback.mp4");

        var data2 = WebCall.of(restTemplate, byte[].class)
                          .skipRestClientError(true)
                          .logRequest(true)
                          .get(Uri, null)
                          .orElse(null);

        assertNotNull(data2);

        MultipartFile mockFile2 = new MockMultipartFile(
            "file",                            // Name of the form field
            UUID.randomUUID().toString() + ".mp4",                     // Original file name
            "video/mp4",                       // Content type
            data2                         // File content as byte array
        );

        var response3 = this.s3FileMetaDataController.updateFile(-1, null, null, response.getId(), mockFile2, false);
        assertNotNull(response3);

        var response4 = this.s3FileMetaDataController.getFileById(-1, null, null, response3.getId(), null, null, null, null, null);
        assertNotNull(response4);
        assertTrue(response4.getStatusCode().is2xxSuccessful());
        assertNotNull(response4.getBody());
    }
}
