package linked_list_set;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class SetImpl implements Set {
    private class Node {
        AtomicMarkableReference<Node> N;
        int x;

        Node(int x, Node next) {
            this.N = new AtomicMarkableReference<>(next, false);
            this.x = x;
        }
    }

    private class Window {
        Node cur, next;
    }

    private final Node head = new Node(Integer.MIN_VALUE, new Node(Integer.MAX_VALUE, null));

    /**
     * Returns the {@link Window}, where cur.x < x <= N.x
     */
    private Window findWindow(int x) {
        retry: while (true) {
            Window w = new Window();
            w.cur = head;
            w.next = w.cur.N.getReference();
            boolean[] removed = new boolean[1];
            while (w.next.x < x) {
                Node node = w.next.N.get(removed);
                if (removed[0]) { // w.N is removed
                    if (!w.cur.N.compareAndSet(w.next, node, false, false))
                        continue retry;
                    w.next = node;
                } else {
                    w.cur = w.next;
                    w.next = node;
                }
            }
            while (true) {
                Node node = w.next.N.get(removed);
                if (removed[0]) {
                    if (!w.cur.N.compareAndSet(w.next, node, false, false))
                        continue retry;
                    w.next = node;
                }
                return w;
            }
        }
    }

    @Override
    public boolean add(int x) {
        while (true) {
            Window w = findWindow(x);
            if (w.next.x == x)
                return false;
            Node node = new Node(x, w.next);
            if (w.cur.N.compareAndSet(w.next, node, false, false))
                return true;
        }
    }

    @Override
    public boolean remove(int x) {
        while (true) {
            Window w = findWindow(x);
            if (w.next.x != x)
                return false;
            Node node = w.next.N.getReference();
            if (w.next.N.compareAndSet(node, node, false, true)) {
                w.cur.N.compareAndSet(w.next, node, false, false);
                return true;
            }
        }
    }

    @Override
    public boolean contains(int x) {
        Window w = findWindow(x);
        return w.next.x == x;
    }
}