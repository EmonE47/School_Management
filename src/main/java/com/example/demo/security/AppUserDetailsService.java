package com.example.demo.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public AppUserDetailsService(StudentRepository studentRepository, TeacherRepository teacherRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedEmail = username.trim().toLowerCase();

        Teacher teacher = teacherRepository.findByEmail(normalizedEmail).orElse(null);
        if (teacher != null) {
            return new AppUserDetails(teacher.getId(), teacher.getEmail(), teacher.getPassword(), teacher.getRole());
        }

        Student student = studentRepository.findByEmail(normalizedEmail).orElse(null);
        if (student != null) {
            return new AppUserDetails(student.getId(), student.getEmail(), student.getPassword(), student.getRole());
        }

        throw new UsernameNotFoundException("No user found with email: " + normalizedEmail);
    }
}
