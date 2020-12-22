package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.core.Mapper;
import net.n2oapp.platform.selection.core.mapper.CatMapper;
import net.n2oapp.platform.selection.core.mapper.EyeColorCatFeatureMapper;
import net.n2oapp.platform.selection.core.model.Cat;
import net.n2oapp.platform.selection.core.model.CatFeature;
import net.n2oapp.platform.selection.core.model.Dog;
import net.n2oapp.platform.selection.core.model.EyeColorCatFeature;

import java.util.List;
import java.util.stream.Collectors;

public class CatEntity extends AnimalEntity<Cat, CatFeature<?>> implements CatMapper<Cat> {

    private final List<CatFeature<?>> features;

    private final DogEntity enemy;

    public CatEntity(String name, double height, String mother, String father, List<String> siblings, List<CatFeature<?>> features, DogEntity enemy) {
        super(name, height, mother, father, siblings);
        this.features = features;
        this.enemy = enemy;
    }

    @Override
    public Cat create() {
        return new Cat();
    }

    @Override
    public Mapper<Dog> dogMapper() {
        return enemy;
    }

    @Override
    public void setDog(Cat cat, Dog enemy) {
        cat.setEnemy(enemy);
    }

    @Override
    public List<? extends Mapper<? extends CatFeature<?>>> featuresMapper() {
        return features.stream().map(feature -> new EyeColorCatFeatureMapperImpl((EyeColorCatFeature) feature)).collect(Collectors.toList());
    }

    @Override
    public void setFeatures(Cat model, List<CatFeature<?>> features) {
        model.setFeatures(features);
    }

    private static class EyeColorCatFeatureMapperImpl implements EyeColorCatFeatureMapper {

        private final EyeColorCatFeature feature;

        private EyeColorCatFeatureMapperImpl(EyeColorCatFeature feature) {
            this.feature = feature;
        }

        @Override
        public EyeColorCatFeature create() {
            return new EyeColorCatFeature();
        }

        @Override
        public void setColor(EyeColorCatFeature feature) {
            feature.setColor(this.feature.color());
        }

    }

}
