package of_deque;

public class main {

    public static void main(String[] args) {
        ObstructionFreeDeque<Integer> a = new ObstructionFreeDeque<>(100);
        for (int i = 0; i < 10; i++) {
            a.pushFirst(i);
        }
        for (int i = 0; i < 10; i++) {
            a.pushLast(i);
        }
        for(int i = 0; i < 10; i++) {
            System.out.println(a.peekLast());
            System.out.println(a.popLast());
            System.out.println(a.peekFirst());
            System.out.println(a.popFirst());
        }

        for(int i = 1; i < 6; i++) {
            //a.pushFirst(i);
            a.pushLast(i);
            System.out.println(a.popFirst());
            //a.pushLast(i);
        }
      //  System.out.println(a.popFirst());
    }

}
