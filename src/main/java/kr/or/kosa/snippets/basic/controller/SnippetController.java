package kr.or.kosa.snippets.basic.controller;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import kr.or.kosa.snippets.basic.model.SnippetTypeBasic;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.service.SnippetService;

@Controller
@RequestMapping("/snippets")
public class SnippetController {

	private final SnippetService snippetService;

	public SnippetController(SnippetService snippetService) {
		this.snippetService = snippetService;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(SnippetTypeBasic.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) {
				setValue(SnippetTypeBasic.valueOf(text));
			}
		});
	}

	@GetMapping
	public String getAllSnippets(Model model) {
		model.addAttribute("snippets", snippetService.getAllSnippets());
		return "basic/snippets/snippetsList";
	}

	@GetMapping("/{id}")
	public String getById(@PathVariable("id") Long id, Model model) {
		// 1) 먼저 DB에서 type 조회
		SnippetTypeBasic type = snippetService.getSnippetTypeById(id);
		// 2) type이 null이면 404 페이지로
		if (type == null) {
			return "error/404";
		}
		// 3) 안전하게 상세조회
		Snippets snippet = snippetService.getSnippetsById(id, type);
		if (snippet == null) {
			return "error/404";
		}
		model.addAttribute("snippet", snippet);
		return "basic/snippets/snippetDetail";
	}

	@GetMapping("/new")
	public String showAddForm(Model model) {
		model.addAttribute("snippet", new Snippets());
		return "basic/snippets/snippetAdd";
	}

	@PostMapping("/new")
	public String addSnippet(@ModelAttribute Snippets snippet, @RequestParam("type") String type,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
		snippet.setType(SnippetTypeBasic.valueOf(type));
		snippetService.insertSnippets(snippet);
		return "redirect:/snippets";
	}

	
//------------------------------------------------------------------------------	
	@GetMapping("/edit/{snippetId}")
    public String editSnippet(@PathVariable Long snippetId, Model model) {
        // 1. 스니펫의 type을 먼저 조회
        SnippetTypeBasic type = snippetService.getSnippetTypeById(snippetId);
        
        if (type == null) {
            return "error/404"; // 타입이 존재하지 않으면 404 페이지로
        }

        // 2. id와 type을 사용하여 해당 스니펫을 가져옵니다.
        Snippets snippet = snippetService.getSnippetsById(snippetId, type);
        if (snippet == null) {
            return "error/404"; // 스니펫이 존재하지 않으면 404 페이지로
        }

        // 3. 스니펫 데이터를 모델에 추가
        model.addAttribute("snippet", snippet);

        // 4. type에 맞는 수정 페이지로 리디렉션
        if (SnippetTypeBasic.CODE.equals(type)) {
            return "snippets/snippetEditCode"; // 코드 수정 페이지
        } else if (SnippetTypeBasic.TEXT.equals(type)) {
            return "snippets/snippetEditText"; // 텍스트 수정 페이지
        } else if (SnippetTypeBasic.IMG.equals(type)) {
            return "snippets/snippetEditImage"; // 이미지 수정 페이지
        }

        return "error/404"; // type이 맞지 않으면 404 페이지로 리디렉션
    }

	@PostMapping("/edit/{snippetId}")
	public String updateSnippet(@PathVariable Long snippetId, @ModelAttribute Snippets snippet) {
	    // 1. type을 먼저 조회
	    SnippetTypeBasic type = snippetService.getSnippetTypeById(snippetId);  // type 조회
	    if (type == null) {
	        return "error/404"; // type이 null일 경우 404 페이지로 리다이렉트
	    }

	    // 2. 기존 스니펫을 가져오기 (type을 전달)
	    Snippets existingSnippet = snippetService.getSnippetsById(snippetId, type); // 여기서 type을 함께 전달

	    if (existingSnippet == null) {
	        return "error/404"; // 스니펫이 없을 경우 404 페이지로 리다이렉트
	    }

	    // 3. 기존 데이터에 수정된 내용 덮어쓰기
	    existingSnippet.setSourceUrl(snippet.getSourceUrl());
	    existingSnippet.setMemo(snippet.getMemo());
	    existingSnippet.setVisibility(snippet.getVisibility());
	    existingSnippet.setLikeCount(snippet.getLikeCount());

	    // 4. 타입에 맞게 content 설정
	    if ("TEXT".equals(type.name())) {
	        existingSnippet.setContent(snippet.getContent());  // 텍스트 타입이면 textContent에 할당
	    } else if ("CODE".equals(type.name())) {
	        existingSnippet.setContent(snippet.getContent());  // 코드 타입이면 codeContent에 할당
	    } else if ("IMG".equals(type.name())) {
	        existingSnippet.setImageUrl(snippet.getContent());  // 이미지 타입이면 imageContent에 할당
	    }

	    // 이미지 파일 처리 (필요한 경우)
	    // 예: if (imageFile != null && !imageFile.isEmpty()) { existingSnippet.setImageUrl("새로운 이미지 URL"); }

	    // 5. 수정된 스니펫을 서비스로 전달하여 업데이트
	    snippetService.updateSnippets(existingSnippet);

	    // 6. 수정 후 상세 페이지로 리디렉션
	    return "redirect:/snippets/" + snippetId;
	}
//--------------------------------------------------------------------------------------------

	@PostMapping("/delete/{id}")
	public String deleteSnippet(@PathVariable("id") Long id) {
		snippetService.deleteSnippets(id);
		return "redirect:/snippets"; // 삭제 후 목록으로만 리다이렉트
	}

	// S3 업로드 유틸 메서드 (필요 시 활성화)
	// private String uploadToS3(MultipartFile file) throws IOException {
	// String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
	// ObjectMetadata metadata = new ObjectMetadata();
	// metadata.setContentType(file.getContentType());
	// metadata.setContentLength(file.getSize());
	// amazonS3Client.putObject(bucket, key, file.getInputStream(), metadata);
	// return amazonS3Client.getUrl(bucket, key).toString();
	// }
}
