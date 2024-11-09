package dsa;
public class StringQueue {
    private Node front;
    private Node rear;
    private int size;

    public StringQueue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    private static class Node {
        private String data;
        private Node next;

        public Node(String data) {
            this.data = data;
            this.next = null;
        }
    }

    public void add(String data) {
        Node newNode = new Node(data);
        if (rear == null) { 
            front = rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    public String poll() {
        if (isEmpty()) {
            return null;
        }
        String data = front.data;
        front = front.next;

        if (front == null) {
            rear = null;
        }
        size--;
        return data;
    }

    public String peek() {
        if (isEmpty()) {
            return null;
        }
        return front.data;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public int size() {
        return size;
    }

    public void clear() {
        front = rear = null;
        size = 0;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "Queue is empty";
        }
        StringBuilder sb = new StringBuilder();
        Node current = front;
        while (current != null) {
            sb.append(current.data).append(" -> ");
            current = current.next;
        }
        sb.append("null");
        return sb.toString();
    }
}
