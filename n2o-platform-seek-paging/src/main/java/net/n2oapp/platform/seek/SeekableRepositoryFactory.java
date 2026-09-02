package net.n2oapp.platform.seek;

import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.lang.NonNull;

import jakarta.persistence.EntityManager;

/**
 * Данный класс сохраняет полную совместимость с {@link JpaRepositoryFactory},
 * при этом добавляя поддержку {@link SeekableRepository}.
 */
public class SeekableRepositoryFactory extends JpaRepositoryFactory {

    public SeekableRepositoryFactory(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected @NonNull RepositoryComposition.RepositoryFragments getRepositoryFragments(
        @NonNull RepositoryMetadata metadata,
        @NonNull EntityManager entityManager,
        @NonNull EntityPathResolver resolver,
        @NonNull CrudMethodMetadata crudMethodMetadata
    ) {
        if (!SeekableRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
            return super.getRepositoryFragments(metadata, entityManager, resolver, crudMethodMetadata);
        }
        JpaEntityInformation<?, Object> entityInformation = getEntityInformation(metadata.getDomainType());
        return RepositoryComposition.RepositoryFragments.just(
            new SeekableRepositoryImpl<>(
                entityInformation,
                entityManager,
                resolver,
                crudMethodMetadata,
                metadata.getRepositoryInterface()
            )
        );
    }
}
