package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.CourseRequest;
import com.example.demo.dto.RegisterTeacherRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.Course;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createCourseForTeacherShouldAssignTeacherAndNormalizeCode() {
        SchoolService schoolService = new SchoolService(
            studentRepository,
            teacherRepository,
            departmentRepository,
            courseRepository,
            passwordEncoder
        );

        Teacher teacher = new Teacher();
        teacher.setEmail("teacher@example.com");
        when(courseRepository.findByCodeIgnoreCase("cse101")).thenReturn(Optional.empty());
        when(teacherRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Course created = schoolService.createCourseForTeacher(
            new CourseRequest(" cse101 ", "Intro to Programming "),
            " Teacher@Example.com "
        );

        assertThat(created.getCode()).isEqualTo("CSE101");
        assertThat(created.getTitle()).isEqualTo("Intro to Programming");
        assertThat(created.getTeacher()).isSameAs(teacher);
    }

    @Test
    void enrollCurrentStudentInCourseShouldAddCourseToStudent() {
        SchoolService schoolService = new SchoolService(
            studentRepository,
            teacherRepository,
            departmentRepository,
            courseRepository,
            passwordEncoder
        );

        Student student = new Student();
        Course course = new Course("CSE101", "Intro to Programming");

        when(studentRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));
        when(studentRepository.save(student)).thenReturn(student);

        Student saved = schoolService.enrollCurrentStudentInCourse(" Student@Example.com ", 3L);

        assertThat(saved.getCourses()).contains(course);
        verify(studentRepository).save(student);
    }

    @Test
    void getEnrolledStudentsForTeacherCourseShouldReturnOnlyThatCourseStudents() {
        SchoolService schoolService = new SchoolService(
            studentRepository,
            teacherRepository,
            departmentRepository,
            courseRepository,
            passwordEncoder
        );

        Course course = new Course("CSE101", "Intro");
        Student first = new Student();
        first.setEmail("first@example.com");
        Student second = new Student();
        second.setEmail("second@example.com");
        course.setStudents(Set.of(first, second));

        when(courseRepository.findByIdAndTeacherEmailIgnoreCase(5L, "teacher@example.com"))
            .thenReturn(Optional.of(course));

        List<Student> students = schoolService.getEnrolledStudentsForTeacherCourse(" Teacher@Example.com ", 5L);

        assertThat(students).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void registerTeacherShouldReturnTeacherResponse() {
        SchoolService schoolService = new SchoolService(
            studentRepository,
            teacherRepository,
            departmentRepository,
            courseRepository,
            passwordEncoder
        );

        when(studentRepository.existsByEmail("teacher@example.com")).thenReturn(false);
        when(teacherRepository.existsByEmail("teacher@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> {
            Teacher teacher = invocation.getArgument(0);
            teacher.setId(10L);
            return teacher;
        });

        UserResponse response = schoolService.registerTeacher(
            new RegisterTeacherRequest("Teacher", "TEACHER@example.com", "password123")
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.email()).isEqualTo("teacher@example.com");
        assertThat(response.role()).isEqualTo(Role.TEACHER.name());
        verify(teacherRepository).save(any(Teacher.class));
    }
}
