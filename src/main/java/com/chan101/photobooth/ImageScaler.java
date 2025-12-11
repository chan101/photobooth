package com.chan101.photobooth;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

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

    @GetMapping("/{*subPath}")
    public ResponseEntity<?> getScaledPhotos(@PathVariable String subPath, @RequestParam int width, @RequestParam int height) throws IOException {

        //scaleImageToJPEGDataURL(imagesPath + subPath, 500, 500, 0.5f);
        BufferedImage original = ImageIO.read(new File(imagesPath + subPath));
        Image scaled = original.getScaledInstance(width, height, Image.SCALE_FAST);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = output.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(output, "jpg", baos); // fast JPEG output
        byte[] imageBytes = baos.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .body(imageBytes);
    }

    public static String scaleImageToJPEGDataURL(String imagePath, int targetWidth, int targetHeight, float quality)
            throws IOException {
        BufferedImage original = ImageIO.read(new File(imagePath));
        if (original == null)
            throw new IOException("Cannot read image");

        // Scale image quickly
        Image scaled = original.getScaledInstance(targetWidth, targetHeight, Image.SCALE_FAST);
        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = output.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();

        // Write JPEG to byte array with specified quality
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality); // 0 = max compression, 1 = best quality
        writer.setOutput(new MemoryCacheImageOutputStream(baos));
        writer.write(null, new IIOImage(output, null, null), param);
        writer.dispose();

        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
