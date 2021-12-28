package stack;

import java.util.EmptyStackException;
import java.util.concurrent.atomic.AtomicReference;

public class StackImpl implements Stack {
    private class Node {
        final Node next;
        final int x;

        Node(int x, Node next) {
            this.next = next;
            this.x = x;
        }
    }

    private final AtomicReference<Node> top = new AtomicReference<>(null);

    @Override
    public void push(int x) {
        while(true) {
            Node node = new Node(x, top.get());
            if(top.compareAndSet(node.next, node)) {
                return;
            }
        }
    }

    @Override
    public int pop() {
        while(true) {
            Node node = top.get();
            if(node == null) {
                throw new EmptyStackException();
            }
            if(top.compareAndSet(node, node.next)) {
                return node.x;
            }
        }
    }
}
