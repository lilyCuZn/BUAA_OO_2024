public class Counter {
    private int requestsCount = 0;

    private RequestPool requestPool;

    public Counter(RequestPool requestPool) {
        this.requestPool = requestPool;
    }

    public synchronized void addCount() {
        requestsCount++; //来一条request，加一
        synchronized (requestPool) {
            requestPool.notifyAll();
        }
    }

    public synchronized void subCount() {
        requestsCount--; //完成一个request，减一
        synchronized (requestPool) {
            requestPool.notifyAll();
        }
    }

    public int getCount() {
        return requestsCount;
    }
}
