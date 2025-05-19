package kr.or.kosa.snippets.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="likes")
@IdClass(LikedId.class)  // 복합 키를 처리할 클래스를 지정
// 이 엔티티는 두 개의 컬럼(user_id, snippet_id)을 조합한 복합 키를 사용
// 복합 키의 구조는 LikedId 클래스에 정의됨
public class Like {

    @Id
    @Column(name = "user_id")
    private int user_id;  // DB INT 타입에 맞게 int 유지

    @Id
    @Column(name = "snippet_id")
    private int snippet_id;  // DB INT 타입에 맞게 int 유지

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;  // 생성 시간
}