package com.anshul.RoomieRadarBackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for file storage based on active Spring profile
 * Handles upload directory creation and provides beans for file operations
 */
@Configuration
public class FileStorageConfig {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * Creates and returns the upload directory path
     * Creates the directory if it doesn't exist
     * 
     * @return Path object representing the upload directory
     */
    @Bean
    public Path fileStoragePath() {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            // Create directory if it doesn't exist
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("Created upload directory: " + path);
            } else {
                System.out.println("Using existing upload directory: " + path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + path, e);
        }

        return path;
    }

    /**
     * Get the upload directory as a string
     * 
     * @return upload directory path
     */
    public String getUploadDir() {
        return uploadDir;
    }
}
