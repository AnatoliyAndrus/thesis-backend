package com.naukma.thesisbackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AvatarService {

    @Value("${custom.avatar-directory}")
    private String avatarDirectory;

    /**
     * saves image into avatar directory
     * @param imageFile image as multipart file
     * @return new image name
     * @throws IOException in case of problem with saving image
     */
    public String saveImage(MultipartFile imageFile) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

        Path filePath = Path.of(avatarDirectory, fileName);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    /**
     * retrieves image from server
     * @param imageName image file name, with extension
     * @return image
     * @throws IOException in case of error in reading file as stream
     */
    public byte[] getImage(String imageName) throws IOException {
        Path filePath = Path.of(avatarDirectory, imageName);

        return Files.exists(filePath)?Files.readAllBytes(filePath):null;
    }

    /**
     * deletes image from server
     * @param imageName name of image to delete
     * @return true if image was deleted successfully, false if it was not located
     * @throws IOException if delete operation failed
     */
    public boolean deleteImage(String imageName) throws IOException {
        Path filePath = Path.of(avatarDirectory, imageName);

        if(Files.exists(filePath)){
            Files.delete(filePath);
            return true;
        }
        else{
            return false;
        }
    }
}
