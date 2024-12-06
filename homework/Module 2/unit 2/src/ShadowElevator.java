import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ShadowElevator extends Thread {
    //不会sleep的影子电梯
    private int time = 0;

    private boolean log = false;

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

    private RequestPool requestPool;

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

    public ShadowElevator(Elevator elevator, int floorNum, int elevatorNum,
                                            Person person, boolean logall) {
        //要注意把person放进inRequests里面...
        time = 0;
        //根据现有的elevator深克隆出一个shadowElevator
        this.elevatorID = elevator.getElevatorID();
        this.floorNum = floorNum;
        this.elevatorNum = elevatorNum;
        this.capacity = elevator.getCapacity();
        this.moveTime = elevator.getMoveTime();
        this.inRequests = elevator.getInRequests().createSame();
        this.wantToReset = elevator.getWantToReset();
        this.isReset = elevator.isReset();
        this.inRequests.addRequest(person);
        inRequests.setEnd(); //需要让这个线程有一个结束条件
        this.pos = elevator.getPos();
        passengerNum = elevator.getPassengerNum();
        direction = elevator.getDirection(); //初始时为上行
        opened = elevator.getOpened();
        outRequests = elevator.getOutRequestsCreateSame();
        if (!logall) {
            log = false;
        }
        strategy = new Strategy(inRequests, floorNum, logall, true);
    }

    public void wantToReset(int capacity, double speed) {
        this.wantToReset = true;
        resetCapacity = capacity;
        resetMoveTime = (int) speed * 1000;
    }

    public boolean getWantToReset() {
        return wantToReset;
    }

    public boolean isReset() {
        return isReset;
    }

    public synchronized void reset() throws InterruptedException {
        isReset = true;
        //先把乘客都请出去
        OpenOutMidwayAndClose(outRequests.get(pos));
        //开始reset
        //TimableOutput.println("RESET_BEGIN-" + elevatorID);
        capacity = resetCapacity;
        moveTime = resetMoveTime;
        resetCapacity = -1;
        resetMoveTime = -1;
        time += 1200;

        isReset = false;
        wantToReset = false;
        //TimableOutput.println("RESET_END-" + elevatorID);
    }

    public void up() throws InterruptedException {
        /*if (LOG) {
            System.out.println(elevatorID);
        }*/
        if (opened) {
            close();
        }
        time += moveTime;
        pos++;
        // TimableOutput.println("ARRIVE-" + pos + "-" + elevatorID);
    }

    public void down() throws InterruptedException {
        if (opened) {
            close();
        }
        time += moveTime;
        pos--;
        // TimableOutput.println("ARRIVE-" + pos + "-" + elevatorID);
    }

    public void open() throws InterruptedException {
        // TimableOutput.println("OPEN-" + pos + "-" + elevatorID); //开始开门
        opened = true;
        time += openTime;
    }

    public void close() throws InterruptedException {
        time += closeTime;
        //TimableOutput.println("CLOSE-" + pos + "-" + elevatorID); //完成关门
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

            //TimableOutput.println("IN-" + passengerID + "-" + pos + "-" + elevatorID);

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
                    //TimableOutput.println("OUT-" + passengerID + "-" + pos + "-" + elevatorID);
                    it.remove();
                    passengerNum--;
                }
            }
        }
    }

    public void OpenOutMidwayAndClose(ArrayList<Person> passengerList) throws InterruptedException {
        open();

        //reset时让乘客中途下去
        out(passengerList); //让该层本来就要下去的人先出去
        //再让剩下的人中途下去
        Iterator it = passengerList.iterator();
        while (it.hasNext()) {
            Person nextPerson = (Person) it.next();
            Integer passengerID = nextPerson.getID();
            //TimableOutput.println("OUT-" + passengerID + "-" + pos + "-" + elevatorID);
            requestPool.addRequest(nextPerson);
            it.remove();
            passengerNum--;
        }

        close();
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

    public int getTime() {
        return time;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Advice advice = strategy.getAdvice(pos, passengerNum, direction,
                                                    wantToReset, outRequests, capacity);
                if (log) {
                    System.out.println(advice.toString() + ":" + elevatorID);
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
    }

}
