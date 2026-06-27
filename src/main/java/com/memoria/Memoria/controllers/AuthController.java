package com.memoria.Memoria.controllers;

import com.memoria.Memoria.dto.user.RegisterRequest;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.services.UserService;
import com.memoria.Memoria.services.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    UserServiceImpl userService;

    @GetMapping("/register")
    public String showRegistrationForm(){
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterRequest request){
        System.out.println("request reached to register post ");
        userService.register(request);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String showLoginForm(){
        return "login";
    }

    @GetMapping("/")
    public String homePage(Principal principal, Model model){
        User loggeduser = userService.findByUsername(principal.getName());
        if(loggeduser == null) return "redirect:/login";
        model.addAttribute("loggedUser", loggeduser);
        return "loggedUserProfile";
    }
}
