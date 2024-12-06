import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
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

        for (int i = 1; i <= elevatorNum; i++) {
            InRequests inRequests = new InRequests(LOGALL);
            Buffer buffer = new Buffer(LOGALL);
            HashMap<Integer, ArrayList<Person>> outRequests = new HashMap<>();
            for (int j = 1; j <= floorNum; j++) {
                outRequests.put(j, new ArrayList<>());
            } //初始化，防止outRequests.get(pos)为空
            Object doubleLock = new Object();
            Flag flag = new Flag();
            int posA = 1;
            Elevator elevatorA = new Elevator(i, floorNum, capacity, 400, false, false, posA,
                    inRequests, outRequests, buffer, requestPool, requestsCounter,
                    doubleLock, -1, flag, LOGALL);
            inRequestsOfElevators.put(i, inRequests);
            elevators.put(i, elevatorA);
            elevatorA.start();
        }
        InputThread inputThread = new InputThread(LOGALL, elevatorNum, floorNum,
                capacity, requestPool, elevators, requestsCounter, inRequestsOfElevators);
        inputThread.start();

        Distributor distributor = new Distributor(requestPool, elevators, inRequestsOfElevators,
                requestsCounter, elevatorNum, floorNum, LOGALL);
        distributor.start();
    }
}
