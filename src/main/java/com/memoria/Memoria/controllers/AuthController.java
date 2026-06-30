package com.memoria.Memoria.controllers;

import com.memoria.Memoria.dto.user.RegisterRequest;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.services.NoteService;
import com.memoria.Memoria.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final NoteService noteService;

    @GetMapping("/")
    public String root() {
        return "redirect:/notes";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest("", "", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute RegisterRequest registerRequest,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.register(registerRequest);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("registration.error", ex.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Account created. Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        User user = userService.requireByUsername(principal.getName());
        model.addAttribute("loggedUser", user);
        model.addAttribute("noteCount", noteService.countByUser(user));
        return "loggedUserProfile";
    }
}
