package com.naukma.thesisbackend.controllers;

import com.naukma.thesisbackend.dtos.PostDto;
import com.naukma.thesisbackend.dtos.UserBasicInfoDto;
import com.naukma.thesisbackend.entities.Post;
import com.naukma.thesisbackend.entities.User;
import com.naukma.thesisbackend.exceptions.AuthenticationFailedException;
import com.naukma.thesisbackend.services.AuthService;
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
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final AvatarService avatarService;
    private final UserService userService;
    private final PostService postService;
    private final AuthService authService;


    public UserController(AvatarService avatarService, UserService userService, PostService postService, AuthService authService){
        this.avatarService = avatarService;
        this.userService = userService;
        this.postService = postService;
        this.authService = authService;
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
                .orElseThrow(()-> new EntityNotFoundException("User not found"))
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
        if(avatarFile == null || avatarFile.getContentType() == null){
            return ResponseEntity.badRequest().body("Invalid input file");
        }
        if (!avatarFile.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().body("Only image files are allowed, got "+avatarFile.getContentType());
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
        if(user.getAvatar()!=null){
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
     * method for getting personal information of user
     * this is sensitive information, can be accessed only by user
     * @param userId id of user
     * @return String value of email
     */
    @GetMapping(value = "/{userId}/profile")
    public ResponseEntity<User> getUserProfile(@PathVariable String userId){
        User user = userService
                .getUserById(userId)
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));

        return ResponseEntity.ok(user);
    }

    /**
     * gets info of user by info from his auth token
     * @return
     */
    @GetMapping(value = "/profile")
    public ResponseEntity<User> getUserOwnInfo(){
        String userId = authService.getCurrentUserId();

        User user = userService
                .getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No such user"));

        return ResponseEntity.ok(user);
    }

    /**
     * method for getting ids of liked posts for user
     * information about liked posts is sensitive, so method can be accessed only by the user, who has same userId as specified in path
     * @param userId id of user
     * @return ids of liked posts
     */
    @GetMapping(value = "/{userId}/liked-posts")
    public ResponseEntity<List<PostDto>> getUserLikedPosts(@PathVariable String userId){
        List<Post> likedPosts = userService.getLikedPostsByUserId(userId);

        List<PostDto> likedPostDtos = likedPosts
                .stream()
                .sorted(Comparator.comparing(Post::getPostedDate).reversed())
                .map(post->postService.postToPostDto(post, userId, false))
                .toList();

        return ResponseEntity.ok(likedPostDtos);
    }

    @GetMapping(value = "/{userId}/posts")
    public ResponseEntity<List<PostDto>> getPostsAuthoredBy(@PathVariable String userId){
        List<Post> posts = userService.getPostsAuthoredBy(userId);

        List<PostDto> postDtos = posts
                .stream()
                .sorted(Comparator.comparing(Post::getPostedDate).reversed())
                .map(post->postService.postToPostDto(post, userId, false))
                .toList();

        return ResponseEntity.ok(postDtos);
    }

    /**
     * deletes user by userId
     * can be accessed only by user with such id or admin
     * @param userId id of user to delete
     * @return ok response
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") String userId){
        userService.delete(userId);
        return ResponseEntity.ok().build();
    }


}