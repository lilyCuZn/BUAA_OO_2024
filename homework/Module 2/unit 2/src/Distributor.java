import com.oocourse.elevator2.TimableOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Distributor extends Thread {
    private RequestPool requestPool;

    private HashMap<Integer, InRequests> inRequestsOfElevators;
    //inRequests of 6 Elevators

    private HashMap<Integer, Elevator> elevators;

    private int elevatorNum;

    private int floorNum;

    private Counter requestsCounter;

    private Object distributorLock;

    private boolean logall;

    private boolean log = true;

    public Distributor(RequestPool requestPool, HashMap<Integer, Elevator> elevators,
                       HashMap<Integer, InRequests> inRequestsOfElevators, Counter requestsCounter,
                       int elevatorNum, int floorNum, Object distributorLock, boolean logall) {
        this.requestPool = requestPool;
        this.inRequestsOfElevators = inRequestsOfElevators;
        this.elevators = elevators;
        this.elevatorNum = elevatorNum;
        this.floorNum = floorNum;
        this.requestsCounter = requestsCounter;
        this.distributorLock = distributorLock;
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
            /*
            for (int i = 1; i <= elevatorNum; i++) {
                Elevator elevator = elevators.get(i);
                if (log) {
                    System.out.println("-----分析" + i + "号电梯接这个乘客要多久-----");
                }
                Random random = new Random();
                elevatorToDistribute = random.nextInt(6) + 1;

                ShadowElevator shadowElevator = new ShadowElevator(elevator, floorNum,
                        elevatorNum, person, logall);
                shadowElevator.start();
                try {
                    //等待线程执行完毕
                    shadowElevator.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int time = shadowElevator.getTime();
                if (log) {
                    TimableOutput.println(i + "号电梯来接" + person.getID() + "号乘客一共需要花：" + time);
                }
                if (time < minTime && !elevator.isReset()) {
                    minTime = time;
                    elevatorToDistribute = i;
                }
                //有点担心reset和distribute时间线的问题
                //不知道线程执行时间……

            }

             */
            if (elevatorToDistribute != -1) {
                synchronized (inRequestsOfElevators.get(elevatorToDistribute)) {
                    if (!elevators.get(elevatorToDistribute).isReset()) {
                        TimableOutput.println("RECEIVE-" + person.getID() +
                                "-" + elevatorToDistribute);
                        inRequestsOfElevators.get(elevatorToDistribute).addRequest(person);
                        //担心出现先delALL再addRequest的情况……
                        //调整了这两句话的前后顺序,不知道能不能解决先RECEIVE再RESET的问题
                        if (log) {
                            System.out.println("*****" + person.getID() +
                                    "号乘客被分配给了这个电梯：" + elevatorToDistribute);
                        }
                        break;
                    }
                    else if (elevators.get(elevatorToDistribute).isReset()) {
                        elevators.get(elevatorToDistribute).addReceiveRequest(person);
                        break;
                    }
                }
            }
        }
    }
}
