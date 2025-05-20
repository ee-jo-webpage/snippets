package kr.or.kosa.snippets.user.mapper;

import kr.or.kosa.snippets.user.model.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    boolean existsByEmail(@Param("email") String email);
    boolean existsByEmailAndEnabledFalse(String email);
    boolean existsByEmailAndDisabledAndDeleted(String email);


    void insertUser(Users user);

    Users findByEmail(String email);

    void updateUser(Users user);
}