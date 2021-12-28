package of_deque;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicStampedReference;

public class ObstructionFreeDeque<T> implements Deque<T> {
    private static final Object RN = new Object();
    private static final Object LN = new Object();
    private static final Object DN = new Object();

    private static final boolean LEFT = false;
    private static final boolean RIGHT = true;

    private final int MAX;
    private final AtomicStampedReference<Object>[] values;
    private final int MOD;

    private boolean equal(AtomicStampedReference<Object> a, AtomicStampedReference<Object> b) {
        return (a.getStamp() == b.getStamp() && a.getReference() == b.getReference());
    }

    public ObstructionFreeDeque(int MAX) {
        this.MAX = MAX;
        int middle = MAX / 2;
        MOD = MAX + 2;
        values = new AtomicStampedReference[this.MAX + 2];
        for (int i = 0; i < middle; i++) {
            values[i] = new AtomicStampedReference<>(LN, 0);
        }

        for (int i = middle; i < values.length; i++) {
            values[i] = new AtomicStampedReference<>(RN, 0);
        }
    }

    private int Oracle(boolean direction) {
        int i = -1;
        while(i < 0 || i > MAX + 1) {
            if (direction == RIGHT) {
                for (i = MAX + 1; i >= 0; i--) {
                    if ((values[i].getReference() == RN || values[i].getReference() == DN) && values[(MAX + i + 1) % MOD].getReference() != RN)
                        return i;
                }
            } else {
                for (i = 0; i <= MAX + 1; i++) {
                    if ((values[i].getReference() == LN || values[i].getReference() == DN) && values[(i + 1) % MOD].getReference() != LN)
                        return i;
                }
            }
        }
        return -1;
    }

    private int RightCheckedOracle(AtomicStampedReference<Object> left, AtomicStampedReference<Object> right) {
        while (true) {
            int k = Oracle(RIGHT);

            left.set(values[(k - 1 + MOD) % MOD].getReference(), values[(k - 1 + MOD) % MOD].getStamp());
            right.set(values[k].getReference(), values[k].getStamp());

            if (right.getReference() == RN && left.getReference() != RN)
                return k;

            if (right.getReference() == DN && !(left.getReference() == RN || left.getReference() == DN)) {
                if (values[(k - 1 + MOD) % (MOD)].compareAndSet(left.getReference(), left.getReference(), left.getStamp(), left.getStamp() + 1))  {
                    if(values[k].compareAndSet(right.getReference(), RN, right.getStamp(), right.getStamp() + 1)) {
                        left.set(left.getReference(), left.getStamp() + 1);
                        right.set(RN, right.getStamp() + 1);
                        return k;
                    }
                }
            }
        }
    }

    private int LeftCheckedOracle(AtomicStampedReference<Object> right, AtomicStampedReference<Object> left) {
        while (true) {
            int k = Oracle(LEFT);

            right.set(values[(k + 1 + MOD) % MOD].getReference(), values[(k + 1 + MOD) % MOD].getStamp());
            left.set(values[k].getReference(), values[k].getStamp());

            if (left.getReference() == LN && right.getReference() != LN)
                return k;

            if (left.getReference() == DN && !(right.getReference() == LN || right.getReference() == DN)) {
                if(values[(k + 1 + MOD) % MOD].compareAndSet(right.getReference(), right.getReference(), right.getStamp(), right.getStamp() + 1)) {
                    if(values[k].compareAndSet(left.getReference(), LN, left.getStamp(), left.getStamp() + 1)) {
                        right.set(right.getReference(), right.getStamp() + 1);
                        left.set(LN, left.getStamp() + 1);
                        return k;
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the deque is full
     */
    @Override
    public void pushFirst(T x) {
        AtomicStampedReference<Object> prev, cur, next, nextnext;

        prev = new AtomicStampedReference<>(DN, 0);
        cur = new AtomicStampedReference<>(DN, 0);
        next = new AtomicStampedReference<>(DN, 0);
        nextnext = new AtomicStampedReference<>(DN, 0);

        while (true) {
            int k = RightCheckedOracle(prev, cur);

            next.set(values[(k + 1 + MOD) % MOD].getReference(), values[(k + 1 + MOD) % MOD].getStamp());

            if (next.getReference() == RN)
                if (values[(k - 1 + MOD) % MOD].compareAndSet(prev.getReference(), prev.getReference(), prev.getStamp(), prev.getStamp() + 1))
                    if (values[k].compareAndSet(cur.getReference(), x, cur.getStamp(), cur.getStamp() + 1))
                        return;

            if (next.getReference() == LN)
                if (values[k].compareAndSet(cur.getReference(), RN, cur.getStamp(), cur.getStamp() + 1))
                    values[(k + 1 + MOD) % MOD].compareAndSet(next.getReference(), DN, next.getStamp(), next.getStamp() + 1);

            if (next.getReference() == DN) {
                nextnext.set(values[(k + 2 + MOD) % MOD].getReference(), values[(k + 2 + MOD) % MOD].getStamp());

                if (!(nextnext.getReference() == RN || nextnext.getReference() == LN || nextnext.getReference() == DN) &&
                    equal(values[(k - 1 + MOD) % MOD], prev) &&
                        equal(values[k], cur)) throw new IllegalStateException();

                if (nextnext.getReference() == LN)
                    if (values[(k + 2 + MOD) % MOD].compareAndSet(nextnext.getReference(), nextnext.getReference(), nextnext.getStamp(), nextnext.getStamp() + 1))
                        values[(k + 1 + MOD) % MOD].compareAndSet(next.getReference(), RN, next.getStamp(), next.getStamp() + 1);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the deque is full
     */
    @Override
    public void pushLast(T x) {
        AtomicStampedReference<Object> prev, cur, next, nextnext;

        prev = new AtomicStampedReference<>(DN, 0);
        cur = new AtomicStampedReference<>(DN, 0);
        next = new AtomicStampedReference<>(DN, 0);
        nextnext = new AtomicStampedReference<>(DN, 0);

        while (true) {
            int k = LeftCheckedOracle(prev, cur);

            next.set(values[(k - 1  + MOD) % MOD].getReference(), values[(k - 1 + MOD) % MOD].getStamp());

            if (next.getReference() == LN)
                if (values[(k + 1 + MOD) % MOD].compareAndSet(prev.getReference(), prev.getReference(), prev.getStamp(), prev.getStamp() + 1))
                    if (values[k].compareAndSet(cur.getReference(), x, cur.getStamp(), cur.getStamp() + 1))
                        return;

            if (next.getReference() == RN)
                if (values[k].compareAndSet(cur.getReference(), LN, cur.getStamp(), cur.getStamp() + 1))
                    values[(k - 1 + MOD) % MOD].compareAndSet(next.getReference(), DN, next.getStamp(), next.getStamp() + 1);

            if (next.getReference() == DN) {
                nextnext.set(values[(k - 2 + MOD) % MOD].getReference(), values[(k - 2 + MOD) % MOD].getStamp());

                if (!(nextnext.getReference() == LN || nextnext.getReference() == RN || nextnext.getReference() == DN) &&
                    equal(values[(k + 1 + MOD) % MOD], prev) &&
                        equal(values[k], cur)) throw new IllegalStateException();

                if (nextnext.getReference() == RN)
                    if (values[(k - 2 + MOD) % MOD].compareAndSet(nextnext.getReference(), nextnext.getReference(), next.getStamp(), nextnext.getStamp() + 1))
                        values[(k - 1 + MOD) % MOD].compareAndSet(next.getReference(), LN, next.getStamp(), next.getStamp() + 1);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T peekFirst() {
        AtomicStampedReference<Object> cur, next;

        next = new AtomicStampedReference<>(DN, 0);
        cur = new AtomicStampedReference<>(DN, 0);

        while (true) {
            int k = RightCheckedOracle(cur, next);

            if ((cur.getReference() == LN || cur.getReference() == DN) && equal(values[(k - 1 + MOD) % MOD], cur))
                throw new NoSuchElementException();

            if (values[k].compareAndSet(next.getReference(), RN, next.getStamp(), next.getStamp() + 1))
                if (values[(k - 1 + MOD) % MOD].compareAndSet(cur.getReference(), cur.getReference(), cur.getStamp(), cur.getStamp() + 1))
                    return (T) cur.getReference();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T peekLast() {
        AtomicStampedReference<Object> cur, next;

        next = new AtomicStampedReference<>(DN, 0);
        cur = new AtomicStampedReference<>(DN, 0);

        while (true) {
            int k = LeftCheckedOracle(cur, next);

            if ((cur.getReference() == RN || cur.getReference() == DN) && equal(values[(k + 1 + MOD) % MOD], cur))
                throw new NoSuchElementException();

            if (values[k].compareAndSet(next.getReference(), LN, next.getStamp(), next.getStamp() + 1))
                if (values[(k + 1 + MOD) % MOD].compareAndSet(cur.getReference(), cur.getReference(), cur.getStamp(), cur.getStamp() + 1))
                    return (T) cur.getReference();
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public T popFirst() {
        AtomicStampedReference<Object> cur, next;

        next = new AtomicStampedReference<>(DN, 0);
        cur = new AtomicStampedReference<>(DN, 0);

        while (true) {
            int k = RightCheckedOracle(cur, next);

            if ((cur.getReference() == LN || cur.getReference() == DN) && equal(values[(k - 1 + MOD) % MOD], cur))
                throw new NoSuchElementException();

            if (values[k].compareAndSet(next.getReference(), RN, next.getStamp(), next.getStamp() + 1))
                if (values[(k - 1 + MOD) % MOD].compareAndSet(cur.getReference(), RN, cur.getStamp(), cur.getStamp() + 1))
                    return (T) cur.getReference();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T popLast() {
        AtomicStampedReference<Object> cur, next;

        next = new AtomicStampedReference<>(DN, 0);
        cur = new AtomicStampedReference<>(DN, 0);

        while (true) {
            int k = LeftCheckedOracle(cur, next);

            if ((cur.getReference() == RN || cur.getReference() == DN) && equal(values[(k + 1 + MOD) % MOD], cur))
                throw new NoSuchElementException();

            if (values[k].compareAndSet(next.getReference(), LN, next.getStamp(), next.getStamp() + 1))
                if (values[(k + 1 + MOD) % MOD].compareAndSet(cur.getReference(), LN, cur.getStamp(), cur.getStamp() + 1))
                    return (T) cur.getReference();
        }
    }
}