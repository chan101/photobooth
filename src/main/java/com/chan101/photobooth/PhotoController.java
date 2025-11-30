package com.chan101.photobooth;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/photos")
public class PhotoController {
	
    @Value("${app.images.path}")
    private String imagesPath;

	@GetMapping
	public ResponseEntity<?> getPhotos() {

	    File directory = new File(imagesPath);

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
				obj1.put("file", file.getPath().replace(imagesPath, "images/"));
			}
			else {
				obj1.put("type", "F");
				obj1.put("file", file.getPath().replace(imagesPath, "images/"));
			}
			
			responseList.put(obj1);
			
		}
	    
	    return ResponseEntity.ok().header("Content-Type", "application/json").body(responseList.toString());
	}



    @PostMapping
    public String uploadPhoto() {
        return "Photo uploaded!";
    }

    @DeleteMapping("/{id}")
    public String deletePhoto(@PathVariable String id) {
        return "Photo " + id + " deleted!";
    }
}
