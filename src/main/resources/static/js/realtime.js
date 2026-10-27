let classRealtimeClient;
const classRealtimeSubscriptions = new Map();
const watchedClassIds = new Set();

function watchClass(classId) {
    if (!classId || typeof Stomp === "undefined") return;
    const normalizedClassId = String(classId);
    watchedClassIds.add(normalizedClassId);
    if (classRealtimeSubscriptions.has(normalizedClassId)) return;
    if (!classRealtimeClient) {
        classRealtimeClient = Stomp.over(new SockJS("/ws-chat"));
        classRealtimeClient.debug = () => {};
        classRealtimeClient.connect({}, () => watchedClassIds.forEach(subscribeClass));
    } else if (classRealtimeClient.connected) subscribeClass(normalizedClassId);
}

function subscribeClass(classId) {
    if (classRealtimeSubscriptions.has(classId)) return;
    const subscription = classRealtimeClient.subscribe(`/topic/classes/${classId}`, (frame) => {
        const update = JSON.parse(frame.body);
        window.dispatchEvent(new CustomEvent("class-data-updated", { detail: update }));
        const selectedExamClass = document.querySelector("#exam-class-filter")?.value;
        if (typeof loadExamSessions === "function"
                && String(update.type || "").startsWith("EXAM_SECURITY")
                && String(update.classId) === String(selectedExamClass)) {
            loadExamSessions();
        }
        if (typeof loadMentorData === "function") loadMentorData().catch(() => {});
        if (typeof loadStudentData === "function" && document.querySelector("#student-id")?.value) loadStudentData().catch(() => {});
    });
    classRealtimeSubscriptions.set(classId, subscription);
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("select[data-classes]").forEach((select) => select.addEventListener("change", () => watchClass(select.value)));
    const examClassSelect = document.querySelector("#exam-class-filter");
    examClassSelect?.addEventListener("change", () => watchClass(examClassSelect.value));
    const initial = document.querySelector("select[data-classes]")?.value;
    if (initial) watchClass(initial);
    else if (examClassSelect?.value) watchClass(examClassSelect.value);
});
