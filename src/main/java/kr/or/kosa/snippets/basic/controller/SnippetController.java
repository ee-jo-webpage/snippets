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

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") Long id, Model model) {
		// 1) type 조회
		SnippetTypeBasic type = snippetService.getSnippetTypeById(id);
		if (type == null) {
			return "error/404";
		}
		// 2) 스니펫 상세 조회
		Snippets snippet = snippetService.getSnippetsById(id, type);
		if (snippet == null) {
			return "error/404";
		}
		// 3) 폼에 바인딩
		model.addAttribute("snippet", snippet);
		return "basic/snippets/snippetEdit"; // 실제 수정 폼 뷰 이름
	}

	@PostMapping("/edit/{id}")
	public String updateSnippet(@PathVariable("id") Long id, @ModelAttribute Snippets snippet,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
		// 1) 기존 데이터를 먼저 가져오되, type을 추가로 조회
		System.out.println("content : " + snippet.getContent());
		SnippetTypeBasic type = snippetService.getSnippetTypeById(id); // type을 먼저 조회
		if (type == null) {
			return "error/404"; // type이 null이라면 404 처리
		}

		// 2) 기존 스니펫 데이터를 가져오기
		Snippets existingSnippet = snippetService.getSnippetsById(id, type);
		if (existingSnippet == null) {
			return "error/404"; // 스니펫이 존재하지 않으면 404 처리
		}

		// 3) 받은 수정 데이터를 기존 데이터에 덮어쓰기
		existingSnippet.setSourceUrl(snippet.getSourceUrl());
		existingSnippet.setType(snippet.getType());
		existingSnippet.setMemo(snippet.getMemo());
		existingSnippet.setVisibility(snippet.getVisibility());
		existingSnippet.setLikeCount(snippet.getLikeCount());
		existingSnippet.setContent(snippet.getContent());

		// 이미지 파일 처리
		if (imageFile != null && !imageFile.isEmpty()) {
			// 이미지 처리 로직 (예시로 이미지 URL을 세팅)
			existingSnippet.setImageUrl("새로운 이미지 URL");
		}

		// 4) 수정된 스니펫 데이터를 서비스로 전달
		snippetService.updateSnippets(existingSnippet);

		// 5) 수정 후 상세 페이지로 리다이렉트
		return "redirect:/snippets/" + id;
	}

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
