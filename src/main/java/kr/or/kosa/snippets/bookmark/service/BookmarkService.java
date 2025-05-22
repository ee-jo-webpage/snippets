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

    public boolean isBookmarked(Long userId, Long snippetId) {
        return false;
    }

    // 새로 추가할 메서드들
    public void addBookmark(Long userId, Long snippetId) {
        log.info("북마크 추가 시도: userId={}, snippetId={}", userId, snippetId);
        bookmarkMapper.insertBookmark(userId, snippetId);
    }
}
