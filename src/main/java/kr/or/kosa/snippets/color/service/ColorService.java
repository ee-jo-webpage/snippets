package kr.or.kosa.snippets.color.service;

import kr.or.kosa.snippets.color.controller.ColorException;
import kr.or.kosa.snippets.color.mapper.ColorMapper;
import kr.or.kosa.snippets.color.model.Color;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ColorService {

    @Autowired
    private ColorMapper colorMapper;


    
    public List<Color> getAllColors() {

        List<Color> colorList =  null;
        try{
            colorList = colorMapper.getAllColors();
        }catch(Exception e){
            e.printStackTrace();
        }
        return colorList;
    }

    
    public Color getColorById(Long colorId) {
        Color color = null;
        try{
            color = colorMapper.getColorByColorId(colorId);

        }catch(Exception e) {
            e.printStackTrace();

        }
        return color;
    }

    
    public List<Color> getColorsByUserId(Long userId) {

        List<Color> colorList = colorMapper.getColorsByUserId(userId);

        return colorList;
    }

    public List<Color> getAllAvailableColorsByUserId(Long userId) {
        try {
            log.info("=== 색상 조회 시작 - 사용자 ID: {} ===", userId);

            // 기본 색상만 조회해보기
            List<Color> defaultColors = colorMapper.getDefaultColors();
            log.info("기본 색상 개수: {}", defaultColors.size());
            for (Color color : defaultColors) {
                log.info("기본 색상: ID={}, name={}, hexCode={}, userId={}",
                        color.getColorId(), color.getName(), color.getHexCode(), color.getUserId());
            }

            // 사용자 커스텀 색상만 조회해보기
            List<Color> userColors = colorMapper.getColorsByUserId(userId);
            log.info("사용자 커스텀 색상 개수: {}", userColors.size());
            for (Color color : userColors) {
                log.info("커스텀 색상: ID={}, name={}, hexCode={}, userId={}",
                        color.getColorId(), color.getName(), color.getHexCode(), color.getUserId());
            }

            // 통합 조회
            List<Color> colorList = colorMapper.getAllAvailableColorsByUserId(userId);
            log.info("통합 조회된 색상 개수: {}", colorList.size());
            for (Color color : colorList) {
                log.info("통합 색상: ID={}, name={}, hexCode={}, userId={}",
                        color.getColorId(), color.getName(), color.getHexCode(), color.getUserId());
            }

            log.info("=== 색상 조회 완료 ===");
            return colorList;
        } catch (Exception e) {
            log.error("사용자 {}의 사용 가능한 색상 조회 중 오류 발생", userId, e);
            throw e;
        }
    }
    
    public void insertColor(Color color) {
        if (color.getName() == null || color.getName().trim().isEmpty()) {
            throw new ColorException("색상 이름을 입력하세요");
        }

        if (color.getHexCode() == null || color.getHexCode().trim().isEmpty()) {
            throw new ColorException("색상 코드가 필요합니다");
        }

        // 헥스 코드 format 검증
        if (!color.getHexCode().matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("올바른 색상 코드 형식이 아닙니다. (예: #FF0000)");
        }
        try {
            // 색상 이름 trim 처리
            color.setName(color.getName().trim());
            color.setHexCode(color.getHexCode().trim().toUpperCase());

            // 중복 색상 확인
            // 같은 사용자가 같은 hexCode 색상을 가지고 있는지 확인
            if (colorMapper.isUserColorOwner(color.getHexCode(), color.getUserId())) {
                throw new IllegalArgumentException("이미 등록된 색상입니다.");
            }

            // 색상 삽입
            colorMapper.insert(color);

            log.info("새 색상 등록 완료 - 사용자 ID: {}, 색상명: {}, 색상코드: {}",
                    color.getUserId(), color.getName(), color.getHexCode());
        } catch (Exception e) {
            log.error("색상 등록 중 오류 발생", e);
            throw e;
        }
    }

    
    public void updateColor(Color color) {
        if (color.getColorId() == null) {
            throw new ColorException("색상 ID가 필요합니다");
        }

        if (color.getName() == null || color.getName().trim().isEmpty()) {
            throw new ColorException("색상 이름을 입력하세요");
        }

        if (color.getHexCode() == null || color.getHexCode().trim().isEmpty()) {
            throw new ColorException("색상 코드가 필요합니다");
        }

        // 헥스 코드 format 검증
        if (!color.getHexCode().matches("^#[0-9A-Fa-f]{6}$")) {
            throw new ColorException("올바른 색상 코드 형식이 아닙니다. (예: #FF0000)");  // IllegalArgumentException → ColorException
        }

        try {
            // 기존 색상 조회
            Color existingColor = colorMapper.getColorByColorId(color.getColorId());
            if (existingColor == null) {
                throw new ColorException("색상을 찾을 수 없습니다. ID: " + color.getColorId());  // 명확한 메시지 추가
            }

            // 권한 확인 (사용자 자신의 색상만 수정 가능)
            if (!existingColor.getUserId().equals(color.getUserId())) {
                throw new ColorException("해당 색상에 접근할 권한이 없습니다");  // 명확한 메시지 추가
            }

            // 색상 이름과 헥스 코드 trim 처리
            color.setName(color.getName().trim());
            color.setHexCode(color.getHexCode().trim().toUpperCase());

            // 중복 색상 확인 (같은 사용자가 같은 hexCode를 가진 다른 색상이 있는지)
            // 단, 자기 자신은 제외
            boolean isDuplicate = colorMapper.isUserColorOwner(color.getHexCode(), color.getUserId())
                    && !existingColor.getHexCode().equals(color.getHexCode());

            if (isDuplicate) {
                throw new ColorException("이미 등록된 색상입니다: " + color.getHexCode());  // DuplicateColorException → ColorException
            }

            // 색상 업데이트
            int updatedRows = colorMapper.update(color);

            if (updatedRows == 0) {
                throw new ColorException("색상 업데이트에 실패했습니다");
            }

            log.info("색상 수정 완료 - 색상 ID: {}, 사용자 ID: {}, 색상명: {}, 색상코드: {}",
                    color.getColorId(), color.getUserId(), color.getName(), color.getHexCode());

        } catch (ColorException e) {
            // ColorException은 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("색상 수정 중 예상치 못한 오류 발생", e);
            throw new ColorException("색상 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    
    public void deleteColor(Long colorId, Long userId) {
        colorMapper.delete(colorId, userId);
    }

}
