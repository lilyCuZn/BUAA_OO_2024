public class Main {
    public static void main(String[]args) throws InterruptedException{
        System.out.println("MultiThread demo");
        for (int i = 0; i < 10; i++) {
            new Test().start();
        }
        Thread.sleep(3000);
        System.out.println("Single Thread demo");
        for (int i = 0; i < 10; i++) {
            new Test().run();
        }
    }
}
