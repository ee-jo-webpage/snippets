let userId = null;

chrome.action.onClicked.addListener(async (tab) => {
    const isLoggedIn = await checkLoginStatus();

    if (isLoggedIn) {
        chrome.tabs.sendMessage(tab.id, {action: "toggleSidebar"});
    } else {
        chrome.tabs.create({url: "http://localhost:8090/login"});
    }
});


// 로그인 상태 확인 함수 (세션 기반)
async function checkLoginStatus() {
    try {
        const res = await fetch("http://localhost:8090/api/session-user", {
            method: "GET",
            credentials: "include", // 세션 쿠키 포함
        });

        return res.ok; // 200이면 로그인, 401이면 비로그인
    } catch (err) {
        console.error("로그인 확인 실패:", err);
        return false;
    }
}


chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    // 1. 스니펫 저장 (POST /api/snippets)
    if (message.action === "sendSnippetToServer") {
        // 디바운스된 저장 함수 호출 (빠른 중복 요청 방지)
        debouncedSendSnippet(message, (res) => {
            if (chrome.runtime.lastError) {
                console.warn("응답 실패:", chrome.runtime.lastError.message);
            } else {
                sendResponse(res);
            }
        });
        return true;
    }

    // 2. 스니펫 수정 (PATCH /api/snippets/{id})
    if (message.action === "updateSnippet") {
        fetch(`http://localhost:8090/api/snippets/${message.snippetId}`, {
            method: "PATCH",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(message.payload),
            credentials: "include",
        })
            .then((res) => {
                if (!res.ok) throw new Error("Patch request failed");
                return res.text(); // 성공만 확인
            })
            .then(() => {
                console.log("스니펫 수정 완료:", message.snippetId);
                sendResponse({success: true});
            })
            .catch((err) => {
                console.warn("스니펫 수정 실패:", err.message);
                sendResponse({success: false, error: err.message});
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
                sendResponse({success: true});
            })
            .catch((err) => {
                sendResponse({success: false, error: err.message});
            });

        return true; // async 응답 위해 필요
    }
});

// debounce 함수 정의 (lodash 없이 사용)
function debounce(func, delay) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), delay);
    };
}

// 디바운스 적용된 스니펫 저장 함수 (마지막 요청만 처리됨)
const debouncedSendSnippet = debounce((message, sendResponse) => {
    fetch("http://localhost:8090/api/snippets", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(message.payload),
        credentials: "include",
    })
        .then((res) => {
            if (!res.ok) throw new Error("Server responded with error");
            return res.json();
        })
        .then((data) => {
            console.log("🟡 디바운스 저장 완료:", data);
            sendResponse({success: true, snippetId: data.snippetId});
        })
        .catch((err) => {
            console.warn("스니펫 저장 실패:", err.message);
            sendResponse({success: false, error: err.message});
        });
}, 500);

