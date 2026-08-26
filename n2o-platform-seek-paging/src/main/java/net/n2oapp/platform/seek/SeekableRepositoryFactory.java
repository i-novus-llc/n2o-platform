package net.n2oapp.platform.seek;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.QuerydslJpaPredicateExecutor;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.lang.NonNull;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Данный класс сохраняет полную совместимость с {@link JpaRepositoryFactory},
 * при этом добавляя поддержку {@link SeekableRepository}.
 */
public class SeekableRepositoryFactory extends JpaRepositoryFactory {

    private final EntityManager entityManager;

    public SeekableRepositoryFactory(EntityManager entityManager) {
        super(entityManager);
        this.entityManager = entityManager;

    }

    @Override
    protected @NonNull RepositoryComposition.RepositoryFragments getRepositoryFragments(@NonNull RepositoryMetadata metadata) {
        RepositoryComposition.RepositoryFragments fragments = super.getRepositoryFragments(metadata);
        RepositoryComposition.RepositoryFragments modifiedFragment = RepositoryComposition.RepositoryFragments.empty();
        for (RepositoryFragment<?> fragment : fragments) {
            Optional<?> implementation = fragment.getImplementation();
            if (implementation.isEmpty() || implementation.get().getClass() != QuerydslJpaPredicateExecutor.class) {
                modifiedFragment = modifiedFragment.append(fragment);
            } else {
                JpaEntityInformation<?, Object> entityInformation = getEntityInformation(metadata.getDomainType());
                boolean isSeekable = SeekableRepository.class.isAssignableFrom(metadata.getRepositoryInterface());
                if (isSeekable) {
                    Object querydsl = implementation.get();
                    modifiedFragment = modifiedFragment.append(
                        RepositoryFragment.implemented(
                            getTargetRepositoryViaReflection(
                                SeekableRepositoryImpl.class,
                                entityInformation,
                                entityManager,
                                SimpleEntityPathResolver.INSTANCE,
                                getField(querydsl),
                                metadata.getRepositoryInterface()
                            )
                        )
                    );
                } else
                    modifiedFragment = modifiedFragment.append(fragment);
            }
        }
        return modifiedFragment;
    }

    // target и field -- жёстко заданные константы (Spring Data класс и его внутреннее поле), не приходят извне.
    @SuppressWarnings("java:S3011")
    private Object getField(Object obj) {
        try {
            Field f = QuerydslJpaPredicateExecutor.class.getDeclaredField("metadata");
            f.setAccessible(true);
            return f.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }


}