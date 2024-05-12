package com.naukma.thesisbackend.controllers;

import com.naukma.thesisbackend.auth.TokenProvider;
import com.naukma.thesisbackend.dtos.JwtDto;
import com.naukma.thesisbackend.dtos.SignInDto;
import com.naukma.thesisbackend.dtos.SignUpDto;
import com.naukma.thesisbackend.exceptions.AuthenticationFailedException;
import com.naukma.thesisbackend.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * controller for handling authentication.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final AuthService userService;
  private final TokenProvider tokenService;

  public AuthController(AuthenticationManager authenticationManager, AuthService userService, TokenProvider tokenService){

    this.authenticationManager = authenticationManager;
    this.userService = userService;
    this.tokenService = tokenService;
  }

  @PostMapping("/signup")
  public ResponseEntity<?> signUp(@RequestBody SignUpDto data) {
    userService.signUp(data);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/signin")
  public ResponseEntity<JwtDto> signIn(@RequestBody SignInDto signInDto) {
    if(!userService.userExists(signInDto.userId())) throw new AuthenticationFailedException("Username or password invalid");

    var usernamePassword = new UsernamePasswordAuthenticationToken(signInDto.userId(), signInDto.password());

    var authUser = authenticationManager.authenticate(usernamePassword);

    var accessToken = tokenService.generateAccessToken((UserDetails) authUser.getPrincipal());

    return ResponseEntity.ok(new JwtDto(accessToken));
  }

}