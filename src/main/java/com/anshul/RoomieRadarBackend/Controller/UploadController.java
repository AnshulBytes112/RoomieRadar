package com.anshul.RoomieRadarBackend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class UploadController {

    private final Path fileStorageLocation;
    // Base URL for accessing files. In production this should be configured.
    // For local development with our WebConfig, it will be relative.
    private static final String DOWNLOAD_URI = "/uploads/";

    public UploadController() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("image") MultipartFile file) {
        // Basic validation
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            // We strip the content type check for now, but in production validating it is
            // good.
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return the download URL
            // Adjust this to be a full URL if needed, but often relative is fine if
            // frontend handles it.
            // Based on frontend usage: <img src={formData.avatar} ... />
            // If the frontend is on localhost:5173 and backend on localhost:8080, relative
            // path might fail
            // if the image tag assumes relative to frontend origin.
            // BUT api.ts sets API_BASE_URL.
            // Wait, standard HTML <img src="/uploads/foo.jpg"> will try to fetch from
            // Frontend Origin (localhost:5173).
            // We need to return the FULL URL including backend host if they are on
            // different ports.
            // OR the frontend needs to prepend API_BASE_URL.

            // Looking at Profile.tsx: <img src={formData.avatar} ... />
            // If avatar is just "/uploads/...", it will break on dev server.

            // I should probably return the full URL if possible, or assume the user knows.
            // Safer to return "/uploads/..." and let's check if I can make it work.
            // Actually, for simplicity, I should probably append the scheme and host.
            // specific to local dev usually http://localhost:8080.

            // Let's rely on a simple relative path for now, but maybe the frontend needs
            // patching?
            // "const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';" in api.ts.
            // Profile.tsx does NOT seem to prepend API_BASE_URL.

            // So if I return "/uploads/xyz.jpg", frontend loads
            // "http://localhost:5173/uploads/xyz.jpg" -> 404.
            // I should return "http://localhost:8080/uploads/xyz.jpg".

            String fileDownloadUri = "http://localhost:8080" + DOWNLOAD_URI + fileName;
            // TODO: In a real app, inject the hostname/port.

            Map<String, String> response = new HashMap<>();
            // The frontend expects "url" or just the string?
            // handleImageUpload in Profile.tsx: const imageUrl = response.url || response;
            response.put("url", fileDownloadUri);

            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
}
