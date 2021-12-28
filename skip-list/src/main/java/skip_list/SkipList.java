package skip_list;

import java.util.Random;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class SkipList implements Set {
    static final int MAX_LEVEL = 30;

    private final Node head = new Node(Integer.MIN_VALUE);
    private final Node tail = new Node(Integer.MAX_VALUE);

    public SkipList() {
        for (int i = 0; i < head.next.length; i++) {
            head.next[i] = new AtomicMarkableReference(tail, false);
        }
    }

    private static final class Node {
        final int value;
        final AtomicMarkableReference<Node>[] next;
        private int topLevel;

        Node(int key) {
            value = key;
            next = new AtomicMarkableReference[MAX_LEVEL + 1];
            for (int i = 0; i < next.length; i++) {
                next[i] = new AtomicMarkableReference(null, false);
            }
            topLevel = MAX_LEVEL;
        }

        Node(int x, int height) {
            value = x;
            next = new AtomicMarkableReference[height + 1];
            for (int i = 0; i < next.length; i++) {
                next[i] = new AtomicMarkableReference(null, false);
            }
            topLevel = height;
        }
    }

    private Random levelRandom = new Random(0);

    private int randomLevel() {
        int newLevel = 0;
        while (newLevel < MAX_LEVEL - 1 && levelRandom.nextFloat() < 0.5f) {
            newLevel += 1;
        }
        return newLevel;
    }

    @Override
    public boolean add(int x) {
        int topLevel = randomLevel();
        int bottomLevel = 0;
        Node[] preds = new Node[MAX_LEVEL + 1];
        Node[] succs = new Node[MAX_LEVEL + 1];
        while (true) {
            boolean found = find(x, preds, succs);
            if (found) {
                return false;
            } else {
                Node newNode = new Node(x, topLevel);
                for (int level = bottomLevel; level <= topLevel; level++) {
                    Node succ = succs[level];
                    newNode.next[level].set(succ, false);
                }
                Node pred = preds[bottomLevel];
                Node succ = succs[bottomLevel];
                newNode.next[bottomLevel].set(succ, false);
                if (!pred.next[bottomLevel].compareAndSet(succ, newNode, false, false)) {
                    continue;
                }
                for (int level = bottomLevel + 1; level <= topLevel; level++) {
                    while (true) {
                        pred = preds[level];
                        succ = succs[level];
                        if (pred.next[level].compareAndSet(succ, newNode, false, false))
                            break;
                        find(x, preds, succs);
                    }
                }
                return true;
            }
        }
    }

    @Override
    public boolean remove(int x) {
        int bottomLevel = 0;
        Node[] preds = new Node[MAX_LEVEL + 1];
        Node[] succs = new Node[MAX_LEVEL + 1];
        Node succ;
        while (true) {
            boolean found = find(x, preds, succs);
            if (!found) {
                return false;
            } else {
                Node nodeToRemove = succs[bottomLevel];
                for (int level = nodeToRemove.topLevel; level >= bottomLevel + 1; level--) {
                    boolean[] marked = {false};
                    succ = nodeToRemove.next[level].get(marked);
                    while (!marked[0]) {
                        nodeToRemove.next[level].attemptMark(succ, true);
                        succ = nodeToRemove.next[level].get(marked);
                    }
                }
                boolean[] marked = {false};
                succ = nodeToRemove.next[bottomLevel].get(marked);
                while (true) {
                    boolean iMarkedIt = nodeToRemove.next[bottomLevel].compareAndSet(succ, succ, false, true);
                    succ = succs[bottomLevel].next[bottomLevel].get(marked);
                    if (iMarkedIt) {
                        find(x, preds, succs);
                        return true;
                    } else if (marked[0]) return false;
                }
            }
        }
    }

    private boolean find(int x, Node[] preds, Node[] succs) {
        int bottomLevel = 0;
        boolean[] marked = {false};
        boolean snip;
        Node pred = null, curr = null, succ = null;
        retry:while (true) {
            pred = head;
            for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
                curr = pred.next[level].getReference();
                while (true) {
                    succ = curr.next[level].get(marked);
                    while (marked[0]) {
                        snip = pred.next[level].compareAndSet(curr, succ, false, false);
                        if (!snip) continue retry;
                        curr = pred.next[level].getReference();
                        succ = curr.next[level].get(marked);
                    }
                    if (curr.value < x) {
                        pred = curr;
                        curr = succ;
                    } else {
                        break;
                    }
                }
                preds[level] = pred;
                succs[level] = curr;
            }
            return (curr.value == x);
        }
    }

    @Override
    public boolean contains(int x) {
        int bottomLevel = 0;
        boolean[] marked = {false};
        Node pred = head, curr = null, succ = null;
        for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
            curr = pred.next[level].getReference();
            while (true) {
                succ = curr.next[level].get(marked);
                while (marked[0]) {
                    curr = pred.next[level].getReference();
                    succ = curr.next[level].get(marked);
                }
                if (curr.value < x) {
                    pred = curr;
                    curr = succ;
                } else {
                    break;
                }
            }
          //  return (curr.value == x);
        }
        return (curr.value == x);
    }

  /*  @Override
    public boolean contains(int x) {
        int bottomLevel = 0;
        int key = x;
        Node pred = head;
        Node curr = null;
        for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
            curr = pred.next[level].getReference();
            while (curr.value < key ) {
                pred = curr;
                curr = pred.next[level].getReference();
                }
            }
        return curr.value == key;
        }*/

}