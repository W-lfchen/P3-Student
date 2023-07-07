package p3.solver;

import p3.graph.Edge;
import p3.graph.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of Kruskal's algorithm, a minimum spanning tree algorithm.
 *
 * @param <N> The type of the nodes in the graph.
 */
public class KruskalMSTCalculator<N> implements MSTCalculator<N> {

    /**
     * Factory for creating new instances of {@link KruskalMSTCalculator}.
     */
    public static MSTCalculator.Factory FACTORY = KruskalMSTCalculator::new;

    /**
     * The graph to calculate the MST for.
     */

    protected final Graph<N> graph;

    /**
     * The edges in the MST.
     */
    protected final Set<Edge<N>> mstEdges;

    /**
     * The groups of nodes in the MST.
     * <p> Each group is represented by a set of nodes. Initially, each node is in its own group. </p>
     * <p> When two nodes are in the same groups, they are in the same MST which is created by {@link #mstEdges}. </p>
     * <p> Every node is in exactly one group. </p>
     */
    protected final List<Set<N>> mstGroups;

    /**
     * Construct a new {@link KruskalMSTCalculator} for the given graph.
     *
     * @param graph the graph to calculate the MST for.
     */
    public KruskalMSTCalculator(Graph<N> graph) {
        this.graph = graph;
        this.mstEdges = new HashSet<>();
        this.mstGroups = new ArrayList<>();
    }

    @Override
    public Graph<N> calculateMST() {
        // initialise state
        init();
        // add all edges that are accepted, in ascending order
        // they are collected into a list to ensure that the order is correct and to stop IntelliJ from being annoying
        mstEdges.addAll(graph.getEdges().stream().sorted(Edge::compareTo).filter(this::acceptEdge).toList());
        // return a graph consisting of the nodes and the edges that have been determined to create the mst
        return Graph.of(graph.getNodes(), mstEdges);
    }

    /**
     * Initializes the {@link #mstEdges} and {@link #mstGroups} with their default values.
     * <p> Initially, {@link #mstEdges} is empty and {@link #mstGroups} contains a set for each node in the graph.
     */
    protected void init() {
        // the operations should be supported by the respective collections
        mstEdges.clear();
        mstGroups.clear();
        // create a new HashSet for each node that contains the node and nothing else
        graph.getNodes().forEach(node -> mstGroups.add(new HashSet<>(Set.of(node))));
    }

    /**
     * Processes an edge during Kruskal's algorithm.
     * <p> If the edge's nodes are in the same MST (group), the edge is skipped.
     * <p> If the edge's nodes are in different MSTs (groups), the groups are merged via the {@link #joinGroups(int, int)} method.
     *
     * @param edge The edge to process.
     * @return {@code true} if the edge was accepted and the two MST's were merged,
     * {@code false} if it was skipped.
     */
    protected boolean acceptEdge(Edge<N> edge) {
        // filter the sets so that only those that contain node a remain, this should be at most one set
        // then simply check whether the set also contains node b and return false if it does
        if (mstGroups.stream().filter(group -> group.contains(edge.a())).anyMatch(group -> group.contains(edge.b())))
            return false;
        // otherwise, join the sets and return true
        // to join the sets, first attempt to find the indices of the set containing the nodes a and b, respectively
        // the process is the same for both, the sets in mstGroups are filtered so that they need to contain the node
        // this should always yield exactly one element, and the exception should never be thrown
        joinGroups(mstGroups.indexOf(mstGroups.stream().filter(group -> group.contains(edge.a())).findFirst().orElseThrow(() -> new IllegalStateException("Can't find a group that contains node a"))), mstGroups.indexOf(mstGroups.stream().filter(group -> group.contains(edge.b())).findFirst().orElseThrow(() -> new IllegalStateException("Can't find a group that contains node b"))));
        // it should be noted that during this entire exercise it is assumed that all the input and states are valid
        // a lot more checks could be run here in order to see whether this is the case
        return true;
    }

    /**
     * Joins two sets in the list of all MST Groups.
     * <p> After joining the larger set will additionally contain all elements of the smaller set and
     * the smaller set will be removed from the list.
     *
     * @param aIndex The index of the first set to join.
     * @param bIndex The index of the second set to join.
     */
    protected void joinGroups(int aIndex, int bIndex) {
        // the sets in mstGroups are modifiable, therefore simply add everything from the smaller set to the bigger one
        // don't forget the call of List#remove which returns the removed element
        mstGroups.get(mstGroups.get(aIndex).size() > mstGroups.get(bIndex).size() ? aIndex : bIndex).addAll(mstGroups.remove(mstGroups.get(aIndex).size() > mstGroups.get(bIndex).size() ? bIndex : aIndex));
    }
}
