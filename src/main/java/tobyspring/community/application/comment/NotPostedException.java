package tobyspring.community.application.comment;

public class NotPostedException extends RuntimeException {
    public NotPostedException(String target) {
        super(target + " 은(는) 아직 게시되지 않았습니다.");
    }
}
