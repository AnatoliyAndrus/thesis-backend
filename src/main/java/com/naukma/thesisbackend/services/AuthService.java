package com.naukma.thesisbackend.services;
import com.naukma.thesisbackend.auth.CustomUserDetails;
import com.naukma.thesisbackend.dtos.SignUpDto;
import com.naukma.thesisbackend.entities.User;
import com.naukma.thesisbackend.enums.UserRole;
import com.naukma.thesisbackend.exceptions.InvalidJwtException;
import com.naukma.thesisbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

  @Autowired
  UserRepository repository;

  @Override
  public UserDetails loadUserByUsername(String username) {
    var user = repository.findByUserId(username)
            .orElseThrow(()->new BadCredentialsException("There is no such user"));
    return new CustomUserDetails(user);
  }

  public User signUp(SignUpDto data) throws InvalidJwtException {

    if (repository.findByUserId(data.userId()).isPresent()) {
      throw new InvalidJwtException("Username already exists");
    }

    String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());

    //By default, the user is created with "User" role
    User user = new User(data.userId(), data.nickname(), data.email(), encryptedPassword, UserRole.USER);

    return repository.save(user);

  }
}