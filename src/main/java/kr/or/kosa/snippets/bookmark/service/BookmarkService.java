package kr.or.kosa.snippets.bookmark.service;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.model.SnippetTypeBasic;
import kr.or.kosa.snippets.basic.service.SnippetService;
import kr.or.kosa.snippets.bookmark.mapper.BookmarkMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BookmarkService {

    @Autowired
    BookmarkMapper bookmarkMapper;

    @Autowired
    SnippetService snippetService;  // SnippetService 추가

    //사용자별 북마크 스니펫 조회 - 색상 정보와 함께 조회
    public List<Snippets> getAllBookmarkByUserId(Long userId) {
        // 1. 북마크된 스니펫 기본 정보 + 색상 정보 조회
        List<Snippets> bookmarkedSnippets = bookmarkMapper.selectBookmakredSnippetsByUserId(userId);
        List<Snippets> completeSnippets = new ArrayList<>();

        // 2. 각 스니펫에 대해 완전한 정보를 스니펫 서비스를 통해 조회하고 색상 정보 유지
        for (Snippets snippet : bookmarkedSnippets) {
            try {
                // 색상 정보 임시 저장
                String colorName = snippet.getName();
                String hexCode = snippet.getHexCode();

                // 타입을 먼저 조회
                SnippetTypeBasic type = snippetService.getSnippetTypeById(snippet.getSnippetId());
                if (type != null) {
                    // 타입별 완전한 스니펫 정보 조회
                    Snippets completeSnippet = snippetService.getSnippetsById(snippet.getSnippetId(), type);
                    if (completeSnippet != null) {
                        // 색상 정보 복원 (MyBatis에서 조인으로 가져온 정보)
                        completeSnippet.setName(colorName);
                        completeSnippet.setHexCode(hexCode);
                        completeSnippets.add(completeSnippet);
                    }
                }
            } catch (Exception e) {
                log.error("북마크된 스니펫 조회 중 오류 발생 - snippetId: {}", snippet.getSnippetId(), e);
                // 오류가 발생한 스니펫은 건너뛰지만 나머지는 계속 처리
            }
        }

        return completeSnippets;
    }

    //북마크 여부 확인
    public boolean isBookmarked(Long userId, Long snippetId) {
        return bookmarkMapper.isBookmarked(userId, snippetId) > 0;
    }

    //북마크 추가
    public void addBookmark(Long userId, Long snippetId) {
        log.info("북마크 추가 시도: userId={}, snippetId={}", userId, snippetId);
        bookmarkMapper.insertBookmark(userId, snippetId);
    }

    //북마크 삭제
    public void removeBookmark(Long userId, Long snippetId) {
        log.info("북마크 삭제 시도: userId={}, snippetId={}", userId, snippetId);
        bookmarkMapper.deleteBookmark(userId, snippetId);
    }

    //북마크 토글 (추가/삭제)
    public boolean toggleBookmark(Long userId, Long snippetId) {
        if (isBookmarked(userId, snippetId)) {
            removeBookmark(userId, snippetId);
            return false; // 삭제됨
        } else {
            addBookmark(userId, snippetId);
            return true; // 추가됨
        }
    }

    //특정 스니펫 조회 - 스니펫 서비스 로직 활용
    public Snippets getSnippetById(Long snippetId) {
        try {
            // 타입을 먼저 조회
            SnippetTypeBasic type = snippetService.getSnippetTypeById(snippetId);
            if (type != null) {
                // 타입별 완전한 스니펫 정보 조회
                return snippetService.getSnippetsById(snippetId, type);
            }
        } catch (Exception e) {
            log.error("스니펫 조회 중 오류 발생 - snippetId: {}", snippetId, e);
        }
        return null;
    }

    //북마크와 함께 스니펫 조회 (북마크 여부 포함)
    public Snippets getSnippetWithBookmarkStatus(Long userId, Long snippetId) {
        Snippets snippet = getSnippetById(snippetId);
        // 여기서는 snippet에 북마크 여부를 직접 설정할 수 없으므로 별도 확인 필요
        // 필요시 Snippets 모델에 isBookmarked 필드를 추가하거나 별도 DTO 사용
        return snippet;
    }

    // 모든 스니펫 조회 - 스니펫 서비스로 위임
    public List<Snippets> getAllSnippets() {
        return snippetService.getAllSnippets();
    }

    // 특정 사용자가 작성한 모든 스니펫 조회 - 스니펫 서비스로 위임
    public List<Snippets> getSnippetsByUserId(Long userId) {
        return snippetService.getUserSnippets(userId);
    }
}