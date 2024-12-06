public class Flag {
    enum State { OCCUPIED, UNOCCUPIED }

    private State state;

    public Flag() {
        this.state = State.UNOCCUPIED;
    }

    public synchronized void setOccupied() {
        waitRelease();
        state = State.OCCUPIED;
        notifyAll();
    }

    public synchronized void setRelease() {
        this.state = State.UNOCCUPIED;
        notifyAll();
    }

    public synchronized void waitRelease() {
        notifyAll();
        while (state == State.OCCUPIED) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
