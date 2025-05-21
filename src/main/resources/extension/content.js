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
const colorMapName = {
    0: "Pastel Yellow",
    1: "Lemon Chiffon",
    2: "Powder Blue",
    3: "Cotton Candy",
    4: "Mint Cream",
    5: "Lavender Mist",
    6: "Sky Blue",
    7: "Peach Puff",
};
const CODE_BLOCK_SELECTORS = `
  .se-code-source, .se-section, se-component-content,
  .w3-code,
  pre code,
  code.hljs,
  .code-block code,
  .highlight code,
  .notion-code-block code
`;

// 코드 스니펫 언어 패턴 감지 함수
function detectLanguage(content = "") {
    // 각 언어별 고유 문법/패턴을 정의한 정규 표현식 리스트
    const patterns = [
        { lang: "JavaScript", regex: /\b(function|const|let|var|=>)\b/ },
        { lang: "TypeScript", regex: /\binterface\b|\bimplements\b/ },
        { lang: "Python", regex: /\bdef |import (os|sys|re|numpy|pandas)/ },
        { lang: "Java", regex: /\bpublic\s+(class|static)|\bimport\s+java\./ },
        { lang: "C", regex: /#include\s*<stdio\.h>/ },
        { lang: "C++", regex: /#include\s*<iostream>/ },
        { lang: "C#", regex: /\busing\s+System|class\s+\w+\s*{/ },
        { lang: "Go", regex: /\bfunc\s+\w+\(|package\s+\w+/ },
        { lang: "Rust", regex: /\bfn\s+\w+\s*\(|use\s+std::/ },
        { lang: "PHP", regex: /<\?php\b/ },
        { lang: "Ruby", regex: /\bdef\s+\w+|puts\s+/ },
        { lang: "Kotlin", regex: /\bfun\s+\w+\(|val\s+\w+/ },
        { lang: "Swift", regex: /\bfunc\s+\w+\(|import\s+Swift/ },
        { lang: "Scala", regex: /\bobject\b|\bdef\b/ },
        { lang: "Perl", regex: /\buse\s+strict;|\bmy\s+\$/ },
        { lang: "Shell", regex: /#!\/bin\/bash|\becho\b/ },
        {
            lang: "HTML",
            regex:
                /<(html|head|body|div|span|a|p|ul|ol|li|h[1-6]|img|form|input|button|section|article|nav|footer|header|main|br|hr|table|thead|tbody|tr|td|th|label|textarea)[\s>]/i,
        },
        { lang: "CSS", regex: /[^{]+\s*{[^}]*}/ },
        { lang: "SQL", regex: /\b(SELECT|INSERT|UPDATE|DELETE)\b/i },
        { lang: "JSON", regex: /^\s*{[^]*}\s*$/ },
        { lang: "XML", regex: /^\s*<\?xml\b/ },
        { lang: "Markdown", regex: /^#{1,6}\s+/m },
        { lang: "YAML", regex: /^[a-zA-Z0-9_-]+:\s+/ },
        { lang: "Dockerfile", regex: /^\s*FROM\s+\w+/ },
        { lang: "Makefile", regex: /^\s*\w+:\s+/ },
        { lang: "Bash", regex: /#!\/bin\/bash/ },
        { lang: "PowerShell", regex: /^\s*Get-/ },
        { lang: "R", regex: /\bfunction\s*\(|<-|library\(/ },
        { lang: "MATLAB", regex: /\bfunction\b.*=\s+\w+/ },
        { lang: "Lua", regex: /\blocal\s+\w+\s*=\s*function\b/ },
    ];

    // 모든 패턴을 순회하며 일치하는 첫 번째 언어 반환
    for (const { lang, regex } of patterns) {
        if (regex.test(content)) return lang;
    }

    // 어떤 패턴과도 일치하지 않으면 "Unknown" 반환
    return "unknown";
}

// 초기화 함수
function init() {
    console.log("init()함수 호출!")
    document.addEventListener("mouseup", handleMouseUp);
    document.addEventListener("click", handleSnippetClick);
    restoreHighlights();
    detectCodeBlocks(); // 코드 블록
    detectImageBlocks(); // 이미지
    detectBackgroundImageBlocks(); // 백그라운드 이미지
}

// 드래그 이벤트처리 함수
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
        removePopup();
        return;
    }

    // 실제 선택된 영역(Range 객체)을 저장해 두고 추후 하이라이팅에 사용
    selectedRange = selection.getRangeAt(0);

    // 선택된 영역의 위치(사각형)를 계산 (화면 좌표 기준)
    const rect = selectedRange.getBoundingClientRect();

    // 해당 위치에 하이라이팅 색상 선택 팝업 표시
    showPopup(rect);
}

// 노드의 고유 XPath 경로 생성
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
    const highlightMeta = {
        sourceUrl: location.href,
        colorId,
        content: highlightText,
    };

    // 서버 저장 요청 시도
    try {
        const serverId = await sendSnippetToServer(highlightMeta);
        if (serverId) {
            console.log("서버 응답 serverId:", serverId);
            highlight.serverId = serverId;
        } else {
            console.warn("서버 저장 실패 → 서버 ID 없음");
        }
    } catch (err) {
        console.error("서버 저장 오류", err);
    }

    // 로컬 스토리지에 저장
    saveHighlight(highlight);

    // 팝업 UI 제거 및 선택 영역 해제
    removePopup();
    window.getSelection().removeAllRanges();
}

// 팝업 제거 함수
function removePopup() {
    console.log("removePopup 호출됨");

    if (popup && popup.parentNode) {
        popup.parentNode.removeChild(popup);
        popup = null;
    }
}

// 선택 영역 위에 색상 선택 팝업 띄우기
function showPopup(rect) {
    removePopup(); // 기존 팝업 제거

    popup = document.createElement("div");
    popup.id = "highlight-popup";

    // 기본 스타일 지정
    popup.style.position = "absolute";
    popup.style.background = "white";
    popup.style.padding = "8px";
    popup.style.borderRadius = "8px";
    popup.style.boxShadow = "0 2px 6px rgba(0, 0, 0, 0.15)";
    popup.style.display = "flex";
    popup.style.alignItems = "center";
    popup.style.zIndex = "999999";
    popup.style.gap = "6px";

    // 팝업 위치 계산 (선택 영역 아래)
    const margin = 6;
    const scrollTop = window.scrollY;
    const scrollLeft = window.scrollX;

    const popupTop = rect.bottom + scrollTop + margin;
    let popupLeft = rect.left + scrollLeft;

    // 팝업을 먼저 DOM에 추가해야 offsetWidth를 정확히 계산할 수 있음
    document.body.appendChild(popup);

    const popupWidth = popup.offsetWidth;
    const viewportWidth = document.documentElement.clientWidth;

    // 오른쪽 화면을 넘는 경우 위치 보정 (왼쪽으로 붙이기)
    if (popupLeft + popupWidth > viewportWidth - 10) {
        popupLeft = Math.max(viewportWidth - popupWidth - 10, 10);
    }

    // 왼쪽 화면도 넘는 경우 보정 (최소 여백 유지)
    if (popupLeft < 10) {
        popupLeft = 10;
    }

    popup.style.top = `${popupTop}px`;
    popup.style.left = `${popupLeft}px`;

    // 색상 버튼 그룹 생성 (처음엔 0~3번만 보임)
    const container = document.createElement("div");
    container.style.display = "flex";
    container.style.gap = "6px";

    for (let colorId = 0; colorId <= 3; colorId++) {
        container.appendChild(createColorBtn(colorId));
    }

    // ▶ 토글 버튼 생성 (4~7번 확장용)
    const toggleBtn = document.createElement("div");
    toggleBtn.textContent = "\u25B6"; // ▶
    toggleBtn.style.cssText = `
    width: 24px;
    height: 24px;
    display: flex;
    justify-content: center;
    align-items: center;
    background: #6bcb5a;
    border-radius: 50%;
    font-size: 14px;
    cursor: pointer;
    color: white;
  `;

    let expanded = false;
    toggleBtn.addEventListener("click", () => {
        expanded = !expanded;
        if (expanded) {
            for (let i = 4; i <= 7; i++) {
                container.appendChild(createColorBtn(i));
            }
            toggleBtn.textContent = "\u25C0"; // ◀
        } else {
            container.querySelectorAll(".color-btn").forEach((btn) => {
                const id = parseInt(btn.dataset.colorId, 10);
                if (id >= 4) btn.remove();
            });
            toggleBtn.textContent = "\u25B6"; // ▶
        }

        // 팝업이 다시 넓어졌을 경우에도 우측 끝 넘지 않도록 보정
        const newPopupWidth = popup.offsetWidth;
        if (popupLeft + newPopupWidth > viewportWidth - 10) {
            popup.style.left = `${Math.max(viewportWidth - newPopupWidth - 10, 10)}px`;
        }
    });

    popup.appendChild(container);
    popup.appendChild(toggleBtn);
}

// 색상 선택용 원형 버튼을 생성하는 함수
function createColorBtn(colorId) {
    // 색상 버튼을 위한 div 요소 생성
    const btn = document.createElement("div");

    // 공통 클래스 추가 (스타일 및 토글 시 구분용)
    btn.className = "color-btn";

    // 어떤 색상인지 구분할 수 있도록 colorId를 dataset에 저장
    btn.dataset.colorId = colorId;

    // 툴팁(마우스 오버 시 설명)을 색상 이름으로 설정
    btn.title = colorMapName[colorId];

    // 버튼의 시각적 스타일 지정 (원형, 배경색, 커서 등)
    btn.style.cssText = `
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background-color: ${colorMap[colorId]};
    cursor: pointer;                        
    border: 1px solid #ccc;
    `;

    // 클릭 이벤트 핸들러 등록
    btn.addEventListener("click", (e) => {
        e.stopPropagation();        // 부모 요소로 클릭 이벤트 전파 방지
        applyHighlight(colorId);   // 해당 색상으로 하이라이팅 적용
    });

    // 완성된 버튼 반환
    return btn;
}

// 스토리지 저장 함수
function saveHighlight(data) {
    chrome.storage.local.get(["highlights"], (result) => {
        const highlights = result.highlights || [];
        highlights.push(data);
        chrome.storage.local.set({ highlights }, () => {
            console.log("highlight 저장됨 (chrome.storage):", data);
        });
    });
}

// 주어진 코드 content 가 이미 저장된 CODE 타입 스니펫인지 확인하는 함수
function isSnippetAlreadySaved(content, highlights) {
    return highlights.some(
        (item) => item.type === "CODE" && item.content.trim() === content.trim()
    );
}

// 페이지 내 코드 블록을 감지, UI 추가 함수
async function detectCodeBlocks() {
    // 로컬 스토리지에서 저장된 하이라이트 목록 가져오기
    const highlights =
        (await chrome.storage.local.get("highlights")).highlights || [];

    // 코드 블록 전체 선택 (예: pre > code, div.code 등)
    const blocks = document.querySelectorAll(CODE_BLOCK_SELECTORS);

    blocks.forEach((block) => {
        // 코드 블록을 감싸는 wrapper 요소 확보 (pre 또는 div)
        const wrapper = block.closest("pre, div") || block.parentElement;
        if (!wrapper) return;

        // wrapper에 position 설정 (버튼과 바 위치 지정용)
        wrapper.style.position = "relative";

        // 코드 텍스트 추출 및 저장 여부 확인
        const codeText = block.innerText.trim();
        const alreadySaved = isSnippetAlreadySaved(codeText, highlights);

        // 언어 정보 추출 (예: class="language-js" → js)
        const classList = Array.from(block.classList);
        const langClass = classList.find((cls) => cls.startsWith("language-"));
        const language = langClass ? langClass.replace("language-", "") : detectLanguage(codeText);

        // 기존 저장 버튼/색상 바 제거 (중복 방지)
        const existingBtn = wrapper.querySelector(".snippet-code-btn");
        if (existingBtn) existingBtn.remove();

        const existingBar = wrapper.querySelector(".code-highlight-bar");
        if (existingBar) existingBar.remove();

        // 이미 저장된 경우 색상 바 표시 (좌측 6px 너비)
        if (alreadySaved) {
            const matched = highlights.find(
                (s) => s.type === "CODE" && s.content.trim() === codeText.trim()
            );
            if (matched?.colorId != null) {
                const bar = document.createElement("div");
                bar.className = "code-highlight-bar";
                bar.style.cssText = `
                                      position: absolute;
                                      top: 0;
                                      left: 0;
                                      width: 6px;
                                      height: 100%;
                                      background-color: ${colorMap[matched.colorId]};
                                      border-top-left-radius: 6px;
                                      border-bottom-left-radius: 6px;
                                    `;
                wrapper.appendChild(bar);
            }
        }

        // 저장/수정 버튼 생성
        const saveBtn = document.createElement("button");
        saveBtn.className = "snippet-code-btn";
        saveBtn.textContent = alreadySaved ? "edit" : "save";
        saveBtn.style.cssText = `
                                  position: absolute;
                                  top: 6px;
                                  left: 6px;
                                  background-color: #6bcb5a;
                                  color: white;
                                  border: none;
                                  border-radius: 6px;
                                  padding: 6px 10px;
                                  font-size: 13px;
                                  cursor: pointer;
                                  z-index: 9999;
                                  display: none;
                                `;

        // 마우스 오버 시 버튼 보이기
        wrapper.addEventListener("mouseenter", () => {
            saveBtn.style.display = "block";
        });
        wrapper.addEventListener("mouseleave", () => {
            saveBtn.style.display = "none";
        });

        // 버튼 클릭 시 팝업 띄우기 (저장 or 수정)
        saveBtn.addEventListener("click", (e) => {
            e.stopPropagation();

            // 버튼 위치 기준 팝업 위치 계산
            const rect = saveBtn.getBoundingClientRect();

            if (alreadySaved) {
                const existing = highlights.find(
                    (s) => s.type === "CODE" && s.content.trim() === codeText.trim()
                );
                const currentMemo = existing?.memo || "";
                const currentColorId = existing?.colorId || 1;

                // 수정 팝업 호출
                showCodeEditPopup(
                    codeText,
                    rect,
                    currentColorId,
                    currentMemo,
                    saveBtn,
                    language
                );
            } else {
                // 신규 저장 팝업 호출
                showCodeColorPopup(codeText, rect, false, saveBtn, language);
            }
        });

        // 버튼을 wrapper에 삽입
        wrapper.appendChild(saveBtn);
    });
}

// 코드스니펫 색상 선택 팝업
function showCodeColorPopup(
    codeText,
    rect,
    isEdit = false,
    saveBtn = null,
    language = null
) {
    removePopup(); // 기존 팝업 제거

    // 팝업 요소 생성 및 스타일 지정
    popup = document.createElement("div");
    popup.id = "code-color-popup";
    popup.style = `
    position: absolute;
    top: ${window.scrollY + rect.bottom + 6}px;
    left: ${window.scrollX + rect.left}px;
    background: white;
    padding: 8px;
    border-radius: 10px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
    display: flex;
    align-items: center;
    gap: 6px;
    z-index: 2147483647;
  `;

    // 색상 버튼 담는 컨테이너
    const container = document.createElement("div");
    container.style.display = "flex";
    container.style.gap = "6px";

    // 기본 색상 버튼(0~3)만 생성
    for (let colorId = 0; colorId <= 3; colorId++) {
        container.appendChild(
            createColorBtnForCode(codeText, colorId, isEdit, saveBtn, language)
        );
    }

    // ▶ 토글 버튼 생성 (색상 확장)
    const toggleBtn = document.createElement("div");
    toggleBtn.textContent = "\u25B6"; // ▶
    toggleBtn.style.cssText = `
    width: 24px;
    height: 24px;
    display: flex;
    justify-content: center;
    align-items: center;
    background: #6bcb5a;
    border-radius: 50%;
    font-size: 14px;
    cursor: pointer;
    color: white;
  `;

    let expanded = false;
    toggleBtn.addEventListener("click", () => {
        expanded = !expanded;

        if (expanded) {
            // 확장: 색상 버튼 4~7 추가
            for (let i = 4; i <= 7; i++) {
                container.appendChild(
                    createColorBtnForCode(codeText, i, isEdit, saveBtn, language)
                );
            }
            toggleBtn.textContent = "\u25C0"; // ◀
        } else {
            // 축소: 4~7 제거
            container.querySelectorAll(".color-btn").forEach((btn) => {
                const id = parseInt(btn.dataset.colorId, 10);
                if (id >= 4) btn.remove();
            });
            toggleBtn.textContent = "\u25B6"; // ▶
        }
    });

    popup.appendChild(container);
    popup.appendChild(toggleBtn);
    document.body.appendChild(popup);
}

// 코드 스니펫 편집 팝업
function showCodeEditPopup(
    codeText,
    rect,
    currentColorId,
    currentMemo,
    saveBtn,
    language
) {
    removePopup(); // 기존 팝업 제거

    // 팝업 요소 생성 및 스타일 지정
    popup = document.createElement("div");
    popup.id = "code-edit-popup";
    popup.style = `
    position: absolute;
    top: ${window.scrollY + rect.bottom + 6}px;
    left: ${window.scrollX + rect.left}px;
    background: white;
    padding: 10px;
    border-radius: 10px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;
    gap: 10px;
    z-index: 2147483647;
    min-width: 220px;
    max-width: 300px;
    box-sizing: border-box;
  `;

    // 색상 버튼 행
    const colorRow = document.createElement("div");
    colorRow.style.display = "flex";
    colorRow.style.flexWrap = "wrap";
    colorRow.style.gap = "6px";

    let selectedColorId = currentColorId;

    for (let colorId = 0; colorId <= 7; colorId++) {
        const btn = document.createElement("div");
        btn.className = "color-btn";
        btn.dataset.colorId = colorId;
        btn.title = colorMapName[colorId];
        btn.style.cssText = `
      width: 24px;
      height: 24px;
      border-radius: 50%;
      background-color: ${colorMap[colorId]};
      border: 1px solid #ccc;
      cursor: pointer;
    `;

        if (colorId === currentColorId) {
            // 현재 선택된 색상은 비활성 표시
            btn.classList.add("current-color");
            btn.style.opacity = "0.3";
            btn.style.border = "1px solid #aaa";
            btn.style.cursor = "not-allowed";
        } else {
            // 클릭 시 선택 표시 변경
            btn.addEventListener("click", () => {
                selectedColorId = colorId;
                colorRow.querySelectorAll(".color-btn").forEach((b) => {
                    b.style.outline = "none";
                });
                btn.style.outline = "2px solid green";
            });
        }

        colorRow.appendChild(btn);
    }

    // 메모 입력창
    const memoInput = document.createElement("textarea");
    memoInput.placeholder = "write memo!";
    memoInput.value = currentMemo;
    memoInput.rows = 2;
    memoInput.className = "memo-input";
    memoInput.style.cssText = `
    width: 100%;
    font-size: 13px;
    padding: 8px 10px;
    border: 1px solid #ddd;
    border-radius: 6px;
    resize: none;
    font-family: inherit;
    box-shadow: inset 0 1px 2px rgba(0,0,0,0.05);
    box-sizing: border-box;
  `;

    // 수정 버튼
    const updateBtn = document.createElement("button");
    updateBtn.textContent = "update";
    updateBtn.style.cssText = `
    align-self: flex-end;
    background-color: #6bcb5a;
    color: white;
    border: none;
    border-radius: 6px;
    padding: 6px 10px;
    font-size: 13px;
    cursor: pointer;
  `;

    updateBtn.addEventListener("click", async () => {
        const memo = memoInput.value.trim();
        await saveCodeSnippet(
            codeText,
            selectedColorId,
            true, // isEdit = true
            saveBtn,
            language,
            memo
        );
        removePopup();
    });

    // 팝업 구성 요소 삽입
    popup.appendChild(colorRow);
    popup.appendChild(memoInput);
    popup.appendChild(updateBtn);
    document.body.appendChild(popup);
}

// 코드 블록 하이라이트용 색상 버튼 생성 함수
function createColorBtnForCode(
    codeText,
    colorId,
    isEdit,
    saveBtn,
    language,
    currentColorId = null
) {
    // 색상 버튼(div) 생성
    const btn = document.createElement("div");
    btn.className = "color-btn";
    btn.dataset.colorId = colorId;
    btn.title = colorMapName[colorId]; // 마우스 오버 시 색상 이름 표시

    // 버튼 스타일 지정 (색상 원형)
    btn.style.cssText = `
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background-color: ${colorMap[colorId]};
    border: 1px solid #ccc;
    cursor: pointer;
  `;

    // 이미 선택된 색상이라면 비활성화 처리
    if (colorId === currentColorId) {
        btn.classList.add("current-color");
        btn.style.opacity = "0.3";
        btn.style.border = "1px solid #aaa";
        btn.style.cursor = "not-allowed";
    } else {
        // 클릭 시 스니펫 저장 및 팝업 닫기
        btn.addEventListener("click", async () => {
            await saveCodeSnippet(codeText, colorId, isEdit, saveBtn, language, null);
            removePopup();

            // 버튼 텍스트를 'edit'으로 전환 (신규 저장 후 표시 변경)
            if (saveBtn) saveBtn.textContent = "edit";
        });
    }

    return btn;
}

// 코드 스니펫 저장 및 수정 함수
async function saveCodeSnippet(
    content,
    colorId,
    isEdit = false,
    saveBtn = null,
    language = null,
    memo = null
) {
    // 기존 하이라이트 스니펫 불러오기
    const { highlights = [] } = await chrome.storage.local.get("highlights");

    if (isEdit) {
        // 편집 모드: 기존 스니펫을 찾아 colorId, memo, language 업데이트
        const updated = highlights.map((s) =>
            s.type === "CODE" && s.content.trim() === content.trim()
                ? { ...s, colorId, memo, language }
                : s
        );

        // 로컬 업데이트 반영
        await chrome.storage.local.set({ highlights: updated });

        // 서버에도 metadata 수정 요청 (비동기 PATCH)
        updateCodeSnippetMetadata(content, colorId, memo);
    } else {
        // 신규 저장: UUID 생성 후 스니펫 객체 구성
        const snippetId = crypto.randomUUID();
        const snippet = {
            snippetId,
            type: "CODE",
            content,
            sourceUrl: location.href,
            createdAt: new Date().toISOString(),
            colorId,
            language,
            memo,
        };

        // 기존 목록에 새 항목 추가
        const updated = [...highlights, snippet];
        await chrome.storage.local.set({ highlights: updated });

        try {
            // 서버에 스니펫 전송 (POST)
            const serverId = await sendSnippetToServer(snippet);
            if (serverId) {
                snippet.serverId = serverId;

                // 받은 serverId를 다시 반영하여 로컬 저장소 갱신
                await chrome.storage.local.set({
                    highlights: updated.map((s) =>
                        s.snippetId === snippetId ? { ...s, serverId } : s
                    ),
                });
            }
        } catch (err) {
            console.warn("⚠️ 서버 저장 실패:", err.message);
        }
    }

    // 버튼 텍스트를 edit으로 변경
    if (saveBtn) saveBtn.textContent = "edit";

    // 코드 블록 다시 탐색하여 UI 갱신 (색상 바 등)
    detectCodeBlocks();

    console.log("CODE 스니펫 저장/수정 완료");
}

// 스니펫 데이터를 서버로 전송하는 함수 (TEXT, CODE, IMG 공통)
async function sendSnippetToServer(snippet) {
    // 공통 필드
    const payload = {
        type: snippet.type || "TEXT",        // 스니펫 타입: TEXT, CODE, IMG
        colorId: snippet.colorId || 1,       // 선택한 색상 ID (기본값 1)
        sourceUrl: snippet.sourceUrl,        // 사용자가 스니펫을 저장한 웹페이지의 출처
        memo: snippet.memo || "",            // 메모 (선택)
    };

    // 스니펫 타입별로 필드 추가
    if (snippet.type === "TEXT" || snippet.type === "CODE") {
        payload.content = snippet.content || ""; // 본문 내용
    }

    if (snippet.type === "CODE" && snippet.language) {
        payload.language = snippet.language;     // 코드 언어 정보
    }

    if (snippet.type === "IMG") {
        payload.imageUrl = snippet.imageUrl || ""; // 이미지 자체 주소
        payload.altText = snippet.altText || "";   // alt 텍스트 (선택)
    }

    try {
        // POST 요청으로 서버에 전송
        const res = await fetch("http://localhost:8090/api/snippets", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });

        // 실패 응답인 경우 경고 출력 후 null 반환
        if (!res.ok) {
            const error = await res.text();
            console.warn("❌ 서버 오류:", error);
            return null;
        }

        // 정상 응답: 서버에서 받은 snippetId 반환
        const result = await res.json();
        return result.snippetId;
    } catch (err) {
        // 네트워크 또는 서버 연결 실패
        console.warn("❌ 네트워크 오류:", err);
        console.warn("⚠️ 현재 서버와 연결되어 있지 않습니다. 저장은 로컬에만 반영됩니다.");
        return null;
    }
}

// 코드 스니펫 수정 서버 전송 함수
function updateCodeSnippetMetadata(content, newColorId, memo) {
    // 로컬 스토리지에서 highlights 배열 불러오기
    chrome.storage.local.get(["highlights"], (result) => {
        const highlights = result.highlights || [];

        // 대상 코드(content)와 일치하는 항목만 수정
        const updated = highlights.map((item) => {
            if (item.type === "CODE" && item.content.trim() === content.trim()) {
                // 서버에 저장된 경우 → 서버에도 PATCH 요청
                if ("serverId" in item) {
                    fetch(`http://localhost:8090/api/snippets/${item.serverId}`, {
                        method: "PATCH",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ colorId: newColorId, memo }),
                    })
                        .then((res) => {
                            if (!res.ok) {
                                return res.text().then((msg) => {
                                    console.warn("⚠️ 서버 코드 메모 수정 실패:", msg);
                                });
                            }
                            console.log("🛰️ 서버 코드 메모 수정 완료:", item.serverId);
                        })
                        .catch((err) => {
                            console.warn("⚠️ 서버 요청 실패:", err.message);
                        });
                }

                // 로컬에서도 해당 항목 업데이트
                return { ...item, colorId: newColorId, memo };
            }

            return item;
        });

        // 업데이트된 highlights를 다시 저장
        chrome.storage.local.set({ highlights: updated }, async () => {
            console.log("코드 스니펫 색상/메모 업데이트 완료:", content);

            // 코드 블록 UI 갱신 (색상 바, 버튼 등)
            detectCodeBlocks();
        });
    });
}

// 텍스트 스니펫 수정 서버 전송 함수
function updateSnippetMetadata(snippetId, newColorId, memo) {
    // 현재 문서 내에서 해당 snippetId를 가진 모든 하이라이트 영역 찾기
    const targets = document.querySelectorAll(
        `snippet[data-snippet-id="${snippetId}"]`
    );

    // 각 요소의 색상 속성/스타일 변경
    targets.forEach((el) => {
        el.setAttribute("data-color", newColorId);
        el.style.backgroundColor = colorMap[newColorId];
    });

    // 로컬 하이라이트 목록 불러오기
    chrome.storage.local.get(["highlights"], (result) => {
        const highlights = result.highlights || [];

        // 해당 snippetId에 해당하는 항목 업데이트
        const updated = highlights.map((item) => {
            if (item.snippetId === snippetId) {
                // 서버에 저장된 경우 → 서버에 PATCH 요청
                if ("serverId" in item) {
                    fetch(`http://localhost:8090/api/snippets/${item.serverId}`, {
                        method: "PATCH",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ colorId: newColorId, memo }),
                    })
                        .then((res) => {
                            if (!res.ok) {
                                return res.text().then((msg) => {
                                    console.warn("⚠️ 서버 색상/메모 변경 실패:", msg);
                                });
                            }
                            console.log("🛰️ 서버 색상/메모 변경 완료:", item.serverId);
                        })
                        .catch((err) => {
                            console.warn("⚠️ 서버 요청 실패:", err.message);
                        });
                }

                // 로컬 항목도 업데이트
                return { ...item, colorId: newColorId, memo };
            }

            return item;
        });

        // 로컬 하이라이트 반영 저장
        chrome.storage.local.set({ highlights: updated }, () => {
            console.log("✅ 색상/메모 업데이트 완료:", snippetId);
        });
    });
}

// 스니펫 클릭시 실행되는 핸들러
function handleSnippetClick(e) {
    // 클릭된 요소 또는 상위 요소 중 snippet 태그를 찾음
    const snippet = e.target.closest("snippet");
    if (!snippet) return;

    // 해당 snippet의 ID 및 색상 정보 추출
    const snippetId = snippet.getAttribute("data-snippet-id");
    const colorId = parseInt(snippet.getAttribute("data-color"), 10);

    // 필수 속성 없으면 무시
    if (!snippetId || isNaN(colorId)) return;

    // 색상/메모 변경 팝업 띄우기
    showUpdatePopup(snippet, snippetId, colorId);
}

// 하이라이트 된 스니펫 수정 팝업
function showUpdatePopup(
    snippetEl,
    snippetId,
    currentColorId,
    currentMemo = ""
) {
    removePopup(); // 기존 팝업 제거

    const rect = snippetEl.getBoundingClientRect(); // 위치 정보 계산
    let selectedColorId = currentColorId;

    // 팝업 생성 및 기본 스타일 설정
    popup = document.createElement("div");
    popup.id = "color-picker-popup";
    popup.style = `
    position: absolute;
    top: ${window.scrollY + rect.bottom + 6}px;
    left: ${window.scrollX + rect.left}px;
    background: white;
    padding: 10px;
    border-radius: 10px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;
    gap: 10px;
    z-index: 2147483647;
    min-width: 220px;
    max-width: 300px;
    box-sizing: border-box;
  `;

    // 색상 버튼 행 구성
    const colorRow = document.createElement("div");
    colorRow.style.display = "flex";
    colorRow.style.gap = "6px";

    for (let colorId = 0; colorId <= 7; colorId++) {
        const btn = document.createElement("div");
        btn.className = "color-btn";
        btn.dataset.colorId = colorId;
        btn.title = colorMapName[colorId];
        btn.style.cssText = `
      width: 24px;
      height: 24px;
      border-radius: 50%;
      background-color: ${colorMap[colorId]};
      cursor: pointer;
      border: 1px solid #ccc;
      opacity: 1;
      position: relative;
    `;

        if (colorId === currentColorId) {
            // 현재 선택된 색상은 비활성화 스타일로 표시
            btn.classList.add("current-color");
            btn.style.opacity = "0.3";
            btn.style.border = "1px solid #aaa";
            btn.style.cursor = "not-allowed";
        } else {
            // 클릭 시 선택된 색상 갱신 및 스타일 적용
            btn.addEventListener("click", () => {
                selectedColorId = colorId;

                // 버튼 스타일 초기화
                popup.querySelectorAll(".color-btn").forEach((b) => {
                    const bColorId = parseInt(b.dataset.colorId);
                    if (b.classList.contains("current-color")) {
                        b.style.opacity = "0.3";
                        b.style.border = "1px solid #aaa";
                        b.style.cursor = "not-allowed";
                    } else {
                        b.style.opacity = "1";
                        b.style.border = "1px solid #ccc";
                        b.style.cursor = "pointer";
                    }
                });

                // 현재 선택된 버튼에 강조 스타일 적용
                btn.style.opacity = "0.8";
                btn.style.border = "2px solid green";
                btn.style.cursor = "not-allowed";
            });
        }

        colorRow.appendChild(btn);
    }

    // 메모 입력창 구성
    const memoInput = document.createElement("textarea");
    memoInput.placeholder = "write memo!";
    memoInput.value = currentMemo;
    memoInput.rows = 2;
    memoInput.className = "memo-input";
    memoInput.style.cssText = `
    width: 100%;
    font-size: 13px;
    padding: 8px 10px;
    border: 1px solid #ddd;
    border-radius: 6px;
    resize: none;
    font-family: inherit;
    box-shadow: inset 0 1px 2px rgba(0,0,0,0.05);
    box-sizing: border-box;
  `;

    // 저장 버튼 생성
    const saveBtn = document.createElement("button");
    saveBtn.textContent = "update";
    saveBtn.style.cssText = `
    align-self: flex-end;
    background-color: #6bcb5a;
    color: white;
    border: none;
    border-radius: 6px;
    padding: 6px 10px;
    font-size: 13px;
    cursor: pointer;
  `;

    // 저장 클릭 → 색상/메모 로컬 및 서버 반영
    saveBtn.addEventListener("click", () => {
        const memo = memoInput.value.trim();
        updateSnippetMetadata(snippetId, selectedColorId, memo);
        removePopup();
    });

    // 팝업 최종 구성
    popup.appendChild(colorRow);
    popup.appendChild(memoInput);
    popup.appendChild(saveBtn);
    document.body.appendChild(popup);
}

// 로컬 스토리지에 저장된 highlights 목록을 기반으로 하이라이팅 하는 함수
function restoreHighlights() {
    // 1. 로컬 저장소에서 highlights 배열 불러오기
    chrome.storage.local.get("highlights", (result) => {
        const highlights = result.highlights || [];

        // 2. 하이라이트 항목 하나씩 순회
        highlights.forEach((item) => {
            // TEXT 타입 하이라이트는 fragments 배열을 가짐
            (item.fragments || []).forEach((frag) => {
                try {
                    // 3. XPath로 실제 노드 찾기
                    const node = getNodeByXPath(frag.xpath);

                    // 유효하지 않은 노드라면 스킵
                    if (!node || node.nodeType !== Node.TEXT_NODE) {
                        console.warn("❌ 복원 실패: 노드 없음", frag.xpath);
                        return;
                    }

                    const text = node.textContent;

                    // 4. 시작/끝 오프셋 계산 (최대 길이 초과 방지)
                    const start = Math.min(frag.startOffset, text.length);
                    const end = Math.min(frag.endOffset, text.length);
                    if (start >= end) return;

                    // 5. 원래 텍스트 분리: before + target + after
                    const before = text.slice(0, start);
                    const target = text.slice(start, end);
                    const after = text.slice(end);

                    // 6. 새 fragment 노드 구성 (before + <snippet>target</snippet> + after)
                    const fragNode = document.createDocumentFragment();
                    if (before) fragNode.appendChild(document.createTextNode(before));

                    const mark = document.createElement("snippet");
                    mark.textContent = target;
                    mark.setAttribute("data-color", String(item.colorId));
                    mark.setAttribute("data-snippet-id", String(item.snippetId)); // snippet 식별자 부여
                    mark.style.backgroundColor = colorMap[item.colorId] || "#FFFF88";
                    mark.style.borderRadius = "2px";
                    mark.style.padding = "0 2px";

                    fragNode.appendChild(mark);

                    if (after) fragNode.appendChild(document.createTextNode(after));

                    // 7. 기존 텍스트 노드를 fragment로 교체
                    node.parentNode.replaceChild(fragNode, node);
                } catch (e) {
                    console.error("❌ 복원 중 오류:", e, frag);
                }
            });
        });
    });
}

// xPath 문자열 기반으로 DOM 에서 노드 찾는 함수
function getNodeByXPath(xpath) {
    // XPath를 평가하여 DOM에서 해당 노드를 탐색
    const result = document.evaluate(
        xpath,                         // XPath 문자열
        document,                      // 기준 컨텍스트 (전체 문서)
        null,                          // 네임스페이스 리졸버 (null이면 기본)
        XPathResult.FIRST_ORDERED_NODE_TYPE, // 첫 번째 노드 하나만 반환
        null                           // 기존 결과 객체 재사용 안 함
    );

    // 일치하는 노드를 반환 (없으면 null)
    return result.singleNodeValue;
}

// 사이드바 iframe 에 필요한 스타일 동적 삽입 함수
function injectSidebarStyle() {
    // 이미 스타일이 삽입된 경우 중복 방지
    if (document.getElementById("sidebar-iframe-style")) return;

    // <style> 요소 생성
    const style = document.createElement("style");
    style.id = "sidebar-iframe-style";

    // 슬라이딩 사이드바 스타일 정의
    style.textContent = `
    .sidebar-iframe {
      position: fixed;
      top: 0;
      right: 0;
      width: 450px;
      height: 100vh;
      border: none;
      background-color: white;
      box-shadow: -8px 0 24px rgba(0, 0, 0, 0.15);
      z-index: 999999;
      transform: translateX(100%);
      transition: transform 0.3s ease-in-out;
    }

    .sidebar-iframe.open {
      transform: translateX(0%);
    }
  `;

    // <head>에 style 삽입
    document.head.appendChild(style);
}

// 이미지 요소 위에 저장/수정 버튼을 띄우고 클릭 이벤트를 등록하는 함수
async function detectImageBlocks() {
    // 로컬 스토리지에서 저장된 하이라이트 정보 가져오기
    const { highlights = [] } = await chrome.storage.local.get("highlights");
    const images = document.querySelectorAll("img");

    images.forEach((img) => {
        const imageUrl = img.src;
        const wrapper = img.parentElement;
        if (!wrapper) return;

        // 버튼 위치를 상대적으로 지정하기 위해 부모 요소에 position 설정
        wrapper.style.position = "relative";

        // ✅ 이전에 삽입된 저장/수정 버튼이 있다면 제거 (중복 방지)
        const existingBtn = wrapper.querySelector(".snippet-edit-btn, .snippet-save-btn");
        if (existingBtn) existingBtn.remove();

        // 이미 저장된 이미지인지 확인
        const matched = highlights.find((item) => item.type === "IMG" && item.imageUrl === imageUrl);
        const alreadySaved = Boolean(matched);

        // 저장 또는 수정 버튼 생성
        const saveBtn = createImageSaveBtn(alreadySaved);

        // 버튼 클릭 시 이벤트 처리
        saveBtn.addEventListener("click", (e) => {
            e.stopPropagation(); // 클릭 이벤트 버블링 방지

            if (alreadySaved && matched) {
                // 저장된 경우 → 수정 팝업
                showImageEditPopup(matched, saveBtn);
            } else {
                // 저장되지 않은 경우 → 저장 팝업
                showImageSavePopup(imageUrl, img.alt || "", saveBtn, false);
            }
        });

        // 마우스 호버 시 버튼 표시
        wrapper.addEventListener("mouseenter", () => saveBtn.style.display = "block");
        wrapper.addEventListener("mouseleave", () => saveBtn.style.display = "none");

        // 버튼 삽입
        wrapper.appendChild(saveBtn);
    });
}

// 배경 이미지가 설정된 요소에 저장/수정 버튼을 띄우고 클릭 이벤트를 등록하는 함수
async function detectBackgroundImageBlocks() {
    // 로컬 스토리지에서 저장된 하이라이트 정보 가져오기
    const { highlights = [] } = await chrome.storage.local.get("highlights");

    // 모든 요소를 대상으로 검사
    const elements = document.querySelectorAll("*");

    elements.forEach((el) => {
        // 해당 요소의 배경 이미지 스타일 가져오기
        const bgImage = getComputedStyle(el).backgroundImage;
        if (!bgImage || bgImage === "none") return;

        // url("...") 또는 url('...') 또는 url(...) 형식에서 실제 이미지 URL 추출
        const urlMatch = bgImage.match(/url\(["']?(.*?)["']?\)/);
        if (!urlMatch || !urlMatch[1]) return;

        const imageUrl = urlMatch[1];

        // 버튼 위치를 위한 position 설정
        el.style.position = "relative";

        // ✅ 기존에 추가된 버튼이 있다면 제거 (중복 방지)
        const existingBtn = el.querySelector(".snippet-edit-btn, .snippet-save-btn");
        if (existingBtn) existingBtn.remove();

        // 저장된 이미지인지 확인
        const matched = highlights.find((item) => item.type === "IMG" && item.imageUrl === imageUrl);
        const alreadySaved = Boolean(matched);

        // 저장 또는 수정 버튼 생성
        const saveBtn = createImageSaveBtn(alreadySaved);

        // 버튼 클릭 이벤트 등록
        saveBtn.addEventListener("click", (e) => {
            e.stopPropagation(); // 이벤트 전파 방지

            if (alreadySaved && matched) {
                // 저장된 경우 → 수정 팝업 호출
                showImageEditPopup(matched, saveBtn);
            } else {
                // 저장되지 않은 경우 → 저장 팝업 호출
                showImageSavePopup(imageUrl, "", saveBtn, true); // true는 background 이미지 플래그
            }
        });

        // 마우스 진입 시 버튼 표시
        el.addEventListener("mouseenter", () => saveBtn.style.display = "block");
        el.addEventListener("mouseleave", () => saveBtn.style.display = "none");

        // 버튼 DOM에 추가
        el.appendChild(saveBtn);
    });
}

// 이미지 저장 또는 수정 버튼을 생성하는 함수
function createImageSaveBtn(alreadySaved) {
    // 버튼 요소 생성
    const btn = document.createElement("button");

    // 저장 상태에 따라 클래스와 텍스트 설정
    btn.className = alreadySaved ? "snippet-edit-btn" : "snippet-save-btn";
    btn.textContent = alreadySaved ? "edit" : "save";

    // 버튼 스타일 지정
    btn.style.cssText = `
        position: absolute;
        top: 6px;
        left: 6px;
        background-color: #6bcb5a;
        color: white;
        border: none;
        border-radius: 6px;
        padding: 6px 10px;
        font-size: 13px;
        cursor: pointer;
        z-index: 9999;
        display: none;
    `;

    return btn;
}

// 이미지 저장 팝업을 띄우고 색상 선택, 메모 입력, 저장 기능을 처리하는 함수
function showImageSavePopup(imgUrl, altText, btnElement, isBackground = false) {
    removePopup(); // 기존 팝업 제거

    // 버튼 위치 기준으로 팝업 위치 계산
    const rect = btnElement.getBoundingClientRect();
    popup = document.createElement("div");
    popup.id = "image-popup";
    popup.style = `
        position: absolute;
        top: ${window.scrollY + rect.bottom + 6}px;
        left: ${window.scrollX + rect.left}px;
        background: white;
        padding: 10px;
        border-radius: 10px;
        box-shadow: 0 2px 6px rgba(0,0,0,0.2);
        z-index: 2147483647;
        display: flex;
        flex-direction: column;
        gap: 10px;
        width: 280px;
    `;

    // 미리보기 요소 생성 (일반 이미지 vs 배경 이미지)
    const preview = isBackground ? document.createElement("div") : document.createElement("img");
    if (isBackground) {
        preview.style = `
            width: 100%; height: 160px;
            background-image: url('${imgUrl}');
            background-size: cover; background-position: center;
            border-radius: 6px;
        `;
    } else {
        preview.src = imgUrl;
        preview.alt = altText || "image";
        preview.style = `
            width: 100%; height: 160px;
            object-fit: cover;
            border-radius: 6px;
        `;
    }

    // 메모 입력 textarea 생성
    const memoInput = document.createElement("textarea");
    memoInput.placeholder = "write memo!";
    memoInput.rows = 2;
    memoInput.style = `
        width: 100%; font-size: 13px;
        padding: 8px 10px;
        border: 1px solid #ddd;
        border-radius: 6px;
        resize: none;
        font-family: inherit;
        box-sizing: border-box;
    `;

    // 색상 선택 버튼들 표시
    const colorRow = document.createElement("div");
    colorRow.style = "display: flex; flex-wrap: wrap; gap: 6px;";
    let selectedColorId = 1; // 기본 색상 ID

    for (let i = 0; i <= 7; i++) {
        const btn = document.createElement("div");
        btn.dataset.colorId = i;
        btn.style = `
            width: 24px; height: 24px;
            border-radius: 50%;
            background-color: ${colorMap[i]};
            border: 1px solid #ccc;
            cursor: pointer;
        `;
        // 색상 클릭 시 선택 표시 변경
        btn.addEventListener("click", () => {
            selectedColorId = i;
            popup.querySelectorAll("[data-color-id]").forEach((b) => {
                b.style.outline = "none";
                b.style.boxShadow = "none";
            });
            btn.style.outline = "2px solid green";
        });
        colorRow.appendChild(btn);
    }

    // 실제 저장 버튼 생성 및 클릭 처리
    const saveBtn = document.createElement("button");
    saveBtn.textContent = "save";
    saveBtn.style = `
        align-self: flex-end;
        background-color: #6bcb5a;
        color: white;
        border: none;
        border-radius: 6px;
        padding: 6px 10px;
        font-size: 13px;
        cursor: pointer;
    `;
    saveBtn.addEventListener("click", async () => {
        const memo = memoInput.value.trim();
        await saveImageSnippet(imgUrl, altText, selectedColorId, memo, btnElement);
        removePopup(); // 저장 후 팝업 닫기
    });

    // 팝업에 요소 추가 후 body에 삽입
    popup.append(preview, colorRow, memoInput, saveBtn);
    document.body.appendChild(popup);
}

// 이미지 스니펫을 저장하거나 수정하고 서버 및 로컬 스토리지에 반영하는 함수
async function saveImageSnippet(imgUrl, altText, colorId, memo, btnElement) {
    // 기존 저장된 스니펫 목록 불러오기
    const { highlights = [] } = await chrome.storage.local.get("highlights");

    // 동일 이미지 URL로 저장된 스니펫 있는지 확인
    let snippet = highlights.find((item) => item.type === "IMG" && item.imageUrl === imgUrl);

    let updated = [];

    if (snippet) {
        // 수정 모드: 기존 정보 유지, 색상/메모만 갱신
        const serverId = snippet.serverId;
        const snippetId = snippet.snippetId;

        const newSnippet = {
            ...snippet,
            colorId,
            memo,
        };

        // 수정한 스니펫으로 교체
        updated = highlights.map((item) =>
            item.snippetId === snippetId ? newSnippet : item
        );

        // 서버에도 PATCH 요청 전송
        if (serverId) {
            try {
                await fetch(`http://localhost:8090/api/snippets/${serverId}`, {
                    method: "PATCH",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ colorId, memo }),
                });
                console.log("🛰️ 서버 이미지 메타 수정 완료:", serverId);
            } catch (err) {
                console.warn("⚠️ 서버 이미지 수정 실패", err.message);
            }
        }

    } else {
        // 신규 저장 모드
        snippet = {
            snippetId: crypto.randomUUID(),
            type: "IMG",
            imageUrl: imgUrl,
            sourceUrl: location.href,
            createdAt: new Date().toISOString(),
            colorId,
            memo,
            altText,
        };

        updated = [...highlights, snippet];

        // 서버에 신규 등록
        try {
            const serverId = await sendSnippetToServer(snippet);
            if (serverId) snippet.serverId = serverId;
        } catch (e) {
            console.warn("❌ 서버 저장 실패", e);
        }
    }

    // 로컬 저장소에 갱신
    await chrome.storage.local.set({ highlights: updated });

    // 버튼 텍스트 'edit'으로 변경
    if (btnElement) btnElement.textContent = "edit";
    detectImageBlocks();
    detectBackgroundImageBlocks();
    console.log("이미지 스니펫 저장/수정 완료", snippet);
}

// 저장된 이미지 스니펫에 대한 수정 팝업을 띄우고 색상/메모를 수정하는 함수
function showImageEditPopup(snippet, btnElement) {
    removePopup(); // 기존 팝업 제거

    const rect = btnElement.getBoundingClientRect();
    let selectedColorId = snippet.colorId || 1;
    const currentMemo = snippet.memo || "";

    popup = document.createElement("div");
    popup.id = "image-edit-popup";
    popup.style = `
        position: absolute;
        top: ${window.scrollY + rect.bottom + 6}px;
        left: ${window.scrollX + rect.left}px;
        background: white;
        padding: 10px;
        border-radius: 10px;
        box-shadow: 0 2px 6px rgba(0,0,0,0.2);
        z-index: 2147483647;
        display: flex;
        flex-direction: column;
        gap: 10px;
        width: 280px;
    `;

    // 이미지 미리보기 구성
    const preview = document.createElement("img");
    preview.src = snippet.imageUrl;
    preview.alt = snippet.altText || "image";
    preview.style = `
        width: 100%; height: 160px;
        object-fit: cover;
        border-radius: 6px;
    `;

    // 메모 입력창 구성
    const memoInput = document.createElement("textarea");
    memoInput.placeholder = "write memo!";
    memoInput.rows = 2;
    memoInput.value = currentMemo;
    memoInput.style = `
        width: 100%;
        font-size: 13px;
        padding: 8px 10px;
        border: 1px solid #ddd;
        border-radius: 6px;
        resize: none;
        font-family: inherit;
        box-sizing: border-box;
    `;

    // 색상 선택 영역 구성
    const colorRow = document.createElement("div");
    colorRow.style = "display: flex; flex-wrap: wrap; gap: 6px;";

    for (let i = 0; i <= 7; i++) {
        const btn = document.createElement("div");
        btn.dataset.colorId = i;
        btn.style = `
            width: 24px; height: 24px;
            border-radius: 50%;
            background-color: ${colorMap[i]};
            border: 1px solid #ccc;
            cursor: pointer;
        `;

        if (i === (snippet.colorId ?? -1)) {
            // 현재 저장된 색상은 비활성화
            btn.style.opacity = "0.3";
            btn.style.border = "1px solid #aaa";
            btn.style.cursor = "not-allowed";
            btn.classList.add("current-color");
        } else {
            // 선택된 색상 강조
            if (i === selectedColorId) {
                btn.style.border = "2px solid green";
                btn.style.opacity = "0.8";
                btn.style.cursor = "not-allowed";
            }

            // 클릭 시 색상 선택 변경
            btn.addEventListener("click", () => {
                selectedColorId = i;

                // 기존 스타일 초기화
                popup.querySelectorAll("[data-color-id]").forEach((b) => {
                    const bId = parseInt(b.dataset.colorId, 10);
                    if (bId === snippet.colorId) {
                        b.style.opacity = "0.3";
                        b.style.border = "1px solid #aaa";
                        b.style.cursor = "not-allowed";
                    } else {
                        b.style.opacity = "1";
                        b.style.border = "1px solid #ccc";
                        b.style.cursor = "pointer";
                    }
                });

                // 새 선택 스타일 반영
                btn.style.border = "2px solid green";
                btn.style.opacity = "0.8";
                btn.style.cursor = "not-allowed";
            });
        }

        colorRow.appendChild(btn);
    }

    // 업데이트 버튼 구성
    const updateBtn = document.createElement("button");
    updateBtn.textContent = "update";
    updateBtn.style = `
        align-self: flex-end;
        background-color: #6bcb5a;
        color: white;
        border: none;
        border-radius: 6px;
        padding: 6px 10px;
        font-size: 13px;
        cursor: pointer;
    `;

    // 클릭 시 서버/스토리지에 반영하고 팝업 종료 및 UI 재검사
    updateBtn.addEventListener("click", async () => {
        const memo = memoInput.value.trim();
        await saveImageSnippet(snippet.imageUrl, snippet.altText, selectedColorId, memo, btnElement);
        removePopup();
        detectImageBlocks();
        detectBackgroundImageBlocks();
    });

    popup.append(preview, colorRow, memoInput, updateBtn);
    document.body.appendChild(popup);
}

// 확장 프로그램과 content.js 간 메시지 통신 핸들러
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    // === 사이드바 열기/닫기 ===
    if (message.action === "toggleSidebar") {
        injectSidebarStyle(); // 슬라이딩 스타일 삽입

        const existing = document.getElementById("snippet-sidebar-wrapper");
        if (existing) {
            // 슬라이드 아웃 (닫기)
            existing.classList.remove("open");
            setTimeout(() => existing.remove(), 300); // 애니메이션 완료 후 제거
            return;
        }

        // iframe 사이드바 생성
        const iframe = document.createElement("iframe");
        iframe.id = "snippet-sidebar-wrapper";
        iframe.src = chrome.runtime.getURL("sidebar/sidebar.html");
        iframe.className = "sidebar-iframe";

        document.body.appendChild(iframe);

        // 슬라이드 인 (열기)
        setTimeout(() => {
            iframe.classList.add("open");
        }, 10); // 다음 tick에서 실행

        // iframe 로딩 완료 후 스니펫 전달
        iframe.onload = () => {
            chrome.storage.local.get("highlights", (result) => {
                iframe.contentWindow.postMessage(
                    {
                        type: "INIT_HIGHLIGHTS",
                        highlights: result.highlights || [],
                    },
                    "*"
                );
            });
        };
    }

    // === 하이라이팅 제거 ===
    if (message.action === "removeHighlight" && message.snippetId) {
        const id = String(message.snippetId);

        // snippetId로 표시된 모든 요소 제거하고 원래 텍스트 복구
        const targets = document.querySelectorAll(
            `snippet[data-snippet-id="${id}"]`
        );
        targets.forEach((el) => {
            const textNode = document.createTextNode(el.textContent);
            el.replaceWith(textNode);
        });

        console.log(`하이라이팅 제거 완료: snippetId = ${id}`);
    }

    // === 코드 블록 버튼 UI 재적용 ===
    if (message.action === "refreshCodeButtons") {
        detectCodeBlocks(); // CODE 타입 버튼 및 표시 재렌더링
    }
});

if (document.readyState === "loading") {
    console.log("DOMContentLoaded 대기 중");
    document.addEventListener("DOMContentLoaded", init);
} else {
    console.log("DOMContentLoaded 완료 → init 실행!");
    init();
}
