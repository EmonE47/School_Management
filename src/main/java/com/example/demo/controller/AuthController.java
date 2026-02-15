package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.RegisterStudentRequest;
import com.example.demo.dto.RegisterTeacherRequest;
import com.example.demo.dto.RegisterUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.SchoolService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SchoolService schoolService;

    public AuthController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@Valid @RequestBody RegisterUserRequest request) {
        return schoolService.registerUser(request);
    }

    @PostMapping("/register/student")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        return schoolService.registerStudent(request);
    }

    @PostMapping("/register/teacher")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerTeacher(@Valid @RequestBody RegisterTeacherRequest request) {
        return schoolService.registerTeacher(request);
    }
}
