package com.example.demo.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.example.demo.dto.CourseRequest;
import com.example.demo.model.Course;
import com.example.demo.model.Student;
import com.example.demo.service.SchoolService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private SchoolService schoolService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CourseController controller;

    @Test
    void getCoursesShouldReturnAllCourses() {
        Course course = new Course("CSE101", "Intro to Programming");
        when(schoolService.getAllCourses()).thenReturn(List.of(course));

        List<Course> result = controller.getCourses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("CSE101");
    }

    @Test
    void createCourseShouldUseAuthenticatedTeacher() {
        CourseRequest request = new CourseRequest("cse101", "Intro to Programming");
        Course created = new Course("CSE101", "Intro to Programming");
        when(authentication.getName()).thenReturn("teacher@example.com");
        when(schoolService.createCourseForTeacher(request, "teacher@example.com")).thenReturn(created);

        Course result = controller.createCourse(request, authentication);

        assertThat(result).isSameAs(created);
        verify(schoolService).createCourseForTeacher(request, "teacher@example.com");
    }

    @Test
    void enrollStudentShouldForwardPathVariablesInCorrectOrder() {
        Student student = new Student();
        when(schoolService.enrollStudentInCourse(11L, 7L)).thenReturn(student);

        Student result = controller.enrollStudent(7L, 11L);

        assertThat(result).isSameAs(student);
        verify(schoolService).enrollStudentInCourse(11L, 7L);
    }

    @Test
    void enrollCurrentStudentShouldUseAuthenticatedStudent() {
        Student student = new Student();
        when(authentication.getName()).thenReturn("student@example.com");
        when(schoolService.enrollCurrentStudentInCourse("student@example.com", 9L)).thenReturn(student);

        Student result = controller.enrollCurrentStudent(9L, authentication);

        assertThat(result).isSameAs(student);
        verify(schoolService).enrollCurrentStudentInCourse("student@example.com", 9L);
    }
}
