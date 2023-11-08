from collections import defaultdict
import heapq

# Function to read nodes file
def read_nodes_fixed(file_path):
    with open(file_path, 'r') as file:
        num_nodes = int(file.readline().strip())  # First line is the number of nodes
        nodes = {}
        for line in file:
            parts = line.strip().split()
            node_id = int(parts[0])
            latitude = float(parts[1])
            longitude = float(parts[2])
            nodes[node_id] = (latitude, longitude)
        return num_nodes, nodes

# Function to read edges file
def read_edges_fixed(file_path):
    with open(file_path, 'r') as file:
        num_edges = int(file.readline().strip())  # First line is the number of edges
        edges = []
        for line in file:
            parts = line.strip().split()
            from_node = int(parts[0])
            to_node = int(parts[1])
            drive_time = int(parts[2])  # in hundredths of a second
            length = float(parts[3])  # in meters
            speed_limit = int(parts[4])  # in km/h
            edges.append((from_node, to_node, drive_time, length, speed_limit))
        return num_edges, edges

# Correcting the read_pois function to handle the actual format of the points of interest file
def read_pois_corrected(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        pois = {}
        # Skip the first line as it may not contain data
        next(file)
        for line in file:
            if line.strip():  # Skip any empty lines
                parts = line.strip().split('\t')
                if len(parts) == 3:  # Ensure there are 3 parts
                    node_id = int(parts[0])
                    code = parts[1]
                    name = parts[2].strip('"')  # Remove quotes around the name
                    pois[node_id] = {'code': code, 'name': name}
        return pois

# Create a graph structure using a dictionary
def create_graph(edges):
    graph = defaultdict(list)
    for from_node, to_node, drive_time, length, speed_limit in edges:
        # Using drive time (converted to seconds) as the weight for the edges
        graph[from_node].append((to_node, drive_time / 100.0))
    return graph

# Implementation of Dijkstra's algorithm
def dijkstra_all_nodes(graph, nodes, start):
    # The priority queue
    queue = [(0, start)]
    # Distances dictionary initialized for all nodes
    distances = {node: float('infinity') for node in nodes}
    # Distance from start to start is 0
    distances[start] = 0

    while queue:
        # The distance to the current node and the node itself
        current_distance, current_node = heapq.heappop(queue)

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

    return distances

# Function to find the nearest points of interest
def find_nearest_pois(graph, nodes, pois, start_node, num_nearest=5):
    # First, get all distances from the start node
    distances = dijkstra_all_nodes(graph, nodes, start_node)

    # Filter out the distances to only include points of interest
    poi_distances = {node: distances[node] for node in pois if node in distances}

    # Sort the points of interest by distance and select the top 'num_nearest'
    nearest_pois = sorted(poi_distances, key=poi_distances.get)[:num_nearest]

    # Return the nearest points of interest and their distances
    return [(poi, pois[poi]['name'], poi_distances[poi]) for poi in nearest_pois]

# File paths
node_file_path = 'noder.txt'
edge_file_path = 'kanter.txt'
poi_file_path = 'interessepkt.txt'

# Reading the data from the files
num_nodes_fixed, nodes_fixed = read_nodes_fixed(node_file_path)
num_edges_fixed, edges_fixed = read_edges_fixed(edge_file_path)
pois_corrected = read_pois_corrected(poi_file_path)

# Creating the graph
graph = create_graph(edges_fixed)

# Define the start node for testing (change this for the real dataset)
start_node_for_test = 0

# Find the 5 nearest points of interest from the test start node
nearest_pois_from_start_corrected = find_nearest_pois(graph, nodes_fixed, pois_corrected, start_node_for_test)

# Print the results
print(f"5 nearest points of interest from node {start_node_for_test}:")
for poi, name, distance in nearest_pois_from_start_corrected:
    print(f"{poi}: {name} ({distance:.2f} seconds)")
