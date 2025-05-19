console.log("content.js 호출!");

let popup = null;
let selectedRange = null;

const colorMap = {
    0: "#FFFF88", // Pastel Yellow
    1: "#FFFACD", // Lemon Chiffon
    2: "#AEC6CF", // Powder Blue
    3: "#FFD1DC", // Cotton Candy
    4: "#C1F0DC", // Mint Cream
    5: "#E6E6FA", // Lavender Mist
    6: "#B0E0E6", // Sky Blue
    7: "#FFDAB9", // Peach Puff
};

function init() {
    console.log("init()함수 호출!")
    document.addEventListener("mouseup", handleMouseUp);
}

function handleMouseUp(e) {
    // 팝업 내부를 클릭한 경우에는 하이라이트 처리를 하지 않고 무시
    // 사용자가 팝업 안에 있는 버튼을 클릭한 경우
    if (popup && popup.contains(e.target)) {
        console.log("popup 내부 클릭!");
        return;
    }

    // 현재 브라우저의 텍스트 선택(selection) 객체를 가져옴
    const selection = window.getSelection();
    console.log("selection 객체:", selection);

    // 선택 영역이 없거나, 커서만 있고 드래그된 내용이 없을 경우
    if (selection.isCollapsed) {
        console.log("selection이 비어 있음 (isCollapsed: true)");

        // 이전에 떠있던 팝업이 있다면 제거
        // removePopup();
        return;
    }

    // 실제 선택된 영역(Range 객체)을 저장해 두고 추후 하이라이팅에 사용
    selectedRange = selection.getRangeAt(0);

    // 선택된 영역의 위치(사각형)를 계산 (화면 좌표 기준)
    const rect = selectedRange.getBoundingClientRect();

    // 해당 위치에 하이라이팅 색상 선택 팝업 표시
    // showPopup(rect);

    // temp
    applyHighlight(1);
}

function getXPathForNode(node) {
    if (node.nodeType === Node.TEXT_NODE) {
        // 텍스트 노드의 경우 부모 요소 기준으로 XPath 생성
        return getXPathForNode(node.parentNode) + "/text()";
    }

    if (node === document.body) {
        return "/html/body";
    }

    let index = 1;
    let sibling = node;

    // 같은 태그명을 가진 이전 형제 노드들을 세어서 인덱스를 계산
    while ((sibling = sibling.previousSibling)) {
        if (sibling.nodeType === Node.ELEMENT_NODE && sibling.nodeName === node.nodeName) {
            index++;
        }
    }

    // 부모 노드의 XPath + 현재 노드 이름 + [index] 형식으로 구성
    return (
        getXPathForNode(node.parentNode) +
        "/" +
        node.nodeName.toLowerCase() +
        "[" + index + "]"
    );
}

// 하이라이트를 적용하는 메인 함수
async function applyHighlight(colorId = 1) {
    console.log("applyHighlight 호출됨");

    // 선택된 Range 객체가 없으면 중단
    if (!selectedRange) {
        console.warn("선택된 Range 없음");
        return;
    }

    // 현재 선택된 Range와 텍스트 정보 추출
    const range = selectedRange;
    const highlightText = range.toString();
    const startNode = range.startContainer;
    const endNode = range.endContainer;
    const startOffset = range.startOffset;
    const endOffset = range.endOffset;

    const fragments = [];     // 하이라이팅된 범위 정보 저장용 (XPath + 오프셋)
    const replacements = [];  // DOM 교체 작업을 위한 정보 저장용

    // TreeWalker를 사용하여 Range 내부에 포함된 텍스트 노드를 모두 순회
    const walker = document.createTreeWalker(
        range.commonAncestorContainer,
        NodeFilter.SHOW_TEXT,
        {
            acceptNode: (node) => {
                const r = document.createRange();
                r.selectNodeContents(node);
                return range.intersectsNode(node)
                    ? NodeFilter.FILTER_ACCEPT
                    : NodeFilter.FILTER_REJECT;
            },
        }
    );

    const nodes = [];
    while (walker.nextNode()) nodes.push(walker.currentNode);

    // 텍스트 노드가 하나도 감지되지 않은 경우, 시작 노드가 텍스트 노드라면 그것만 처리
    if (nodes.length === 0 && startNode.nodeType === Node.TEXT_NODE) {
        nodes.push(startNode);
    }

    // 각 텍스트 노드에 대해 하이라이팅 처리
    for (const node of nodes) {
        if (!node || !node.textContent) continue;

        const text = node.textContent;
        let hStart = 0;
        let hEnd = text.length;

        // 시작/끝 노드라면 오프셋 조정
        if (node === startNode) hStart = startOffset;
        if (node === endNode) hEnd = endOffset;

        // 시작과 끝이 같거나 역전된 경우 무시
        if (hStart >= hEnd) continue;

        const before = text.slice(0, hStart);
        const target = text.slice(hStart, hEnd);
        const after = text.slice(hEnd);

        // 선택된 텍스트가 공백일 경우 무시
        if (!target.trim()) continue;

        // XPath 경로 계산
        const xpath = getXPathForNode(node);
        if (!xpath) continue;

        // 현재 노드의 하이라이트 영역 정보 저장
        fragments.push({ xpath, startOffset: hStart, endOffset: hEnd });

        // 새로운 Fragment 생성: 기존 텍스트 → (before + <snippet>target</snippet> + after)
        const frag = document.createDocumentFragment();
        if (before) frag.appendChild(document.createTextNode(before));

        const mark = document.createElement("snippet");
        mark.setAttribute("data-color", String(colorId));
        mark.textContent = target;
        mark.style.backgroundColor = colorMap[colorId];
        mark.style.borderRadius = "2px";
        mark.style.padding = "0 2px";
        mark.classList.add("highlight-temp");

        frag.appendChild(mark);
        if (after) frag.appendChild(document.createTextNode(after));

        // 나중에 실제 DOM 교체를 위해 저장
        replacements.push({ node, frag });
    }

    // 실제 DOM 교체 작업 수행
    replacements.forEach(({ node, frag }) => {
        node.parentNode.replaceChild(frag, node);
    });

    // 하이라이트할 영역이 하나도 없으면 종료
    if (fragments.length === 0) return;

    // 하이라이트 메타 정보 구성
    const highlight = {
        sourceUrl: location.href,
        colorId,
        fragments,
        content: highlightText,
        createdAt: new Date().toISOString(),
        type: "TEXT",
    };

    // 클라이언트 고유 식별자 생성
    const localUUID = crypto.randomUUID();
    highlight.snippetId = localUUID;

    // 생성된 <snippet> 태그에 UUID 적용 (temp → 실제 하이라이트로 확정)
    document.querySelectorAll("snippet.highlight-temp").forEach((el) => {
        el.setAttribute("data-snippet-id", localUUID);
        el.classList.remove("highlight-temp");
    });

    // 서버로 보낼 요약 데이터
    // const highlightMeta = {
    //     sourceUrl: location.href,
    //     colorId,
    //     content: highlightText,
    // };

    // 서버 저장 요청 시도
    // try {
    //     const serverId = await sendSnippetToServer(highlightMeta);
    //     if (serverId) {
    //         console.log("서버 응답 serverId:", serverId);
    //         highlight.serverId = serverId;
    //     } else {
    //         console.warn("서버 저장 실패 → 서버 ID 없음");
    //     }
    // } catch (err) {
    //     console.error("서버 저장 오류", err);
    // }

    // 로컬 스토리지에 저장
    // saveHighlight(highlight);

    // 팝업 UI 제거 및 선택 영역 해제
    // removePopup();
    window.getSelection().removeAllRanges();
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
