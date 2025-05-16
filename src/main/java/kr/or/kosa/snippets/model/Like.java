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