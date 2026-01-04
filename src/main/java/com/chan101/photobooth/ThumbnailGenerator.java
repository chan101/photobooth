package com.chan101.photobooth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import net.coobird.thumbnailator.Thumbnails;

@Component
@Order(1)
public class ThumbnailGenerator implements CommandLineRunner {
 
    @Value("${app.images.path}")
    private String imagesPath;

    
    private static final java.util.Set<String> IMAGE_EXTS = java.util.Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif", "webp", "svg", "heif", "raw"
    );

    private static final java.util.Set<String> VIDEO_EXTS = java.util.Set.of(
        "mp4", "m4v", "mov", "avi", "wmv", "flv", "f4v", "mkv", "webm", "mpeg", "mpg",
        "3gp", "3g2", "ts", "mts", "m2ts", "vob", "ogg", "ogv"
    );


    @Override
    public void run(String... args) throws IOException {
        System.out.println("Generating thumbnails");
        generateThumbnail(imagesPath);
        System.out.println("Thumbnails generated");
    }

    public void generateThumbnail(String path) throws IOException{
        if(path.equals(imagesPath+"/thumbnail"))return;
        File[] files = new File(path).listFiles();
        for(File file: files){
            if(file.isDirectory()){
                generateThumbnail(file.getAbsolutePath());
            }
            else{
                switch (formatCheck(file.getName())) {
                    case "image":
                        String outputPath = imagesPath + "/thumbnail" +file.getAbsolutePath().replace(imagesPath, "");
                        Path folderPath = Paths.get(outputPath.replace(file.getName(), ""));
                        if(!Files.exists(folderPath)){
                            Files.createDirectories(folderPath);
                        }
                        if(!new File(outputPath).exists()){
                            Thumbnails.of(file).size(200, 200).outputQuality(1).toFile(outputPath);
                        }
                        break;
                
                    default:
                        break;
                }
                
            }
        }
    }


    public static String formatCheck(String ext) {
        if (ext == null) return "unknown";

        String cleaned = ext.trim();
        if (cleaned.isEmpty()) return "unknown";

        // If a filename was passed instead of an extension, pick the part after the last dot
        int lastDot = cleaned.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < cleaned.length() - 1) {
            cleaned = cleaned.substring(lastDot + 1);
        }

        // Normalize
        cleaned = cleaned.toLowerCase();

        // If there are still dots or spaces, remove them
        cleaned = cleaned.replaceAll("\\s+", "");
        cleaned = cleaned.replaceAll("^\\.+", ""); // leading dots

        if (cleaned.isEmpty()) return "unknown";

        if (VIDEO_EXTS.contains(cleaned)) return "video";
        if (IMAGE_EXTS.contains(cleaned)) return "image";
        return "unknown";
    }

}
