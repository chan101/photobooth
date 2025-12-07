package com.chan101.photobooth;

import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/photos")
public class PhotoController {
	
    @Value("${app.images.path}")
    private String imagesPath;

	@GetMapping("/{*subPath}")
	public ResponseEntity<?> getPhotos(@PathVariable String subPath) {
		

	    File directory = new File(imagesPath+subPath);

	    // If directory doesn't exist → return 404
	    if (!directory.exists() || !directory.isDirectory()) {
	        return ResponseEntity
	                .status(HttpStatus.NOT_FOUND)
	                .body(Map.of("error", "The specified path is not a valid directory."));
	    }

	    File[] filesList = directory.listFiles();

	    // If directory is empty
	    if (filesList == null || filesList.length == 0) {
	        return ResponseEntity.ok(Map.of("files", List.of()));
	    }
	    JSONArray responseList = new JSONArray();
	    for (File file : filesList) {
	    	JSONObject obj1 = new JSONObject();
			if(file.isDirectory()) {
				obj1.put("type", "D");
				obj1.put("file", file.getPath().replace(imagesPath, ""));
			}
			else {
				obj1.put("type", "F");
				obj1.put("file", file.getPath().replace(imagesPath, ""));
			}
			
			responseList.put(obj1);
			
		}
	    
	    return ResponseEntity.ok().header("Content-Type", "application/json").body(responseList.toString());
	}


    @PutMapping(value = "/{*subPath}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@PathVariable String subPath, @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty."));
        }

        try {
            Path targetPath = Path.of(imagesPath + subPath, file.getOriginalFilename());

            Files.write(targetPath, file.getBytes());

            return ResponseEntity.ok().body(Map.of("message", "Upload successful"));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.toString()));
        }
    }
    
    @PostMapping(value = "/{*subPath}")
    public ResponseEntity<?> createDirectory(@PathVariable String subPath){
    	File newDir = new File(imagesPath, subPath);
    	if(newDir.mkdir()) {
    		return ResponseEntity.ok().body(Map.of("message","Folder created successfully"));
    	}
    	else {
    		return ResponseEntity.internalServerError().body(Map.of("message","Folder could not be created"));
    	}
		
    }

    @DeleteMapping("/{*subPath}")
    public ResponseEntity<?> deletePhoto(@PathVariable String subPath) {
    	
    	
    	File file = new File(imagesPath+subPath);
    	
    	if(!file.exists()) {
    		return ResponseEntity.badRequest().body(Map.of("message","File does not exist"));
    	}
    	
    	if(file.delete()) {
    		return ResponseEntity.ok().body(Map.of("message","File "+ subPath +" deleted successfully"));
    	}
    	else {
    		return ResponseEntity.badRequest().body(Map.of("message","File "+ subPath +" could not be deleted successfully"));
    	}
    	
    }
}
