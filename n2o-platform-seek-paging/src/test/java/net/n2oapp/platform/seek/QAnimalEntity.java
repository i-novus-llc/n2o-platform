package net.n2oapp.platform.seek;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

import java.io.Serial;
import java.time.LocalDate;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QAnimalEntity extends EntityPathBase<AnimalEntity> {

    @Serial
    private static final long serialVersionUID = -83826579L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAnimalEntity animalEntity = new QAnimalEntity(AnimalEntity.class, forVariable("AnimalEntity"), INITS);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath email = createString("name");

    public final DatePath<LocalDate> birthDate = createDate("birthDate", LocalDate.class);

    public QAnimalEntity(Class<? extends AnimalEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
    }

}
