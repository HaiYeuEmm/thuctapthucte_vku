const api = async (url, options = {}) => {
    const response = await fetch(url, { headers: { "Content-Type": "application/json" }, ...options });
    if (!response.ok) throw new Error(`API ${response.status}`);
    return response.status === 204 ? null : response.json();
};

const showToast = (message) => {
    const toast = document.querySelector("#toast");
    toast.textContent = message;
    toast.style.display = "block";
    window.setTimeout(() => { toast.style.display = "none"; }, 2600);
};

const escapeHtml = (value = "") => String(value).replace(/[&<>'"]/g, (character) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;"
}[character]));

const formatDate = (value) => value ? new Date(value).toLocaleString("vi-VN") : "-";

async function loadDashboard() {
    try {
        const [classes, mentors, students] = await Promise.all([
            api("/api/classes"), api("/api/users?role=MENTOR"), api("/api/users?role=STUDENT")
        ]);
        document.querySelector("#class-count").textContent = classes.length;
        document.querySelector("#mentor-count").textContent = mentors.length;
        document.querySelector("#student-count").textContent = students.length;
        const classRows = classes.map((item) => `
            <tr><td>${escapeHtml(item.name)}</td><td>${escapeHtml(item.term)}</td>
            <td>${escapeHtml(item.mentor?.fullName || "Chưa phân công")}</td>
            <td>${item.students?.length || 0}</td></tr>`).join("") || emptyRow(4);
        document.querySelector("#overview-class-table").innerHTML = classRows;
        document.querySelector("#class-table").innerHTML = classRows;
        document.querySelector("#mentor-table").innerHTML = mentors.map((item) =>
            `<tr><td>${item.id}</td><td>${escapeHtml(item.fullName)}</td><td>${escapeHtml(item.email)}</td></tr>`
        ).join("") || emptyRow(3);
        document.querySelector("#student-table").innerHTML = students.map((item) =>
            `<tr><td>${item.id}</td><td>${escapeHtml(item.fullName)}</td><td>${escapeHtml(item.email)}</td></tr>`
        ).join("") || emptyRow(3);
        await loadTrainingOperations(classes);
    } catch (error) { showToast("Chưa thể tải dữ liệu. Hãy kiểm tra máy chủ."); }
}

async function loadTrainingOperations(classes) {
    const status = document.querySelector("#operations-status");
    if (!status) return;
    if (!classes.length) {
        ["schedule", "assignment", "attendance", "evaluation"].forEach((name) => {
            document.querySelector(`#operations-${name}-count`).textContent = "0";
        });
        status.textContent = "Chưa có lớp";
        document.querySelector("#operations-description").textContent = "Tạo lớp thực tập để bắt đầu theo dõi vận hành đào tạo.";
        return;
    }
    try {
        const classData = await Promise.all(classes.map(async (trainingClass) => {
            const classId = trainingClass.id;
            const [schedules, assignments, attendance, evaluations] = await Promise.all([
                api(`/api/schedules/class/${classId}`),
                api(`/api/assignments/class/${classId}`),
                api(`/api/attendance/class/${classId}`),
                api(`/api/evaluations/class/${classId}`)
            ]);
            return { schedules, assignments, attendance, evaluations };
        }));
        const totals = classData.reduce((counts, item) => ({
            schedules: counts.schedules + item.schedules.length,
            assignments: counts.assignments + item.assignments.length,
            attendance: counts.attendance + item.attendance.length,
            evaluations: counts.evaluations + item.evaluations.length
        }), { schedules: 0, assignments: 0, attendance: 0, evaluations: 0 });
        document.querySelector("#operations-schedule-count").textContent = totals.schedules;
        document.querySelector("#operations-assignment-count").textContent = totals.assignments;
        document.querySelector("#operations-attendance-count").textContent = totals.attendance;
        document.querySelector("#operations-evaluation-count").textContent = totals.evaluations;
        status.textContent = "Đã cập nhật";
        document.querySelector("#operations-description").textContent =
            `Tổng hợp từ ${classes.length} lớp thực tập.`;
    } catch (error) {
        status.textContent = "Không tải được dữ liệu";
        document.querySelector("#operations-description").textContent =
            error.message || "Không thể tải dữ liệu vận hành đào tạo.";
    }
}

const emptyRow = (columns) => `<tr><td colspan="${columns}" class="subtle">Chưa có dữ liệu</td></tr>`;

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-view]").forEach((button) => button.addEventListener("click", () => {
        document.querySelectorAll("[data-view]").forEach((item) => item.classList.remove("active"));
        document.querySelectorAll(".view").forEach((item) => item.classList.remove("active"));
        button.classList.add("active");
        document.querySelector(`#${button.dataset.view}`).classList.add("active");
        document.querySelector("#page-title").textContent = button.dataset.title;
    }));
    document.querySelector("#class-form")?.addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = new FormData(event.target);
        try {
            await api("/api/classes", { method: "POST", body: JSON.stringify(Object.fromEntries(form)) });
            event.target.reset(); showToast("Đã tạo lớp thực tập."); await loadDashboard();
        } catch (error) { showToast("Không thể tạo lớp."); }
    });
    loadDashboard();
});