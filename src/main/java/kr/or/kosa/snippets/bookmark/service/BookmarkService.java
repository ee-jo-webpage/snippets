package kr.or.kosa.snippets.bookmark.service;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.model.SnippetTypeBasic;
import kr.or.kosa.snippets.basic.service.SnippetService;
import kr.or.kosa.snippets.bookmark.mapper.BookmarkMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
        try {
            Integer count = bookmarkMapper.countBookmark(userId, snippetId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("북마크 상태 확인 중 오류 발생 - 사용자 ID: {}, 스니펫 ID: {}, 오류: {}",
                    userId, snippetId, e.getMessage(), e);
            return false; // 오류 발생 시 북마크되지 않은 것으로 처리
        }
    }



    //북마크 추가
    public void addBookmark(Long userId, Long snippetId) {
        try {
            // 중복 확인을 한 번 더 수행 (동시성 문제 대비)
            if (isBookmarked(userId, snippetId)) {
                log.warn("이미 북마크된 스니펫입니다 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);
                return; // 이미 북마크되어 있으면 그냥 리턴 (예외 발생하지 않음)
            }

            bookmarkMapper.insertBookmark(userId, snippetId);
            log.info("북마크 추가 성공 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);
        } catch (DuplicateKeyException e) {
            // Primary Key 중복 오류가 발생해도 이미 북마크되어 있다는 의미이므로 정상 처리
            log.warn("북마크 중복 추가 시도 (이미 존재함) - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);
            // 예외를 던지지 않고 정상 처리
        } catch (Exception e) {
            log.error("북마크 추가 중 오류 발생 - 사용자 ID: {}, 스니펫 ID: {}, 오류: {}",
                    userId, snippetId, e.getMessage(), e);
            throw new RuntimeException("북마크 추가 중 오류가 발생했습니다.", e);
        }
    }

    //북마크 삭제
    public void removeBookmark(Long userId, Long snippetId) {
        try {
            int deletedRows = bookmarkMapper.deleteBookmarkk(userId, snippetId);
            if (deletedRows == 0) {
                log.warn("삭제할 북마크가 없습니다 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);
            } else {
                log.info("북마크 제거 성공 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);
            }
        } catch (Exception e) {
            log.error("북마크 제거 중 오류 발생 - 사용자 ID: {}, 스니펫 ID: {}, 오류: {}",
                    userId, snippetId, e.getMessage(), e);
            throw new RuntimeException("북마크 제거 중 오류가 발생했습니다.", e);
        }
    }


    //북마크 토글 (추가/삭제)
    public boolean toggleBookmark(Long userId, Long snippetId) {
        try {
            // 현재 북마크 상태 확인
            boolean isCurrentlyBookmarked = isBookmarked(userId, snippetId);

            if (isCurrentlyBookmarked) {
                // 이미 북마크되어 있으면 제거
                removeBookmark(userId, snippetId);
                log.info("북마크 제거 완료 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);
                return false; // 북마크 제거됨
            } else {
                // 북마크되어 있지 않으면 추가
                addBookmark(userId, snippetId);
                log.info("북마크 추가 완료 - 사용자 ID: {}, 스니펫 ID: {}", userId, snippetId);
                return true; // 북마크 추가됨
            }
        } catch (Exception e) {
            log.error("북마크 토글 중 오류 발생 - 사용자 ID: {}, 스니펫 ID: {}, 오류: {}",
                    userId, snippetId, e.getMessage(), e);
            throw new RuntimeException("북마크 처리 중 오류가 발생했습니다.", e);
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