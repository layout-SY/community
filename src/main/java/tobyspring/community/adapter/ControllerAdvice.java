package tobyspring.community.adapter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tobyspring.community.application.DuplicateEmailException;

import java.time.LocalDateTime;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {DuplicateEmailException.class})
    protected ProblemDetail duplicateException(DuplicateEmailException ex) {
        String message = ex.getMessage();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);

        problemDetail.setProperty("exception", ex.getClass().getSimpleName());
        problemDetail.setProperty("time", LocalDateTime.now().toString());

        return problemDetail;
    }
}
