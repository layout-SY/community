package tobyspring.community.adapter;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tobyspring.community.application.AlreadyStateException;
import tobyspring.community.application.DuplicateEmailException;
import tobyspring.community.application.NotFoundException;

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

    private static @NonNull ProblemDetail setProblemDetailWithRuntimeException(RuntimeException ex, HttpStatus httpStatus) {
        String message = ex.getMessage();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, message);

        problemDetail.setProperty("exception", ex.getClass().getSimpleName());
        problemDetail.setProperty("time", LocalDateTime.now().toString());
        return problemDetail;
    }
}
