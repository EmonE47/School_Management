ALTER TABLE courses
ADD COLUMN teacher_id BIGINT;

ALTER TABLE courses
ADD CONSTRAINT fk_courses_teacher
FOREIGN KEY (teacher_id)
REFERENCES teachers(id)
ON DELETE SET NULL;

CREATE INDEX idx_courses_teacher_id
ON courses(teacher_id);
