package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.Fetcher;
import net.n2oapp.platform.selection.api.Joiner;
import net.n2oapp.platform.selection.api.Selection;
import net.n2oapp.platform.selection.api.SelectionPropagation;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

@SuppressWarnings("java:S1192")
class JoinerSerializer extends AbstractSerializer {

    @Override
    void serializeProperty(SelectionMeta meta, String self, SelectionProperty property, Writer out) throws IOException {
        out.append("\t");
        NestedSelectionFetcher nested = appendJoined(meta, property, out);
        out.append(" ");
        out.append("join");
        out.append(capitalize(property.getName()));
        out.append("(");
        out.append(Collection.class.getCanonicalName());
        out.append("<");
        out.append(meta.getEntityTypeVariable());
        out.append(">");
        out.append(" ");
        out.append("entities");
        out.append(");");
        if (property.isWithNestedJoiner()) {
            out.append("\n\t");
            out.append(getQualifiedName(property.getSelection()));
            out.append(property.getGenerics(nested.selection, "?", property.getSelection().getFetcherTypeVariable() == null ? null : nested.fetcher, "?"));
            out.append(" ");
            out.append(property.getName());
            out.append(getSuffix());
            out.append("();");
        }
    }

    private NestedSelectionFetcher appendJoined(SelectionMeta meta, SelectionProperty property, Writer out) throws IOException {
        out.append("java.util.Map<");
        out.append(meta.getIdTypeVariable());
        out.append(", ");
        SelectionMeta nestedSelective = property.getSelection();
        NestedSelectionFetcher nested = new NestedSelectionFetcher();
        if (nestedSelective != null) {
            nested.fetcher = getQualifiedName(nestedSelective, Fetcher.class.getSimpleName());
            if (nestedSelective.getSelectionTypeVariable() == null) {
                nested.fetcher += property.getGenerics("?");
            } else {
                nested.selection = getQualifiedName(nestedSelective, Selection.class.getSimpleName());
                String generics = property.getGenerics();
                nested.selection += generics;
                nested.fetcher += property.getGenerics(nested.selection, "?");
            }
            if (property.getCollectionType() == null) {
                out.append(nested.fetcher);
            } else {
                out.append(property.getCollectionType().toString());
                out.append("<");
                out.append(nested.fetcher);
                out.append(">");
            }
        } else {
            out.append(property.getOriginalTypeStr());
        }
        out.append(">");
        return nested;
    }

    @Override
    Class<?> getInterfaceRaw() {
        return Joiner.class;
    }

    @Override
    void preSerialize(SelectionMeta meta, String self, Writer out) throws IOException {
        appendOverride(out);
        out.append("\tdefault ");
        appendResolution(meta, out);
        out.append(" resolveIterable(final java.lang.Iterable<? extends ");
        out.append(meta.getFetcherType());
        out.append("> fetchers, final ");
        out.append(meta.getSelectionType());
        out.append(" selection, ");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(" propagation) {\n");
        out.append("\t\tif (fetchers == null) return null;\n");
        out.append("\t\tif (fetchers instanceof java.util.Collection) {\n");
        out.append("\t\t\tif (((java.util.Collection) fetchers).isEmpty()) return ").append(Joiner.Resolution.class.getCanonicalName()).append(".").append("empty();\n");
        out.append("\t\t} else {\n");
        out.append("\t\t\tif (!fetchers.iterator().hasNext()) return ").append(Joiner.Resolution.class.getCanonicalName()).append(".").append("empty();\n");
        out.append("\t\t}\n");
        if (meta.getParent() == null) {
            appendExplicitPropagation(out);
            appendReturnNullIfSelectionEmpty(out);
            out.append("\t\tjava.util.Collection<");
            out.append(meta.getEntityTypeVariable());
            out.append("> entities = new java.util.ArrayList<>();\n");
            out.append("\t\tjava.util.LinkedHashMap<");
            out.append(meta.getIdTypeVariable());
            out.append(", ");
            out.append(meta.getModelType());
            out.append("> models = new java.util.LinkedHashMap<>();\n");
            out.append("\t\tfor (java.util.Iterator<? extends ").append(meta.getFetcherType()).append("> iter = fetchers.iterator(); iter.hasNext(); ) {\n");
            out.append("\t\t\t");
            out.append(meta.getFetcherType()).append(" fetcher = iter.next();\n");
            out.append("\t\t\t");
            out.append(meta.getEntityTypeVariable());
            out.append(" entity = fetcher.getUnderlyingEntity();\n");
            out.append("\t\t\t");
            out.append(meta.getIdTypeVariable()).append(" id = getId(entity);\n");
            out.append("\t\t\tif (models.containsKey(id)) iter.remove();\n");
            out.append("\t\t\telse {\n");
            out.append("\t\t\t\tmodels.put(getId(entity), fetcher.create());\n");
            out.append("\t\t\t\tentities.add(entity);\n");
            out.append("\t\t\t}\n");
            out.append("\t\t}\n");
            out.append("\t\t");
            appendResolution(meta, out);
            out.append(" resolution = ").append(Joiner.Resolution.class.getCanonicalName()).append(".from(entities, models);\n");
        } else {
            out.append("\t\t");
            appendResolution(meta, out);
            out.append(" resolution = ");
            out.append(getQualifiedName(meta.getParent()));
            out.append(".super.resolveIterable(fetchers, selection, propagation);\n");
            out.append("\t\tif (resolution == null) return null;\n");
            appendExplicitPropagation(out);
        }
        out.append("\t\tjava.util.Iterator<java.util.Map.Entry<");
        out.append(meta.getIdTypeVariable());
        out.append(", ");
        out.append(meta.getModelType());
        out.append(">> iter = resolution.models.entrySet().iterator();\n");
        out.append("\t\tfor (");
        out.append(meta.getFetcherType());
        out.append(" fetcher : fetchers) {\n");
        out.append("\t\t\t");
        out.append(meta.getModelType());
        out.append(" model = iter.next().getValue();\n");
        for (SelectionProperty property : meta.getProperties()) {
            if (!property.isJoined()) {
                FetcherSerializer.appendProperty(out, property, "fetcher", "\t");
            }
        }
        out.append("\t\t}\n");
        for (SelectionProperty property : meta.getProperties()) {
            if (property.isJoined()) {
                out.append("\t\t");
                appendSelectionPredicate(out, property);
                out.append(" {\n");
                if (!property.isWithNestedJoiner()) {
                    out.append("\t\t\tfinal ");
                    out.append(SelectionPropagation.class.getCanonicalName());
                    out.append(" fPropagation = propagation;\n");
                    out.append("\t\t\t");
                    NestedSelectionFetcher nested = appendJoined(meta, property, out);
                    out.append(" joined = join").append(capitalize(property.getName())).append("(resolution.entities);\n");
                    out.append("\t\t\tfor (java.util.Map.Entry<");
                    out.append(meta.getIdTypeVariable());
                    out.append(", ");
                    if (property.selective()) {
                        if (property.getCollectionType() != null) {
                            out.append(property.getCollectionType().toString()).append("<");
                        }
                        out.append(nested.fetcher);
                        if (property.getCollectionType() != null) {
                            out.append(">");
                        }
                    } else
                        out.append(property.getOriginalTypeStr());
                    out.append("> entry : joined.entrySet()) {\n");
                    out.append("\t\t\t\tresolution.models.get(entry.getKey()).set");
                    out.append(capitalize(property.getName()));
                    out.append("(entry.getValue()");
                    if (!property.selective()) {
                        out.append(");\n");
                    } else {
                        if (property.getCollectionType() != null) {
                            out.append(".stream().map(nestedFetcher -> nestedFetcher.resolve(selection == null ? null : selection.get");
                            out.append(capitalize(property.getName()));
                            out.append("(), fPropagation == ");
                            out.append(SelectionPropagation.class.getCanonicalName());
                            out.append(".");
                            out.append(SelectionPropagation.NESTED.name());
                            out.append(" ? fPropagation : selection.get");
                            out.append(capitalize(property.getName()));
                            out.append("().propagation())).collect(java.util.stream.Collectors");
                            if (property.getCollectionType().toString().equals("java.util.Set"))
                                out.append(".toSet()));\n");
                            else if (property.getCollectionType().toString().equals("java.util.List"))
                                out.append(".toList()));\n");
                        } else {
                            out.append(".resolve(selection == null ? null : selection.get");
                            out.append(capitalize(property.getName()));
                            out.append("(), fPropagation == ");
                            out.append(SelectionPropagation.class.getCanonicalName());
                            out.append(".");
                            out.append(SelectionPropagation.NESTED.name());
                            out.append(" ? fPropagation : selection.get");
                            out.append(capitalize(property.getName()));
                            out.append("().propagation()));\n");
                        }
                    }
                } else {
                    out.append("\t\t\tjava.util.Map joined = join");
                    out.append(capitalize(property.getName()));
                    out.append("(resolution.entities); // raw use here\n");
                    out.append("\t\t\t");
                    out.append(getQualifiedName(property.getSelection()));
                    out.append(" nestedJoiner = ");
                    out.append(property.getName());
                    out.append(getSuffix());
                    out.append("();\n");
                    out.append("\t\t\t");
                    out.append(Joiner.Resolution.class.getCanonicalName());
                    out.append(" nestedResolution = nestedJoiner.resolveIterable(");
                    if (property.getCollectionType() == null) {
                        out.append("new java.util.ArrayList(joined.values())");
                    } else {
                        out.append("(java.util.Collection) joined.values().stream().flatMap(o -> ((java.util.Collection) o).stream()).collect(java.util.stream.Collectors.toList())");
                    }
                    out.append(", selection == null ? null : selection.get");
                    out.append(capitalize(property.getName()));
                    out.append("(), propagation == ");
                    out.append(SelectionPropagation.class.getCanonicalName());
                    out.append(".");
                    out.append(SelectionPropagation.NESTED.name());
                    out.append(" ? propagation : selection.get");
                    out.append(capitalize(property.getName()));
                    out.append("().propagation());\n");
                    out.append("\t\t\tfor (");
                    out.append(meta.getFetcherType());
                    out.append(" fetcher : fetchers) {\n");
                    out.append("\t\t\t\t");
                    out.append(meta.getIdTypeVariable());
                    out.append(" id = getId(fetcher.getUnderlyingEntity());\n");
                    out.append("\t\t\t\t");
                    out.append(meta.getModelType());
                    out.append(" model = resolution.models.get(id);\n");
                    String nestedFetcherRaw = getQualifiedName(property.getSelection(), Fetcher.class.getSimpleName());
                    if (property.getCollectionType() == null) {
                        out.append("\t\t\t\t");
                        out.append(nestedFetcherRaw);
                        out.append(" nestedFetcher = (");
                        out.append(nestedFetcherRaw);
                        out.append(") joined.get(id);\n");
                        out.append("\t\t\t\tif (nestedFetcher == null) continue;\n");
                        out.append("\t\t\t\tjava.lang.Object nestedId = nestedJoiner.getId(nestedFetcher.getUnderlyingEntity());\n");
                        out.append("\t\t\t\t");
                        out.append(property.getSelection().getTarget().toString());
                        out.append(" nestedModel = (");
                        out.append(property.getSelection().getTarget().toString());
                        out.append(") nestedResolution.models.get(nestedId);\n");
                        out.append("\t\t\t\tmodel.set");
                        out.append(capitalize(property.getName()));
                        out.append("(nestedModel);\n");
                    } else {
                        out.append("\t\t\t\t");
                        out.append(property.getCollectionType().toString());
                        out.append("<");
                        out.append(nestedFetcherRaw);
                        out.append("> nestedFetchers = (");
                        out.append(property.getCollectionType().toString());
                        out.append("<");
                        out.append(nestedFetcherRaw);
                        out.append(">) joined.get(id);\n");
                        out.append("\t\t\t\tif (nestedFetchers == null) continue;\n");
                        out.append("\t\t\t\t");
                        out.append(property.getCollectionType().toString());
                        out.append(" nestedModels = new ");
                        if (property.getCollectionType().toString().equals("java.util.List"))
                            out.append("java.util.ArrayList();\n");
                        else if (property.getCollectionType().toString().equals("java.util.Set"))
                            out.append("java.util.HashSet();\n");
                        out.append("\t\t\t\tfor (");
                        out.append(nestedFetcherRaw);
                        out.append(" nestedFetcher : nestedFetchers) {\n");
                        out.append("\t\t\t\t\t");
                        out.append(property.getSelection().getTarget().toString());
                        out.append(" nestedModel = (");
                        out.append(property.getSelection().getTarget().toString());
                        out.append(") nestedResolution.models.get(nestedJoiner.getId(nestedFetcher.getUnderlyingEntity()));\n");
                        out.append("\t\t\t\t\tnestedModels.add(nestedModel);\n");
                        out.append("\t\t\t\t}\n");
                        out.append("\t\t\t\tmodel.set");
                        out.append(capitalize(property.getName()));
                        out.append("(nestedModels);\n");
                    }
                }
                out.append("\t\t\t}\n");
                out.append("\t\t}\n");
            }
        }
        out.append("\t\treturn resolution;\n");
        out.append("\t}\n");
    }

    @Override
    GenericSignature getGenericSignature(SelectionMeta meta) {
        return meta.getJoinerGenericSignature();
    }

    @Override
    String getExtendsSignature(SelectionMeta meta) {
        return meta.getJoinerExtendsSignature();
    }

    @Override
    boolean shouldSerialize(SelectionProperty property) {
        return property.isJoined();
    }

    private void appendResolution(SelectionMeta meta, Writer out) throws IOException {
        out.append(Joiner.Resolution.class.getCanonicalName());
        out.append("<");
        out.append(meta.getModelType());
        out.append(", ");
        out.append(meta.getEntityTypeVariable());
        out.append(", ");
        out.append(meta.getIdTypeVariable());
        out.append(">");
    }

    private static class NestedSelectionFetcher {
        String selection;
        String fetcher;
    }

}
