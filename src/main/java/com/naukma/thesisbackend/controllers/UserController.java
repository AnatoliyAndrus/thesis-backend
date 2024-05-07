package com.naukma.thesisbackend.controllers;

import com.naukma.thesisbackend.dtos.PostDto;
import com.naukma.thesisbackend.dtos.UserBasicInfoDto;
import com.naukma.thesisbackend.entities.Post;
import com.naukma.thesisbackend.entities.User;
import com.naukma.thesisbackend.services.AvatarService;
import com.naukma.thesisbackend.services.PostService;
import com.naukma.thesisbackend.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final AvatarService avatarService;
    private final UserService userService;
    private final PostService postService;


    public UserController(AvatarService avatarService, UserService userService, PostService postService){
        this.avatarService = avatarService;
        this.userService = userService;
        this.postService = postService;
    }

    /**
     * method for getting avatar of specific user
     * @param userId id of user
     * @return user avatar as resource
     * @throws IOException if problem occured with reading data
     */
    @GetMapping("/{userId}/avatar")
    public ResponseEntity<Resource> getUserAvatar(@PathVariable String userId) throws IOException {
        String avatar = userService.getUserById(userId)
                .orElseThrow(()-> new EntityNotFoundException("No such user"))
                .getAvatar();

        byte[] imageBytes = avatarService.getImage(avatar);

        if (imageBytes != null) {
            ByteArrayResource resource = new ByteArrayResource(imageBytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * method which saves new avatar of user and deletes previous if existed
     * @param userId id of user
     * @param avatarFile image file, encoded in multipart/form-data
     * @return status code 200 if avatar was successfully saved/replaced and 400 if there is no image file in request, or it has non-image type
     * @throws IOException
     */
    @PostMapping("/{userId}/avatar")
    public ResponseEntity<?> saveOrUpdateUserAvatar(@PathVariable String userId, @RequestParam("avatar") MultipartFile avatarFile) throws IOException {
        if(avatarFile == null){
            return ResponseEntity.badRequest().body("Image file required");
        }
        if (avatarFile.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().body("Only image files are allowed");
        }

        User user = userService
                .getUserById(userId)
                .orElseThrow(()-> new EntityNotFoundException("No such user"));

        String newAvatar = avatarService.saveImage(avatarFile);
        String prevAvatar = user.getAvatar();

        user.setAvatar(newAvatar);

        //if user already had avatar, it is deleted from file system
        if(prevAvatar!=null){
            avatarService.deleteImage(user.getAvatar());
        }

        userService.save(user);

        return ResponseEntity.ok().body(null);
    }


    /**
     * method which deletes previous user avatar if existed
     * @param userId id of user
     * @return status 200 if successfully deleted and 404 if user does not have avatar
     * @throws IOException
     */
    @DeleteMapping("/{userId}/avatar")
    public ResponseEntity<?> deleteAvatar(@PathVariable String userId) throws IOException {
        User user = userService
                .getUserById(userId)
                .orElseThrow(()-> new EntityNotFoundException("No such user"));

        //if user already had avatar, it is deleted from file system
        if(user.getAvatar()==null){
            avatarService.deleteImage(user.getAvatar());
            user.setAvatar(null);
            userService.save(user);
            return ResponseEntity.ok().body(null);
        }
        else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is nothing to delete");
        }
    }

    /**
     * method for getting basic, not sensitive information of user available for everyone
     * @param userId id of user
     * @return object with not sensitive user information
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserBasicInfoDto> getUserBasicInfo(@PathVariable String userId){
        return ResponseEntity.ok(
                userService
                        .getUserById(userId)
                        .orElseThrow(()-> new EntityNotFoundException("No such user"))
                        .toUserBasicInfoDto());
    }

    /**
     * method for getting email of user
     * email is sensitive information, so method can be accessed only by the user, who has same userId as specified in path
     * @param userId id of user
     * @return String value of email
     */
    @GetMapping(value = "/{userId}/email", produces = "text/plain")
    public String getUserEmail(@PathVariable String userId){
        return userService
                .getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No such user"))
                .getEmail();
    }

    /**
     * method for getting ids of liked posts for user
     * information about liked posts is sensitive, so method can be accessed only by the user, who has same userId as specified in path
     * @param userId id of user
     * @return ids of liked posts
     */
    @GetMapping(value = "/{userId}/liked-posts")
    public ResponseEntity<Set<PostDto>> getUserLikedPosts(@PathVariable String userId){
        Set<Post> likedPosts = userService.getLikedPostsByUserId(userId);

        Set<PostDto> likedPostDtos = likedPosts
                .stream()
                .map(post->postService.postToPostDto(post, null, false))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(likedPostDtos);
    }


}