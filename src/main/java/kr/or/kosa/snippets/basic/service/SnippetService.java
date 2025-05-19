package kr.or.kosa.snippets.basic.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.or.kosa.snippets.basic.mapper.SnippetsMapper;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.model.Snippettype;

@Service
@Transactional
public class SnippetService {

	private SnippetsMapper snippetsMapper;
	
	@Autowired
	public SnippetService(SnippetsMapper snippetsMapper) {
		this.snippetsMapper = snippetsMapper;
	}
	
	public List<Snippets> getAllSnippets() {
        return snippetsMapper.getAllSnippets();
    }
	
	public Snippets getSnippetsById(int snippetid, Snippettype type) {
	    return snippetsMapper.getSnippetsById(snippetid, type);
	}
	
	public Snippettype getSnippetTypeById(int snippetid) {
	    return snippetsMapper.getSnippetTypeById(snippetid);
	}
	
	//추가하기
	public void insertSnippets(Snippets snippets) {
	    // 기본 insert 후 snippets.snippetid에 생성된 PK값이 들어있음
	    snippetsMapper.insertSnippetBasic(snippets);

	    if (snippets.getType().equals(Snippettype.code)) {
	        snippetsMapper.insertSnippetCode(snippets);
	    } else if (snippets.getType().equals(Snippettype.text)) {
	        snippetsMapper.insertSnippetText(snippets);
	    } else if (snippets.getType().equals(Snippettype.img)) {
	        snippetsMapper.insertSnippetImage(snippets);
	    }
	}
	
	//삭제하기
	@Transactional
	public void deleteSnippets(int snippetid) {
	    // 1. 먼저 스니펫 타입 조회
	    Snippets snippet = snippetsMapper.getSnippetsById(snippetid, null); // type 파라미터가 null이어도 조회 가능하게 수정 필요

	    if (snippet == null) {
	        throw new RuntimeException("해당 스니펫이 존재하지 않습니다. snippetid=" + snippetid);
	    }

	    Snippettype type = snippet.getType();

	    // 2. 타입에 맞는 자식 테이블 한 곳만 삭제
	    if (type == Snippettype.code) {
	        snippetsMapper.deleteSnippetCodeBySnippetId(snippetid);
	    } else if (type == Snippettype.text) {
	        snippetsMapper.deleteSnippetTextBySnippetId(snippetid);
	    } else if (type == Snippettype.img) {
	        snippetsMapper.deleteSnippetImageBySnippetId(snippetid);
	    } else {
	        throw new RuntimeException("알 수 없는 스니펫 타입입니다: " + type);
	    }

	    // 3. 부모 테이블 스니펫 삭제
	    snippetsMapper.deleteSnippets(snippetid);
	}

	
	//수정하기
	@Transactional
	public void updateSnippets(Snippets snippet) {
	    if (snippet.getType().equals(Snippettype.code)) {
	        snippetsMapper.updateSnippetCode(snippet);
	    } else if (snippet.getType().equals(Snippettype.text)) {
	        snippetsMapper.updateSnippetText(snippet);
	    } else if (snippet.getType().equals(Snippettype.img)) {
	        snippetsMapper.updateSnippetImage(snippet);
	    }
	    snippetsMapper.updateSnippetBasic(snippet);
	}


	
}
