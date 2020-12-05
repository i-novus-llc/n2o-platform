package net.n2oapp.platform.seek;

import com.querydsl.core.types.Predicate;
import net.n2oapp.platform.jaxrs.seek.SeekableCriteria;
import net.n2oapp.platform.jaxrs.seek.SeekedPage;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface SeekableRepository<T> extends QuerydslPredicateExecutor<T> {

    SeekedPage<T> findAll(SeekableCriteria criteria);
    SeekedPage<T> findAll(SeekableCriteria criteria, Predicate predicate);

}
