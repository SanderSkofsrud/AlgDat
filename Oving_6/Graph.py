import sys
from collections import deque


class Graph:
    """
    A class to represent an unweighted directed graph.
    """

    def __init__(self, nodes_count):
        """
        Initializes a Graph with a given number of nodes.

        Parameters:
            nodes_count (int): Total number of nodes in the graph.
        """
        self.nodes_count = nodes_count
        self.adj_list = {node: [] for node in range(nodes_count)}

    def add_edge(self, from_node, to_node):
        """
        Adds an edge to the graph.

        Parameters:
            from_node (int): The starting node of the edge.
            to_node (int): The ending node of the edge.
        """
        if to_node not in self.adj_list[from_node]:
            self.adj_list[from_node].append(to_node)

    def bfs(self, start_node):
        """
        Performs Breadth-First Search (BFS) on the graph from a given starting node.

        Parameters:
            start_node (int): The node from which BFS starts.

        Returns:
            tuple: A tuple containing two lists - predecessors of each node and distances from the start node.
        """
        visited = [False] * self.nodes_count
        distance = [-1] * self.nodes_count
        predecessor = [-1] * self.nodes_count

        queue = deque([start_node])
        visited[start_node] = True
        distance[start_node] = 0

        while queue:
            node = queue.popleft()
            for neighbor in self.adj_list[node]:
                if not visited[neighbor]:
                    visited[neighbor] = True
                    distance[neighbor] = distance[node] + 1
                    predecessor[neighbor] = node
                    queue.append(neighbor)

        return predecessor, distance

    def topological_sort(self):
        """
        Performs topological sorting on the graph using non-recursive DFS.

        Returns:
            list: A list of nodes in topologically sorted order.
        """
        visited = [False] * self.nodes_count
        stack = []

        def non_recursive_dfs(node):
            """
            Non-recursive DFS helper function.
            Made to avoid recursion limit in Python.

            Parameters:
                node (int): The starting node for DFS.
            """
            temp_stack = [node]
            while temp_stack:
                current_node = temp_stack[-1]
                visited[current_node] = True

                all_neighbors_visited = True
                for neighbor in self.adj_list[current_node]:
                    if not visited[neighbor]:
                        all_neighbors_visited = False
                        temp_stack.append(neighbor)
                        break

                if all_neighbors_visited:
                    stack.append(temp_stack.pop())

        for node in range(self.nodes_count):
            if not visited[node]:
                non_recursive_dfs(node)

        return stack[::-1]

    def display_bfs(self, start_node):
        """
        Displays the BFS result.

        Parameters:
            start_node (int): The node from which BFS starts.
        """
        predecessor, distance = self.bfs(start_node)
        print("Node Forgj Dist")
        for i in range(self.nodes_count):
            pred = predecessor[i]
            print(i, "_" if pred == -1 else pred, distance[i])

    @classmethod
    def from_file(cls, filepath):
        """
        Loads a graph from a given file.

        Parameters:
            filepath (str): Path to the input file.

        Returns:
            Graph: A graph loaded from the input file.
        """
        with open(filepath, 'r') as file:
            lines = file.readlines()
            nodes_count, _ = map(int, lines[0].split())
            graph = cls(nodes_count)
            for line in lines[1:]:
                from_node, to_node = map(int, line.split())
                graph.add_edge(from_node, to_node)
        return graph


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Usage: python Graph.py <input_file> [start_node]")
        sys.exit(1)

    file_name = sys.argv[1]
    graph = Graph.from_file(file_name)

    # If a start node is provided, use it. Otherwise, default to 0.
    start_node = int(sys.argv[2]) if len(sys.argv) > 2 else 0

    # For BFS
    graph.display_bfs(start_node)

    # For Topological Sort
    sorted_order = graph.topological_sort()
    print("Topological Order:", sorted_order)
