import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import javax.swing.*;

/**
 * Main class implementing the Dijkstra and ALT algorithms for finding shortest paths in a graph.
 * This class handles the entire process including reading graph data from files,
 * executing pathfinding algorithms, and visualizing the results on a map.
 */
public class ALT {
  /**
   * The main method that orchestrates reading graph data, executing pathfinding algorithms,
   * and visualizing results.
   *
   * @param args Command line arguments, not used in this application.
   */
  public static void main(String[] args) {
    Graph g = new Graph();
    try{
      BufferedReader nodesReader = new BufferedReader(new FileReader("norden/noder.txt"));
      BufferedReader edgesReader = new BufferedReader(new FileReader("norden/kanter.txt"));
      BufferedReader POIReader = new BufferedReader(new FileReader("norden/interessepkt.txt"));

      g.readNodes(nodesReader);
      g.readEdges(edgesReader);
      g.readInterestPoints(POIReader);

      Node start = g.node[5009309]; //   Kårvåg - 3292784
      Node destination = g.node[999080];   //    Gjemnes - 7352330
      Node orkanger = g.node[2266026];
      Node trondheimCamping = g.node[3005466];
      Node hotellOstersund = g.node[3240367];
      Node trondheim = g.node[7826348];
      Node selbustrand = g.node[5009309];
      Node greenstarHotelLahti = g.node[999080];

      int ladestasjon = 4;
      int spisested = 8;
      int drikkested = 16;
      int numberOfPoints = 5;

      String[] landmarks = {"Nordkapp", "Kristiansand", "Krakow", "Bremen", "Joensuu"};

      String filename = "preprocessedNordicMap.txt";
      FileWriter dijkstra = new FileWriter("dijkstraNodes.txt");
      FileWriter altAlgorithm = new FileWriter("altNodes.txt");

      long endTime;
      long startTime;
      if(!new File(filename).exists()){
        startTime = System.currentTimeMillis();
        g.preprocessMap(landmarks, filename);
        endTime= System.currentTimeMillis();
        System.out.println("Time spent preprocessing map: "+(endTime-startTime));
      }

      startTime = System.currentTimeMillis();
      g.readPreProcessedMap(filename);
      endTime = System.currentTimeMillis();
      System.out.println("Time spent reading map: "+(endTime-startTime) + " ms\n");

      startTime = System.currentTimeMillis();
      g.dijkstra(start,destination);
      endTime = System.currentTimeMillis();
      writeSearchResults(destination,dijkstra);
      System.out.println("Time spent on dijkstra: "+(endTime-startTime) + " ms");
      System.out.println("Time used from start->end: "+formatSeconds(((Prev)g.node[destination.value].d).dist/100) + "\n");

      int dijkstraVisited = 0;
      for(int i = 0; i<g.N; i++){
        if(g.visited[i]) dijkstraVisited++;
      }

      startTime = System.currentTimeMillis();
      g.altAlgorithm(start,destination);
      endTime = System.currentTimeMillis();
      writeSearchResults(destination,altAlgorithm);

      System.out.println("Time spent on alt algorithm: "+(endTime-startTime) + "ms");
      System.out.println("Time used from start->end: "+formatSeconds(((Prev)g.node[destination.value].d).dist/100) + "\n");

      int altVisited = 0;
      for(int i = 0; i<g.N; i++){
        if(g.visited[i]) altVisited++;
      }
      System.out.println("Alt visited nodes vs dijkstra visited nodes: " + altVisited + '/' + dijkstraVisited);

      Node[] ladestasjoner = g.dijkstra(orkanger,ladestasjon,numberOfPoints);
      Node[] drikkesteder = g.dijkstra(trondheimCamping,drikkested,numberOfPoints);
      Node[] spisesteder = g.dijkstra(hotellOstersund,spisested,numberOfPoints);
      System.out.println("Charging stations the closest to Orkanger");
      Arrays.stream(ladestasjoner).forEach(s-> System.out.println(s.value + ": " +s.name + " with type: " + s.classification));
      Arrays.stream(ladestasjoner).forEach(System.out::println);
      System.out.println("\nDrinking places the closest to Trondheim Camping");
      Arrays.stream(drikkesteder).forEach(s-> System.out.println(s.name + " with type: " + s.classification));
      Arrays.stream(drikkesteder).forEach(System.out::println);
      System.out.println("\nEating places the closest to Hotell Östersund");
      Arrays.stream(spisesteder).forEach(s-> System.out.println(s.name + " with type: " + s.classification));
      Arrays.stream(spisesteder).forEach(System.out::println);

      MapViewer mapViewer = new MapViewer();
      mapViewer.updateDijkstra(g.visitedNodesDijkstra, g.shortestPathNodesDijkstra);
      mapViewer.updateAlt(g.visitedNodesAlt, g.shortestPathNodesAlt);
      mapViewer.updateLandmarks(ladestasjoner, "Ladestasjoner");
      mapViewer.updateLandmarks(drikkesteder, "Drikkesteder");
      mapViewer.updateLandmarks(spisesteder, "Spisesteder");
      //mapViewer.drawNodes(g.visitedNodesDijkstra, Color.RED);
      //mapViewer.drawNodes(g.shortestPathNodesDijkstra, Color.BLUE);
      mapViewer.showMap();

    } catch (IOException e){
      e.printStackTrace();
    }
  }

  /**
   * Writes the search results of the pathfinding algorithms to a file.
   *
   * @param destination The end node of the path.
   * @param fw          FileWriter to write the output to.
   * @throws IOException If an I/O error occurs.
   */
  private static void writeSearchResults(Node destination, FileWriter fw) throws IOException {
    int index = 0;
    Node n = destination;
    Prev p;
    while(n!=null){
      p=(Prev)n.d;
      n=p.prev;
      index++;
    }
    int adjust = index/500;
    n = destination;
    for(int i = 0; i<index; i++){
      if(i%(adjust+1)==0) fw.write(n+"\n");
      p=(Prev)n.d;
      n=p.prev;
    }
    fw.close();

  }

  /**
   * Formats time from seconds into a human-readable format of HH:MM:SS.
   *
   * @param timeInSeconds Time in seconds to be formatted.
   * @return A string representing the formatted time.
   */
  static String formatSeconds(int timeInSeconds)
  {
    int secondsLeft = timeInSeconds % 3600 % 60;
    int minutes = (int) Math.floor(timeInSeconds % 3600 / 60);
    int hours = (int) Math.floor(timeInSeconds / 3600);

    String HH = ((hours       < 10) ? "0" : "") + hours;
    String MM = ((minutes     < 10) ? "0" : "") + minutes;
    String SS = ((secondsLeft < 10) ? "0" : "") + secondsLeft;

    return HH + ":" + MM + ":" + SS;
  }

  /**
   * Represents a node in the graph, with properties for edges, data storage, value, geographical coordinates, classification, and name.
   */
  static class Node{
    Edge edge1; // First edge in the linked list of edges
    Object d; // Data related to the node, typically used for pathfinding
    int value; // Node identifier
    String latitude; // Geographical latitude
    String longitude; // Geographical longitude
    int classification; // Type or category of the node
    String name; // Human-readable name of the node

    /**
     * Constructs a Node with a specified value.
     *
     * @param i The value/identifier for the node.
     */
    Node(int i) {
      value = i;
    }

    /**
     * Constructs a Node with specified value and geographical coordinates.
     *
     * @param i         The value/identifier for the node.
     * @param latitude  The geographical latitude of the node.
     * @param longitude The geographical longitude of the node.
     */
    Node(int i, String latitude, String longitude) {
      value = i;
      this.latitude = latitude;
      this.longitude = longitude;
    }

    @Override
    public String toString() {
      return latitude + "," + longitude;
    }
  }

  /**
   * Represents an edge in the graph, containing references to a destination node and the next edge in the linked list.
   */
  static class Edge{
    Node to; // Destination node of this edge
    Edge next; // Next edge in the list
    /**
     * Constructs an Edge with a specified destination node and the next edge.
     *
     * @param n   The destination node of the edge.
     * @param nxt The next edge in the linked list.
     */
    Edge(Node n, Edge nxt) {
      to = n;
      next = nxt;
    }
  }

  /**
   * A specialized Edge class that includes a weight for the edge, used in pathfinding algorithms.
   */
  static class WEdge extends Edge {
    int weight; // Weight or cost associated with this edge

    /**
     * Constructs a weighted edge with specified destination node, next edge, and weight.
     *
     * @param n    The destination node of the edge.
     * @param nxt  The next edge in the linked list.
     * @param wght The weight/cost associated with this edge.
     */
    public WEdge(Node n, WEdge nxt, int wght) {
      super(n, nxt);
      weight = wght;
    }
  }

  /**
   * Represents pathfinding data for a node, used in graph traversal algorithms.
   */
  static class Prev {
    int dist; // Distance from the source node
    int estimate; // Estimated distance to the destination node (used in ALT algorithm)
    Node prev; // Reference to the previous node in the path
    static final int inf = 100000000; // Represents an infinite distance

    /**
     * Constructs a Prev object with default values.
     */
    public Prev() {
      dist = inf;
      estimate = 0;
    }

    /**
     * Calculates the total estimated distance to the destination node.
     *
     * @return Total distance considering both distance and estimate.
     */
    public int getDistance() {
      return this.dist + this.estimate;
    }
  }

  /**
   * Represents the graph structure, containing nodes, edges, and methods for graph processing and pathfinding algorithms.
   */
  static class Graph {
    int N, K, P; // Number of nodes, edges, and points of interest
    Node[] node; // Array of nodes
    Node[] transposed; // Array of transposed nodes
    boolean[] visited; // Array of booleans indicating whether a node has been visited
    boolean[] found; // Array of booleans indicating whether a node has been found
    PriorityQueue<Node> pq; // Priority queue used in pathfinding algorithms
    HashMap<String, Node> interestPoints; // Map of interest points, with the name as key and the node as value
    int[] landmarks; // Array of landmark nodes
    int[][] fromLandmark; // Array of distances from landmarks to all nodes
    int[][] toLandmark; // Array of distances to landmarks from all nodes
    List<Node> visitedNodesDijkstra; // List of visited nodes in Dijkstra's algorithm
    List<Node> shortestPathNodesDijkstra; // List of nodes in the shortest path in Dijkstra's algorithm
    List<Node> visitedNodesAlt; // List of visited nodes in the ALT algorithm
    List<Node> shortestPathNodesAlt; // List of nodes in the shortest path in the ALT algorithm

    /**
     * Default constructor to initialize graph-related structures.
     */
    public Graph(){
      visitedNodesDijkstra = new ArrayList<>();
      shortestPathNodesDijkstra = new ArrayList<>();
      visitedNodesAlt = new ArrayList<>();
      shortestPathNodesAlt = new ArrayList<>();
    }

    /**
     * Constructs a Graph from a BufferedReader, reading graph data.
     *
     * @param br BufferedReader to read graph data from.
     * @throws IOException If an I/O error occurs while reading.
     */
    public Graph(BufferedReader br)throws IOException{
      visited = new boolean[N];
      found = new boolean[N];
      interestPoints=new HashMap<>();
      newGraph(br);
    }

    /**
     * Initializes a new graph with data read from a BufferedReader.
     *
     * @param br BufferedReader containing the graph data.
     * @throws IOException If an I/O error occurs while reading.
     */
    public void newGraph(BufferedReader br)throws IOException {
      StringTokenizer st = new StringTokenizer(br.readLine());
      N=Integer.parseInt(st.nextToken());
      node=new Node[N];
      for(int i = 0; i<N; i++) node[i] = new Node(i);
      K=Integer.parseInt(st.nextToken());
      for(int i = 0; i<K; i++){
        st = new StringTokenizer(br.readLine());
        int from = Integer.parseInt(st.nextToken());
        int to = Integer.parseInt(st.nextToken());
        int weight = Integer.parseInt(st.nextToken());
        WEdge w = new WEdge(node[to],(WEdge) node[from].edge1,weight);
        node[from].edge1 = w;
      }
    }

    /**
     * Reads node data from a BufferedReader and populates the graph with nodes.
     *
     * @param br BufferedReader to read node data from.
     * @throws IOException If an I/O error occurs while reading.
     */
    void readNodes(BufferedReader br) throws IOException {
      System.out.println("Reading nodes");
      StringTokenizer st = new StringTokenizer(br.readLine());
      N = Integer.parseInt(st.nextToken());
      node = new Node[N];
      transposed = new Node[N];
      for (int i = 0; i < N; i++) {
        st = new StringTokenizer(br.readLine());
        int value = Integer.parseInt(st.nextToken());
        String latitude = st.nextToken();
        String longitude = st.nextToken();
        node[i] = new Node(value,latitude,longitude);
        transposed[i] = new Node(value,latitude,longitude);
      }
    }

    /**
     * Reads edge data from a BufferedReader and adds edges to the graph.
     *
     * @param br BufferedReader to read edge data from.
     * @throws IOException If an I/O error occurs while reading.
     */
    void readEdges(BufferedReader br) throws IOException {
      System.out.println("Reading edges");
      StringTokenizer st = new StringTokenizer(br.readLine());
      K = Integer.parseInt(st.nextToken());
      for (int i = 0; i < K; i++) {
        st = new StringTokenizer(br.readLine());
        int from = Integer.parseInt(st.nextToken());
        int to = Integer.parseInt(st.nextToken());
        int weight = Integer.parseInt(st.nextToken());
        int length = Integer.parseInt(st.nextToken());
        WEdge w = new WEdge(node[to], (WEdge) node[from].edge1, weight);
        WEdge w2 = new WEdge(transposed[from], (WEdge) transposed[to].edge1, weight);
        node[from].edge1 = w;
        transposed[to].edge1 = w2;
      }
    }

    /**
     * Reads interest points from a BufferedReader and maps them to corresponding nodes in the graph.
     *
     * @param br BufferedReader to read interest points from.
     * @throws IOException If an I/O error occurs while reading.
     */
    void readInterestPoints(BufferedReader br) throws IOException {
      System.out.println("Reading interest points");
      interestPoints=new HashMap<>();
      StringTokenizer st = new StringTokenizer(br.readLine());
      P = Integer.parseInt(st.nextToken());
      for (int i = 0; i < P; i++) {
        st = new StringTokenizer(br.readLine());
        int nodeValue = Integer.parseInt(st.nextToken());
        int category = Integer.parseInt(st.nextToken());
        StringBuilder name = new StringBuilder(st.nextToken());
        while (st.hasMoreTokens())
          name.append(" ").append(st.nextToken());
        name = new StringBuilder(name.toString().replaceAll(String.valueOf('"'), ""));
        node[nodeValue].classification=category;
        node[nodeValue].name = name.toString();
        interestPoints.put(name.toString(),node[nodeValue]);
      }
    }

    /**
     * Executes Dijkstra's algorithm to find the nearest points of a specified type from a starting node.
     *
     * @param s              The starting node.
     * @param type           The type of points to find.
     * @param numberOfPoints The number of points to find.
     * @return An array of nodes representing the nearest points of the specified type.
     */
    public Node[] dijkstra(Node s, int type, int numberOfPoints) {
      Node[] interestPoints = new Node[numberOfPoints + 1]; // Size adjusted to accommodate the starting node
      interestPoints[0] = s; // Manually add the starting node
      int counter = 1; // Start from 1 as the starting node is already added
      visited = new boolean[N];
      found = new boolean[N];
      initPrev(s);
      pq = makePrio(s);
      found[s.value] = true;

      while(counter < interestPoints.length){
        Node n = pq.poll();
        if (n != s && (n.classification & type) == type) { // Check if not the starting node and matches the type
          interestPoints[counter++] = n;
        }
        for(WEdge w = (WEdge)n.edge1; w!= null; w=(WEdge) w.next){
          shorten(n,w);
        }
      }
      return interestPoints;
    }

    /**
     * Executes the standard Dijkstra's algorithm from a starting node to explore the graph.
     *
     * @param s The starting node.
     */
    public void dijkstra(Node s) {
      visited = new boolean[N];
      found = new boolean[N];
      initPrev(s);
      pq = makePrio(s);
      found[s.value] = true;
      while(!pq.isEmpty()){
        Node n = pq.poll();
        visited[n.value]=true;
        for(WEdge w = (WEdge)n.edge1; w!= null; w=(WEdge) w.next){
          shorten(n,w);
        }
      }
    }

    /**
     * Executes Dijkstra's algorithm from a start node to an end node and stores the path.
     *
     * @param start The starting node.
     * @param end   The destination node.
     */
    public void dijkstra(Node start,Node end){
      visited = new boolean[N];
      found = new boolean[N];
      visitedNodesDijkstra.clear();
      shortestPathNodesDijkstra.clear();

      initPrev(start);
      pq = makePrio(start);
      found[start.value] = true;

      while(!visited[end.value]){
        Node n = pq.poll();
        visited[n.value]=true;
        visitedNodesDijkstra.add(n);

        for(WEdge w = (WEdge)n.edge1; w!= null; w=(WEdge) w.next){
          shorten(n,w);
        }
      }

      Node n = end;
      while (n != null) {
        shortestPathNodesDijkstra.add(n);
        n = ((Prev) n.d).prev;
      }
      Collections.reverse(shortestPathNodesDijkstra);
    }

    /**
     * Executes Dijkstra's algorithm on the transposed graph starting from a node.
     *
     * @param s The starting node.
     */
    public void dijkstraTransposed(Node s) {
      visited = new boolean[N];
      found = new boolean[N];
      initPrevTransposed(s);
      pq = makePrio(s);
      while(!pq.isEmpty()){
        Node n = pq.poll();
        visited[n.value]=true;
        for(WEdge w = (WEdge)n.edge1; w!= null; w=(WEdge) w.next){
          shorten(n,w);
        }
      }
    }

    /**
     * Initializes the Prev objects for each node in the graph with respect to a transposed graph starting from a given node.
     *
     * @param s The starting node for the pathfinding.
     */
    private void initPrevTransposed(Node s){
      for(int i=N; i-->0;){
        transposed[i].d=new Prev();
      }
      ((Prev)s.d).dist=0;
    }

    /**
     * Initializes the Prev objects for each node in the graph starting from a given node.
     *
     * @param s The starting node for the pathfinding.
     */
    private void initPrev(Node s){
      for(int i=N; i-->0;){
        node[i].d=new Prev();
      }
      ((Prev)s.d).dist=0;
    }

    /**
     * Attempts to shorten the path to a node based on a given edge and updates the pathfinding data.
     *
     * @param n Node from which the edge originates.
     * @param w The weighted edge to consider for shortening the path.
     */
    private void shorten(Node n, WEdge w){
      if(visited[w.to.value]) return;

      Prev nd = (Prev)n.d;
      Prev md=(Prev)w.to.d;

      if(!found[w.to.value]){
        pq.add(w.to);
        found[w.to.value] = true;
      }

      if(md.dist>nd.dist+w.weight){
        md.dist = nd.dist + w.weight;
        md.prev = n;
        pq.remove(w.to);
        pq.add(w.to);
      }
    }

    /**
     * Creates and returns a priority queue for nodes based on their distance estimations, starting with a given node.
     *
     * @param s The starting node for the priority queue.
     * @return A PriorityQueue of Node objects ordered by their distance estimations.
     */
    private PriorityQueue<Node> makePrio(Node s){
      PriorityQueue<Node> pq = new PriorityQueue<>(N, Comparator.comparingInt(a -> ((Prev) a.d).getDistance()));
      pq.add(s);
      return pq;
    }

    /**
     * Executes the ALT (A* Landmark) algorithm for pathfinding between two nodes.
     *
     * @param start The starting node.
     * @param end   The destination node.
     */
    public void altAlgorithm(Node start, Node end){
      visited = new boolean[N];
      found = new boolean[N];
      visitedNodesAlt.clear();
      shortestPathNodesAlt.clear();
      initPrev(start);
      pq = makePrio(start);
      while(!visited[end.value]){
        Node n = pq.poll();
        visited[n.value]=true;
        visitedNodesAlt.add(n);
        for(WEdge w = (WEdge)n.edge1; w!= null; w=(WEdge) w.next){
          altShorten(n,end, w);
        }
      }

      Node n = end;
      while (n != null) {
        shortestPathNodesAlt.add(n);
        n = ((Prev) n.d).prev;
      }
      Collections.reverse(shortestPathNodesAlt);
    }

    /**
     * Attempts to shorten the path to a node using the ALT heuristic, considering a given edge.
     *
     * @param n Node from which the edge originates.
     * @param e The end node of the pathfinding process.
     * @param w The weighted edge to consider for shortening the path.
     */
    private void altShorten(Node n, Node e, WEdge w){
      if(visited[w.to.value]) return;
      Prev nd = (Prev)n.d;
      Prev md=(Prev)w.to.d;
      if(!found[w.to.value]){
        calculateEstimate(w.to,e);
        pq.add(w.to);
        found[w.to.value] = true;
      }
      if(md.dist>nd.dist+w.weight){
        md.dist = nd.dist + w.weight;
        md.prev = n;
        pq.remove(w.to);
        pq.add(w.to);
      }
    }

    /**
     * Calculates and sets the heuristic estimate for a node in the context of the ALT algorithm.
     *
     * @param n       The node for which to calculate the estimate.
     * @param endNode The destination node of the pathfinding process.
     */
    private void calculateEstimate(Node n, Node endNode) {
      int largestEstimate = 0;
      int previous = -1;
      for (int i = 0; i < landmarks.length; i++) {
        int estimateFromLandmark = fromLandmark[i][endNode.value] - fromLandmark[i][n.value];
        int estimateToLandmark = toLandmark[i][n.value] - toLandmark[i][endNode.value];
        largestEstimate = Math.max(estimateToLandmark, estimateFromLandmark);
        if (previous > largestEstimate) largestEstimate = previous;
        previous = largestEstimate;
      }
      if (largestEstimate > 0) ((Prev)n.d).estimate = largestEstimate;
    }

    /**
     * Preprocesses the map by running Dijkstra's algorithm from each landmark, storing distances for the ALT algorithm.
     *
     * @param landmarks An array of landmark names to be used in preprocessing.
     * @param filename  The filename to save the preprocessed data.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void preprocessMap(String[] landmarks, String filename) throws IOException{
      System.out.println("Preprocessing...");
      int[][] dijkstraLengths = new int[landmarks.length][N];
      for (int i = 0; i < landmarks.length; i++) {
        dijkstra(findInterestPoints(landmarks[i]));
        for (int j = 0; j < N; j++) {
          dijkstraLengths[i][j] = ((Prev)node[j].d).dist;
        }
      }
      System.out.println("Dijkstra done");
      int[][] dijkstraLengthsTransposed = new int[landmarks.length][N];
      for (int i = 0; i < landmarks.length; i++) {
        dijkstraTransposed(findTransposedInterestPoint(landmarks[i]));
        for (int j = 0; j < N; j++) {
          dijkstraLengthsTransposed[i][j] = ((Prev)transposed[j].d).dist;
        }
      }
      System.out.println("Dijkstra transposed done");
      FileWriter fw = new FileWriter(filename);
      for (int i = 0; i < landmarks.length; i++) {
        fw.write(String.valueOf(findInterestPoints(landmarks[i]).value));
        if(i+1<landmarks.length) fw.write(" ");
      }
      fw.write("\n");
      System.out.println("Landmarks was written");
      for (int i = 0; i < N; i++) {
        for (int j = 0; j < landmarks.length; j++) {
          fw.write(String.valueOf(dijkstraLengths[j][i]));
          if(j+1<landmarks.length) fw.write(" ");
        }
        fw.write("\n");
      }
      System.out.println("Dijkstra was written");
      for (int i = 0; i < N; i++) {
        for (int j = 0; j < landmarks.length; j++) {
          fw.write(String.valueOf(dijkstraLengthsTransposed[j][i]));
          if(j+1<landmarks.length) fw.write(" ");
        }
        if(i+1<N) fw.write("\n");
      }
      System.out.println("Dijkstra transposed was written");
      fw.close();
    }

    /**
     * Reads preprocessed map data from a file, which includes distances from and to landmarks for the ALT algorithm.
     *
     * @param filename The filename from which to read the preprocessed data.
     * @throws IOException If an I/O error occurs while reading from the file.
     */
    public void readPreProcessedMap(String filename) throws IOException {
      BufferedReader br = new BufferedReader(new FileReader(filename));
      StringTokenizer str = new StringTokenizer(br.readLine());
      final int size = str.countTokens();
      this.landmarks=new int[size];
      this.fromLandmark= new int[size][N];
      this.toLandmark = new int[size][N];
      for (int i = 0; i < size; i++) {
        landmarks[i] = Integer.parseInt(str.nextToken());
      }
      System.out.println("Landmarks was read");
      for (int i = 0; i < N; i++) {
        str = new StringTokenizer(br.readLine());
        for (int j = 0; j < size; j++) {
          fromLandmark[j][i] = Integer.parseInt(str.nextToken());
        }
      }
      System.out.println("Distances from landmarks were read");
      for (int i = 0; i < N; i++) {
        str = new StringTokenizer(br.readLine());
        for (int j = 0; j < size; j++) {
          toLandmark[j][i] = Integer.parseInt(str.nextToken());
        }
      }

      System.out.println("Distances to landmarks were read");
      System.out.println("Pre processed map was read");
    }

    /**
     * Finds a node in the graph based on its name.
     *
     * @param s The name of the node to find.
     * @return The node with the specified name.
     */
    public Node findInterestPoints(String s){
      return interestPoints.get(s);
    }

    /**
     * Finds a node in the transposed graph based on its name.
     *
     * @param s The name of the node to find.
     * @return The node with the specified name.
     */
    public Node findTransposedInterestPoint(String s){
      return transposed[interestPoints.get(s).value];
    }
  }

  /**
   * Manages the visualization of the map and the display of the paths and points on it.
   * This class uses JMapViewer to render the map and display paths calculated by the pathfinding algorithms.
   */
  static class MapViewer {
    private JMapViewer map; // The map
    private JRadioButton showDijkstraRadioButton; // Radio button for showing Dijkstra's path
    private JRadioButton showAltRadioButton; // Radio button for showing the ALT path
    private List<JRadioButton> landmarkRadioButtons; // Radio buttons for showing landmark paths
    private ButtonGroup radioButtonGroup; // Button group for radio buttons
    private List<Node> dijkstraNodes; // List of visited nodes in Dijkstra's algorithm
    private List<Node> dijkstraPath; // List of nodes in the shortest path in Dijkstra's algorithm
    private List<Node> altNodes; // List of visited nodes in the ALT algorithm
    private List<Node> altPath; // List of nodes in the shortest path in the ALT algorithm
    private List<Node[]> landmarks; // List of landmark nodes
    private List<String> landmarkNames = new ArrayList<>(); // List of landmark names

    /**
     * Constructor initializes the map and structures used for visualization.
     */
    public MapViewer() {
      initializeMap();
      landmarks = new ArrayList<>();
    }

    /**
     * Initializes the map settings and configures its default state.
     */
    private void initializeMap() {
      map = new JMapViewer();
      map.setTileSource(new OsmTileSource.Mapnik());
      map.setZoomControlsVisible(true);
      map.setDisplayPosition(new ICoordinate() {
        @Override
        public double getLat() {
          return 63.4305;
        }

        @Override
        public void setLat(double v) {

        }

        @Override
        public double getLon() {
          return 10.3951;
        }

        @Override
        public void setLon(double v) {

        }
      }, 5);
    }

    /**
     * Initializes and returns the sidebar for the map interface, providing user controls.
     *
     * @return JPanel containing the sidebar with controls.
     */
    private JPanel initializeSidebar() {
      JPanel sidebar = new JPanel();
      radioButtonGroup = new ButtonGroup();
      sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

      // Dijkstra checkbox
      showDijkstraRadioButton = new JRadioButton("Show Dijkstra");
      showDijkstraRadioButton.addActionListener(e -> toggleDijkstra());
      radioButtonGroup.add(showDijkstraRadioButton);
      sidebar.add(showDijkstraRadioButton);

      // ALT checkbox
      showAltRadioButton = new JRadioButton("Show ALT");
      showAltRadioButton.addActionListener(e -> toggleAlt());
      radioButtonGroup.add(showAltRadioButton);
      sidebar.add(showAltRadioButton);

      // Landmark checkboxes
      landmarkRadioButtons = new ArrayList<>();
      for (int i = 0; i < landmarks.size(); i++) {
        JRadioButton landmarkRadioButton = new JRadioButton("Show " + landmarkNames.get(i));
        int finalI = i;
        landmarkRadioButton.addActionListener(e -> toggleLandmark(finalI));
        landmarkRadioButtons.add(landmarkRadioButton);
        radioButtonGroup.add(landmarkRadioButton);
        sidebar.add(landmarkRadioButton);
      }
      sidebar.setVisible(true);
      sidebar.setMinimumSize(new Dimension(100, 600));
      map.add(sidebar, BorderLayout.WEST);

      return sidebar;
    }

    /**
     * Updates the map with nodes and paths found by Dijkstra's algorithm.
     *
     * @param n List of nodes visited during Dijkstra's algorithm.
     * @param p List of nodes forming the shortest path found by Dijkstra's algorithm.
     */
    public void updateDijkstra(List<Node> n, List<Node> p) {
      dijkstraNodes = n;
      dijkstraPath = p;
    }

    /**
     * Updates the map with nodes and paths found by the ALT algorithm.
     *
     * @param n List of nodes visited during the ALT algorithm.
     * @param p List of nodes forming the shortest path found by the ALT algorithm.
     */
    public void updateAlt(List<Node> n, List<Node> p) {
      altNodes = n;
      altPath = p;
    }

    /**
     * Updates the map with landmark points.
     *
     * @param n    Array of nodes representing landmarks.
     * @param name The name of the landmark group.
     */
    public void updateLandmarks(Node[] n, String name) {
      landmarks.add(n);
      landmarkNames.add(name);
    }

    /**
     * Toggles the display of the Dijkstra path on the map.
     */
    private void toggleDijkstra() {
      if (showDijkstraRadioButton.isSelected()) {
        map.getMapMarkerList().clear();
        map.repaint();
        for (Node node : dijkstraNodes) {
          if (!dijkstraPath.contains(node)) {
            double lat = Double.parseDouble(node.latitude);
            double lon = Double.parseDouble(node.longitude);
            MapMarkerDot marker = new MapMarkerDot(Color.RED, lat, lon);
            map.addMapMarker(marker);
          }
        }

        for (Node node : dijkstraPath) {
          double lat = Double.parseDouble(node.latitude);
          double lon = Double.parseDouble(node.longitude);
          MapMarkerDot marker = new MapMarkerDot(Color.BLUE, lat, lon);
          map.addMapMarker(marker);
        }
      } else {
        map.getMapMarkerList().clear();
      }
      map.repaint();
    }

    /**
     * Toggles the display of the ALT path on the map.
     */
    private void toggleAlt() {
      map.getMapMarkerList().clear();
      map.repaint();
      if (showAltRadioButton.isSelected()) {
        for (Node node : altNodes) {
          if (!altPath.contains(node)) {
            double lat = Double.parseDouble(node.latitude);
            double lon = Double.parseDouble(node.longitude);
            MapMarkerDot marker = new MapMarkerDot(Color.RED, lat, lon);
            map.addMapMarker(marker);
          }
        }

        for (Node node : altPath) {
          double lat = Double.parseDouble(node.latitude);
          double lon = Double.parseDouble(node.longitude);
          MapMarkerDot marker = new MapMarkerDot(Color.BLUE, lat, lon);
          map.addMapMarker(marker);
        }
      } else {
        map.getMapMarkerList().clear();
      }
      map.repaint();
    }

    /**
     * Toggles the display of a specific landmark group on the map.
     *
     * @param landmarkIndex The index of the landmark group to toggle.
     */
    private void toggleLandmark(int landmarkIndex) {
      if (landmarkRadioButtons.get(landmarkIndex).isSelected()) {
        map.getMapMarkerList().clear();
        map.repaint();
        for (Node node : landmarks.get(landmarkIndex)) {
          if (node == landmarks.get(landmarkIndex)[0]) {
            double lat = Double.parseDouble(node.latitude);
            double lon = Double.parseDouble(node.longitude);
            MapMarkerDot marker = new MapMarkerDot(Color.BLUE, lat, lon);
            map.addMapMarker(marker);
          } else {
            double lat = Double.parseDouble(node.latitude);
            double lon = Double.parseDouble(node.longitude);
            MapMarkerDot marker = new MapMarkerDot(Color.RED, lat, lon);
            map.addMapMarker(marker);
          }
        }
      } else {
        map.getMapMarkerList().clear();
      }
      map.repaint();
    }

    /**
     * Shows the map with the current settings and visualizations.
     */
    public void showMap() {
      JFrame frame = new JFrame("Map Viewer");
      frame.setLayout(new BorderLayout());

      JPanel sidebar = initializeSidebar();

      frame.add(sidebar, BorderLayout.WEST);
      frame.add(map, BorderLayout.CENTER);

      frame.setSize(800, 600);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
    }
  }
}
