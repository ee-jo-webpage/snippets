package kr.or.kosa.snippets.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 이내로 입력해주세요.")
    private String nickname;

    @Pattern(
            regexp = "^(|(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()_+]).{8,20})$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다."
    )
    private String password; // 비워도 허용되므로 @Pattern으로 처리
}