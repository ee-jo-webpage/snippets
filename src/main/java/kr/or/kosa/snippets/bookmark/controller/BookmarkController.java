package kr.or.kosa.snippets.bookmark.controller;

import jakarta.servlet.http.HttpSession;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.bookmark.model.Bookmark;
import kr.or.kosa.snippets.bookmark.service.BookmarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@RequestMapping("/bookmark")
@Slf4j
@Controller
public class BookmarkController {

    @Autowired
    BookmarkService bookmarkService;



    //사용자별 북마크 조회
    @GetMapping
    public String getAllBookmarksByUserId(HttpSession session, Model model) {
        Long currentId = (Long) session.getAttribute("userId");
        log.info("사용자 ID: {}", currentId);

        List<Snippets> bookmarkList = bookmarkService.getAllBookmarkByUserId(currentId);
        log.info("조회된 북마크 수: {}", bookmarkList.size());

        model.addAttribute("bookmarks", bookmarkList);
        model.addAttribute("count", bookmarkList.size());

        return "bookmark/bookmark-list"; // simple-test.html로 변경
    }


}
