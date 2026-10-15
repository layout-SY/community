package tobyspring.community.domain.post;

import tobyspring.community.application.InvalidParameterException;

import java.util.regex.Pattern;

record Tag(String keyword) {
    private static final Pattern TAG_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]+$");

    Tag {
        String trimmed = keyword.trim();

        if(!TAG_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidParameterException("태그명은 한글, 영문, 숫자만 사용할 수 있습니다 : " + keyword);
        }

        if(trimmed.length() > 15) {
            throw new InvalidParameterException("태그명은 15자리를 넘길 수 없습니다 : " + keyword);
        }

        if(trimmed.contains(" ")) {
            throw new InvalidParameterException("태그명은 공백이 있을 수 없습니다 : " + keyword);
        }

        if(trimmed.isEmpty()) {
            throw new InvalidParameterException("태그명은 비어 있을 수 없습니다 : " + keyword);
        }

        keyword = trimmed.toLowerCase();
    }

    public String displayTag() {
        return "#" + keyword;
    }
}
