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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;
    private final NotificationService notificationService;

    // 게시판 목록
    @GetMapping
    public String boardList(Model model) {
        try {
            List<BoardCategory> categories = boardService.getAllCategories();
            model.addAttribute("categories", categories);
            return "community/categoryList";
        } catch (Exception e) {
            model.addAttribute("error", "데이터베이스 연결에 문제가 있습니다. 관리자에게 문의하세요.");
            return "error/database-error";
        }
    }

    // 게시글 목록
    @GetMapping("/category/{categoryId}")
    public String postList(@PathVariable Integer categoryId, Model model) {
        try {
            BoardCategory category = boardService.getCategoryById(categoryId);
            List<Post> posts = boardService.getPostsByCategoryId(categoryId);

            model.addAttribute("category", category);
            model.addAttribute("posts", posts);
            return "community/postList";
        } catch (Exception e) {
            model.addAttribute("error", "게시글 목록을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }

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

    // 게시글 작성 폼
    @GetMapping("/post/new")
    public String newPostForm(Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인 체크
        if (userDetails == null) {
            return "redirect:/login?message=loginRequired";
        }

        try {
            List<BoardCategory> categories = boardService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("post", new Post());
            model.addAttribute("currentUserNickname", userDetails.getNickname());

            return "community/postForm";
        } catch (Exception e) {
            model.addAttribute("error", "게시글 작성 폼을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }

    // 게시글 작성 처리
    @PostMapping("/post/new")
    public String addPost(@ModelAttribute Post post,
                          @RequestParam(value = "files", required = false) MultipartFile[] files,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인 체크
        if (userDetails == null) {
            return "redirect:/login?message=loginRequired";
        }

        try {
            // CustomUserDetails에서 사용자 ID 가져오기
            Long userId = getCurrentUserId(userDetails);
            post.setUserId(userId.intValue());

            // 기본값 설정
            if (post.getStatus() == null || post.getStatus().isEmpty()) {
                post.setStatus("published");
            }
            // isNotice는 boolean이므로 null 체크 불필요 (기본값은 false)
            // 만약 null 체크가 필요하다면 Post 모델에서 Boolean으로 변경해야 함

            Integer postId = boardService.createPost(post, files);
            return "redirect:/community/post/" + postId + "?success=created";

        } catch (IOException e) {
            return "redirect:/community/post/new?error=file";
        } catch (Exception e) {
            e.printStackTrace(); // 디버깅용
            return "redirect:/community/post/new?error=save";
        }
    }

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
            post.setUserId(currentUserId.intValue());
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

    /**
     * CustomUserDetails에서 사용자 ID 추출
     */
    private Long getCurrentUserId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userDetails.getUserId();
    }

    /**
     * 관리자 권한 체크
     */
    private boolean isAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}