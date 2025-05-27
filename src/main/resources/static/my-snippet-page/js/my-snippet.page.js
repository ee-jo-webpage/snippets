// // ===== 전역 변수 =====
// let currentUser = null;
// let isMobileMenuOpen = false;
//
// // ===== DOM 로드 완료 시 초기화 =====
// document.addEventListener('DOMContentLoaded', function() {
//     initializeApp();
// });
//
// // ===== 앱 초기화 =====
// function initializeApp() {
//     checkLoginStatus();
//     initializeSidebarNavigation();
//     initializeMobileMenu();
//     initializeUserProfile();
//
//     // 페이지별 특별한 초기화가 필요한 경우
//     const currentPage = getCurrentPage();
//     initializePage(currentPage);
// }
//
// // ===== 현재 페이지 감지 =====
// function getCurrentPage() {
//     const path = window.location.pathname;
//     if (path.includes('/snippets')) return 'snippets';
//     if (path.includes('/bookmarks')) return 'bookmarks';
//     if (path.includes('/tags')) return 'tags';
//     if (path.includes('/colors')) return 'colors';
//     if (path.includes('/popular')) return 'popular';
//     if (path.includes('/liked')) return 'liked';
//     if (path.includes('/recent')) return 'recent';
//     if (path.includes('/settings')) return 'settings';
//     return 'home';
// }
//
// // ===== 페이지별 초기화 =====
// function initializePage(page) {
//     switch(page) {
//         case 'snippets':
//             console.log('스니펫 페이지 초기화');
//             break;
//         case 'bookmarks':
//             console.log('북마크 페이지 초기화');
//             break;
//         case 'tags':
//             console.log('태그 페이지 초기화');
//             break;
//         default:
//             console.log('기본 페이지 초기화');
//     }
// }
//
// // ===== 로그인 상태 확인 =====
// async function checkLoginStatus() {
//     try {
//         // 실제 환경에서는 서버 API 호출
//         // const response = await fetch('/api/auth/status');
//         // const userData = await response.json();
//
//         // 임시 데이터 (개발용)
//         const isLoggedIn = false; // 실제로는 서버에서 확인
//
//         if (isLoggedIn) {
//             currentUser = {
//                 id: 1,
//                 name: '홍길동',
//                 email: 'user@example.com',
//                 avatar: '👤'
//             };
//             updateAuthUI(true);
//         } else {
//             updateAuthUI(false);
//         }
//     } catch (error) {
//         console.error('로그인 상태 확인 실패:', error);
//         updateAuthUI(false);
//     }
// }
//
// // ===== 인증 UI 업데이트 =====
// function updateAuthUI(isLoggedIn) {
//     const authButtons = document.querySelector('.auth-buttons');
//     const userProfile = document.getElementById('userProfile');
//
//     if (isLoggedIn && currentUser) {
//         // 로그인된 상태
//         authButtons.innerHTML = `
//             <div style="display: flex; align-items: center; gap: 12px;">
//                 <span style="color: var(--text-secondary); font-size: 14px;">${currentUser.name}님</span>
//                 <button class="btn btn-ghost" onclick="logout()">로그아웃</button>
//             </div>
//         `;
//
//         // 사용자 프로필 표시
//         if (userProfile) {
//             userProfile.style.display = 'flex';
//             userProfile.querySelector('.user-avatar').textContent = currentUser.avatar;
//             userProfile.querySelector('.user-name').textContent = currentUser.name;
//             userProfile.querySelector('.user-email').textContent = currentUser.email;
//         }
//     } else {
//         // 로그아웃 상태
//         authButtons.innerHTML = `
//             <a href="/login" class="btn btn-ghost">로그인</a>
//             <a href="/register" class="btn btn-primary">회원가입</a>
//         `;
//
//         // 사용자 프로필 숨김
//         if (userProfile) {
//             userProfile.style.display = 'none';
//         }
//     }
// }
//
// // ===== 로그아웃 처리 =====
// async function logout() {
//     try {
//         // 실제 환경에서는 서버에 로그아웃 요청
//         // await fetch('/api/auth/logout', { method: 'POST' });
//
//         currentUser = null;
//         updateAuthUI(false);
//
//         // 메인 페이지로 리다이렉트
//         if (window.location.pathname !== '/') {
//             window.location.href = '/';
//         }
//
//         showNotification('로그아웃되었습니다.', 'info');
//     } catch (error) {
//         console.error('로그아웃 실패:', error);
//         showNotification('로그아웃 중 오류가 발생했습니다.', 'error');
//     }
// }
//
// // ===== 사이드바 네비게이션 초기화 =====
// function initializeSidebarNavigation() {
//     const currentPath = window.location.pathname;
//     const sidebarLinks = document.querySelectorAll('.sidebar-link');
//
//     // 현재 페이지에 해당하는 링크 활성화
//     sidebarLinks.forEach(link => {
//         const href = link.getAttribute('href');
//         if (href === currentPath || (currentPath === '/' && href === '/snippets')) {
//             // 기존 active 클래스 제거
//             document.querySelector('.sidebar-link.active')?.classList.remove('active');
//             // 현재 링크에 active 클래스 추가
//             link.classList.add('active');
//         }
//
//         // 링크 클릭 이벤트 (SPA 방식으로 전환 시 사용)
//         link.addEventListener('click', function(e) {
//             // 실제 페이지 이동이 아닌 SPA 방식으로 처리하려면 주석 해제
//             // e.preventDefault();
//             // navigateToPage(href);
//         });
//     });
// }
//
// // ===== 모바일 메뉴 초기화 =====
// function initializeMobileMenu() {
//     // 사이드바 외부 클릭 시 모바일 메뉴 닫기
//     document.addEventListener('click', function(event) {
//         const sidebar = document.getElementById('sidebar');
//         const menuBtn = document.querySelector('.mobile-menu-btn');
//
//         if (window.innerWidth <= 768 &&
//             isMobileMenuOpen &&
//             !sidebar.contains(event.target) &&
//             !menuBtn.contains(event.target)) {
//             closeMobileMenu();
//         }
//     });
//
//     // ESC 키로 모바일 메뉴 닫기
//     document.addEventListener('keydown', function(event) {
//         if (event.key === 'Escape' && isMobileMenuOpen) {
//             closeMobileMenu();
//         }
//     });
//
//     // 화면 크기 변경 시 모바일 메뉴 상태 초기화
//     window.addEventListener('resize', function() {
//         if (window.innerWidth > 768 && isMobileMenuOpen) {
//             closeMobileMenu();
//         }
//     });
// }
//
// // ===== 모바일 메뉴 토글 =====
// function toggleMobileMenu() {
//     const sidebar = document.getElementById('mainSidebar');
//
//     if (isMobileMenuOpen) {
//         closeMobileMenu();
//     } else {
//         openMobileMenu();
//     }
// }
//
// // ===== 모바일 메뉴 열기 =====
// function openMobileMenu() {
//     const sidebar = document.getElementById('mainSidebar');
//     sidebar.classList.add('mobile-open');
//     isMobileMenuOpen = true;
//
//     // 배경 스크롤 방지
//     document.body.style.overflow = 'hidden';
// }
//
// // ===== 모바일 메뉴 닫기 =====
// function closeMobileMenu() {
//     const sidebar = document.getElementById('mainSidebar');
//     sidebar.classList.remove('mobile-open');
//     isMobileMenuOpen = false;
//
//     // 배경 스크롤 복원
//     document.body.style.overflow = '';
// }
//
// // ===== 사용자 프로필 초기화 =====
// function initializeUserProfile() {
//     // 프로필 관련 이벤트 리스너 등록
//     const userProfile = document.getElementById('userProfile');
//     if (userProfile) {
//         userProfile.addEventListener('click', function() {
//             // 사용자 프로필 클릭 시 마이페이지로 이동
//             window.location.href = '/profile';
//         });
//     }
// }
//
// // ===== 알림 표시 =====
// function showNotification(message, type = 'info', duration = 3000) {
//     // 기존 알림 제거
//     const existingNotification = document.querySelector('.notification');
//     if (existingNotification) {
//         existingNotification.remove();
//     }
//
//     // 새 알림 생성
//     const notification = document.createElement('div');
//     notification.className = `notification notification-${type}`;
//     notification.innerHTML = `
//         <div class="notification-content">
//             <span class="notification-icon">${getNotificationIcon(type)}</span>
//             <span class="notification-message">${message}</span>
//             <button class="notification-close" onclick="closeNotification(this)">×</button>
//         </div>
//     `;
//
//     // 알림 스타일
//     notification.style.cssText = `
//         position: fixed;
//         top: 80px;
//         right: 20px;
//         background: white;
//         border-radius: 8px;
//         box-shadow: 0 4px 20px var(--shadow-medium);
//         border-left: 4px solid ${getNotificationColor(type)};
//         z-index: 10000;
//         min-width: 300px;
//         opacity: 0;
//         transform: translateX(100%);
//         transition: all 0.3s ease;
//     `;
//
//     document.body.appendChild(notification);
//
//     // 애니메이션으로 표시
//     setTimeout(() => {
//         notification.style.opacity = '1';
//         notification.style.transform = 'translateX(0)';
//     }, 100);
//
//     // 자동 제거
//     setTimeout(() => {
//         closeNotification(notification);
//     }, duration);
// }
//
// // ===== 알림 아이콘 반환 =====
// function getNotificationIcon(type) {
//     switch(type) {
//         case 'success': return '✅';
//         case 'error': return '❌';
//         case 'warning': return '⚠️';
//         default: return 'ℹ️';
//     }
// }
//
// // ===== 알림 색상 반환 =====
// function getNotificationColor(type) {
//     switch(type) {
//         case 'success': return 'var(--sage-green)';
//         case 'error': return '#e53e3e';
//         case 'warning': return 'var(--warm-beige)';
//         default: return 'var(--forest-green)';
//     }
// }
//
// // ===== 알림 닫기 =====
// function closeNotification(element) {
//     const notification = element.closest ? element.closest('.notification') : element;
//     if (notification) {
//         notification.style.opacity = '0';
//         notification.style.transform = 'translateX(100%)';
//         setTimeout(() => {
//             notification.remove();
//         }, 300);
//     }
// }
//
// // ===== 페이지 네비게이션 (SPA 방식) =====
// function navigateToPage(url) {
//     // SPA 방식으로 페이지 전환 시 사용
//     // 실제 구현에서는 라우터 라이브러리 사용 권장
//
//     // 현재는 일반적인 페이지 이동
//     window.location.href = url;
// }
//
// // ===== 검색 기능 =====
// function initializeSearch() {
//     const searchInput = document.getElementById('searchInput');
//     if (searchInput) {
//         let searchTimeout;
//
//         searchInput.addEventListener('input', function(e) {
//             clearTimeout(searchTimeout);
//             const query = e.target.value.trim();
//
//             // 디바운싱: 300ms 후에 검색 실행
//             searchTimeout = setTimeout(() => {
//                 if (query.length >= 2) {
//                     performSearch(query);
//                 } else {
//                     clearSearchResults();
//                 }
//             }, 300);
//         });
//
//         // 엔터 키로 검색
//         searchInput.addEventListener('keypress', function(e) {
//             if (e.key === 'Enter') {
//                 const query = e.target.value.trim();
//                 if (query.length >= 2) {
//                     performSearch(query);
//                 }
//             }
//         });
//     }
// }
//
// // ===== 검색 실행 =====
// async function performSearch(query) {
//     try {
//         // 실제 환경에서는 서버 API 호출
//         // const response = await fetch(`/api/search?q=${encodeURIComponent(query)}`);
//         // const results = await response.json();
//
//         console.log('검색 실행:', query);
//         // displaySearchResults(results);
//     } catch (error) {
//         console.error('검색 실패:', error);
//         showNotification('검색 중 오류가 발생했습니다.', 'error');
//     }
// }
//
// // ===== 검색 결과 초기화 =====
// function clearSearchResults() {
//     const resultsContainer = document.getElementById('searchResults');
//     if (resultsContainer) {
//         resultsContainer.innerHTML = '';
//         resultsContainer.style.display = 'none';
//     }
// }
//
// // ===== 테마 전환 =====
// function toggleTheme() {
//     const body = document.body;
//     const currentTheme = body.getAttribute('data-theme');
//     const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
//
//     body.setAttribute('data-theme', newTheme);
//     localStorage.setItem('theme', newTheme);
//
//     showNotification(`${newTheme === 'dark' ? '다크' : '라이트'} 테마로 변경되었습니다.`, 'info');
// }
//
// // ===== 테마 초기화 =====
// function initializeTheme() {
//     const savedTheme = localStorage.getItem('theme') || 'light';
//     document.body.setAttribute('data-theme', savedTheme);
// }
//
// // ===== 키보드 단축키 =====
// function initializeKeyboardShortcuts() {
//     document.addEventListener('keydown', function(e) {
//         // Ctrl/Cmd + K: 검색 포커스
//         if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
//             e.preventDefault();
//             const searchInput = document.getElementById('searchInput');
//             if (searchInput) {
//                 searchInput.focus();
//             }
//         }
//
//         // Ctrl/Cmd + N: 새 스니펫
//         if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
//             e.preventDefault();
//             window.location.href = '/snippets/new';
//         }
//
//         // Ctrl/Cmd + B: 북마크 토글 (스니펫 상세 페이지에서)
//         if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
//             const bookmarkBtn = document.getElementById('bookmarkBtn');
//             if (bookmarkBtn) {
//                 e.preventDefault();
//                 bookmarkBtn.click();
//             }
//         }
//     });
// }
//
// // ===== 스크롤 이벤트 처리 =====
// function initializeScrollHandling() {
//     let lastScrollTop = 0;
//     const header = document.querySelector('.header');
//
//     window.addEventListener('scroll', function() {
//         const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
//
//         // 헤더 숨김/표시 (모바일에서)
//         if (window.innerWidth <= 768) {
//             if (scrollTop > lastScrollTop && scrollTop > 100) {
//                 // 아래로 스크롤 - 헤더 숨김
//                 header.style.transform = 'translateY(-100%)';
//             } else {
//                 // 위로 스크롤 - 헤더 표시
//                 header.style.transform = 'translateY(0)';
//             }
//         }
//
//         lastScrollTop = scrollTop;
//     });
// }
//
// // ===== 유틸리티 함수들 =====
//
// // 날짜 포맷팅
// function formatDate(date) {
//     return new Intl.DateTimeFormat('ko-KR', {
//         year: 'numeric',
//         month: 'long',
//         day: 'numeric',
//         hour: '2-digit',
//         minute: '2-digit'
//     }).format(new Date(date));
// }
//
// // 텍스트 truncate
// function truncateText(text, maxLength = 100) {
//     if (text.length <= maxLength) return text;
//     return text.substring(0, maxLength) + '...';
// }
//
// // 디바운스 함수
// function debounce(func, wait) {
//     let timeout;
//     return function executedFunction(...args) {
//         const later = () => {
//             clearTimeout(timeout);
//             func(...args);
//         };
//         clearTimeout(timeout);
//         timeout = setTimeout(later, wait);
//     };
// }
//
// // 로컬 스토리지 유틸리티
// const storage = {
//     set: (key, value) => {
//         try {
//             localStorage.setItem(key, JSON.stringify(value));
//         } catch (e) {
//             console.error('로컬 스토리지 저장 실패:', e);
//         }
//     },
//
//     get: (key, defaultValue = null) => {
//         try {
//             const item = localStorage.getItem(key);
//             return item ? JSON.parse(item) : defaultValue;
//         } catch (e) {
//             console.error('로컬 스토리지 읽기 실패:', e);
//             return defaultValue;
//         }
//     },
//
//     remove: (key) => {
//         try {
//             localStorage.removeItem(key);
//         } catch (e) {
//             console.error('로컬 스토리지 삭제 실패:', e);
//         }
//     }
// };
//
// // ===== 전역 함수 내보내기 (window 객체에 추가) =====
// window.toggleMobileMenu = toggleMobileMenu;
// window.logout = logout;
// window.toggleTheme = toggleTheme;
// window.showNotification = showNotification;
// window.closeNotification = closeNotification;
//
// // ===== 개발 모드 전용 =====
// if (process?.env?.NODE_ENV === 'development') {
//     // 개발 도구 추가
//     window.devTools = {
//         currentUser,
//         showNotification,
//         storage,
//         checkLoginStatus
//     };
//
//     console.log('🚀 개발 모드 - devTools 사용 가능');
// }