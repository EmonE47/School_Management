package com.example.demo.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        boolean isTeacher = authentication.getAuthorities()
            .stream()
            .anyMatch(authority -> "ROLE_TEACHER".equals(authority.getAuthority()));

        if (isTeacher) {
            return "redirect:/teacher/dashboard";
        }
        return "redirect:/student/dashboard";
    }

    @ResponseBody
    @GetMapping(value = "/teacher/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public String teacherDashboard() {
        return dashboardHtml("Teacher Dashboard");
    }

    @ResponseBody
    @GetMapping(value = "/student/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public String studentDashboard() {
        return dashboardHtml("Student Dashboard");
    }

    private String dashboardHtml(String title) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body {
                        margin: 0;
                        min-height: 100vh;
                        display: grid;
                        place-items: center;
                        font-family: Arial, sans-serif;
                        background: #f2f4f8;
                    }
                    .card {
                        text-align: center;
                    }
                    h1 {
                        margin: 0 0 16px;
                        font-size: 24px;
                    }
                    button {
                        border: none;
                        border-radius: 6px;
                        padding: 12px 24px;
                        font-size: 16px;
                        cursor: pointer;
                        color: #fff;
                        background: #2f80ed;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <h1>%s</h1>
                    <form method="post" action="/logout" accept-charset="%s">
                        <button type="submit">Logout</button>
                    </form>
                </div>
            </body>
            </html>
            """.formatted(title, title, StandardCharsets.UTF_8.name());
    }
}
