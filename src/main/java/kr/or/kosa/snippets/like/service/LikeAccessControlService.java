package kr.or.kosa.snippets.like.service;

import kr.or.kosa.snippets.like.mapper.LikeSnippetMapper;
import kr.or.kosa.snippets.like.model.Snippet;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 좋아요 관련 접근 제어를 담당하는 서비스
 */
@Service
public class LikeAccessControlService {

    @Autowired
    private LikeSnippetMapper likeSnippetMapper;

    /**
     * 사용자가 특정 스니펫에 접근할 수 있는지 확인
     * @param snippetId 스니펫 ID
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 접근 가능 여부
     */
    public boolean canAccessSnippet(Long snippetId, CustomUserDetails userDetails) {
        if (snippetId == null || snippetId <= 0) {
            return false;
        }

        Snippet snippet = likeSnippetMapper.getSnippetById(snippetId);
        if (snippet == null) {
            return false;
        }

        // 공개 스니펫인 경우 누구나 접근 가능
        if (snippet.isVisibility()) {
            return true;
        }

        // 비공개 스니펫인 경우 로그인한 소유자만 접근 가능
        if (userDetails != null && userDetails.getUserId().equals((long) snippet.getUserId())) {
            return true;
        }

        return false;
    }

    /**
     * 사용자가 특정 스니펫의 소유자인지 확인
     * @param snippetId 스니펫 ID
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 소유자 여부
     */
    public boolean isSnippetOwner(Long snippetId, CustomUserDetails userDetails) {
        if (snippetId == null || snippetId <= 0 || userDetails == null) {
            return false;
        }

        Snippet snippet = likeSnippetMapper.getSnippetById(snippetId);
        if (snippet == null) {
            return false;
        }

        return userDetails.getUserId().equals((long) snippet.getUserId());
    }

    /**
     * 스니펫이 공개되어 있는지 확인
     * @param snippetId 스니펫 ID
     * @return 공개 여부
     */
    public boolean isPublicSnippet(Long snippetId) {
        if (snippetId == null || snippetId <= 0) {
            return false;
        }

        Snippet snippet = likeSnippetMapper.getSnippetById(snippetId);
        if (snippet == null) {
            return false;
        }

        return snippet.isVisibility();
    }

    /**
     * 접근 거부 시 로그 기록
     * @param snippetId 스니펫 ID
     * @param userDetails 사용자 정보
     * @param reason 거부 사유
     */
    public void logAccessDenied(Long snippetId, CustomUserDetails userDetails, String reason) {
        String userInfo = userDetails != null ?
                "User ID: " + userDetails.getUserId() + ", Nickname: " + userDetails.getNickname() :
                "Anonymous";

        System.out.println("접근 거부 - 스니펫 ID: " + snippetId + ", 사용자: " + userInfo + ", 사유: " + reason);
    }

    /**
     * 접근 성공 시 로그 기록
     * @param snippetId 스니펫 ID
     * @param userDetails 사용자 정보
     * @param action 수행한 작업
     */
    public void logAccessGranted(Long snippetId, CustomUserDetails userDetails, String action) {
        String userInfo = userDetails != null ?
                "User ID: " + userDetails.getUserId() + ", Nickname: " + userDetails.getNickname() :
                "Anonymous";

        System.out.println("접근 허용 - 스니펫 ID: " + snippetId + ", 사용자: " + userInfo + ", 작업: " + action);
    }
}