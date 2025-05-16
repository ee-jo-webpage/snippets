package kr.or.kosa.snippets.controller;

import kr.or.kosa.snippets.mapper.LikeMapper;
import kr.or.kosa.snippets.mapper.SnippetMapper;
import kr.or.kosa.snippets.model.Snippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DebugController {

    @Autowired
    private SnippetMapper snippetMapper;

    @Autowired
    private LikeMapper likeMapper;

    @GetMapping("/debug/snippet-likes")
    public Map<String, Object> debugSnippetLikes() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Snippet> allSnippets = snippetMapper.getAllPublicSnippets();
            List<Snippet> popularSnippets = snippetMapper.getPopularSnippets(100);

            List<Map<String, Object>> snippetData = new ArrayList<>();
            for (Snippet snippet : allSnippets) {
                Map<String, Object> data = new HashMap<>();
                long actualLikeCount = likeMapper.countLikes(snippet.getSnippetId());

                data.put("snippetId", snippet.getSnippetId());
                data.put("memo", snippet.getMemo());
                data.put("storedLikeCount", snippet.getLikeCount());
                data.put("actualLikeCount", actualLikeCount);

                snippetData.add(data);
            }

            List<Map<String, Object>> popularData = new ArrayList<>();
            // popularData 부분 수정
            for (Snippet snippet : popularSnippets) {
                Map<String, Object> data = new HashMap<>();

                data.put("snippetId", snippet.getSnippetId());
                data.put("memo", snippet.getMemo());
                data.put("likeCount", snippet.getLikeCount());
                data.put("createdAt", snippet.getCreatedAt()); // 생성일 추가

                popularData.add(data);
            }

            result.put("allSnippets", snippetData);
            result.put("popularSnippets", popularData);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}