package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.core.entity.CatEntity;
import net.n2oapp.platform.selection.core.entity.DogEntity;
import net.n2oapp.platform.selection.core.model.Cat;
import net.n2oapp.platform.selection.core.model.ColorEnum;
import net.n2oapp.platform.selection.core.model.EyeColorCatFeature;
import net.n2oapp.platform.selection.core.model.TailLengthDogFeature;
import net.n2oapp.platform.selection.core.selection.CatSelection;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class SelectorTest {

    @Test
    public void testResolve() {
        TailLengthDogFeature dogFeature = new TailLengthDogFeature();
        dogFeature.setLength(10);
        DogEntity dog = new DogEntity("Balto", 10.0, "Helena", "George", List.of("Spike"), Collections.singletonList(dogFeature));
        EyeColorCatFeature feature = new EyeColorCatFeature();
        feature.setColor(ColorEnum.RED);
        CatEntity cat = new CatEntity("Fibby", 20.0, "Matilda", "Felidae", Collections.emptyList(), List.of(feature), dog);
        CatSelection<Cat> catSelection = new CatSelection<>();
        catSelection.setSelectHeight(SelectionEnum.T);
        Selector.resolve(cat, catSelection);
    }

}
