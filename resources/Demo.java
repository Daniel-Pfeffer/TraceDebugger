public class Demo {
    public static void main(String[] args) {
        // Create a bst and insert some values
        Node root = new Node(10);
        root.insert(5);
        root.insert(15);
        root.insert(3);
        root.insert(7);
        root.insert(12);
        root.insert(18);
        root.insert(1);
        root.insert(4);
        root.insert(6);
        root.insert(8);

        // Print the values in order
        System.out.print("In-order traversal: ");
        root.printInOrder();
        System.out.println();

        // Check if the tree contains certain values
        System.out.println("Contains 7: " + root.contains(7));
        System.out.println("Contains 20: " + root.contains(20));
        // Insert new value and try again
        root.insert(20);
        System.out.println("Contains 20 after insertion: " + root.contains(20));
    }


    static class Node {
        int value;
        Node left;
        Node right;

        public Node(int value) {
            this.value = value;
            this.left = null;
            this.right = null;
        }

        public void insert(int value) {
            if (value < this.value) {
                if (this.left == null) {
                    this.left = new Node(value);
                } else {
                    this.left.insert(value);
                }
            } else if (value > this.value) {
                if (this.right == null) {
                    this.right = new Node(value);
                    throw new IllegalArgumentException("Value already exists in the tree: " + value);
                } else {
                    this.right.insert(value);
                }
            }
        }

        public boolean contains(int value) {
            if (value == this.value) {
                return true;
            } else if (value < this.value) {
                return this.left != null && this.left.contains(value);
            }
            return this.right != null && this.right.contains(value);
        }

        public void printInOrder() {
            if (this.left != null) {
                this.left.printInOrder();
            }
            System.out.print(this.value + " ");
            if (this.right != null) {
                this.right.printInOrder();
            }
        }
    }
}