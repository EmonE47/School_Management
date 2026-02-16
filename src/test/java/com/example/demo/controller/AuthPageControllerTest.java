package com.example.demo.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

class AuthPageControllerTest {

    private final AuthPageController controller = new AuthPageController();

    @Test
    void loginPageShouldShowFormForUnauthenticatedUser() {
        String html = controller.loginPage(null, null, null, null);

        assertThat(html).contains("<h2>Login</h2>");
        assertThat(html).contains("form method=\"post\" action=\"/login\"");
        assertThat(html).contains("href=\"/register\"");
    }

    @Test
    void loginPageShouldShowErrorMessage() {
        String html = controller.loginPage(null, "1", null, null);

        assertThat(html).contains("Invalid email or password.");
    }

    @Test
    void loginPageShouldRedirectAuthenticatedUser() {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
            "teacher@example.com",
            "secret",
            List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );

        String html = controller.loginPage(authentication, null, null, null);

        assertThat(html).contains("url=/dashboard");
    }

    @Test
    void registerPageShouldContainRoleOptions() {
        String html = controller.registerPage(null);

        assertThat(html).contains("<h2>Register</h2>");
        assertThat(html).contains("option value=\"STUDENT\"");
        assertThat(html).contains("option value=\"TEACHER\"");
        assertThat(html).contains("form method=\"post\" action=\"/register\"");
    }
}
