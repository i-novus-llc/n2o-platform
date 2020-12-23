package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.api.Mapper;
import net.n2oapp.platform.selection.core.mapper.DogMapper;
import net.n2oapp.platform.selection.core.mapper.TailLengthDogFeatureMapper;
import net.n2oapp.platform.selection.core.model.Dog;
import net.n2oapp.platform.selection.core.model.DogFeature;
import net.n2oapp.platform.selection.core.model.TailLengthDogFeature;

import java.util.List;
import java.util.stream.Collectors;

public class DogEntity extends AnimalEntity<Dog, DogFeature> implements DogMapper {

    private final List<? extends DogFeature> features;

    public DogEntity(String name, double height, String mother, String father, List<String> siblings, List<? extends DogFeature> features) {
        super(name, height, mother, father, siblings);
        this.features = features;
    }

    @Override
    public Dog create() {
        return new Dog();
    }

    @Override
    public List<? extends Mapper<? extends DogFeature>> featuresMapper() {
        return features.stream().map(feature -> new TailLengthDogFeatureMapperImpl((TailLengthDogFeature) feature)).collect(Collectors.toList());
    }

    private static class TailLengthDogFeatureMapperImpl implements TailLengthDogFeatureMapper {

        private final TailLengthDogFeature feature;

        private TailLengthDogFeatureMapperImpl(TailLengthDogFeature feature) {
            this.feature = feature;
        }

        @Override
        public TailLengthDogFeature create() {
            return new TailLengthDogFeature();
        }

        @Override
        public void setLength(TailLengthDogFeature feature) {
            feature.setLength(this.feature.length());
        }

    }

}
