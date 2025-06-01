
import $$jwdebug.$$$TraceRecorder;

public class Demo {

    public static void main(String[] args) {
        $$$TraceRecorder.enterMethod("Demo", "main");
        $$$TraceRecorder.setLocalVariable("args", args);
        Node root = (Node)$$$TraceRecorder.setLocalVariable("root", $$$TraceRecorder.recordNewObjectCreation(new Node(10)));
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
        System.out.print("In-order traversal: ");
        root.printInOrder();
        System.out.println();
        System.out.println("Contains 7: " + root.contains(7));
        System.out.println("Contains 20: " + root.contains(20));
        root.insert(20);
        System.out.println("Contains 20 after insertion: " + root.contains(20));
        $$$TraceRecorder.exitMethod();
        $$$TraceRecorder.verifyFinished();
    }

    static class Node {
        int value;
        Node left;
        Node right;

        public Node(int value) {
            $$$TraceRecorder.enterMethod("Node", "<init>");
            $$$TraceRecorder.setLocalVariable("value", value);
            this.value = $$$TraceRecorder.setField(this, "value", value);
            this.left = $$$TraceRecorder.setField(this, "left", (Node)null);
            this.right = $$$TraceRecorder.setField(this, "right", (Node)null);
            $$$TraceRecorder.exitMethod();
        }

        public void insert(int value) {
            $$$TraceRecorder.enterMethod("Node", "insert");
            $$$TraceRecorder.setLocalVariable("value", value);
            if ($$$TraceRecorder.recordCondition((value < this.value), "(value < this.value)")) {
                if ($$$TraceRecorder.recordCondition((this.left == null), "(this.left == null)")) {
                    this.left = $$$TraceRecorder.setField(this, "left", $$$TraceRecorder.recordNewObjectCreation(new Node(value)));
                } else {
                    this.left.insert(value);
                }
            } else if (value > this.value) {
                if ($$$TraceRecorder.recordCondition((this.right == null), "(this.right == null)")) {
                    this.right = $$$TraceRecorder.setField(this, "right", $$$TraceRecorder.recordNewObjectCreation(new Node(value)));
                } else {
                    this.right.insert(value);
                }
            }
            $$$TraceRecorder.exitMethod();
        }

        public boolean contains(int value) {
            $$$TraceRecorder.enterMethod("Node", "contains");
            $$$TraceRecorder.setLocalVariable("value", value);
            if ($$$TraceRecorder.recordCondition((value == this.value), "(value == this.value)")) {
                $$$TraceRecorder.exitMethod();
                return $$$TraceRecorder.returnValue(true);
            } else if (value < this.value) {
                $$$TraceRecorder.exitMethod();
                return $$$TraceRecorder.returnValue(this.left != null && this.left.contains(value));
            } else {
                $$$TraceRecorder.exitMethod();
                return $$$TraceRecorder.returnValue(this.right != null && this.right.contains(value));
            }
            $$$TraceRecorder.exitMethod();
        }

        public void printInOrder() {
            $$$TraceRecorder.enterMethod("Node", "printInOrder");
            if ($$$TraceRecorder.recordCondition((this.left != null), "(this.left != null)")) {
                this.left.printInOrder();
            }
            System.out.print(this.value + " ");
            if ($$$TraceRecorder.recordCondition((this.right != null), "(this.right != null)")) {
                this.right.printInOrder();
            }
            $$$TraceRecorder.exitMethod();
        }
    }
}