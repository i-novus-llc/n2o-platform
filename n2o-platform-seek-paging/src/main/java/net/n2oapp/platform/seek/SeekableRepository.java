package net.n2oapp.platform.seek;

import com.querydsl.core.types.Predicate;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface SeekableRepository<T> extends QuerydslPredicateExecutor<T> {

    List<T> findAll(SeekableCriteria criteria);
    List<T> findAll(SeekableCriteria criteria, Predicate predicate);

}
