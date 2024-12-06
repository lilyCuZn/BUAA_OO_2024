public class Test extends Thread{
    public void run() {
        System.out.println(currentThread().getName() + ":" + "Hello OO!");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(currentThread().getName() + ":I love OO!");
    }
}
