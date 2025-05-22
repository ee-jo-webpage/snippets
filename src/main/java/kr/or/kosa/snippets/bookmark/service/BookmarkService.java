package kr.or.kosa.snippets.bookmark.service;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.bookmark.mapper.BookmarkMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BookmarkService {

    @Autowired
    BookmarkMapper bookmarkMapper;

    //사용자별 북마크 스니펫 조회
    public List<Snippets> getAllBookmarkByUserId(Long userId) {
        return bookmarkMapper.selectBookmakredSnippetsByUserId(userId);
    }

    //북마크 여부 확인 (수정)
    public boolean isBookmarked(Long userId, Long snippetId) {
        return bookmarkMapper.isBookmarked(userId, snippetId) > 0;
    }

    //북마크 추가
    public void addBookmark(Long userId, Long snippetId) {
        log.info("북마크 추가 시도: userId={}, snippetId={}", userId, snippetId);
        bookmarkMapper.insertBookmark(userId, snippetId);
    }

    //북마크 삭제 (추가)
    public void removeBookmark(Long userId, Long snippetId) {
        log.info("북마크 삭제 시도: userId={}, snippetId={}", userId, snippetId);
        bookmarkMapper.deleteBookmark(userId, snippetId);
    }

    //북마크 토글 (추가/삭제) (추가)
    public boolean toggleBookmark(Long userId, Long snippetId) {
        if (isBookmarked(userId, snippetId)) {
            removeBookmark(userId, snippetId);
            return false; // 삭제됨
        } else {
            addBookmark(userId, snippetId);
            return true; // 추가됨
        }
    }

    //특정 스니펫 조회 (추가)
    public Snippets getSnippetById(Long snippetId) {
        return bookmarkMapper.selectSnippetById(snippetId);
    }

    //북마크와 함께 스니펫 조회 (북마크 여부 포함) (추가)
    public Snippets getSnippetWithBookmarkStatus(Long userId, Long snippetId) {
        Snippets snippet = bookmarkMapper.selectSnippetById(snippetId);
        // 여기서는 snippet에 북마크 여부를 직접 설정할 수 없으므로 별도 확인 필요
        return snippet;
    }

    // 모든 스니펫 조회 (추가)
    public List<Snippets> getAllSnippets() {
        return bookmarkMapper.selectAllSnippets();
    }
}