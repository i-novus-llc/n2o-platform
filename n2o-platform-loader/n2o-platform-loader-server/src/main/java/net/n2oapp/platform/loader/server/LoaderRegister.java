package net.n2oapp.platform.loader.server;

import java.util.ArrayList;
import java.util.List;

public class LoaderRegister {
    private List<LoaderInfo> loaders = new ArrayList<>();

    public <T> LoaderRegister add(ServerLoader<T> loader,
                                        String target,
                                        Class<T> type) {
        loaders.add(LoaderInfo.asObject(loader, target, type));
        return this;
    }

    public <T> LoaderRegister addIterable(ServerLoader<List<T>> loader,
                                  String target,
                                  Class<T> elementType) {
        loaders.add(LoaderInfo.asIterable(loader, target, elementType));
        return this;
    }

    public LoaderInfo find(String purpose) {
        return loaders.stream().filter(l -> l.getTarget().equals(purpose)).findFirst().orElseThrow();
    }
}
