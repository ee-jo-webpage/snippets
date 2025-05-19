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

        // ▶ 스니펫 내용 미리보기 (100자 이내)
        const contentDiv = document.createElement("div");
        contentDiv.className = "snippet-content";
        contentDiv.textContent = `“${extractPreview(h)}”`;

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
        const { highlights = [] } = await chrome.storage.local.get("highlights");

        // 2. 삭제 대상 스니펫 찾기
        const target = highlights.find((h) => h.snippetId === snippetId);

        // 3. 해당 항목 제외한 새 리스트 저장
        const updated = highlights.filter((h) => h.snippetId !== snippetId);
        await chrome.storage.local.set({ highlights: updated });
        console.log("로컬에서 삭제됨:", snippetId);

        // 4. content.js에 코드 버튼 재적용 요청
        chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
            if (tabs[0]?.id) {
                chrome.tabs.sendMessage(tabs[0].id, {
                    action: "refreshCodeButtons",
                });
            }
        });

        // 5. 서버에도 DELETE 요청 (serverId가 있을 경우)
        if (target?.serverId) {
            try {
                const res = await fetch(
                    `http://localhost:8090/api/snippets/${target.serverId}`,
                    { method: "DELETE" }
                );
                if (!res.ok) {
                    const msg = await res.text();
                    console.warn("⚠️ 서버 삭제 실패:", msg);
                } else {
                    console.log("🛰️ 서버에서도 삭제 완료:", target.serverId);
                }
            } catch (err) {
                console.warn("⚠️ 서버 요청 실패 (무시 가능):", err.message);
            }
        }

        // 6. content.js에 하이라이트 제거 요청
        chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
            chrome.tabs.sendMessage(tabs[0].id, {
                action: "removeHighlight",
                snippetId,
            });
        });

        // 7. 사이드바 UI에서 카드 제거
        const card = document.querySelector(
            `.snippet-card[data-snippet-id="${snippetId}"]`
        );
        if (card) card.remove();
        // UI 갱신: 남은 하이라이트 기반으로 다시 렌더링
        renderHighlights(updated);
    } catch (err) {
        console.error("삭제 처리 중 오류:", err);
    }
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