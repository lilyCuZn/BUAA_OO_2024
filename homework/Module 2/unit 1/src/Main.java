import com.oocourse.elevator1.TimableOutput;

public class Main {
    private static final int elevatorNum = 6;

    private static final int floorNum = 11;

    private static final int capacity = 6;

    private static final boolean LOGALL = false;

    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        Distributor distributor = new Distributor(elevatorNum, floorNum, capacity, LOGALL);
        distributor.start();
    }
}
