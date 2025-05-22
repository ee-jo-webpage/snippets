package kr.or.kosa.snippets.tag.service;

import kr.or.kosa.snippets.tag.mapper.TagMapper;
import kr.or.kosa.snippets.tag.model.SnippetTag;
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
    public List<TagItem> getAllTags() {
        return tagMapper.selectAllTags();
    }

    //이름으로 태그 조회
    @Transactional
    public TagItem getTagByName(String name) {
        return tagMapper.selectTagByName(name);
    }

    //태그 입력
    @Transactional
    public TagItem createTag(String name) {

        TagItem tag = new TagItem(name);
        tagMapper.insertTag(tag);

        return tag;
    }

    //스니펫에 태그 추가
    @Transactional
    public boolean addTagToSnippet(Long snippetId, Long tagId) {
        //이미 연결되어 있는지 확인
        if (tagMapper.countSnippetTag(snippetId, tagId) == 0) {
            SnippetTag snippetTag = new SnippetTag(snippetId, tagId);
            tagMapper.insertSnippetTag(snippetTag);

            return true;
        }

        //이미 연결되어 있으면
        return false;
    }

    //특정 스니펫의 모든 태그 조회
    public List<TagItem> getTagsBySnippetId(Long snippetId) {
        return tagMapper.selectTagBySnippetId(snippetId);
    }

}
