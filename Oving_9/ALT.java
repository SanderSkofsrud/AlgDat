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
 * This class implements the Dijkstra and ALT algorithms for finding shortest paths in a graph.
 */
public class ALT {
  /**
   * The main method reads a graph and a number of queries from an input file.
   * Then it calls the Dijkstra and ALT algorithms to solve the queries,
   * and finally writes the results to an output file.
   *
   * @param args not used.
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

      Node start = g.node[2800567]; //   Kårvåg - 3292784
      Node destination = g.node[7705656];   //    Gjemnes - 7352330
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
   * This method writes the results of the queries to an output file.
   *
   * @param destination  the destination node.
   * @param fw           the file writer.
   * @throws IOException exception thrown if something goes wrong when writing to the file.
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
   * This method formats the time from seconds to HH:MM:SS.
   *
   * @param timeInSeconds the time in seconds.
   * @return              the formatted time.
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

  static class Node{
    Edge edge1;
    Object d;
    int value;
    String latitude;
    String longitude;
    int classification;
    String name;

    Node(int i){
      value=i;
    }

    Node(int i, String latitude, String longitude){
      value=i;
      this.latitude=latitude;
      this.longitude=longitude;
    }

    @Override
    public String toString() {
      return latitude + "," + longitude;
    }
  }

  static class Edge{
    Node to;
    Edge next;
    Edge(Node n, Edge nxt){
      to=n;
      next=nxt;
    }
  }

  static class WEdge extends Edge {
    int weight;
    public WEdge(Node n, WEdge nxt, int wght){
      super(n,nxt);
      weight=wght;
    }
  }

  static class Prev {
    int dist;
    int estimate;
    Node prev;
    static int inf = 100000000;

    public Prev(){
      dist=inf;
      estimate=0;
    }

    public int getDistance() {
      return this.dist+this.estimate;
    }
  }

  static class Graph {
    int N, K, P;
    Node[] node;
    Node[] transposed;
    boolean[] visited;
    boolean[] found;
    PriorityQueue<Node> pq;
    HashMap<String, Node> interestPoints;
    int[] landmarks;
    int[][] fromLandmark;
    int[][] toLandmark;
    List<Node> visitedNodesDijkstra;
    List<Node> shortestPathNodesDijkstra;
    List<Node> visitedNodesAlt;
    List<Node> shortestPathNodesAlt;
    public Graph(){
      visitedNodesDijkstra = new ArrayList<>();
      shortestPathNodesDijkstra = new ArrayList<>();
      visitedNodesAlt = new ArrayList<>();
      shortestPathNodesAlt = new ArrayList<>();
    }

    public Graph(BufferedReader br)throws IOException{
      visited = new boolean[N];
      found = new boolean[N];
      interestPoints=new HashMap<>();
      newGraph(br);
    }

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

    private void initPrevTransposed(Node s){
      for(int i=N; i-->0;){
        transposed[i].d=new Prev();
      }
      ((Prev)s.d).dist=0;
    }

    private void initPrev(Node s){
      for(int i=N; i-->0;){
        node[i].d=new Prev();
      }
      ((Prev)s.d).dist=0;
    }
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

    private PriorityQueue<Node> makePrio(Node s){
      PriorityQueue<Node> pq = new PriorityQueue<>(N, Comparator.comparingInt(a -> ((Prev) a.d).getDistance()));
      pq.add(s);
      return pq;
    }

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


    public Node findInterestPoints(String s){
      return interestPoints.get(s);
    }

    public Node findTransposedInterestPoint(String s){
      return transposed[interestPoints.get(s).value];
    }
  }

  static class MapViewer {
    private JMapViewer map;
    private JRadioButton showDijkstraRadioButton;
    private JRadioButton showAltRadioButton;
    private List<JRadioButton> landmarkCheckBoxes;
    private ButtonGroup radioButtonGroup;
    private List<Node> dijkstraNodes;
    private List<Node> dijkstraPath;
    private List<Node> altNodes;
    private List<Node> altPath;
    private List<Node[]> landmarks;
    private List<String> landmarkNames = new ArrayList<>();
    public MapViewer() {
      initializeMap();
      landmarks = new ArrayList<>();
    }

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
      landmarkCheckBoxes = new ArrayList<>();
      for (int i = 0; i < landmarks.size(); i++) {
        JRadioButton landmarkRadioButton = new JRadioButton("Show " + landmarkNames.get(i));
        int finalI = i;
        landmarkRadioButton.addActionListener(e -> toggleLandmark(finalI));
        landmarkCheckBoxes.add(landmarkRadioButton);
        radioButtonGroup.add(landmarkRadioButton);
        sidebar.add(landmarkRadioButton);
      }
      sidebar.setVisible(true);
      sidebar.setMinimumSize(new Dimension(100, 600));
      map.add(sidebar, BorderLayout.WEST);

      return sidebar;
    }

    public void updateDijkstra(List<Node> n, List<Node> p) {
      dijkstraNodes = n;
      dijkstraPath = p;
    }

    public void updateAlt(List<Node> n, List<Node> p) {
      altNodes = n;
      altPath = p;
    }

    public void updateLandmarks(Node[] n, String name) {
      landmarks.add(n);
      landmarkNames.add(name);
    }

    private void toggleDijkstra() {
      if (showDijkstraRadioButton.isSelected()) {
        map.getMapMarkerList().clear();
        map.repaint();
        for (Node node : dijkstraNodes) {
          if (!dijkstraPath.contains(node)) {
            double lat = Double.parseDouble(node.latitude);
            double lon = Double.parseDouble(node.longitude);
            MapMarkerDot marker = new MapMarkerDot(Color.RED, lat, lon); // Or your preferred color
            map.addMapMarker(marker);
          }
        }

        for (Node node : dijkstraPath) {
          double lat = Double.parseDouble(node.latitude);
          double lon = Double.parseDouble(node.longitude);
          MapMarkerDot marker = new MapMarkerDot(Color.BLUE, lat, lon); // Or your preferred color
          map.addMapMarker(marker);
        }
      } else {
        // Code to remove Dijkstra path markers
        // This can be optimized based on how you're tracking these markers
        map.getMapMarkerList().clear();
      }
      map.repaint();
    }


    private void toggleAlt() {
      map.getMapMarkerList().clear();
      map.repaint();
      if (showAltRadioButton.isSelected()) {
        for (Node node : altNodes) {
          if (!altPath.contains(node)) {
            double lat = Double.parseDouble(node.latitude);
            double lon = Double.parseDouble(node.longitude);
            MapMarkerDot marker = new MapMarkerDot(Color.RED, lat, lon); // Or your preferred color
            map.addMapMarker(marker);
          }
        }

        for (Node node : altPath) {
          double lat = Double.parseDouble(node.latitude);
          double lon = Double.parseDouble(node.longitude);
          MapMarkerDot marker = new MapMarkerDot(Color.BLUE, lat, lon); // Or your preferred color
          map.addMapMarker(marker);
        }
      } else {
        // Code to remove ALT path markers
        // This can be optimized based on how you're tracking these markers
        map.getMapMarkerList().clear();
      }
      map.repaint();
    }


    private void toggleLandmark(int landmarkIndex) {
      if (landmarkCheckBoxes.get(landmarkIndex).isSelected()) {
        map.getMapMarkerList().clear();
        map.repaint();
        // Assuming landmarks is a list of Node objects for landmarks
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
        // Code to remove landmark marker
        // This can be optimized based on how you're tracking these markers
        map.getMapMarkerList().clear();
      }
      map.repaint();
    }


    public void drawNodes(List<Node> nodes, Color color) {
      for (Node node : nodes) {
        double lat = Double.parseDouble(node.latitude);
        double lon = Double.parseDouble(node.longitude);
        MapMarkerDot marker = new MapMarkerDot(color, lat, lon);
        map.addMapMarker(marker);
      }
    }

    public void drawRoute(List<Coordinate> routeCoordinates) {
      List<MapMarker> routeMarkers = new ArrayList<>();
      for (Coordinate coord : routeCoordinates) {
        routeMarkers.add(new MapMarkerDot(coord.getLat(), coord.getLon()));
      }

      MapPolygonImpl routeLine = new MapPolygonImpl(routeMarkers);
      map.addMapPolygon(routeLine);

      for (MapMarker marker : routeMarkers) {
        map.addMapMarker(marker);
      }
    }

    public void showMap() {
      //drawNodes(dijkstraNodes, Color.RED);
      //drawNodes(dijkstraPath, Color.BLUE);
      //drawNodes(altNodes, Color.GREEN);
      //drawNodes(altPath, Color.YELLOW);
      JFrame frame = new JFrame("Map Viewer");
      frame.setLayout(new BorderLayout());

      JPanel sidebar = initializeSidebar();

      frame.add(sidebar, BorderLayout.WEST);
      frame.add(map, BorderLayout.CENTER);

      frame.setSize(800, 600);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
    }

    public static class Coordinate {
      private double lat;
      private double lon;

      public Coordinate(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
      }

      public double getLat() {
        return lat;
      }

      public double getLon() {
        return lon;
      }
    }
  }
}
