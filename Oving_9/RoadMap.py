from collections import defaultdict
import heapq
import random
import numpy as np
import pandas as pd
import time
import tkinter
import tkintermapview as tkmap


# Function to read nodes file
def read_nodes_fixed(file_path):
    """
    Read nodes data from a file and return the number of nodes and a dictionary mapping node IDs to (latitude, longitude) coordinates.

    Args:
        file_path (str): The path to the file containing nodes data.

    Returns:
        tuple: A tuple containing the number of nodes and a dictionary mapping node IDs to (latitude, longitude) coordinates.
    """
    data = np.loadtxt(file_path, skiprows=1, dtype={'names': ('node_id', 'latitude', 'longitude'),
                                                    'formats': ('i4', 'f8', 'f8')})
    num_nodes = len(data)
    nodes = {row['node_id']: (row['latitude'], row['longitude']) for row in data}
    return num_nodes, nodes


# Function to read edges file
def read_edges_fixed(file_path):
    """
    Read edges data from a file and return the number of edges and a list of edge tuples, each containing (from_node, to_node, drive_time, length, speed_limit) information.

    Args:
        file_path (str): The path to the file containing edges data.

    Returns:
        tuple: A tuple containing the number of edges and a list of edge tuples.
    """
    data = np.loadtxt(file_path, skiprows=1,
                      dtype={'names': ('from_node', 'to_node', 'drive_time', 'length', 'speed_limit'),
                             'formats': ('i4', 'i4', 'i4', 'f8', 'i4')})
    num_edges = len(data)
    edges = [(row['from_node'], row['to_node'], row['drive_time'], row['length'], row['speed_limit']) for row in data]
    return num_edges, edges


# Corrected function to read points of interest
def read_pois_corrected(file_path):
    """
    Read points of interest data from a file and return a dictionary mapping node IDs to POI information.

    Args:
        file_path (str): The path to the file containing points of interest data.

    Returns:
        dict: A dictionary mapping node IDs to points of interest information.
    """
    df = pd.read_csv(file_path, sep='\t', encoding='utf-8', header=None, skiprows=1)
    df.columns = ['node_id', 'code', 'name']
    df['name'] = df['name'].str.strip('"')

    # Drop duplicates based on 'node_id'
    df = df.drop_duplicates(subset='node_id')

    pois = df.set_index('node_id').to_dict('index')

    return pois


# Create a graph structure using a dictionary
def create_graph(edges):
    """
    Create a graph structure from a list of edges with weighted connections between nodes.

    Args:
        edges (list): List of edge tuples containing (from_node, to_node, drive_time, length, speed_limit) information.

    Returns:
        defaultdict: A dictionary representing the graph structure with nodes as keys and lists of neighboring nodes as values.
    """
    graph = defaultdict(list)
    for from_node, to_node, drive_time, length, speed_limit in edges:
        # Using drive time (converted to seconds) as the weight for the edges
        graph[from_node].append((to_node, drive_time / 100.0))
    return graph


# Implementation of Dijkstra's algorithm
def dijkstra_all_nodes(graph, nodes, start):
    """
    Implement Dijkstra's algorithm to find the shortest paths from a starting node to all other nodes in the graph.

    Args:
        graph (defaultdict): A graph structure representing road network connectivity.
        nodes (dict): Dictionary mapping node IDs to their coordinates.
        start (int): The starting node for Dijkstra's algorithm.

    Returns:
        dict: A dictionary mapping node IDs to their shortest distances from the starting node.
        int: The total number of nodes processed.
        dict: A dictionary mapping node IDs to their coordinates.
    """
    # The priority queue
    queue = [(0, start)]
    # Distances dictionary initialized for all nodes
    distances = {node: float('infinity') for node in nodes}
    # Distance from start to start is 0
    distances[start] = 0
    nodes_processed = 0
    node_coords = {start: nodes[start]}  # Initialize with the starting node's coordinates

    while queue:
        # The distance to the current node and the node itself
        current_distance, current_node = heapq.heappop(queue)
        nodes_processed += 1

        # Nodes can get added to the queue multiple times. We only
        # process a vertex the first time we remove it from the queue.
        if current_distance > distances[current_node]:
            continue

        # Look at all the neighbors of this node and evaluate them
        for neighbor, weight in graph[current_node]:
            distance = current_distance + weight

            # If a shorter path to the neighbor has been found
            if distance < distances[neighbor]:
                distances[neighbor] = distance
                heapq.heappush(queue, (distance, neighbor))
                node_coords[neighbor] = nodes[neighbor]  # Update with the actual neighbor's coordinates

    return distances, nodes_processed, node_coords


# Function to find the nearest points of interest
def find_nearest_pois(graph, nodes, pois, start_node, num_nearest=5):
    """
    Find the nearest points of interest from a starting node based on precomputed distances.

    Args:
        graph (defaultdict): A graph structure representing road network connectivity.
        nodes (list): List of node IDs in the graph.
        pois (dict): A dictionary mapping node IDs to points of interest information.
        start_node (int): The starting node for finding nearest points of interest.
        num_nearest (int, optional): The number of nearest points of interest to find (default is 5).

    Returns:
        list: A list of tuples containing node ID, POI name, and distance to the nearest points of interest.
    """
    # First, get all distances from the start node
    distances = dijkstra_all_nodes(graph, nodes, start_node)

    # Filter out the distances to only include points of interest
    poi_distances = {node: distances[node] for node in pois if node in distances}

    # Sort the points of interest by distance and select the top 'num_nearest'
    nearest_pois = sorted(poi_distances, key=poi_distances.get)[:num_nearest]

    # Return the nearest points of interest and their distances
    return [(poi, pois[poi]['name'], poi_distances[poi]) for poi in nearest_pois]


# Choosing landmarks for the ALT algorithm
def choose_landmarks(graph, num_landmarks=3):
    """
    Choose a set of landmarks for the ALT algorithm.

    Args:
        graph (defaultdict): A graph structure representing road network connectivity.
        num_landmarks (int, optional): The number of landmarks to choose (default is 3).

    Returns:
        list: A list of node IDs representing the chosen landmarks.
    """
    return random.sample(list(graph.keys()), num_landmarks)


# Precomputing distances from each landmark to all nodes
def precompute_landmark_distances(graph, nodes, landmarks):
    """
    Precompute distances from each landmark to all nodes in the graph for the ALT algorithm.

    Args:
        graph (defaultdict): A graph structure representing road network connectivity.
        nodes (list): List of node IDs in the graph.
        landmarks (list): List of chosen landmarks for precomputing distances.

    Returns:
        dict: A dictionary mapping landmark node IDs to dictionaries of distances to all nodes.
    """
    landmark_distances = {landmark: dijkstra_all_nodes(graph, nodes, landmark) for landmark in landmarks}
    return landmark_distances


# A* search using the landmark heuristic
def a_star_search(graph, nodes, start, target, landmark_distances):
    """
    Implement the A* search algorithm with a landmark heuristic to find the shortest distance from a start node to a target node.

    Args:
        graph (defaultdict): A graph structure representing road network connectivity.
        nodes (dict): Dictionary mapping node IDs to their coordinates.
        start (int): The starting node for the A* search.
        target (int): The target node for the A* search.
        landmark_distances (dict): Precomputed distances from landmarks to all nodes.

    Returns:
        float or None: The shortest distance from the start node to the target node, or None if no path exists.
        int: The total number of nodes processed.
        dict: A dictionary mapping node IDs to their coordinates.
        list: The list of nodes forming the shortest path from start to target.
    """
    # Priority queue for A*
    queue = [(0, start)]
    # Cost from start to the node
    g_score = {node: float('infinity') for node in nodes}
    g_score[start] = 0
    # Estimated cost from start to goal through the node
    f_score = {node: float('infinity') for node in nodes}
    f_score[start] = landmark_heuristic(start, target, landmark_distances)
    nodes_processed = 0
    node_coords = {start: nodes[start]}  # Initialize with the starting node's coordinates
    came_from = {}  # To store the path

    while queue:
        # Current node is the node in the queue with the lowest estimated cost
        current = heapq.heappop(queue)[1]
        nodes_processed += 1

        # If we've reached our target, reconstruct the path
        if current == target:
            path = []
            while current in came_from:
                path.append(current)
                current = came_from[current]
            path.append(start)  # Optional: include start node in path
            path.reverse()  # Reverse the path to be start->target
            return g_score[target], nodes_processed, node_coords, path

        # Look at all the neighbors of this node
        for neighbor, weight in graph[current]:
            tentative_g_score = g_score[current] + weight
            if tentative_g_score < g_score[neighbor]:
                came_from[neighbor] = current
                g_score[neighbor] = tentative_g_score
                f_score[neighbor] = tentative_g_score + landmark_heuristic(neighbor, target, landmark_distances)
                heapq.heappush(queue, (f_score[neighbor], neighbor))
                node_coords[neighbor] = nodes[neighbor]  # Update with the actual neighbor's coordinates

    return None, nodes_processed, node_coords, []  # Return None if there is no path




# Heuristic function for A* using the landmark method
def landmark_heuristic(node, target, landmark_distances):
    """
    Compute the landmark heuristic for A* search.

    Args:
        node (int): The current node.
        target (int): The target node.
        landmark_distances (dict): Precomputed distances from landmarks to all nodes.

    Returns:
        int: The heuristic value.
    """
    heuristic = 0
    for landmark, distances_dict in landmark_distances.items():
        if node in distances_dict and target in distances_dict:
            heuristic = max(heuristic, abs(distances_dict[node] - distances_dict[target]))
    return heuristic


def run_tests():
    # File paths
    node_file_path = 'island/noder.txt'
    edge_file_path = 'island/kanter.txt'
    poi_file_path = 'island/interessepkt.txt'

    # Reading the data from the files
    num_nodes_fixed, nodes_fixed = read_nodes_fixed(node_file_path)
    num_edges_fixed, edges_fixed = read_edges_fixed(edge_file_path)
    pois_corrected = read_pois_corrected(poi_file_path)

    # Creating the graph
    graph = create_graph(edges_fixed)

    # Choosing landmarks and precomputing distances for the ALT algorithm
    landmarks = choose_landmarks(graph, num_landmarks=3)
    landmark_distances = precompute_landmark_distances(graph, nodes_fixed, landmarks)

    # Define the start node for testing (change this for the real dataset)
    #start_node_for_test = random.choice(list(nodes_fixed))
    # Define a random target node for testing
    #target_node_for_test = random.choice(list(nodes_fixed))
    start_node_for_test = 0
    target_node_for_test = 5

    if start_node_for_test == target_node_for_test:
        target_node_for_test += 1

    # Running Dijkstra's algorithm
    start_time = time.time()
    dijkstra_distances, nodes_processed, node_coords = dijkstra_all_nodes(graph, nodes_fixed, start_node_for_test)
    dijkstra_time = time.time() - start_time

    # Running A* search using the landmark heuristic
    start_time = time.time()
    a_star_distance, nodes_processed_a, node_coords_a, path_a = a_star_search(graph, nodes_fixed, start_node_for_test, target_node_for_test,
                                                       landmark_distances)
    a_star_time = time.time() - start_time

    # Output the results of Dijkstra's algorithm
    print(f"Start Node: {start_node_for_test}")
    print(f"Target Node: {target_node_for_test}")
    print(f"Number of Nodes Processed: {nodes_processed}")
    print("Dijkstra's Algorithm:")
    print(f"Distance: {dijkstra_distances[target_node_for_test]}")
    print(f"Time: {dijkstra_time}")
    print()

    # Output the results of A* search
    print(f"Start Node: {start_node_for_test}")
    print(f"Target Node: {target_node_for_test}")
    print(f"Number of Nodes Processed: {nodes_processed_a}")
    print("A* Search:")
    print(f"Distance: {a_star_distance}")
    print(f"Time: {a_star_time}")
    print()

    # Find the nearest points of interest
    nearest_pois = find_nearest_pois(graph, nodes_fixed, pois_corrected, start_node_for_test)
    print(f"Start Node: {start_node_for_test}")
    print("Nearest Points of Interest:")
    for poi in nearest_pois:
        print(poi)
    print()

    #print(node_coords)

    # Make the list show just the coordinates
    node_coords_list = []
    for key in node_coords_a:
        node_coords_list.append(node_coords_a[key])

    path_list = []
    for node in path_a:
        path_list.append(node_coords_a[node])
    #print(node_coords_list)
    make_map(node_coords_list, path_list)


def make_map(coords, path):
    global prev
    root = tkinter.Tk()
    root.geometry(f"{1000}x{700}")
    root.title('Map')

    # create map widget
    map_widget = tkmap.TkinterMapView(root, width=1000, height=700, corner_radius=0)
    map_widget.pack(fill="both", expand=True)

    map_widget.set_zoom(3)

    new_icon = tkinter.PhotoImage(file="resources/glow_small.png")

    start_marker = map_widget.set_marker(coords[0][0], coords[0][1])

    end_marker = map_widget.set_marker(coords[-1][0], coords[-1][1])

    path_obj = []

    for coord in path:
        if coord == coords[0] or coord == coords[-1]:
            continue
        print(f"Setting marker at {coord[0], coord[1]}")
        marker = map_widget.set_marker(coord[0], coord[1])
        path_obj.append(marker)
        if marker is None:
            print("Marker is none")
        else:
            marker.icon = new_icon
            if marker.icon != new_icon:
                print("Icon not set")

    count = 0

    for coord in coords:
        count += 1
        print(f"Count: {count}")
        if coord == coords[0] or coord == coords[-1]:
            continue
        if coord in path:
            continue
        print(f"Setting marker at {coord[0], coord[1]}")
        marker = map_widget.set_marker(coord[0], coord[1])
        if marker is None:
            print("Marker is none")
        else:
            marker.icon = new_icon
            if marker.icon != new_icon:
                print("Icon not set")

    for obj in path_obj:
        if obj == path_obj[0]:
            map_widget.set_path([start_marker.position, obj.position])
            prev = obj
        elif obj == path_obj[-1]:
            map_widget.set_path([obj.position, end_marker.position])
        else:
            map_widget.set_path([prev.position, obj.position])
            prev = obj

    root.mainloop()

run_tests()