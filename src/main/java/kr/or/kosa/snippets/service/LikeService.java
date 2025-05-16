package kr.or.kosa.snippets.service;

import kr.or.kosa.snippets.config.AppConfig;
import kr.or.kosa.snippets.mapper.LikeMapper;
import kr.or.kosa.snippets.mapper.SnippetMapper;
import kr.or.kosa.snippets.model.Like;
import kr.or.kosa.snippets.model.Snippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeService {

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private SnippetMapper snippetMapper;

    // 좋아요 추가 (고정 사용자) - snippets 테이블 업데이트 없음
    public void addLike(Integer snippetId) {
        Integer userId = AppConfig.getFixedUserId(); // 고정 사용자 ID (2)

        // 스니펫이 존재하는지 확인
                Snippet snippet = snippetMapper.getSnippetById(snippetId);
        if (snippet == null) {
            throw new RuntimeException("스니펫을 찾을 수 없습니다: " + snippetId);
        }

        System.out.println("좋아요 추가 - userId: " + userId + ", snippetId: " + snippetId);
        System.out.println("스니펫 정보: " + snippet.getMemo() + " (소유자: " + snippet.getUserId() + ")");

        // INSERT IGNORE로 인해 중복 좋아요는 자동 방지됨
        likeMapper.addLike(userId, snippetId);

        // snippets 테이블은 업데이트하지 않음 (기존 데이터 보존)
        long actualCount = likeMapper.countLikes(snippetId);
        snippetMapper.updateLikeCount(snippetId, (int) actualCount);

        System.out.println("현재 실제 좋아요 수: " + actualCount);
    }

    // 좋아요 취소 (고정 사용자) - snippets 테이블 업데이트 없음
    public void removeLike(Integer snippetId) {
        Integer userId = AppConfig.getFixedUserId(); // 고정 사용자 ID (2)

        System.out.println("좋아요 취소 - userId: " + userId + ", snippetId: " + snippetId);
        likeMapper.removeLike(userId, snippetId);

        // 실시간 좋아요 수 조회 후 snippets 테이블 업데이트
        long actualCount = likeMapper.countLikes(snippetId);
        snippetMapper.updateLikeCount(snippetId, (int) actualCount);

        System.out.println("현재 실제 좋아요 수: " + actualCount);
    }

    // 특정 스니펫의 좋아요 수 반환 (항상 실시간 계산)
    public long getLikesCount(Integer snippetId) {
        // likes 테이블에서 실제 개수 계산 (snippets.like_count 무시)
        long count = likeMapper.countLikes(snippetId);
        System.out.println("스니펫 " + snippetId + "의 실제 좋아요 수: " + count);
        return count;
    }

    // 고정 사용자가 누른 좋아요 목록 반환
    public List<Like> getUserLikes() {
        Integer userId = AppConfig.getFixedUserId(); // 고정 사용자 ID (1)
        return likeMapper.getUserLikes(userId);
    }

    // 고정 사용자가 특정 스니펫에 좋아요를 눌렀는지 확인
    public boolean isLiked(Integer snippetId) {
        Integer userId = AppConfig.getFixedUserId(); // 고정 사용자 ID (1)
        return likeMapper.isLiked(userId, snippetId);
    }
}