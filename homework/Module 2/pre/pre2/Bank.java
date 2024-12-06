
class Bank extends Thread{
    private int money;
    private String name;

    public String getname() {
        return name;
    }

    public int getMoney(){
        return money;
    }

    // synchronized修饰方法，锁为Bank对象关联的锁
    public synchronized void deposit(int m) {
        // 每次只能有一个Customer线程进入这个方法
        money += m;
    }
    public synchronized void withdraw(int m) {
        // 每次只能有一个Customer线程进入这个方法
        money -= m;
    }
    public static void main(String[]args) throws InterruptedException {
        Bank bank=new Bank();
        Customer.bank=bank;
        for(int i=1;i <= 10; i++ ){
            new Customer().start();
        }
        Thread.sleep(3000); //很重要，否则会在Customer线程没结束的时候打印
        System.out.println(bank.getMoney());
        // 由于保证了互斥，最后结果一定是0
    }
}