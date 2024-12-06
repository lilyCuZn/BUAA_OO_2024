class Customer extends Thread{
    public static Bank bank;
    public void run(){
        for(int i=1;i <= 10; i++ ){
            bank.deposit(100);
            try {
                Thread.sleep(100); //休眠0.1s
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            bank.withdraw(100);
        }
    }
}