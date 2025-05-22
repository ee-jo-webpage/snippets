package kr.or.kosa.snippets.snippetExt.exception;

public class DuplicateSnippetException extends RuntimeException {
    public DuplicateSnippetException(String message) {
        super(message);
    }
}

