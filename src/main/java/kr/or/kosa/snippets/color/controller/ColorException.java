package kr.or.kosa.snippets.color.controller;

public class ColorException extends RuntimeException{
    public ColorException() {
        super();
    }

    public ColorException(String message) {
        super(message);
    }

    public ColorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ColorException(Throwable cause) {
        super(cause);
    }
}
