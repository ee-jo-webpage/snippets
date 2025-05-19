console.log("content.js loaded!");

function init() {
    console.log("init start!")
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.action === "toggleSidebar") {

        const existing = document.getElementById("snippet-sidebar-wrapper");
        if (existing) {
            // 슬라이드 아웃 애니메이션
            existing.classList.remove("open");
            setTimeout(() => existing.remove(), 300); // 애니메이션 후 제거
            return;
        }

        const iframe = document.createElement("iframe");
        iframe.id = "snippet-sidebar-wrapper";
        iframe.src = chrome.runtime.getURL("sidebar/sidebar.html");
        iframe.className = "sidebar-iframe";

        document.body.appendChild(iframe);


        setTimeout(() => {
            iframe.classList.add("open");
        }, 10);

        iframe.onload = () => {
            chrome.storage.local.get("highlights", (result) => {
                iframe.contentWindow.postMessage(
                    { type: "INIT_HIGHLIGHTS", highlights: result.highlights || [] },
                    "*"
                );
            });
        };
    }
});

if (document.readyState === "loading") {
    console.log("DOMContentLoaded 대기 중");
    document.addEventListener("DOMContentLoaded", init);
} else {
    console.log("DOMContentLoaded 완료 → init 실행!");
    init();
}
