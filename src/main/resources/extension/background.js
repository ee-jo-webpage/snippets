chrome.action.onClicked.addListener(async (tab) => {
    const loggedIn = await checkLoginStatus();

    if (loggedIn) {
        // 로그인 O → 사이드바 토글 메시지 전송
        chrome.tabs.sendMessage(tab.id, { action: "toggleSidebar" });
    } else {
        // 로그인 X → 로그인 페이지로 이동
        chrome.tabs.create({ url: "http://localhost:8090/login" });
    }
});

// 로그인 상태 확인 함수 (세션 기반)
async function checkLoginStatus() {
    try {
        const res = await fetch("http://localhost:8090/api/session-user", {
            method: "GET",
            credentials: "include",
        });

        if (res.ok) {
            const user = await res.json();
            console.log("로그인됨:", user.username);
            return true;
        } else {
            console.warn("로그인 안됨");
            return false;
        }
    } catch (err) {
        console.error("로그인 확인 실패:", err);
        return false;
    }
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    // 1. 스니펫 저장 (POST /api/snippets)
    if (message.action === "sendSnippetToServer") {
        fetch("http://localhost:8090/api/snippets", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(message.payload),
            credentials: "include",
        })
            .then((res) => {
                if (!res.ok) throw new Error("Server responded with error");
                return res.json();
            })
            .then((data) => {
                console.log("스니펫 저장 완료:", data);
                sendResponse({ success: true, snippetId: data.snippetId });
            })
            .catch((err) => {
                console.warn("스니펫 저장 실패:", err.message);
                sendResponse({ success: false, error: err.message });
            });
        return true; // 비동기 응답을 위해 반드시 필요
    }

    // 2. 스니펫 수정 (PATCH /api/snippets/{id})
    if (message.action === "updateSnippet") {
        fetch(`http://localhost:8090/api/snippets/${message.snippetId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(message.payload),
            credentials: "include",
        })
            .then((res) => {
                if (!res.ok) throw new Error("Patch request failed");
                return res.text(); // 성공만 확인
            })
            .then(() => {
                console.log("스니펫 수정 완료:", message.snippetId);
                sendResponse({ success: true });
            })
            .catch((err) => {
                console.warn("스니펫 수정 실패:", err.message);
                sendResponse({ success: false, error: err.message });
            });
        return true;
    }

    // 3. 스니펫 삭제 (DELETE /api/snippets/{id})
    if (message.action === "deleteSnippet") {
        fetch(`http://localhost:8090/api/snippets/${message.snippetId}`, {
            method: "DELETE",
            credentials: "include", //
        })
            .then((res) => {
                if (!res.ok) throw new Error("Delete failed");
                sendResponse({ success: true });
            })
            .catch((err) => {
                sendResponse({ success: false, error: err.message });
            });

        return true; // async 응답 위해 필요
    }
});
