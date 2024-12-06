import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;

import java.io.IOException;
import java.util.ArrayList;

public class Distributor extends Thread {
    private boolean log = true;
    private int elevatorNum;

    private int floorNum;

    private int capacity; //每个电梯的容量

    private ArrayList<Elevator> elevators;

    private ElevatorInput elevatorInput;

    private ArrayList<ArrayList<Entry>> requestPool;

    private int requestNum;

    public Distributor(int elevatorNum, int floorNum, int capacity, boolean logall) {
        requestNum = 0;
        requestPool = new ArrayList<>();
        this.elevatorNum = elevatorNum;
        this.floorNum = floorNum;
        this.capacity = capacity;
        elevators = new ArrayList<Elevator>();
        if (!logall) {
            log = false;
        }
        ArrayList<Entry> tempList = new ArrayList<>();
        tempList.add(new Entry(0));

        elevators.add(0, new Elevator(0, 0, 0, 0, tempList, logall));
        requestPool.add(0, new ArrayList<>());
        //初始化第0项，都为0，无效项
        for (int i = 1; i <= elevatorNum; i++) {
            ArrayList<Entry> inRequests = new ArrayList<>();
            inRequests.add(0, new Entry(0));
            for (int j = 1; j <= floorNum; j++) {
                Entry entry = new Entry(floorNum);
                inRequests.add(j, entry);
            }
            requestPool.add(i, inRequests);
            //elevator.start();要在哪里？？
            Elevator elevator = new Elevator(i, floorNum, elevatorNum,
                    capacity, inRequests, logall);
            elevators.add(i, elevator);
            elevator.start();
        }
    }

    @Override
    public void run() {
        elevatorInput = new ElevatorInput(System.in);
        PersonRequest request;
        while (true) {
            request = elevatorInput.nextPersonRequest();
            if (request == null) {
                for (int i = 1; i <= elevatorNum; i++) {
                    this.closeElevator(i);
                }
                break;
            } else {
                if (log) {
                    System.out.println("这个request是ta的：" + request.getPersonId());
                }
                requestNum++;
                if (log) {
                    System.out.println(request.getPersonId());
                    System.out.println("现在有几个request了：" + requestNum);
                }
                distribute(request); //要用到try-catch吗？
                //distribute：把request分配给不同电梯的inRequests
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //防止因为输入错误导致整个程序都终止了
        //不知道有没有这个必要……？
    }

    public void closeElevator(int elevatorID) {
        synchronized (requestPool.get(elevatorID)) { //不知道为什么要同步
            Entry entry = requestPool.get(elevatorID).get(0); //0楼要进入的乘客：用这个list表示一些需要同步的电梯状态
            entry.getToFloorList().get(0).set(0, 1); //表示这个电梯已经关闭了
            requestPool.get(elevatorID).notifyAll();
        }
    }

    public void distribute(PersonRequest request) {
        int elevatorID = request.getElevatorId();
        int from = request.getFromFloor();
        int to = request.getToFloor();
        int personID = request.getPersonId();
        synchronized (requestPool.get(elevatorID)) { //思考一下为什么需要同步:因为elevator线程也会访问inRequests
            Entry entry = requestPool.get(elevatorID).get(from);
            entry.addPassenger(to, personID);
            if (log) {
                System.out.println("加入了" + personID + "号乘客，ta要去" + to);
            }
            //乘坐elevatorID的、从from层出发的乘客中，加入一个要到to层的personID。
            requestPool.get(elevatorID).notifyAll();
        }
    }
}
