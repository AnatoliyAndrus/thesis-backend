package com.naukma.thesisbackend.controllers;

import com.naukma.thesisbackend.auth.TokenProvider;
import com.naukma.thesisbackend.dtos.JwtDto;
import com.naukma.thesisbackend.dtos.SignInDto;
import com.naukma.thesisbackend.dtos.SignUpDto;
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
  private final AuthService service;
  private final TokenProvider tokenService;

  public AuthController(AuthenticationManager authenticationManager, AuthService service, TokenProvider tokenService){

    this.authenticationManager = authenticationManager;
    this.service = service;
    this.tokenService = tokenService;
  }

  @PostMapping("/signup")
  public ResponseEntity<?> signUp(@RequestBody SignUpDto data) {
    service.signUp(data);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/signin")
  public ResponseEntity<JwtDto> signIn(@RequestBody SignInDto data) {
    var usernamePassword = new UsernamePasswordAuthenticationToken(data.userId(), data.password());

    var authUser = authenticationManager.authenticate(usernamePassword);

    var accessToken = tokenService.generateAccessToken((UserDetails) authUser.getPrincipal());

    return ResponseEntity.ok(new JwtDto(accessToken));
  }

}