package kr.or.kosa.snippets.model;

import java.io.Serializable;

public class LikedId implements Serializable {
    private int user_id;   // Like.java와 동일하게 int 타입
    private int snippet_id; // Like.java와 동일하게 int 타입

    //기본 생성자, equals(), hashCode() 필요
    public LikedId(){}

    public LikedId(int user_id, int snippet_id) {
        this.user_id = user_id;
        this.snippet_id = snippet_id;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        LikedId likedId = (LikedId) o;
        return user_id == likedId.user_id && snippet_id == likedId.snippet_id;
    }

    @Override
    public int hashCode() {
        return 31 * (user_id + snippet_id);
    }
}