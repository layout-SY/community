package tobyspring.community.adapter;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tobyspring.community.adapter.security.AuthenticateException;
import tobyspring.community.application.member.exception.*;
import tobyspring.community.application.post.exception.NotAuthorException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {DuplicateEmailException.class})
    protected ProblemDetail duplicateException(DuplicateEmailException ex) {

        return setProblemDetailWithRuntimeException(ex, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(value = {AlreadyStateException.class})
    protected ProblemDetail alreadyStateException(AlreadyStateException ex) {

        return setProblemDetailWithRuntimeException(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {NotFoundException.class})
    protected ProblemDetail notFoundException(NotFoundException ex) {
        return setProblemDetailWithRuntimeException(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {AuthenticateException.class})
    protected ProblemDetail authenticateException(AuthenticateException ex) {
        return setProblemDetailWithRuntimeException(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {DeactivateMemberException.class})
    protected ProblemDetail DeactivateMemberException(DeactivateMemberException ex) {
        return setProblemDetailWithRuntimeException(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {NullPointerException.class})
    protected ProblemDetail nullPointerException(NullPointerException ex) {
        return setProblemDetailWithRuntimeException(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {NotActivatedUserException.class})
    protected ProblemDetail notActivatedUserException(NotActivatedUserException ex) {
        return setProblemDetailWithRuntimeException(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {NotAuthorException.class})
    protected ProblemDetail notAuthorException(NotAuthorException ex) {
        return setProblemDetailWithRuntimeException(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {InvalidParameterException.class})
    protected ProblemDetail invalidParameterException(InvalidParameterException ex) {
        return setProblemDetailWithRuntimeException(ex, HttpStatus.BAD_REQUEST);
    }

    private static @NonNull ProblemDetail setProblemDetailWithRuntimeException(RuntimeException ex, HttpStatus httpStatus) {
        String message = ex.getMessage();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, message);

        problemDetail.setProperty("exception", ex.getClass().getSimpleName());
        problemDetail.setProperty("time", LocalDateTime.now().toString());
        return problemDetail;
    }
}
