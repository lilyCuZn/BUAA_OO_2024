import com.oocourse.elevator2.TimableOutput;

import java.util.HashMap;

public class Main {
    private static final int elevatorNum = 6;

    private static final int floorNum = 11;

    private static final int capacity = 6;

    private static final boolean LOGALL = false;

    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        RequestPool requestPool = new RequestPool(LOGALL);
        HashMap<Integer, InRequests> inRequestsOfElevators = new HashMap<>();
        HashMap<Integer, Elevator> elevators = new HashMap<>();
        Counter requestsCounter = new Counter(requestPool);
        Object distributorLock = new Object();

        for (int i = 1; i <= elevatorNum; i++) {
            InRequests inRequests = new InRequests(LOGALL);
            InRequests receiveRequests = new InRequests(LOGALL);
            Elevator elevator = new Elevator(i, floorNum, elevatorNum, capacity,
                    inRequests, receiveRequests, requestPool, requestsCounter,
                    distributorLock, LOGALL);
            inRequestsOfElevators.put(i, inRequests);
            elevators.put(i, elevator);
            elevator.start();
        }

        InputThread inputThread = new InputThread(LOGALL, elevatorNum, floorNum,
                capacity, requestPool, elevators, requestsCounter);
        inputThread.start();

        Distributor distributor = new Distributor(requestPool, elevators, inRequestsOfElevators,
                requestsCounter, elevatorNum, floorNum, distributorLock, LOGALL);
        distributor.start();
    }
}
