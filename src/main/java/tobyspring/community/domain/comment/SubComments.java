package tobyspring.community.domain.comment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Getter
public class SubComments extends Comment{

    private Long commentId;
}
