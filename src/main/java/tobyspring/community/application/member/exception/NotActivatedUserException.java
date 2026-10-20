package tobyspring.community.application.member.exception;

public class NotActivatedUserException extends RuntimeException {
    public NotActivatedUserException() {
        super("활성화된 사용자만 게시글을 작성할 수 있습니다.");
    }
}
