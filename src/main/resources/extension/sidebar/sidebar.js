const colorMap = {
    0: "#FFFF88",
    1: "#FFFACD",
    2: "#AEC6CF",
    3: "#FFD1DC",
    4: "#C1F0DC",
    5: "#E6E6FA",
    6: "#B0E0E6",
    7: "#FFDAB9",
};

// 사이드바에 하이라이트된 스니펫 목록을 카드 형식으로 렌더링하는 함수
function renderHighlights(highlights) {
    const root = document.getElementById("sidebar-root");
    root.innerHTML = ""; // 기존 목록 초기화

    // 스니펫이 하나도 없는 경우 → 비어 있음 UI 표시
    if (!highlights || highlights.length === 0) {
        const emptyWrapper = document.createElement("div");
        emptyWrapper.className = "empty-state";

        const img = document.createElement("img");
        img.src = chrome.runtime.getURL("images/empty-snippet.jpeg");
        img.alt = "No snippets yet";

        const msg = document.createElement("p");
        msg.textContent = "저장된 스니펫이 없습니다.";

        emptyWrapper.appendChild(img);
        emptyWrapper.appendChild(msg);
        root.appendChild(emptyWrapper);
        return;
    }

    // 각 스니펫 정보를 카드 형태로 렌더링
    highlights.forEach((h) => {
        const card = document.createElement("div");
        card.className = "snippet-card";
        card.dataset.snippetId = h.snippetId;
        card.style.backgroundColor = colorMap[h.colorId] || "#FFFF88";

        // ▶ 스니펫 내용 미리보기 or 썸네일
        const contentDiv = document.createElement("div");
        contentDiv.className = "snippet-content";

        if (h.type === "IMG" && h.imageUrl) {
            // 이미지 스니펫일 경우 썸네일 이미지로 표시
            const img = document.createElement("img");
            img.src = h.imageUrl;
            img.alt = h.altText || "image";
            img.style = `
                        width: 100%;
                        height: 120px;
                        object-fit: cover;
                        border-radius: 6px;
                        `;
            contentDiv.appendChild(img);
        } else {
            // TEXT, CODE 스니펫은 요약 미리보기
            contentDiv.textContent = `“${extractPreview(h)}”`;
        }

        // ▶ 카드 하단 푸터 구성 (출처 + 언어 + 삭제 버튼)
        const footer = document.createElement("div");
        footer.className = "snippet-footer";

        // 왼쪽 그룹: 출처 + 언어
        const leftGroup = document.createElement("div");
        leftGroup.className = "footer-left";

        const domainLink = document.createElement("a");
        domainLink.className = "snippet-domain";
        domainLink.href = h.sourceUrl;
        domainLink.target = "_blank";
        domainLink.rel = "noopener noreferrer";
        domainLink.textContent = getDomain(h.sourceUrl); // 도메인 추출
        leftGroup.appendChild(domainLink);

        // 언어 자동 감지 (CODE 타입일 때만 표시)
        const lang = h.language || detectLanguage(h.content);
        const showLang = h.type === "CODE" && lang !== "Unknown";
        if (showLang) {
            const langSpan = document.createElement("span");
            langSpan.className = "snippet-lang-inline";
            langSpan.textContent = ` | ${lang}`;
            leftGroup.appendChild(langSpan);
        }

        // 오른쪽 그룹: 삭제 버튼
        const rightGroup = document.createElement("div");
        rightGroup.className = "footer-right";

        const deleteBtn = document.createElement("button");
        deleteBtn.className = "delete-btn";
        deleteBtn.textContent = "delete";
        deleteBtn.dataset.snippetId = h.snippetId;
        rightGroup.appendChild(deleteBtn);

        // 왼쪽/오른쪽 푸터 그룹 합치기
        footer.appendChild(leftGroup);
        footer.appendChild(rightGroup);

        // 카드에 내용 및 푸터 삽입
        card.appendChild(contentDiv);
        card.appendChild(footer);

        // 루트에 카드 추가
        root.appendChild(card);
    });

}

// content 요약 정보 추출 함수
function extractPreview(snippet) {
    if (!snippet.content) return "(내용 없음)";
    return snippet.content.length > 100
        ? snippet.content.slice(0, 100) + "..."
        : snippet.content;
}

// 출처 정보 추출 함수
function getDomain(url) {
    try {
        return new URL(url).hostname.replace(/^www\./, "");
    } catch {
        return "(출처 없음)";
    }
}

// 지정된 id 로 로컬, 서버 모두 삭제하는 함수
async function deleteSnippet(snippetId) {
    try {
        // 1. 로컬 highlights 불러오기
        const {highlights = []} = await chrome.storage.local.get("highlights");

        // 2. 삭제 대상 찾기
        const target = highlights.find((h) => h.snippetId === snippetId);

        // 3. 로컬에서 제거 후 저장
        const updated = highlights.filter((h) => h.snippetId !== snippetId);
        await chrome.storage.local.set({highlights: updated});
        console.log("로컬에서 삭제됨:", snippetId);

        // 4. 코드 하이라이트 버튼 재적용
        chrome.tabs.query({active: true, currentWindow: true}, (tabs) => {
            if (tabs[0]?.id) {
                chrome.tabs.sendMessage(tabs[0].id, {
                    action: "refreshCodeButtons",
                });
            }
        });

        //  5. 서버에서 삭제 (background.js에 프록시 요청)
        if (target?.serverId) {
            chrome.runtime.sendMessage(
                {
                    action: "deleteSnippet",
                    snippetId: target.serverId,
                },
                (response) => {
                    if (response?.success) {
                        console.log("🛰️ 서버에서 삭제 완료:", target.serverId);
                    } else {
                        console.warn("❌ 서버 삭제 실패:", response?.error);
                    }
                }
            );
        }

        // 6. content.js에 하이라이트 제거 요청
        chrome.tabs.query({active: true, currentWindow: true}, (tabs) => {
            chrome.tabs.sendMessage(tabs[0].id, {
                action: "removeHighlight",
                snippetId,
            });
        });

        // 7. UI에서 카드 제거 + 다시 렌더링
        const card = document.querySelector(
            `.snippet-card[data-snippet-id="${snippetId}"]`
        );
        if (card) card.remove();
        renderHighlights(updated);

        // 삭제된 이미지 스니펫이면 → 이미지 위 버튼 텍스트를 "save"로 바꿈
        if (target?.type === "IMG") {
            // 이미지와 배경 이미지 모두 재스캔하여 버튼 상태 초기화
            chrome.tabs.query({active: true, currentWindow: true}, (tabs) => {
                if (tabs[0]?.id) {
                    chrome.tabs.sendMessage(tabs[0].id, {
                        action: "refreshImageButtons", // content.js 에서 이 메시지 처리
                    });
                }
            });
        }

    } catch (err) {
        console.error("삭제 처리 중 오류:", err);
    }
}

// 정렬 팝업 토글 버튼 이벤트 핸들러
document.getElementById("sortToggleBtn").addEventListener("click", () => {
    const popup = document.getElementById("sortPopup");

    // 현재 표시 여부에 따라 토글 처리
    popup.style.display = popup.style.display === "none" ? "block" : "none";
});

// 초기 정렬 세팅 함수
(async function initSort() {
    const {sortType} = await chrome.storage.local.get("sortType");

    // 저장된 정렬 타입이 있다면 UI에 반영
    if (sortType) {
        setSelectedSort(sortType);
    }
})();

// 코드 스니펫 언어 패턴 감지 함수
function detectLanguage(content = "") {
    // 각 언어별 고유 문법/패턴을 정의한 정규 표현식 리스트
    const patterns = [
        {lang: "JavaScript", regex: /\b(function|const|let|var|=>)\b/},
        {lang: "TypeScript", regex: /\binterface\b|\bimplements\b/},
        {lang: "Python", regex: /\bdef |import (os|sys|re|numpy|pandas)/},
        {lang: "Java", regex: /\bpublic\s+(class|static)|\bimport\s+java\./},
        {lang: "C", regex: /#include\s*<stdio\.h>/},
        {lang: "C++", regex: /#include\s*<iostream>/},
        {lang: "C#", regex: /\busing\s+System|class\s+\w+\s*{/},
        {lang: "Go", regex: /\bfunc\s+\w+\(|package\s+\w+/},
        {lang: "Rust", regex: /\bfn\s+\w+\s*\(|use\s+std::/},
        {lang: "PHP", regex: /<\?php\b/},
        {lang: "Ruby", regex: /\bdef\s+\w+|puts\s+/},
        {lang: "Kotlin", regex: /\bfun\s+\w+\(|val\s+\w+/},
        {lang: "Swift", regex: /\bfunc\s+\w+\(|import\s+Swift/},
        {lang: "Scala", regex: /\bobject\b|\bdef\b/},
        {lang: "Perl", regex: /\buse\s+strict;|\bmy\s+\$/},
        {lang: "Shell", regex: /#!\/bin\/bash|\becho\b/},
        {
            lang: "HTML",
            regex:
                /<(html|head|body|div|span|a|p|ul|ol|li|h[1-6]|img|form|input|button|section|article|nav|footer|header|main|br|hr|table|thead|tbody|tr|td|th|label|textarea)[\s>]/i,
        },
        {lang: "CSS", regex: /[^{]+\s*{[^}]*}/},
        {lang: "SQL", regex: /\b(SELECT|INSERT|UPDATE|DELETE)\b/i},
        {lang: "JSON", regex: /^\s*{[^]*}\s*$/},
        {lang: "XML", regex: /^\s*<\?xml\b/},
        {lang: "Markdown", regex: /^#{1,6}\s+/m},
        {lang: "YAML", regex: /^[a-zA-Z0-9_-]+:\s+/},
        {lang: "Dockerfile", regex: /^\s*FROM\s+\w+/},
        {lang: "Makefile", regex: /^\s*\w+:\s+/},
        {lang: "Bash", regex: /#!\/bin\/bash/},
        {lang: "PowerShell", regex: /^\s*Get-/},
        {lang: "R", regex: /\bfunction\s*\(|<-|library\(/},
        {lang: "MATLAB", regex: /\bfunction\b.*=\s+\w+/},
        {lang: "Lua", regex: /\blocal\s+\w+\s*=\s*function\b/},
    ];

    // 모든 패턴을 순회하며 일치하는 첫 번째 언어 반환
    for (const {lang, regex} of patterns) {
        if (regex.test(content)) return lang;
    }

    // 어떤 패턴과도 일치하지 않으면 "Unknown" 반환
    return "Unknown";
}

// 정렬 항목 클릭 이벤트 핸들러
document.querySelectorAll("#sortPopup div").forEach((item) => {
    item.addEventListener("click", async (e) => {
        const sortType = e.target.dataset.sort; // "recent" | "oldest" | "color"

        const {highlights = []} = await chrome.storage.local.get("highlights");
        let sorted = [...highlights]; // 원본 복사 후 정렬

        // 정렬 조건별로 처리
        if (sortType === "recent") {
            sorted.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        } else if (sortType === "oldest") {
            sorted.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
        } else if (sortType === "color") {
            sorted.sort((a, b) => (a.colorId || 0) - (b.colorId || 0));
        }

        // 정렬 상태를 로컬에 저장
        await chrome.storage.local.set({highlights: sorted, sortType});

        // UI에 반영
        setSelectedSort(sortType); // 선택된 항목 강조
        renderHighlights(sorted);  // 정렬된 카드 렌더링
        document.getElementById("sortPopup").style.display = "none"; // 팝업 닫기
    });
});

// 선택된 정렬 기준 표시 함수
function setSelectedSort(type) {
    document.querySelectorAll("#sortPopup div").forEach((el) => {
        el.classList.toggle("selected", el.dataset.sort === type);
    });
}

// 사이드바 삭제 버튼 클릭 핸들러
document.addEventListener("click", async (e) => {
    // 클릭된 요소가 .delete-btn 클래스인지 확인
    if (e.target.classList.contains("delete-btn")) {
        const snippetId = e.target.dataset.snippetId;
        if (!snippetId) return;

        // 실제 삭제 함수 호출
        await deleteSnippet(snippetId);
    }
});

// 사이드바 종료 이벤트 핸들러
document.addEventListener("DOMContentLoaded", () => {
    const powerBtn = document.querySelector(".sidebar-power-btn");
    if (powerBtn) {
        powerBtn.addEventListener("click", () => {
            // 부모(content.js)에게 메시지 보냄
            window.parent.postMessage({ type: "CLOSE_SIDEBAR_IFRAME" }, "*");
        });
    }
});

chrome.storage.local.get("highlights", (result) => {
    console.log("highlights 호출!")
    renderHighlights(result.highlights || []);
});

chrome.storage.onChanged.addListener((changes, area) => {
    if (area === "local" && changes.highlights) {
        renderHighlights(changes.highlights.newValue || []);
        console.log("🔁 로컬 스토리지 변경 감지됨");
    }
});