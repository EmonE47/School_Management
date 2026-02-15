package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthPageController {

    @ResponseBody
    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String loginPage(
        Authentication authentication,
        @RequestParam(required = false) String error,
        @RequestParam(required = false) String logout,
        @RequestParam(required = false) String registered
    ) {
        if (authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)) {
            return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta http-equiv="refresh" content="0; url=/dashboard">
                </head>
                <body>Redirecting...</body>
                </html>
                """;
        }

        String message = "";
        if (error != null) {
            message = "<p style='color:#c62828;'>Invalid email or password.</p>";
        } else if (logout != null) {
            message = "<p style='color:#2e7d32;'>Logged out successfully.</p>";
        } else if (registered != null) {
            message = "<p style='color:#2e7d32;'>Registration successful. Please log in.</p>";
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Login</title>
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
                        width: 320px;
                        background: #fff;
                        padding: 24px;
                        border-radius: 10px;
                        box-shadow: 0 8px 24px rgba(0,0,0,.08);
                    }
                    h2 { margin: 0 0 16px; }
                    input, button, a {
                        width: 100%%;
                        box-sizing: border-box;
                        margin-top: 10px;
                        border-radius: 6px;
                        font-size: 14px;
                    }
                    input {
                        border: 1px solid #d0d7de;
                        padding: 10px;
                    }
                    button {
                        border: none;
                        padding: 10px;
                        color: #fff;
                        background: #2f80ed;
                        cursor: pointer;
                    }
                    a {
                        display: inline-block;
                        text-align: center;
                        text-decoration: none;
                        padding: 10px;
                        color: #2f80ed;
                        border: 1px solid #2f80ed;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <h2>Login</h2>
                    %s
                    <form method="post" action="/login">
                        <input type="text" name="username" placeholder="Email" required />
                        <input type="password" name="password" placeholder="Password" required />
                        <button type="submit">Login</button>
                    </form>
                    <a href="/register">Register</a>
                </div>
            </body>
            </html>
            """.formatted(message);
    }

    @ResponseBody
    @GetMapping(value = "/register", produces = MediaType.TEXT_HTML_VALUE)
    public String registerPage(@RequestParam(required = false) String error) {
        String message = "";
        if (error != null) {
            message = "<p style='color:#c62828;'>Registration failed. Check role or email data.</p>";
        }
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Register</title>
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
                        width: 360px;
                        background: #fff;
                        padding: 24px;
                        border-radius: 10px;
                        box-shadow: 0 8px 24px rgba(0,0,0,.08);
                    }
                    h2 { margin: 0 0 16px; }
                    input, select, button, a {
                        width: 100%%;
                        box-sizing: border-box;
                        margin-top: 10px;
                        border-radius: 6px;
                        font-size: 14px;
                    }
                    input, select {
                        border: 1px solid #d0d7de;
                        padding: 10px;
                    }
                    button {
                        border: none;
                        padding: 10px;
                        color: #fff;
                        background: #2f80ed;
                        cursor: pointer;
                    }
                    a {
                        display: inline-block;
                        text-align: center;
                        text-decoration: none;
                        padding: 10px;
                        color: #2f80ed;
                        border: 1px solid #2f80ed;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <h2>Register</h2>
                    <p style="margin: 0; font-size: 13px; color: #546e7a;">Student department is assigned automatically.</p>
                    %s
                    <form method="post" action="/register">
                        <input type="text" name="name" placeholder="Full name" required />
                        <input type="email" name="email" placeholder="Email" required />
                        <input type="password" name="password" placeholder="Password" minlength="6" required />
                        <select id="role" name="role" required>
                            <option value="STUDENT">STUDENT</option>
                            <option value="TEACHER">TEACHER</option>
                        </select>
                        <button type="submit">Create Account</button>
                    </form>
                    <a href="/login">Back to Login</a>
                </div>
            </body>
            </html>
            """.formatted(message);
    }
}
