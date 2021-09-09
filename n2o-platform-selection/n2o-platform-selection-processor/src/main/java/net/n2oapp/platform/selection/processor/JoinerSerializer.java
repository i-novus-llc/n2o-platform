package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.Fetcher;
import net.n2oapp.platform.selection.api.Joiner;
import net.n2oapp.platform.selection.api.Selection;
import net.n2oapp.platform.selection.api.SelectionPropagation;

import java.io.IOException;
import java.io.Writer;

@SuppressWarnings("java:S1192")
class JoinerSerializer extends AbstractSerializer {

    @Override
    void serializeProperty(SelectionMeta meta, String self, SelectionProperty property, Writer out) throws IOException {
        out.append("\t");
        NestedSelectionFetcher nested = appendJoined(meta, property, out);
        out.append(" ");
        out.append("join");
        out.append(capitalize(property.getName()));
        out.append("(java.util.List");
        out.append("<");
        out.append(meta.getEntityTypeVariable());
        out.append(">");
        out.append(" ");
        out.append("entities, ");
        out.append("java.util.List");
        out.append("<");
        out.append(meta.getIdTypeVariable());
        out.append("> ids");
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
            out.append("\t\tfinal int size = com.google.common.collect.Iterables.size(fetchers);\n");
            out.append("\t\tfinal java.util.List<").append(meta.getModelType()).append("> models = new java.util.ArrayList<>(size);\n");
            out.append("\t\tfinal java.util.List<").append(meta.getIdTypeVariable()).append("> uniqueIds = new java.util.ArrayList<>(size);\n");
            out.append("\t\tfinal java.util.List<").append(meta.getEntityTypeVariable()).append("> entities = new java.util.ArrayList<>(size);\n");
            out.append("\t\tnet.n2oapp.platform.selection.api.FenwickTree tree = new net.n2oapp.platform.selection.api.FenwickTree(size + 1);\n");
            out.append("\t\tfinal boolean[] duplicate = new boolean[size];\n");
            out.append("\t\tint fetcherIdx = 0;\n");
            out.append("\t\tfor (java.util.Iterator<? extends ").append(meta.getFetcherType()).append("> iter = fetchers.iterator(); iter.hasNext();) {\n");
            out.append("\t\t\t").append(meta.getFetcherType()).append(" fetcher = iter.next();\n");
            out.append("\t\t\tfinal ").append(meta.getIdTypeVariable()).append(" id = getId(fetcher.getUnderlyingEntity());\n");
            out.append("\t\t\tfinal int duplicateIdx = uniqueIds.indexOf(id);\n");
            out.append("\t\t\tif (duplicateIdx < 0) {\n");
            out.append("\t\t\t\tmodels.add(fetcher.create());\n");
            out.append("\t\t\t\tuniqueIds.add(id);\n");
            out.append("\t\t\t\tentities.add(fetcher.getUnderlyingEntity());\n");
            out.append("\t\t\t} else {\n");
            out.append("\t\t\t\tmodels.add(models.get(duplicateIdx + tree.sum(duplicateIdx)));\n");
            out.append("\t\t\t\ttree.increment(uniqueIds.size());\n");
            out.append("\t\t\t\tduplicate[fetcherIdx] = true;\n");
            out.append("\t\t\t}\n");
            out.append("\t\t\tfetcherIdx++;\n");
            out.append("\t\t}\n");
            out.append("\t\ttree = null;\n");
            out.append("\t\t");
            appendResolution(meta, out);
            out.append(" resolution = ").append(Joiner.Resolution.class.getCanonicalName()).append(".from(entities, uniqueIds, models, duplicate);\n");
            out.append("\t\tint modelIdx = 0;\n");
            out.append("\t\tfetcherIdx = 0;\n");
        } else {
            out.append("\t\t");
            appendResolution(meta, out);
            out.append(" resolution = ");
            out.append(getQualifiedName(meta.getParent()));
            out.append(".super.resolveIterable(fetchers, selection, propagation);\n");
            out.append("\t\tif (resolution == null) return null;\n");
            out.append("\t\tfinal java.util.List<").append(meta.getIdTypeVariable()).append("> uniqueIds = resolution.uniqueIds;\n");
            out.append("\t\tfinal java.util.List<").append(meta.getEntityTypeVariable()).append("> entities = resolution.uniqueEntities;\n");
            out.append("\t\tfinal java.util.List<").append(meta.getModelType()).append("> models = resolution.models;\n");
            out.append("\t\tfinal boolean[] duplicate = resolution.duplicate;\n");
            out.append("\t\tint modelIdx = 0;\n");
            out.append("\t\tint fetcherIdx = 0;\n");
            appendExplicitPropagation(out);
        }
        out.append("\t\tfor (");
        out.append(meta.getFetcherType());
        out.append(" fetcher : fetchers) {\n");
        out.append("\t\t\tif (duplicate[fetcherIdx++]) continue;\n");
        out.append("\t\t\t");
        out.append(meta.getModelType());
        out.append(" model = models.get(modelIdx++);\n");
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
                out.append("\t\t\tmodelIdx = 0;\n");
                out.append("\t\t\tfetcherIdx = 0;\n");
                if (!property.isWithNestedJoiner()) {
                    out.append("\t\t\tfinal ");
                    out.append(SelectionPropagation.class.getCanonicalName());
                    out.append(" fPropagation = propagation;\n");
                    out.append("\t\t\t");
                    NestedSelectionFetcher nested = appendJoined(meta, property, out);
                    out.append(" joined = join").append(capitalize(property.getName())).append("(entities, uniqueIds);\n");
                    out.append("\t\t\tfor (").append(meta.getFetcherType()).append(" fetcher : fetchers) {\n");
                    out.append("\t\t\t\tif (duplicate[fetcherIdx++]) continue;\n");
                    out.append("\t\t\t\t").append(meta.getModelType()).append(" model = models.get(modelIdx++);\n");
                    out.append("\t\t\t\t").append(meta.getIdTypeVariable()).append(" id = getId(fetcher.getUnderlyingEntity());\n");
                    out.append("\t\t\t\t");
                    final String varName;
                    if (property.selective()) {
                        if (property.getCollectionType() != null) {
                            out.append(property.getCollectionType().toString()).append("<");
                        }
                        out.append(nested.fetcher);
                        if (property.getCollectionType() != null) {
                            varName = "nestedFetchers";
                            out.append("> nestedFetchers");
                        } else {
                            varName = "nestedFetcher";
                            out.append(" nestedFetcher");
                        }
                    } else {
                        varName = "nested";
                        out.append(property.getOriginalTypeStr()).append(" nested");
                    }
                    out.append(" = joined.get(id);\n");
                    out.append("\t\t\t\tif (").append(varName).append(" == null) continue;\n");
                    out.append("\t\t\t\tmodel.set").append(capitalize(property.getName())).append("(");
                    if (!property.selective()) {
                        out.append(varName).append(");\n");
                    } else {
                        if (property.getCollectionType() != null) {
                            out.append(varName).append(".stream().map(nestedFetcher -> nestedFetcher.resolve(selection == null ? null : selection.get");
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
                            out.append(varName).append(".resolve(selection == null ? null : selection.get");
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
                    out.append("\t\t\t");
                    NestedSelectionFetcher nested = appendJoined(meta, property, out);
                    out.append(" joined = join").append(capitalize(property.getName())).append("(entities, uniqueIds);\n");
                    out.append("\t\t\t");
                    out.append(getQualifiedName(property.getSelection()));
                    out.append(" nestedJoiner = ");
                    out.append(property.getName());
                    out.append(getSuffix());
                    out.append("();\n");
                    out.append("\t\t\t");
                    out.append(Joiner.Resolution.class.getCanonicalName()).append("<").append(property.getTypeStr()).append(", ?, ?>");
                    out.append(" nestedResolution = nestedJoiner.resolveIterable(");
                    if (property.getCollectionType() == null) {
                        out.append("joined.values()");
                    } else {
                        out.append("new net.n2oapp.platform.selection.api.FlatteningIterable(joined.values())");
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
                    out.append("\t\t\t\tif (duplicate[fetcherIdx++]) continue;\n");
                    out.append("\t\t\t\t");
                    out.append(meta.getModelType());
                    out.append(" model = models.get(modelIdx++);\n");
                    out.append("\t\t\t\t");
                    out.append(meta.getIdTypeVariable());
                    out.append(" id = getId(fetcher.getUnderlyingEntity());\n");
                    if (property.getCollectionType() == null) {
                        out.append("\t\t\t\t");
                        out.append(nested.fetcher);
                        out.append(" nestedFetcher = joined.get(id);\n");
                        out.append("\t\t\t\tif (nestedFetcher == null) continue;\n");
                        out.append("\t\t\t\tjava.lang.Object nestedId = nestedJoiner.getId(nestedFetcher.getUnderlyingEntity());\n");
                        out.append("\t\t\t\tfinal int nestedIdx = nestedResolution.uniqueIds.indexOf(nestedId);\n");
                        out.append("\t\t\t\t").append(property.getTypeStr()).append(" nestedModel = nestedResolution.models.get(nestedIdx);\n");
                        out.append("\t\t\t\tmodel.set");
                        out.append(capitalize(property.getName()));
                        out.append("(nestedModel);\n");
                    } else {
                        out.append("\t\t\t\t");
                        out.append(property.getCollectionType().toString());
                        out.append("<");
                        out.append(nested.fetcher);
                        out.append("> nestedFetchers = joined.get(id);\n");
                        out.append("\t\t\t\tif (nestedFetchers == null) continue;\n");
                        out.append("\t\t\t\t").append(property.getCollectionType().toString()).append("<").append(property.getTypeStr()).append(">").append(" nestedModels = new ");
                        if (property.getCollectionType().toString().equals("java.util.List"))
                            out.append("java.util.ArrayList<>(nestedFetchers.size());\n");
                        else if (property.getCollectionType().toString().equals("java.util.Set"))
                            out.append("java.util.HashSet<>(nestedFetchers.size());\n");
                        out.append("\t\t\t\tfor (");
                        out.append(nested.fetcher);
                        out.append(" nestedFetcher : nestedFetchers) {\n");
                        out.append("\t\t\t\t\tjava.lang.Object nestedId = nestedJoiner.getId(nestedFetcher.getUnderlyingEntity());\n");
                        out.append("\t\t\t\t\tfinal int nestedIdx = nestedResolution.uniqueIds.indexOf(nestedId);\n");
                        out.append("\t\t\t\t\t").append(property.getTypeStr()).append(" nestedModel = nestedResolution.models.get(nestedIdx);\n");
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
