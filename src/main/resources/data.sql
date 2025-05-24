-- 더미데이터 삽입

-- 사용자 데이터
INSERT INTO `users` (`email`, `nickname`, `password`, `enabled`, `created_at`, `role`) VALUES
('admin@example.com', '관리자', '$2a$10$65ixGN84pEFJXzbnk1x2N.y8.clEfZ/h1LyMzAE3vZ6wBIfwNEi36', 1, '2024-01-15 10:00:00', 'ADMIN'),
('john@example.com', 'John Doe', '$2a$10$65ixGN84pEFJXzbnk1x2N.y8.clEfZ/h1LyMzAE3vZ6wBIfwNEi36', 1, '2024-02-01 14:30:00', 'USER'),
('jane@example.com', 'Jane Smith', '$2a$10$65ixGN84pEFJXzbnk1x2N.y8.clEfZ/h1LyMzAE3vZ6wBIfwNEi36', 1, '2024-02-10 09:15:00', 'USER'),
('mike@example.com', 'Mike Johnson', '$2a$10$65ixGN84pEFJXzbnk1x2N.y8.clEfZ/h1LyMzAE3vZ6wBIfwNEi36', 1, '2024-02-20 16:45:00', 'USER'),
('sarah@example.com', 'Sarah Wilson', '$2a$10$65ixGN84pEFJXzbnk1x2N.y8.clEfZ/h1LyMzAE3vZ6wBIfwNEi36', 0, '2024-03-01 11:20:00', 'USER');

-- 이메일 인증 데이터
INSERT INTO `email_verifications` (`user_id`, `token`, `expires_at`, `is_verified`) VALUES
(1, 'token123456789', '2024-01-15 12:00:00', 1),
(2, 'token987654321', '2024-02-01 16:30:00', 1),
(3, 'token456789123', '2024-02-10 11:15:00', 1),
(4, 'token321654987', '2024-02-20 18:45:00', 1),
(5, 'token789123456', '2024-03-01 13:20:00', 0);

-- 폴더 데이터
INSERT INTO `folders` (`user_id`, `name`, `parent_folder_id`) VALUES
-- 사용자 1 (관리자) 폴더
(1, 'JavaScript', NULL),
(1, 'Python', NULL),
(1, 'React', 1),
(1, 'Node.js', 1),
-- 사용자 2 (John) 폴더
(2, 'Frontend', NULL),
(2, 'Backend', NULL),
(2, 'CSS Tips', 5),
(2, 'API Documentation', 6),
-- 사용자 3 (Jane) 폴더
(3, 'Design Resources', NULL),
(3, 'Code Snippets', NULL),
(3, 'UI Components', 9),
-- 사용자 4 (Mike) 폴더
(4, 'DevOps', NULL),
(4, 'Database', NULL);

-- 스니펫 색상 데이터
INSERT INTO `snippet_colors` (`user_id`, `name`, `hex_code`) VALUES
-- 공통 색상 (user_id NULL)
(NULL, 'Red', '#FF5733'),
(NULL, 'Blue', '#3498DB'),
(NULL, 'Green', '#27AE60'),
(NULL, 'Yellow', '#F1C40F'),
(NULL, 'Purple', '#9B59B6'),
-- 사용자별 커스텀 색상
(1, 'Admin Orange', '#E67E22'),
(2, 'John Blue', '#2980B9'),
(3, 'Jane Pink', '#E91E63'),
(4, 'Mike Gray', '#95A5A6');

-- 태그 데이터 (사용자별)
INSERT INTO `tags` (`user_id`, `name`) VALUES
-- 사용자 1 (관리자) 태그
(1, 'javascript'),
(1, 'react'),
(1, 'nodejs'),
(1, 'tutorial'),
(1, 'best-practice'),
-- 사용자 2 (John) 태그
(2, 'frontend'),
(2, 'css'),
(2, 'html'),
(2, 'responsive'),
(2, 'animation'),
-- 사용자 3 (Jane) 태그
(3, 'design'),
(3, 'ui'),
(3, 'ux'),
(3, 'component'),
(3, 'figma'),
-- 사용자 4 (Mike) 태그
(4, 'devops'),
(4, 'docker'),
(4, 'aws'),
(4, 'database'),
(4, 'mysql');

-- 스니펫 데이터
INSERT INTO `snippets` (`user_id`, `folder_id`, `color_id`, `source_url`, `type`, `memo`, `like_count`, `visibility`) VALUES
-- 사용자 1 스니펫
(1, 1, 1, 'https://developer.mozilla.org', 'code', 'JavaScript 배열 메소드 정리', 15, 1),
(1, 1, 2, NULL, 'text', 'React Hook 사용법 노트', 8, 1),
(1, 3, 3, 'https://reactjs.org', 'code', 'useState 훅 예제', 22, 1),
(1, 2, 4, NULL, 'text', 'Python 가상환경 설정 방법', 5, 1),
-- 사용자 2 스니펫
(2, 5, 2, 'https://css-tricks.com', 'code', 'CSS Grid 레이아웃', 12, 1),
(2, 7, 3, NULL, 'img', '반응형 웹 디자인 예시', 3, 1),
(2, 6, 1, 'https://nodejs.org', 'code', 'Express.js 라우터 설정', 7, 1),
-- 사용자 3 스니펫
(3, 9, 8, 'https://figma.com', 'img', 'UI 컴포넌트 디자인', 18, 1),
(3, 11, 5, NULL, 'text', '사용자 경험 개선 아이디어', 4, 1),
(3, 10, 3, NULL, 'code', 'CSS 애니메이션 코드', 9, 1),
-- 사용자 4 스니펫
(4, 12, 9, 'https://docker.com', 'code', 'Docker Compose 설정', 11, 1),
(4, 13, 2, NULL, 'text', 'MySQL 성능 최적화 팁', 6, 0);

-- 코드 스니펫 내용
INSERT INTO `snippet_codes` (`snippet_id`, `content`, `language`) VALUES
(1, 'const numbers = [1, 2, 3, 4, 5];\nconst doubled = numbers.map(num => num * 2);\nconsole.log(doubled);', 'javascript'),
(3, 'import React, { useState } from ''react'';\n\nfunction Counter() {\n  const [count, setCount] = useState(0);\n  return (\n    <div>\n      <p>Count: {count}</p>\n      <button onClick={() => setCount(count + 1)}>+</button>\n    </div>\n  );\n}', 'javascript'),
(5, '.grid-container {\n  display: grid;\n  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));\n  gap: 20px;\n  padding: 20px;\n}', 'css'),
(7, 'const express = require(''express'');\nconst router = express.Router();\n\nrouter.get(''/users'', (req, res) => {\n  res.json({ message: ''Users endpoint'' });\n});\n\nmodule.exports = router;', 'javascript'),
(10, '@keyframes fadeIn {\n  from { opacity: 0; transform: translateY(20px); }\n  to { opacity: 1; transform: translateY(0); }\n}\n\n.fade-in {\n  animation: fadeIn 0.5s ease-out;\n}', 'css'),
(11, 'version: ''3.8''\nservices:\n  web:\n    build: .\n    ports:\n      - "3000:3000"\n  db:\n    image: mysql:8.0\n    environment:\n      MYSQL_ROOT_PASSWORD: password', 'yaml');

-- 텍스트 스니펫 내용
INSERT INTO `snippet_texts` (`snippet_id`, `content`) VALUES
(2, 'React Hook 사용 시 주의사항:\n1. 컴포넌트 최상위에서만 호출\n2. 조건문 안에서 사용 금지\n3. useEffect 의존성 배열 주의\n4. 커스텀 훅으로 로직 재사용'),
(4, 'Python 가상환경 설정:\n1. python -m venv venv\n2. source venv/bin/activate (Linux/Mac)\n3. venv\\Scripts\\activate (Windows)\n4. pip install -r requirements.txt'),
(9, 'UX 개선 아이디어:\n- 로딩 상태 명확히 표시\n- 에러 메시지 사용자 친화적으로\n- 키보드 네비게이션 지원\n- 접근성 고려한 색상 사용'),
(12, 'MySQL 성능 최적화:\n- 적절한 인덱스 설정\n- 쿼리 실행 계획 분석\n- 불필요한 컬럼 조회 피하기\n- 커넥션 풀 설정');

-- 이미지 스니펫 내용
INSERT INTO `snippet_images` (`snippet_id`, `image_url`, `alt_text`) VALUES
(6, '/images/responsive-design-example.png', '반응형 웹 디자인 예시 스크린샷'),
(8, '/images/ui-component-design.png', 'Figma에서 제작한 UI 컴포넌트 디자인');

-- 스니펫-태그 연결
INSERT INTO `snippet_tags` (`snippet_id`, `tag_id`) VALUES
-- 스니펫 1 (JavaScript 배열 메소드)
(1, 1), (1, 5),
-- 스니펫 2 (React Hook 노트)
(1, 2), (1, 4),
-- 스니펫 3 (useState 훅)
(3, 2), (3, 4), (3, 5),
-- 스니펫 4 (Python 가상환경)
(4, 4),
-- 스니펫 5 (CSS Grid)
(5, 6), (5, 7), (5, 9),
-- 스니펫 6 (반응형 디자인)
(6, 9), (6, 10),
-- 스니펫 7 (Express.js)
(7, 3),
-- 스니펫 8 (UI 컴포넌트)
(8, 11), (8, 12), (8, 14),
-- 스니펫 9 (UX 아이디어)
(9, 13),
-- 스니펫 10 (CSS 애니메이션)
(10, 7), (10, 10),
-- 스니펫 11 (Docker)
(11, 16), (11, 17),
-- 스니펫 12 (MySQL)
(12, 19), (12, 20);

-- 좋아요 데이터
INSERT INTO `likes` (`user_id`, `snippet_id`) VALUES
-- 사용자들이 서로의 스니펫에 좋아요
(2, 1), (3, 1), (4, 1), (2, 3), (3, 3), (4, 3),
(1, 5), (3, 5), (4, 5), (1, 7), (3, 7),
(1, 8), (2, 8), (4, 8), (1, 9), (2, 9),
(1, 11), (2, 11), (3, 11), (1, 12), (2, 12);

-- 북마크 데이터
INSERT INTO `bookmarks` (`user_id`, `snippet_id`) VALUES
-- 사용자들이 유용한 스니펫 북마크
(2, 1), (2, 3), (2, 4),
(3, 1), (3, 5), (3, 7),
(4, 1), (4, 3), (4, 8),
(1, 5), (1, 8), (1, 11);