package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.Selection;
import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.Animal;
import net.n2oapp.platform.selection.core.model.Food;

public interface AnimalSelection<E extends Animal> extends Selection<E> {

    @SelectionKey("name")
    SelectionEnum selectName();

    @SelectionKey("height")
    SelectionEnum selectHeight();

    @SelectionKey("mother")
    SelectionEnum selectMother();

    @SelectionKey("father")
    SelectionEnum selectFather();

    @SelectionKey("siblings")
    SelectionEnum selectSiblings();

    @SelectionKey("favoriteFood")
    SelectionEnum selectFavoriteFood();

    @SelectionKey("favoriteFood")
    Selection<? extends Food> favoriteFoodSelection();

}
