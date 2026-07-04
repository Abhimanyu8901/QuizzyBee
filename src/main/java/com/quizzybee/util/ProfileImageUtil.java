package com.quizzybee.util;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class ProfileImageUtil {
    private static final Path PROFILE_IMAGE_DIR = Path.of(System.getProperty("user.dir"), "profile-images");

    private ProfileImageUtil() {
    }

    public static String saveProfileImage(int userId, File sourceFile) throws IOException {
        Files.createDirectories(PROFILE_IMAGE_DIR);

        String extension = getExtension(sourceFile.getName());
        String fileName = "user-" + userId + "-" + UUID.randomUUID() + extension;
        Path destination = PROFILE_IMAGE_DIR.resolve(fileName);
        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toAbsolutePath().toString();
    }

    public static void deleteProfileImage(String imagePath) throws IOException {
        if (imagePath == null || imagePath.isBlank()) {
            return;
        }

        Path path = Path.of(imagePath).toAbsolutePath().normalize();
        if (path.startsWith(PROFILE_IMAGE_DIR.toAbsolutePath().normalize())) {
            Files.deleteIfExists(path);
        }
    }

    public static String toImageUri(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        File file = new File(imagePath);
        if (!file.exists()) {
            return null;
        }
        return file.toURI().toString();
    }

    public static Image loadImage(String imageUri) {
        return new Image(imageUri, 104, 104, false, true);
    }

    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(dotIndex) : ".png";
    }
}
