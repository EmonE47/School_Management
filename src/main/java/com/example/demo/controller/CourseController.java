package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CourseRequest;
import com.example.demo.model.Course;
import com.example.demo.model.Student;
import com.example.demo.service.SchoolService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final SchoolService schoolService;

    public CourseController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public List<Course> getCourses() {
        return schoolService.getAllCourses();
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public Course createCourse(@Valid @RequestBody CourseRequest request, Authentication authentication) {
        return schoolService.createCourseForTeacher(request, authentication.getName());
    }

    @PostMapping("/{courseId}/students/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Student enrollStudent(
        @PathVariable Long courseId,
        @PathVariable Long studentId
    ) {
        return schoolService.enrollStudentInCourse(studentId, courseId);
    }

    @PostMapping("/{courseId}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public Student enrollCurrentStudent(@PathVariable Long courseId, Authentication authentication) {
        return schoolService.enrollCurrentStudentInCourse(authentication.getName(), courseId);
    }
}
