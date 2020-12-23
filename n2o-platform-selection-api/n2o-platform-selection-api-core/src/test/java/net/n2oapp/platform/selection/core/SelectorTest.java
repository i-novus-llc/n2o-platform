package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.core.entity.CatEntity;
import net.n2oapp.platform.selection.core.entity.DogEntity;
import net.n2oapp.platform.selection.core.entity.FishEntity;
import net.n2oapp.platform.selection.core.model.*;
import net.n2oapp.platform.selection.core.selection.CatSelection;
import net.n2oapp.platform.selection.core.selection.DogSelection;
import net.n2oapp.platform.selection.core.selection.EyeColorCatFeatureSelection;
import net.n2oapp.platform.selection.core.selection.FishSelection;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SelectorTest {

    @Test
    public void testResolve() {
        TailLengthDogFeature dogFeature = new TailLengthDogFeature();
        dogFeature.setLength(10);
        DogEntity dog = new DogEntity("Balto", 10.0, "Helena", "George", List.of("Spike"), Collections.singletonList(dogFeature));
        EyeColorCatFeature feature = new EyeColorCatFeature();
        feature.setColor(ColorEnum.RED);
        CatEntity cat = new CatEntity("Fibby", 20.0, "Matilda", "Felidae", Collections.emptyList(), List.of(feature), dog);
        cat.setFavoriteFoods(List.of(new FishEntity("Pollock", ColorEnum.RED, List.of(Ocean.ARCTIC))));
        CatSelection catSelection = new CatSelection() {};
        catSelection.setSelectHeight(SelectionEnum.T);
        catSelection.setSelectEnemy(SelectionEnum.T);
        catSelection.setEnemySelection(new DogSelection() {});
        catSelection.setSelectFeatures(SelectionEnum.T);
        EyeColorCatFeatureSelection eyeColorCatFeatureSelection = new EyeColorCatFeatureSelection();
        eyeColorCatFeatureSelection.setSelectColor(SelectionEnum.T);
        catSelection.setFeaturesSelection(eyeColorCatFeatureSelection);
        catSelection.setSelectFavoriteFood(SelectionEnum.T);
        catSelection.setFavoriteFoodSelection(new FishSelection());
        FishSelection fishSelection = new FishSelection();
        fishSelection.setSelectColor(SelectionEnum.T);
        fishSelection.setSelectHabitats(SelectionEnum.T);
        catSelection.setFavoriteFoodSelection(fishSelection);
        Cat resolved = Selector.resolve(cat, catSelection);
        assertNotNull(resolved.getHeight());
        assertNotNull(resolved.getEnemy());
        assertNotNull(resolved.getFeatures());
        assertNull(resolved.getEnemy().getName());
        assertNull(resolved.getMother());
        assertNotNull(resolved.getFavoriteFood());
    }

}
