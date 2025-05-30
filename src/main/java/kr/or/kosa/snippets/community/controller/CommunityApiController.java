package kr.or.kosa.snippets.community.controller;

import kr.or.kosa.snippets.community.model.BoardCategory;
import kr.or.kosa.snippets.community.model.Post;
import kr.or.kosa.snippets.community.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityApiController {
    private final BoardService boardService;

    /**
     * 카테고리 목록 API
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        try {
            List<BoardCategory> categories = boardService.getAllCategories();

            Map<String, Object> response = new HashMap<>();
            response.put("categories", categories);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("카테고리 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "카테고리를 불러올 수 없습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 전체 게시글 목록 API
     */
    @GetMapping("/posts/all")
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Post> posts = boardService.getAllPosts();

            // 간단한 페이징 처리 (실제로는 DB에서 LIMIT/OFFSET 사용 권장)
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, posts.size());
            List<Post> pagedPosts = posts.subList(Math.max(0, startIndex), endIndex);

            // 페이징 정보
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("totalPages", (int) Math.ceil((double) posts.size() / size));
            pagination.put("totalCount", posts.size());
            pagination.put("hasNext", endIndex < posts.size());
            pagination.put("hasPrev", page > 1);

            // 카테고리별 개수 (선택사항)
            Map<String, Integer> categoryCounts = new HashMap<>();
            List<BoardCategory> categories = boardService.getAllCategories();
            categoryCounts.put("all", posts.size());
            for (BoardCategory category : categories) {
                long count = posts.stream()
                        .filter(post -> category.getCategoryId().equals(post.getCategoryId()))
                        .count();
                categoryCounts.put(category.getCategoryId().toString(), (int) count);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("posts", pagedPosts);
            response.put("pagination", pagination);
            response.put("categoryCounts", categoryCounts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("전체 게시글 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "게시글을 불러올 수 없습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 카테고리별 게시글 목록 API
     */
    @GetMapping("/posts/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getPostsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Post> posts = boardService.getPostsByCategoryId(categoryId);

            // 간단한 페이징 처리
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, posts.size());
            List<Post> pagedPosts = posts.subList(Math.max(0, startIndex), endIndex);

            // 페이징 정보
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("totalPages", (int) Math.ceil((double) posts.size() / size));
            pagination.put("totalCount", posts.size());
            pagination.put("hasNext", endIndex < posts.size());
            pagination.put("hasPrev", page > 1);

            Map<String, Object> response = new HashMap<>();
            response.put("posts", pagedPosts);
            response.put("pagination", pagination);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("카테고리별 게시글 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "게시글을 불러올 수 없습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 검색 API
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Post> posts = boardService.searchPosts(keyword, searchType, categoryId, null, null);

            // 간단한 페이징 처리
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, posts.size());
            List<Post> pagedPosts = posts.subList(Math.max(0, startIndex), endIndex);

            // 페이징 정보
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("totalPages", (int) Math.ceil((double) posts.size() / size));
            pagination.put("totalCount", posts.size());
            pagination.put("hasNext", endIndex < posts.size());
            pagination.put("hasPrev", page > 1);

            Map<String, Object> response = new HashMap<>();
            response.put("posts", pagedPosts);
            response.put("pagination", pagination);
            response.put("totalCount", posts.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("게시글 검색 오류: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "검색 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}