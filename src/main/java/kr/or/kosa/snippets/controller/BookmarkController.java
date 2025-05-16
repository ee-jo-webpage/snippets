package kr.or.kosa.snippets.controller;

import jakarta.servlet.http.HttpSession;
import kr.or.kosa.snippets.model.Bookmark;
import kr.or.kosa.snippets.model.Snippet;
import kr.or.kosa.snippets.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/bookmark")
public class BookmarkController {

    @Autowired
    BookmarkService bookmarkService;

    @GetMapping
    public String getBookmarkedSnippetByUserId(HttpSession session, Model model) {
        Long currentUserId = (Long)session.getAttribute("userId");

        List<Snippet> bookmarks = bookmarkService.getBookmaredSnippets(currentUserId);


        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("userId", currentUserId);
        model.addAttribute("viewType", "bookmarks");

        return "bookmarks/bookmark";
    }
    //특정 사용자의 북마크한 스니펫 조회

}
