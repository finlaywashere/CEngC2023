import random
import networkx as nx
import xml.etree.ElementTree as ET
import matplotlib.pyplot as plt

# Set the number of nodes you want in the graph
num_nodes = 100000  # Change this to your desired number of nodes

# Set the maximum and minimum latitude and longitude values for the bounding box
# Adjust these values to constrain the area
min_lat = 45.20
max_lat = 45.60
min_lon = -75.80
max_lon = -75.40

# Create an empty graph
G = nx.Graph()

# Function to generate random nodes within the specified bounding box
def generate_random_node():
    lat = random.uniform(min_lat, max_lat)
    lon = random.uniform(min_lon, max_lon)
    return lat, lon

# Add nodes to the graph
for node_id in range(1, num_nodes + 1):
    node_coords = generate_random_node()
    G.add_node(node_id, lat=node_coords[0], lon=node_coords[1])

# Function to generate random edges
def generate_random_edges(node, max_edges):
    num_edges = random.randint(1, max_edges)
    edges = random.sample(list(G.nodes() - {node}), num_edges)
    return [(node, neighbor) for neighbor in edges]

NUM_EDGES = 2  # Change this to your desired number of edges per node

# Add random edges to the nodes
for node in G.nodes():
    max_edges = min(2, num_nodes - 1)  # Maximum possible edges is 4 or less
    edges_to_add = generate_random_edges(node, max_edges)
    G.add_edges_from(edges_to_add)

# Create an XML representation of the graph
root = ET.Element("graph")

# Add nodes as node elements
for node, data in G.nodes(data=True):
    node_element = ET.SubElement(root, "node", id=str(node))
    ET.SubElement(node_element, "lat").text = str(data["lat"])
    ET.SubElement(node_element, "lon").text = str(data["lon"])

# Add edges as way elements
for u, v in G.edges():
    way_element = ET.SubElement(root, "way", id=str(random.randint(1, 10000)))  # Assign a random way ID
    ET.SubElement(way_element, "node").text = str(u)
    ET.SubElement(way_element, "node").text = str(v)

# Create an ElementTree object and write it to a file
tree = ET.ElementTree(root)
tree.write("random_graph.xml")

# Draw and save the graph as a PNG image with labels
if num_nodes < 1000:
    pos = nx.spring_layout(G)
    nx.draw(G, pos, with_labels=True, node_size=300, node_color='lightblue', font_size=10, font_color='black')
    plt.savefig("random_graph.png")
    plt.show()

print("Random graph saved to 'random_graph.xml' and 'random_graph.png'.")
