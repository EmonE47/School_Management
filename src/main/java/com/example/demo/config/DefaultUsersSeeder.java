package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.model.Department;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherRepository;

@Component
public class DefaultUsersSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUsersSeeder.class);

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.department-name:General}")
    private String defaultDepartmentName;

    @Value("${app.seed.teacher.name:Default Teacher}")
    private String defaultTeacherName;

    @Value("${app.seed.teacher.email:teacher@example.com}")
    private String defaultTeacherEmail;

    @Value("${app.seed.teacher.password:teacher123}")
    private String defaultTeacherPassword;

    @Value("${app.seed.student.name:Default Student}")
    private String defaultStudentName;

    @Value("${app.seed.student.email:student@example.com}")
    private String defaultStudentEmail;

    @Value("${app.seed.student.password:student123}")
    private String defaultStudentPassword;

    public DefaultUsersSeeder(
        TeacherRepository teacherRepository,
        StudentRepository studentRepository,
        DepartmentRepository departmentRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        try {
            Department department = departmentRepository.findByNameIgnoreCase(defaultDepartmentName.trim())
                .orElseGet(() -> departmentRepository.save(new Department(defaultDepartmentName.trim())));

            String teacherEmail = defaultTeacherEmail.trim().toLowerCase();
            if (!teacherRepository.existsByEmail(teacherEmail) && !studentRepository.existsByEmail(teacherEmail)) {
                Teacher teacher = new Teacher();
                teacher.setName(defaultTeacherName.trim());
                teacher.setEmail(teacherEmail);
                teacher.setPassword(passwordEncoder.encode(defaultTeacherPassword));
                teacher.setRole(Role.TEACHER);
                teacherRepository.save(teacher);
                logger.info("Seeded default teacher: {}", teacherEmail);
            }

            String studentEmail = defaultStudentEmail.trim().toLowerCase();
            if (!studentRepository.existsByEmail(studentEmail) && !teacherRepository.existsByEmail(studentEmail)) {
                Student student = new Student();
                student.setName(defaultStudentName.trim());
                student.setEmail(studentEmail);
                student.setPassword(passwordEncoder.encode(defaultStudentPassword));
                student.setRole(Role.STUDENT);
                student.setDepartment(department);
                studentRepository.save(student);
                logger.info("Seeded default student: {}", studentEmail);
            }
        } catch (RuntimeException ex) {
            logger.warn("Skipping default user seeding due to database permission/config issue: {}", ex.getMessage());
        }
    }
}
