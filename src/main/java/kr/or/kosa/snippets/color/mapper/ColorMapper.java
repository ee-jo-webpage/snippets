package kr.or.kosa.snippets.color.mapper;

import kr.or.kosa.snippets.color.model.Color;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ColorMapper {

    /**
     * 전체 색상 조회
     */
    List<Color> getAllColors();

    /**
     * 기본 색상 조회 (사용자 Null)
     */
    List<Color> getDefaultColors();

    /**
     * 사용자별 색상 조회
     */
    List<Color> getColorsByUserId(Long userId); //조건조회  (id primary key)

    /**
     * 사용자별 사용 가능한 색상 조회 (기본 색상 + 사용자가 설정한 색상)
     */
    List<Color> getAllAvailableColorsByUserId(Long userId); //조건조회  (id primary key)

    /**
     * 색상 id로 색상 조회
     */
    Color getColorByColorId(Long colorId);

    /**
     * 색상 입력
     */
    void insert(Color color);

    /**
     * 색상 수정
     *
     * @return
     */
    int update(Color color);

    /**
     * 색상 삭제
     */
    void delete(Long colorId, Long userId);

    /**
     * 사용자가 특정 색상을 소유하는지 확인 (hex_code 기준)
     */
    boolean isUserColorOwner(String hexCode, Long userId);

    /**
     * 색상이 기본 색상인지 확인 (hex_code 기준)
     */
    boolean isDefaultColor(String hexCode);


//    /**
//     * 특정 사용자가 해당 hex_code의 색상을 갖고 있는지 확인
//     */
//    Color getUserColorByHexCode(String hexCode, Long userId);
//
//    /**
//     * 기본 색상 중에서 해당 hex_code의 색상을 조회
//     */
//    Color getDefaultColorByHexCode(String hexCode);
}
