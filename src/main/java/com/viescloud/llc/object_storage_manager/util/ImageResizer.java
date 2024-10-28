package com.viescloud.llc.object_storage_manager.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import javax.imageio.ImageIO;

import com.viescloud.llc.viesspringutils.util.MultiTask;

import io.github.techgnious.IVCompressor;
import io.github.techgnious.dto.IVSize;
import io.github.techgnious.dto.ImageFormats;
import io.github.techgnious.exception.ImageException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageResizer {

    private static final MultiTask<byte[]> multiTask = MultiTask.of(null, 3);

    public final static IVCompressor compressor = new IVCompressor();

    private ImageResizer() {}

    public static byte[] resizeImageUsingGraphics2D(byte[] inputImageBytes, int newWidth, int newHeight, String formatName) throws IOException {
        // Convert byte[] to BufferedImage
        ByteArrayInputStream bais = new ByteArrayInputStream(inputImageBytes);
        BufferedImage originalImage = ImageIO.read(bais);

        // Create a new BufferedImage with the desired dimensions
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());

        // Create a Graphics2D object to draw the resized image
        Graphics2D g2d = resizedImage.createGraphics();
        
        // Draw the resized image
        g2d.drawImage(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();  // Free system resources used by g2d

        // Convert BufferedImage to byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, formatName, baos);
        return baos.toByteArray();
    }

    public static byte[] resizeImageUsingCompressor(byte[] data, ImageFormats imageFormat, int width, int height) throws ImageException {
        IVSize customRes = new IVSize();
        customRes.setWidth(width);
        customRes.setHeight(height);
        return compressor.resizeImageWithCustomRes(data, imageFormat, customRes);
    }

    public static Optional<byte[]> resizeImage(byte[] data, int width, int height, String formatName) {
        var data1 = data;
        var data2 = data.clone();

        multiTask.submitTask(data1, d -> {
            try {
                return resizeImageUsingCompressor(data1, ImageFormats.valueOf(formatName.toUpperCase()), width, height);
            } catch (ImageException e) {
                log.error(e.getMessage(), e);
                return null;
            }
        });

        multiTask.submitTask(data2, d -> {
            try {
                return resizeImageUsingGraphics2D(data2, width, height, formatName);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return null;
            }
        });

        var resultOpt = multiTask.waitUntilOneTaskDoneOptional(null);
        multiTask.cancelAllOnGoingTask();
        multiTask.clearDoneTasks();

        return resultOpt;
    }
}