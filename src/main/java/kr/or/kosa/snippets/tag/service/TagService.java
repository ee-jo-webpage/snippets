package kr.or.kosa.snippets.tag.service;

import kr.or.kosa.snippets.tag.mapper.TagMapper;
import kr.or.kosa.snippets.tag.model.TagItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TagService {

    @Autowired
    TagMapper tagMapper;

    //모든 태그 조회
    public List<TagItem> selectAllTags() {
        return tagMapper.selectAllTags();
    }

}
