package com.example.demo.integration;

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

    private static final String TEACHER_EMAIL = "teacher1@example.com";
    private static final String STUDENT_EMAIL = "student1@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Long seededCourseId;

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
        teacher.setEmail(TEACHER_EMAIL);
        teacher.setPassword("ignored");
        teacher.setRole(Role.TEACHER);
        teacher = teacherRepository.save(teacher);

        Student student = new Student();
        student.setName("Student One");
        student.setEmail(STUDENT_EMAIL);
        student.setPassword("ignored");
        student.setRole(Role.STUDENT);
        student.setDepartment(department);
        studentRepository.save(student);

        Course seededCourse = new Course("CSE220", "Networks");
        seededCourse.setTeacher(teacher);
        seededCourse = courseRepository.save(seededCourse);
        seededCourseId = seededCourse.getId();
    }

    @Test
    void unauthenticatedUserShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void registerApiShouldCreateStudent() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "New Student",
                      "email": "newstudent@example.com",
                      "password": "password123",
                      "role": "STUDENT"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("newstudent@example.com"))
            .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void teacherShouldCreateCourse() throws Exception {
        mockMvc.perform(post("/api/courses")
                .with(user(TEACHER_EMAIL).roles("TEACHER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "cse210",
                      "title": "Database Systems"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("CSE210"));
    }

    @Test
    void studentShouldNotCreateCourse() throws Exception {
        mockMvc.perform(post("/api/courses")
                .with(user(STUDENT_EMAIL).roles("STUDENT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "cse300",
                      "title": "Operating Systems"
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void studentShouldEnrollAndSeeCourseInMyCourses() throws Exception {
        mockMvc.perform(post("/api/courses/{courseId}/enroll", seededCourseId)
                .with(user(STUDENT_EMAIL).roles("STUDENT")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(STUDENT_EMAIL));

        mockMvc.perform(get("/api/students/me/courses")
                .with(user(STUDENT_EMAIL).roles("STUDENT")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("CSE220"));
    }
}
