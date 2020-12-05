package net.n2oapp.platform.seek;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

import java.io.Serial;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QFood extends EntityPathBase<Food> {

    @Serial
    private static final long serialVersionUID = -1139156153L;

    public static final QFood food = new QFood("food");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public QFood(String variable) {
        super(Food.class, forVariable(variable));
    }

    public QFood(PathMetadata metadata) {
        super(Food.class, metadata);
    }

}
