package net.n2oapp.platform.seek;

import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.lang.NonNull;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;

public class CustomRepositoryFactory extends JpaRepositoryFactory {

    private final EntityManager entityManager;

    public CustomRepositoryFactory(EntityManager entityManager) {
        super(entityManager);
        this.entityManager = entityManager;
    }

    @Override
    protected @NonNull RepositoryComposition.RepositoryFragments getRepositoryFragments(@NonNull RepositoryMetadata metadata) {
        final RepositoryComposition.RepositoryFragments[] modifiedFragments = {RepositoryComposition.RepositoryFragments.empty()};
        RepositoryComposition.RepositoryFragments fragments = super.getRepositoryFragments(metadata);
        fragments.stream().forEach(
            f -> {
                if (f.getImplementation().isPresent()) {
                    boolean isSeekable = SeekableRepository.class.isAssignableFrom(metadata.getRepositoryInterface());
                    if (isSeekable) {
                        Object querydsl = f.getImplementation().get();
                        modifiedFragments[0] = modifiedFragments[0].append(
                            RepositoryFragment.implemented(
                                new SeekableRepositoryImpl<>(
                                    getEntityInformation(metadata.getDomainType()),
                                    entityManager,
                                    SimpleEntityPathResolver.INSTANCE,
                                    (CrudMethodMetadata) getField(querydsl, "metadata")
                                )
                            )
                        );
                    }
                } else {
                    modifiedFragments[0].append(f);
                }
            }
        );
        return modifiedFragments[0];
    }

    private Object getField(Object obj, String field) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }


}
