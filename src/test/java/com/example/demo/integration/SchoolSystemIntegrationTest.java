package com.example.demo.integration;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.model.Course;
import com.example.demo.model.Department;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherRepository;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SchoolSystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void setUpData() {
        studentRepository.deleteAll();
        courseRepository.deleteAll();
        teacherRepository.deleteAll();
        departmentRepository.deleteAll();

        Department department = new Department("General");
        department = departmentRepository.save(department);

        Teacher teacher = new Teacher();
        teacher.setName("Teacher One");
        teacher.setEmail("teacher1@example.com");
        teacher.setPassword("ignored");
        teacher.setRole(Role.TEACHER);
        teacherRepository.save(teacher);

        Student student = new Student();
        student.setName("Student One");
        student.setEmail("student1@example.com");
        student.setPassword("ignored");
        student.setRole(Role.STUDENT);
        student.setDepartment(department);
        studentRepository.save(student);
    }

    @Test
    void protectedRouteShouldRedirectUnauthenticatedUserToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void registerApiShouldCreateStudentUser() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "name", "New Student",
            "email", "newstudent@example.com",
            "password", "password123",
            "role", "STUDENT"
        ));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("newstudent@example.com"))
            .andExpect(jsonPath("$.role").value("STUDENT"));

        assertThat(studentRepository.existsByEmail("newstudent@example.com")).isTrue();
    }

    @Test
    void teacherShouldCreateCourseAndViewOwnCourses() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "code", "cse210",
            "title", "Database Systems"
        ));

        mockMvc.perform(post("/api/courses")
                .with(user("teacher1@example.com").roles("TEACHER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("CSE210"));

        mockMvc.perform(get("/api/teachers/me/courses")
                .with(user("teacher1@example.com").roles("TEACHER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("CSE210"))
            .andExpect(jsonPath("$[0].title").value("Database Systems"));
    }

    @Test
    void studentMustNotCreateCourse() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "code", "cse300",
            "title", "Operating Systems"
        ));

        mockMvc.perform(post("/api/courses")
                .with(user("student1@example.com").roles("STUDENT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isForbidden());
    }

    @Test
    void studentShouldEnrollAndSeeCourseInMyCourses() throws Exception {
        Teacher teacher = teacherRepository.findByEmail("teacher1@example.com").orElseThrow();
        Course course = new Course("CSE220", "Networks");
        course.setTeacher(teacher);
        course = courseRepository.save(course);

        mockMvc.perform(post("/api/courses/{courseId}/enroll", course.getId())
                .with(user("student1@example.com").roles("STUDENT")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("student1@example.com"));

        mockMvc.perform(get("/api/students/me/courses")
                .with(user("student1@example.com").roles("STUDENT")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("CSE220"));
    }
}
