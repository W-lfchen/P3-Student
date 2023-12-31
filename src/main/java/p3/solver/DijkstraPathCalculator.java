package p3.solver;

import p3.graph.Edge;
import p3.graph.Graph;

import java.util.*;
import java.util.stream.Stream;

/**
 * Implementation of Dijkstra's algorithm, a single-source shortest path algorithm.
 *
 * @param <N> The type of the nodes in the graph.
 */
public class DijkstraPathCalculator<N> implements PathCalculator<N> {

    /**
     * Factory for creating new instances of {@link DijkstraPathCalculator}.
     */
    public static PathCalculator.Factory FACTORY = DijkstraPathCalculator::new;

    /**
     * Tje graph to calculate paths in.
     */
    protected final Graph<N> graph;

    /**
     * The distance from the start node to each node in the graph.
     */
    protected final Map<N, Integer> distances = new HashMap<>();

    /**
     * The predecessor of each node in the graph along the shortest path to the start node.
     */
    protected final Map<N, N> predecessors = new HashMap<>();

    /**
     * The set of nodes that have not yet been visited.
     */
    protected final Set<N> remainingNodes = new HashSet<>();

    /**
     * Construct a new {@link DijkstraPathCalculator} for the given graph.
     *
     * @param graph the graph to calculate paths in.
     */
    public DijkstraPathCalculator(Graph<N> graph) {
        this.graph = graph;
    }

    /**
     * Calculate the shortest path between two given nodes, {@code start} and {@code end}, using Dijkstra's algorithm.
     *
     * <p>
     * This method calculates the shortest path from {@code start} to all other nodes and saves the results
     * to {@link #distances} and {@link #predecessors}.
     * </p>
     *
     * @param start the start node, first node in the returned list
     * @param end   the end node, last node in the returned list
     * @return a list of nodes, from {@code start} to {@code end}, in the order they need to be traversed to get the
     * shortest path between those two nodes
     */
    @Override
    public List<N> calculatePath(final N start, final N end) {
        // initialise state
        init(start);
        // the algorithm is done if no nodes remain
        while (!remainingNodes.isEmpty()) {
            // get the current working node and immediately remove it from remainingNodes
            N node = extractMin();
            remainingNodes.remove(node);
            // relax with each adjacent edge
            // it should be noted that the graph is undirected, but this should not cause issues
            graph.getAdjacentEdges(node).forEach(edge -> relax(edge.a(), edge.b(), edge));
        }
        // now that the graph and maps are in the required state, reconstruct the path
        return reconstructPath(start, end);
    }

    /**
     * Initializes the {@link #distances} and {@link #predecessors} maps as well as the {@link #remainingNodes} set, i.e., resets and repopulates them with
     * default values. The default value for {@link #distances} is {@code 0} for the start node and {@link Integer#MAX_VALUE} for every other node
     * and the default value for {@link #predecessors} is {@code null} for every node.
     *
     * @param start the start node
     */
    protected void init(N start) {
        // the clear operation should be supported for everything, wipe the maps and set
        distances.clear();
        predecessors.clear();
        remainingNodes.clear();
        // the starting distance is the maximum value so that it may be overridden
        // except for the start node, which is 0 units away from itself
        graph.getNodes().forEach(node -> distances.put(node, node.equals(start) ? 0 : Integer.MAX_VALUE));
        // no predecessor has been determined at this point
        graph.getNodes().forEach(node -> predecessors.put(node, null));
        // all nodes remain
        remainingNodes.addAll(graph.getNodes());
    }

    /**
     * Determines the next node from the set of remaining nodes that should be visited.
     * <p> This implementation returns the node with the minimal weight in the {@link #remainingNodes} set.
     *
     * @return the next unprocessed node with minimal weight
     */
    protected N extractMin() {
        // reduce them by finding the smallest element, according to the element's entry in distances
        // the exception should never be thrown, but accessing the element using Optional#get without any checks is something one should not do
        return remainingNodes.stream().reduce((x, y) -> distances.get(x) < distances.get(y) ? x : y).orElseThrow(() -> new IllegalStateException("RemainingNodes is empty!"));
    }

    /**
     * Updates the {@link #distances} and {@link #predecessors} maps if a shorter path between {@code from} and
     * {@code to} is found. If no shorter path is found, the maps remain unchanged.
     *
     * @param from the node that is used to reach {@code to}
     * @param to   the target node for this update
     * @param edge the edge between {@code from} and {@code to}.
     */
    protected void relax(N from, N to, Edge<N> edge) {
        // do nothing if the distance doesn't need to be adjusted
        if (distances.get(from) + edge.weight() < distances.get(to)) {
            // otherwise, adjust the distance value
            distances.put(to, distances.get(from) + edge.weight());
            // don't forget to set the predecessor accordingly
            predecessors.put(to, from);
        }
    }

    /**
     * Reconstructs the shortest path from {@code start} to {@code end} by using the {@link #predecessors} map.
     * <p> The returned path contains {@code start} as the first element and {@code end} as the last element.
     *
     * @param start the start node
     * @param end   the end node
     * @return a list of nodes in the order they need to be traversed to get the shortest path from the start node to the end node.
     */
    protected List<N> reconstructPath(N start, N end) {
        // create a stream that begins with end and contains every predecessor until start, then append start
        // the result of this is inserted into a new ArrayList, with everything being added at the first index in order to reverse the order
        // that way, the list begins with start and ends with end, then return an unmodifiable view of it
        // the combiner (3rd parameter of collect) should never be used, it is merely there as a requirement
        return Collections.unmodifiableList(Stream.concat(Stream.iterate(end, x -> !x.equals(start), predecessors::get), Stream.of(start)).collect(ArrayList::new, (list, node) -> list.add(0, node), ArrayList::addAll));
    }
}
