chrome.action.onClicked.addListener(async (tab) => {
    // const loggedIn = await checkLoginStatus();

    // if (loggedIn) {
        // 로그인 O → 사이드바 토글 메시지 전송
        chrome.tabs.sendMessage(tab.id, { action: "toggleSidebar" });
    // } else {
        // 로그인 X → 로그인 페이지로 이동
        // chrome.tabs.create({ url: "http://localhost:8090/login" });
    // }
});

// 로그인 상태 확인 함수 (세션 기반)
// async function checkLoginStatus() {
//     try {
//         const res = await fetch("http://localhost:8090/api/session-user", {
//             method: "GET",
//             credentials: "include",
//         });
//
//         if (res.ok) {
//             const user = await res.json();
//             console.log("로그인됨:", user.username);
//             return true;
//         } else {
//             console.warn("로그인 안됨");
//             return false;
//         }
//     } catch (err) {
//         console.error("로그인 확인 실패:", err);
//         return false;
//     }
// }
