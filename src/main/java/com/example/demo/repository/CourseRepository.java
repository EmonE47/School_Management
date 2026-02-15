package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCodeIgnoreCase(String code);
    List<Course> findByTeacherEmailIgnoreCase(String teacherEmail);
    Optional<Course> findByIdAndTeacherEmailIgnoreCase(Long id, String teacherEmail);
}
