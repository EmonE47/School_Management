package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.example.demo.dto.RegisterUserRequest;
import com.example.demo.model.Role;
import com.example.demo.service.SchoolService;

@Controller
public class RegisterFormController {

    private final SchoolService schoolService;

    public RegisterFormController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @PostMapping("/register")
    public RedirectView registerFromForm(
        @RequestParam String name,
        @RequestParam String email,
        @RequestParam String password,
        @RequestParam String role,
        @RequestParam(required = false) Long departmentId
    ) {
        try {
            Role selectedRole = Role.valueOf(role.trim().toUpperCase());
            schoolService.registerUser(new RegisterUserRequest(name, email, password, selectedRole, departmentId));
            return new RedirectView("/login?registered", true);
        } catch (Exception ex) {
            return new RedirectView("/register?error", true);
        }
    }
}
