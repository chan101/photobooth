package com.chan101.photobooth;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Graphics2D;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/scaleImage")
@CrossOrigin(origins = "*")
public class ImageScaler {

    @Value("${app.images.path}")
    private String imagesPath;

    // in-memory cache
    private final Map<String, byte[]> imageCache = new ConcurrentHashMap<>();

    @GetMapping("/{*subPath}")
    public ResponseEntity<?> getScaledPhotos(
            @PathVariable String subPath,
            @RequestParam int width,
            @RequestParam int height) throws IOException {

        String imageFullPath = imagesPath + subPath;
        String cacheKey = imageFullPath + "_" + width + "_" + height;

        // Return cached image if present
        if (imageCache.containsKey(cacheKey)) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                    .body(imageCache.get(cacheKey));
        }

        // Load image
        BufferedImage original = ImageIO.read(new File(imageFullPath));
        Image scaled = original.getScaledInstance(width, height, Image.SCALE_FAST);

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = output.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(output, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        // Cache result
        imageCache.put(cacheKey, imageBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .body(imageBytes);
    }
}
