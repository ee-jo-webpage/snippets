package kr.or.kosa.snippets.like.service;

import kr.or.kosa.snippets.like.mapper.LikeMapper;
import kr.or.kosa.snippets.like.mapper.LikeSnippetMapper;
import kr.or.kosa.snippets.like.model.Like;
import kr.or.kosa.snippets.like.model.Snippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeService {

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private LikeSnippetMapper likeSnippetMapper;

    // 좋아요 추가 (실제 사용자 ID 사용)
    public void addLike(Long snippetId, Long userId) {
        // 스니펫이 존재하는지 확인
        Snippet snippet = likeSnippetMapper.getSnippetById(snippetId);
        if (snippet == null) {
            throw new RuntimeException("스니펫을 찾을 수 없습니다: " + snippetId);
        }

        System.out.println("좋아요 추가 - userId: " + userId + ", snippetId: " + snippetId);
        System.out.println("스니펫 정보: " + snippet.getMemo() + " (소유자: " + snippet.getUserId() + ")");

        // INSERT IGNORE로 인해 중복 좋아요는 자동 방지됨
        likeMapper.addLike(userId, snippetId);

        // snippets 테이블의 like_count 업데이트
        long actualCount = likeMapper.countLikes(snippetId);
        likeSnippetMapper.updateLikeCount(snippetId, (int) actualCount);

        System.out.println("현재 실제 좋아요 수: " + actualCount);
    }

    // 좋아요 취소 (실제 사용자 ID 사용)
    public void removeLike(Long snippetId, Long userId) {
        System.out.println("좋아요 취소 - userId: " + userId + ", snippetId: " + snippetId);
        likeMapper.removeLike(userId, snippetId);

        // 실시간 좋아요 수 조회 후 snippets 테이블 업데이트
        long actualCount = likeMapper.countLikes(snippetId);
        likeSnippetMapper.updateLikeCount(snippetId, (int) actualCount);

        System.out.println("현재 실제 좋아요 수: " + actualCount);
    }

    // 특정 스니펫의 좋아요 수 반환 (항상 실시간 계산)
    public long getLikesCount(Long snippetId) {
        // likes 테이블에서 실제 개수 계산 (snippets.like_count 무시)
        long count = likeMapper.countLikes(snippetId);
        System.out.println("스니펫 " + snippetId + "의 실제 좋아요 수: " + count);
        return count;
    }

    // 특정 사용자가 누른 좋아요 목록 반환
    public List<Like> getUserLikes(Long userId) {
        return likeMapper.getUserLikes(userId);
    }

    // 특정 사용자가 특정 스니펫에 좋아요를 눌렀는지 확인
    public boolean isLiked(Long snippetId, Long userId) {
        return likeMapper.isLiked(userId, snippetId);
    }
}