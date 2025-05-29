//package kr.or.kosa.snippets.basic.controller;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.github.pagehelper.PageHelper;
//import com.github.pagehelper.PageInfo;
//
//import kr.or.kosa.snippets.basic.model.SnippetTypeBasic;
//import kr.or.kosa.snippets.basic.model.Snippets;
//import kr.or.kosa.snippets.basic.service.SnippetService;
//import kr.or.kosa.snippets.color.model.Color;
//import kr.or.kosa.snippets.color.service.ColorService;
//import kr.or.kosa.snippets.user.service.CustomUserDetails;
//
//@RestController
//@RequestMapping("/api/snippets")
//public class SnippetRestController {
//
//    private final SnippetService snippetService;
//    private final ColorService colorService;
//
//    public SnippetRestController(SnippetService snippetService, ColorService colorService) {
//        this.snippetService = snippetService;
//        this.colorService = colorService;
//    }
//
//    /**
//     * 사용자별 스니펫 목록 조회 (페이징 지원)
//     */
//    @GetMapping
//    public ResponseEntity<Map<String, Object>> getUserSnippets(
//            @AuthenticationPrincipal CustomUserDetails details,
//            @RequestParam(value = "page", defaultValue = "1") int page,
//            @RequestParam(value = "size", defaultValue = "30") int size) {
//
//        // 1) 로그인 체크
//        if (details == null || details.getUserId() == null) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("success", false);
//            errorResponse.put("message", "로그인이 필요합니다.");
//            return ResponseEntity.status(401).body(errorResponse);
//        }
//
//        Long userId = details.getUserId();
//
//        // 2) 페이징 설정
//        PageHelper.startPage(page, size);
//
//        // 3) 스니펫 조회
//        List<Snippets> snippets = snippetService.getUserSnippets(userId);
//        PageInfo<Snippets> pageInfo = new PageInfo<>(snippets);
//
//        // 4) 스니펫 데이터 변환 (색상 정보 포함)
//        List<Map<String, Object>> snippetDataList = convertSnippetsToResponseData(snippets);
//
//        // 5) 응답 데이터 구성
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", true);
//        response.put("data", snippetDataList);
//
//        // 페이징 정보
//        Map<String, Object> pagination = new HashMap<>();
//        pagination.put("currentPage", pageInfo.getPageNum());
//        pagination.put("totalPages", pageInfo.getPages());
//        pagination.put("totalItems", pageInfo.getTotal());
//        pagination.put("pageSize", pageInfo.getPageSize());
//        pagination.put("hasNext", pageInfo.isHasNextPage());
//        pagination.put("hasPrevious", pageInfo.isHasPreviousPage());
//
//        response.put("pagination", pagination);
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * 특정 태그의 스니펫 목록 조회
//     */
//    @GetMapping("/tag/{tagId}")
//    public ResponseEntity<List<Map<String, Object>>> getSnippetsByTag(
//            @PathVariable("tagId") Long tagId,
//            @AuthenticationPrincipal CustomUserDetails details) {
//
//        // 로그인 체크
//        if (details == null || details.getUserId() == null) {
//            return ResponseEntity.status(401).build();
//        }
//
//        try {
//            // 태그별 스니펫 조회 (서비스에 메서드가 있다고 가정)
//            List<Snippets> snippets = snippetService.getSnippetsByTagAndUser(tagId, details.getUserId());
//
//            // 데이터 변환
//            List<Map<String, Object>> responseData = convertSnippetsToResponseData(snippets);
//
//            return ResponseEntity.ok(responseData);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).build();
//        }
//    }
//
//    /**
//     * 단일 스니펫 상세 조회
//     */
//    @GetMapping("/{snippetId}")
//    public ResponseEntity<Map<String, Object>> getSnippetById(
//            @PathVariable("snippetId") Long snippetId,
//            @AuthenticationPrincipal CustomUserDetails details) {
//
//        // 로그인 체크
//        if (details == null || details.getUserId() == null) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("success", false);
//            errorResponse.put("message", "로그인이 필요합니다.");
//            return ResponseEntity.status(401).body(errorResponse);
//        }
//
//        try {
//            // 스니펫 타입 조회
//            SnippetTypeBasic type = snippetService.getSnippetTypeById(snippetId);
//            if (type == null) {
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "스니펫을 찾을 수 없습니다.");
//                return ResponseEntity.status(404).body(errorResponse);
//            }
//
//            // 스니펫 조회
//            Snippets snippet = snippetService.getSnippetsById(snippetId, type);
//            if (snippet == null) {
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "스니펫을 찾을 수 없습니다.");
//                return ResponseEntity.status(404).body(errorResponse);
//            }
//
//            // 권한 체크 (자신의 스니펫인지 확인)
//            if (!snippet.getUserId().equals(details.getUserId())) {
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("message", "접근 권한이 없습니다.");
//                return ResponseEntity.status(403).body(errorResponse);
//            }
//
//            // 응답 데이터 구성
//            Map<String, Object> snippetData = convertSnippetToResponseData(snippet);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("data", snippetData);
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("success", false);
//            errorResponse.put("message", "서버 오류가 발생했습니다.");
//            return ResponseEntity.status(500).body(errorResponse);
//        }
//    }
//
//    /**
//     * 스니펫 목록을 응답 데이터로 변환
//     */
//    private List<Map<String, Object>> convertSnippetsToResponseData(List<Snippets> snippets) {
//        List<Map<String, Object>> responseList = new ArrayList<>();
//
//        for (Snippets snippet : snippets) {
//            Map<String, Object> snippetData = convertSnippetToResponseData(snippet);
//            responseList.add(snippetData);
//        }
//
//        return responseList;
//    }
//
//    /**
//     * 단일 스니펫을 응답 데이터로 변환
//     */
//    private Map<String, Object> convertSnippetToResponseData(Snippets snippet) {
//        Map<String, Object> data = new HashMap<>();
//
//        // 기본 정보
//        data.put("id", snippet.getSnippetId());
//        data.put("snippetId", snippet.getSnippetId());
//        data.put("userId", snippet.getUserId());
//        data.put("type", snippet.getType().toString());
//        data.put("memo", snippet.getMemo());
//        data.put("sourceUrl", snippet.getSourceUrl());
//        data.put("visibility", snippet.getVisibility());
//        data.put("likeCount", snippet.getLikeCount());
//        data.put("createdAt", snippet.getCreatedAt());
//        data.put("updatedAt", snippet.getUpdatedAt());
//
//        // 제목 설정 (memo를 title로 사용)
//        data.put("title", snippet.getMemo());
//
//        // 타입별 특화 정보
//        switch (snippet.getType()) {
//            case CODE:
//                data.put("content", snippet.getContent());
//                data.put("language", snippet.getLanguage());
//                break;
//            case TEXT:
//                data.put("content", snippet.getContent());
//                break;
//            case IMG:
//                data.put("imageUrl", snippet.getImageUrl());
//                data.put("altText", snippet.getAltText());
//                // 이미지의 경우 altText나 memo를 content로도 제공
//                data.put("content", snippet.getAltText() != null ? snippet.getAltText() : snippet.getMemo());
//                break;
//        }
//
//        // 색상 정보 추가
//        if (snippet.getColorId() != null) {
//            try {
//                Color color = colorService.getColorById(snippet.getColorId());
//                if (color != null) {
//                    data.put("colorId", color.getColorId());
//                    data.put("hexCode", color.getHexCode());
//                    data.put("colorName", color.getName());
//                }
//            } catch (Exception e) {
//                // 색상 조회 실패시 기본값 설정
//                data.put("colorId", null);
//                data.put("hexCode", null);
//                data.put("colorName", null);
//            }
//        } else {
//            data.put("colorId", null);
//            data.put("hexCode", null);
//            data.put("colorName", null);
//        }
//
//        return data;
//    }
//}