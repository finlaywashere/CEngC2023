import random
import json

# Define the maximum number of nodes to be used as locations
NUM_NODES = 100000  # Change this to your desired maximum number of nodes

# Define a list of package sources
# PACKAGE_SOURCES = [32, 46]
NUM_PACKAGE_SOURCES = 500
PACKAGE_SOURCES = random.sample(range(1, NUM_NODES + 1), NUM_PACKAGE_SOURCES)
NUM_BOTS = 25
NUM_PACKAGES = 1500
BOT_MAX_CAPACITY = 50

# Generate bot information
bots = []
for bot_id in range(1, NUM_BOTS + 1):
    bot_location = random.randint(1, NUM_NODES)  # Random location for the bot
    bot_capacity = random.randint(20, BOT_MAX_CAPACITY)  # Random capacity for the bot (adjust the range as needed)
    bot_info = {
        "location": bot_location,
        "capacity": bot_capacity
    }
    bots.append(bot_info)

# Generate package information
packages = []
for package_id in range(1, NUM_PACKAGES + 1):
    package_source = random.choice(PACKAGE_SOURCES)  # Random source for the package
    package_destination = random.randint(1, NUM_NODES)  # Random destination for the package

    # Ensure the destination is different from the source
    while package_destination == package_source:
        package_destination = random.randint(1, NUM_NODES)

    package_info = {
        "source": package_source,
        "destination": package_destination
    }
    packages.append(package_info)

# Create the JSON output
output_data = {
    "bots": bots,
    "packages": packages
}

# Serialize the JSON data
json_output = json.dumps(output_data, indent=4)

# Write the JSON data to a file
with open("bot_packages.json", "w") as file:
    file.write(json_output)

print("Data saved to 'bot_packages.json'.")
