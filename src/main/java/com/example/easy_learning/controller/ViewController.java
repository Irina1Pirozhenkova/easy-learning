package com.example.easy_learning.controller;

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ViewController {

  @GetMapping({"/login", "/login-page"})
  public String loginPage() {
    return "login";   // src/main/resources/templates/login.html
  }

  @GetMapping("/register")
  public String registerPage() {
    return "register"; // templates/register.html
  }

  @GetMapping("/")
  public String home() {
    return "index";   // templates/index.html
  }
}

