package com.viescloud.llc.object_storage_manager.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.viescloud.llc.object_storage_manager.model.ObjectStorageData;
import com.viescloud.llc.object_storage_manager.service.ObjectStorageService;
import com.viescloud.llc.object_storage_manager.util.ImageResizer;
import com.viescloud.llc.viesspringutils.exception.HttpResponseThrowers;
import com.viescloud.llc.viesspringutils.model.UserPermissionEnum;
import com.viescloud.llc.viesspringutils.util.ReflectionUtils;

import io.github.techgnious.IVCompressor;
import io.github.techgnious.dto.IVSize;
import io.github.techgnious.dto.ImageFormats;
import io.github.techgnious.dto.ResizeResolution;
import io.github.techgnious.dto.VideoFormats;
import io.github.techgnious.exception.VideoException;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public abstract class ObjectStorageController<I, T extends ObjectStorageData, S extends ObjectStorageService<I, T, ?>> {
    protected static final Map<String, VideoFormats> VIDEO_FORMATS = Map.of(
        "mp4", VideoFormats.MP4,
        "mkv", VideoFormats.MKV,
        "avi", VideoFormats.AVI,
        "mov", VideoFormats.MOV,
        "wmv", VideoFormats.WMV,
        "flv", VideoFormats.FLV
    );

    protected static final Map<String, ImageFormats> IMAGE_FORMATS = Map.of(
        "png", ImageFormats.PNG,
        // "jpg", ImageFormats.JPG, //TODO: fix this format to work
        "jpeg", ImageFormats.JPEG
    );

    protected ObjectStorageService<I, T, ?> objectStorageService;
    protected IVCompressor compressor;

    public ObjectStorageController(ObjectStorageService<I, T, ?> objectStorageService) { 
        this.objectStorageService = objectStorageService;
        this.compressor = new IVCompressor();
    }

    protected abstract T fromMultipartFile(MultipartFile file, int userId, byte[] data, boolean publicity);

    @GetMapping("file")
    @SuppressWarnings("unchecked")
    public ResponseEntity<byte[]> getFileByCriteria(
            @RequestHeader(required = false, defaultValue = "0") Integer user_id,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) I id,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height,
            @RequestParam(required = false) ResizeResolution resizeResolution,
            @RequestParam(required = false) VideoFormats videoFormat,
            @RequestParam(required = false, defaultValue = "png") String imageFormat) {
        
        this.validateInput(id, path, fileName);
        
        try (var metadata = this.objectStorageService.getFileByCriteria(id, path, fileName, user_id)) {
            if(!ObjectUtils.isEmpty(resizeResolution)) {
                width = resizeResolution.getWidth();
                height = resizeResolution.getHeight();
            }
            
            if (!ObjectUtils.isEmpty(width) && !ObjectUtils.isEmpty(height)) {
                if(metadata.getContentType().startsWith("image")) {
                    var imageFormatEnum = Optional.ofNullable(IMAGE_FORMATS.get(imageFormat.toLowerCase()))
                                                  .orElse(ImageFormats.PNG);
    
                    resizeImage(metadata, imageFormatEnum, width, height);
                }
                else if(metadata.getContentType().startsWith("video")) {
                    //TODO: resize video not supported yet
                    // resizeVideo(metadata, videoFormat, width, height);
                }
            }
    
            if (!ObjectUtils.isEmpty(metadata)) {
                this.objectStorageService.checkIsRelatedToUser(metadata, user_id, List.of(UserPermissionEnum.READ));
                return ResponseEntity.ok().header("Content-Type", metadata.getContentType()).body(metadata.getData());
            }
            else
                return (ResponseEntity<byte[]>) HttpResponseThrowers.throwNotFound("File not found");
        }
        catch(ResponseStatusException e) {
            throw e;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return (ResponseEntity<byte[]>) HttpResponseThrowers.throwServerError("Failed to get file");
        }
    }

    private void resizeImage(T metadata, ImageFormats imageFormat, int width, int height) {
        if(ObjectUtils.isEmpty(imageFormat)) {
            imageFormat = Optional.ofNullable(IMAGE_FORMATS.get(metadata.getContentType().split("/")[1].toLowerCase()))
                                  .orElse(ImageFormats.PNG);
        }

        var result = ImageResizer.resizeImage(metadata.getData(), width, height, imageFormat.getType())
                                 .orElseThrow(() -> HttpResponseThrowers.throwServerErrorException("Failed to resize image"));

        metadata.setData(result);
        metadata.setContentType(String.format("image/%s", imageFormat.getType()));
    }

    private void resizeVideo(T metadata, VideoFormats videoFormat, int width, int height) {
        //TODO: resize video not supported yet

        IVSize customRes = new IVSize();
        customRes.setWidth(width);
        customRes.setHeight(height);

        var currentVideoFormat = Optional.ofNullable(VIDEO_FORMATS.get(metadata.getContentType().split("/")[1].toLowerCase()))
                                         .orElseThrow(() -> HttpResponseThrowers.throwServerErrorException("Unknown video format"));
        
        if (ObjectUtils.isEmpty(videoFormat))
            videoFormat = currentVideoFormat;

        try {
            var result = metadata.getData();
            if(currentVideoFormat != videoFormat)
                result = this.compressor.convertVideoFormat(result, currentVideoFormat, videoFormat);
            result = this.compressor.reduceVideoSizeWithCustomRes(result, videoFormat, customRes);
            metadata.setData(result);
        } 
        catch (VideoException e) {
            log.error(e.getMessage(), e);
            HttpResponseThrowers.throwServerError("Server experience unknown error when resize video");
        }

        metadata.setContentType(String.format("video/%s", videoFormat.getType()));
    }

    @GetMapping("metadata/all")
    public List<T> getMetadata(@RequestHeader(required = false) Integer user_id) {

        if(ObjectUtils.isEmpty(user_id))
            HttpResponseThrowers.throwUnauthorized("Unauthorized");

        return this.objectStorageService.getAll(user_id);
    }

    @SuppressWarnings("unchecked")
    @GetMapping("metadata")
    public T getMetadataByCriteria(
            @RequestHeader(required = false) Integer user_id,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) I id) {

        if(ObjectUtils.isEmpty(user_id))
            HttpResponseThrowers.throwUnauthorized("Unauthorized");
        this.validateInput(id, path, fileName);
        var metadata = this.objectStorageService.getFileMetaDataByCriteria(id, path, fileName, user_id);
        if (!ObjectUtils.isEmpty(metadata)) {
            this.objectStorageService.checkIsRelatedToUser(metadata, user_id, List.of(UserPermissionEnum.READ));
            return metadata;
        }
        else
            return (T) HttpResponseThrowers.throwNotFound("Metadata not found");
    }

    @SuppressWarnings("unchecked")
    @PostMapping("file")
    public T uploadFile(
        @RequestHeader(required = false) Integer user_id, 
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "publicity", required = false) Boolean publicity)
            throws IOException {
        if(ObjectUtils.isEmpty(user_id))
            HttpResponseThrowers.throwUnauthorized("Unauthorized");
        if(publicity == null)
            publicity = false;
        
        try(var metadata = this.fromMultipartFile(file, user_id, file.getBytes(), publicity)) {
            return this.objectStorageService.post(metadata);
        }
        catch(ResponseStatusException e) {
            throw e;
        }
        catch(Exception e) {
            log.error(e.getMessage(), e);
            return (T) HttpResponseThrowers.throwServerError("Failed to upload file");
        }
    }

    @SuppressWarnings("unchecked")
    @PutMapping("file")
    public T updateFile(
            @RequestHeader(required = false) Integer user_id,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) I id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "publicity", required = false) Boolean publicity) throws IOException {

        if(ObjectUtils.isEmpty(user_id))
            HttpResponseThrowers.throwUnauthorized("Unauthorized");
        this.validateInput(id, path, fileName);
        T metadata = this.objectStorageService.getFileByCriteria(id, path, fileName, user_id);
        if (ObjectUtils.isEmpty(metadata))
            HttpResponseThrowers.throwBadRequest("No file metadata found");
        this.objectStorageService.checkIsRelatedToUser(metadata, user_id, List.of(UserPermissionEnum.WRITE));

        try(var fileMetaData = this.fromMultipartFile(file, user_id, file.getBytes(), publicity)) {
            if(metadata.getSize() == fileMetaData.getSize() && metadata.getData().equals(fileMetaData.getData()))
                return metadata;

            metadata.setInputUserId(user_id);
            metadata.setSize(fileMetaData.getSize());
            metadata.setData(fileMetaData.getData());
            
            var result = this.objectStorageService.put((I) ReflectionUtils.getIdFieldValue(metadata), metadata);
            this.objectStorageService.replaceOnStorage(metadata.getData(), metadata.getPath());

            metadata.close();
            fileMetaData.close();
            return result;
        }
        catch(ResponseStatusException e) {
            throw e;
        }
        catch(Exception e) {
            log.error(e.getMessage(), e);
            return (T) HttpResponseThrowers.throwServerError("Failed to upload file");
        }
    }

    @PatchMapping("metadata")
    public T patchMetaData(
            @RequestHeader(required = false) Integer user_id,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) I id,
            @RequestBody T fileMetaData) {

        if(ObjectUtils.isEmpty(user_id))
            HttpResponseThrowers.throwUnauthorized("Unauthorized");
        this.validateInput(id, path, fileName);
        var metadata = this.objectStorageService.getFileMetaDataByCriteria(id, path, fileName, user_id);
        if (ObjectUtils.isEmpty(metadata))
            HttpResponseThrowers.throwBadRequest("No file metadata found");
        this.objectStorageService.checkIsRelatedToUser(metadata, user_id, List.of(UserPermissionEnum.WRITE));
        return this.objectStorageService.patchFileMetaData(metadata, fileMetaData);
    }

    @DeleteMapping("file")
    public void deleteFile(
            @RequestHeader(required = false) Integer user_id,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) I id) {

        if(ObjectUtils.isEmpty(user_id))
            HttpResponseThrowers.throwUnauthorized("Unauthorized");
        this.validateInput(id, path, fileName);
        var metadata = this.objectStorageService.getFileMetaDataByCriteria(id, path, fileName, user_id);
        if (ObjectUtils.isEmpty(metadata))
            HttpResponseThrowers.throwBadRequest("No file metadata found");
        this.objectStorageService.checkIsRelatedToUser(metadata, user_id, List.of(UserPermissionEnum.DELETE));
        this.objectStorageService.delete(metadata);
    }

    private void validateInput(I id, String path, String fileName) {
        if(ObjectUtils.isEmpty(id) && ObjectUtils.isEmpty(path) && ObjectUtils.isEmpty(fileName))
            HttpResponseThrowers.throwBadRequest("No id, path or fileName provided");
    }
}
