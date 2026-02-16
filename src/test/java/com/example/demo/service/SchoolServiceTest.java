package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dto.CourseRequest;
import com.example.demo.dto.RegisterUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.Course;
import com.example.demo.model.Department;
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

    private SchoolService schoolService;

    @BeforeEach
    void setUp() {
        schoolService = new SchoolService(
            studentRepository,
            teacherRepository,
            departmentRepository,
            courseRepository,
            passwordEncoder
        );
        ReflectionTestUtils.setField(schoolService, "defaultDepartmentName", "General");
    }

    @Test
    void createCourseForTeacherShouldAssignTeacherAndNormalizeCode() {
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
    void registerUserForStudentShouldUseDefaultDepartmentAndEncodePassword() {
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "ALICE@example.com",
            "password123",
            Role.STUDENT,
            null
        );
        Department department = new Department("General");
        department.setId(9L);

        when(studentRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(teacherRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(departmentRepository.findByNameIgnoreCase("General")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            student.setId(77L);
            return student;
        });

        UserResponse response = schoolService.registerUser(request);

        assertThat(response.id()).isEqualTo(77L);
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.role()).isEqualTo("STUDENT");

        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());
        Student savedStudent = studentCaptor.getValue();
        assertThat(savedStudent.getDepartment()).isSameAs(department);
        assertThat(savedStudent.getPassword()).isEqualTo("encoded-pass");
    }
}
