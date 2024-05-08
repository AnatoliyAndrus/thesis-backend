package com.naukma.thesisbackend.services;

import com.naukma.thesisbackend.entities.Post;
import com.naukma.thesisbackend.entities.PostLike;
import com.naukma.thesisbackend.entities.User;
import com.naukma.thesisbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    /**
     * method which is used for getting user from database by userId
     * @param userId id of user
     * @return user with userId matching requested one
     */
    public Optional<User> getUserById(String userId) {
        return userRepository.
                findByUserId(userId);
    }

    public Set<Post> getLikedPostsByUserId(String userId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("No such user"));

        return user.
                getPostLikes()
                .stream()
                .map(PostLike::getPost)
                .collect(Collectors.toSet());
    }

    public User save(User user){
        return userRepository.save(user);
    }
}
