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

// 초기화 함수
function init() {
    console.log("init()함수 호출!")
    document.addEventListener("mouseup", handleMouseUp);
    detectCodeBlocks(); // 코드 블록
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
        const language = langClass ? langClass.replace("language-", "") : null;

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
    memoInput.placeholder = "메모 입력...";
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
        // updateCodeSnippetMetadata(content, colorId, memo);
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

// 스니펫 데이터 서버 전송 함수
async function sendSnippetToServer(snippet) {
    // 서버에 보낼 요청 본문 구성
    const payload = {
        colorId: snippet.colorId || 1,               // 색상 ID (기본값 1)
        sourceUrl: snippet.sourceUrl,                // 스니펫 발생한 페이지 URL
        type: snippet.type || "TEXT",                // 스니펫 타입 (TEXT or CODE)
        content: snippet.content || "",              // 스니펫 본문
        language: snippet.language || "",            // 코드 언어 정보 (선택)
    };

    try {
        // POST 요청 전송
        const res = await fetch("http://localhost:8090/api/snippets", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });

        // 응답 상태가 실패일 경우 경고 로그 출력 후 null 반환
        if (!res.ok) {
            const error = await res.text();
            console.warn("❌ 서버 오류:", error);
            return null;
        }

        // 정상 응답인 경우 서버에서 받은 snippetId 반환
        const result = await res.json();
        return result.snippetId;
    } catch (err) {
        // 네트워크 또는 서버 연결 문제
        console.warn("❌ 네트워크 오류:", err);
        console.warn(
            "⚠️ 현재 서버와 연결되어 있지 않습니다. 저장은 로컬에만 반영됩니다."
        );
        return null;
    }
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
