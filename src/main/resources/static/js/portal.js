const portalApi = async (url, options = {}) => {
    const response = await fetch(url, { headers: { "Content-Type": "application/json" }, ...options });
    if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(error.message || `API ${response.status}`);
    }
    return response.status === 204 ? null : response.json();
};

let evaluationClassRoster = [];
let evaluationDetailRequestId = 0;

const portalEscape = (value = "") => String(value).replace(/[&<>'"]/g, (character) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;"
}[character]));

const portalToast = (message) => {
    const toast = document.querySelector("#toast");
    toast.textContent = message;
    toast.style.display = "block";
    window.setTimeout(() => { toast.style.display = "none"; }, 2600);
};

const switchPortalView = () => {
    document.querySelectorAll("[data-view]").forEach((button) => button.addEventListener("click", (event) => {
        if (document.body.classList.contains("code-exam-active")) {
            event.preventDefault();
            portalToast("Hãy hoàn thành và nộp bài code trước khi chuyển màn hình.");
            return;
        }
        document.querySelectorAll("[data-view]").forEach((item) => item.classList.remove("active"));
        document.querySelectorAll(".view").forEach((item) => item.classList.remove("active"));
        button.classList.add("active");
        document.querySelector(`#${button.dataset.view}`).classList.add("active");
        document.querySelector("#page-title").textContent = button.dataset.title;
        if (button.dataset.view === "mentor-materials") loadMentorMaterials();
        document.body.classList.toggle("submission-page-active", button.dataset.view === "student-submit");
        if (button.dataset.view === "mentor-exams") loadExamSessions();
        if (button.dataset.view === "mentor-message") loadMentorChat().catch((error) => portalToast(error.message || "Không thể tải tin nhắn."));
        const mentorTrainingNav = document.querySelector("#mentor-training-nav");
        if (button.closest("#mentor-training-nav")) {
            mentorTrainingNav.classList.add("expanded");
            document.querySelector("#mentor-training-toggle")?.setAttribute("aria-expanded", "true");
            document.querySelector("#mentor-training-toggle")?.classList.add("active");
        } else {
            mentorTrainingNav?.classList.remove("expanded");
            document.querySelector("#mentor-training-toggle")?.setAttribute("aria-expanded", "false");
            document.querySelector("#mentor-training-toggle")?.classList.remove("active");
        }
        if (button.closest("#student-work-nav")) {
            document.querySelector("#student-work-nav")?.classList.add("expanded");
            document.querySelector("#student-work-toggle")?.setAttribute("aria-expanded", "true");
            document.querySelector("#student-work-toggle")?.classList.add("active");
        } else {
            document.querySelector("#student-work-toggle")?.classList.remove("active");
        }
    }));
    document.querySelector("#student-work-toggle")?.addEventListener("click", (event) => {
        const group = event.currentTarget.closest(".nav-group");
        const expanded = group.classList.toggle("expanded");
        event.currentTarget.setAttribute("aria-expanded", String(expanded));
        event.currentTarget.classList.toggle("active", expanded);
    });
    document.querySelector("#mentor-training-toggle")?.addEventListener("click", (event) => {
        const group = event.currentTarget.closest(".nav-group");
        const expanded = group.classList.toggle("expanded");
        event.currentTarget.setAttribute("aria-expanded", String(expanded));
        event.currentTarget.classList.toggle("active", expanded);
    });
    document.querySelectorAll("[data-test-mode]").forEach((button) => button.addEventListener("click", () => {
        document.querySelectorAll("[data-test-mode]").forEach((item) => {
            const active = item === button;
            item.classList.toggle("active", active);
            item.setAttribute("aria-selected", String(active));
        });
        document.querySelectorAll("[data-test-panel]").forEach((panel) => {
            const active = panel.dataset.testPanel === button.dataset.testMode;
            panel.hidden = !active;
            panel.classList.toggle("active", active);
        });
    }));
};

const loadClasses = async () => {
    const classes = await portalApi("/api/classes");
    document.querySelectorAll("select[data-classes]").forEach((select) => {
        select.innerHTML = classes.map((item) => `<option value="${item.id}">${portalEscape(item.name)} · ${portalEscape(item.term)}</option>`).join("");
    });
    document.querySelectorAll("select[data-students]").forEach((select) => {
        const selectedClass = classes.find((item) => String(item.id) === String(document.querySelector("[data-mentor-class]")?.value));
        select.innerHTML = (selectedClass?.students || []).map((item) => `<option value="${item.id}">${portalEscape(item.fullName)} (#${item.id})</option>`).join("") || "<option value=\"\">Lớp chưa có sinh viên</option>";
    });
    const list = document.querySelector("#class-list");
    if (list) list.innerHTML = classes.map((item) => `<li class="list-item"><div><div class="list-title">${portalEscape(item.name)}</div><div class="list-meta">${portalEscape(item.term)} · ${portalEscape(item.major)}</div></div><span class="badge">${item.students?.length || 0} SV</span></li>`).join("") || "<li class='subtle'>Chưa có lớp được phân công.</li>";
    renderEvaluationClassOptions(classes);
    const mentorClassFilter = document.querySelector("#exam-class-filter");
    if (mentorClassFilter) {
        const mentorId = document.querySelector("#mentor-id")?.value;
        const mentorClasses = classes.filter((item) => String(item.mentor?.id) === String(mentorId));
        mentorClassFilter.innerHTML = mentorClasses.map((item) => `<option value="${item.id}">${portalEscape(item.name)} · ${portalEscape(item.term)}</option>`).join("") || "<option value=\"\">Chưa có lớp phụ trách</option>";
        if (mentorClasses.length) {
            watchClass(mentorClassFilter.value);
            loadExamSessions().catch((error) => portalToast(error.message || "Không thể tải phiên kiểm tra."));
        }
    }
    const mentorChatClass = document.querySelector("#mentor-chat-class");
    if (mentorChatClass) {
        const mentorId = document.querySelector("#mentor-id")?.value;
        const mentorClasses = classes.filter((item) => String(item.mentor?.id) === String(mentorId));
        mentorChatClass.innerHTML = mentorClasses.map((item) =>
            `<option value="${item.id}">${portalEscape(item.name)} · ${portalEscape(item.term)}</option>`
        ).join("") || "<option value=\"\">Chưa có lớp phụ trách</option>";
        mentorChatClass.disabled = mentorClasses.length === 0;
        if (mentorClasses.length) loadMentorChat().catch((error) => portalToast(error.message || "Không thể tải tin nhắn."));
    }
    const attendanceClass = document.querySelector("#attendance-class");
    if (attendanceClass) {
        const mentorId = document.querySelector("#mentor-id")?.value;
        const mentorClasses = classes.filter((item) => String(item.mentor?.id) === String(mentorId));
        attendanceClass.innerHTML = mentorClasses.map((item) =>
            `<option value="${item.id}">${portalEscape(item.name)} · ${portalEscape(item.term)}</option>`
        ).join("") || "<option value=\"\">Chưa có lớp phụ trách</option>";
        attendanceClass.disabled = mentorClasses.length === 0;
        if (mentorClasses.length) {
            await loadAttendanceRoster().catch((error) => portalToast(error.message || "Không thể tải danh sách điểm danh."));
        } else {
            document.querySelector("#attendance-roster").innerHTML =
                "<tr><td colspan=\"2\" class=\"subtle\">Chưa có lớp phụ trách.</td></tr>";
        }
    }
    if (document.querySelector("[data-mentor-class]")) {
        await loadMentorData().catch((error) => portalToast(error.message || "Không thể tải dữ liệu lớp."));
    }
};

let examSessionsRequestId = 0;
let examSessionsRefreshTimer;

const loadExamSessions = async () => {
    const classId = document.querySelector("#exam-class-filter")?.value;
    const mentorId = document.querySelector("#mentor-id")?.value;
    const table = document.querySelector("#exam-session-table");
    if (!table) return;
    const requestId = ++examSessionsRequestId;
    if (!classId || !mentorId) {
        table.innerHTML = "<tr><td colspan=\"6\" class=\"subtle\">Chưa có lớp phụ trách.</td></tr>";
        return;
    }
    try {
        const sessions = await portalApi(`/api/exams/class/${classId}?mentorId=${mentorId}`);
        if (requestId !== examSessionsRequestId
                || String(document.querySelector("#exam-class-filter")?.value) !== String(classId)) return;
        table.innerHTML = sessions.length ? sessions.map((session) => `
            <tr>
                <td><strong>${portalEscape(session.studentName)}</strong><small class="table-subline">#${session.studentId}</small></td>
                <td>${portalEscape(session.assignmentTitle)}</td>
                <td><span class="exam-state ${session.status === "REVIEW_REQUIRED" ? "review" : "locked"}">${session.status === "REVIEW_REQUIRED" ? "Cần xem xét" : "Đã khóa"}</span><small class="table-subline">${session.violationCount || 0} sự kiện</small></td>
                <td>${portalEscape(session.lastViolation || "—")}</td>
                <td>${portalEscape(session.updatedAt ? new Date(session.updatedAt).toLocaleString("vi-VN") : "—")}</td>
                <td><div class="exam-actions"><button class="btn" type="button" data-exam-decision="RESUME" data-session-id="${session.id}">Cho tiếp tục</button><button class="btn secondary" type="button" data-exam-decision="LOCK" data-session-id="${session.id}">Khóa bài</button></div></td>
            </tr>`).join("") : "<tr><td colspan=\"6\" class=\"subtle\">Không có phiên nào đang chờ duyệt hoặc bị khóa.</td></tr>";
        table.querySelectorAll("[data-exam-decision]").forEach((button) => button.addEventListener("click", async () => {
            button.disabled = true;
            try {
                await portalApi(`/api/exams/${button.dataset.sessionId}/decision`, {
                    method: "POST", body: JSON.stringify({ mentorId: Number(mentorId), decision: button.dataset.examDecision })
                });
                portalToast(button.dataset.examDecision === "RESUME" ? "Đã cho phép tiếp tục bài kiểm tra." : "Đã khóa bài kiểm tra.");
                await loadExamSessions();
            } catch (error) {
                button.disabled = false;
                portalToast(error.message || "Không thể cập nhật phiên kiểm tra.");
            }
        }));
    } catch (error) {
        if (requestId !== examSessionsRequestId) return;
        table.innerHTML = `<tr><td colspan="6" class="subtle">${portalEscape(error.message || "Không thể tải phiên kiểm tra.")}</td></tr>`;
    }
};

const startExamSessionAutoRefresh = () => {
    if (examSessionsRefreshTimer || !document.querySelector("#exam-session-table")) return;
    examSessionsRefreshTimer = window.setInterval(() => {
        const monitorOpen = document.querySelector("#mentor-exams")?.classList.contains("active");
        if (monitorOpen && document.visibilityState === "visible") {
            loadExamSessions().catch((error) => console.error("Không thể cập nhật phiên kiểm tra:", error));
        }
    }, 2500);
};

const loadMentorData = async () => {
    const classId = document.querySelector("[data-mentor-class]")?.value;
    if (!classId) return;
    const [assignments, schedules, evaluations] = await Promise.all([
        portalApi(`/api/assignments/class/${classId}`), portalApi(`/api/schedules/class/${classId}`), portalApi(`/api/evaluations/class/${classId}`)
    ]);
    const assignmentList = document.querySelector("#assignment-list");
    const scheduleList = document.querySelector("#schedule-list");
    if (assignmentList) assignmentList.innerHTML = assignments.map((item) => `<li class="list-item"><div><div class="list-title">${portalEscape(item.title)}</div><div class="list-meta">Thời hạn mở bài: ${portalEscape(item.deadline || "Chưa đặt")}${item.durationMinutes ? ` · Thời gian làm bài: ${item.durationMinutes} phút` : ""}</div></div><span class="badge">Bài tập</span></li>`).join("") || "<li class='subtle'>Chưa có bài tập.</li>";
    if (scheduleList) scheduleList.innerHTML = schedules.map((item) => `<li class="list-item"><div><div class="list-title">${portalEscape(item.title)}</div><div class="list-meta">${portalEscape(item.startsAt)} → ${portalEscape(item.endsAt)}</div></div><span class="badge">${portalEscape(item.type)}</span></li>`).join("") || "<li class='subtle'>Chưa có lịch trình.</li>";
    const evaluationCount = document.querySelector("#evaluation-count");
    if (evaluationCount) evaluationCount.textContent = evaluations.length;
};

const updateStudentOptions = async (classId = document.querySelector("[data-mentor-class]")?.value) => {
    if (!classId) return;
    const trainingClass = (await portalApi("/api/classes")).find((item) => String(item.id) === String(classId));
    document.querySelectorAll("select[data-students]").forEach((select) => {
        select.innerHTML = (trainingClass?.students || []).map((item) => `<option value="${item.id}">${portalEscape(item.fullName)} (#${item.id})</option>`).join("") || "<option value=\"\">Lớp chưa có sinh viên</option>";
    });
};

const renderEvaluationClassOptions = (classes) => {
    const select = document.querySelector("#evaluation-class-filter");
    const list = document.querySelector("#evaluation-student-list");
    if (!select || !list) return;
    const mentorId = document.querySelector("#mentor-id")?.value;
    const mentorClasses = classes.filter((item) => String(item.mentor?.id) === String(mentorId));
    evaluationClassRoster = mentorClasses;
    select.innerHTML = mentorClasses.map((item) =>
        `<option value="${item.id}">${portalEscape(item.name)} · ${portalEscape(item.term)}</option>`
    ).join("") || "<option value=\"\">Chưa có lớp phụ trách</option>";
    select.disabled = mentorClasses.length === 0;
    if (!mentorClasses.length) {
        list.innerHTML = "<p class='subtle'>Chưa có lớp phụ trách.</p>";
        return;
    }
    renderEvaluationStudents(mentorClasses[0]);
};

const renderEvaluationStudents = (trainingClass) => {
    const list = document.querySelector("#evaluation-student-list");
    const detail = document.querySelector("#evaluation-student-detail");
    if (!list || !detail) return;
    evaluationDetailRequestId += 1;
    detail.hidden = true;
    detail.replaceChildren();
    list.dataset.classId = trainingClass.id;
    list.innerHTML = (trainingClass.students || []).map((student) => `
        <article class="evaluation-student-row">
            <div>
                <strong>${portalEscape(student.fullName)}</strong>
                <small>Mã sinh viên: ${student.id}</small>
            </div>
            <button class="btn secondary" type="button" data-evaluation-student="${student.id}" data-student-name="${portalEscape(student.fullName)}">Chi tiết</button>
        </article>`).join("") || "<p class='subtle'>Lớp này chưa có sinh viên.</p>";
};

const renderEvaluationPie = (title, entries, detail = "") => {
    const total = entries.reduce((sum, entry) => sum + entry.count, 0);
    let accumulatedPercent = 0;
    const segments = total
        ? entries.map((entry) => {
            const startPercent = accumulatedPercent;
            accumulatedPercent += (entry.count / total) * 100;
            return `${entry.color} ${startPercent}% ${accumulatedPercent}%`;
        }).join(", ")
        : "#dce6e2 0% 100%";
    return `<article class="evaluation-chart-card">
        <h3>${title}</h3>
        <div class="evaluation-chart-layout">
            <div class="evaluation-pie" style="background:conic-gradient(${segments})">
                <span>${total}</span><small>Tổng</small>
            </div>
            <ul class="evaluation-chart-legend">
                ${entries.map((entry) => `<li><i style="--legend-color:${entry.color}"></i><span>${entry.label}</span><strong>${entry.count}</strong></li>`).join("")}
            </ul>
        </div>
        ${detail ? `<p class="evaluation-chart-detail">${detail}</p>` : ""}
    </article>`;
};

const renderCodeAssignmentPrompt = (assignment) => assignment.description
    ? `<div class="evaluation-code-prompt"><strong>Đề bài / yêu cầu của giáo viên</strong><p>${portalEscape(assignment.description)}</p></div>`
    : "<p class='subtle'>Giáo viên chưa nhập đề bài hoặc yêu cầu cho bài code này.</p>";

const loadEvaluationStudentDetail = async (studentId, studentName, classId) => {
    const detail = document.querySelector("#evaluation-student-detail");
    const list = document.querySelector("#evaluation-student-list");
    const requestId = ++evaluationDetailRequestId;
    detail.hidden = false;
    list.hidden = true;
    detail.innerHTML = "<p class='subtle'>Đang tải chi tiết sinh viên...</p>";
    try {
        const [attendanceRecords, assignments, submissions] = await Promise.all([
            portalApi(`/api/attendance/student/${studentId}`),
            portalApi(`/api/assignments/class/${classId}`),
            portalApi(`/api/submissions/student/${studentId}`)
        ]);
        if (requestId !== evaluationDetailRequestId) return;
        const attendance = attendanceRecords.filter((item) => String(item.classId) === String(classId));
        const tests = assignments.filter((assignment) => assignment.type === "QUIZ" || assignment.type === "CODE");
        const classSubmissions = submissions.filter((submission) =>
            assignments.some((assignment) => assignment.id === submission.assignmentId)
        );
        const submissionsByAssignment = new Map(classSubmissions.map((submission) => [submission.assignmentId, submission]));
        const attendanceCounts = {
            present: attendance.filter((item) => item.status === "PRESENT").length,
            late: attendance.filter((item) => item.status === "LATE").length,
            excused: attendance.filter((item) => item.status === "EXCUSED").length,
            absent: attendance.filter((item) => item.status === "ABSENT").length
        };
        const attendanceDeduction = (attendanceCounts.late + attendanceCounts.excused) * 0.5 + attendanceCounts.absent;
        const turnedIn = assignments.filter((assignment) => submissionsByAssignment.has(assignment.id)).length;
        const notTurnedIn = Math.max(assignments.length - turnedIn, 0);
        const scoredTests = tests.map((assignment) => ({
            assignment,
            submission: submissionsByAssignment.get(assignment.id)
        })).filter(({ submission }) => submission?.score != null);
        const average = scoredTests.length
            ? scoredTests.reduce((sum, item) => sum + item.submission.score, 0) / scoredTests.length
            : null;
        const testRows = tests.map((assignment) => {
            const submission = submissionsByAssignment.get(assignment.id);
            if (!submission) {
                return `<article class="evaluation-test-card">
                    <div class="evaluation-test-head"><div><strong>${portalEscape(assignment.title)}</strong><small>${assignment.type === "CODE" ? "Bài code" : "Quiz"}</small></div><span class="badge">Chưa nộp</span></div>
                    ${assignment.type === "CODE" ? renderCodeAssignmentPrompt(assignment) : ""}
                </article>`;
            }
            if (assignment.type === "QUIZ") {
                return `<article class="evaluation-test-card">
                    <div class="evaluation-test-head"><div><strong>${portalEscape(assignment.title)}</strong><small>Quiz · Đã chấm tự động</small></div><span class="badge">${submission.score ?? "—"} / ${assignment.maxScore}</span></div>
                </article>`;
            }
            return `<article class="evaluation-test-card">
                <div class="evaluation-test-head"><div><strong>${portalEscape(assignment.title)}</strong><small>Bài code · ${portalEscape(submission.language || assignment.language || "Không rõ ngôn ngữ")}</small></div><span class="badge">${submission.score == null ? "Chờ chấm" : `${submission.score} / ${assignment.maxScore}`}</span></div>
                ${renderCodeAssignmentPrompt(assignment)}
                <p class="list-meta">Nộp lúc: ${portalEscape(submission.submittedAt ? new Date(submission.submittedAt).toLocaleString("vi-VN") : "—")}</p>
                <label for="submitted-code-${submission.id}">Mã nguồn sinh viên đã nộp</label>
                <textarea id="submitted-code-${submission.id}" class="evaluation-code" rows="12" readonly>${portalEscape(submission.code || "")}</textarea>
                <form class="evaluation-grade-form" data-submission-id="${submission.id}" data-max-score="${assignment.maxScore}">
                    <div class="form-grid">
                        <div><label for="grade-score-${submission.id}">Điểm (tối đa ${assignment.maxScore})</label><input id="grade-score-${submission.id}" name="score" type="number" min="0" max="${assignment.maxScore}" step="0.1" value="${submission.score ?? ""}" required></div>
                        <div class="evaluation-feedback-field"><label for="grade-feedback-${submission.id}">Nhận xét</label><textarea id="grade-feedback-${submission.id}" name="feedback" rows="2" placeholder="Nhận xét về bài làm..." required>${portalEscape(submission.feedback || "")}</textarea></div>
                    </div>
                    <div class="form-actions"><button class="btn" type="submit">Lưu điểm và nhận xét</button></div>
                </form>
            </article>`;
        }).join("");
        detail.dataset.studentId = studentId;
        detail.dataset.classId = classId;
        detail.innerHTML = `
            <div class="panel-head evaluation-detail-head">
                <div><div class="eyebrow">Hồ sơ sinh viên</div><h2>${portalEscape(studentName)}</h2><p class="subtle">Mã sinh viên: ${studentId}</p></div>
                <button id="evaluation-back" class="btn secondary" type="button">Quay lại danh sách</button>
            </div>
            <div class="evaluation-summary">
                ${renderEvaluationPie("Chuyên cần", [
                    { label: "Có mặt", count: attendanceCounts.present, color: "#218a70" },
                    { label: "Đi trễ (-0,5)", count: attendanceCounts.late, color: "#e6a45b" },
                    { label: "Vắng có phép (-0,5)", count: attendanceCounts.excused, color: "#5b8def" },
                    { label: "Vắng học (-1)", count: attendanceCounts.absent, color: "#d96c6c" }
                ], `Điểm chuyên cần bị trừ: ${attendanceDeduction.toLocaleString("vi-VN")} điểm`)}
                ${renderEvaluationPie("Bài tập đã nộp", [
                    { label: "Đã nộp", count: turnedIn, color: "#218a70" },
                    { label: "Chưa nộp", count: notTurnedIn, color: "#dce6e2" }
                ])}
                <article class="evaluation-average-card"><span>Điểm trung bình bài thi</span><strong>${average == null ? "Chưa có điểm" : average.toFixed(2)}</strong><small>${scoredTests.length} bài quiz/code đã có điểm · Trung bình cộng điểm từng bài</small></article>
            </div>
            <div class="evaluation-test-list">
                <h3>Bài quiz và bài code</h3>
                ${testRows || "<p class='subtle'>Lớp chưa có bài quiz hoặc bài code.</p>"}
            </div>`;
        list.hidden = true;
    } catch (error) {
        if (requestId !== evaluationDetailRequestId) return;
        detail.innerHTML = `<p class="subtle">${portalEscape(error.message || "Không thể tải chi tiết sinh viên.")}</p><button id="evaluation-back" class="btn secondary" type="button">Quay lại danh sách</button>`;
    }
};

const attendanceStatuses = [
    { value: "LATE", label: "Đi trễ" },
    { value: "PRESENT", label: "Có mặt" },
    { value: "EXCUSED", label: "Có phép" },
    { value: "ABSENT", label: "Vắng" }
];

const loadAttendanceRoster = async () => {
    const classId = document.querySelector("#attendance-class")?.value;
    const date = document.querySelector("#attendance-date")?.value;
    const roster = document.querySelector("#attendance-roster");
    if (!roster) return;
    if (!classId || !date) {
        roster.innerHTML = "<tr><td colspan=\"5\" class=\"subtle\">Chọn lớp và ngày điểm danh.</td></tr>";
        return;
    }

    roster.innerHTML = "<tr><td colspan=\"5\" class=\"subtle\">Đang tải danh sách học sinh...</td></tr>";
    try {
        const [classes, attendances] = await Promise.all([
            portalApi("/api/classes"),
            portalApi(`/api/attendance/class/${classId}`)
        ]);
        if (String(document.querySelector("#attendance-class")?.value) !== String(classId)
                || document.querySelector("#attendance-date")?.value !== date) return;

        const trainingClass = classes.find((item) => String(item.id) === String(classId));
        const students = trainingClass?.students || [];
        const attendanceByStudent = new Map(
            attendances.filter((item) => item.date === date).map((item) => [String(item.studentId), item.status])
        );
        roster.innerHTML = students.length ? students.map((student) => `
            <tr data-attendance-row="${student.id}">
                <td><span class="attendance-student-name">${portalEscape(student.fullName)}</span><small class="table-subline">#${student.id}</small></td>
                ${attendanceStatuses.map((status) => `
                    <td class="attendance-action-cell">
                        <button class="attendance-choice${attendanceByStudent.get(String(student.id)) === status.value ? " selected" : ""}" type="button" data-attendance-status="${status.value}" aria-pressed="${attendanceByStudent.get(String(student.id)) === status.value}" aria-label="${status.label} - ${portalEscape(student.fullName)}">${status.label}</button>
                    </td>`).join("")}
            </tr>`).join("") : "<tr><td colspan=\"5\" class=\"subtle\">Lớp này chưa có học sinh.</td></tr>";
    } catch (error) {
        roster.innerHTML = `<tr><td colspan="5" class="subtle">${portalEscape(error.message || "Không thể tải danh sách học sinh.")}</td></tr>`;
        throw error;
    }
};

const saveAttendanceRoster = async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const classId = form.querySelector("#attendance-class").value;
    const date = form.querySelector("#attendance-date").value;
    const rows = [...form.querySelectorAll("[data-attendance-row]")];
    const button = form.querySelector("#attendance-save");
    if (!rows.length) return portalToast("Chưa có danh sách học sinh để điểm danh.");
    const incomplete = rows.find((row) => !row.querySelector(".attendance-choice.selected"));
    if (incomplete) {
        incomplete.querySelector(".attendance-choice")?.focus();
        return portalToast("Hãy chọn trạng thái điểm danh cho từng học sinh.");
    }

    button.disabled = true;
    try {
        const results = await Promise.allSettled(rows.map((row) => portalApi("/api/attendance", {
            method: "POST",
            body: JSON.stringify({
                classId: Number(classId),
                studentId: Number(row.dataset.attendanceRow),
                date,
                status: row.querySelector(".attendance-choice.selected").dataset.attendanceStatus
            })
        })));
        const failed = results.filter((result) => result.status === "rejected");
        if (failed.length) {
            const reason = failed[0].reason;
            const detail = reason instanceof Error ? reason.message : "Lỗi máy chủ không xác định";
            portalToast(`Đã lưu ${results.length - failed.length}/${results.length} học sinh; ${failed.length} bản ghi lỗi: ${detail}`);
            await loadAttendanceRoster();
            return;
        }
        portalToast(`Đã lưu điểm danh cho ${results.length} học sinh ngày ${new Date(`${date}T00:00:00`).toLocaleDateString("vi-VN")}.`);
        await loadAttendanceRoster();
    } catch (error) {
        portalToast(error.message || "Không thể lưu điểm danh.");
    } finally {
        button.disabled = false;
    }
};

const submitJsonForm = (selector, url, transform, successMessage) => {
    document.querySelector(selector)?.addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = event.currentTarget;
        const button = form.querySelector("button[type=submit]");
        const formData = Object.fromEntries(new FormData(form));
        button.disabled = true;
        try {
            await portalApi(url, { method: "POST", body: JSON.stringify(transform(formData)) });
            form.reset();
            if (formData.classId) {
                document.querySelectorAll("[data-mentor-class]").forEach((select) => {
                    select.value = formData.classId;
                });
            }
            portalToast(successMessage);
            await loadMentorData(); await updateStudentOptions();
        } catch (error) {
            portalToast(error.message || "Dữ liệu chưa hợp lệ.");
        } finally { button.disabled = false; }
    });
};

const setupAssignmentEditor = () => {
    const form = document.querySelector("#mentor-assignment-form");
    if (!form) return;
    const typeSelect = form.querySelector("#assignment-type");
    const descriptionField = form.querySelector("#assignment-description-field");
    const descriptionInput = descriptionField.querySelector("[name=description]");
    const descriptionLabel = form.querySelector("#assignment-description-label");
    const languageField = form.querySelector("#assignment-language-field");
    const languageInput = languageField.querySelector("[name=language]");
    const durationField = form.querySelector("#assignment-duration-field");
    const durationInput = durationField.querySelector("[name=durationMinutes]");
    const quizEditor = form.querySelector("#quiz-question-editor");
    const questionList = form.querySelector("#quiz-question-list");
    const questionTemplate = form.querySelector("#quiz-question-template");
    const syncQuestionCards = () => {
        const cards = [...questionList.querySelectorAll(".quiz-authoring-card")];
        cards.forEach((card, index) => {
            card.querySelector("legend").textContent = `Câu hỏi ${index + 1}`;
            const removeButton = card.querySelector(".quiz-question-remove");
            removeButton.hidden = cards.length === 1;
            removeButton.disabled = cards.length === 1;
        });
    };
    const addQuestionCard = () => {
        questionList.append(questionTemplate.content.cloneNode(true));
        syncQuestionCards();
    };
    const updateTypeFields = () => {
        const isCode = typeSelect.value === "CODE";
        const isQuiz = typeSelect.value === "QUIZ";
        const isTimedExam = isCode || isQuiz;
        descriptionField.hidden = !typeSelect.value;
        descriptionLabel.textContent = isCode ? "Đề bài và yêu cầu code" : "Mô tả";
        descriptionInput.placeholder = isCode ? "Mô tả bài toán, yêu cầu đầu vào/đầu ra và tiêu chí hoàn thành" : "Yêu cầu và tiêu chí đánh giá";
        descriptionInput.required = isCode;
        languageField.hidden = !isCode;
        languageInput.required = isCode;
        durationField.hidden = !isTimedExam;
        durationInput.required = isTimedExam;
        quizEditor.hidden = !isQuiz;
        quizEditor.querySelectorAll("input, textarea, select").forEach((field) => {
            field.required = isQuiz;
        });
        if (isQuiz && !questionList.children.length) addQuestionCard();
    };
    typeSelect.addEventListener("change", updateTypeFields);
    updateTypeFields();
    form.querySelector("#add-quiz-question").addEventListener("click", addQuestionCard);
    questionList.addEventListener("click", (event) => {
        const removeButton = event.target.closest(".quiz-question-remove");
        if (!removeButton || questionList.children.length <= 1) return;
        removeButton.closest(".quiz-authoring-card").remove();
        syncQuestionCards();
    });
};

const injectStudentSubmissionControls = () => {
    return null;
};

const assignmentPageSize = 5;
const assignmentPages = { FILE: 1, QUIZ: 1, CODE: 1 };

const renderStudentAssignments = async (expandAssignmentId = null) => {
    const studentId = document.querySelector("#student-id")?.value;
    const lists = {
        FILE: document.querySelector("#student-file-list"),
        QUIZ: document.querySelector("#student-quiz-list"),
        CODE: document.querySelector("#student-code-list")
    };
    const classLabel = document.querySelector("#student-assignment-class");
    if (!studentId || !lists.FILE) return;
    try {
        const classes = await portalApi("/api/classes");
        const studentClass = classes.find((item) => (item.students || []).some((student) => String(student.id) === String(studentId)));
        if (!studentClass) { Object.values(lists).forEach((list) => { list.innerHTML = "<p class='subtle'>Bạn chưa được phân vào lớp nào.</p>"; }); return; }
        if (classLabel) classLabel.textContent = studentClass.name;
        const [assignments, submissions] = await Promise.all([
            portalApi(`/api/assignments/class/${studentClass.id}`), portalApi(`/api/submissions/student/${studentId}`)
        ]);
        const submissionByAssignment = new Map((submissions || []).map((item) => [String(item.assignmentId), item]));
        Object.entries(lists).forEach(([type, list]) => {
            const typeAssignments = assignments.filter((assignment) => assignment.type === type);
            const pageCount = Math.max(1, Math.ceil(typeAssignments.length / assignmentPageSize));
            assignmentPages[type] = Math.min(assignmentPages[type] || 1, pageCount);
            const currentPage = assignmentPages[type];
            const pageAssignments = typeAssignments.slice((currentPage - 1) * assignmentPageSize, currentPage * assignmentPageSize);
            list.innerHTML = typeAssignments.length ? pageAssignments.map((assignment) => {
            const submission = submissionByAssignment.get(String(assignment.id));
            const deadline = new Date(assignment.deadline);
            const deadlineValid = Number.isFinite(deadline.getTime());
            const expired = deadlineValid && deadline.getTime() < Date.now();
            const status = submission
                ? `Đã nộp${submission.score == null ? "" : ` · ${submission.score}/${assignment.maxScore} điểm`}`
                : (expired ? "Đã đóng · Chưa nộp" : "Chưa nộp");
            const deadlineLabel = deadlineValid
                ? `Thời hạn mở bài: ${deadline.toLocaleString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" })}`
                : "Chưa xác định thời hạn mở bài";
            const timeLimitLabel = assignment.durationMinutes ? ` · Thời gian làm bài: ${assignment.durationMinutes} phút` : "";
            const fileMeta = submission && (submission.fileName || submission.language)
                ? `<div class="assignment-submission-meta"><strong>Bài đã nộp:</strong> ${portalEscape(submission.fileName || "Bài làm hiện tại")} ${submission.submittedAt ? `· ${portalEscape(new Date(submission.submittedAt).toLocaleString("vi-VN"))}` : ""}</div>`
                : "";
            const form = assignment.type === "FILE"
                ? expired
                    ? `<div class="assignment-upload-box">${fileMeta}<p class="deadline-notice expired">${submission ? "Đã hết thời hạn mở bài. Không thể nộp lại bài này." : "Đã hết thời hạn mở bài. Hệ thống không nhận bài nộp mới."}</p></div>`
                    : `<div class="assignment-upload-box">${fileMeta}<div class="file-upload-zone"><div class="file-upload-intro"><span class="file-upload-icon" aria-hidden="true">↑</span><div><strong>${submission ? "Chọn tệp mới để nộp lại" : "Tải tệp bài làm lên"}</strong><small>Chọn tệp từ thiết bị của bạn</small></div></div><input id="assignment-file-${assignment.id}" class="assignment-file" type="file" accept=".pdf,.doc,.docx,.zip,.rar,.txt,.png,.jpg,.jpeg,.ppt,.pptx" aria-label="Chọn tệp bài làm" required><span class="file-selected-name" data-file-name>Chưa chọn tệp</span></div><p class="deadline-notice">${deadlineLabel} · Dung lượng tối đa 10 MB.</p><button class="btn" data-submit-file type="button" disabled>${submission ? "Nộp lại bài" : "Nộp bài tập"}</button></div>`
                : assignment.type === "CODE"
                    ? submission
                        ? `<p class="deadline-notice">Bạn đã nộp bài kiểm tra này${submission.score == null ? "" : ` · ${submission.score}/${assignment.maxScore} điểm`}. Không thể làm lại.</p>`
                        : expired ? `<p class="deadline-notice expired">Đã hết thời hạn mở bài. Không thể bắt đầu bài kiểm tra.</p>` : `<p class="deadline-notice">${deadlineLabel}</p><button class="btn" data-start-code type="button">Bắt đầu kiểm tra</button>`
                    : submission
                        ? `<p class="deadline-notice">${expired ? "Đã hết thời hạn mở bài. Không thể làm lại bài trắc nghiệm." : "Bạn đã nộp bài trắc nghiệm này"}${submission.score == null ? "" : ` · ${submission.score}/${assignment.maxScore} điểm`}</p>${expired ? "" : `<button class="btn" data-start-quiz type="button">Làm lại trắc nghiệm</button>`}`
                        : expired ? `<p class="deadline-notice expired">Đã hết thời hạn mở bài. Không thể bắt đầu bài kiểm tra.</p>` : `<p class="deadline-notice">${deadlineLabel}</p><div class="quiz-questions" data-questions></div><button class="btn" data-start-quiz type="button">Bắt đầu trắc nghiệm</button>`;
            const isFile = assignment.type === "FILE";
            const expanded = isFile && String(expandAssignmentId) === String(assignment.id);
            const fileAction = isFile
                ? `<button class="btn ${expanded ? "" : "secondary"} assignment-open-button" data-toggle-submission type="button" aria-expanded="${expanded}" ${expired && !submission ? "disabled" : ""}>${expired ? (submission ? "Bài đã nộp · đã đóng" : "Đã đóng") : (expanded ? "Đóng phần nộp" : (submission ? "Xem / nộp lại" : "Mở bài tập"))}</button>`
                : "";
            const fileForm = isFile ? `<div class="assignment-form" data-submission-panel ${expanded ? "" : "hidden"}>${form}</div>` : `<div class="assignment-form">${form}</div>`;
            return `<article class="assignment-card${expired ? " is-expired" : ""}" data-assignment-id="${assignment.id}" data-assignment-type="${assignment.type}" data-assignment-language="${portalEscape(assignment.language || "")}"><div class="assignment-card-head"><div><h3>${portalEscape(assignment.title)}</h3><p class="list-meta">${portalEscape(deadlineLabel + timeLimitLabel)}</p></div><span class="badge assignment-status ${submission ? "submitted" : (expired ? "expired" : "pending")}">${portalEscape(status)}</span></div>${assignment.description ? `<p class="subtle assignment-description">${portalEscape(assignment.description)}</p>` : ""}<div class="assignment-card-actions">${fileAction}</div>${fileForm}</article>`;
            }).join("") : "<p class='subtle'>Chưa có bài tập dạng này.</p>";
            if (typeAssignments.length > assignmentPageSize) {
                list.insertAdjacentHTML("beforeend", `<nav class="assignment-pagination" aria-label="Phân trang bài ${type === "FILE" ? "nộp" : "kiểm tra"}"><span>Trang ${currentPage} / ${pageCount}<small>${typeAssignments.length} bài tập</small></span><div><button class="btn secondary" type="button" data-assignment-page="${currentPage - 1}" ${currentPage === 1 ? "disabled" : ""} aria-label="Trang trước">←</button><button class="btn secondary" type="button" data-assignment-page="${currentPage + 1}" ${currentPage === pageCount ? "disabled" : ""} aria-label="Trang sau">→</button></div></nav>`);
                list.querySelectorAll("[data-assignment-page]").forEach((button) => button.addEventListener("click", () => {
                    assignmentPages[type] = Number(button.dataset.assignmentPage);
                    renderStudentAssignments();
                }));
            }
            list.querySelectorAll("[data-toggle-submission]").forEach((button) => button.addEventListener("click", () => {
                const panel = button.closest(".assignment-card").querySelector("[data-submission-panel]");
                panel.hidden = !panel.hidden;
                button.setAttribute("aria-expanded", String(!panel.hidden));
                button.textContent = panel.hidden ? (submissionByAssignment.has(String(button.closest("[data-assignment-id]").dataset.assignmentId)) ? "Xem / nộp lại" : "Mở bài tập") : "Đóng phần nộp";
                button.classList.toggle("secondary", panel.hidden);
            }));
            list.querySelectorAll("[data-submit-file]").forEach((button) => button.addEventListener("click", () => submitStudentFile(button, studentId)));
            list.querySelectorAll(".assignment-file").forEach((input) => input.addEventListener("change", () => {
                const uploadZone = input.closest(".file-upload-zone");
                const fileName = uploadZone.querySelector("[data-file-name]");
                const submitButton = input.closest(".assignment-card").querySelector("[data-submit-file]");
                const selectedFile = input.files?.[0];
                fileName.textContent = selectedFile ? selectedFile.name : "Chưa chọn tệp";
                uploadZone.classList.toggle("has-file", Boolean(selectedFile));
                submitButton.disabled = !selectedFile;
            }));
            list.querySelectorAll("[data-start-quiz]").forEach((button) => button.addEventListener("click", () => loadStudentQuiz(button, studentId)));
            list.querySelectorAll("[data-start-code]").forEach((button) => button.addEventListener("click", () => startCodeExam(button, studentId)));
        });
    } catch (error) { Object.values(lists).forEach((list) => { list.innerHTML = `<p class="subtle">${portalEscape(error.message || "Không thể tải bài tập.")}</p>`; }); }
};

const submitStudentFile = async (button, studentId) => {
    const card = button.closest("[data-assignment-id]");
    const file = card.querySelector(".assignment-file").files[0];
    if (!file) return portalToast("Hãy chọn file bài làm.");
    const body = new FormData(); body.append("assignmentId", card.dataset.assignmentId); body.append("studentId", studentId); body.append("file", file);
    try { await portalApi("/api/submissions/file", { method: "POST", headers: {}, body }); portalToast("Đã lưu bài nộp đúng hạn."); await renderStudentAssignments(card.dataset.assignmentId); } catch (error) { portalToast(error.message); }
};

let activeExamSession = null;
let examAwaitingMentor = false;
let examMarkupBeforeLock = "";
let examDecisionPoll = null;
let examCountdownInterval = null;
let examSecurityListenersInstalled = false;

const examOverlay = () => document.querySelector("#code-exam-overlay");
const examContent = () => document.querySelector("#code-exam-content");

const renderExamLock = (reason, permanentlyLocked = false) => {
    const content = examContent();
    if (!content) return;
    content.innerHTML = `<div class="exam-security-lock"><span class="exam-security-lock-mark">!</span><h2>${permanentlyLocked ? "Bài kiểm tra đã bị khóa" : "Phiên thi tạm khóa"}</h2><p>${portalEscape(reason || (permanentlyLocked ? "Mentor đã quyết định khóa bài thi này." : "Đã ghi nhận sự kiện rời màn hình kiểm tra."))}</p><p>${permanentlyLocked ? "Bạn không thể tiếp tục phiên thi này." : "Thông tin đã được gửi cho mentor. Hãy chờ quyết định cho phép tiếp tục."}</p></div>`;
};

const returnStudentToHome = async (message) => {
    activeExamSession = null;
    examAwaitingMentor = false;
    if (examDecisionPoll) window.clearInterval(examDecisionPoll);
    if (examCountdownInterval) window.clearInterval(examCountdownInterval);
    examDecisionPoll = null;
    examCountdownInterval = null;
    document.body.classList.remove("exam-session-active", "code-exam-active", "exam-security-locked");
    examOverlay().hidden = true;
    if (document.fullscreenElement) await document.exitFullscreen().catch(() => {});
    document.querySelector('[data-view="student-home"]')?.click();
    if (message) portalToast(message);
};

const pollExamDecision = () => {
    if (examDecisionPoll) window.clearInterval(examDecisionPoll);
    examDecisionPoll = window.setInterval(async () => {
        if (!activeExamSession || !examAwaitingMentor) return;
        try {
            const state = await portalApi(`/api/exams/${activeExamSession.id}/student/${activeExamSession.studentId}`);
            activeExamSession = state;
            if (state.status === "TIME_EXPIRED") {
                window.clearInterval(examDecisionPoll);
                examDecisionPoll = null;
                await returnStudentToHome("Đã hết thời gian làm bài. Phiên thi đã kết thúc.");
            } else if (state.status === "LOCKED") {
                window.clearInterval(examDecisionPoll);
                examDecisionPoll = null;
                await returnStudentToHome("Giáo viên đã khóa bài thi. Bạn đã được đưa về trang chủ.");
            } else if (state.status === "ACTIVE") {
                window.clearInterval(examDecisionPoll);
                examDecisionPoll = null;
                const content = examContent();
                content.innerHTML = `<div class="exam-security-approved">Mentor đã cho phép tiếp tục. Bấm nút bên dưới để tải bài thi và tiếp tục làm bài.<br><button class="btn" id="exam-resume-button" type="button">Tiếp tục làm bài</button></div><div id="resumed-exam-content" aria-live="polite"></div>`;
                const resumeButton = document.querySelector("#exam-resume-button");
                resumeButton?.addEventListener("click", async () => {
                    if (!resumeButton || resumeButton.disabled) return;
                    resumeButton.disabled = true;
                    try {
                        if (document.documentElement.requestFullscreen) await document.documentElement.requestFullscreen();
                        if (!document.fullscreenElement) throw new Error("Trình duyệt chưa cho phép bật toàn màn hình.");
                        const examContentHost = document.querySelector("#resumed-exam-content");
                        examContentHost.innerHTML = "<p class='exam-loading'>Đang tải bài thi và câu hỏi...</p>";
                        await renderExamFromDatabase(state, examContentHost);
                        examAwaitingMentor = false;
                        activeExamSession = state;
                        document.body.classList.remove("exam-security-locked");
                        document.querySelector(".exam-security-approved")?.remove();
                    } catch (error) {
                        const examContentHost = document.querySelector("#resumed-exam-content");
                        if (examContentHost) {
                            examContentHost.innerHTML = `<p class="deadline-notice expired">${portalEscape(error.message || "Không thể tải bài thi từ cơ sở dữ liệu.")}</p>`;
                        }
                        portalToast(error.message || "Không thể mở bài thi.");
                    } finally {
                        if (resumeButton.isConnected) resumeButton.disabled = false;
                    }
                });
            }
        } catch (_) { /* Keep the locked screen until the server can be reached. */ }
    }, 1800);
};

const flagExamViolation = async (reason) => {
    if (!activeExamSession || examAwaitingMentor || activeExamSession.status !== "ACTIVE") return;
    examAwaitingMentor = true;
    examMarkupBeforeLock = examContent()?.innerHTML || "";
    document.body.classList.add("exam-security-locked");
    renderExamLock(reason);
    try {
        activeExamSession = await portalApi(`/api/exams/${activeExamSession.id}/violation`, {
            method: "POST", body: JSON.stringify({ studentId: activeExamSession.studentId, reason })
        });
    } catch (error) {
        portalToast("Không gửi được cảnh báo đến máy chủ. Màn hình vẫn bị khóa.");
    }
    pollExamDecision();
};

const installExamSecurityListeners = () => {
    if (examSecurityListenersInstalled) return;
    examSecurityListenersInstalled = true;
    document.addEventListener("visibilitychange", () => {
        if (document.visibilityState === "hidden") flagExamViolation("Chuyển sang tab khác hoặc ẩn cửa sổ bài kiểm tra");
    });
    window.addEventListener("blur", () => {
        if (activeExamSession && document.visibilityState === "visible") {
            flagExamViolation("Cửa sổ bài kiểm tra mất tiêu điểm");
        }
    });
    document.addEventListener("fullscreenchange", () => {
        if (activeExamSession && !document.fullscreenElement && !examAwaitingMentor) flagExamViolation("Thoát chế độ toàn màn hình");
    });
    window.addEventListener("keydown", (event) => {
        if (!activeExamSession) return;
        const key = event.key.toLowerCase();
        const screenshotShortcut = event.key === "PrintScreen"
            || (event.shiftKey && event.metaKey && ["3", "4", "5", "s"].includes(key))
            || (event.shiftKey && event.ctrlKey && key === "s");
        if (!screenshotShortcut) return;
        event.preventDefault();
        flagExamViolation("Phát hiện phím hoặc tổ hợp phím chụp màn hình");
    }, true);
    document.addEventListener("contextmenu", (event) => {
        if (!activeExamSession) return;
        event.preventDefault();
        flagExamViolation("Mở menu ngữ cảnh trong bài kiểm tra");
    });
};

const startSecureExam = async (assignmentId, studentId, title, mode) => {
    let fullscreenRequest = null;
    const overlay = examOverlay();
    try {
        document.body.classList.add("exam-session-active", "code-exam-active");
        overlay.hidden = false;
        document.querySelector("#exam-mode-label").textContent = mode;
        document.querySelector("#code-exam-title").textContent = title;
        examContent().innerHTML = "<p class='exam-loading'>Đang mở bài kiểm tra an toàn...</p>";

        if (document.documentElement.requestFullscreen) {
            fullscreenRequest = document.documentElement.requestFullscreen().then(() => true).catch(() => false);
        }
        const session = await portalApi("/api/exams/start", {
            method: "POST", body: JSON.stringify({ assignmentId: Number(assignmentId), studentId: Number(studentId) })
        });
        if (session.status !== "ACTIVE") {
            throw new Error(session.status === "TIME_EXPIRED"
                ? "Đã hết thời gian làm bài, không thể bắt đầu lại."
                : "Phiên thi hiện không thể bắt đầu.");
        }
        activeExamSession = session;
        installExamSecurityListeners();
        if (!fullscreenRequest || !(await fullscreenRequest)) {
            portalToast("Trình duyệt không bật được toàn màn hình; bài thi vẫn sẽ mở trong cửa sổ hiện tại.");
        }
        return session;
    } catch (error) {
        if (!activeExamSession) {
            if (fullscreenRequest) await fullscreenRequest;
            overlay.hidden = true;
            document.body.classList.remove("exam-session-active", "code-exam-active");
            if (document.fullscreenElement) await document.exitFullscreen().catch(() => {});
        }
        throw error;
    }
};

const finishSecureExam = async (message) => {
    activeExamSession = null;
    examAwaitingMentor = false;
    if (examDecisionPoll) window.clearInterval(examDecisionPoll);
    if (examCountdownInterval) window.clearInterval(examCountdownInterval);
    examDecisionPoll = null;
    examCountdownInterval = null;
    document.body.classList.remove("exam-session-active", "code-exam-active", "exam-security-locked");
    examOverlay().hidden = true;
    if (document.fullscreenElement) await document.exitFullscreen().catch(() => {});
    portalToast(message);
    await renderStudentAssignments();
};

const startExamCountdown = (session, host) => {
    if (!session?.timeLimitEndsAt) return;
    if (examCountdownInterval) window.clearInterval(examCountdownInterval);
    const countdown = document.createElement("p");
    countdown.className = "exam-countdown";
    countdown.setAttribute("role", "timer");
    host.prepend(countdown);
    const update = () => {
        const remaining = Math.max(0, new Date(session.timeLimitEndsAt).getTime() - Date.now());
        const totalSeconds = Math.ceil(remaining / 1000);
        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const seconds = totalSeconds % 60;
        const time = [hours, minutes, seconds].map((part) => String(part).padStart(2, "0")).join(":");
        countdown.textContent = `Thời gian còn lại: ${time}`;
        if (remaining > 0) return;
        window.clearInterval(examCountdownInterval);
        examCountdownInterval = null;
        host.querySelectorAll("input, textarea, button").forEach((element) => { element.disabled = true; });
        countdown.textContent = "Đã hết thời gian làm bài.";
        portalApi(`/api/exams/${session.assignmentId}/student/${session.studentId}/complete`, { method: "POST" })
            .catch((error) => portalToast(error.message || "Không thể kết thúc phiên thi."))
            .finally(() => finishSecureExam("Đã hết thời gian làm bài. Phiên thi đã kết thúc."));
    };
    update();
    if (Date.now() < new Date(session.timeLimitEndsAt).getTime()) {
        examCountdownInterval = window.setInterval(update, 1000);
    }
};

const renderQuizExam = async (assignmentId, studentId, host = examContent(), session = activeExamSession) => {
    host.innerHTML = "<p class='exam-loading'>Đang tải câu hỏi...</p>";
    const questions = await portalApi(`/api/submissions/quiz/${assignmentId}/questions`);
    if (!questions.length) throw new Error("Bài trắc nghiệm chưa có câu hỏi.");
    host.innerHTML = `<div class="exam-quiz-questions">${questions.map((question) => `<fieldset class="quiz-question"><legend>${portalEscape(question.questionText)}</legend>${["A", "B", "C", "D"].map((option) => `<label><input type="radio" name="question-${question.id}" value="${option}"> ${option}. ${portalEscape(question[`option${option}`])}</label>`).join("")}</fieldset>`).join("")}</div><div class="form-actions"><button class="btn" data-submit-quiz type="button">Nộp bài kiểm tra</button></div>`;
    host.querySelector("[data-submit-quiz]").addEventListener("click", async () => {
        const answers = {};
        questions.forEach((question) => {
            const answer = host.querySelector(`input[name="question-${question.id}"]:checked`);
            if (answer) answers[question.id] = answer.value;
        });
        try {
            const result = await portalApi(`/api/submissions/quiz/${assignmentId}/submit`, {
                method: "POST", body: JSON.stringify({ studentId: Number(studentId), answers })
            });
            await finishSecureExam(`Đã nộp bài kiểm tra. Điểm: ${result.score ?? 0}`);
        } catch (error) { portalToast(error.message); }
    });
    startExamCountdown(session, host);
};

const renderCodeExam = (assignment, studentId, host = examContent(), session = activeExamSession) => {
    host.innerHTML = `<p class="exam-language">Ngôn ngữ: <strong>${portalEscape(assignment.language || "Chưa xác định")}</strong></p><textarea id="code-exam-editor" rows="20" maxlength="200000" placeholder="Viết mã nguồn tại đây..." aria-label="Mã nguồn bài làm"></textarea><div class="form-actions"><button class="btn" id="code-exam-submit" type="button">Nộp bài và kết thúc</button></div>`;
    host.querySelector("#code-exam-submit").addEventListener("click", async () => {
        const code = host.querySelector("#code-exam-editor").value.trim();
        if (!code) return portalToast("Mã nguồn không được để trống.");
        try {
            await portalApi("/api/submissions/code", {
                method: "POST",
                body: JSON.stringify({ assignmentId: Number(assignment.id), studentId: Number(studentId), code })
            });
            await finishSecureExam("Đã nộp bài kiểm tra code.");
        } catch (error) { portalToast(error.message); }
    });
    startExamCountdown(session, host);
};

const renderExamFromDatabase = async (session, host) => {
    const assignments = await portalApi(`/api/assignments/class/${session.classId}`);
    const assignment = assignments.find((item) => String(item.id) === String(session.assignmentId));
    if (!assignment) throw new Error("Không tìm thấy bài thi trong lớp của bạn.");
    document.querySelector("#code-exam-title").textContent = assignment.title;
    if (assignment.type === "QUIZ") {
        document.querySelector("#exam-mode-label").textContent = "Bài kiểm tra trắc nghiệm";
        await renderQuizExam(assignment.id, session.studentId, host, session);
    } else if (assignment.type === "CODE") {
        document.querySelector("#exam-mode-label").textContent = "Bài kiểm tra bằng code";
        renderCodeExam(assignment, session.studentId, host, session);
    } else {
        throw new Error("Bài được giao không phải bài thi trắc nghiệm hoặc code.");
    }
};

const recoverActiveExam = async (studentId) => {
    try {
        const session = await portalApi(`/api/exams/student/${studentId}/active`);
        if (!session) return;
        if (session.status === "TIME_EXPIRED") {
            await returnStudentToHome("Đã hết thời gian làm bài. Phiên thi đã kết thúc.");
            return;
        }
        if (session.status === "LOCKED") {
            await returnStudentToHome("Phiên thi đã bị giáo viên khóa. Bạn đã được đưa về trang chủ.");
            return;
        }
        activeExamSession = session;
        examAwaitingMentor = true;
        document.body.classList.add("exam-session-active", "code-exam-active", "exam-security-locked");
        installExamSecurityListeners();
        examOverlay().hidden = false;
        document.querySelector("#exam-mode-label").textContent = "Khôi phục phiên kiểm tra";
        document.querySelector("#code-exam-title").textContent = session.assignmentTitle;
        renderExamLock(session.lastViolation || "Phát hiện phiên kiểm tra đang dở sau khi tải lại trang.", session.status === "LOCKED");
        if (session.status === "ACTIVE") {
            activeExamSession = await portalApi(`/api/exams/${session.id}/violation`, {
                method: "POST", body: JSON.stringify({ studentId: Number(studentId), reason: "Tải lại hoặc rời trang khi bài kiểm tra đang diễn ra" })
            });
        }
        if (session.status !== "LOCKED") pollExamDecision();
    } catch (_) { /* The student dashboard still works if no exam session exists. */ }
};

const startCodeExam = async (button, studentId) => {
    const card = button.closest("[data-assignment-id]");
    const assignmentId = Number(card.dataset.assignmentId);
    try {
        await startSecureExam(assignmentId, studentId, card.querySelector("h3").textContent, "Bài kiểm tra bằng code");
        renderCodeExam({
            id: assignmentId,
            language: card.dataset.assignmentLanguage || "Chưa xác định"
        }, studentId, examContent(), activeExamSession);
    } catch (error) { portalToast(error.message || "Không thể bắt đầu bài kiểm tra."); }
};

const loadStudentQuiz = async (button, studentId) => {
    const card = button.closest("[data-assignment-id]");
    const assignmentId = Number(card.dataset.assignmentId);
    button.disabled = true;
    button.textContent = "Đang mở bài thi...";
    try {
        await startSecureExam(assignmentId, studentId, card.querySelector("h3").textContent, "Bài kiểm tra trắc nghiệm");
        await renderQuizExam(assignmentId, studentId, examContent(), activeExamSession);
    } catch (error) {
        if (activeExamSession) {
            examContent().innerHTML = `<div class="exam-security-lock"><h2>Không tải được đề thi</h2><p>${portalEscape(error.message || "Không thể tải câu hỏi từ máy chủ.")}</p><button class="btn" id="retry-quiz-load" type="button">Tải lại câu hỏi</button></div>`;
            examContent().querySelector("#retry-quiz-load")?.addEventListener("click", async (event) => {
                const retryButton = event.currentTarget;
                retryButton.disabled = true;
                try {
                    await renderQuizExam(assignmentId, studentId, examContent(), activeExamSession);
                } catch (retryError) {
                    examContent().querySelector(".exam-security-lock p").textContent =
                        retryError.message || "Không thể tải câu hỏi từ máy chủ.";
                    retryButton.disabled = false;
                }
            });
        } else {
            portalToast(error.message || "Không thể bắt đầu bài kiểm tra.");
        }
    } finally {
        if (button.isConnected) {
            button.disabled = false;
            button.textContent = "Bắt đầu trắc nghiệm";
        }
    }
};

const formatDateKey = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

const formatDisplayDate = (value) => {
    if (!value) return "Chưa xác định";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return `${date.getDate().toString().padStart(2, "0")}/${(date.getMonth() + 1).toString().padStart(2, "0")}/${date.getFullYear()}`;
};

const loadStudentCalendar = async () => {
    const studentId = document.querySelector("#student-id")?.value;
    const calendar = document.querySelector("#student-calendar");
    const detailList = document.querySelector("#calendar-detail-list");
    const detailPanel = document.querySelector("#calendar-detail-panel");
    const monthLabel = document.querySelector("#calendar-month-label");
    const calendarClass = document.querySelector("#student-calendar-class");
    if (!calendar || !studentId || !detailList || !monthLabel) return;

    if (calendarClass) calendarClass.textContent = "Đang tải...";
    try {
        const classes = await portalApi("/api/classes");
        const studentClass = classes.find((item) => (item.students || []).some((student) => String(student.id) === String(studentId))) || classes[0];
        if (!studentClass) {
            calendar.innerHTML = "<div class='subtle'>Bạn chưa thuộc lớp nào.</div>";
            if (calendarClass) calendarClass.textContent = "Chưa có lớp";
            return;
        }

        if (calendarClass) calendarClass.textContent = studentClass.name || "Lớp của bạn";

        const [schedules, assignments] = await Promise.all([
            portalApi(`/api/schedules/class/${studentClass.id}`),
            portalApi(`/api/assignments/class/${studentClass.id}`)
        ]);

        const eventsByDate = new Map();
        (schedules || []).forEach((event) => {
            const startDate = new Date(event.startsAt);
            const nextAssignment = (assignments || [])
                .filter((item) => new Date(item.deadline) >= startDate)
                .sort((first, second) => new Date(first.deadline) - new Date(second.deadline))[0] || assignments?.[0];
            const deadlineText = nextAssignment ? ` · Thời hạn mở bài: ${nextAssignment.title} (${formatDisplayDate(nextAssignment.deadline)})` : "";
            const key = formatDateKey(startDate);
            const items = eventsByDate.get(key) || [];
            items.push({ type: "Lịch trình", title: event.title, detail: `${event.type} · ${formatDisplayDate(event.startsAt)} → ${formatDisplayDate(event.endsAt)}${deadlineText}` });
            eventsByDate.set(key, items);
        });
        (assignments || []).forEach((item) => {
            const deadlineDate = new Date(item.deadline);
            const key = formatDateKey(deadlineDate);
            const items = eventsByDate.get(key) || [];
            items.push({ type: "Deadline", title: item.title, detail: `Mở bài đến: ${formatDisplayDate(item.deadline)}` });
            eventsByDate.set(key, items);
        });

        const monthDate = new Date();
    monthDate.setDate(1);
        const selectedDate = formatDateKey(new Date());
    const monthStart = new Date(monthDate.getFullYear(), monthDate.getMonth(), 1);
    const monthEnd = new Date(monthDate.getFullYear(), monthDate.getMonth() + 1, 0);
    const startWeekDay = monthStart.getDay();
    const totalDays = monthEnd.getDate();
    const calendarDays = [];

    for (let i = 0; i < (startWeekDay === 0 ? 6 : startWeekDay - 1); i++) {
        calendarDays.push({ empty: true });
    }

    for (let day = 1; day <= totalDays; day++) {
        const key = formatDateKey(new Date(monthDate.getFullYear(), monthDate.getMonth(), day));
        const items = (eventsByDate.get(key) || []).slice(0, 2);
        calendarDays.push({
            day,
            key,
            items,
            hasEvent: items.length > 0,
            isToday: key === formatDateKey(new Date()),
            isSelected: key === selectedDate
        });
    }

    while (calendarDays.length % 7 !== 0) {
        calendarDays.push({ empty: true });
    }

    const weekdayNames = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];
    const weekHeader = weekdayNames.map((day) => `<div class="calendar-header-cell">${day}</div>`).join("");
    const daysHtml = calendarDays.map((cell) => {
        if (cell.empty) return '<div class="calendar-day empty"></div>';
        return `
            <button type="button" class="calendar-day ${cell.hasEvent ? "has-event" : ""} ${cell.isToday ? "today" : ""} ${cell.isSelected ? "selected" : ""}" data-date="${cell.key}">
                <span class="calendar-day-number">${cell.day}</span>
                ${cell.items.map((item) => `<span class="calendar-tag ${item.type === "Deadline" ? "deadline" : "schedule"}" title="${portalEscape(item.title)}">${item.type === "Deadline" ? "⏰ " : "📌 "}${portalEscape(item.type === "Deadline" ? `Thời hạn mở bài: ${item.title}` : item.title)}</span>`).join("")}
            </button>
        `;
    }).join("");

    calendar.innerHTML = `${weekHeader}${daysHtml}`;
    monthLabel.textContent = new Date(monthDate.getFullYear(), monthDate.getMonth(), 1).toLocaleDateString("vi-VN", { month: "long", year: "numeric" });

    const selectedItems = eventsByDate.get(selectedDate) || [];
    document.querySelector("#calendar-detail-title").textContent = `Chi tiết ngày ${formatDisplayDate(selectedDate)}`;
    detailList.innerHTML = selectedItems.length
        ? selectedItems.map((item) => `<li class="list-item"><div><div class="list-title">${portalEscape(item.title)}</div><div class="list-meta">${portalEscape(item.detail)}</div></div></li>`).join("")
        : "<li class='subtle'>Không có lịch trình hoặc thời hạn mở bài trong ngày này.</li>";

        calendar.querySelectorAll(".calendar-day:not(.empty)").forEach((button) => {
        button.addEventListener("click", () => {
            const date = button.dataset.date;
            const items = eventsByDate.get(date) || [];
            calendar.querySelectorAll(".calendar-day.selected").forEach((item) => item.classList.remove("selected"));
            button.classList.add("selected");
            document.querySelector("#calendar-detail-title").textContent = `Chi tiết ngày ${formatDisplayDate(date)}`;
            detailList.innerHTML = items.length
                ? items.map((item) => `<li class="list-item"><div><div class="list-title">${portalEscape(item.title)}</div><div class="list-meta">${portalEscape(item.detail)}</div></div></li>`).join("")
                : "<li class='subtle'>Không có lịch trình hoặc thời hạn mở bài trong ngày này.</li>";
            if (detailPanel) {
                detailPanel.classList.add("is-highlighted");
                detailPanel.classList.remove("attention");
                void detailPanel.offsetWidth;
                detailPanel.classList.add("attention");
                detailPanel.scrollIntoView({ behavior: "smooth", block: "nearest" });
            }
        });
        });

        document.querySelector("#calendar-prev")?.addEventListener("click", () => {
        monthDate.setMonth(monthDate.getMonth() - 1);
        loadStudentCalendar();
        });

        document.querySelector("#calendar-next")?.addEventListener("click", () => {
        monthDate.setMonth(monthDate.getMonth() + 1);
        loadStudentCalendar();
        });
    } catch (error) {
        if (calendarClass) calendarClass.textContent = "Không tải được lịch";
        calendar.innerHTML = `<div class="subtle">${portalEscape(error.message || "Không thể tải lịch trình.")}</div>`;
        detailList.innerHTML = "<li class='subtle'>Kiểm tra API lớp, lịch trình và deadline.</li>";
    }
};

const renderLearningMaterials = (container, materials) => {
    if (!materials.length) {
        container.innerHTML = "<p class='subtle'>Chưa có giáo trình nào được tải lên.</p>";
        return;
    }
    container.innerHTML = materials.map((material) => {
        const fileType = (material.fileType || "FILE").toUpperCase();
        const size = material.fileSize ? `${(material.fileSize / (1024 * 1024)).toFixed(1)} MB` : "Tài liệu";
        const icon = fileType.includes("PDF") ? "📄" : fileType.includes("PPT") ? "📊" : "📘";
        return `
            <div class="material-item">
                <div class="material-item-main">
                    <div class="material-icon">${icon}</div>
                    <div>
                        <div class="material-name">${portalEscape(material.title || material.fileName || "Tài liệu")}</div>
                        <div class="material-meta">${portalEscape(material.fileName || fileType)} · ${portalEscape(size)}</div>
                        ${material.description ? `<div class="material-meta">${portalEscape(material.description)}</div>` : ""}
                    </div>
                </div>
                <a class="material-download" href="${portalEscape(material.fileUrl)}" download="${portalEscape(material.fileName || "")}">Tải xuống</a>
            </div>
        `;
    }).join("");
};

const loadLearningMaterials = async (containerSelector) => {
    const container = document.querySelector(containerSelector);
    if (!container) return;
    try {
        const materials = await portalApi("/api/materials");
        renderLearningMaterials(container, materials);
    } catch (error) {
        container.innerHTML = `<p class="subtle">${portalEscape(error.message || "Không thể tải giáo trình.")}</p>`;
    }
};

const loadStudentMaterials = () => loadLearningMaterials("#student-material-list");
const loadMentorMaterials = () => loadLearningMaterials("#mentor-material-list");

let studentChatClient;
let studentChatGroup;
let studentChatUserId;

let mentorChatClient;
let mentorChatGroup;
let mentorChatSubscription;
let mentorChatUserId;

const subscribeMentorChatGroup = () => {
    if (!mentorChatClient?.connected || !mentorChatGroup) return;
    if (mentorChatSubscription) mentorChatSubscription.unsubscribe();
    mentorChatSubscription = mentorChatClient.subscribe(`/topic/groups/${mentorChatGroup.id}`, (frame) => {
        renderMentorChatMessage(JSON.parse(frame.body));
    });
};

const renderMentorChatMessage = (message) => {
    const host = document.querySelector("#mentor-chat-messages");
    if (!host || String(message.groupId) !== String(mentorChatGroup?.id)) return;
    if (message.id && host.querySelector(`[data-chat-message-id="${message.id}"]`)) return;
    host.querySelector(".subtle")?.remove();
    const item = document.createElement("div");
    item.className = `message${Number(message.senderId) === Number(mentorChatUserId) ? " mine" : ""}`;
    if (message.id) item.dataset.chatMessageId = message.id;
    item.textContent = `${message.senderName}: ${message.content}`;
    host.appendChild(item);
    host.scrollTop = host.scrollHeight;
};

const loadMentorChat = async () => {
    const select = document.querySelector("#mentor-chat-class");
    const host = document.querySelector("#mentor-chat-messages");
    const label = document.querySelector("#mentor-chat-group-label");
    const mentorId = document.querySelector("#mentor-id")?.value;
    const classId = select?.value;
    if (!select || !host || !mentorId || !classId) return;

    host.innerHTML = "<div class='subtle'>Đang tải tin nhắn...</div>";
    try {
        const group = await portalApi(`/api/chat/mentor/${mentorId}/class/${classId}/group`);
        if (String(select.value) !== String(classId)) return;
        mentorChatGroup = group;
        mentorChatUserId = Number(mentorId);
        label.textContent = `Lớp: ${group.className}`;
        const messages = await portalApi(`/api/chat/groups/${group.id}/messages?userId=${mentorId}`);
        if (String(select.value) !== String(classId)) return;
        host.innerHTML = messages.length ? "" : "<div class='subtle'>Chưa có tin nhắn trong lớp này.</div>";
        messages.forEach(renderMentorChatMessage);

        if (typeof SockJS !== "undefined" && typeof Stomp !== "undefined") {
            if (!mentorChatClient) {
                mentorChatClient = Stomp.over(new SockJS("/ws-chat"));
                mentorChatClient.debug = () => {};
                mentorChatClient.connect({}, subscribeMentorChatGroup, () => {
                    mentorChatSubscription = null;
                });
            } else if (mentorChatClient.connected) {
                subscribeMentorChatGroup();
            }
        }
    } catch (error) {
        mentorChatGroup = null;
        label.textContent = "Không thể kết nối cuộc trao đổi của lớp.";
        host.innerHTML = `<div class="subtle">${portalEscape(error.message || "Không thể tải tin nhắn.")}</div>`;
        throw error;
    }
};

const sendMentorChatMessage = async (event) => {
    event.preventDefault();
    const input = document.querySelector("#mentor-chat-input");
    const content = input.value.trim();
    if (!mentorChatGroup || !content) {
        portalToast("Hãy chọn lớp và nhập nội dung tin nhắn.");
        return;
    }
    const button = event.currentTarget.querySelector("button[type='submit']");
    button.disabled = true;
    try {
        const message = await portalApi(`/api/chat/groups/${mentorChatGroup.id}/messages`, {
            method: "POST",
            body: JSON.stringify({ senderId: mentorChatUserId, content })
        });
        renderMentorChatMessage(message);
        input.value = "";
    } catch (error) {
        portalToast(error.message || "Gửi tin nhắn thất bại.");
    } finally {
        button.disabled = false;
    }
};

const renderStudentChatMessage = (message) => {
    const host = document.querySelector("#student-chat-messages");
    if (!host) return;
    const empty = host.querySelector(".subtle");
    if (empty) empty.remove();
    const item = document.createElement("div");
    item.className = `message${Number(message.senderId) === Number(studentChatUserId) ? " mine" : ""}`;
    item.textContent = `${message.senderName}: ${message.content}`;
    host.appendChild(item);
    host.scrollTop = host.scrollHeight;
};

const loadStudentChat = async () => {
    const studentId = document.querySelector("#student-id")?.value;
    const host = document.querySelector("#student-chat-messages");
    const mentorLabel = document.querySelector("#student-chat-mentor");
    if (!studentId || !host) return;
    studentChatUserId = Number(studentId);
    try {
        studentChatGroup = await portalApi(`/api/chat/student/${studentId}/group`);
        mentorLabel.textContent = `Mentor hướng dẫn: ${portalEscape(studentChatGroup.mentor.fullName)}`;
        const messages = await portalApi(`/api/chat/groups/${studentChatGroup.id}/messages?userId=${studentId}`);
        host.innerHTML = messages.length ? "" : "<div class='subtle'>Chưa có tin nhắn. Hãy gửi lời chào mentor.</div>";
        messages.forEach(renderStudentChatMessage);
        if (typeof SockJS !== "undefined" && typeof Stomp !== "undefined") {
            studentChatClient = Stomp.over(new SockJS("/ws-chat"));
            studentChatClient.debug = () => {};
            studentChatClient.connect({}, () => {
                studentChatClient.subscribe(`/topic/groups/${studentChatGroup.id}`, (frame) => renderStudentChatMessage(JSON.parse(frame.body)));
            });
        }
    } catch (error) {
        mentorLabel.textContent = "Chưa có mentor hướng dẫn";
        host.innerHTML = `<div class="subtle">${portalEscape(error.message || "Không thể tải cuộc trao đổi.")}</div>`;
    }
};

const sendStudentChatMessage = async (event) => {
    event.preventDefault();
    const input = document.querySelector("#student-chat-input");
    const content = input.value.trim();
    if (!studentChatGroup || !content) return portalToast("Chưa kết nối được với mentor của lớp.");
    try {
        if (studentChatClient?.connected) {
            studentChatClient.send("/app/chat.send", {}, JSON.stringify({ groupId: studentChatGroup.id, senderId: studentChatUserId, content }));
        } else {
            renderStudentChatMessage(await portalApi(`/api/chat/groups/${studentChatGroup.id}/messages`, {
                method: "POST", body: JSON.stringify({ senderId: studentChatUserId, content })
            }));
        }
        input.value = "";
    } catch (error) {
        portalToast(error.message || "Gửi tin nhắn thất bại.");
    }
};

const loadStudentData = async () => {
    const studentId = document.querySelector("#student-id")?.value;
    if (!studentId) return;
    await loadStudentCalendar();
    await renderStudentAssignments();
    await loadStudentMaterials();
};

document.addEventListener("DOMContentLoaded", () => {
    switchPortalView();
    window.addEventListener("beforeunload", (event) => {
        if (!document.body.classList.contains("code-exam-active")) return;
        if (activeExamSession?.status === "ACTIVE") {
            const payload = JSON.stringify({ studentId: activeExamSession.studentId, reason: "Đóng hoặc tải lại trang trong khi đang làm bài kiểm tra" });
            const endpoint = `/api/exams/${activeExamSession.id}/violation`;
            const sent = navigator.sendBeacon?.(endpoint, new Blob([payload], { type: "application/json" }));
            if (!sent) fetch(endpoint, { method: "POST", headers: { "Content-Type": "application/json" }, body: payload, keepalive: true }).catch(() => {});
        }
        event.preventDefault();
        event.returnValue = "Bạn chưa hoàn thành bài kiểm tra.";
    });
    setupAssignmentEditor();
    injectStudentSubmissionControls();
    loadClasses().catch(() => portalToast("Không thể tải danh sách lớp."));
    document.querySelectorAll("select[data-classes]").forEach((select) => select.addEventListener("change", () => {
        if (select.matches("[data-mentor-class]")) {
            document.querySelectorAll("[data-mentor-class]").forEach((mentorSelect) => {
                if (mentorSelect !== select) mentorSelect.value = select.value;
            });
            loadMentorData().catch(() => portalToast("Không thể tải dữ liệu lớp."));
        }
        if (select.id !== "attendance-class") {
            updateStudentOptions(select.value).catch(() => portalToast("Không thể tải danh sách sinh viên."));
        }
    }));
    const attendanceDate = document.querySelector("#attendance-date");
    if (attendanceDate) {
        const today = formatDateKey(new Date());
        attendanceDate.value = today;
        attendanceDate.max = today;
        attendanceDate.addEventListener("change", () => {
            loadAttendanceRoster().catch((error) => portalToast(error.message || "Không thể tải điểm danh."));
        });
    }
    document.querySelector("#attendance-class")?.addEventListener("change", () => {
        loadAttendanceRoster().catch((error) => portalToast(error.message || "Không thể tải danh sách điểm danh."));
    });
    document.querySelector("#attendance-roster")?.addEventListener("click", (event) => {
        const choice = event.target.closest(".attendance-choice");
        if (!choice) return;
        const row = choice.closest("[data-attendance-row]");
        row.querySelectorAll(".attendance-choice").forEach((button) => {
            const selected = button === choice;
            button.classList.toggle("selected", selected);
            button.setAttribute("aria-pressed", String(selected));
        });
    });
    document.querySelector("#attendance-form")?.addEventListener("submit", saveAttendanceRoster);
    document.querySelector("#exam-class-filter")?.addEventListener("change", () => loadExamSessions());
    startExamSessionAutoRefresh();
    document.querySelector("#mentor-chat-class")?.addEventListener("change", () => {
        loadMentorChat().catch((error) => portalToast(error.message || "Không thể tải tin nhắn."));
    });
    document.querySelector("#mentor-chat-form")?.addEventListener("submit", sendMentorChatMessage);
    document.querySelector("#mentor-material-form")?.addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = event.currentTarget;
        const button = form.querySelector("button[type=submit]");
        const formData = new FormData(form);
        button.disabled = true;
        try {
            await portalApi("/api/materials", {
                method: "POST",
                headers: {},
                body: formData
            });
            form.reset();
            portalToast("Đã tải giáo trình lên. Sinh viên có thể tải tài liệu.");
            await loadMentorMaterials();
        } catch (error) {
            portalToast(error.message || "Không thể tải giáo trình lên.");
        } finally {
            button.disabled = false;
        }
    });
    if (document.querySelector("#student-id")?.value) {
        const studentId = document.querySelector("#student-id").value;
        loadStudentData().then(() => recoverActiveExam(studentId)).catch((error) => portalToast(error.message || "Không thể tải dữ liệu sinh viên."));
        loadStudentChat();
    }
    document.querySelector("#student-chat-form")?.addEventListener("submit", sendStudentChatMessage);
    document.querySelector("#mentor-assignment-form")?.addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = event.currentTarget;
        const button = form.querySelector("button[type=submit]");
        const typeSelect = form.querySelector("#assignment-type");
        const assignmentType = typeSelect.value;
        const data = Object.fromEntries(new FormData(form));
        const questions = [...form.querySelectorAll(".quiz-authoring-card")].map((card) => ({
            questionText: card.querySelector("[name=questionText]").value.trim(),
            optionA: card.querySelector("[name=optionA]").value.trim(),
            optionB: card.querySelector("[name=optionB]").value.trim(),
            optionC: card.querySelector("[name=optionC]").value.trim(),
            optionD: card.querySelector("[name=optionD]").value.trim(),
            correctOption: card.querySelector("[name=correctOption]").value,
            points: Number(card.querySelector("[name=points]").value)
        }));
        button.disabled = true;
        try {
            let assignmentId = Number(form.dataset.createdAssignmentId);
            if (!assignmentId) {
                const assignment = await portalApi("/api/assignments", {
                    method: "POST",
                    body: JSON.stringify({
                        ...data,
                        classId: Number(data.classId),
                        type: assignmentType,
                        language: assignmentType === "CODE" ? data.language.trim() : null,
                        durationMinutes: assignmentType === "CODE" || assignmentType === "QUIZ"
                            ? Number(data.durationMinutes)
                            : null
                    })
                });
                assignmentId = assignment.id;
            }
            if (assignmentType === "QUIZ") {
                try {
                    await portalApi(`/api/submissions/quiz/${assignmentId}/questions/batch`, {
                        method: "POST",
                        body: JSON.stringify(questions)
                    });
                } catch (error) {
                    form.dataset.createdAssignmentId = assignmentId;
                    typeSelect.disabled = true;
                    throw new Error(`Đã tạo bài tập #${assignmentId}, nhưng chưa lưu được câu hỏi. Bấm giao lại để thử lưu câu hỏi; ${error.message}`);
                }
            }
            delete form.dataset.createdAssignmentId;
            typeSelect.disabled = false;
            form.reset();
            document.querySelectorAll("[data-mentor-class]").forEach((select) => {
                select.value = data.classId;
            });
            form.querySelector("#quiz-question-list").replaceChildren();
            form.querySelector("#assignment-type").dispatchEvent(new Event("change"));
            portalToast("Đã giao bài tập.");
            await loadMentorData();
        } catch (error) {
            portalToast(error.message || "Không thể tạo bài tập.");
        } finally {
            button.disabled = false;
        }
    });
    submitJsonForm("#schedule-form", "/api/schedules", (data) => ({ ...data, classId: Number(data.classId) }), "Đã tạo lịch trình.");
    document.querySelector("#evaluation-class-filter")?.addEventListener("change", (event) => {
        const trainingClass = evaluationClassRoster.find((item) => String(item.id) === event.currentTarget.value);
        if (trainingClass) renderEvaluationStudents(trainingClass);
    });
    document.querySelector("#evaluation-student-list")?.addEventListener("click", (event) => {
        const button = event.target.closest("[data-evaluation-student]");
        if (!button) return;
        loadEvaluationStudentDetail(button.dataset.evaluationStudent, button.dataset.studentName,
            event.currentTarget.dataset.classId);
    });
    document.querySelector("#evaluation-student-detail")?.addEventListener("click", (event) => {
        if (event.target.closest("#evaluation-back")) {
            evaluationDetailRequestId += 1;
            document.querySelector("#evaluation-student-detail").hidden = true;
            document.querySelector("#evaluation-student-list").hidden = false;
        }
    });
    document.querySelector("#evaluation-student-detail")?.addEventListener("submit", async (event) => {
        const form = event.target.closest(".evaluation-grade-form");
        if (!form) return;
        event.preventDefault();
        const button = form.querySelector("button[type=submit]");
        const detail = document.querySelector("#evaluation-student-detail");
        const studentId = detail.dataset.studentId;
        const classId = detail.dataset.classId;
        const studentName = detail.querySelector(".evaluation-detail-head h2").textContent;
        button.disabled = true;
        try {
            await portalApi(`/api/submissions/${form.dataset.submissionId}/grade`, {
                method: "POST",
                body: JSON.stringify({
                    mentorId: Number(document.querySelector("#mentor-id").value),
                    score: Number(form.elements.score.value),
                    feedback: form.elements.feedback.value.trim()
                })
            });
            portalToast("Đã lưu điểm và nhận xét.");
            await loadEvaluationStudentDetail(studentId, studentName, classId);
        } catch (error) {
            portalToast(error.message || "Không thể lưu điểm bài code.");
            button.disabled = false;
        }
    });
});
