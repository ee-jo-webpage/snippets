package kr.or.kosa.snippets.snippetExt.component;

import kr.or.kosa.snippets.snippetExt.model.SnippetExtCreate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class SnippetQueue {
    private final Queue<SnippetExtCreate> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(SnippetExtCreate snippet) {
        queue.add(snippet);
    }

    public List<SnippetExtCreate> drainAll() {
        List<SnippetExtCreate> list = new ArrayList<>();
        while (!queue.isEmpty()) {
            list.add(queue.poll());
        }
        return list;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
