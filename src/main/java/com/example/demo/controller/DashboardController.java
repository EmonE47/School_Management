package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        boolean isTeacher = authentication.getAuthorities()
            .stream()
            .anyMatch(authority -> "ROLE_TEACHER".equals(authority.getAuthority()));

        if (isTeacher) {
            return "redirect:/teacher/dashboard";
        }
        return "redirect:/student/dashboard";
    }

    @ResponseBody
    @GetMapping(value = "/teacher/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public String teacherDashboard() {
        return teacherDashboardHtml();
    }

    @ResponseBody
    @GetMapping(value = "/student/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public String studentDashboard() {
        return studentDashboardHtml();
    }

    private String teacherDashboardHtml() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Teacher Dashboard</title>
                <style>
                    body {
                        margin: 0;
                        font-family: Arial, sans-serif;
                        background: #f3f6fa;
                        color: #1f2937;
                    }
                    .page {
                        max-width: 960px;
                        margin: 0 auto;
                        padding: 24px 16px 40px;
                    }
                    .topbar {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        gap: 12px;
                        margin-bottom: 16px;
                    }
                    h1 {
                        margin: 0;
                        font-size: 28px;
                    }
                    .grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
                        gap: 16px;
                    }
                    .card {
                        background: #fff;
                        border: 1px solid #d5dde8;
                        border-radius: 10px;
                        padding: 16px;
                    }
                    h2 {
                        margin-top: 0;
                        font-size: 18px;
                    }
                    .field {
                        display: grid;
                        gap: 6px;
                        margin-bottom: 10px;
                    }
                    label {
                        font-size: 13px;
                        font-weight: 600;
                    }
                    input {
                        border: 1px solid #c0ccda;
                        border-radius: 6px;
                        padding: 10px;
                        font-size: 14px;
                    }
                    .row {
                        display: flex;
                        gap: 8px;
                        flex-wrap: wrap;
                    }
                    ul {
                        list-style: none;
                        padding: 0;
                        margin: 0;
                        display: grid;
                        gap: 8px;
                    }
                    li {
                        border: 1px solid #e1e7ef;
                        border-radius: 8px;
                        padding: 10px;
                        background: #fbfcfe;
                    }
                    .muted {
                        color: #64748b;
                        font-size: 14px;
                    }
                    .message {
                        margin-top: 10px;
                        padding: 10px;
                        border-radius: 8px;
                        font-size: 14px;
                        display: none;
                    }
                    .ok {
                        background: #e8f7ea;
                        color: #166534;
                        border: 1px solid #a7e0af;
                    }
                    .err {
                        background: #feeaea;
                        color: #991b1b;
                        border: 1px solid #f3b0b0;
                    }
                    button {
                        border: none;
                        border-radius: 6px;
                        padding: 10px 14px;
                        font-size: 14px;
                        cursor: pointer;
                        color: #fff;
                        background: #2f80ed;
                    }
                    button.secondary {
                        background: #4b5563;
                    }
                </style>
            </head>
            <body>
                <div class="page">
                    <div class="topbar">
                        <h1>Teacher Dashboard</h1>
                        <form method="post" action="/logout">
                            <button type="submit" class="secondary">Logout</button>
                        </form>
                    </div>

                    <div class="grid">
                        <section class="card">
                            <h2>Add Course</h2>
                            <div class="field">
                                <label for="code">Course Code</label>
                                <input id="code" type="text" placeholder="CSE101">
                            </div>
                            <div class="field">
                                <label for="title">Course Title</label>
                                <input id="title" type="text" placeholder="Introduction to Programming">
                            </div>
                            <div class="row">
                                <button type="button" onclick="createCourse()">Create Course</button>
                                <button type="button" class="secondary" onclick="loadMyCourses()">Refresh Courses</button>
                            </div>
                            <div id="teacherMessage" class="message"></div>
                        </section>

                        <section class="card">
                            <h2>My Courses</h2>
                            <ul id="courseList"></ul>
                            <p id="courseEmpty" class="muted">No courses added yet.</p>
                        </section>
                    </div>

                    <section class="card" style="margin-top:16px;">
                        <h2>Students In Selected Course</h2>
                        <ul id="studentList"></ul>
                        <p id="studentEmpty" class="muted">Choose a course to view enrolled students.</p>
                    </section>
                </div>

                <script>
                    async function api(url, options) {
                        const res = await fetch(url, options || {});
                        const contentType = res.headers.get('content-type') || '';
                        const body = contentType.includes('application/json')
                            ? await res.json()
                            : await res.text();
                        if (!res.ok) {
                            throw new Error(typeof body === 'string' ? body : JSON.stringify(body));
                        }
                        return body;
                    }

                    function showMessage(text, isError) {
                        const box = document.getElementById('teacherMessage');
                        box.textContent = text;
                        box.className = 'message ' + (isError ? 'err' : 'ok');
                        box.style.display = 'block';
                    }

                    function clearMessage() {
                        const box = document.getElementById('teacherMessage');
                        box.style.display = 'none';
                        box.textContent = '';
                    }

                    async function createCourse() {
                        clearMessage();
                        const code = document.getElementById('code').value.trim();
                        const title = document.getElementById('title').value.trim();
                        if (!code || !title) {
                            showMessage('Course code and title are required.', true);
                            return;
                        }
                        try {
                            await api('/api/courses', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ code: code, title: title })
                            });
                            document.getElementById('code').value = '';
                            document.getElementById('title').value = '';
                            showMessage('Course created successfully.', false);
                            await loadMyCourses();
                        } catch (error) {
                            showMessage('Failed to create course. ' + error.message, true);
                        }
                    }

                    async function loadMyCourses() {
                        const list = document.getElementById('courseList');
                        const empty = document.getElementById('courseEmpty');
                        list.innerHTML = '';
                        try {
                            const courses = await api('/api/teachers/me/courses');
                            if (!courses.length) {
                                empty.style.display = 'block';
                                return;
                            }
                            empty.style.display = 'none';
                            courses.forEach(course => {
                                const li = document.createElement('li');
                                const info = document.createElement('div');
                                info.textContent = course.code + ' - ' + course.title;
                                const row = document.createElement('div');
                                row.className = 'row';
                                row.style.marginTop = '8px';
                                const btn = document.createElement('button');
                                btn.textContent = 'View Enrolled Students';
                                btn.onclick = () => loadStudentsForCourse(course.id);
                                row.appendChild(btn);
                                li.appendChild(info);
                                li.appendChild(row);
                                list.appendChild(li);
                            });
                        } catch (error) {
                            empty.style.display = 'block';
                            empty.textContent = 'Failed to load courses.';
                        }
                    }

                    async function loadStudentsForCourse(courseId) {
                        const list = document.getElementById('studentList');
                        const empty = document.getElementById('studentEmpty');
                        list.innerHTML = '';
                        try {
                            const students = await api('/api/teachers/me/courses/' + courseId + '/students');
                            if (!students.length) {
                                empty.style.display = 'block';
                                empty.textContent = 'No students enrolled in this course yet.';
                                return;
                            }
                            empty.style.display = 'none';
                            students.forEach(student => {
                                const li = document.createElement('li');
                                li.textContent = student.name + ' (' + student.email + ')';
                                list.appendChild(li);
                            });
                        } catch (error) {
                            empty.style.display = 'block';
                            empty.textContent = 'Failed to load enrolled students.';
                        }
                    }

                    loadMyCourses();
                </script>
            </body>
            </html>
            """;
    }

    private String studentDashboardHtml() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Student Dashboard</title>
                <style>
                    body {
                        margin: 0;
                        font-family: Arial, sans-serif;
                        background: #f3f6fa;
                        color: #1f2937;
                    }
                    .page {
                        max-width: 960px;
                        margin: 0 auto;
                        padding: 24px 16px 40px;
                    }
                    .topbar {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        gap: 12px;
                        margin-bottom: 16px;
                    }
                    h1 {
                        margin: 0;
                        font-size: 28px;
                    }
                    .grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
                        gap: 16px;
                    }
                    .card {
                        background: #fff;
                        border: 1px solid #d5dde8;
                        border-radius: 10px;
                        padding: 16px;
                    }
                    h2 {
                        margin-top: 0;
                        font-size: 18px;
                    }
                    ul {
                        list-style: none;
                        padding: 0;
                        margin: 0;
                        display: grid;
                        gap: 8px;
                    }
                    li {
                        border: 1px solid #e1e7ef;
                        border-radius: 8px;
                        padding: 10px;
                        background: #fbfcfe;
                    }
                    .row {
                        display: flex;
                        gap: 8px;
                        flex-wrap: wrap;
                    }
                    .muted {
                        color: #64748b;
                        font-size: 14px;
                    }
                    button {
                        border: none;
                        border-radius: 6px;
                        padding: 10px 14px;
                        font-size: 14px;
                        cursor: pointer;
                        color: #fff;
                        background: #2f80ed;
                    }
                    button.secondary {
                        background: #4b5563;
                    }
                </style>
            </head>
            <body>
                <div class="page">
                    <div class="topbar">
                        <h1>Student Dashboard</h1>
                        <form method="post" action="/logout">
                            <button type="submit" class="secondary">Logout</button>
                        </form>
                    </div>

                    <div class="row" style="margin-bottom: 16px;">
                        <button type="button" onclick="loadAllCourses()">Refresh Available Courses</button>
                        <button type="button" class="secondary" onclick="loadMyCourses()">Refresh My Courses</button>
                    </div>

                    <div class="grid">
                        <section class="card">
                            <h2>Available Courses</h2>
                            <ul id="allCourseList"></ul>
                            <p id="allCourseEmpty" class="muted">No available courses.</p>
                        </section>

                        <section class="card">
                            <h2>My Enrolled Courses</h2>
                            <ul id="myCourseList"></ul>
                            <p id="myCourseEmpty" class="muted">You are not enrolled in any course yet.</p>
                        </section>
                    </div>
                </div>

                <script>
                    async function api(url, options) {
                        const res = await fetch(url, options || {});
                        const contentType = res.headers.get('content-type') || '';
                        const body = contentType.includes('application/json')
                            ? await res.json()
                            : await res.text();
                        if (!res.ok) {
                            throw new Error(typeof body === 'string' ? body : JSON.stringify(body));
                        }
                        return body;
                    }

                    async function loadAllCourses() {
                        const list = document.getElementById('allCourseList');
                        const empty = document.getElementById('allCourseEmpty');
                        list.innerHTML = '';
                        try {
                            const courses = await api('/api/courses');
                            if (!courses.length) {
                                empty.style.display = 'block';
                                return;
                            }
                            empty.style.display = 'none';
                            courses.forEach(course => {
                                const li = document.createElement('li');
                                const title = document.createElement('div');
                                title.textContent = course.code + ' - ' + course.title;
                                const row = document.createElement('div');
                                row.className = 'row';
                                row.style.marginTop = '8px';
                                const enroll = document.createElement('button');
                                enroll.textContent = 'Enroll';
                                enroll.onclick = async () => {
                                    enroll.disabled = true;
                                    try {
                                        await api('/api/courses/' + course.id + '/enroll', { method: 'POST' });
                                        await loadMyCourses();
                                    } catch (error) {
                                        alert('Enrollment failed. ' + error.message);
                                    } finally {
                                        enroll.disabled = false;
                                    }
                                };
                                row.appendChild(enroll);
                                li.appendChild(title);
                                li.appendChild(row);
                                list.appendChild(li);
                            });
                        } catch (error) {
                            empty.style.display = 'block';
                            empty.textContent = 'Failed to load available courses.';
                        }
                    }

                    async function loadMyCourses() {
                        const list = document.getElementById('myCourseList');
                        const empty = document.getElementById('myCourseEmpty');
                        list.innerHTML = '';
                        try {
                            const courses = await api('/api/students/me/courses');
                            if (!courses.length) {
                                empty.style.display = 'block';
                                return;
                            }
                            empty.style.display = 'none';
                            courses.forEach(course => {
                                const li = document.createElement('li');
                                li.textContent = course.code + ' - ' + course.title;
                                list.appendChild(li);
                            });
                        } catch (error) {
                            empty.style.display = 'block';
                            empty.textContent = 'Failed to load enrolled courses.';
                        }
                    }

                    loadAllCourses();
                    loadMyCourses();
                </script>
            </body>
            </html>
            """;
    }
}
