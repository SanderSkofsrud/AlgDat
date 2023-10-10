import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Represents an edge in the flow graph.
 */
class Edge {
  int from, to;
  int capacity;
  int flow;
  Edge reverse;  // Residual edge

  /**
   * Constructor to initialize an edge.
   * @param from Starting node of the edge.
   * @param to Ending node of the edge.
   * @param capacity Capacity of the edge.
   */
  public Edge(int from, int to, int capacity) {
    this.from = from;
    this.to = to;
    this.capacity = capacity;
    this.flow = 0;
  }

  /**
   * Returns the remaining capacity of the edge.
   * @return Remaining capacity.
   */
  public int remainingCapacity() {
    return capacity - flow;
  }

  /**
   * Augments the flow of the edge.
   * @param bottleNeck The value to augment the flow by.
   */
  public void augment(int bottleNeck) {
    flow += bottleNeck;
    reverse.flow -= bottleNeck;
  }
}

/**
 * Represents a flow graph with nodes and edges.
 */
class FlowGraph {
  private final int n;
  private List<List<Edge>> adjList;

  /**
   * Constructor to initialize a flow graph with n nodes.
   * @param n Number of nodes in the graph.
   */
  public FlowGraph(int n) {
    this.n = n;
    adjList = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      adjList.add(new ArrayList<>());
    }
  }

  /**
   * Adds an edge to the flow graph.
   * Time complexity: O(1)
   * @param from Starting node of the edge.
   * @param to Ending node of the edge.
   * @param capacity Capacity of the edge.
   */
  public void addEdge(int from, int to, int capacity) {
    Edge edge = new Edge(from, to, capacity);
    Edge reverseEdge = new Edge(to, from, 0);  // Residual edge with 0 capacity

    edge.reverse = reverseEdge;
    reverseEdge.reverse = edge;

    adjList.get(from).add(edge);
    adjList.get(to).add(reverseEdge);  // Add the residual edge
  }

  /**
   * Returns the edges connected to a given node.
   * Time complexity: O(1)
   * @param node The node for which edges are to be fetched.
   * @return List of edges connected to the node.
   */
  public List<Edge> getEdges(int node) {
    return adjList.get(node);
  }

  /**
   * Performs BFS on the flow graph to find a path from source to sink.
   * Time complexity: O(E) where E is the number of edges in the graph.
   * @param source The source node.
   * @param sink The sink node.
   * @return List of edges representing the path, or null if no path is found.
   */
  public List<Edge> bfs(int source, int sink) {
    boolean[] visited = new boolean[n];
    Edge[] prev = new Edge[n];  // To store the path

    Queue<Integer> queue = new LinkedList<>();
    queue.offer(source);
    visited[source] = true;

    while (!queue.isEmpty()) {
      int node = queue.poll();

      for (Edge edge : getEdges(node)) {
        if (!visited[edge.to] && edge.remainingCapacity() > 0) {
          visited[edge.to] = true;
          prev[edge.to] = edge;

          if (edge.to == sink) {  // Found a path
            List<Edge> path = new LinkedList<>();
            while (edge != null) {
              path.add(edge);
              edge = prev[edge.from];
            }
            Collections.reverse(path);
            return path;
          }

          queue.offer(edge.to);
        }
      }
    }

    return null;  // No path found
  }
}

/**
 * Implements the Edmonds-Karp algorithm for finding maximum flow in a flow graph.
 */
public class EdmondsKarp {
  private final FlowGraph graph;

  /**
   * Constructor to initialize the Edmonds-Karp algorithm with a given flow graph.
   * @param graph The flow graph on which the algorithm will be applied.
   */
  public EdmondsKarp(FlowGraph graph) {
    this.graph = graph;
  }

  /**
   * Computes the maximum flow from source to sink using the Edmonds-Karp algorithm.
   * Time complexity: O(V * E^2) where V is the number of vertices and E is the number of edges.
   * @param source The source node.
   * @param sink The sink node.
   * @return Maximum flow value.
   */
  public int getMaxFlow(int source, int sink) {
    int maxFlow = 0;
    int pathsCount = 0;

    System.out.println("Maksimum flyt fra " + source + " til " + sink + " med Edmond-Karp");
    System.out.println("Økning : Flytøkende vei");

    List<Edge> path;
    while ((path = graph.bfs(source, sink)) != null) {
      int bottleneck = findBottleneck(path);
      System.out.print(bottleneck + " ");
      for (Edge edge : path) {
        System.out.print(edge.from + " ");
      }
      System.out.println(sink);

      augmentFlow(path, bottleneck);
      maxFlow += bottleneck;
      pathsCount++;
    }

    System.out.println("Maksimal flyt ble " + maxFlow);
    System.out.println("Her ble det brukt " + pathsCount + " flytøkende veier.");
    return maxFlow;
  }

  /**
   * Finds the bottleneck capacity of a given path.
   * Time complexity: O(P) where P is the length of the path.
   * @param path The path for which the bottleneck is to be found.
   * @return Bottleneck capacity value.
   */
  private int findBottleneck(List<Edge> path) {
    int bottleneck = Integer.MAX_VALUE;
    for (Edge edge : path) {
      bottleneck = Math.min(bottleneck, edge.remainingCapacity());
    }
    return bottleneck;
  }

  /**
   * Augments the flow along a given path by a specified bottleneck value.
   * Time complexity: O(P) where P is the length of the path.
   * @param path The path along which the flow is to be augmented.
   * @param bottleneck The value by which the flow is to be augmented.
   */
  private void augmentFlow(List<Edge> path, int bottleneck) {
    for (Edge edge : path) {
      edge.augment(bottleneck);
    }
  }

  /**
   * Main method to execute the Edmonds-Karp algorithm.
   * Note: When compiling using 'javac EdmondsKarp.java', you may have to add the encoding option with UTF-8.
   * 'javac -encoding UTF-8 EdmondsKarp.java'
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    Scanner consoleScanner = new Scanner(System.in);
    System.out.print("Enter the filename: ");
    String filename = consoleScanner.nextLine();

    FlowGraph graph = null;

    try {
      File file = new File(filename);
      Scanner fileScanner = new Scanner(file);

      int nodes = fileScanner.nextInt();
      int edges = fileScanner.nextInt();
      graph = new FlowGraph(nodes);

      for (int i = 0; i < edges; i++) {
        int from = fileScanner.nextInt();
        int to = fileScanner.nextInt();
        int capacity = fileScanner.nextInt();
        graph.addEdge(from, to, capacity);
      }

      fileScanner.close();

    } catch (FileNotFoundException e) {
      System.out.println("File not found.");
      return;
    }

    System.out.print("Enter source node: ");
    int source = consoleScanner.nextInt();

    System.out.print("Enter sink node: ");
    int sink = consoleScanner.nextInt();

    EdmondsKarp ek = new EdmondsKarp(graph);
    ek.getMaxFlow(source, sink);
  }
}
