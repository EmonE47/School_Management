package com.example.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CourseRequest;
import com.example.demo.dto.DepartmentRequest;
import com.example.demo.dto.RegisterStudentRequest;
import com.example.demo.dto.RegisterTeacherRequest;
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

@Service
@Transactional(readOnly = true)
public class SchoolService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.department-name:General}")
    private String defaultDepartmentName;

    public SchoolService(
        StudentRepository studentRepository,
        TeacherRepository teacherRepository,
        DepartmentRepository departmentRepository,
        CourseRepository courseRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse registerStudent(RegisterStudentRequest request) {
        return createStudent(request.name(), request.email(), request.password(), request.departmentId());
    }

    @Transactional
    public UserResponse registerTeacher(RegisterTeacherRequest request) {
        return createTeacher(request.name(), request.email(), request.password());
    }

    @Transactional
    public UserResponse registerUser(RegisterUserRequest request) {
        return switch (request.role()) {
            case STUDENT -> createStudent(request.name(), request.email(), request.password(), request.departmentId());
            case TEACHER -> createTeacher(request.name(), request.email(), request.password());
        };
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        if (departmentRepository.findByNameIgnoreCase(request.name().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already exists");
        }
        Department department = new Department(request.name().trim());
        return departmentRepository.save(department);
    }

    @Transactional
    public Course createCourse(CourseRequest request) {
        if (courseRepository.findByCodeIgnoreCase(request.code().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course code already exists");
        }
        Course course = new Course(request.code().trim().toUpperCase(), request.title().trim());
        return courseRepository.save(course);
    }

    @Transactional
    public Student enrollStudentInCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        student.getCourses().add(course);
        return studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        studentRepository.deleteById(studentId);
    }

    @Transactional
    public void deleteTeacher(Long teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found");
        }
        teacherRepository.deleteById(teacherId);
    }

    private void ensureEmailIsUnique(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        if (studentRepository.existsByEmail(normalizedEmail) || teacherRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }
    }

    private UserResponse createStudent(String name, String email, String rawPassword, Long departmentId) {
        ensureEmailIsUnique(email);
        Department department;
        if (departmentId == null) {
            department = departmentRepository.findByNameIgnoreCase(defaultDepartmentName.trim())
                .orElseGet(() -> departmentRepository.save(new Department(defaultDepartmentName.trim())));
        } else {
            department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        }

        Student student = new Student();
        student.setName(name.trim());
        student.setEmail(email.trim().toLowerCase());
        student.setPassword(passwordEncoder.encode(rawPassword));
        student.setRole(Role.STUDENT);
        student.setDepartment(department);

        Student saved = studentRepository.save(student);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole().name());
    }

    private UserResponse createTeacher(String name, String email, String rawPassword) {
        ensureEmailIsUnique(email);

        Teacher teacher = new Teacher();
        teacher.setName(name.trim());
        teacher.setEmail(email.trim().toLowerCase());
        teacher.setPassword(passwordEncoder.encode(rawPassword));
        teacher.setRole(Role.TEACHER);

        Teacher saved = teacherRepository.save(teacher);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole().name());
    }
}
