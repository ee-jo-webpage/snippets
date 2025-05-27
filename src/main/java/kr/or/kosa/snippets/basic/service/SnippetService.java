package kr.or.kosa.snippets.basic.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kosa.snippets.basic.mapper.SnippetsMapper;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.model.SnippetTypeBasic;

@Service
@Transactional
public class SnippetService {

    private final SnippetsMapper snippetsMapper;

    public SnippetService(SnippetsMapper snippetsMapper) {
        this.snippetsMapper = snippetsMapper;
    }

    public List<Snippets> getAllSnippets() {
        return snippetsMapper.getAllSnippets();
    }
    
    //userId로 상세조회
    public List<Snippets> getUserSnippets(Long userId) {
    	return snippetsMapper.getUserSnippets(userId);
    }
    

    public Snippets getSnippetsById(Long snippetId, SnippetTypeBasic type) {
        Snippets snippet = snippetsMapper.getSnippetsById(snippetId, type);
        return snippet;
    }

    public SnippetTypeBasic getSnippetTypeById(Long snippetId) {
        return snippetsMapper.getSnippetTypeById(snippetId);
    }

    public void insertSnippets(Snippets snippets) {
        snippetsMapper.insertSnippetBasic(snippets);
        if (snippets.getType() == SnippetTypeBasic.CODE) {
            snippetsMapper.insertSnippetCode(snippets);
        } else if (snippets.getType() == SnippetTypeBasic.TEXT) {
            snippetsMapper.insertSnippetText(snippets);
        } else if (snippets.getType() == SnippetTypeBasic.IMG) {
            snippetsMapper.insertSnippetImage(snippets);
        }
    }

    public void deleteSnippets(Long snippetId) {
        // 1) type을 먼저 조회하고
        SnippetTypeBasic type = snippetsMapper.getSnippetTypeById(snippetId);
        if (type == null) {
            throw new RuntimeException("해당 스니펫이 존재하지 않습니다. snippetId=" + snippetId);
        }
        
        // 2) type에 맞춰 자식 테이블 삭제
        if (type == SnippetTypeBasic.CODE) {
            snippetsMapper.deleteSnippetCodeBySnippetId(snippetId);
        } else if (type == SnippetTypeBasic.TEXT) {
            snippetsMapper.deleteSnippetTextBySnippetId(snippetId);
        } else if (type == SnippetTypeBasic.IMG) {
            snippetsMapper.deleteSnippetImageBySnippetId(snippetId);
        }
        
        // 3) 마지막으로 부모 테이블 삭제
        snippetsMapper.deleteSnippets(snippetId);
    }

    public void updateSnippets(Snippets snippet) {
        // 먼저 Snippets 테이블 업데이트
        snippetsMapper.updateSnippetBasic(snippet);

        // 자식 테이블 내용도 업데이트
        if (snippet.getType() == SnippetTypeBasic.CODE) {
            // 코드 스니펫은 코드와 언어도 업데이트해야 함
            System.out.println("Updating CODE snippet...");
            snippetsMapper.updateSnippetCode(snippet);
        } else if (snippet.getType() == SnippetTypeBasic.TEXT) {
            // 텍스트 스니펫은 텍스트 내용만 업데이트
            System.out.println("Updating TEXT snippet...");
            snippetsMapper.updateSnippetText(snippet);
        } else if (snippet.getType() == SnippetTypeBasic.IMG) {
            // 이미지 스니펫은 이미지 URL과 Alt 텍스트도 업데이트
            System.out.println("Updating IMG snippet...");
            snippetsMapper.updateSnippetImage(snippet);
        }
    }
    
    public void updateSnippetCode(Snippets snippet) {
        // 코드 스니펫의 content와 language 업데이트
        System.out.println("Updating CODE content and language...");
        System.out.println("Content: " + snippet.getContent());
        System.out.println("Language: " + snippet.getLanguage());
        snippetsMapper.updateSnippetCode(snippet);  // 여기서 updateSnippetCode 메소드 호출
    }

    public void updateSnippetText(Snippets snippet) {
        // 텍스트 스니펫의 content 업데이트
        System.out.println("Updating TEXT content...");
        snippetsMapper.updateSnippetText(snippet);  // 여기서 updateSnippetText 메소드 호출
    }

    public void updateSnippetImage(Snippets snippet) {
        // 이미지 스니펫의 imageUrl과 altText 업데이트
        System.out.println("Updating IMG imageUrl and altText...");
        snippetsMapper.updateSnippetImage(snippet);  // 여기서 updateSnippetImage 메소드 호출
    }
    
  

 
}