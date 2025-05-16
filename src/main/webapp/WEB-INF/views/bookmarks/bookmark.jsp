<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>스니펫 북마크</title>
    <script src="https://unpkg.com/lucide@latest/dist/umd/lucide.js"></script>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        .code-block {
            background-color: #f3f4f6;
            border-radius: 0.375rem;
            padding: 1rem;
            overflow-x: auto;
        }
        .snippet-card {
            transition: all 0.3s ease;
        }
        .snippet-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
        }
        .bookmark-btn {
            transition: all 0.2s ease;
        }
        .bookmark-btn.active {
            background-color: #fef3c7;
            color: #d97706;
        }
        .bookmark-btn:hover {
            transform: scale(1.1);
        }
    </style>
</head>
<body class="bg-gray-50 min-h-screen">
<div class="container mx-auto px-4 py-8">
    <!-- 헤더 -->
    <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900">스니펫 북마크</h1>
        <div class="flex gap-2">
            <button id="allTab" onclick="showTab('all')"
                    class="tab-btn px-4 py-2 rounded-lg font-medium transition-colors bg-blue-600 text-white">
                전체 스니펫
            </button>
            <button id="bookmarkTab" onclick="showTab('bookmarks')"
                    class="tab-btn px-4 py-2 rounded-lg font-medium transition-colors bg-white text-gray-600 hover:bg-gray-100">
                북마크 (<span id="bookmarkCount">0</span>)
            </button>
        </div>
    </div>

    <!-- 전체 스니펫 -->
    <div id="allSnippets" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <c:forEach var="snippet" items="${snippets}">
            <div class="snippet-card bg-white rounded-lg shadow-md border border-gray-200 p-4"
                 data-snippet-id="${snippet.snippetId}">
                <!-- 카드 헤더 -->
                <div class="flex justify-between items-start mb-3">
                    <div class="flex items-center gap-2">
                        <i data-lucide="${snippet.type == 'code' ? 'code' : 'eye'}" class="w-4 h-4"></i>
                        <span class="text-sm text-gray-600">${snippet.type}</span>
                    </div>
                    <button class="bookmark-btn p-2 rounded-full bg-gray-100 text-gray-400 hover:bg-gray-200"
                            onclick="toggleBookmark(${snippet.snippetId}, this)">
                        <i data-lucide="bookmark" class="w-5 h-5"></i>
                    </button>
                </div>

                <!-- 스니펫 제목 -->
                <h3 class="text-lg font-semibold mb-2">${snippet.memo}</h3>

                <!-- 텍스트 내용 -->
                <c:if test="${not empty snippet.texts && snippet.texts.size() > 0}">
                    <p class="text-gray-600 text-sm mb-3 line-clamp-2">
                            ${snippet.texts[0].content}
                    </p>
                </c:if>

                <!-- 코드 미리보기 -->
                <c:if test="${not empty snippet.codes && snippet.codes.size() > 0}">
                    <div class="code-block mb-3">
                        <div class="flex justify-between items-center mb-2">
                                <span class="px-2 py-1 rounded text-xs bg-blue-100 text-blue-800">
                                        ${snippet.codes[0].language}
                                </span>
                        </div>
                        <pre class="text-sm text-gray-800"><code>${snippet.codes[0].content.length() > 100 ?
                                snippet.codes[0].content.substring(0, 100).concat('...') :
                                snippet.codes[0].content}</code></pre>
                    </div>
                </c:if>

                <!-- 이미지 미리보기 -->
                <c:if test="${not empty snippet.images && snippet.images.size() > 0}">
                    <div class="mb-3">
                        <img src="${snippet.images[0].imageUrl}"
                             alt="${snippet.images[0].altText}"
                             class="w-full h-32 object-cover rounded-md">
                    </div>
                </c:if>

                <!-- 카드 하단 정보 -->
                <div class="flex items-center justify-between text-sm text-gray-500">
                    <div class="flex items-center gap-4">
                        <div class="flex items-center gap-1">
                            <i data-lucide="user" class="w-4 h-4"></i>
                            <span>User ${snippet.userId}</span>
                        </div>
                        <div class="flex items-center gap-1">
                            <i data-lucide="calendar" class="w-4 h-4"></i>
                            <span><fmt:formatDate value="${snippet.createdAt}" pattern="yyyy-MM-dd"/></span>
                        </div>
                        <div class="flex items-center gap-1">
                            <i data-lucide="heart" class="w-4 h-4"></i>
                            <span>${snippet.likeCount}</span>
                        </div>
                    </div>
                    <button onclick="viewDetail(${snippet.snippetId})"
                            class="text-blue-600 hover:text-blue-800 font-medium">
                        자세히 보기
                    </button>
                </div>
            </div>
        </c:forEach>
    </div>

    <!-- 북마크된 스니펫 (처음엔 숨김) -->
    <div id="bookmarkedSnippets" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6" style="display: none;">
        <!-- JavaScript로 동적 생성 -->
    </div>

    <!-- 빈 상태 메시지 -->
    <div id="emptyState" class="text-center py-12" style="display: none;">
        <i data-lucide="bookmark" class="w-16 h-16 text-gray-300 mx-auto mb-4"></i>
        <h3 class="text-lg font-medium text-gray-900 mb-2">북마크한 스니펫이 없습니다</h3>
        <p class="text-gray-500">스니펫을 북마크하면 여기에 표시됩니다.</p>
    </div>
</div>

<!-- 상세보기 모달 -->
<div id="detailModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50" style="display: none;">
    <div class="bg-white rounded-lg max-w-4xl max-h-[90vh] overflow-y-auto">
        <div class="flex justify-between items-center p-6 border-b">
            <h2 id="modalTitle" class="text-xl font-bold"></h2>
            <div class="flex gap-2">
                <button id="modalBookmarkBtn" class="bookmark-btn p-2 rounded-full bg-gray-100 text-gray-400 hover:bg-gray-200">
                    <i data-lucide="bookmark" class="w-5 h-5"></i>
                </button>
                <button onclick="closeModal()" class="p-2 rounded-full bg-gray-100 text-gray-600 hover:bg-gray-200">
                    <span class="text-xl">✕</span>
                </button>
            </div>
        </div>
        <div id="modalContent" class="p-6">
            <!-- 모달 내용은 JavaScript로 동적 생성 -->
        </div>
    </div>
</div>

<script>
    // 전역 변수
    let currentView = 'all';
    let bookmarkedSnippets = new Set();
    let snippetsData = [];
    const currentUserId = ${sessionScope.userId != null ? sessionScope.userId : 1}; // 세션에서 사용자 ID 가져오기

    // 페이지 로드 시 초기화
    document.addEventListener('DOMContentLoaded', function() {
        // Lucide 아이콘 초기화
        lucide.createIcons();

        // 서버에서 스니펫 데이터 가져오기
        snippetsData = [
            <c:forEach var="snippet" items="${snippets}" varStatus="status">
            {
                snippetId: ${snippet.snippetId},
                userId: ${snippet.userId},
                folderId: ${snippet.folderId},
                colorId: ${snippet.colorId},
                sourceUrl: '${snippet.sourceUrl}',
                type: '${snippet.type}',
                memo: '${snippet.memo}',
                createdAt: '${snippet.createdAt}',
                updatedAt: '${snippet.updatedAt}',
                likeCount: ${snippet.likeCount},
                visibility: '${snippet.visibility}',
                codes: [
                    <c:forEach var="code" items="${snippet.codes}">
                    {
                        content: `${code.content}`,
                        language: '${code.language}'
                    },
                    </c:forEach>
                ],
                images: [
                    <c:forEach var="image" items="${snippet.images}">
                    {
                        imageUrl: '${image.imageUrl}',
                        altText: '${image.altText}'
                    },
                    </c:forEach>
                ],
                texts: [
                    <c:forEach var="text" items="${snippet.texts}">
                    {
                        content: '${text.content}'
                    },
                    </c:forEach>
                ]
            }<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];

        // 북마크 상태 로드
        loadBookmarks();
    });

    // 북마크 상태 로드
    function loadBookmarks() {
        fetch(`/api/bookmarks/${currentUserId}`)
            .then(response => response.json())
            .then(bookmarks => {
                bookmarkedSnippets.clear();
                bookmarks.forEach(bookmark => {
                    bookmarkedSnippets.add(bookmark.snippetId);
                });
                updateBookmarkButtons();
                updateBookmarkCount();
            })
            .catch(error => {
                console.error('북마크 로드 실패:', error);
                // 로컬 스토리지에서 읽기 (fallback)
                const saved = localStorage.getItem('bookmarks');
                if (saved) {
                    bookmarkedSnippets = new Set(JSON.parse(saved));
                    updateBookmarkButtons();
                    updateBookmarkCount();
                }
            });
    }

    // 북마크 버튼 상태 업데이트
    function updateBookmarkButtons() {
        document.querySelectorAll('.snippet-card').forEach(card => {
            const snippetId = parseInt(card.dataset.snippetId);
            const bookmarkBtn = card.querySelector('.bookmark-btn');
            const icon = bookmarkBtn.querySelector('i');

            if (bookmarkedSnippets.has(snippetId)) {
                bookmarkBtn.classList.add('active');
                icon.setAttribute('data-lucide', 'bookmark-check');
            } else {
                bookmarkBtn.classList.remove('active');
                icon.setAttribute('data-lucide', 'bookmark');
            }
        });
        lucide.createIcons();
    }

    // 북마크 토글
    function toggleBookmark(snippetId, button) {
        const isBookmarked = bookmarkedSnippets.has(snippetId);

        if (isBookmarked) {
            // 북마크 제거
            fetch(`/api/bookmarks/${currentUserId}/${snippetId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        bookmarkedSnippets.delete(snippetId);
                        updateBookmarkButton(button, false);
                        updateBookmarkCount();
                        if (currentView === 'bookmarks') {
                            showBookmarks();
                        }
                    }
                })
                .catch(error => {
                    console.error('북마크 제거 실패:', error);
                    // 로컬에서 처리 (fallback)
                    bookmarkedSnippets.delete(snippetId);
                    localStorage.setItem('bookmarks', JSON.stringify([...bookmarkedSnippets]));
                    updateBookmarkButton(button, false);
                    updateBookmarkCount();
                });
        } else {
            // 북마크 추가
            fetch('/api/bookmarks', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    userId: currentUserId,
                    snippetId: snippetId
                })
            })
                .then(response => {
                    if (response.ok) {
                        bookmarkedSnippets.add(snippetId);
                        updateBookmarkButton(button, true);
                        updateBookmarkCount();
                    }
                })
                .catch(error => {
                    console.error('북마크 추가 실패:', error);
                    // 로컬에서 처리 (fallback)
                    bookmarkedSnippets.add(snippetId);
                    localStorage.setItem('bookmarks', JSON.stringify([...bookmarkedSnippets]));
                    updateBookmarkButton(button, true);
                    updateBookmarkCount();
                });
        }
    }

    // 개별 북마크 버튼 업데이트
    function updateBookmarkButton(button, isBookmarked) {
        const icon = button.querySelector('i');
        if (isBookmarked) {
            button.classList.add('active');
            icon.setAttribute('data-lucide', 'bookmark-check');
        } else {
            button.classList.remove('active');
            icon.setAttribute('data-lucide', 'bookmark');
        }
        lucide.createIcons();
    }

    // 북마크 개수 업데이트
    function updateBookmarkCount() {
        document.getElementById('bookmarkCount').textContent = bookmarkedSnippets.size;
    }

    // 탭 전환
    function showTab(tab) {
        currentView = tab;

        // 탭 버튼 스타일 변경
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('bg-blue-600', 'text-white');
            btn.classList.add('bg-white', 'text-gray-600', 'hover:bg-gray-100');
        });

        if (tab === 'all') {
            document.getElementById('allTab').classList.remove('bg-white', 'text-gray-600', 'hover:bg-gray-100');
            document.getElementById('allTab').classList.add('bg-blue-600', 'text-white');
            document.getElementById('allSnippets').style.display = 'grid';
            document.getElementById('bookmarkedSnippets').style.display = 'none';
            document.getElementById('emptyState').style.display = 'none';
        } else {
            document.getElementById('bookmarkTab').classList.remove('bg-white', 'text-gray-600', 'hover:bg-gray-100');
            document.getElementById('bookmarkTab').classList.add('bg-blue-600', 'text-white');
            showBookmarks();
        }
    }

    // 북마크 스니펫 표시
    function showBookmarks() {
        const bookmarkedContainer = document.getElementById('bookmarkedSnippets');
        const allContainer = document.getElementById('allSnippets');
        const emptyState = document.getElementById('emptyState');

        allContainer.style.display = 'none';

        if (bookmarkedSnippets.size === 0) {
            bookmarkedContainer.style.display = 'none';
            emptyState.style.display = 'block';
            return;
        }

        emptyState.style.display = 'none';
        bookmarkedContainer.innerHTML = '';

        // 북마크된 스니펫 카드 복사
        document.querySelectorAll('.snippet-card').forEach(card => {
            const snippetId = parseInt(card.dataset.snippetId);
            if (bookmarkedSnippets.has(snippetId)) {
                const clonedCard = card.cloneNode(true);
                bookmarkedContainer.appendChild(clonedCard);
            }
        });

        bookmarkedContainer.style.display = 'grid';
        lucide.createIcons();
    }

    // 상세보기
    function viewDetail(snippetId) {
        const snippet = snippetsData.find(s => s.snippetId === snippetId);
        if (!snippet) return;

        document.getElementById('modalTitle').textContent = snippet.memo;

        // 모달 북마크 버튼 상태 설정
        const modalBookmarkBtn = document.getElementById('modalBookmarkBtn');
        modalBookmarkBtn.onclick = () => toggleBookmark(snippetId, modalBookmarkBtn);

        if (bookmarkedSnippets.has(snippetId)) {
            modalBookmarkBtn.classList.add('active');
            modalBookmarkBtn.querySelector('i').setAttribute('data-lucide', 'bookmark-check');
        } else {
            modalBookmarkBtn.classList.remove('active');
            modalBookmarkBtn.querySelector('i').setAttribute('data-lucide', 'bookmark');
        }

        // 모달 내용 생성
        let modalContent = '';

        // 텍스트 내용
        if (snippet.texts && snippet.texts.length > 0) {
            snippet.texts.forEach(text => {
                modalContent += `<div class="mb-4"><p class="text-gray-700">${text.content}</p></div>`;
            });
        }

        // 코드 내용
        if (snippet.codes && snippet.codes.length > 0) {
            snippet.codes.forEach(code => {
                modalContent += `
                        <div class="mb-6">
                            <div class="bg-gray-50 rounded-lg p-4">
                                <div class="flex justify-between items-center mb-3">
                                    <span class="px-3 py-1 bg-blue-100 text-blue-800 rounded text-sm">${code.language}</span>
                                </div>
                                <pre class="text-sm overflow-x-auto"><code>${code.content}</code></pre>
                            </div>
                        </div>
                    `;
            });
        }

        // 이미지 내용
        if (snippet.images && snippet.images.length > 0) {
            snippet.images.forEach(image => {
                modalContent += `
                        <div class="mb-4">
                            <img src="${image.imageUrl}" alt="${image.altText}" class="max-w-full h-auto rounded-lg">
                            ${image.altText ? `<p class="text-sm text-gray-500 mt-2">${image.altText}</p>` : ''}
                        </div>
                    `;
            });
        }

        // 메타 정보
        modalContent += `
                <div class="mt-6 pt-4 border-t">
                    <div class="grid grid-cols-2 gap-4 text-sm">
                        <div><span class="font-medium">작성자:</span> User ${snippet.userId}</div>
                        <div><span class="font-medium">폴더:</span> Folder ${snippet.folderId}</div>
                        <div><span class="font-medium">생성일:</span> ${snippet.createdAt}</div>
                        <div><span class="font-medium">수정일:</span> ${snippet.updatedAt}</div>
                        ${snippet.sourceUrl ? `<div class="col-span-2"><span class="font-medium">출처:</span> <a href="${snippet.sourceUrl}" target="_blank" class="text-blue-600 hover:underline">${snippet.sourceUrl}</a></div>` : ''}
                    </div>
                </div>
            `;

        document.getElementById('modalContent').innerHTML = modalContent;
        document.getElementById('detailModal').style.display = 'flex';
        lucide.createIcons();
    }

    // 모달 닫기
    function closeModal() {
        document.getElementById('detailModal').style.display = 'none';
    }

    // 모달 외부 클릭 시 닫기
    document.getElementById('detailModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeModal();
        }
    });

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeModal();
        }
    });
</script>
</body>
</html> 'Escape') {
closeModal();
}
});
</script>
</body>
</html>