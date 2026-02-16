package com.example.demo.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardControllerTest {

    private final DashboardController controller = new DashboardController();

    @Test
    void dashboardShouldRedirectTeacherToTeacherDashboard() {
        Authentication teacherAuth = UsernamePasswordAuthenticationToken.authenticated(
            "teacher@example.com",
            "secret",
            List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );

        String redirect = controller.dashboard(teacherAuth);

        assertThat(redirect).isEqualTo("redirect:/teacher/dashboard");
    }

    @Test
    void dashboardShouldRedirectStudentToStudentDashboard() {
        Authentication studentAuth = UsernamePasswordAuthenticationToken.authenticated(
            "student@example.com",
            "secret",
            List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        String redirect = controller.dashboard(studentAuth);

        assertThat(redirect).isEqualTo("redirect:/student/dashboard");
    }

    @Test
    void teacherDashboardShouldContainTeacherFeatures() {
        String html = controller.teacherDashboard();

        assertThat(html).contains("Teacher Dashboard");
        assertThat(html).contains("Add Course");
        assertThat(html).contains("/api/courses");
        assertThat(html).contains("/api/teachers/me/courses");
    }

    @Test
    void studentDashboardShouldContainEnrollmentFeatures() {
        String html = controller.studentDashboard();

        assertThat(html).contains("Student Dashboard");
        assertThat(html).contains("Available Courses");
        assertThat(html).contains("/api/courses/");
        assertThat(html).contains("/api/students/me/courses");
    }
}
