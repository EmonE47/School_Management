package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Student;
import com.example.demo.model.Course;
import com.example.demo.service.SchoolService;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final SchoolService schoolService;

    public StudentController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public List<Student> getStudents() {
        return schoolService.getAllStudents();
    }

    @GetMapping("/me/courses")
    @PreAuthorize("hasRole('STUDENT')")
    public List<Course> getMyCourses(Authentication authentication) {
        return schoolService.getCoursesForStudent(authentication.getName());
    }

    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable Long studentId) {
        schoolService.deleteStudent(studentId);
    }
}
