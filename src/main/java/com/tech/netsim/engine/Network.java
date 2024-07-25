
package com.tech.netsim.engine;

import java.util.*;

/**
 * Represents the overall computer network.
 */
public class Network {

    public int getDefaultNetworkLatency() {
        return defaultNetworkLatency;
    }

    public Map<String, List<NodeWeigth>> getConnections() {
        return connections;
    }

    /**
     * Default latency for the network
     */
    private int defaultNetworkLatency = 0;

    /**
     * The network graph representation. It's represented by an adjacency list. For each node it lists what are
     * the connected nodes and the weight associated to each one.
     */
    Map<String, List<NodeWeigth>> connections = new HashMap<String, List<NodeWeigth>>();

    //----------------------------------------------------------------------------------------------------------------

    /**
     * Default constructor -- publicly disabled since this is a factory class.
     *
     * @param latency Default latency for the network.
     */
    protected Network(int latency) {
        this.defaultNetworkLatency = latency;
    }

    /**
     * Factory method for creating a Network is a certain default latency.
     *
     * @param latency (ms)
     * @return the Network object
     */
    public static Network createWithLatency(int latency) {
        return new Network(latency);
    }

    //----------------------------------------------------------------------------------------------------------------

    /**
     * Connects two nodes on the network identified by they respective ids. The default network latency is used.
     *
     * @param idA First node.
     * @param idB Second node.
     */
    public synchronized void connect(String idA, String idB) {
        connect(idA, idB, defaultNetworkLatency);
    }

    /**
     * Connects two nodes on the network identified by they respective ids. The latency between the two nodes
     * needs to be specified.
     *
     * @param idA     First node.
     * @param idB     Second node.
     * @param latency The latency (in msec)
     */
    public synchronized void connect(String idA, String idB, int latency) {
        // Adjacency lists are symmetrical. If there's a NodeWeigth from A to B, there's also one from B to A
        addEdge(idA, idB, latency);
        addEdge(idB, idA, latency);
    }

    /**
     * Update the NodeWeigth table (graph) by adding a single NodeWeigth from firstEdge to secondEdge.
     *
     * @param firstEdge     First edge
     * @param secondEdge     Second edge
     * @param latency The latency
     */
    private void addEdge(String firstEdge, String secondEdge, int latency) {
        List nodeEdges = connections.get(firstEdge);
        if (nodeEdges == null) {
            nodeEdges = new ArrayList<NodeWeigth>();
        }

        nodeEdges.add(new NodeWeigth(secondEdge, latency));
        connections.put(firstEdge, nodeEdges);
    }

    public synchronized NetworkPath sendPacket(String idA, String idB) {
        //-------------------------------------------------------
        // Implementation of Dijkstra's algorithm (basic version)
        //-------------------------------------------------------

        // Collection of unvisited nodes (initially all nodes)
        Set<String> unvisited = new HashSet<String>();

        // Tentative distances to target from each node (INFINITE for all except origin)
        Map<String, Integer> dist = new HashMap<String, Integer>();

        // Collection that tracks nodes and the minimal path used to reach them
        Map<String, String> path = new HashMap<String, String>();

        // Initialize distances, unvisited nodes, and path tracking
        for (String node : connections.keySet()) {
            dist.put(node, node.equals(idA) ? 0 : Integer.MAX_VALUE);
            unvisited.add(node);
            path.put(node, null);
        }

        // For all unvisited nodes, proceed until we have nothing else to do
        // Note that this may exit if the target node is not found (disconnected graph). foundTarget tracks that.
        boolean foundTarget = false;
        while (!unvisited.isEmpty()) {
            // Find minimal node
            String minNode = findMinNode(unvisited, dist);
            unvisited.remove(minNode);

            // If we have found the target node we can exit.
            if (minNode.equals(idB)) {
                // We have only really found the target node if the distance is finite. If not, it's a disconnected graph.
                if (dist.get(minNode) != Integer.MAX_VALUE) {
                    foundTarget = true;
                }
                break;
            }

            // Get list of neighbours. For each neighbours calculate the estimated distance through his path
            // I.e., going from the current minimal node plus the weigth of getting to the neighbour.
            // It the cost of going through there is less than any previous estimated cost, update its cost
            List<NodeWeigth> neighbours = connections.get(minNode);
            for (NodeWeigth v : neighbours) {
                if (unvisited.contains(v.node)) {
                    int distThroughNode = dist.get(minNode) + v.weight;
                    if (distThroughNode < dist.get(v.node)) {
                        dist.put(v.node, distThroughNode);
                        path.put(v.node, minNode);                  // Path to v is through minNode
                    }
                }
            }
        }

        // Reconstruct the minimal path using the tracking that has been done (only if a path has been found)
        NetworkPath minPath = new NetworkPath();

        if (foundTarget) {
            String current = idB;

            minPath.addFirst(current);
            minPath.setTime(dist.get(idB));

            while (true) {
                String next = path.get(current);
                if (next == null)
                    break;

                minPath.addFirst(next);
                current = next;
            }
        }

        return minPath;
    }

    /**
     * Given a set of unvisited nodes and a map of corresponding distances, find the unvisited node that has
     * the smallest estimated distance. Utility method for being used with sendPacket/Dijkstra's algorithm.
     *
     * @param unvisitedNodes Nodes that have not yet been visited.
     * @param distances      Complete list of estimated distances in the graph (visited and unvisited nodes).
     * @return The id of the node with the smallest estimated distance.
     */
    private String findMinNode(Set<String> unvisitedNodes, Map<String, Integer> distances) {
        String minNode = null;
        int minDist = Integer.MAX_VALUE;

        for (String node : unvisitedNodes) {
            int d = distances.get(node);
            if (d <= minDist) {
                minDist = d;
                minNode = node;
            }
        }

        return minNode;
    }

    //----------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        ArrayList<String> nodes = new ArrayList<String>();

        for (String node : connections.keySet()) {
            for (NodeWeigth c : connections.get(node)) {
                nodes.add(String.format("[%s,%s,%d]", node, c.node, c.weight));
            }
        }
        Collections.sort(nodes);

        return "[" + String.join(",", nodes) + "]";
    }

    //----------------------------------------------------------------------------------------------------------------

    /**
     * Auxiliary class (structure) representing a pair of (Node, Weight).
     */
    private class NodeWeigth {
        public NodeWeigth(String node, int weight) {
            this.node = node;
            this.weight = weight;
        }

        public String node;
        public int weight;
    }
}
