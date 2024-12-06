import com.oocourse.elevator2.TimableOutput;
import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.ResetRequest;
import com.oocourse.elevator2.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InputThread extends Thread {
    private boolean log = true;

    private int elevatorNum;

    private int floorNum;

    private int capacity;

    private HashMap<Integer, Elevator> elevators;

    private ElevatorInput elevatorInput;

    private RequestPool requestPool; //和inRequests数据类型一样

    private Counter requestsCounter;

    public InputThread(boolean logall, int elevatorNum, int floorNum, int capacity,
                                    RequestPool requestPool, HashMap<Integer, Elevator> elevators,
                                    Counter requestsCounter) {
        this.elevatorNum = elevatorNum;
        this.floorNum = floorNum;
        this.capacity = capacity;
        this.requestPool = requestPool;
        this.elevators = elevators;
        this.requestsCounter = requestsCounter;
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
            if (request instanceof ResetRequest) { //假如是reset指令
                ResetRequest resetRequest = (ResetRequest) request;
                int elevatorID = resetRequest.getElevatorId();
                int capacity = resetRequest.getCapacity();
                double speed = resetRequest.getSpeed();
                if (log) {
                    TimableOutput.println("InputThread中，reset.speed:" + speed);
                }
                elevators.get(elevatorID).wantToReset(capacity, speed);
            } else if (request instanceof PersonRequest) {
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
}
