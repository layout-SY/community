package tobyspring.community.application.member.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("이미 존재하는 이메일 입니다 : " + email);
    }
}
