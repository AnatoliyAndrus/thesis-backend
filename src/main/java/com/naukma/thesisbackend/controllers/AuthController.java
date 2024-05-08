package com.naukma.thesisbackend.controllers;

import com.naukma.thesisbackend.auth.TokenProvider;
import com.naukma.thesisbackend.dtos.JwtDto;
import com.naukma.thesisbackend.dtos.SignInDto;
import com.naukma.thesisbackend.dtos.SignUpDto;
import com.naukma.thesisbackend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * controller for handling authentication.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  @Autowired
  private AuthenticationManager authenticationManager;
  @Autowired
  private AuthService service;
  @Autowired
  private TokenProvider tokenService;

  @PostMapping("/signup")
  public ResponseEntity<?> signUp(@RequestBody @Valid SignUpDto data) {
    service.signUp(data);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/signin")
  public ResponseEntity<JwtDto> signIn(@RequestBody @Valid SignInDto data) {
    var usernamePassword = new UsernamePasswordAuthenticationToken(data.userId(), data.password());

    var authUser = authenticationManager.authenticate(usernamePassword);

    var accessToken = tokenService.generateAccessToken((UserDetails) authUser.getPrincipal());

    return ResponseEntity.ok(new JwtDto(accessToken));
  }

}