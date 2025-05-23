package kr.or.kosa.snippets.user.model;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    private Long userId;

    private String email;


    private String nickname;


    private String password;


    private boolean enabled;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private String reason;

    private String role;

    private String loginType;
}