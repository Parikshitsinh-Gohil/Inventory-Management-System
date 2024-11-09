package dsa;
import java.util.Iterator;

public class CustomLinkedList implements Iterable<String> {
    private Node head;
    private Node tail;
    private int size;

    private static class Node {
        String data;
        Node next;

        Node(String data) {
            this.data = data;
            this.next = null;
        }
    }

    public CustomLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void addLast(String data) {
        Node newNode = new Node(data);
        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public String removeLast() {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty. Cannot remove.");
        }
        if (head == tail) {
            String data = head.data;
            head = tail = null;
            size--;
            return data;
        }

        Node current = head;
        while (current.next != tail) {
            current = current.next;
        }
        String data = tail.data;
        tail = current;
        tail.next = null;
        size--;
        return data;
    }

    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "List is empty";
        }
        StringBuilder sb = new StringBuilder();
        Node current = head;
        while (current != null) {
            sb.append(current.data).append(" -> ");
            current = current.next;
        }
        sb.append("null");
        return sb.toString();
    }

    @Override
    public Iterator<String> iterator() {
        return new CustomLinkedListIterator();
    }

    private class CustomLinkedListIterator implements Iterator<String> {
        private Node current = head;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            String data = current.data;
            current = current.next;
            return data;
        }
    }
}
