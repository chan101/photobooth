package com.chan101.photobooth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

@RestController
@RequestMapping("/photos")
@CrossOrigin(origins = "*")
public class PhotoController {

	@Value("${app.images.path}")
	private String imagesPath;

	@GetMapping("/{*subPath}")
	public ResponseEntity<?> getPhotos(@PathVariable String subPath) {

		File directory = new File(imagesPath + subPath);

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
			if (file.isDirectory() ) {
				obj1.put("type", "D");
				String filePath[] = file.getPath().split("/");
				obj1.put("file", filePath[filePath.length - 1]);
			} else {
				obj1.put("type", "F");
				String filePath[] = file.getPath().split("/");
				obj1.put("file", filePath[filePath.length - 1]);
			}
			if(!file.getName().equals("thumbnail")){
				responseList.put(obj1);
			}

		}

		return ResponseEntity.ok().header("Content-Type", "application/json").body(responseList.toString());
	}

	@PutMapping(value = "/{*subPath}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> uploadFiles(@PathVariable String subPath,
			@RequestParam("files") List<MultipartFile> files) {

		if (files.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(Map.of("error", "No files uploaded."));
		}

		try {
			for (MultipartFile file : files) {
				if (file.isEmpty()) {
					continue; // Skip empty files, or you could return an error for each
				}

				Path targetPath = Path.of(imagesPath + subPath, file.getOriginalFilename());
				Files.write(targetPath, file.getBytes());
				ExecutorService executor = Executors.newFixedThreadPool(8);
				CompletableFuture.runAsync(() -> {try {
					Thumbnails.of(targetPath.toString()).size(200, 200).outputQuality(1).toFile(imagesPath +"/thumbnail"+subPath+"/"+file.getOriginalFilename());
				} catch (IOException e) {
					e.printStackTrace();
				}}, executor);
				 
			}

			return ResponseEntity.ok().body(Map.of("message", "Upload successful"));

		} catch (IOException e) {
			return ResponseEntity.internalServerError().body(Map.of("message", e.toString()));
		}
	}

	@PostMapping(value = "/{*subPath}")
	public ResponseEntity<?> createDirectory(@PathVariable String subPath) {
		File newDir = new File(imagesPath, subPath);
		File newDirThumbnail = new File(imagesPath + "/thumbnail", subPath);
		if (newDir.mkdir()) {
			newDirThumbnail.mkdir();
			return ResponseEntity.ok().body(Map.of("message", "Folder created successfully"));
		} else {
			return ResponseEntity.internalServerError().body(Map.of("message", "Folder could not be created"));
		}

	}

	@DeleteMapping("/{*subPath}")
	public ResponseEntity<?> deletePhoto(@RequestBody String body, @PathVariable String subPath) {

		try {
			JSONArray filesToDelete = new JSONArray(body);
			for (int i = 0; i < filesToDelete.length(); i++) {
				File file = new File(imagesPath + subPath + "/" + filesToDelete.getString(i));
				File thumbnail = new File(imagesPath +"thumbnail/"+ subPath + "/" + filesToDelete.getString(i));
				if (!file.exists()) {
					return ResponseEntity.badRequest().body(Map.of("message", "File does not exist"));
				}
				if (!file.delete()) {
					thumbnail.delete();
					return ResponseEntity.badRequest()
							.body(Map.of("message", "File " + subPath + " could not be deleted."));
				}
			}
		} catch (JSONException e) {

			JSONObject folderToDelete = new JSONObject(body);
			File file = new File(imagesPath + subPath + "/" + folderToDelete.getString("folder"));
			File thumbnailFolder = new File(imagesPath + "/thumbnail" + subPath + "/" + folderToDelete.getString("folder"));
			if (!file.exists()) {
					return ResponseEntity.badRequest().body(Map.of("message", "File does not exist"));
				}
			if (!file.delete()) {
				return ResponseEntity.badRequest()
						.body(Map.of("message", "File " + subPath + " could not be deleted."));
			}
			thumbnailFolder.delete();
			return ResponseEntity.ok().body(Map.of("message", "Files deleted successfully"));
		}
		return ResponseEntity.ok().body(Map.of("message", "Files deleted successfully"));
	}
}
