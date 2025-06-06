package kr.or.kosa.snippets.community.controller;

import kr.or.kosa.snippets.community.model.BoardCategory;
import kr.or.kosa.snippets.community.model.Post;
import kr.or.kosa.snippets.community.model.PostAttachment;
import kr.or.kosa.snippets.community.service.BoardService;
import kr.or.kosa.snippets.community.service.CommentService;
import kr.or.kosa.snippets.community.service.NotificationService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Objects;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;
    private final NotificationService notificationService;

    // 게시판 메인 페이지
    @GetMapping
    public String boardMain(@RequestParam(required = false) Integer categoryId, Model model) {
        try {
            List<BoardCategory> categories = boardService.getAllCategories();
            List<Post> posts;

            if (categoryId != null) {
                // 특정 카테고리가 지정된 경우
                posts = boardService.getPostsByCategoryId(categoryId);
                BoardCategory currentCategory = boardService.getCategoryById(categoryId);
                model.addAttribute("currentCategoryId", categoryId);
                model.addAttribute("currentCategoryName", currentCategory != null ? currentCategory.getName() : "알 수 없음");
                model.addAttribute("category", currentCategory);
            } else {
                // 기본값: 자유게시판 (categoryId = 1)
                posts = boardService.getPostsByCategoryId(1);
                model.addAttribute("currentCategoryId", 1);
                model.addAttribute("currentCategoryName", "자유게시판");
                model.addAttribute("category", boardService.getCategoryById(1));
            }

            model.addAttribute("categories", categories);
            model.addAttribute("posts", posts);

            return "community/categoryList"; // 메인 템플릿
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "데이터베이스 연결에 문제가 있습니다. 관리자에게 문의하세요.");
            return "error/database-error";
        }
    }


    // 게시글 목록 - AJAX 지원 추가
    @GetMapping("/category/{categoryId}")
    public String postList(@PathVariable Integer categoryId,
                           @RequestParam(required = false) Boolean ajax,
                           Model model) {
        try {
            BoardCategory category = boardService.getCategoryById(categoryId);
            List<Post> posts = boardService.getPostsByCategoryId(categoryId);
            List<BoardCategory> categories = boardService.getAllCategories(); // 전체 카테고리 정보 추가

            model.addAttribute("category", category);
            model.addAttribute("posts", posts);
            model.addAttribute("categories", categories);
            model.addAttribute("currentCategoryId", categoryId);
            model.addAttribute("currentCategoryName", category.getName());

            // AJAX 요청인 경우 콘텐츠만 포함된 템플릿 반환
            if (ajax != null && ajax) {
                return "community/postListContent"; // 콘텐츠만 포함하는 템플릿
            }

            // 일반 요청인 경우 전체 템플릿 반환
            return "community/postList";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "게시글 목록을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }


    // 전체 게시글 목록 - AJAX 지원 추가
    @GetMapping("/posts/all")
    public String allPostsList(@RequestParam(required = false) Boolean ajax,
                               Model model) {
        try {
            List<Post> allPosts = boardService.getAllPosts();
            List<BoardCategory> categories = boardService.getAllCategories();

            model.addAttribute("posts", allPosts);
            model.addAttribute("categories", categories);
            model.addAttribute("currentCategoryId", "all");
            model.addAttribute("currentCategoryName", "전체 게시글");
            model.addAttribute("category", new BoardCategory()); // 빈 카테고리 객체 추가

            // AJAX 요청인 경우 콘텐츠만 포함된 템플릿 반환
            if (ajax != null && ajax) {
                return "community/postListContent"; // 콘텐츠만 포함하는 템플릿
            }

            // 일반 요청인 경우 전체 템플릿 반환
            return "community/postList";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "게시글 목록을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }


    /**
     * 게시글 작성 폼
     * 중요: 이 메서드는 @GetMapping("/post/{postId}") 메서드보다 반드시 위에 위치해야 합니다.
     * Spring MVC는 더 구체적인 URL 패턴을 먼저 처리하므로, 명시적인 "/post/new" 경로가
     * 와일드카드 경로인 "/post/{postId}"보다 먼저 처리되어야 합니다.
     */
    @GetMapping("/post/new")
    public String newPostForm(@RequestParam(required = false) Integer categoryId,
                              Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인 체크
        if (userDetails == null) {
            return "redirect:/login?message=loginRequired";
        }

        try {
            System.out.println("=== 게시글 작성 폼 접근 ===");

            // 카테고리 목록 조회
            List<BoardCategory> categories = boardService.getAllCategories();
            model.addAttribute("categories", categories);

            // 새 게시글 객체 생성
            Post post = new Post();

            // 카테고리 ID가 지정된 경우 설정
            if (categoryId != null) {
                post.setCategoryId(categoryId);
                System.out.println("선택된 카테고리 ID: " + categoryId);
            }

            model.addAttribute("post", post);
            model.addAttribute("isNew", true);
            model.addAttribute("currentUserNickname", userDetails.getNickname());

            return "community/postForm";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "게시글 작성 폼을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }

    /*
    // 게시글 상세
    @GetMapping("/post/{postId}")
    public String postDetail(@PathVariable Integer postId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        try {
            Post post = boardService.getPostById(postId);
            if (post == null) {
                return "redirect:/community";
            }

            // CustomUserDetails에서 사용자 ID 추출
            Long currentUserId = getCurrentUserId(userDetails);
            List<PostAttachment> attachments = boardService.getAttachmentsByPostId(postId);

            model.addAttribute("post", post);
            model.addAttribute("attachments", attachments);
            model.addAttribute("currentUserId", currentUserId);
            model.addAttribute("isLoggedIn", userDetails != null);
            model.addAttribute("currentUserNickname", userDetails != null ? userDetails.getNickname() : null);

            return "community/postDetail";
        } catch (Exception e) {
            model.addAttribute("error", "게시글을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }
*/


    @GetMapping("/post/{postId}")
    public String postDetail(@PathVariable Integer postId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        System.out.println("=== 게시글 상세 조회 시작 ===");
        System.out.println("요청된 postId: " + postId);

        try {
            // 1. 게시글 조회
            Post post = boardService.getPostById(postId);
            if (post == null) {
                System.out.println("게시글을 찾을 수 없음");
                return "redirect:/community";
            }
            System.out.println("게시글 조회 성공: " + post.getTitle());

            // 2. 사용자 ID 추출 (안전하게)
            Long currentUserId = getCurrentUserId(userDetails);
            System.out.println("현재 사용자 ID: " + currentUserId);
            System.out.println("게시글 작성자 ID: " + post.getUserId());

            // 3. 첨부파일 조회
            List<PostAttachment> attachments = null;
            try {
                attachments = boardService.getAttachmentsByPostId(postId);
                System.out.println("첨부파일 개수: " + (attachments != null ? attachments.size() : 0));
            } catch (Exception e) {
                System.out.println("첨부파일 조회 오류: " + e.getMessage());
                attachments = new ArrayList<>();
            }

            // 4. 권한 체크 (수정된 버전)
            boolean canEdit = false;
            boolean canDelete = false;

            if (currentUserId != null && post.getUserId() != null) {
                System.out.println("\n=== 권한 체크 시작 ===");

                // Long과 Long을 직접 비교 (Objects.equals 사용)
                boolean isAuthor = Objects.equals(currentUserId, post.getUserId());

                // 관리자 확인
                boolean isAdmin = isAdmin(userDetails);

                canEdit = isAuthor || isAdmin;
                canDelete = isAuthor || isAdmin;

                System.out.println("권한 체크 결과:");
                System.out.println("- 현재 사용자 ID: " + currentUserId + " (타입: " + currentUserId.getClass().getSimpleName() + ")");
                System.out.println("- 게시글 작성자 ID: " + post.getUserId() + " (타입: " + post.getUserId().getClass().getSimpleName() + ")");
                System.out.println("- Objects.equals() 결과: " + isAuthor);
                System.out.println("- 관리자 여부: " + isAdmin);
                System.out.println("- 수정 권한: " + canEdit);
                System.out.println("- 삭제 권한: " + canDelete);
                System.out.println("=== 권한 체크 완료 ===\n");
            } else {
                System.out.println("❌ 사용자 ID 또는 게시글 작성자 ID가 null");
            }

            // 5. 모델에 데이터 추가
            model.addAttribute("post", post);
            model.addAttribute("attachments", attachments != null ? attachments : new ArrayList<>());
            model.addAttribute("currentUserId", currentUserId);
            model.addAttribute("isLoggedIn", userDetails != null);
            model.addAttribute("currentUserNickname", userDetails != null ? userDetails.getNickname() : null);
            model.addAttribute("canEdit", canEdit);
            model.addAttribute("canDelete", canDelete);

            System.out.println("=== 게시글 상세 조회 성공 ===");
            return "community/postDetail";

        } catch (Exception e) {
            System.out.println("=== 게시글 상세 조회 실패 ===");
            System.out.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "게시글을 불러올 수 없습니다: " + e.getMessage());
            return "redirect:/community?error=post_load_failed";
        }
    }


    // BoardController.java - addPost 메서드 수정
    @PostMapping("/post/new")
    public String addPost(@ModelAttribute Post post,
                          @RequestParam(value = "files", required = false) MultipartFile[] files,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {

        System.out.println("=== 게시글 등록 디버깅 시작 ===");

        // 로그인 체크
        if (userDetails == null) {
            System.out.println("ERROR: 사용자가 로그인되지 않음");
            return "redirect:/login?message=loginRequired";
        }

        try {
            // 사용자 정보 로깅
            Long userId = getCurrentUserId(userDetails);
            System.out.println("현재 사용자 ID: " + userId);
            System.out.println("현재 사용자 닉네임: " + userDetails.getNickname());

            // 게시글 정보 로깅
            System.out.println("게시글 제목: " + post.getTitle());
            System.out.println("게시글 내용 길이: " + (post.getContent() != null ? post.getContent().length() : 0));
            System.out.println("카테고리 ID: " + post.getCategoryId());
            System.out.println("공지사항 여부: " + post.isNotice());

            // 필수 필드 검증
            if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
                System.out.println("ERROR: 제목이 비어있음");
                return "redirect:/community/post/new?error=title";
            }

            if (post.getContent() == null || post.getContent().trim().isEmpty()) {
                System.out.println("ERROR: 내용이 비어있음");
                return "redirect:/community/post/new?error=content";
            }

            if (post.getCategoryId() == null) {
                System.out.println("ERROR: 카테고리가 선택되지 않음");
                return "redirect:/community/post/new?error=category";
            }

            // 사용자 ID 설정
            if (userId != null) {
                post.setUserId(userId);
                System.out.println("Post에 설정된 사용자 ID: " + post.getUserId());
            } else {
                System.out.println("ERROR: 사용자 ID가 null");
                return "redirect:/community/post/new?error=user";
            }

            // 기본값 설정
            if (post.getStatus() == null || post.getStatus().isEmpty()) {
                post.setStatus("published");
                System.out.println("게시글 상태를 'published'로 설정");
            }

            // 공지사항 체크박스 처리 (체크되지 않으면 false로 설정)
            System.out.println("공지사항 설정 전: " + post.isNotice());
            // boolean primitive 타입이므로 별도 처리 불필요
            System.out.println("공지사항 설정 후: " + post.isNotice());

            // 파일 정보 로깅
            if (files != null) {
                System.out.println("첨부파일 개수: " + files.length);
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isEmpty()) {
                        System.out.println("파일 " + (i+1) + ": " + files[i].getOriginalFilename()
                                + " (크기: " + files[i].getSize() + " bytes)");
                    }
                }
            }

            // 게시글 생성
            System.out.println("게시글 생성 시작...");
            Integer postId = boardService.createPost(post, files);
            System.out.println("게시글 생성 완료. 생성된 게시글 ID: " + postId);

            System.out.println("=== 게시글 등록 성공 ===");
            return "redirect:/community/post/" + postId + "?success=created";

        } catch (IOException e) {
            System.out.println("ERROR: 파일 업로드 오류 - " + e.getMessage());
            e.printStackTrace();
            return "redirect:/community/post/new?error=file";
        } catch (Exception e) {
            System.out.println("ERROR: 게시글 저장 오류 - " + e.getMessage());
            e.printStackTrace();
            return "redirect:/community/post/new?error=save";
        }
    }

    /*

    // 게시글 수정 폼
    @GetMapping("/post/{postId}/edit")
    public String editPostForm(@PathVariable Integer postId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        // 로그인 체크
        if (userDetails == null) {
            return "redirect:/login?message=loginRequired";
        }

        try {
            Post post = boardService.getPostById(postId);
            if (post == null) {
                return "redirect:/community";
            }

            Long currentUserId = getCurrentUserId(userDetails);
            // 작성자 또는 관리자만 수정 가능
            if (!post.getUserId().equals(currentUserId.intValue()) && !isAdmin(userDetails)) {
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            List<BoardCategory> categories = boardService.getAllCategories();
            List<PostAttachment> attachments = boardService.getAttachmentsByPostId(postId);

            model.addAttribute("post", post);
            model.addAttribute("categories", categories);
            model.addAttribute("attachments", attachments);

            return "community/postEditForm";
        } catch (Exception e) {
            return "redirect:/community";
        }
    }

    // 게시글 수정 처리
    @PostMapping("/post/{postId}/edit")
    public String updatePost(@PathVariable Integer postId,
                             @ModelAttribute Post post,
                             @RequestParam(value = "files", required = false) MultipartFile[] files,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인 체크
        if (userDetails == null) {
            return "redirect:/login?message=loginRequired";
        }

        try {
            Long currentUserId = getCurrentUserId(userDetails);
            Post existingPost = boardService.getPostById(postId);

            if (existingPost == null ||
                    (!existingPost.getUserId().equals(currentUserId.intValue()) && !isAdmin(userDetails))) {
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            post.setPostId(postId);
            post.setUserId(currentUserId);
            boardService.updatePost(post, files);
            return "redirect:/community/post/" + postId + "?success=updated";

        } catch (IOException e) {
            return "redirect:/community/post/" + postId + "/edit?error=file";
        } catch (Exception e) {
            return "redirect:/community/post/" + postId + "/edit?error=save";
        }
    }

    // 게시글 삭제
    @PostMapping("/post/{postId}/delete")
    public String deletePost(@PathVariable Integer postId,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인 체크
        if (userDetails == null) {
            return "redirect:/login?message=loginRequired";
        }

        try {
            Post post = boardService.getPostById(postId);
            if (post == null) {
                return "redirect:/community";
            }

            Long currentUserId = getCurrentUserId(userDetails);
            if (!post.getUserId().equals(currentUserId.intValue()) && !isAdmin(userDetails)) {
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            Integer categoryId = post.getCategoryId();
            boardService.deletePost(postId);
            return "redirect:/community/category/" + categoryId + "?success=deleted";

        } catch (Exception e) {
            return "redirect:/community";
        }
    }
*/

    // 게시글 수정 처리 - 디버깅 개선
    @PostMapping("/post/{postId}/edit")
    public String updatePost(@PathVariable Integer postId,
                             @ModelAttribute Post post,
                             @RequestParam(value = "files", required = false) MultipartFile[] files,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        System.out.println("=== 게시글 수정 처리 시작 ===");
        System.out.println("게시글 ID: " + postId);

        // 로그인 체크
        if (userDetails == null) {
            System.out.println("로그인되지 않은 사용자");
            return "redirect:/login?message=loginRequired";
        }

        try {
            // 기존 게시글 조회
            Post existingPost = boardService.getPostById(postId);
            if (existingPost == null) {
                System.out.println("기존 게시글을 찾을 수 없음");
                return "redirect:/community?error=post_not_found";
            }

            Long currentUserId = getCurrentUserId(userDetails);
            System.out.println("현재 사용자 ID: " + currentUserId);
            System.out.println("기존 게시글 작성자 ID: " + existingPost.getUserId());

            // 권한 체크 - Objects.equals 사용
            boolean isAuthor = Objects.equals(currentUserId, existingPost.getUserId());
            boolean isAdmin = isAdmin(userDetails);

            if (!isAuthor && !isAdmin) {
                System.out.println("수정 권한 없음");
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            // 수정할 데이터 설정
            post.setPostId(postId);
            post.setUserId(existingPost.getUserId()); // 원래 작성자 유지

            // 입력값 검증
            if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
                System.out.println("제목이 비어있음");
                return "redirect:/community/post/" + postId + "/edit?error=title";
            }

            if (post.getContent() == null || post.getContent().trim().isEmpty()) {
                System.out.println("내용이 비어있음");
                return "redirect:/community/post/" + postId + "/edit?error=content";
            }

            System.out.println("수정할 내용:");
            System.out.println("- 제목: " + post.getTitle());
            System.out.println("- 카테고리 ID: " + post.getCategoryId());
            System.out.println("- 공지사항 여부: " + post.isNotice());

            // 게시글 수정
            boardService.updatePost(post, files);

            System.out.println("=== 게시글 수정 성공 ===");
            return "redirect:/community/post/" + postId + "?success=updated";

        } catch (IOException e) {
            System.out.println("파일 처리 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/community/post/" + postId + "/edit?error=file";
        } catch (Exception e) {
            System.out.println("게시글 수정 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/community/post/" + postId + "/edit?error=save";
        }
    }

    @GetMapping("/post/{postId}/edit")
    public String editPostForm(@PathVariable Integer postId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        System.out.println("=== 게시글 수정 폼 요청 ===");
        System.out.println("게시글 ID: " + postId);

        // 로그인 체크
        if (userDetails == null) {
            System.out.println("로그인되지 않은 사용자");
            return "redirect:/login?message=loginRequired";
        }

        try {
            // 게시글 조회
            Post post = boardService.getPostById(postId);
            if (post == null) {
                System.out.println("게시글을 찾을 수 없음");
                return "redirect:/community?error=post_not_found";
            }

            Long currentUserId = getCurrentUserId(userDetails);
            System.out.println("현재 사용자 ID: " + currentUserId);
            System.out.println("게시글 작성자 ID: " + post.getUserId());

            // 권한 체크 - Objects.equals 사용으로 수정
            boolean isAuthor = Objects.equals(currentUserId, post.getUserId());
            boolean isAdmin = isAdmin(userDetails);

            System.out.println("권한 체크:");
            System.out.println("- 작성자 여부: " + isAuthor);
            System.out.println("- 관리자 여부: " + isAdmin);

            if (!isAuthor && !isAdmin) {
                System.out.println("수정 권한 없음");
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            // 카테고리 목록과 첨부파일 목록 조회
            List<BoardCategory> categories = boardService.getAllCategories();
            List<PostAttachment> attachments = boardService.getAttachmentsByPostId(postId);

            model.addAttribute("post", post);
            model.addAttribute("categories", categories);
            model.addAttribute("attachments", attachments != null ? attachments : new ArrayList<>());

            System.out.println("=== 게시글 수정 폼 로드 성공 ===");
            return "community/postEdit"; // 또는 "community/postEditDetail"

        } catch (Exception e) {
            System.out.println("게시글 수정 폼 로드 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/community/post/" + postId + "?error=load_failed";
        }
    }

    // 게시글 삭제
    @PostMapping("/post/{postId}/delete")
    public String deletePost(@PathVariable Integer postId,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        System.out.println("=== 게시글 삭제 요청 ===");
        System.out.println("게시글 ID: " + postId);

        // 로그인 체크
        if (userDetails == null) {
            System.out.println("로그인되지 않은 사용자");
            return "redirect:/login?message=loginRequired";
        }

        try {
            // 게시글 조회
            Post post = boardService.getPostById(postId);
            if (post == null) {
                System.out.println("게시글을 찾을 수 없음");
                return "redirect:/community?error=post_not_found";
            }

            Long currentUserId = getCurrentUserId(userDetails);
            System.out.println("현재 사용자 ID: " + currentUserId);
            System.out.println("게시글 작성자 ID: " + post.getUserId());

            // 권한 체크 - Objects.equals 사용으로 수정
            boolean isAuthor = Objects.equals(currentUserId, post.getUserId());
            boolean isAdmin = isAdmin(userDetails);

            System.out.println("권한 체크:");
            System.out.println("- 작성자 여부: " + isAuthor);
            System.out.println("- 관리자 여부: " + isAdmin);

            if (!isAuthor && !isAdmin) {
                System.out.println("삭제 권한 없음");
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            // 카테고리 ID 저장 (삭제 후 해당 카테고리로 돌아가기 위해)
            Integer categoryId = post.getCategoryId();

            // 게시글 삭제 (첨부파일도 함께 삭제됨)
            boardService.deletePost(postId);

            System.out.println("=== 게시글 삭제 성공 ===");
            return "redirect:/community?categoryId=" + categoryId + "&success=deleted";

        } catch (Exception e) {
            System.out.println("게시글 삭제 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/community/post/" + postId + "?error=delete_failed";
        }
    }

    // 게시글 검색
    @GetMapping("/search")
    public String searchPosts(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String searchType,
                              @RequestParam(required = false) Integer categoryId,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                              Model model) {
        try {
            List<Post> posts = boardService.searchPosts(keyword, searchType, categoryId, startDate, endDate);
            List<BoardCategory> categories = boardService.getAllCategories();

            model.addAttribute("posts", posts);
            model.addAttribute("categories", categories);
            model.addAttribute("keyword", keyword);
            model.addAttribute("searchType", searchType);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            return "community/searchResults";
        } catch (Exception e) {
            return "redirect:/community";
        }
    }

    // 첨부파일 다운로드
    @GetMapping("/attachment/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Integer attachmentId) {
        try {
            PostAttachment attachment = boardService.getAttachmentById(attachmentId);
            if (attachment == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            // 5단계: 파일 존재 및 읽기 가능 여부 확인
            System.out.println("- 파일 존재 여부: " + resource.exists());
            System.out.println("- 파일 읽기 가능: " + resource.isReadable());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + attachment.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //첨부파일 삭제 (AJAX)
    @DeleteMapping("/attachment/{attachmentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAttachment(
            @PathVariable Integer attachmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== 첨부파일 삭제 요청 ===");
            System.out.println("첨부파일 ID: " + attachmentId);

            // 로그인 체크
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 첨부파일 정보 조회
            PostAttachment attachment = boardService.getAttachmentById(attachmentId);
            if (attachment == null) {
                response.put("success", false);
                response.put("message", "첨부파일을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 게시글 정보 조회 (권한 체크용)
            Post post = boardService.getPostById(attachment.getPostId());
            if (post == null) {
                response.put("success", false);
                response.put("message", "게시글을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Long currentUserId = getCurrentUserId(userDetails);

            // 권한 체크 - 게시글 작성자 또는 관리자만 삭제 가능
            boolean isAuthor = Objects.equals(currentUserId, post.getUserId());
            boolean isAdmin = isAdmin(userDetails);

            if (!isAuthor && !isAdmin) {
                response.put("success", false);
                response.put("message", "첨부파일을 삭제할 권한이 없습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 실제 파일 삭제
            try {
                Path filePath = Paths.get(attachment.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    System.out.println("실제 파일 삭제 성공: " + attachment.getFileName());
                } else {
                    System.out.println("파일이 존재하지 않음: " + attachment.getFilePath());
                }
            } catch (IOException e) {
                System.out.println("파일 삭제 실패: " + e.getMessage());
                // 파일 삭제 실패해도 DB 정보는 삭제 진행
            }

            // DB에서 첨부파일 정보 삭제
            boardService.deleteAttachment(attachmentId);

            response.put("success", true);
            response.put("message", "첨부파일이 삭제되었습니다.");

            System.out.println("=== 첨부파일 삭제 완료 ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ 첨부파일 삭제 오류: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "첨부파일 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 이미지 업로드 (Summernote용)
    @PostMapping("/upload-image")
    @ResponseBody
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, String> result = new HashMap<>();

        // 로그인 체크
        if (userDetails == null) {
            result.put("error", "로그인이 필요합니다.");
            return result;
        }

        try {
            if (file.isEmpty()) {
                result.put("error", "파일이 비어있습니다.");
                return result;
            }

            // 이미지 파일 검증
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("error", "이미지 파일만 업로드 가능합니다.");
                return result;
            }

            // 파일 크기 검증 (5MB 제한)
            if (file.getSize() > 5 * 1024 * 1024) {
                result.put("error", "파일 크기는 5MB를 초과할 수 없습니다.");
                return result;
            }

            // 업로드 디렉토리 생성
            Path uploadDir = Paths.get("uploads", "images").toAbsolutePath();
            Files.createDirectories(uploadDir);

            // 파일명 생성 (중복 방지)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadDir.resolve(filename);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath);

            // 웹에서 접근 가능한 URL 반환
            result.put("imageUrl", "/uploads/images/" + filename);

        } catch (IOException e) {
            result.put("error", "파일 업로드 중 오류가 발생했습니다.");
        }

        return result;
    }


    /*
    //CustomUserDetails에서 사용자 ID 추출 (안전한 버전)
    private Long getCurrentUserId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            System.out.println("userDetails가 null입니다.");
            return null;
        }

        try {
            Long userId = userDetails.getUserId();
            System.out.println("추출된 사용자 ID: " + userId);
            return userId;
        } catch (Exception e) {
            System.out.println("사용자 ID 추출 중 오류: " + e.getMessage());
            return null;
        }
    }


     //관리자 권한 체크

    private boolean isAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
    */

    /**
     * CustomUserDetails에서 사용자 ID 추출 (안전한 버전)
     */
    private Long getCurrentUserId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }

        try {
            Long userId = userDetails.getUserId();
            return userId;
        } catch (Exception e) {
            System.out.println("사용자 ID 추출 중 오류: " + e.getMessage());
            return null;
        }
    }

    /**
     * 관리자 권한 체크
     */
    private boolean isAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        try {
            return userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        } catch (Exception e) {
            System.out.println("관리자 권한 체크 오류: " + e.getMessage());
            return false;
        }
    }
}