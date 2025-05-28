package kr.or.kosa.snippets.snippetExt.component;

import kr.or.kosa.snippets.snippetExt.model.SnippetExtCreate;
import kr.or.kosa.snippets.snippetExt.service.SnippetExtService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SnippetBulkInsertScheduler {

    // private final SnippetQueue snippetQueue;
    // private final SnippetExtService snippetExtService;
    //
    // @Scheduled(fixedDelay = 60000) // 1분마다 실행
    // public void flushSnippets() {
    //     if (snippetQueue.isEmpty()) return;
    //
    //     List<SnippetExtCreate> batch = snippetQueue.drainAll();
    //     if (!batch.isEmpty()) {
    //         snippetExtService.saveAll(batch);
    //         System.out.println("[스케줄러] " + batch.size() + "건 bulk insert 완료");
    //     }
    // }
}
