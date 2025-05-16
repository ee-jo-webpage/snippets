package kr.or.kosa.snippets.service;

import kr.or.kosa.snippets.mapper.TagMapper;
import kr.or.kosa.snippets.model.Snippet;
import kr.or.kosa.snippets.model.SnippetTag;
import kr.or.kosa.snippets.model.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TagServiceImpl {

    @Autowired
    TagMapper tagMapper;

    //모든 태그 조회
    public List<Tag> selectAllTags() {
        return tagMapper.selectAllTags();
    }


    //태그 입력
    @Transactional
    public Tag createTag(String name) {

        Tag tag = new Tag(name);
        tagMapper.insertTag(tag);

        return tag;
    }

    //이름으로 태그 조회
    @Transactional
    public Tag selectTagByName(String name) {
        return tagMapper.selectTagByName(name);
    }

    //태그Id로 태그 조회
    @Transactional
    public Tag selectTagByTagId(Long tagId) {
        return tagMapper.selectTagById(tagId);
    }


    //태그 수정
    public Tag updateTag(Long tagId, String newName) {
        Tag tag = tagMapper.selectTagById(tagId);
        if (tag != null) {
            tag.setName(newName);
            tagMapper.updateTag(tag);
        }

        return tag;
    }

    //태그 삭제 - 연결된 관계도 삭제
    @Transactional
    public boolean deleteTag(Long tagId) {
        Tag tag = tagMapper.selectTagById(tagId);

        if (tag != null) {
            //먼저 snippet_tag에서 관련 레코드 삭제
            tagMapper.deleteSnippetTagsByTagId(tagId);
            //그 다음 태그 삭제
            tagMapper.deleteTag(tagId);
            return true;
        }

        return false;
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

    //스니펫에서 태그 제거
    @Transactional
    public boolean removeTagFromSnippet(Long snippetId, Long tagId) {
        return tagMapper.deleteSnippetTag(snippetId, tagId) > 0;
    }

    //특정 스니펫의 모든 태그 조회
    public List<Tag> selectTagsBySnippetId(Long snippetId) {
        return tagMapper.selectTagBySnippetId(snippetId);
    }

    //특정 태그를 가진 모든 스니펫 ID 조회
    public List<Snippet> selectSnippetIdsByTagId(Long tagId) {
        return tagMapper.selectSnippetIdByTagId(tagId);
    }

    //특정 태그를 가진 모든 스니펫 조회
    public List<Snippet> selectSnippetsByTagId(Long tagId) {
        return tagMapper.selectSnippetsByTagId(tagId);
    }

    //태그 이름 중복 체크
    public boolean isTagNameExists(String name) {
        return tagMapper.selectTagByName(name) != null;
    }

}
