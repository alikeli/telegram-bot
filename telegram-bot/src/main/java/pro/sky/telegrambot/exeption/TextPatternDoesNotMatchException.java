package pro.sky.telegrambot.exeption;

public class TextPatternDoesNotMatchException extends RuntimeException{
    public TextPatternDoesNotMatchException() {
    }

    public TextPatternDoesNotMatchException(String message) {
        super(message);
    }

    public TextPatternDoesNotMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public TextPatternDoesNotMatchException(Throwable cause) {
        super(cause);
    }

    public TextPatternDoesNotMatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
