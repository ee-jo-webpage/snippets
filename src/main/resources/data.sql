-- 스키마 지정
USE `MY_SCHEMA`;

-- 사용자 데이터 (user_id 제거)
INSERT INTO `users` (`email`, `nickname`, `password`, `enabled`, `created_at`, `updated_at`, `deleted_at`, `reason`, `role`) VALUES
                                                                                                                                 ('admin@example.com', '관리자', '$2a$10$rJ0NKHSlxfUUtQz.wQbvNOPmUxfXKHn0O3RxOuS1fI0BVxQs.i/iG', 1, '2023-01-01 00:00:00', NULL, NULL, NULL, 'ADMIN'),
                                                                                                                                 ('user1@example.com', '코딩하는사람', '$2a$10$65ixGN84pEFJXzbnk1x2N.y8.clEfZ/h1LyMzAE3vZ6wBIfwNEi36', 1, '2023-01-02 10:30:00', '2023-02-15 15:45:00', NULL, NULL, 'USER'),
                                                                                                                                 ('user2@example.com', '개발자킴', '$2a$10$6hCFrT6mA0K1xrNlsZ7g8eYJJXbR5KZjdoP7DP/AUm.LoIwOTvxqq', 1, '2023-01-05 14:20:00', NULL, NULL, NULL, 'USER');

-- 폴더 데이터 (folder_id 제거, user_id는 1,2,3 대신 실제 생성된 값 사용)
INSERT INTO `folders` (`user_id`, `name`, `parent_folder_id`) VALUES
                                                                  (1, '관리자 폴더', NULL),
                                                                  (2, '자바스크립트', NULL),
                                                                  (2, '리액트', NULL),
                                                                  (2, '유틸리티', 2),
                                                                  (3, '자바', NULL),
                                                                  (3, '스프링', NULL);

-- 스니펫 색상 데이터 (color_id 제거)
INSERT INTO `snippet_colors` (`user_id`, `name`, `hex_code`) VALUES
                                                                 (NULL, '파란색', '#3498db'),
                                                                 (NULL, '빨간색', '#e74c3c'),
                                                                 (NULL, '녹색', '#2ecc71'),
                                                                 (NULL, '노란색', '#f1c40f'),
                                                                 (2, '연한 파란색', '#a7d1f7');

-- 태그 데이터 (tag_id 제거)
INSERT INTO `tags` (`name`) VALUES
                                ('JavaScript'),
                                ('React'),
                                ('Java'),
                                ('Spring'),
                                ('Frontend'),
                                ('Backend');

-- 스니펫 데이터 (snippet_id 제거, 다른 ID들은 실제 생성된 값 참조)
INSERT INTO `snippets` (`user_id`, `folder_id`, `color_id`, `source_url`, `type`, `memo`, `created_at`, `updated_at`, `like_count`, `visibility`) VALUES
                                                                                                                                                      (2, 2, 1, 'https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map', 'code', '배열 맵 함수 예제', '2023-02-01 10:00:00', NULL, 5, 1),
                                                                                                                                                      (2, 3, 3, 'https://reactjs.org/docs/hooks-state.html', 'code', 'React useState 훅 사용법', '2023-02-03 14:45:00', NULL, 8, 1),
                                                                                                                                                      (2, 4, 5, NULL, 'code', '날짜 포맷팅 유틸리티 함수', '2023-02-05 16:15:00', NULL, 2, 1),
                                                                                                                                                      (3, 5, 2, 'https://docs.oracle.com/javase/tutorial/java/concepts/', 'code', '자바 Stream API 예제', '2023-02-07 11:25:00', NULL, 7, 1),
                                                                                                                                                      (3, 6, 5, 'https://spring.io/guides/gs/rest-service/', 'code', 'Spring Boot Rest Controller 예제', '2023-02-09 10:45:00', NULL, 9, 1),
                                                                                                                                                      (2, 2, 3, NULL, 'text', '자바스크립트 학습 노트', '2023-02-20 11:40:00', NULL, 2, 1),
                                                                                                                                                      (3, 6, 1, 'https://spring.io/blog', 'text', '스프링 프레임워크 정리', '2023-02-21 15:10:00', NULL, 7, 1),
                                                                                                                                                      (2, 4, 4, 'https://unsplash.com/photos/example1', 'img', '웹 디자인 참고 이미지', '2023-02-24 14:15:00', NULL, 9, 1),
                                                                                                                                                      (3, 5, 2, 'https://unsplash.com/photos/example2', 'img', '자바 프로젝트 구조도', '2023-02-25 16:30:00', NULL, 5, 1);

-- 코드 스니펫 데이터 (snippet_id는 실제 생성된 값 참조)
INSERT INTO `snippet_codes` (`snippet_id`, `content`, `language`) VALUES
                                                                      (1, '// 배열의 각 요소를 두 배로 만들기\nconst numbers = [1, 2, 3, 4, 5];\nconst doubled = numbers.map(num => num * 2);\nconsole.log(doubled); // [2, 4, 6, 8, 10]', 'javascript'),
                                                                      (2, 'import React, { useState } from "react";\n\nfunction Counter() {\n  const [count, setCount] = useState(0);\n  \n  return (\n    <div>\n      <p>You clicked {count} times</p>\n      <button onClick={() => setCount(count + 1)}>\n        Click me\n      </button>\n    </div>\n  );\n}', 'jsx'),
                                                                      (3, '/**\n * 날짜를 YYYY-MM-DD 형식으로 포맷팅\n */\nfunction formatDate(date) {\n  const year = date.getFullYear();\n  const month = String(date.getMonth() + 1).padStart(2, "0");\n  const day = String(date.getDate()).padStart(2, "0");\n  \n  return `${year}-${month}-${day}`;\n}\n\nconst today = new Date();\nconsole.log(formatDate(today));', 'javascript'),
                                                                      (4, 'import java.util.Arrays;\nimport java.util.List;\nimport java.util.stream.Collectors;\n\npublic class StreamExample {\n    public static void main(String[] args) {\n        List<String> names = Arrays.asList("John", "Jane", "Jack", "Joe");\n        \n        // 이름이 J로 시작하고 4글자인 이름만 필터링\n        List<String> filteredNames = names.stream()\n            .filter(name -> name.startsWith("J"))\n            .filter(name -> name.length() == 4)\n            .collect(Collectors.toList());\n            \n        System.out.println(filteredNames); // [John, Jack]\n    }\n}', 'java'),
                                                                      (5, 'import org.springframework.web.bind.annotation.RestController;\nimport org.springframework.web.bind.annotation.RequestMapping;\nimport org.springframework.web.bind.annotation.GetMapping;\nimport org.springframework.web.bind.annotation.PathVariable;\n\n@RestController\n@RequestMapping("/api/users")\npublic class UserController {\n\n    @GetMapping("/{id}")\n    public User getUserById(@PathVariable Long id) {\n        // 실제로는 서비스 계층에서 사용자 조회\n        return userService.findById(id)\n            .orElseThrow(() -> new ResourceNotFoundException("User not found"));\n    }\n}', 'java');

-- 텍스트 스니펫 데이터 (snippet_id는 실제 생성된 값 참조)
INSERT INTO `snippet_texts` (`snippet_id`, `content`) VALUES
                                                          (6, '자바스크립트 기초 문법 정리\n\n1. 변수 선언\n- var: 함수 스코프\n- let: 블록 스코프, 재할당 가능\n- const: 블록 스코프, 재할당 불가능\n\n2. 함수 선언\n- 함수 선언식: function name() {}\n- 함수 표현식: const name = function() {}\n- 화살표 함수: const name = () => {}\n\n3. 배열 메서드\n- map(): 각 요소를 변환하여 새 배열 반환\n- filter(): 조건에 맞는 요소만 새 배열로 반환\n- reduce(): 배열의 모든 요소를 하나의 값으로 줄임\n\n4. 비동기 처리\n- Promise\n- async/await\n\n다음에 더 공부할 내용: 클로저, 프로토타입, 이벤트 루프'),
                                                          (7, '스프링 프레임워크 핵심 개념\n\n1. IoC (Inversion of Control)\n- 객체의 생성과 관계 설정, 사용, 생명 주기 관리 등을 프레임워크가 대신 해줌\n\n2. DI (Dependency Injection)\n- 객체 간의 의존관계를 외부에서 주입\n- 생성자 주입, 세터 주입, 필드 주입\n\n3. AOP (Aspect Oriented Programming)\n- 공통 관심사를 분리하여 모듈화\n- 트랜잭션, 로깅, 보안 등에 활용\n\n4. PSA (Portable Service Abstraction)\n- 환경과 세부 기술의 변화에 관계없이 일관된 방식으로 기술 접근\n- JDBC, JPA, 트랜잭션 등\n\n5. 스프링 부트\n- 자동 설정 (Auto Configuration)\n- 내장 서버\n- 스타터 의존성\n\n다음에 공부할 내용: 스프링 시큐리티, 스프링 데이터 JPA');

-- 이미지 스니펫 데이터 (snippet_id는 실제 생성된 값 참조)
INSERT INTO `snippet_images` (`snippet_id`, `image_url`, `alt_text`) VALUES
                                                                         (8, 'https://example.com/images/web-design-reference.jpg', '웹 디자인 레이아웃 참고 이미지'),
                                                                         (9, 'https://example.com/images/java-project-structure.png', '자바 프로젝트 계층 구조 다이어그램');

-- 스니펫-태그 연결 (snippet_id, tag_id는 실제 생성된 값 참조)
INSERT INTO `snippet_tags` (`snippet_id`, `tag_id`) VALUES
                                                        (1, 1),
                                                        (1, 5),
                                                        (2, 2),
                                                        (2, 5),
                                                        (3, 1),
                                                        (3, 5),
                                                        (4, 3),
                                                        (4, 6),
                                                        (5, 4),
                                                        (5, 6),
                                                        (6, 1),
                                                        (6, 5),
                                                        (7, 4),
                                                        (7, 6),
                                                        (8, 5),
                                                        (9, 3),
                                                        (9, 6);

-- 좋아요 데이터 (user_id, snippet_id는 실제 생성된 값 참조)
INSERT INTO `likes` (`user_id`, `snippet_id`, `created_at`) VALUES
                                                                (1, 5, '2023-02-10 10:00:00'),
                                                                (2, 4, '2023-02-08 14:30:00'),
                                                                (2, 5, '2023-02-10 15:45:00'),
                                                                (3, 1, '2023-02-02 09:15:00'),
                                                                (3, 2, '2023-02-04 11:20:00');

-- 북마크 데이터 (user_id, snippet_id는 실제 생성된 값 참조)
INSERT INTO `bookmarks` (`user_id`, `snippet_id`) VALUES
                                                      (1, 2),
                                                      (1, 5),
                                                      (2, 4),
                                                      (2, 9),
                                                      (3, 1),
                                                      (3, 8);