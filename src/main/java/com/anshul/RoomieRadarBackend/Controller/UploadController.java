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
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("image") MultipartFile file,
            jakarta.servlet.http.HttpServletRequest request) {
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
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Build dynamic URL from request - works for localhost, network IPs, and
            // production
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();

            String fileDownloadUri;
            if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
                fileDownloadUri = scheme + "://" + serverName + DOWNLOAD_URI + fileName;
            } else {
                fileDownloadUri = scheme + "://" + serverName + ":" + serverPort + DOWNLOAD_URI + fileName;
            }

            Map<String, String> response = new HashMap<>();
            response.put("url", fileDownloadUri);

            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
}
