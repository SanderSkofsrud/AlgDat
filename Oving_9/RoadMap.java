import java.util.*;
import java.io.*;
import java.util.stream.Collectors;


public class RoadMap {

  private static Graph graph;
  private static Map<Integer, POI> pois;

  static {
    loadGraphData();
    loadPOIs();
  }

  public static void main(String[] args) {
    performTests();
  }

  private static void loadGraphData() {
    try {
      GraphBuilder builder = new GraphBuilder();
      graph = builder.buildGraph("norden/noder.txt", "norden/kanter.txt");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void loadPOIs() {
    try {
      List<POI> poisList = new FileParser().readPOIs("norden/interessepkt.txt");
      pois = poisList.stream()
              .collect(Collectors.toMap(POI::getNodeId, poi -> poi, (existing, replacement) -> existing));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void performTests() {
    RoadMap roadMap = new RoadMap();

    // Tests for shortest path
    GraphTest.testShortestPath(roadMap, graph, 2800567, 7705656);
    GraphTest.testShortestPath(roadMap, graph, 7705656, 2800567);

    //Tests for nearest POIs
    GraphTest.testNearestPOIs(graph, pois, 3005466, 5, 16); // Replace with actual node ID and number of POIs
  }


//  public static void main(String[] args) {
//    try {
//      // Load graph data
//      GraphBuilder builder = new GraphBuilder();
//      Graph graph = builder.buildGraph("norden/noder.txt", "norden/kanter.txt");
//
//      // Load POIs
//      List<POI> poisList = new FileParser().readPOIs("norden/interessepkt.txt");
//      Map<Integer, POI> pois = poisList.stream()
//              .collect(Collectors.toMap(POI::getNodeId, poi -> poi, (existing, replacement) -> existing));
//
//
//      // Create an instance of RoadMap for accessing its inner classes
//      RoadMap roadMap = new RoadMap();
//
//      // Test cases
//      GraphTest.testShortestPath(roadMap, graph, 2800567, 7705656);
//      GraphTest.testShortestPath(roadMap, graph, 7705656, 2800567);
//      //GraphTest.testNearestPOIs(graph, pois, 123456, 5); // Replace with actual node ID and number of POIs
//
//      // Add more tests as needed
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }

  public static class Node {
    private int id;
    private double latitude;
    private double longitude;

    public Node(int id, double latitude, double longitude) {
      this.id = id;
      this.latitude = latitude;
      this.longitude = longitude;
    }

    public int getId() {
      return id;
    }

    public double getLatitude() {
      return latitude;
    }

    public double getLongitude() {
      return longitude;
    }

    @Override
    public String toString() {
      return "Node{" +
              "id=" + id +
              ", latitude=" + latitude +
              ", longitude=" + longitude +
              '}';
    }
  }

  public static class Edge {
    private int fromNode;
    private int toNode;
    private int driveTime; // In 1/100 seconds
    private double length; // In meters
    private int speedLimit; // In km/h

    public Edge(int fromNode, int toNode, int driveTime, double length, int speedLimit) {
      this.fromNode = fromNode;
      this.toNode = toNode;
      this.driveTime = driveTime;
      this.length = length;
      this.speedLimit = speedLimit;
    }

    public int getFromNode() {
      return fromNode;
    }

    public int getToNode() {
      return toNode;
    }

    public int getDriveTime() {
      return driveTime;
    }

    public double getLength() {
      return length;
    }

    public int getSpeedLimit() {
      return speedLimit;
    }

    @Override
    public String toString() {
      return "Edge{" +
              "fromNode=" + fromNode +
              ", toNode=" + toNode +
              ", driveTime=" + driveTime +
              ", length=" + length +
              ", speedLimit=" + speedLimit +
              '}';
    }
  }

  public static class POI {
    private int nodeId;
    private int code;
    private String name;

    public POI(int nodeId, int code, String name) {
      this.nodeId = nodeId;
      this.code = code;
      this.name = name;
    }

    // Getters and Setters
    public int getNodeId() { return nodeId; }
    public int getCode() { return code; }
    public String getName() { return name; }

    public Map<Integer, POI> readPOIs(String filePath) throws IOException {
      Map<Integer, POI> pois = new HashMap<>();
      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        br.readLine(); // Skip the header line if there's any
        while ((line = br.readLine()) != null) {
          String[] parts = line.split("\t");
          int nodeId = Integer.parseInt(parts[0]);
          int code = Integer.parseInt(parts[1]);
          String name = parts[2].replaceAll("^\"|\"$", ""); // Remove quotes from name
          pois.put(nodeId, new POI(nodeId, code, name));
        }
      }
      return pois;
    }
  }

  public static class Graph {
    private Map<Integer, Node> nodes;
    private Map<Integer, List<Edge>> adjacencyList;

    public Graph() {
      this.nodes = new HashMap<>();
      this.adjacencyList = new HashMap<>();
    }

    public void addNode(Node node) {
      nodes.put(node.getId(), node);
      if (!adjacencyList.containsKey(node.getId())) {
        adjacencyList.put(node.getId(), new LinkedList<>());
      }
    }

    public void addEdge(Edge edge) {
      adjacencyList.get(edge.getFromNode()).add(edge);
      // If the graph is undirected, add the reverse edge as well:
      // adjacencyList.get(edge.getToNode()).add(new Edge(edge.getToNode(), edge.getFromNode(), edge.getDriveTime(), edge.getLength(), edge.getSpeedLimit()));
    }

    public Node getNode(int id) {
      return nodes.get(id);
    }

    public List<Edge> getEdgesFromNode(int nodeId) {
      return adjacencyList.getOrDefault(nodeId, new LinkedList<>());
    }

    public Map<Integer, Node> getNodes() {
      return nodes;
    }

    public Map<Integer, List<Edge>> getAdjacencyList() {
      return adjacencyList;
    }

    @Override
    public String toString() {
      return "Graph{" +
              "nodes=" + nodes +
              ", adjacencyList=" + adjacencyList +
              '}';
    }
  }

  public static class FileParser {

    public static List<Node> readNodes(String filePath) throws IOException {
      List<Node> nodes = new ArrayList<>();
      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        br.readLine(); // Skip the first line (total count)
        String line;
        while ((line = br.readLine()) != null) {
          String[] parts = line.split("\\s+");
          int id = Integer.parseInt(parts[0]);
          double latitude = Double.parseDouble(parts[1]);
          double longitude = Double.parseDouble(parts[2]);
          nodes.add(new Node(id, latitude, longitude));
        }
      }
      return nodes;
    }

    public static List<Edge> readEdges(String filePath) throws IOException {
      List<Edge> edges = new ArrayList<>();
      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        br.readLine(); // Skip the first line
        String line;
        while ((line = br.readLine()) != null) {
          String[] parts = line.split("\\t");
          int fromNode = Integer.parseInt(parts[0]);
          int toNode = Integer.parseInt(parts[1]);
          int driveTime = Integer.parseInt(parts[2]);
          double length = Double.parseDouble(parts[3]);
          int speedLimit = Integer.parseInt(parts[4]);
          edges.add(new Edge(fromNode, toNode, driveTime, length, speedLimit));
        }
      }
      return edges;
    }

    public List<POI> readPOIs(String filePath) throws IOException {
      List<POI> pois = new ArrayList<>();
      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        br.readLine(); // Skip the first line
        String line;
        while ((line = br.readLine()) != null) {
          String[] parts = line.split("\\t");
          int nodeId = Integer.parseInt(parts[0]);
          int code = Integer.parseInt(parts[1]);
          String name = parts[2].replace("\"", ""); // Removing quotes
          pois.add(new POI(nodeId, code, name));
        }
      }
      return pois;
    }
  }

  public static class GraphBuilder {

    public Graph buildGraph(String nodeFilePath, String edgeFilePath) throws IOException {
      Graph graph = new Graph();
      List<Node> nodes = FileParser.readNodes(nodeFilePath);
      List<Edge> edges = FileParser.readEdges(edgeFilePath);

      for (Node node : nodes) {
        graph.addNode(node);
      }

      for (Edge edge : edges) {
        graph.addEdge(edge);
      }

      return graph;
    }
  }

  public class DijkstraAlgorithm {
    public static Map<Integer, Double> shortestPath(Graph graph, int startNodeId) {
      Map<Integer, Double> distances = new HashMap<>();
      distances.put(startNodeId, 0.0);

      PriorityQueue<NodeDistancePair> queue = new PriorityQueue<>();
      queue.add(new NodeDistancePair(startNodeId, 0.0));

      while (!queue.isEmpty()) {
        NodeDistancePair pair = queue.poll();
        int nodeId = pair.getNodeId();
        double currentDistance = pair.getDistance();

        if (currentDistance > distances.getOrDefault(nodeId, Double.MAX_VALUE)) {
          continue;
        }

        for (Edge edge : graph.getEdgesFromNode(nodeId)) {
          int neighbor = edge.getToNode();
          double newDistance = currentDistance + edge.getDriveTime();
          if (newDistance < distances.getOrDefault(neighbor, Double.MAX_VALUE)) {
            distances.put(neighbor, newDistance);
            queue.add(new NodeDistancePair(neighbor, newDistance));
          }
        }
      }

      return distances;
    }

    private static class NodeDistancePair implements Comparable<NodeDistancePair> {
      private int nodeId;
      private double distance;

      public NodeDistancePair(int nodeId, double distance) {
        this.nodeId = nodeId;
        this.distance = distance;
      }

      public int getNodeId() {
        return nodeId;
      }

      public double getDistance() {
        return distance;
      }

      @Override
      public int compareTo(NodeDistancePair other) {
        return Double.compare(this.distance, other.distance);
      }
    }
  }

  public class ALTAlgorithm {

    private List<Integer> landmarks;
    private Map<Integer, Map<Integer, Double>> landmarkDistances;

    public ALTAlgorithm(Graph graph, int numLandmarks) {
      this.landmarks = chooseLandmarks(graph, Math.min(numLandmarks, graph.getNodes().size() / 10)); // Limit the number of landmarks
      this.landmarkDistances = precomputeLandmarkDistances(graph, landmarks);
    }

    private List<Integer> chooseLandmarks(Graph graph, int numLandmarks) {
      // Choose 'numLandmarks' random nodes as landmarks
      List<Integer> allNodeIds = new ArrayList<>(graph.getNodes().keySet());
      Collections.shuffle(allNodeIds);
      return allNodeIds.subList(0, numLandmarks);
    }

    private Map<Integer, Map<Integer, Double>> precomputeLandmarkDistances(Graph graph, List<Integer> landmarks) {
      Map<Integer, Map<Integer, Double>> distances = new HashMap<>();
      for (int landmark : landmarks) {
        distances.put(landmark, DijkstraAlgorithm.shortestPath(graph, landmark));
      }
      return distances;
    }

    public double shortestPath(Graph graph, int startNodeId, int targetNodeId) {
      // Initialize the data structures
      Map<Integer, Double> gScore = new HashMap<>();
      Map<Integer, Double> fScore = new HashMap<>();
      Set<Integer> closedSet = new HashSet<>();
      Queue<NodeDistancePair> openSet = new PriorityQueue<>();

      // Initialize scores and add the start node to the open set
      graph.getNodes().forEach((nodeId, node) -> {
        gScore.put(nodeId, Double.MAX_VALUE);
        fScore.put(nodeId, Double.MAX_VALUE);
      });
      gScore.put(startNodeId, 0.0);
      fScore.put(startNodeId, landmarkHeuristic(startNodeId, targetNodeId));
      openSet.add(new NodeDistancePair(startNodeId, fScore.get(startNodeId)));

      while (!openSet.isEmpty()) {
        NodeDistancePair currentPair = openSet.poll();
        int currentNode = currentPair.getNodeId();
        if (currentNode == targetNodeId) {
          return gScore.get(targetNodeId);
        }

        closedSet.add(currentNode);

        for (Edge edge : graph.getAdjacencyList().get(currentNode)) {
          int neighbor = edge.getToNode();
          if (closedSet.contains(neighbor)) continue;

          double tentativeGScore = gScore.get(currentNode) + edge.getDriveTime();
          if (tentativeGScore < gScore.get(neighbor)) {
            gScore.put(neighbor, tentativeGScore);
            fScore.put(neighbor, tentativeGScore + landmarkHeuristic(neighbor, targetNodeId));
            if (!openSet.contains(new NodeDistancePair(neighbor, fScore.get(neighbor)))) {
              openSet.add(new NodeDistancePair(neighbor, fScore.get(neighbor)));
            }
          }
        }
      }

      return -1; // No path found
    }

    private double landmarkHeuristic(int node, int target) {
      double heuristic = 0.0;
      for (int landmark : landmarks) {
        double distanceToLandmark = landmarkDistances.get(landmark).getOrDefault(node, Double.MAX_VALUE);
        double distanceFromLandmarkToTarget = landmarkDistances.get(landmark).getOrDefault(target, Double.MAX_VALUE);
        heuristic = Math.max(heuristic, Math.abs(distanceToLandmark - distanceFromLandmarkToTarget));
      }
      return heuristic;
    }

    private static class NodeDistancePair implements Comparable<NodeDistancePair> {
      private int nodeId;
      private double distance;

      public NodeDistancePair(int nodeId, double distance) {
        this.nodeId = nodeId;
        this.distance = distance;
      }

      public int getNodeId() {
        return nodeId;
      }

      public double getDistance() {
        return distance;
      }

      @Override
      public int compareTo(NodeDistancePair other) {
        return Double.compare(this.distance, other.distance);
      }
    }
  }

  public class GraphUtility {

    public static List<POI> findNearestPOIs(Graph graph, int startNodeId, int numPOIs, Map<Integer, POI> pois) {
      Map<Integer, Double> distances = DijkstraAlgorithm.shortestPath(graph, startNodeId);

      PriorityQueue<Map.Entry<Integer, Double>> pq = new PriorityQueue<>(
              Map.Entry.comparingByValue()
      );

      for (Map.Entry<Integer, Double> entry : distances.entrySet()) {
        if (pois.containsKey(entry.getKey())) {
          pq.add(entry);
        }
      }

      List<POI> nearestPOIs = new ArrayList<>();
      while (!pq.isEmpty() && nearestPOIs.size() < numPOIs) {
        Map.Entry<Integer, Double> entry = pq.poll();
        nearestPOIs.add(pois.get(entry.getKey()));
      }

      return nearestPOIs;
    }

    // The categories for this are defined as this:
    // 1: Place name
    // 2: Gas station
    // 4: Charging station
    // 8: Food place
    // 16: Drinks place
    // 32: Accomodation
    // A place may have multiple categories, in which case the sum of the categories is used.
    // For example, a place that is both a gas station and a food place will have the category 10.
    public static List<POI> findNearestPOIsInCategory(Graph graph, int startNodeId, int numPOIs, Map<Integer, POI> pois, int category) {
      // Calculate shortest paths from start node
      Map<Integer, Double> distances = DijkstraAlgorithm.shortestPath(graph, startNodeId);

      // Priority Queue to sort POIs by distance
      PriorityQueue<Map.Entry<Integer, Double>> pq = new PriorityQueue<>(
              Map.Entry.comparingByValue()
      );

      // Add all POIs to the priority queue
      for (Map.Entry<Integer, Double> entry : distances.entrySet()) {
        if (pois.containsKey(entry.getKey())) {
          pq.add(entry);
        }
      }

      // List to store nearest POIs of the specified category
      List<POI> nearestPOIs = new ArrayList<>();
      while (!pq.isEmpty() && nearestPOIs.size() < numPOIs) {
        Map.Entry<Integer, Double> entry = pq.poll();
        POI poi = pois.get(entry.getKey());
        // Check if POI's category matches the specified category
        if ((poi.getCode() & category) != 0) {
          nearestPOIs.add(poi);
        }
      }

      return nearestPOIs;
    }

  }

  public class GraphTest {
    private static void testShortestPath(RoadMap roadMap, RoadMap.Graph graph, int startNodeId, int endNodeId) {
      RoadMap.DijkstraAlgorithm dijkstra = roadMap.new DijkstraAlgorithm();
      long startTime = System.currentTimeMillis();
      Map<Integer, Double> shortestPathResult = dijkstra.shortestPath(graph, startNodeId);
      long endTime = System.currentTimeMillis();

      if (!shortestPathResult.containsKey(endNodeId)) {
        System.out.println("No path found from " + startNodeId + " to " + endNodeId);
        return;
      }

      RoadMap.ALTAlgorithm alt = roadMap.new ALTAlgorithm(graph, 10);
      long startTime2 = System.currentTimeMillis();
      double shortestPathResult2 = alt.shortestPath(graph, startNodeId, endNodeId);
      long endTime2 = System.currentTimeMillis();

      double totalTimeInHundredthsOfSecond = shortestPathResult.get(endNodeId); // This is the total time in hundredths of a second
      double totalTimeInSeconds = totalTimeInHundredthsOfSecond / 100.0;
      String formattedTime = formatTime(totalTimeInSeconds);

      double totalTimeInHundredthsOfSecond2 = shortestPathResult2; // This is the total time in hundredths of a second
      double totalTimeInSeconds2 = totalTimeInHundredthsOfSecond2 / 100.0;
      String formattedTime2 = formatTime(totalTimeInSeconds2);

      double totalDistanceInMeters = calculateTotalDistance(graph, shortestPathResult, startNodeId, endNodeId);
      double totalDistanceInKm = totalDistanceInMeters / 1000.0;


      System.out.println("Shortest path from " + startNodeId + " to " + endNodeId + " using Dijkstra's algorithm:");
      System.out.println("Distance: " + String.format("%.2f km", totalDistanceInKm));
      System.out.println("Estimated driving time: " + formattedTime);
      System.out.println("Time elapsed: " + (endTime - startTime) + " ms");

      System.out.println("Shortest path from " + startNodeId + " to " + endNodeId + " using ALT algorithm:");
      //System.out.println("Distance: " + String.format("%.2f km", shortestPathResult2/1000));
      System.out.println("Estimated driving time: " + formattedTime2);
      System.out.println("Time elapsed: " + (endTime2 - startTime2) + " ms");
    }

    private static double calculateTotalDistance(RoadMap.Graph graph, Map<Integer, Double> shortestPathResult, int startNodeId, int endNodeId) {
      double totalDistance = 0.0;
      int currentNode = endNodeId;

      while (currentNode != startNodeId) {
        final int finalCurrentNode = currentNode; // effectively final for use in lambda
        double distanceToCurrentNode = graph.getEdgesFromNode(currentNode).stream()
                .filter(edge -> edge.getToNode() == finalCurrentNode)
                .mapToDouble(Edge::getLength)
                .findFirst()
                .orElse(0.0);

        totalDistance += distanceToCurrentNode;

        Integer nextNode = graph.getEdgesFromNode(currentNode).stream()
                .filter(edge -> shortestPathResult.containsKey(edge.getToNode()))
                .min(Comparator.comparingDouble(edge -> shortestPathResult.get(edge.getToNode())))
                .map(Edge::getToNode)
                .orElse(null);

        if (nextNode == null) {
          break;
        }

        currentNode = nextNode;
      }

      return totalDistance;
    }


    private static String formatTime(double totalTimeInSeconds) {
      int hours = (int) (totalTimeInSeconds / 3600);
      int minutes = (int) ((totalTimeInSeconds % 3600) / 60);
      int seconds = (int) (totalTimeInSeconds % 60);
      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static void testNearestPOIs(RoadMap.Graph graph, Map<Integer, RoadMap.POI> pois, int startNodeId, int numPOIs, int category) {
      List<RoadMap.POI> nearestPOIs = RoadMap.GraphUtility.findNearestPOIsInCategory(graph, startNodeId, numPOIs, pois, category);
      System.out.println("Nearest POIs from Node " + startNodeId + ":");
      for (RoadMap.POI poi : nearestPOIs) {
        // Display POI name. Distance computation needs to be added in findNearestPOIs method.
        System.out.println(poi.getName());
        System.out.println(poi.getCode());
      }
    }
  }

  class Visualization {
    // Methods for graphical representation (optional)
  }
}
