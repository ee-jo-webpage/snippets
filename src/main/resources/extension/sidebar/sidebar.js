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

        // 푸터 그룹 합치기
        footer.appendChild(leftGroup);

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