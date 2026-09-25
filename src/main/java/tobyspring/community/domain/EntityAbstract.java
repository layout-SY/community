package tobyspring.community.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
public class EntityAbstract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter(onMethod_ = {@Nullable})
    private Long id;
}
