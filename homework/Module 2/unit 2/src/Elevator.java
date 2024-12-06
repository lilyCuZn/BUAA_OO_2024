import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Elevator extends Thread {
    private boolean log = true;
    private volatile int moveTime = 400; //让其它线程读取moveTime时能看到最新值

    private volatile int capacity; //容量：让其它线程读取moveTime时能看到最新值

    private int resetMoveTime = -1;

    private int resetCapacity = -1;

    private int openTime = 200;

    private int closeTime = 200;

    private int elevatorID;

    private int floorNum;

    private int elevatorNum;

    private boolean direction;

    private int passengerNum; //现有乘客数量

    private int pos; //当前位置

    private boolean opened = false; //电梯门有没有打开？

    private volatile boolean wantToReset = false;

    private boolean isReset = false;

    private Strategy strategy;

    private InRequests inRequests;
    //每层楼想要进来的人，是【动态的】
    //distributor随着输入把request分给各个电梯的inRequests，所以需要【上锁】

    private HashMap<Integer, ArrayList<Person>> outRequests;
    //已经进入电梯的、每层楼想要出去的人
    //似乎也需要【上锁】？

    private InRequests receiveRequests; //在reset期间被分配给电梯的、还没receive的人们

    private RequestPool requestPool;

    private Counter requestsCounter;

    private Object distributorLock;

    public int getElevatorID() {
        return elevatorID;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getMoveTime() {
        return moveTime;
    }

    public boolean getDirection() {
        return direction;
    }

    public boolean getOpened() {
        return opened;
    }

    public void addReceiveRequest(Person person) {
        this.receiveRequests.addRequest(person);
    }

    public HashMap<Integer, ArrayList<Person>> getOutRequestsCreateSame() {
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

    public int getPos() {
        return pos;
    }

    public int getPassengerNum() {
        return passengerNum;
    }

    public InRequests getInRequests() {
        return inRequests;
    }

    public boolean inRequestsIsEmpty() {
        return inRequests.isEmpty();
    }

    public boolean inRequestsIsOver() {
        return inRequests.isEnd(); //在Strategy中有判断empty且Over才会结束线程
    }

    public Elevator(int elevatorID, int floorNum, int elevatorNum, int capacity,
                    InRequests inRequests, InRequests receiveRequests, RequestPool requestPool,
                    Counter requestsCounter, Object distributorLock, boolean logall) {
        this.elevatorID = elevatorID;
        this.floorNum = floorNum;
        this.elevatorNum = elevatorNum;
        this.capacity = capacity;
        this.inRequests = inRequests;
        this.receiveRequests = receiveRequests;
        this.pos = 1;
        passengerNum = 0;
        direction = true; //初始时为上行
        opened = false;
        this.distributorLock = distributorLock;
        this.requestsCounter = requestsCounter;
        if (!logall) {
            log = false;
        }
        strategy = new Strategy(inRequests, floorNum, logall, false);
        outRequests = new HashMap<>();
        this.requestPool = requestPool;
        for (int i = 1; i <= floorNum; i++) {
            outRequests.put(i, new ArrayList<>());
        } //初始化，防止outRequests.get(pos)为空
    }

    public void wantToReset(int capacity, double speed) {
        this.wantToReset = true;
        resetCapacity = capacity;
        resetMoveTime = (int) (speed * 1000);
        synchronized (inRequests) {
            inRequests.notifyAll(); //打断原来的wait,让strategy能判断下一步
        }
        if (log) {
            TimableOutput.println("这个电梯wanttoreset：" + elevatorID);
            System.out.println("speed:" + speed);
            System.out.println("resetMoveTime:" + resetMoveTime);
        }
    }

    public boolean getWantToReset() {
        return wantToReset;
    }

    public boolean isReset() {
        return isReset;
    }

    public void reset() throws InterruptedException {
        OpenOutMidwayAndClose();
        synchronized (inRequests) { //reset期间，inrequests不应该加东西
            isReset = true;

            //开始reset
            TimableOutput.println("RESET_BEGIN-" + elevatorID);

            //取消所有RECEIVE
            inRequests.delAll(requestPool); //这个应该要在reset-begin之后？
            //互测WA

            capacity = resetCapacity;
            moveTime = resetMoveTime;
            if (log) {
                System.out.println("reset后，capacity为：" + capacity + ", moveTime为：" + moveTime);
            }
            resetCapacity = -1;
            resetMoveTime = -1;

            sleep(1200);


            TimableOutput.println("RESET_END-" + elevatorID);

            isReset = false;
            wantToReset = false;

            receiveRequests.receiveAllRequests(elevatorID); //把rest期间积累的receive都输出
            inRequests.addManyRequests(receiveRequests);

            synchronized (distributorLock) {
                distributorLock.notify(); //用于所有电梯都在reset时distributor-wait
                //现在没用了
            }
            inRequests.notifyAll();
        }
        synchronized (requestPool) {
            requestPool.notifyAll(); //提醒distributor注意看看有没有结束
        }
    }

    public void up() throws InterruptedException {
        /*if (LOG) {
            System.out.println(elevatorID);
        }*/
        if (opened) {
            close();
        }
        sleep(moveTime);
        pos++;
        TimableOutput.println("ARRIVE-" + pos + "-" + elevatorID);
    }

    public void down() throws InterruptedException {
        if (opened) {
            close();
        }
        sleep(moveTime);
        pos--;
        TimableOutput.println("ARRIVE-" + pos + "-" + elevatorID);
    }

    public void open() throws InterruptedException {
        TimableOutput.println("OPEN-" + pos + "-" + elevatorID); //开始开门
        opened = true;
        sleep(openTime);
    }

    public void close() throws InterruptedException {
        sleep(closeTime);
        TimableOutput.println("CLOSE-" + pos + "-" + elevatorID); //完成关门
        opened = false;
    }

    private void inSameDirectPerson() {
        //有同方向的、在这层门口等候的旅客，让这层楼等待的【ta们】都上
        //ArrayList<Person> list = inRequests.getRequests().get(pos);
        while (passengerNum < capacity) {
            Person sameDirectPerson = inRequests.getOneSameDirectPerson(pos, direction);
            if (sameDirectPerson == null) {
                break;
            }
            if (!outRequests.containsKey(sameDirectPerson.getToFloor())) {
                outRequests.put(sameDirectPerson.getToFloor(), new ArrayList<>());
                //假如outRequests里没有到toFloor的人，那么把这个地方初始化
            }
            outRequests.get(sameDirectPerson.getToFloor()).add(sameDirectPerson);
            passengerNum++;
            int passengerID = sameDirectPerson.getID();

            TimableOutput.println(
                    "IN-" + passengerID + "-" + pos + "-" + elevatorID);

            inRequests.delRequest(pos, sameDirectPerson);
        }
    }

    public void out(ArrayList<Person> passengerLists) throws InterruptedException {
        //把pos层要下的乘客都送出去
        synchronized (passengerLists) {
            Iterator it = passengerLists.iterator();
            while (it.hasNext()) {
                Person nextPerson = (Person) it.next();
                Integer passengerID = nextPerson.getID();
                if (nextPerson.getToFloor() == pos) { //这个乘客确实要在当前位置出去
                    TimableOutput.println(
                            "OUT-" + passengerID + "-" + pos + "-" + elevatorID);
                    it.remove();
                    passengerNum--;
                    requestsCounter.subCount();
                }
            }
        }
    }

    public void OpenOutMidwayAndClose() throws InterruptedException {
        //reset时让乘客中途下去
        ArrayList<Person> passengerList = outRequests.get(pos);
        //本来就要在这里下去的乘客,直接下去
        if (!passengerList.isEmpty()) {
            if (!opened) {
                open();
            }
            out(passengerList);
        }
        //再让剩下的人中途下去,注意需要改他们的起始地址！！
        if (passengerNum != 0)
        {
            if (!opened) {
                open();
            }
            for (Map.Entry<Integer, ArrayList<Person>> entry : outRequests.entrySet()) {
                Integer key = entry.getKey();
                ArrayList<Person> value = entry.getValue();

                Iterator it = value.iterator();
                while (it.hasNext()) {
                    Person nextPerson = (Person) it.next();
                    nextPerson.setFromFloor(pos);
                    Integer passengerID = nextPerson.getID();
                    TimableOutput.println(
                            "OUT-" + passengerID + "-" + pos + "-" + elevatorID);
                    requestPool.addRequest(nextPerson);
                    it.remove();
                    passengerNum--;
                }
            }
        }
        if (opened) {
            close();
        }
    }

    public void move() throws InterruptedException {
        if (direction == true) {
            up();
        }
        else if (direction == false) {
            down();
        }
    }

    public void openAndClose() throws InterruptedException {
        //让到达目的地的人出电梯，再让想上电梯的、且目的地方向和电梯方向相同的乘客上电梯
        open();

        if (outRequests.get(pos).isEmpty() == false) {
            out(outRequests.get(pos));
            //让达到目的地的人全部出电梯
        }
        //有同方向的、在这层门口等候的旅客，让这层楼等待的【ta们】都上
        inSameDirectPerson();

        close();
    }

    public int getPickUpTime(Person person) {
        int time = 0;
        int pickUpPos = person.getFromFloor(); //在这里接上那个人


        return time;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Advice advice = strategy.getAdvice(pos, passengerNum, direction,
                        wantToReset, outRequests, capacity);
                if (log) {
                    TimableOutput.println(advice.toString() + ":" + elevatorID);
                }
                if (advice == Advice.RESET) {
                    reset();
                }
                //输出RESET_BEGIN-电梯ID 和输出RESET_END-电梯ID之间电梯不得参与电梯调度
                //即不能开关门、进出乘客、上下楼层、输出RECEIVE（见RECEIVE约束）等，处于静默状态。
                if (advice == Advice.OVER) {
                    break;
                }
                else if (advice == Advice.MOVE) {
                    move();
                }
                else if (advice == Advice.REVERSE) {
                    direction = !direction;
                }
                else if (advice == Advice.WAIT) {
                    synchronized (inRequests) {
                        inRequests.wait();
                    }
                }
                else if (advice == Advice.OPEN) {
                    openAndClose();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (log) {
            TimableOutput.println("这个电梯over了：" + elevatorID);
        }
    }
}
