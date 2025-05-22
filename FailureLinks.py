from collections import defaultdict, deque

class TrieNode:
    def __init__(self):
        self.children = {}
        self.is_end = False
        self.failure = None
        self.output = []  # List of patterns that end at this node
        self.node_id = None

class AhoCorasick:
    def __init__(self):
        self.root = TrieNode()
        self.root.failure = self.root
        self.node_counter = 0
        self.root.node_id = self.node_counter
        self.node_counter += 1

    def add_pattern(self, pattern):
        """Add a pattern to the trie"""
        node = self.root
        for char in pattern:
            if char not in node.children:
                node.children[char] = TrieNode()
                node.children[char].node_id = self.node_counter
                self.node_counter += 1
            node = node.children[char]
        node.is_end = True
        node.output.append(pattern)

    def build_failure_links(self):
        """Build failure links using BFS"""
        queue = deque()

        # Initialize failure links for depth-1 nodes (direct children of root)
        for char, child in self.root.children.items():
            child.failure = self.root
            queue.append(child)

        # BFS to build failure links for deeper nodes
        while queue:
            current = queue.popleft()

            for char, child in current.children.items():
                queue.append(child)

                # Find the failure link for this child
                failure_node = current.failure

                # Keep following failure links until we find a node that has
                # a transition for 'char' or we reach the root
                while failure_node != self.root and char not in failure_node.children:
                    failure_node = failure_node.failure

                # Set the failure link
                if char in failure_node.children and failure_node.children[char] != child:
                    child.failure = failure_node.children[char]
                else:
                    child.failure = self.root

                # Copy output patterns from failure node
                child.output.extend(child.failure.output)

    def get_failure_links_representation(self):
        """Get a readable representation of failure links"""
        result = []

        def traverse(node, path=""):
            if node.node_id is not None:
                failure_path = self.get_path_to_node(node.failure) if node.failure else "None"
                result.append({
                    'node_id': node.node_id,
                    'path': path if path else "ROOT",
                    'failure_node_id': node.failure.node_id if node.failure else None,
                    'failure_path': failure_path,
                    'is_end': node.is_end,
                    'output': node.output
                })

            for char, child in sorted(node.children.items()):
                traverse(child, path + char)

        traverse(self.root)
        return result

    def get_path_to_node(self, target_node):
        """Find path from root to a given node"""
        if target_node == self.root:
            return "ROOT"

        def find_path(node, path=""):
            if node == target_node:
                return path

            for char, child in node.children.items():
                result = find_path(child, path + char)
                if result is not None:
                    return result
            return None

        return find_path(self.root) or "UNKNOWN"

# Your keywords
keywords = [
    "bonus", "get", "free", "100", "win", "deposit", "claim",
    "real", "cash", "account", "makukuha", "join", "gcash",
    "daily", "iyong", "new", "libreng", "please", "deposito",
    "libre", "manalo", "winner", "play", "day", "visit", "sign",
    "ka", "pesos", "register", "enjoy", "welcome", "deposito",
    "libreng", "daily"
]

# Remove duplicates while preserving order
unique_keywords = []
seen = set()
for keyword in keywords:
    if keyword not in seen:
        unique_keywords.append(keyword)
        seen.add(keyword)

print(f"Processing {len(unique_keywords)} unique keywords:")
print(unique_keywords)
print("\n" + "="*80 + "\n")

# Build the Aho-Corasick automaton
ac = AhoCorasick()

# Add all patterns
for pattern in unique_keywords:
    ac.add_pattern(pattern)

# Build failure links
ac.build_failure_links()

# Get failure links representation
failure_links = ac.get_failure_links_representation()

print("AHO-CORASICK FAILURE LINKS")
print("="*80)
print(f"{'Node ID':<8} {'Path':<12} {'Failure ID':<10} {'Failure Path':<15} {'End?':<5} {'Output'}")
print("-" * 80)

for link in failure_links:
    output_str = str(link['output']) if link['output'] else "[]"
    if len(output_str) > 30:
        output_str = output_str[:27] + "..."

    print(f"{link['node_id']:<8} {link['path']:<12} {str(link['failure_node_id']):<10} "
          f"{link['failure_path']:<15} {str(link['is_end']):<5} {output_str}")

print("\n" + "="*80)
print("FAILURE LINK ANALYSIS")
print("="*80)

# Analyze some interesting failure links
interesting_cases = []
for link in failure_links:
    if link['failure_path'] != "ROOT" and link['path'] != "ROOT":
        interesting_cases.append(link)

print(f"\nFound {len(interesting_cases)} non-trivial failure links:")
print("-" * 50)

for case in interesting_cases[:15]:  # Show first 15 interesting cases
    print(f"Node '{case['path']}' → fails to '{case['failure_path']}'")
    if case['output']:
        print(f"  Outputs: {case['output']}")

# Show some specific examples of how failure links work
print("\n" + "="*80)
print("FAILURE LINK EXAMPLES")
print("="*80)

print("\nExample 1: Overlapping suffixes")
print("- When matching 'daily' and we're at 'd-a-i-l-y'")
print("- If next char doesn't match, we can fall back to a shorter suffix")

print("\nExample 2: Common prefixes")
print("- Keywords like 'deposit' and 'deposito' share prefix 'deposit'")
print("- Failure links help avoid re-scanning shared portions")

print("\nExample 3: Substring patterns")
print("- If 'get' appears within 'register', failure links ensure we don't miss it")

# Show root connections
root_connections = [link for link in failure_links if link['failure_path'] == "ROOT" and link['path'] != "ROOT"]
print(f"\n{len(root_connections)} nodes connect directly to ROOT via failure links")
print("This means they have no proper suffix that's also a prefix of any pattern.")