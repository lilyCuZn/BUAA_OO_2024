import com.oocourse.elevator3.TimableOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Distributor extends Thread {
    private RequestPool requestPool;

    private HashMap<Integer, InRequests> inRequestsOfElevators;
    //inRequests of 12 Elevators

    private HashMap<Integer, Elevator> elevators;

    private int elevatorNum;

    private int floorNum;

    private Counter requestsCounter;

    private boolean logall;

    private boolean log = true;

    public Distributor(RequestPool requestPool, HashMap<Integer, Elevator> elevators,
                       HashMap<Integer, InRequests> inRequestsOfElevators, Counter requestsCounter,
                       int elevatorNum, int floorNum, boolean logall) {
        this.requestPool = requestPool;
        this.inRequestsOfElevators = inRequestsOfElevators;
        this.elevators = elevators;
        this.elevatorNum = elevatorNum;
        this.floorNum = floorNum;
        this.requestsCounter = requestsCounter;
        this.logall = logall;
        if (!logall) {
            log = false;
        }
    }

    public void run() {
        while (true) {
            if (log) {
                TimableOutput.println("此时requestPool.isEnd:" + requestPool.isEnd());
                TimableOutput.println("此时requestPool.isEmpty:" + requestPool.isEmpty());
                TimableOutput.println("counter：" + requestsCounter.getCount());
            }
            if (requestPool.isEnd() && requestPool.isEmpty() && (requestsCounter.getCount() == 0)) {
                for (Map.Entry<Integer, InRequests> entry : inRequestsOfElevators.entrySet()) {
                    InRequests r = entry.getValue();
                    r.setEnd(); //六个电梯的inRequests都end
                }
                for (Map.Entry<Integer, InRequests> entry : inRequestsOfElevators.entrySet()) {
                    InRequests r = entry.getValue();
                    synchronized (r) {
                        r.notifyAll();
                    }
                }
                break;
            }
            Person person = requestPool.getOneRequestAndRemove();
            if (person == null) {
                synchronized (requestPool) {
                    try {
                        requestPool.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                continue;
            }
            try {
                distribute(person);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (log) {
            TimableOutput.println("distributor线程结束了");
        }
    }

    public void distribute(Person person) throws InterruptedException {
        int minTime = Integer.MAX_VALUE;
        int elevatorToDistribute = -1;

        //把person分配给某个电梯
        while (true) {
            Random random = new Random();
            elevatorToDistribute = random.nextInt(6) + 1;
            if (elevatorToDistribute != -1) {
                boolean isDouble = elevators.get(elevatorToDistribute).isDouble();
                int transferFloor = elevators.get(elevatorToDistribute).getTransferFloor();
                char doubleID = getDoubleEleId(person, isDouble, transferFloor);
                if (!elevators.get(elevatorToDistribute).isReset()) {
                    if (doubleID == 'X') {
                        synchronized (elevators.get(elevatorToDistribute).getInRequests()) {
                            TimableOutput.println("RECEIVE-" + person.getID() +
                                    "-" + elevatorToDistribute);
                            elevators.get(elevatorToDistribute).getInRequests().addRequest(person);
                        }
                    } else {
                        synchronized (elevators.get(elevatorToDistribute).getInRequests()) {
                            TimableOutput.println("RECEIVE-" + person.getID() +
                                    "-" + elevatorToDistribute + "-" + doubleID);
                            if (doubleID == 'A') {
                                elevators.get(elevatorToDistribute).
                                        getInRequests().addRequest(person);
                            } else { //B
                                elevators.get(elevatorToDistribute + 6).
                                        getInRequests().addRequest(person);
                            }
                        }
                    }
                    //担心出现先delALL再addRequest的情况……
                    //调整了这两句话的前后顺序,不知道能不能解决先RECEIVE再RESET的问题
                    if (log) {
                        System.out.println("*****" + person.getID() +
                                "号乘客被分配给了这个电梯：" + elevatorToDistribute);
                    }
                    break;
                }
                else if (elevators.get(elevatorToDistribute).isReset()) {
                    elevators.get(elevatorToDistribute).addBuffer(person);
                    break;
                }
            }
        }
    }

    public static char getDoubleEleId(Person person, boolean isDouble, int transferFloor) {
        if (!isDouble) {
            return 'X';
        } else {
            if (person.getFromFloor() < transferFloor) {
                return 'A';
            } else if (person.getFromFloor() > transferFloor) {
                return 'B';
            } else if (person.getToFloor() > transferFloor) {
                return 'B';
            } else if (person.getToFloor() < transferFloor) {
                return 'A';
            }
        }
        return 'X';
    }
}
