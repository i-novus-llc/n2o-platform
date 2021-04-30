package net.n2oapp.platform.seek;

import com.querydsl.core.types.Predicate;
import net.n2oapp.platform.jaxrs.seek.Seekable;
import net.n2oapp.platform.jaxrs.seek.SeekedPage;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SeekableRepository<T> extends QuerydslPredicateExecutor<T> {

    SeekedPage<T> findAll(Seekable seekable);
    SeekedPage<T> findAll(Seekable seekable, Predicate predicate);

}
