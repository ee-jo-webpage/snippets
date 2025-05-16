package kr.or.kosa.snippets.service;

import kr.or.kosa.snippets.mapper.BookmarkMapper;
import kr.or.kosa.snippets.model.Bookmark;
import kr.or.kosa.snippets.model.Snippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookmarkService {

    @Autowired
    BookmarkMapper bookmarkMapper;

    //특정 사용자의 북마크한 스니펫 가져오기
    public List<Snippet> getBookmaredSnippets(Long userId) {
//        return snippetMapper
        return bookmarkMapper.getBookmarkedSnippets(userId);
    }

    //북마크 추가
//    Bookmark inser

    //특정 사용자의 스니펫 북마크 해제
    void removeBookmarkFromSnippet(Long userId, Long bookmarkId) {

    }


}
