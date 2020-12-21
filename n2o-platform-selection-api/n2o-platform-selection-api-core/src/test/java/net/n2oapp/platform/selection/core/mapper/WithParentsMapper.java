package net.n2oapp.platform.selection.core.mapper;

import net.n2oapp.platform.selection.core.Mapper;
import net.n2oapp.platform.selection.core.SelectionKey;

public interface WithParentsMapper<E> extends Mapper<E> {

    @SelectionKey("mother")
    void setMother(E model, E mother);

    @SelectionKey("mother")
    Mapper<E> motherMapper();

    @SelectionKey("father")
    void setFather(E model, E father);

    @SelectionKey("father")
    Mapper<E> fatherMapper();

}
