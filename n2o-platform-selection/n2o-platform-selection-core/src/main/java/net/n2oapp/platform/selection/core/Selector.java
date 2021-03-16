package net.n2oapp.platform.selection.core;

import com.google.common.base.Preconditions;
import net.n2oapp.platform.selection.api.*;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.data.util.Streamable;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static net.n2oapp.platform.selection.api.SelectionPropagationEnum.*;

/**
 * Основная логика по выборочному отображению.
 * Все методы, которые не принимают {@link Joiner}, подвержены проблеме {@code N+1}
 */
public final class Selector {

    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends Collection>, Supplier<? extends Collection<?>>> SUPPORTED_COLLECTIONS = Map.of(
            List.class, ArrayList::new,
            Set.class, HashSet::new
    );

    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends Collection>, Supplier<? extends Collection<?>>> SUPPORTED_EMPTY_COLLECTIONS = Map.of(
            List.class, Collections::emptyList,
            Set.class, Collections::emptySet
    );

    private static final ResolvableType FETCHER_RAW = ResolvableType.forRawClass(Fetcher.class);
    private static final ResolvableType SELECTION_RAW = ResolvableType.forRawClass(Selection.class);
    private static final ResolvableType JOINER_RAW = ResolvableType.forRawClass(Joiner.class);
    private static final ResolvableType COLLECTION_RAW = ResolvableType.forRawClass(Collection.class);
    private static final ResolvableType MAP_RAW = ResolvableType.forRawClass(Map.class);

    private static final String VIOLATION = "Violation in %s";
    private static final String UNMATCHED_ID = "Unmatched id %s for selection key %s";

    private boolean failOnAllPropagation;
    private boolean failOnNestedPropagation;

    private Selector() {
    }

    public Selector failOnAllPropagation(boolean failOnAllPropagation) {
        this.failOnAllPropagation = failOnAllPropagation;
        return this;
    }

    public Selector failOnNestedPropagation(boolean failOnNestedPropagation) {
        this.failOnNestedPropagation = failOnNestedPropagation;
        return this;
    }

    /**
     * Кешированные дескрипторы fetcher-ов
     */
    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Fetcher>, FetcherDescriptor> FETCHER_DESCRIPTORS = new ConcurrentHashMap<>();

    /**
     * Кешированные дескрипторы выборок
     */
    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Selection>, SelectionDescriptor> SELECTION_DESCRIPTORS = new ConcurrentHashMap<>();

    /**
     * Кешированные дескрипторы joiner-ов
     */
    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Joiner>, JoinerDescriptor> JOINER_DESCRIPTORS = new ConcurrentHashMap<>();

    private static final ConcurrentMap<SelectionDescriptor, List<FetcherDescriptor>> CHECKED_FETCHERS_AGAINST_SELECTION = new ConcurrentHashMap<>();
    private static final ConcurrentMap<SelectionDescriptor, List<JoinerDescriptor>> CHECKED_JOINERS_AGAINST_SELECTION = new ConcurrentHashMap<>();
    private static final ConcurrentMap<JoinerDescriptor, List<FetcherDescriptor>> CHECKED_FETCHERS_AGAINST_JOINER = new ConcurrentHashMap<>();

    /**
     * @param joiner Группировщик запросов.
     * @param fetchers {@link Streamable} fetcher-ов
     * @param selection Выборка клиента
     * @return {@link Streamable} элементов типа {@code <T>}, чьи элементы выборочно отображены в соответствии с {@code selection}
     */
    @SuppressWarnings("unchecked")
    public <T, ID, E, F extends Fetcher<T>, S extends Streamable<T>> S resolveStreamable(
            @NonNull Joiner<? extends T, ? extends ID, E, ? super F> joiner,
            Streamable<? extends F> fetchers,
            Selection<? extends T> selection
    ) {
        Iterable<T> res;
        if (empty(selection) || empty(fetchers)) {
            res = null;
        } else {
            Map<ID, F> batch = this.<T, ID, E, F>makeBatch(joiner, fetchers);
            Map<ID, T> resolvedBatch = this.<T, ID, E, F>resolveTopLevelBatch(joiner, batch, selection);
            res = fetchers.map(fetcher -> resolvedBatch.get(Preconditions.checkNotNull(joiner.getId(joiner.getUnderlyingEntity(fetcher)))));
        }
        return (S) res;
    }

    /**
     * Данный метод подвержен проблеме {@code N+1} и не должен использоваться в общем случае.
     * Вместо него используйте метод {@link Selector#resolveStreamable(Joiner, Streamable, Selection)}
     * @param fetchers Streamable fetcher-ов
     * @param selection Выборка
     * @param <T> Тип DTO
     * @return {@link Streamable} DTO, чьи поля выборочно заполнены в соответствии с {@code selection}
     */
    @SuppressWarnings("unchecked")
    public <T, S extends Streamable<T>> S resolveStreamable(Streamable<? extends Fetcher<? extends T>> fetchers, Selection<? extends T> selection) {
        Iterable<T> res;
        if (empty(selection) || empty(fetchers)) {
            res = null;
        } else {
            res = fetchers.map(fetcher -> resolve(fetcher, selection));
        }
        return (S) res;
    }

    /**
     * Данный метод подвержен проблеме {@code N+1}. Вместо него используйте {@link Selector#resolve(Joiner, Fetcher, net.n2oapp.platform.selection.api.Selection)}
     * @param fetcher Fetcher
     * @param selection Выборка
     * @return Выборочное отображение E в соответствии с {@code selection}
     */
    public <T> T resolve(Fetcher<? extends T> fetcher, Selection<? extends T> selection) {
        if (fetcher == null || empty(selection))
            return null;
        final SelectionPropagationEnum propagation = selection.propagation() == null ? NORMAL : selection.propagation();
        if (propagation == NESTED || propagation == ALL) {
            checkPropagationSupported(propagation);
            return fetchAll(fetcher, selection, propagation, emptySet());
        } else {
            SelectionDescriptor selectionDescriptor = getSelectionDescriptor(selection);
            FetcherDescriptor fetcherDescriptor = getFetcherDescriptor(fetcher);
            checkFetcherAgainstSelection(fetcherDescriptor, selectionDescriptor);
            T model = Preconditions.checkNotNull(fetcher.create());
            for (Map.Entry<String, SelectionDescriptor.SelectionAccessor> selectionAccessorEntry : selectionDescriptor.accessors.entrySet()) {
                SelectionDescriptor.SelectionAccessor selectionAccessor = selectionAccessorEntry.getValue();
                FetcherDescriptor.FetcherAccessor fetcherAccessor = fetcherDescriptor.accessors.get(selectionAccessorEntry.getKey());
                if (fetcherAccessor.isNested()) {
                    Selection<?> nestedSelection = (Selection<?>) invoke(selectionAccessor.nestedSelectionAccessor, selection);
                    if (empty(nestedSelection))
                        continue;
                    Object nested = invoke(fetcherAccessor.nestedFetcherAccessor, fetcher);
                    nestedSelection(model, nested, fetcher, fetcherAccessor, nestedSelection, this::resolve);
                } else {
                    SelectionEnum select = (SelectionEnum) invoke(selectionAccessor.selectionEnumAccessor, selection);
                    if (select == null || !select.asBoolean())
                        continue;
                    invoke(fetcherAccessor.fetchMethod, fetcher, model);
                }
            }
            return model;
        }
    }

    private void checkPropagationSupported(SelectionPropagationEnum propagation) {
        Preconditions.checkArgument(propagation != ALL || !failOnAllPropagation, "ALL propagation not supported");
        Preconditions.checkArgument(propagation != NESTED || !failOnNestedPropagation, "NESTED propagation not supported");
    }

    public <T, ID, E, F extends Fetcher<T>> T resolve(
        @NonNull Joiner<? extends T, ? extends ID, E, ? super F> joiner,
        F fetcher,
        Selection<? extends T> selection
    ) {
        Preconditions.checkNotNull(joiner);
        if (fetcher == null || empty(selection))
            return null;
        ID id = Preconditions.checkNotNull(joiner.getId(joiner.getUnderlyingEntity(fetcher)));
        return this.<T, ID, E, F>resolveTopLevelBatch(joiner, Map.of(id, fetcher), selection).get(id);
    }

    private <T, ID, E, F extends Fetcher<T>> Map<ID, F> makeBatch(
            Joiner<? extends T, ? extends ID, E, ? super F> joiner,
            Iterable<? extends F> fetch
    ) {
        Map<ID, F> batch = new HashMap<>();
        for (F fetcher : fetch) {
            E entity = joiner.getUnderlyingEntity(fetcher);
            ID id = Preconditions.checkNotNull(joiner.getId(entity));
            batch.put(id, fetcher);
        }
        return batch;
    }

    private boolean empty(Iterable<?> iterable) {
        return iterable == null || !iterable.iterator().hasNext();
    }

    @SuppressWarnings("unchecked")
    private <T, ID, E, F extends Fetcher<T>> Map<ID, T> resolveTopLevelBatch(
            @NonNull Joiner<? extends T, ? extends ID, E, ? super F> joiner,
            @NonNull Map<ID, F> batch,
            Selection<? extends T> selection
    ) {
        if (CollectionUtils.isEmpty(batch) || empty(selection))
            return emptyMap();
        SelectionPropagationEnum propagation = selection.propagation();
        if (propagation == null)
            propagation = NORMAL;
        if (propagation == ALL || propagation == NESTED) {
            checkPropagationSupported(propagation);
            return this.<T, ID, F, E>fetchAll(batch, joiner, selection, propagation);
        } else {
            JoinerDescriptor joinerDescriptor = getJoinerDescriptor(joiner);
            SelectionDescriptor selectionDescriptor = getSelectionDescriptor(selection);
            checkJoinerAgainstSelection(joinerDescriptor, selectionDescriptor);
            checkBatchOfFetchers(batch, selectionDescriptor, joinerDescriptor);
            List<E> entities = new ArrayList<>(batch.size());
            Map<ID, T> result = new HashMap<>(batch.size());
            this.<T, ID, E, F>prepareForGrouping(joiner, batch, entities, result);
            for (Map.Entry<String, SelectionDescriptor.SelectionAccessor> selectionAccessorEntry : selectionDescriptor.accessors.entrySet()) {
                SelectionDescriptor.SelectionAccessor selectionAccessor = selectionAccessorEntry.getValue();
                String key = selectionAccessorEntry.getKey();
                JoinerDescriptor.JoinerAccessor joinerAccessor = joinerDescriptor.accessors.get(key);
                if (!selectionAccessor.isNested()) {
                    SelectionEnum select = (SelectionEnum) invoke(selectionAccessor.selectionEnumAccessor, selection);
                    if (select == null || !select.asBoolean())
                        continue;
                    for (Map.Entry<ID, F> entry : batch.entrySet()) {
                        T model = result.get(entry.getKey());
                        F fetcher = entry.getValue();
                        FetcherDescriptor fetcherDescriptor = getFetcherDescriptor(fetcher);
                        FetcherDescriptor.FetcherAccessor fetcherAccessor = fetcherDescriptor.accessors.get(key);
                        invoke(fetcherAccessor.fetchMethod, fetcher, model);
                    }
                } else {
                    Selection<?> nestedSelection = (Selection<?>) invoke(selectionAccessor.nestedSelectionAccessor, selection);
                    if (empty(nestedSelection))
                        continue;
                    if (joinerAccessor == null) {
                        for (Map.Entry<ID, F> entry : batch.entrySet()) {
                            T model = result.get(entry.getKey());
                            F fetcher = entry.getValue();
                            FetcherDescriptor fetcherDescriptor = getFetcherDescriptor(fetcher);
                            FetcherDescriptor.FetcherAccessor fetcherAccessor = fetcherDescriptor.accessors.get(key);
                            Object nested = invoke(fetcherAccessor.nestedFetcherAccessor, fetcher);
                            nestedSelection(model, nested, fetcher, fetcherAccessor, nestedSelection, this::resolve);
                        }
                    } else {
                        Map<ID, Object> nestedBatch = (Map<ID, Object>) invoke(joinerAccessor.joinMethod, joiner, entities);
                        Joiner<?, ?, ?, ?> nestedJoiner = joinerAccessor.nestedJoinerAccessor != null ? (Joiner<?, ?, ?, ?>) invoke(joinerAccessor.nestedJoinerAccessor, joiner) : null;
                        if (nestedJoiner == null) {
                            for (Map.Entry<ID, Object> entry : nestedBatch.entrySet()) {
                                T model = result.get(entry.getKey());
                                F fetcher = batch.get(entry.getKey());
                                Preconditions.checkState(model != null, UNMATCHED_ID, entry.getKey(), key);
                                nestedSelection(model, Preconditions.checkNotNull(entry.getValue()), fetcher, getFetcherDescriptor(fetcher).accessors.get(key), nestedSelection, this::resolve);
                            }
                        } else {
                            Map<ID, Object> resolvedNestedBatch = resolveNestedBatch(nestedJoiner, nestedBatch, nestedSelection, this::resolveTopLevelBatch);
                            applyResolvedNestedBatch(batch, result, key, resolvedNestedBatch);
                        }
                    }
                }
            }
            return result;
        }
    }

    private <T, ID, E, F extends Fetcher<T>> void prepareForGrouping(Joiner<? extends T, ? extends ID, E, ? super F> joiner, Map<ID, F> batch, List<E> entities, Map<ID, T> result) {
        for (Map.Entry<ID, F> entry : batch.entrySet()) {
            F fetcher = entry.getValue();
            result.put(entry.getKey(), Preconditions.checkNotNull(fetcher.create()));
            entities.add(Preconditions.checkNotNull(joiner.getUnderlyingEntity(fetcher)));
        }
    }

    private <T, ID, F extends Fetcher<T>> void applyResolvedNestedBatch(Map<ID, F> srcBatch, Map<ID, T> result, String key, Map<ID, Object> resolvedNestedBatch) {
        for (Map.Entry<ID, Object> entry : resolvedNestedBatch.entrySet()) {
            T model = result.get(entry.getKey());
            Preconditions.checkState(model != null, UNMATCHED_ID, entry.getKey(), key);
            F fetcher = srcBatch.get(entry.getKey());
            FetcherDescriptor fetcherDescriptor = getFetcherDescriptor(fetcher);
            FetcherDescriptor.FetcherAccessor fetcherAccessor = fetcherDescriptor.accessors.get(key);
            invoke(fetcherAccessor.fetchMethod, fetcher, model, entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private <T, ID, F extends Fetcher<T>, E> Map<ID, T> fetchAll(
            Map<ID, F> batch,
            @NonNull Joiner<? extends T, ? extends ID, E, ? super F> joiner,
            Selection<? extends T> selection,
            final SelectionPropagationEnum propagation
    ) {
        SelectionDescriptor selectionDescriptor = selection == null ? null : getSelectionDescriptor(selection);
        JoinerDescriptor joinerDescriptor = getJoinerDescriptor(joiner);
        if (selectionDescriptor != null) {
            checkBatchOfFetchers(batch, selectionDescriptor, joinerDescriptor);
            checkJoinerAgainstSelection(joinerDescriptor, selectionDescriptor);
        }
        List<E> entities = new ArrayList<>(batch.size());
        Map<ID, T> result = new HashMap<>(batch.size());
        this.<T, ID, E, F>prepareForGrouping(joiner, batch, entities, result);
        Set<String> joined = new HashSet<>(1);
        for (Map.Entry<String, JoinerDescriptor.JoinerAccessor> entry : joinerDescriptor.accessors.entrySet()) {
            JoinerDescriptor.JoinerAccessor joinerAccessor = entry.getValue();
            SelectionDescriptor.SelectionAccessor selectionAccessor = selectionDescriptor == null ? null : selectionDescriptor.accessors.get(entry.getKey());
            Selection<?> nestedSelection = selectionAccessor == null ? null : (Selection<?>) invoke(selectionAccessor.nestedSelectionAccessor, selection);
            if (propagation == ALL && empty(nestedSelection))
                continue;
            Map<ID, Object> nestedBatch = (Map<ID, Object>) invoke(joinerAccessor.joinMethod, joiner, entities);
            Joiner<?, ?, ?, ?> nestedJoiner = joinerAccessor.nestedJoinerAccessor == null ? null : (Joiner<?, ?, ?, ?>) invoke(joinerAccessor.nestedJoinerAccessor, joiner);
            if (nestedJoiner == null) {
                for (Map.Entry<ID, Object> nested : nestedBatch.entrySet()) {
                    T model = result.get(nested.getKey());
                    Preconditions.checkState(model != null, UNMATCHED_ID, nested.getKey(), entry.getKey());
                    F fetcher = batch.get(nested.getKey());
                    FetcherDescriptor fetcherDescriptor = getFetcherDescriptor(fetcher);
                    FetcherDescriptor.FetcherAccessor fetcherAccessor = fetcherDescriptor.accessors.get(entry.getKey());
                    nestedSelection(model, nested.getValue(), fetcher, fetcherAccessor, nestedSelection, (nestedFetcher, ignored) -> propagate(propagation, nestedSelection, nestedFetcher));
                }
            } else {
                Map<ID, Object> resolvedNestedBatch = resolveNestedBatch(nestedJoiner, nestedBatch, nestedSelection, (g, m, s) -> {
                    if (propagation == ALL)
                        return resolveTopLevelBatch(g, m, s);
                    else
                        return fetchAll(m, g, s, NESTED);
                });
                applyResolvedNestedBatch(batch, result, entry.getKey(), resolvedNestedBatch);
            }
            joined.add(entry.getKey());
        }
        for (Map.Entry<ID, F> entry : batch.entrySet()) {
            F fetcher = entry.getValue();
            FetcherDescriptor fetcherDescriptor = getFetcherDescriptor(fetcher);
            fetchAll(result.get(entry.getKey()), fetcher, fetcherDescriptor, selection, selectionDescriptor, propagation, joined);
        }
        return result;
    }

    private <T, ID, F extends Fetcher<T>> void checkBatchOfFetchers(Map<ID, F> batch, SelectionDescriptor selectionDescriptor, JoinerDescriptor joinerDescriptor) {
        for (F fetcher : batch.values()) {
            FetcherDescriptor fetcherDescriptor = getFetcherDescriptor(fetcher);
            checkFetcherAgainstSelection(fetcherDescriptor, selectionDescriptor);
            checkFetcherAgainstJoiner(fetcherDescriptor, joinerDescriptor);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <ID> Map<ID, Object> resolveNestedBatch(
            @NonNull Joiner joiner,
            Map<ID, ?> batch,
            Selection<?> selection,
            TriFunction<Joiner, Map, Selection, Map<Object, Object>> nestedResolve
    ) {
        if (CollectionUtils.isEmpty(batch))
            return emptyMap();
        Boolean toManyAssociation = null;
        Class<? extends Collection> collectionClass = null;
        Map<Object, List<ID>> correlationIds = new HashMap<>(batch.size());
        Map<Object, Fetcher<?>> workingBatch = new HashMap<>();
        for (Map.Entry<ID, ?> entry : batch.entrySet()) {
            if (entry.getValue() instanceof Collection) {
                if (Boolean.FALSE.equals(toManyAssociation)) {
                    throw new IllegalStateException("Type of batch elements should be homogenous");
                } else {
                    toManyAssociation = true;
                }
                Collection<Fetcher<?>> fetchers = (Collection<Fetcher<?>>) entry.getValue();
                if (collectionClass != null && collectionClass.isAssignableFrom(fetchers.getClass()))
                    throw new IllegalStateException("Class of collections should be same");
                else {
                    collectionClass = getSupportedCollection(fetchers.getClass());
                }
                for (Fetcher<?> fetcher : fetchers) {
                    Object id = Preconditions.checkNotNull(joiner.getId(joiner.getUnderlyingEntity(fetcher)));
                    correlationIds.computeIfAbsent(id, ignored -> new ArrayList<>(1)).add(entry.getKey());
                    workingBatch.put(id, fetcher);
                }
            } else {
                if (Boolean.TRUE.equals(toManyAssociation))
                    throw new IllegalStateException("Type of batch elements should be homogenous");
                else
                    toManyAssociation = false;
                Fetcher<?> fetcher = (Fetcher<?>) entry.getValue();
                Object id = Preconditions.checkNotNull(joiner.getId(joiner.getUnderlyingEntity(fetcher)));
                correlationIds.computeIfAbsent(id, ignored -> new ArrayList<>(1)).add(entry.getKey());
                workingBatch.put(id, fetcher);
            }
        }
        Map<Object, Object> resolved = nestedResolve.apply(joiner, workingBatch, selection);
        Map<ID, Object> result = new HashMap<>(correlationIds.size());
        for (Map.Entry<Object, Object> entry : resolved.entrySet()) {
            for (ID id : correlationIds.get(entry.getKey())) {
                if (toManyAssociation) {
                    final Class<? extends Collection> finalCollectionClass = collectionClass;
                    Collection collection = (Collection) result.computeIfAbsent(id, ignored -> SUPPORTED_COLLECTIONS.get(finalCollectionClass).get());
                    collection.add(entry.getValue());
                } else {
                    result.put(id, entry.getValue());
                }
            }
        }
        return result;
    }

    private <T> T fetchAll(
            Fetcher<? extends T> fetcher,
            Selection<? extends T> selection,
            SelectionPropagationEnum propagation,
            Set<String> skip
    ) {
        if (fetcher == null)
            return null;
        FetcherDescriptor fetcherDescriptor = getFetcherDescriptor(fetcher);
        SelectionDescriptor selectionDescriptor = null;
        if (selection != null) {
            selectionDescriptor = getSelectionDescriptor(selection);
            checkFetcherAgainstSelection(fetcherDescriptor, selectionDescriptor);
        }
        T model = Preconditions.checkNotNull(fetcher.create());
        fetchAll(model, fetcher, fetcherDescriptor, selection, selectionDescriptor, propagation, skip);
        return model;
    }

    private <T> void fetchAll(T model, Fetcher<? extends T> fetcher, FetcherDescriptor fetcherDescriptor, Selection<? extends T> selection, SelectionDescriptor selectionDescriptor, SelectionPropagationEnum propagation, Set<String> skip) {
        for (Map.Entry<String, FetcherDescriptor.FetcherAccessor> fetcherAccessorEntry : fetcherDescriptor.accessors.entrySet()) {
            if (skip.contains(fetcherAccessorEntry.getKey()))
                continue;
            FetcherDescriptor.FetcherAccessor fetcherAccessor = fetcherAccessorEntry.getValue();
            if (!fetcherAccessor.isNested()) {
                invoke(fetcherAccessor.fetchMethod, fetcher, model);
            } else {
                final SelectionDescriptor.SelectionAccessor selectionAccessor;
                final Selection<?> nestedSelection;
                if (selection == null || propagation == NESTED) {
                    nestedSelection = null;
                } else {
                    selectionAccessor = selectionDescriptor.accessors.get(fetcherAccessorEntry.getKey());
                    nestedSelection = selectionAccessor == null ? null : (Selection<?>) invoke(selectionAccessor.nestedSelectionAccessor, selection);
                }
                if (propagation == ALL && empty(nestedSelection))
                    continue;
                Object nested = invoke(fetcherAccessor.nestedFetcherAccessor, fetcher);
                nestedSelection(model, nested, fetcher, fetcherAccessor, nestedSelection, (nestedFetcher, ignored) -> propagate(propagation, nestedSelection, nestedFetcher));
            }
        }
    }

    private Object propagate(SelectionPropagationEnum propagation, Selection<?> nestedSelection, Fetcher<?> nestedFetcher) {
        if (propagation == NESTED) {
            return fetchAll(nestedFetcher, null, NESTED, emptySet());
        } else {
            return resolve(nestedFetcher, nestedSelection);
        }
    }

    @SuppressWarnings({"unchecked"})
    private <T> void nestedSelection(
            T model,
            Object nested,
            Fetcher<? extends T> fetcher,
            FetcherDescriptor.FetcherAccessor fetcherAccessor,
            Selection<?> nestedSelection,
            BiFunction<Fetcher<?>, Selection<?>, Object> nestedResolve
    ) {
        if (nested == null)
            return;
        if (fetcherAccessor.isToManyAssociation()) {
            Collection<Fetcher<?>> fetchers = (Collection<Fetcher<?>>) nested;
            Collection<Object> result = getTargetCollection(fetchers.getClass(), fetchers.isEmpty());
            for (Fetcher<?> nestedFetcher : fetchers) {
                Object resolved = nestedResolve.apply(nestedFetcher, nestedSelection);
                result.add(resolved);
            }
            invoke(fetcherAccessor.fetchMethod, fetcher, model, result);
        } else {
            Fetcher<?> nestedFetcher = (Fetcher<?>) nested;
            Object resolve = nestedResolve.apply(nestedFetcher, nestedSelection);
            invoke(fetcherAccessor.fetchMethod, fetcher, model, resolve);
        }
    }

    @SuppressWarnings("rawtypes")
    private Collection getTargetCollection(Class<? extends Collection> collectionClass, boolean empty) {
        Class<? extends Collection> supportedCollection = getSupportedCollection(collectionClass);
        return empty ? SUPPORTED_EMPTY_COLLECTIONS.get(supportedCollection).get() : SUPPORTED_COLLECTIONS.get(supportedCollection).get();
    }

    @SuppressWarnings("rawtypes")
    private Class<? extends Collection> getSupportedCollection(Class<? extends Collection> collectionClass) {
        for (Map.Entry<Class<? extends Collection>, Supplier<? extends Collection<?>>> entry : SUPPORTED_COLLECTIONS.entrySet()) {
            if (entry.getKey().isAssignableFrom(collectionClass))
                return entry.getKey();
        }
        throw new IllegalStateException("Unexpected collection of type " + collectionClass + " provided");
    }

    private JoinerDescriptor getJoinerDescriptor(Joiner<?, ?, ?, ?> joiner) {
        return JOINER_DESCRIPTORS.computeIfAbsent(joiner.getClass(), clazz -> {
            ResolvableType type = ResolvableType.forClass(Joiner.class, clazz);
            ResolvableType target = type.getGeneric(0);
            ResolvableType idType = type.getGeneric(1);
            ResolvableType entityType = type.getGeneric(2);
            ResolvableType fetcherType = type.getGeneric(3);
            Map<String, List<Method>> methods = groupBySelectionKey(clazz, 2);
            Map<String, JoinerDescriptor.JoinerAccessor> accessors = new HashMap<>();
            for (Map.Entry<String, List<Method>> entry : methods.entrySet()) {
                List<Method> list = entry.getValue();
                Method fetchMethod = list.get(0);
                Method nestedJoinerAccessor;
                if (list.size() == 1) {
                    nestedJoinerAccessor = null;
                } else {
                    nestedJoinerAccessor = list.get(1);
                    if (!Map.class.isAssignableFrom(fetchMethod.getReturnType())) {
                        Method temp = fetchMethod;
                        fetchMethod = nestedJoinerAccessor;
                        nestedJoinerAccessor = temp;
                    }
                }
                checkJoinerAccessor(clazz, entry.getKey(), fetchMethod, nestedJoinerAccessor, idType, entityType);
                ResolvableType fetchMethodTargetType = ResolvableType.forType(Map.class, ResolvableType.forMethodReturnType(fetchMethod)).getGeneric(1);
                ResolvableType collectionType;
                if (COLLECTION_RAW.isAssignableFrom(fetchMethodTargetType)) {
                    collectionType = fetchMethodTargetType;
                    fetchMethodTargetType = ResolvableType.forType(Collection.class, fetchMethodTargetType).getGeneric(0);
                } else {
                    collectionType = null;
                    fetchMethodTargetType = ResolvableType.forType(Fetcher.class, fetchMethodTargetType).getGeneric(0);
                }
                accessors.put(
                        entry.getKey(),
                        new JoinerDescriptor.JoinerAccessor(
                                fetchMethod,
                                nestedJoinerAccessor,
                                fetchMethodTargetType,
                                collectionType
                        )
                );
            }
            return new JoinerDescriptor(target, accessors, fetcherType, clazz);
        });
    }

    private FetcherDescriptor getFetcherDescriptor(Fetcher<?> fetcher) {
        return FETCHER_DESCRIPTORS.computeIfAbsent(fetcher.getClass(), clazz -> {
            ResolvableType fetcherTarget = ResolvableType.forClass(Fetcher.class, clazz).getGeneric(0);
            Map<String, List<Method>> methods = groupBySelectionKey(clazz, 2);
            Map<String, FetcherDescriptor.FetcherAccessor> result = new HashMap<>(methods.size());
            for (Map.Entry<String, List<Method>> entry : methods.entrySet()) {
                List<Method> list = entry.getValue();
                Method fetchMethod = list.get(0);
                if (list.size() == 2) {
                    Method nestedFetcherAccessor = list.get(1);
                    ResolvableType nestedReturnType = ResolvableType.forMethodReturnType(nestedFetcherAccessor);
                    if (
                        !FETCHER_RAW.isAssignableFrom(nestedReturnType) &&
                        !COLLECTION_RAW.isAssignableFrom(nestedReturnType)
                    ) {
                        Method temp = fetchMethod;
                        fetchMethod = nestedFetcherAccessor;
                        nestedFetcherAccessor = temp;
                        list.set(0, fetchMethod);
                        list.set(1, nestedFetcherAccessor);
                    }
                    nestedReturnType = ResolvableType.forMethodReturnType(nestedFetcherAccessor);
                    Preconditions.checkArgument(
                        fetchMethod.getParameterCount() == 2,
                        "Fetcher's fetch method must have 2 parameters when nested fetcher accessor is present. " +
                        VIOLATION,
                        fetchMethod
                    );
                    ResolvableType fetchMethodTarget = ResolvableType.forMethodParameter(fetchMethod, 1);
                    ResolvableType generic;
                    if (!COLLECTION_RAW.isAssignableFrom(nestedReturnType)) {
                        assertReturnsFetcher(nestedFetcherAccessor, nestedReturnType);
                        generic = ResolvableType.forType(Fetcher.class, nestedReturnType).getGeneric(0);
                    } else {
                        nestedReturnType = ResolvableType.forType(Collection.class, nestedReturnType);
                        assertReturnsFetcher(nestedFetcherAccessor, nestedReturnType.getGeneric(0));
                        Preconditions.checkArgument(COLLECTION_RAW.isAssignableFrom(fetchMethodTarget),
                            "Second param of fetch method must be of collection type when nested fetcher accessor is present" +
                            VIOLATION,
                            fetchMethod
                        );
                        fetchMethodTarget = ResolvableType.forType(Collection.class, fetchMethodTarget).getGeneric(0);
                        generic = ResolvableType.forType(Fetcher.class, nestedReturnType.getGeneric(0)).getGeneric(0);
                    }
                    Preconditions.checkArgument(
                            generic.isAssignableFrom(fetchMethodTarget),
                            "Fetcher's nested fetcher accessor return type not assignable to fetch method second param. " +
                            VIOLATION,
                            nestedFetcherAccessor
                    );
                } else {
                    Preconditions.checkArgument(
                        fetchMethod.getParameterCount() == 1,
                        "Fetcher property with no nested fetcher accessor must have 1 parameter. Violation in %s",
                        fetchMethod
                    );
                }
                ResolvableType fetchMethodFirstArg = ResolvableType.forMethodParameter(fetchMethod, 0);
                Preconditions.checkArgument(
                        fetchMethodFirstArg.isAssignableFrom(fetcherTarget),
                        "Fetcher's fetch method first argument must be the same, or a subtype of type returned by 'create' method. " +
                        VIOLATION,
                        fetchMethod
                );
            }
            for (Map.Entry<String, List<Method>> entry : methods.entrySet()) {
                List<Method> list = entry.getValue();
                Method nestedFetcherAccessor = list.size() > 1 ? list.get(1) : null;
                ResolvableType collectionType = nestedFetcherAccessor != null && COLLECTION_RAW.isAssignableFrom(nestedFetcherAccessor.getReturnType()) ? ResolvableType.forMethodReturnType(nestedFetcherAccessor) : null;
                result.put(entry.getKey(), new FetcherDescriptor.FetcherAccessor(entry.getKey(), list.get(0), nestedFetcherAccessor, collectionType));
            }
            return new FetcherDescriptor(fetcherTarget, result, clazz);
        });
    }

    private void assertReturnsFetcher(Method nestedFetcherAccessor, ResolvableType returnType) {
        Preconditions.checkArgument(
            FETCHER_RAW.isAssignableFrom(returnType),
            "Fetcher's nested fetcher accessor must return instance of Fetcher class. Violation in %s",
            nestedFetcherAccessor
        );
    }

    private Object invoke(Method m, Object obj, Object...args) {
        try {
            return m.invoke(obj, args);
        } catch (IllegalAccessException e) {
//          В нормальных ситуации исключения быть не должно,
//          потому что мы вызываем только публичные методы публичных классов
            throw new IllegalStateException("Can't access method " + m, e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Method " + m + " threw exception during reflection access: " + e.getTargetException().getMessage(), e.getTargetException());
        }
    }

    private SelectionDescriptor getSelectionDescriptor(Selection<?> selection) {
        return SELECTION_DESCRIPTORS.computeIfAbsent(selection.getClass(), clazz -> {
            Map<String, List<Method>> methods = groupBySelectionKey(clazz, 1);
            ReflectionUtils.doWithFields(clazz, field -> getFromField(clazz, methods, field));
            Map<String, SelectionDescriptor.SelectionAccessor> result = new HashMap<>(methods.size());
            for (Map.Entry<String, List<Method>> entry : methods.entrySet()) {
                List<Method> list = entry.getValue();
                Method m = list.get(0);
                Method selectionEnumAccessor;
                Method nestedSelectionAccessor;
                if (m.getReturnType() == SelectionEnum.class) {
                    selectionEnumAccessor = m;
                    nestedSelectionAccessor = null;
                } else {
                    selectionEnumAccessor = null;
                    nestedSelectionAccessor = m;
                }
                checkSelectionAccessors(clazz, entry, selectionEnumAccessor, nestedSelectionAccessor);
                result.put(entry.getKey(), new SelectionDescriptor.SelectionAccessor(selectionEnumAccessor, nestedSelectionAccessor));
            }
            ResolvableType type = ResolvableType.forClass(Selection.class, clazz).getGeneric(0);
            return new SelectionDescriptor(type, result, clazz);
        });
    }

    @SuppressWarnings("rawtypes")
    private void checkSelectionAccessors(Class<? extends Selection> clazz, Map.Entry<String, List<Method>> entry, Method selectionEnumAccessor, Method nestedSelectionAccessor) {
        if (selectionEnumAccessor != null) {
            assertReturnsSelectionEnum(clazz, entry, selectionEnumAccessor);
            assertSelectionMethodZeroParams(selectionEnumAccessor);
        } else {
            assertSelectionMethodZeroParams(nestedSelectionAccessor);
            assertReturnsSelection(clazz, entry, nestedSelectionAccessor);
        }
    }

    private Map<String, List<Method>> groupBySelectionKey(Class<?> clazz, int maxSize) {
        Map<String, List<Method>> methodsRelatedToKey = new HashMap<>();
        doWithMethods(clazz, method -> {
            SelectionKey key = method.getAnnotation(SelectionKey.class);
            if (key == null)
                return;
            methodsRelatedToKey.compute(key.value(), (s, list) -> ensureNoDuplicates(method, s, list, maxSize));
        });
        return methodsRelatedToKey;
    }

    private void getFromField(Class<?> clazz, Map<String, List<Method>> methods, java.lang.reflect.Field field) {
        SelectionKey key = field.getAnnotation(SelectionKey.class);
        if (key != null) {
            PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(clazz, field.getName());
            Preconditions.checkArgument(
                descriptor != null && descriptor.getReadMethod() != null,
                "Field %s annotated with %s must have read method (getter) without arguments.",
                SelectionKey.class.getSimpleName(),
                field
            );
            Method method = descriptor.getReadMethod();
            methods.compute(key.value(), (s, list) -> ensureNoDuplicates(method, s, list, 1));
        }
    }

    private void assertReturnsSelection(Class<?> clazz, Map.Entry<String, List<Method>> entry, Method nestedSelectionAccessor) {
        Preconditions.checkArgument(
            SELECTION_RAW.isAssignableFrom(ResolvableType.forMethodReturnType(nestedSelectionAccessor)),
            "No nested selection accessor specified for property %s for type %s",
            entry.getKey(),
            clazz
        );
    }

    private void assertReturnsSelectionEnum(Class<?> clazz, Map.Entry<String, List<Method>> entry, Method selectionEnumAccessor) {
        Preconditions.checkArgument(
            selectionEnumAccessor.getReturnType() == SelectionEnum.class,
            "No %s accessor found for property %s in selection of type %s",
            SelectionEnum.class.getSimpleName(),
            entry.getKey(),
            clazz
        );
    }

    private void assertSelectionMethodZeroParams(Method selectionEnumAccessor) {
        Preconditions.checkArgument(
            selectionEnumAccessor.getParameterCount() == 0,
            "Selection's methods annotated with %s must have zero arguments. Violation in %s",
            SelectionKey.class.getSimpleName(),
            selectionEnumAccessor
        );
    }

    private List<Method> ensureNoDuplicates(Method method, String key, List<Method> list, int maxSize) {
        if (list == null)
            return new ArrayList<>(Collections.singletonList(method));
        if (list.contains(method))
            return list;
        Preconditions.checkArgument(list.size() < maxSize, "Key %s meets more than maximum of %s times", key, maxSize);
        list.add(method);
        return list;
    }

    private void doWithMethods(Class<?> clazz, ReflectionUtils.MethodCallback callback) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (!method.getDeclaringClass().isInterface() && !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                continue;
            try {
                callback.doWith(method);
                method.setAccessible(true);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            doWithMethods(clazz.getSuperclass(), callback);
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            doWithMethods(ifc, callback);
        }
    }

    @SuppressWarnings("rawtypes")
    private void checkJoinerAccessor(
            Class<? extends Joiner> clazz,
            String selectionKey,
            Method fetchMethod,
            Method nestedJoinerAccessor,
            ResolvableType idType,
            ResolvableType entityType
    ) {
        ResolvableType fetchMethodReturns = ResolvableType.forMethodReturnType(fetchMethod, clazz);
        Preconditions.checkArgument(
                MAP_RAW.isAssignableFrom(fetchMethodReturns),
                "Joiner's join method should return Map. Violation in %s for type %s. Selection key: %s",
                fetchMethod,
                clazz,
                selectionKey
        );
        ResolvableType fetchMethodParam = ResolvableType.forMethodParameter(fetchMethod, 0, clazz);
        Preconditions.checkArgument(
                COLLECTION_RAW.isAssignableFrom(fetchMethodParam) && fetchMethodParam.getRawClass().isAssignableFrom(Collection.class),
                "Joiner's join method param must be assignable to Collection. Violation in %s for type %s. Selection key: %s",
                fetchMethod,
                clazz,
                selectionKey
        );
        fetchMethodParam = ResolvableType.forType(Collection.class, fetchMethodParam).getGeneric(0);
        Preconditions.checkArgument(
                entityType.isAssignableFrom(fetchMethodParam) && fetchMethodParam.isAssignableFrom(entityType),
                "Joiner's join method param entity type mismatch in %s. Expected: %s, Actual: %s",
                clazz,
                entityType,
                fetchMethodParam
        );
        ResolvableType returnedMapKeyType = ResolvableType.forType(Map.class, fetchMethodReturns).getGeneric(0);
        ResolvableType returnedMapValueType = ResolvableType.forType(Map.class, fetchMethodReturns).getGeneric(1);
        if (COLLECTION_RAW.isAssignableFrom(returnedMapValueType))
            returnedMapValueType = ResolvableType.forType(Collection.class, returnedMapValueType).getGeneric(0);
        Preconditions.checkArgument(
                idType.isAssignableFrom(returnedMapKeyType),
                "Joiner's join method first type argument (key type in Map) is not assignable from id type declared in %s. Declared id type: %s, Actual: %s",
                clazz,
                idType,
                returnedMapKeyType
        );
        Preconditions.checkArgument(
                FETCHER_RAW.isAssignableFrom(returnedMapValueType),
                "Joiner's join method return type second type argument must be assignable to Fetcher. Violation in %s for type %s. Selection key %s",
                fetchMethod,
                clazz,
                selectionKey
        );
        if (nestedJoinerAccessor != null) {
            ResolvableType nestedJoinerReturns = ResolvableType.forMethodReturnType(nestedJoinerAccessor);
            Preconditions.checkArgument(
                    JOINER_RAW.isAssignableFrom(nestedJoinerReturns),
                    "Nested joiner accessor must return Joiner. Violation in %s for type %s. Selection key: %s",
                    nestedJoinerAccessor,
                    clazz,
                    selectionKey
            );
            ResolvableType nestedTarget = ResolvableType.forType(Joiner.class, nestedJoinerReturns).getGeneric(0);
            ResolvableType joinMethodTarget = ResolvableType.forType(Fetcher.class, ResolvableType.forMethodReturnType(fetchMethod).getGeneric(1)).getGeneric(0);
            Preconditions.checkArgument(
                    nestedTarget.isAssignableFrom(joinMethodTarget),
                    "Nested joiner target must be assignable from join method target. Violation in selection key %s for type %s",
                    selectionKey,
                    clazz
            );
        }
    }

    private void checkFetcherAgainstSelection(FetcherDescriptor fetcherDescriptor, SelectionDescriptor selectionDescriptor) {
        cacheCheck(CHECKED_FETCHERS_AGAINST_SELECTION, selectionDescriptor, fetcherDescriptor, () -> {
            Preconditions.checkArgument(
                    selectionDescriptor.targetType.isAssignableFrom(fetcherDescriptor.targetType),
                    "Selection %s target type is not assignable from fetcher %s target type",
                    selectionDescriptor.selectionClass,
                    fetcherDescriptor.fetcherClass
            );
            for (Map.Entry<String, SelectionDescriptor.SelectionAccessor> selectionAccessorEntry : selectionDescriptor.accessors.entrySet()) {
                SelectionDescriptor.SelectionAccessor selectionAccessor = selectionAccessorEntry.getValue();
                FetcherDescriptor.FetcherAccessor fetcherAccessor = fetcherDescriptor.accessors.get(selectionAccessorEntry.getKey());
                Preconditions.checkArgument(
                        fetcherAccessor != null,
                        "Property %s defined in selection, but fetcher is unaware of it. Selection: %s. Fetcher: %s",
                        selectionAccessorEntry.getKey(),
                        selectionDescriptor.selectionClass,
                        fetcherDescriptor.fetcherClass
                );
                if (fetcherAccessor.isNested()) {
                    Preconditions.checkArgument(
                            selectionAccessor.isNested(),
                            "Property %s has nested fetcher accessor, but selection is unaware of it. Selection of type %s, fetcher of type %s",
                            selectionAccessorEntry.getKey(),
                            selectionDescriptor.selectionClass,
                            fetcherDescriptor.fetcherClass
                    );
                } else {
                    Preconditions.checkArgument(
                            !selectionAccessor.isNested(),
                            "Property %s has nested selection, but fetcher is unaware of it. Selection of type %s, fetcher of type %s",
                            selectionDescriptor.selectionClass,
                            fetcherDescriptor.fetcherClass
                    );
                }
            }
        });
    }

    private void checkJoinerAgainstSelection(JoinerDescriptor joinerDescriptor, SelectionDescriptor selectionDescriptor) {
        cacheCheck(CHECKED_JOINERS_AGAINST_SELECTION, selectionDescriptor, joinerDescriptor, () -> {
            Preconditions.checkArgument(
                    selectionDescriptor.targetType.isAssignableFrom(joinerDescriptor.targetType),
                    "Selection %s target type %s is not assignable from joiner %s target type %s",
                    selectionDescriptor.selectionClass,
                    selectionDescriptor.targetType,
                    joinerDescriptor.joinerClass,
                    joinerDescriptor.targetType
            );
            for (Map.Entry<String, SelectionDescriptor.SelectionAccessor> selectionAccessorEntry : selectionDescriptor.accessors.entrySet()) {
                SelectionDescriptor.SelectionAccessor selectionAccessor = selectionAccessorEntry.getValue();
                JoinerDescriptor.JoinerAccessor joinerAccessor = joinerDescriptor.accessors.get(selectionAccessorEntry.getKey());
                if (joinerAccessor == null)
                    continue;
                Preconditions.checkArgument(
                        selectionAccessor.isNested(),
                        "Key %s has nested joiner accessor, but selection is non nested. Selection of type %s, joiner of type %s",
                        selectionAccessorEntry.getKey(),
                        selectionDescriptor.selectionClass,
                        joinerDescriptor.joinerClass
                );
            }
        });
    }

    private void checkFetcherAgainstJoiner(FetcherDescriptor fetcherDescriptor, JoinerDescriptor joinerDescriptor) {
        cacheCheck(CHECKED_FETCHERS_AGAINST_JOINER, joinerDescriptor, fetcherDescriptor, () -> {
            Preconditions.checkArgument(
                    joinerDescriptor.targetType.isAssignableFrom(joinerDescriptor.targetType),
                    "Joiner %s target type %s is not assignable from fetcher %s target type %s",
                    joinerDescriptor.joinerClass,
                    joinerDescriptor.targetType,
                    fetcherDescriptor.fetcherClass,
                    fetcherDescriptor.targetType
            );
            Preconditions.checkArgument(
                    joinerDescriptor.fetcherType.isAssignableFrom(fetcherDescriptor.fetcherClass),
                    "Fetcher %s is not assignable to joiner declared fetcher type %s. Joiner is %s",
                    fetcherDescriptor.fetcherClass,
                    joinerDescriptor.fetcherType,
                    joinerDescriptor.joinerClass
            );
            for (Map.Entry<String, JoinerDescriptor.JoinerAccessor> joinerAccessorEntry : joinerDescriptor.accessors.entrySet()) {
                String selectionKey = joinerAccessorEntry.getKey();
                FetcherDescriptor.FetcherAccessor fetcherAccessor = fetcherDescriptor.accessors.get(selectionKey);
                Preconditions.checkArgument(
                        fetcherAccessor != null,
                        "Joiner %s has accessor for key %s, but fetcher %s is unaware of it",
                        joinerDescriptor.joinerClass,
                        selectionKey,
                        fetcherDescriptor.fetcherClass
                );
                Preconditions.checkArgument(
                        fetcherAccessor.isNested(),
                        "Joiner %s has accessor for key %s, but fetcher accessor is not nested (joiner can be defined only on nested selection keys). Fetcher: %s",
                        joinerDescriptor.joinerClass,
                        selectionKey,
                        fetcherDescriptor.fetcherClass
                );
                JoinerDescriptor.JoinerAccessor joinerAccessor = joinerAccessorEntry.getValue();
                if (fetcherAccessor.isToManyAssociation()) {
                    Preconditions.checkArgument(
                            joinerAccessor.isToManyAssociation(),
                            "Key % defined as ToMany association in fetcher %s, but joiner %s defined it as ToOne association",
                            selectionKey,
                            fetcherDescriptor.fetcherClass,
                            joinerDescriptor.joinerClass
                    );
                    Preconditions.checkArgument(
                            joinerAccessor.collectionType.resolve().isAssignableFrom(fetcherAccessor.collectionType.resolve()),
                            "Joiner %s collection type %s is not assignable to fetcher %s collection type %s",
                            joinerDescriptor.joinerClass,
                            joinerAccessor.collectionType,
                            fetcherDescriptor.fetcherClass,
                            fetcherAccessor.collectionType
                    );
                } else {
                    Preconditions.checkArgument(
                            !joinerAccessor.isToManyAssociation(),
                            "Key % defined as ToOne association in fetcher %s, but joiner %s defined it as ToMany association",
                            selectionKey,
                            fetcherDescriptor.fetcherClass,
                            joinerDescriptor.joinerClass
                    );
                }
            }
        });
    }

    private <K, V> void cacheCheck(ConcurrentMap<K, List<V>> cache, K checkKey, V checkValue, Runnable check) {
        List<V> checked = cache.computeIfAbsent(checkKey, ignored -> new ArrayList<>());
        if (!checked.contains(checkValue)) {
            check.run();
            synchronized (checked) {
                if (!checked.contains(checkValue))
                    checked.add(checkValue);
            }
        }
    }

    public static Selector create() {
        return new Selector();
    }

    private boolean empty(Selection<?> selection) {
        return selection == null || selection.empty();
    }

    @FunctionalInterface
    private interface TriFunction<P1, P2, P3, R> {
        R apply(P1 p1, P2 p2, P3 p3);
    }

}
