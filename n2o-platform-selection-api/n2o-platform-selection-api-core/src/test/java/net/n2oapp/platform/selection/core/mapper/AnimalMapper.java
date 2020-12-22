package net.n2oapp.platform.selection.core.mapper;

import net.n2oapp.platform.selection.core.Mapper;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.Animal;
import net.n2oapp.platform.selection.core.model.Food;

import java.util.List;

public interface AnimalMapper<E extends Animal> extends Mapper<E> {

    @SelectionKey("name")
    void setName(E e);

    @SelectionKey("height")
    void setHeight(E e);

    @SelectionKey("mother")
    void setMother(E e);

    @SelectionKey("father")
    void setFather(E e);

    @SelectionKey("siblings")
    void setSiblings(E e);

    @SelectionKey("favoriteFood")
    List<? extends FoodMapper<? extends Food>> foodMappers();

    @SelectionKey("favoriteFood")
    void setFavoriteFood(E e, List<Food> favoriteFood);

}
