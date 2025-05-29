package kr.or.kosa.snippets.like.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LikeUserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * userId로 nickname 조회
     * @param userId 사용자 ID
     * @return nickname (없으면 "알 수 없음" 반환)
     */
    public String getNicknameByUserId(long userId) {
        try {
            String sql = "SELECT nickname FROM users WHERE user_id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (Exception e) {
            System.err.println("nickname 조회 실패 - userId: " + userId + ", 오류: " + e.getMessage());
            return "알 수 없음";
        }
    }

    /**
     * 여러 userId들의 nickname을 한 번에 조회 (성능 최적화)
     * @param userIds 사용자 ID 목록
     * @return userId -> nickname 매핑
     */
    public Map<Long, String> getNicknamesByUserIds(List<Long> userIds) {
        Map<Long, String> nicknameMap = new HashMap<>();

        if (userIds == null || userIds.isEmpty()) {
            return nicknameMap;
        }

        try {
            // IN 절을 위한 플레이스홀더 생성
            String placeholders = userIds.stream()
                    .map(id -> "?")
                    .collect(Collectors.joining(","));

            String sql = "SELECT user_id, nickname FROM users WHERE user_id IN (" + placeholders + ")";

            jdbcTemplate.query(sql, userIds.toArray(), (rs) -> {
                nicknameMap.put(rs.getLong("user_id"), rs.getString("nickname"));
            });

        } catch (Exception e) {
            System.err.println("nickname 일괄 조회 실패: " + e.getMessage());
        }

        // 조회되지 않은 userId들은 기본값 설정
        for (long userId : userIds) {
            if (!nicknameMap.containsKey(userId)) {
                nicknameMap.put(userId, "알 수 없음");
            }
        }

        return nicknameMap;
    }
}