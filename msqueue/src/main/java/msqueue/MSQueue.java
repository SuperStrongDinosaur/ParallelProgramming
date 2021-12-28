package msqueue;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public class MSQueue implements Queue {
  /*  private final AtomicReference<Node> head;
    private final AtomicReference<Node> tail;

    public MSQueue() {
        Node dummy = new Node(-1);
        this.head = new AtomicReference<>(dummy);
        this.tail = new AtomicReference<>(dummy);
    }

    @Override
    public void enqueue(int x) {
        Node newTail = new Node(x);
        while(true) {
            Node curTail = tail.get();
            if(tail.get().next.compareAndSet(null, newTail)) {
                tail.compareAndSet(curTail, newTail);
                return;
            } else {
                tail.compareAndSet(curTail, curTail.next.get());
            }
        }
    }

    @Override
    public int dequeue() {
        while(true) {
            Node curHead = head.get();
            Node curTail = tail.get();
            if(curHead != curTail) {
                Node curHeadNext = curHead.next.get();
                if(head.compareAndSet(curHead, curHeadNext)) {
                    return curHeadNext.x;
                }
            } else {
                if(curTail.next == null) throw new NoSuchElementException();
                tail.compareAndSet(curTail, curTail.next.get());
            }
        }
    }

    @Override
    public int peek() {
        while(true) {
            Node curHead = head.get();
            Node curTail = tail.get();
            if(curHead != curTail) {
                return curHead.next.get().x;
            } else {
                if(curTail.next == null) throw new NoSuchElementException();
                tail.compareAndSet(curTail, curTail.next.get());
            }
        }
    }

    private class Node {
        final int x;
        final AtomicReference<Node> next = new AtomicReference<>(null);
        Node(int x) {
            this.x = x;
        }
    }*/

    private class Node {
        int value;
        AtomicReference<Node> next;

        Node(int val) {
            value = val;
            next = new AtomicReference(null);
        }
    }

    public MSQueue() {
        Node dummy = new Node(-1);
        this.head = new AtomicReference<>(dummy);
        this.tail = new AtomicReference<>(dummy);
    }

    private final AtomicReference<Node> head;
    private final AtomicReference<Node> tail;

    @Override
    public void enqueue(int value) {
        Node node = new Node(value);
        while (true) {
            Node last = tail.get();
            Node next = last.next.get();
            if (last == tail.get()) {
                if (next == null) {
                    if (last.next.compareAndSet(next, node)) {
                        tail.compareAndSet(last, node);
                        return;
                    }
                } else {
                    tail.compareAndSet(last, next);
                }
            }
        }
    }

    @Override
    public int dequeue() {
        while (true) {
            Node first = head.get();
            Node last = tail.get();
            Node next = first.next.get();
            if (first == head.get()) {
                if (first == last) {
                    if (next == null) {
                        throw new NoSuchElementException();
                        }
                    tail.compareAndSet(last, next);
                    } else {
                    int value = next.value;
                    if (head.compareAndSet(first, next))
                        return value;
                    }
                }
            }
        }

    @Override
    public int peek() {
      /*  Node first = head.get();
        Node last = tail.get();
        Node next = first.next.get();
        if(first == last)
            throw new NoSuchElementException();
        return next.value;*/

        while (true) {
            Node first = head.get();
            Node last = tail.get();
            Node next = first.next.get();
            if (first == head.get()) {
                if (first == last) {
                    if (next == null) {
                        throw new NoSuchElementException();
                    }
                    tail.compareAndSet(last, next);
                } else {
                    return next.value;
                }
            }
        }
    }

}