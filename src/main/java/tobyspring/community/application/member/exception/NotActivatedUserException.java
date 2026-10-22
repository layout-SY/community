package tobyspring.community.application.member.exception;

public class NotActivatedUserException extends RuntimeException {
    public NotActivatedUserException() {
        super("활성화된 사용자만 컨텐츠를 이용하실 수 있습니다.");
    }
}
