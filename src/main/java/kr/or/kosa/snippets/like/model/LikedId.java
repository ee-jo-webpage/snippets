package kr.or.kosa.snippets.like.model;

import java.io.Serializable;
// 직렬화 가능한 인터페이스
// JPA 복합 키 클래스는 반드시 Serializable을 구현해야 함

public class LikedId implements Serializable {
    // JPA 복합 키를 위한 클래스
    // Like 엔티티의 @IdClass로 사용됨

    private Long user_id;
    // 복합 키의 첫 번째 부분: 사용자 ID
    // Like 클래스의 user_id 필드와 이름과 타입이 정확히 일치해야 함

    private Long snippet_id;
    // 복합 키의 두 번째 부분: 스니펫 ID
    // Like 클래스의 snippet_id 필드와 이름과 타입이 정확히 일치해야 함

    //기본 생성자, equals(), hashCode() 필요
    public LikedId(){}
    // JPA 복합 키 클래스는 반드시 기본 생성자가 있어야 함
    // 기본 생성자가 없으면 JPA가 객체를 생성할 수 없음

    public LikedId(Long user_id, Long snippet_id) {
        this.user_id = user_id;
        this.snippet_id = snippet_id;
    }

    @Override
    public boolean equals(Object o) {
        // 두 복합 키가 같은지 비교하는 메서드
        // JPA에서 엔티티의 동일성 판단에 사용됨

        if(this == o) return true;
        // 같은 객체 참조라면 true 반환 (성능 최적화)

        if(o == null || getClass() != o.getClass()) return false;
        // null이거나 다른 클래스의 객체라면 false 반환

        LikedId likedId = (LikedId) o;
        // Object를 LikedId로 타입 캐스팅

        return user_id == likedId.user_id && snippet_id == likedId.snippet_id;
        // 두 복합 키의 모든 필드가 같은지 확인
    }

    @Override
    public int hashCode() {
        // 해시코드 생성 메서드
        // HashMap, HashSet 등에서 객체를 빠르게 찾기 위해 사용
        int result = Long.hashCode(user_id);
        result = 31 * result + Long.hashCode(snippet_id);
        return result;
        // 간단한 해시코드 계산 공식
        // 31은 소수로, 해시 충돌을 줄이는 일반적인 관례
    }
}