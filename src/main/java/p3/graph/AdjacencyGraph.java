package p3.graph;

import p3.SetUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An implementation of an immutable {@link Graph} that uses an {@link AdjacencyMatrix} to store the graph.
 *
 * @param <N> the type of the nodes in this graph.
 */
public class AdjacencyGraph<N> implements Graph<N> {

    /**
     * The adjacency matrix that stores the graph.
     */
    private final AdjacencyMatrix matrix;

    /**
     * A map from nodes to their indices in the adjacency matrix.
     * Every node in the graph is mapped to a distinct index in the range [0, {@link #matrix}.size() -1].
     * This map is the inverse of {@link #indexNodes}.
     */
    private final Map<N, Integer> nodeIndices = new HashMap<>();

    /**
     * A map from indices in the adjacency matrix to the nodes they represent.
     * Every index in the range [0, {@link #matrix}.size() -1] is mapped to a distinct node in the graph.
     * This map is the inverse of {@link #nodeIndices}.
     */
    private final Map<Integer, N> indexNodes = new HashMap<>();

    /**
     * The nodes in this graph.
     */
    private final Set<N> nodes;

    /**
     * The edges in this graph.
     */
    private final Set<Edge<N>> edges;

    /**
     * Constructs a new {@link AdjacencyGraph} with the given nodes and edges.
     *
     * @param nodes the nodes in the graph.
     * @param edges the edges in the graph.
     */
    public AdjacencyGraph(Set<N> nodes, Set<Edge<N>> edges) {
        matrix = new AdjacencyMatrix(nodes.size());
        this.nodes = SetUtils.immutableCopyOf(nodes);
        this.edges = SetUtils.immutableCopyOf(edges);

        // the two maps are already initialised which doesn't make a lot of sense but this also won't be changed
        // therefore, the maps just need to be filled with the correct entries

        // the sole purpose of using this class here instead of int is to increment the value in a lambda expression.
        // variables in lambdas need to be final/effectively final, but the state of a referenced object may change.
        AtomicInteger index = new AtomicInteger(0);
        // insert a random node and an index in nodeIndices. The index is guaranteed to be unique due to the increment and starts at 0
        nodes.forEach(node -> nodeIndices.put(node, index.getAndIncrement()));
        // since this is supposed to be the exact inverse, construct indexNodes using nodeIndices, by getting a nodes' index
        nodes.forEach(node -> indexNodes.put(nodeIndices.get(node), node));
        // add each edge to the adjacency matrix, get the indices of the nodes using the newly created map
        edges.forEach(edge -> matrix.addEdge(nodeIndices.get(edge.a()), nodeIndices.get(edge.b()), edge.weight()));
    }

    @Override
    public Set<N> getNodes() {
        return nodes;
    }

    @Override
    public Set<Edge<N>> getEdges() {
        return edges;
    }

    @Override
    public Set<Edge<N>> getAdjacentEdges(N node) {
        // get all indices of the nodes that are connected to node by filtering a stream of all indices
        // then create a new edge from node to the node that maps to index with the corresponding weight for each index
        // then collect the edges into an unmodifiable set
        return IntStream.range(0, nodes.size()).filter(index -> matrix.getAdjacent(nodeIndices.get(node))[index] != 0).mapToObj(index -> new EdgeImpl<>(node, indexNodes.get(index), matrix.getWeight(index, nodeIndices.get(node)))).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public MutableGraph<N> toMutableGraph() {
        return MutableGraph.of(nodes, edges);
    }

    @Override
    public Graph<N> toGraph() {
        return this;
    }
}
