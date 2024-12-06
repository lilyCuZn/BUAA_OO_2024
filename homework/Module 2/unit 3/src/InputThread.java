import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.TimableOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InputThread extends Thread {
    private boolean log = true;

    private boolean logall;

    private int elevatorNum;

    private int floorNum;

    private int capacity;

    private HashMap<Integer, Elevator> elevators;

    private ElevatorInput elevatorInput;

    private RequestPool requestPool; //和inRequests数据类型一样

    private Counter requestsCounter;

    private HashMap<Integer, InRequests> inRequestsOfElevators;

    public InputThread(boolean logall, int elevatorNum, int floorNum, int capacity,
                                    RequestPool requestPool, HashMap<Integer, Elevator> elevators,
                                    Counter requestsCounter,
                       HashMap<Integer, InRequests> inRequestsOfElevators) {
        this.elevatorNum = elevatorNum;
        this.floorNum = floorNum;
        this.capacity = capacity;
        this.requestPool = requestPool;
        this.elevators = elevators;
        this.requestsCounter = requestsCounter;
        this.inRequestsOfElevators = inRequestsOfElevators;
        this.logall = logall;
        if (!logall) {
            log = false;
        }
    }

    @Override
    public void run() {
        elevatorInput = new ElevatorInput(System.in);
        Request request;
        while (true) {
            request = elevatorInput.nextRequest();
            if (request == null) {
                requestPool.setEnd();
                break;
            }
            if (request instanceof NormalResetRequest) { //假如是普通的reset指令
                NormalResetRequest normalResetRequest = (NormalResetRequest) request;
                int elevatorID = normalResetRequest.getElevatorId();
                int capacity = normalResetRequest.getCapacity();
                double speed = normalResetRequest.getSpeed();
                if (log) {
                    TimableOutput.println("InputThread中，reset.speed:" + speed);
                }
                elevators.get(elevatorID).wantToReset(capacity, speed);
            } else if (request instanceof DoubleCarResetRequest) {
                DoubleCarResetRequest doubleReset = (DoubleCarResetRequest) request;
                int elevatorID = doubleReset.getElevatorId();
                int transferFloor = doubleReset.getTransferFloor();
                int capacity = doubleReset.getCapacity();
                double speed = doubleReset.getSpeed();

                Elevator elevatorA = elevators.get(elevatorID);
                InRequests otherInRequests = new InRequests(logall);
                synchronized (inRequestsOfElevators) {
                    inRequestsOfElevators.put(elevatorID + 6, otherInRequests);
                } //把elevatorB的inRequests也放进这个list里，方便distributor。setEnd
                elevatorA.setOtherInRequests(otherInRequests); //用于bufferReceive

                elevatorA.wantToDoubleReset(transferFloor, capacity, speed);
                int moveTime = (int) (1000 * speed);
                Elevator elevatorB = new Elevator(elevatorID, floorNum, capacity, moveTime,
                        true, true,
                        transferFloor + 1, otherInRequests, newOutRequests(), //都是新建的空的
                        elevatorA.getBuffer(), requestPool, requestsCounter,
                        elevatorA.getDoubleLock(),
                        transferFloor, elevatorA.getFlag(), log);
                //两个电梯共享一个buffer
                synchronized (elevators) {
                    elevators.put(elevatorID + 6, elevatorB);
                }
                elevatorB.start();
            } else if (request instanceof PersonRequest) {
                if (log) {
                    System.out.println("接到了request，他是：" + ((PersonRequest) request).getPersonId());
                }
                PersonRequest personRequest = (PersonRequest) request;
                Person person = new Person(personRequest.getPersonId(),
                        personRequest.getFromFloor(), personRequest.getToFloor());
                requestPool.addRequest(person);
                requestsCounter.addCount();
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<Integer, ArrayList<Person>> newOutRequests() {
        HashMap<Integer, ArrayList<Person>> outRequests = new HashMap<>();
        for (int j = 1; j <= floorNum; j++) {
            outRequests.put(j, new ArrayList<>());
        } //初始化，防止outRequests.get(pos)为空
        return outRequests;
    }

    public HashMap<Integer, ArrayList<Person>> createSameOutRequests(HashMap<Integer,
            ArrayList<Person>> outRequests) {
        HashMap<Integer, ArrayList<Person>> sameOutRequests = new HashMap<>();

        for (Map.Entry<Integer, ArrayList<Person>> entry : outRequests.entrySet()) {
            Integer key = entry.getKey();
            ArrayList<Person> value = entry.getValue();

            ArrayList<Person> clonedList = new ArrayList<>();
            for (Person person : value) {
                clonedList.add(person.createSame());
            }

            sameOutRequests.put(key, clonedList);
        }
        return sameOutRequests;
    }
}
