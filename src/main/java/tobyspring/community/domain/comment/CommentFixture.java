package tobyspring.community.domain.comment;

public class CommentFixture {
    public static final CommentStatus COMMON_COMMENT_STATUS = CommentStatus.HIDDEN;
    public static final String COMMENT_CONTENT = "댓글 내용입니다.";

    public static CreateCommentRequest createCommentRequest(Long parentCommentId) {
        return createCommentRequest(parentCommentId, COMMENT_CONTENT);
    }

    public static CreateCommentRequest createCommentRequest(Long parentCommentId, String content) {
        return new CreateCommentRequest(content, parentCommentId);
    }

    public static ModifyCommentRequest modifyCommentRequest() {
        return modifyCommentRequest("수정하고자 하는 댓글입니다.", COMMON_COMMENT_STATUS);
    }


    public static ModifyCommentRequest modifyCommentRequest(String content, CommentStatus status) {
        return new ModifyCommentRequest(content, status);
    }
}
