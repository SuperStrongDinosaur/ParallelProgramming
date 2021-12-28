import msqueue.MSQueue;

public class main {
    public static void main(String[] args) {
        MSQueue a = new MSQueue();
        for(int i = 0; i < 10; i++)
            a.enqueue(i);
        for(int i = 0; i < 10; i++) {
            System.out.println(a.peek());
            System.out.println(a.dequeue());
        }
    }
}
