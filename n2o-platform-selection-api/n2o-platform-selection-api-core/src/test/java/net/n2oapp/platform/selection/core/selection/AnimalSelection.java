package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.Selection;
import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.Animal;
import net.n2oapp.platform.selection.core.model.AnimalFeature;
import net.n2oapp.platform.selection.core.model.Food;

public abstract class AnimalSelection<E extends Animal<F>, F extends AnimalFeature> implements Selection<E> {

    @SelectionKey("name")
    SelectionEnum selectName;

    @SelectionKey("height")
    SelectionEnum selectHeight;

    @SelectionKey("mother")
    SelectionEnum selectMother;

    @SelectionKey("father")
    SelectionEnum selectFather;

    @SelectionKey("siblings")
    SelectionEnum selectSiblings;

    @SelectionKey("favoriteFood")
    SelectionEnum selectFavoriteFood;

    @SelectionKey("favoriteFood")
    Selection<? extends Food> favoriteFoodSelection;

    @SelectionKey("features")
    SelectionEnum selectFeatures;

    @SelectionKey("features")
    Selection<? extends F> featuresSelection;

    public SelectionEnum getSelectName() {
        return selectName;
    }

    public void setSelectName(SelectionEnum selectName) {
        this.selectName = selectName;
    }

    public SelectionEnum getSelectHeight() {
        return selectHeight;
    }

    public void setSelectHeight(SelectionEnum selectHeight) {
        this.selectHeight = selectHeight;
    }

    public SelectionEnum getSelectMother() {
        return selectMother;
    }

    public void setSelectMother(SelectionEnum selectMother) {
        this.selectMother = selectMother;
    }

    public SelectionEnum getSelectFather() {
        return selectFather;
    }

    public void setSelectFather(SelectionEnum selectFather) {
        this.selectFather = selectFather;
    }

    public SelectionEnum getSelectSiblings() {
        return selectSiblings;
    }

    public void setSelectSiblings(SelectionEnum selectSiblings) {
        this.selectSiblings = selectSiblings;
    }

    public SelectionEnum getSelectFavoriteFood() {
        return selectFavoriteFood;
    }

    public void setSelectFavoriteFood(SelectionEnum selectFavoriteFood) {
        this.selectFavoriteFood = selectFavoriteFood;
    }

    public Selection<? extends Food> getFavoriteFoodSelection() {
        return favoriteFoodSelection;
    }

    public void setFavoriteFoodSelection(Selection<? extends Food> favoriteFoodSelection) {
        this.favoriteFoodSelection = favoriteFoodSelection;
    }

    public SelectionEnum getSelectFeatures() {
        return selectFeatures;
    }

    public void setSelectFeatures(SelectionEnum selectFeatures) {
        this.selectFeatures = selectFeatures;
    }

    public Selection<? extends F> getFeaturesSelection() {
        return featuresSelection;
    }

    public void setFeaturesSelection(Selection<? extends F> featuresSelection) {
        this.featuresSelection = featuresSelection;
    }

}
