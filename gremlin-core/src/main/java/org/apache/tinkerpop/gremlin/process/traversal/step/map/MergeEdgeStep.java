/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.process.traversal.step.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.tinkerpop.gremlin.process.traversal.Merge;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.TraverserGenerator;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.ConstantTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.IdentityTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.Mutating;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalOptionParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Parameters;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.CallbackRegistry;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.Event;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.ListCallbackRegistry;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.EventStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalUtil;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.Attachable;
import org.apache.tinkerpop.gremlin.structure.util.CloseableIterator;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

/**
 * Implementation for the {@code mergeE()} step covering both the start step version and the one used mid-traversal.
 */
public class MergeEdgeStep<S> extends MergeStep<S, Edge, Object> {

    private static final Set allowedTokens = new LinkedHashSet(Arrays.asList(T.id, T.label, Direction.IN, Direction.OUT));

    public static void validateMapInput(final Map map, final boolean ignoreTokens) {
        MergeStep.validate(map, ignoreTokens, allowedTokens);
    }

    private Traversal.Admin<S, Object> outVTraversal = null;
    private Traversal.Admin<S, Object> inVTraversal = null;

    public MergeEdgeStep(final Traversal.Admin traversal, final boolean isStart) {
        super(traversal, isStart);
    }

    public MergeEdgeStep(final Traversal.Admin traversal, final boolean isStart, final Map merge) {
        super(traversal, isStart, merge);
    }

    public MergeEdgeStep(final Traversal.Admin traversal, final boolean isStart, final Traversal.Admin<S,Map> mergeTraversal) {
        super(traversal, isStart, mergeTraversal);
    }

    /**
     * Gets the traversal that will be used to provide the {@code Map} that will be used to identify the Direction.OUT
     * vertex during merge.
     */
    public Traversal.Admin<S, Object> getOutVTraversal() {
        return outVTraversal;
    }

    /**
     * Gets the traversal that will be used to provide the {@code Map} that will be used to identify the Direction.IN
     * vertex during merge.
     */
    public Traversal.Admin<S, Object> getInVTraversal() {
        return inVTraversal;
    }

    @Override
    public void addChildOption(final Merge token, final Traversal.Admin<S, Object> traversalOption) {
        if (token == Merge.outV) {
            this.outVTraversal = this.integrateChild(traversalOption);
        } else if (token == Merge.inV) {
            this.inVTraversal = this.integrateChild(traversalOption);
        } else {
            super.addChildOption(token, traversalOption);
        }
    }

    @Override
    public <S, C> List<Traversal.Admin<S, C>> getLocalChildren() {
        final List<Traversal.Admin<S, C>> children = super.getLocalChildren();
        if (outVTraversal != null) children.add((Traversal.Admin<S, C>) outVTraversal);
        if (inVTraversal != null) children.add((Traversal.Admin<S, C>) inVTraversal);
        return children;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        if (outVTraversal != null)
            result ^= outVTraversal.hashCode();
        if (inVTraversal != null)
            result ^= inVTraversal.hashCode();
        return result;
    }

    @Override
    public void reset() {
        super.reset();
        if (outVTraversal != null) outVTraversal.reset();
        if (inVTraversal != null) inVTraversal.reset();
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, mergeTraversal, onCreateTraversal, onMatchTraversal, outVTraversal, inVTraversal);
    }

    @Override
    public void setTraversal(final Traversal.Admin<?, ?> parentTraversal) {
        super.setTraversal(parentTraversal);
        this.integrateChild(outVTraversal);
        this.integrateChild(inVTraversal);
    }

    @Override
    public MergeEdgeStep<S> clone() {
        final MergeEdgeStep<S> clone = (MergeEdgeStep<S>) super.clone();
        clone.outVTraversal = outVTraversal != null ? outVTraversal.clone() : null;
        clone.inVTraversal = inVTraversal != null ? inVTraversal.clone() : null;
        return clone;
    }

    @Override
    protected Set getAllowedTokens() {
        return allowedTokens;
    }

    /**
     * Use the {@code Map} of search criteria to most efficiently return a {@code Stream<Edge>} of matching elements.
     * Providers might override this method when extending this step to provide their own optimized mechanisms for
     * matching the list of edges. This implementation is only optimized for the {@link T#id} so any other usage
     * will simply be in-memory filtering which could be slow.
     */
    protected Iterator<Edge> searchEdges(final Map<?,?> search) {
        final Graph graph = getGraph();

        final Object edgeId = search.get(T.id);
        final String edgeLabel = (String) search.get(T.label);
        final Object fromId = search.get(Direction.OUT);
        final Object toId = search.get(Direction.IN);

        GraphTraversal t;
        if (edgeId != null) {
            // if eid:
            //   g.E(edgeId).hasLabel(edgeLabel).where(__.outV().hasId(fromId)).where(__.inV().hasId(toId));
            t = graph.traversal().E(edgeId);
            if (edgeLabel != null)
                t = t.hasLabel(edgeLabel);
            if (fromId != null)
                t = t.where(__.outV().hasId(fromId));
            if (toId != null)
                t = t.where(__.inV().hasId(toId));
        } else if (fromId != null) {
            // else if fromId:
            //   g.V(fromId).outE(edgeLabel).where(__.inV().hasId(toId));
            t = graph.traversal().V(fromId);
            if (edgeLabel != null)
                t = t.outE(edgeLabel);
            else
                t = t.outE();
            if (toId != null)
                t = t.where(__.inV().hasId(toId));
        } else if (toId != null) {
            // else if toId:
            //   g.V(toId).inE(edgeLabel);
            t = graph.traversal().V(toId);
            if (edgeLabel != null)
                t = t.inE(edgeLabel);
            else
                t = t.inE();
        } else {
            // else:
            t = graph.traversal().E();
        }

        // add property constraints
        for (final Map.Entry<?,?> e : search.entrySet()) {
            final Object k = e.getKey();
            if (!(k instanceof String)) continue;
            t = t.has((String) k, e.getValue());
        }

        // this should auto-close the underlying traversal
        return t;
    }

    protected Map<?,?> resolveVertices(final Map map, final Traverser.Admin<S> traverser) {
        resolveVertex(Merge.outV, Direction.OUT, map, traverser, outVTraversal);
        resolveVertex(Merge.inV, Direction.IN, map, traverser, inVTraversal);
        return map;
    }

    protected void resolveVertex(final Merge token, final Direction direction, final Map map,
            final Traverser.Admin<S> traverser, final Traversal.Admin<S, Object> traversal) {
        // no Direction specified in the map, nothing to resolve
        if (!map.containsKey(direction))
            return;

        final Object value = map.get(direction);
        if (Objects.equals(token, value)) {
            if (traversal == null) {
                throw new IllegalArgumentException(String.format(
                        "option(%s) must be specified if it is used for %s", token, direction));
            }
            final Vertex vertex = resolveVertex(traverser, traversal);
            if (vertex == null)
                throw new IllegalArgumentException(String.format(
                        "Could not resolve vertex for option(%s)", token));
            map.put(direction, vertex.id());
        } else if (value instanceof Vertex) {
            // flatten Vertex down to its id
            map.put(direction, ((Vertex) value).id());
        }
    }

    /*
     * outV/inV traversal can either provide a Map (which we then use to search for a vertex) or it can provide a
     * Vertex directly.
     */
    protected Vertex resolveVertex(final Traverser.Admin<S> traverser, final Traversal.Admin<S, Object> traversal) {
        final Object o = TraversalUtil.apply(traverser, traversal);
        if (o instanceof Vertex)
            return (Vertex) o;
        else if (o instanceof Map) {
            final Map search = (Map) o;
            final Vertex v = IteratorUtils.findFirst(MergeVertexStep.searchVertices(getGraph(), search)).get();
            return tryAttachVertex(v);
        }
        return null;
    }

    @Override
    protected Iterator<Edge> flatMap(final Traverser.Admin<S> traverser) {
        final Map unresolvedMergeMap = materializeMap(traverser, mergeTraversal);
        validateMapInput(unresolvedMergeMap, false);

        /*
         * Create a copy of the unresolved map and attempt to resolve any Vertex references.
         */
        final Map mergeMap = resolveVertices(new LinkedHashMap<>(unresolvedMergeMap), traverser);

        Iterator<Edge> edges = searchEdges(mergeMap);

        if (onMatchTraversal != null) {

            edges = IteratorUtils.peek(edges, e -> {

                // if this was a start step the traverser is initialized with placeholder edge, so override that with
                // the matched Edge so that the option() traversal can operate on it properly
                if (isStart) traverser.set((S) e);

                // assume good input from GraphTraversal - folks might drop in a T here even though it is immutable
                final Map<String, ?> onMatchMap = materializeMap(traverser, onMatchTraversal);
                validateMapInput(onMatchMap, true);

                onMatchMap.forEach((key, value) -> {
                    // trigger callbacks for eventing - in this case, it's a EdgePropertyChangedEvent. if there's no
                    // registry/callbacks then just set the property
                    if (this.callbackRegistry != null && !callbackRegistry.getCallbacks().isEmpty()) {
                        final EventStrategy eventStrategy =
                                getTraversal().getStrategies().getStrategy(EventStrategy.class).get();
                        final Property<?> p = e.property(key);
                        final Property<Object> oldValue =
                                p.isPresent() ? eventStrategy.detach(e.property(key)) : null;
                        final Event.EdgePropertyChangedEvent vpce = new Event.EdgePropertyChangedEvent(eventStrategy.detach(e), oldValue, value);
                        this.callbackRegistry.getCallbacks().forEach(c -> c.accept(vpce));
                    }
                    e.property(key, value);
                });

            });

        }

        /*
         * Search produced results, and onMatch action will be triggered.
         */
        if (edges.hasNext()) {
            return edges;
        }

        // make sure we close the search traversal
        CloseableIterator.closeIterator(edges);

        final Map onCreateMap = onCreateMap(traverser, unresolvedMergeMap, mergeMap);

        // check for from/to vertices, which must be specified for the create action
        if (!onCreateMap.containsKey(Direction.OUT))
            throw new IllegalArgumentException("Out Vertex not specified - edge cannot be created");
        if (!onCreateMap.containsKey(Direction.IN))
            throw new IllegalArgumentException("In Vertex not specified - edge cannot be created");

        final Vertex fromV = resolveVertex(onCreateMap.get(Direction.OUT));
        final Vertex toV = resolveVertex(onCreateMap.get(Direction.IN));
        final String label = (String) onCreateMap.getOrDefault(T.label, Edge.DEFAULT_LABEL);

        final List<Object> properties = new ArrayList<>();

        // add property constraints
        for (final Map.Entry e : ((Map<?,?>) onCreateMap).entrySet()) {
            final Object k = e.getKey();
            if (k.equals(Direction.OUT) || k.equals(Direction.IN) || k.equals(T.label)) continue;
            properties.add(k);
            properties.add(e.getValue());
        }

        final Edge edge = fromV.addEdge(label, toV, properties.toArray());

        // trigger callbacks for eventing - in this case, it's a VertexAddedEvent
        if (this.callbackRegistry != null && !callbackRegistry.getCallbacks().isEmpty()) {
            final EventStrategy eventStrategy = getTraversal().getStrategies().getStrategy(EventStrategy.class).get();
            final Event.EdgeAddedEvent vae = new Event.EdgeAddedEvent(eventStrategy.detach(edge));
            this.callbackRegistry.getCallbacks().forEach(c -> c.accept(vae));
        }

        return IteratorUtils.of(edge);
    }

    protected Map onCreateMap(final Traverser.Admin<S> traverser, final Map unresolvedMergeMap, final Map mergeMap) {
        // no onCreateTraversal - use main mergeMap argument
        if (onCreateTraversal == null)
            return mergeMap;

        final Map onCreateMap = materializeMap(traverser, onCreateTraversal);
        validateMapInput(onCreateMap, false);

        /*
         * Now we need to merge the two maps - onCreate should inherit traits from mergeMap, and it is not allowed to
         * override values for any keys.
         */

        /*
         * We use the unresolved version here in case onCreateMap uses Merge tokens or Vertex objects for its values.
         */
        validateNoOverrides(unresolvedMergeMap, onCreateMap);

        /*
         * Use the resolved version here so that onCreateMap picks up fully resolved vertex arguments from the main
         * merge argument and so we don't re-resolve them below.
         */
        onCreateMap.putAll(mergeMap);

        /*
         * Do any final vertex resolution, for example if Merge tokens were used in option(onCreate) but not in the main
         * merge argument.
         */
        resolveVertices(onCreateMap, traverser);

        return onCreateMap;
    }

    /*
     * Resolve the argument for Direction.IN/OUT into a proper Vertex.
     */
    protected Vertex resolveVertex(final Object arg) {
        if (arg instanceof Vertex)
            return tryAttachVertex((Vertex) arg);

        final Iterator<Vertex> it = getGraph().vertices(arg);
        try {
            // otherwise use the arg as a vertex id
            if (!it.hasNext())
                throw new IllegalArgumentException(
                    String.format("Vertex id %s could not be found and edge could not be created", arg));
            return it.next();
        } finally {
            CloseableIterator.closeIterator(it);
        }
    }

    /**
     * Tries to attach a {@link Vertex} to its host {@link Graph} of the traversal. If the {@link Vertex} cannot be
     * found then an {@code IllegalArgumentException} is expected.
     */
    protected Vertex tryAttachVertex(final Vertex v) {
        if (v instanceof Attachable) {
            try {
                return ((Attachable<Vertex>) v).attach(Attachable.Method.get(getGraph()));
            } catch (IllegalStateException ise) {
                throw new IllegalArgumentException(String.format("%s could not be found and edge could not be created", v));
            }
        } else {
            return v;
        }
    }

}
