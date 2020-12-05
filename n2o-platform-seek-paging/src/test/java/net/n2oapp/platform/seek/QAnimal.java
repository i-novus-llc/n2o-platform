package net.n2oapp.platform.seek;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

import java.io.Serial;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QAnimal extends EntityPathBase<Animal> {

    @Serial
    private static final long serialVersionUID = 668059368L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAnimal animalEntity = new QAnimal("animalEntity");

    public final DatePath<java.time.LocalDate> birthDate = createDate("birthDate", java.time.LocalDate.class);

    public final QFood favoriteFood;

    public final NumberPath<java.math.BigDecimal> height = createNumber("height", java.math.BigDecimal.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final QAnimal parent;

    public final NumberPath<java.math.BigDecimal> weight = createNumber("weight", java.math.BigDecimal.class);

    public QAnimal(String variable) {
        this(Animal.class, forVariable(variable), INITS);
    }

    public QAnimal(PathMetadata metadata, PathInits inits) {
        this(Animal.class, metadata, inits);
    }

    public QAnimal(Class<? extends Animal> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.favoriteFood = inits.isInitialized("favoriteFood") ? new QFood(forProperty("favoriteFood")) : null;
        this.parent = inits.isInitialized("parent") ? new QAnimal(forProperty("parent"), inits.get("parent")) : null;
    }

}
