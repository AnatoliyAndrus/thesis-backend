package com.naukma.thesisbackend.services;
import com.naukma.thesisbackend.auth.CustomUserDetails;
import com.naukma.thesisbackend.dtos.SignUpDto;
import com.naukma.thesisbackend.entities.User;
import com.naukma.thesisbackend.enums.UserRole;
import com.naukma.thesisbackend.exceptions.InvalidJwtException;
import com.naukma.thesisbackend.repositories.UserRepository;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {


  private final UserRepository userRepository;

  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    var user = userRepository.findByUserId(username).get();
    return new CustomUserDetails(user);
  }

  /**
   * method for signing user up
   * @param signUpDto user sign up request
   * @return saved user
   * @throws InvalidJwtException if the user with such userId as in request already exists
   */
  public User signUp(SignUpDto signUpDto) throws InvalidJwtException {

    if (userRepository.findByUserId(signUpDto.userId()).isPresent()) throw new InvalidJwtException("User with such userId already exists");

    String encryptedPassword = new BCryptPasswordEncoder().encode(signUpDto.password());

    User user = new User(signUpDto.userId(), signUpDto.nickname(), signUpDto.email(), encryptedPassword, UserRole.USER);

    return userRepository.save(user);

  }

  /**
   * method for getting userId from security context
   * @return userId or null if user is not authenticated
   */
  public @Nullable String getCurrentUserId(){
    return SecurityContextHolder.getContext().getAuthentication().isAuthenticated()&&SecurityContextHolder.getContext().getAuthentication().getName()!="anonymousUser"?
            SecurityContextHolder.getContext().getAuthentication().getName()
            :null;
  }

  public boolean userExists(String userId){
    return userRepository.existsByUserId(userId);
  }

}