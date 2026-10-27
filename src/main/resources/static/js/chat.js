let stompClient;
let selectedGroup;
let currentUserId;

const chatApi = async (url, options = {}) => {
    const response = await fetch(url, { headers: { "Content-Type": "application/json" }, ...options });
    const body = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(body.message || `API ${response.status}`);
    return body;
};

const chatEscape = (value = "") => String(value).replace(/[&<>'"]/g, (character) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" }[character]));
const chatToast = (message) => { const toast = document.querySelector("#chat-toast"); toast.textContent = message; toast.classList.add("show"); window.setTimeout(() => toast.classList.remove("show"), 2800); };
const initials = (name = "?") => name.trim().split(/\s+/).slice(-2).map((part) => part[0]).join("").toUpperCase();

function setConnection(connected, message) {
    document.querySelector("#connection-dot").classList.toggle("connected", connected);
    document.querySelector("#connection-label").textContent = message;
}

function connectSocket() {
    stompClient = Stomp.over(new SockJS("/ws-chat"));
    stompClient.debug = () => {};
    stompClient.connect({}, () => { setConnection(true, "Đã kết nối realtime"); subscribeGroup(); }, () => setConnection(false, "Mất kết nối realtime"));
}

function subscribeGroup() {
    if (!stompClient?.connected || !selectedGroup) return;
    stompClient.subscribe(`/topic/groups/${selectedGroup.id}`, (frame) => appendMessage(JSON.parse(frame.body)));
}

function appendMessage(message) {
    const empty = document.querySelector("#chat-empty");
    if (empty) empty.remove();
    const mine = Number(message.senderId) === Number(currentUserId);
    const node = document.createElement("article");
    node.className = `chat-bubble${mine ? " mine" : ""}`;
    node.innerHTML = `<div class="chat-sender">${chatEscape(message.senderName)}</div><div>${chatEscape(message.content)}</div><div class="chat-time">${new Date(message.sentAt).toLocaleString("vi-VN")}</div>`;
    document.querySelector("#messages").appendChild(node);
    document.querySelector("#messages").scrollTop = document.querySelector("#messages").scrollHeight;
}

async function loadMessages() {
    const messages = await chatApi(`/api/chat/groups/${selectedGroup.id}/messages?userId=${currentUserId}`);
    document.querySelector("#messages").innerHTML = messages.length ? "" : "<div id='chat-empty' class='chat-empty'>Chưa có tin nhắn. Hãy mở đầu cuộc trao đổi của nhóm.</div>";
    messages.forEach(appendMessage);
}

function renderGroup(group) {
    const item = document.createElement("button");
    item.className = `group-item${selectedGroup?.id === group.id ? " active" : ""}`;
    item.innerHTML = `<span class="group-dot">${initials(group.name)}</span><span>${chatEscape(group.name)}<small style="display:block;color:#8fb2c8;font-size:10px">${group.members.length} thành viên</small></span>`;
    item.addEventListener("click", () => selectGroup(group));
    document.querySelector("#groups").appendChild(item);
}

async function selectGroup(group) {
    selectedGroup = group;
    document.querySelector("#group-name").textContent = group.name;
    document.querySelector("#group-meta").textContent = `${group.members.length} thành viên · Mentor: ${group.mentor.fullName}`;
    document.querySelector("#member-list").innerHTML = group.members.map((member) => `<li class="member"><span class="member-avatar">${initials(member.fullName)}</span><span>${chatEscape(member.fullName)}<small style="display:block;color:#789096">${member.role}</small></span></li>`).join("");
    document.querySelector("#groups").innerHTML = "";
    await loadGroups();
    try { await loadMessages(); subscribeGroup(); } catch (error) { chatToast(error.message); }
}

async function loadGroups() {
    const role = document.querySelector("#current-role").value;
    const groups = await chatApi(`/api/chat/groups?userId=${currentUserId}&role=${role}`);
    document.querySelector("#groups").innerHTML = "";
    groups.forEach(renderGroup);
    if (!selectedGroup && groups[0]) await selectGroup(groups[0]);
}

function sendChatMessage(payload) {
    if (!payload || !payload.groupId || !payload.senderId || !payload.content) {
        throw new Error("Thiếu dữ liệu tin nhắn.");
    }

    if (stompClient?.connected) {
        stompClient.send("/app/chat.send", {}, JSON.stringify(payload));
        return null;
    }

    return chatApi(`/api/chat/groups/${payload.groupId}/messages`, {
        method: "POST",
        body: JSON.stringify({ senderId: payload.senderId, content: payload.content })
    });
}

async function loadStudents() {
    const students = await chatApi("/api/users?role=STUDENT");
    document.querySelector("#student-to-add").innerHTML = students.map((student) => `<option value="${student.id}">${chatEscape(student.fullName)} (#${student.id})</option>`).join("") || "<option value=''>Chưa có sinh viên</option>";
}

document.addEventListener("DOMContentLoaded", () => {
    currentUserId = Number(document.querySelector("#current-user-id").value);
    document.querySelector("#current-role").addEventListener("change", () => {
        selectedGroup = null; loadGroups().catch((error) => chatToast(error.message));
        document.querySelector("#mentor-tools").style.display = document.querySelector("#current-role").value === "MENTOR" ? "block" : "none";
    });
    document.querySelector("#current-user-id").addEventListener("change", () => { currentUserId = Number(document.querySelector("#current-user-id").value); loadGroups().catch((error) => chatToast(error.message)); });
    document.querySelector("#create-group").addEventListener("click", () => document.querySelector("#group-modal").classList.add("open"));
    document.querySelector("#cancel-group").addEventListener("click", () => document.querySelector("#group-modal").classList.remove("open"));
    document.querySelector("#group-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = Object.fromEntries(new FormData(event.target));
        try { await chatApi("/api/chat/groups", { method: "POST", body: JSON.stringify({ mentorId: currentUserId, classId: Number(data.classId), name: data.name }) }); event.target.reset(); document.querySelector("#group-modal").classList.remove("open"); await loadGroups(); chatToast("Đã tạo nhóm chat."); }
        catch (error) { chatToast(error.message); }
    });
    document.querySelector("#member-form").addEventListener("submit", async (event) => {
        event.preventDefault(); if (!selectedGroup) return chatToast("Hãy chọn nhóm trước.");
        try { selectedGroup = await chatApi(`/api/chat/groups/${selectedGroup.id}/members/${new FormData(event.target).get("studentId")}`, { method: "POST" }); await selectGroup(selectedGroup); chatToast("Đã thêm sinh viên vào nhóm."); }
        catch (error) { chatToast(error.message); }
    });
    document.querySelector("#message-form").addEventListener("submit", async (event) => {
        event.preventDefault(); const input = document.querySelector("#message-input"); const content = input.value.trim();
        if (!selectedGroup || !content) return chatToast("Chọn nhóm và nhập nội dung tin nhắn.");
        if (content.length > 2000) return chatToast("Tin nhắn không được vượt quá 2000 ký tự.");

        try {
            const message = sendChatMessage({ groupId: selectedGroup.id, senderId: currentUserId, content });
            if (message) appendMessage(message);
            input.value = "";
        } catch (error) {
            chatToast(error.message || "Gửi tin nhắn thất bại.");
        }
    });
    document.querySelector("#mentor-tools").style.display = "none";
    loadStudents().catch(() => chatToast("Không thể tải danh sách sinh viên."));
    loadGroups().catch((error) => chatToast(error.message));
    connectSocket();
});