package tobyspring.community.domain.shared;

import static java.util.Objects.requireNonNull;

public class RequestValidator {

    public static void lengthValidator(String target, String targetName, Long limitLength) {
        if ((target != null) && target.length() > limitLength)
            throw new IllegalArgumentException(targetName + "은 " + limitLength + "을(를) 넘길 수 없습니다.");
    }

    public static void nullValidator(String target, String targetName) {
        requireNonNull(target, targetName + "이(가) 없습니다.");
    }
}
