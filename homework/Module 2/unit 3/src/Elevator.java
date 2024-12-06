import com.oocourse.elevator3.TimableOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Elevator extends Thread {
    private boolean log = true;

    private boolean isDouble = false;

    private boolean doubleID = false; //false表示在下区，true表示在上区
    //当isDouble = false时无所谓下区上区，默认为false

    private int transferFloor = -1;

    private Object doubleLock;

    private Flag flag;

    private volatile int moveTime = 400; //让其它线程读取moveTime时能看到最新值

    private volatile int capacity; //容量：让其它线程读取moveTime时能看到最新值

    private int resetMoveTime = -1;

    private int resetCapacity = -1;

    private int resetTransferFloor = -1;

    private final int openTime = 200;

    private final int closeTime = 200;

    private int elevatorID;

    private boolean direction;

    private int passengerNum; //现有乘客数量

    private int pos; //当前位置

    private boolean opened = false; //电梯门有没有打开？

    private volatile boolean wantToReset = false;

    private volatile boolean wantToDoubleReset = false;

    private boolean isReset = false;

    private boolean isDoubleReset = false;

    private Strategy strategy;

    private InRequests inRequests; //每层楼想要进来的人，是【动态的】
    //distributor随着输入把request分给各个电梯的inRequests，所以需要【上锁】

    private InRequests otherInRequests; //另一部双轿厢电梯的inRequests

    private HashMap<Integer, ArrayList<Person>> outRequests;
    //已经进入电梯的、每层楼想要出去的人

    private Buffer buffer; //在reset期间被分配给电梯的、还没receive的人们

    private RequestPool requestPool;

    private Counter requestsCounter;

    public void addBuffer(Person person) {
        this.buffer.addRequest(person);
    }

    public int getElevatorID() {
        return elevatorID;
    }

    public InRequests getInRequests() {
        return inRequests;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public HashMap<Integer, ArrayList<Person>> getOutRequests() {
        return outRequests;
    }

    public Flag getFlag() {
        return flag;
    }

    public Object getDoubleLock() {
        return doubleLock;
    }

    public boolean isDouble() {
        return isDouble;
    }

    public int getTransferFloor() {
        return transferFloor;
    }

    public Elevator(int elevatorID, int floorNum, int capacity, int moveTime, boolean isDouble,
                    boolean doubleID, int pos, InRequests inRequests, HashMap<Integer,
                    ArrayList<Person>> outRequests, Buffer buffer,
                    RequestPool requestPool, Counter requestsCounter, Object doubleLock,
                    int transferFloor, Flag flag, boolean logall) {
        this.elevatorID = elevatorID;
        this.capacity = capacity;
        this.inRequests = inRequests;
        this.outRequests = outRequests;
        this.buffer = buffer;
        this.isDouble = isDouble;
        this.pos = pos;
        this.moveTime = moveTime;
        passengerNum = 0;
        this.transferFloor = transferFloor;
        direction = true; //初始时为上行
        opened = false;
        this.doubleID = doubleID;
        this.requestsCounter = requestsCounter;
        if (!logall) {
            log = false;
        }
        this.doubleLock = doubleLock;
        this.flag = flag;
        strategy = new Strategy(inRequests, floorNum, logall, false, outRequests);
        this.requestPool = requestPool;
    }

    private void printOut(int passengerID, int pos, int elevatorID) {
        if (!isDouble) {
            TimableOutput.println(
                    "OUT-" + passengerID + "-" + pos + "-" + elevatorID);
        } else {
            char doubleChar;
            doubleChar = (!doubleID) ? 'A' : 'B';
            TimableOutput.println(
                    "OUT-" + passengerID + "-" + pos + "-" + elevatorID + "-" + doubleChar);
        }
    }

    public void setOtherInRequests(InRequests inRequests) {
        this.otherInRequests = inRequests;
    }

    private void printIn(int passengerID, int pos, int elevatorID) {
        if (!isDouble) {
            TimableOutput.println(
                    "IN-" + passengerID + "-" + pos + "-" + elevatorID);
        } else {
            char doubleChar;
            doubleChar = (!doubleID) ? 'A' : 'B';
            TimableOutput.println(
                    "IN-" + passengerID + "-" + pos + "-" + elevatorID + "-" + doubleChar);
        }
    }

    private void printArrive(int pos, int elevatorID) {
        if (!isDouble) {
            TimableOutput.println("ARRIVE-" + pos + "-" + elevatorID);
        } else {
            char doubleChar;
            doubleChar = (!doubleID) ? 'A' : 'B';
            TimableOutput.println("ARRIVE-" + pos + "-" + elevatorID + "-" + doubleChar);
        }
    }

    private void printOpen(int pos, int elevatorID) {
        if (!isDouble) {
            TimableOutput.println("OPEN-" + pos + "-" + elevatorID);
        } else {
            char doubleChar;
            doubleChar = (!doubleID) ? 'A' : 'B';
            TimableOutput.println("OPEN-" + pos + "-" + elevatorID + "-" + doubleChar);
        }
    }

    private void printClose(int pos, int elevatorID) {
        if (!isDouble) {
            TimableOutput.println("CLOSE-" + pos + "-" + elevatorID);
        } else {
            char doubleChar;
            doubleChar = (!doubleID) ? 'A' : 'B';
            TimableOutput.println("CLOSE-" + pos + "-" + elevatorID + "-" + doubleChar);
        }
    }

    public void wantToReset(int capacity, double speed) {
        this.wantToReset = true;
        resetCapacity = capacity;
        resetMoveTime = (int) (speed * 1000);
        synchronized (inRequests) {
            inRequests.notifyAll(); //打断原来的wait,让strategy能判断下一步
        }
    }

    public void wantToDoubleReset(int transferFloor, int capacity, double speed) {
        this.wantToDoubleReset = true;
        this.wantToReset = true;
        resetCapacity = capacity;
        resetMoveTime = (int) (speed * 1000);
        resetTransferFloor = transferFloor;
        synchronized (inRequests) {
            inRequests.notifyAll();
        }
    }

    public boolean isReset() {
        return isReset;
    }

    public void reset() throws InterruptedException {
        isReset = true;
        OpenOutMidwayAndClose();
        synchronized (inRequests) { //reset期间，inrequests不应该加东西
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
            buffer.receive(inRequests, otherInRequests, this);
            inRequests.notifyAll();
        }
        synchronized (requestPool) {
            requestPool.notifyAll(); //提醒distributor注意看看有没有结束
        }
    }

    public void doubleReset() throws InterruptedException {
        isReset = true;
        OpenOutMidwayAndClose();
        synchronized (inRequests) { //reset期间，inrequests不应该加东西
            isDoubleReset = true;
            //开始reset
            TimableOutput.println("RESET_BEGIN-" + elevatorID);
            //取消所有RECEIVE
            inRequests.delAll(requestPool); //这个应该要在reset-begin之后？
            //互测WA
            capacity = resetCapacity;
            moveTime = resetMoveTime;
            transferFloor = resetTransferFloor;
            if (log) {
                System.out.println("doubleReset后，capacity为：" +
                        capacity + ", moveTime为：" + moveTime);
            }
            pos = transferFloor - 1;
            sleep(1200);
            TimableOutput.println("RESET_END-" + elevatorID);
            isReset = false;
            isDoubleReset = false;
            wantToDoubleReset = false;
            wantToReset = false;
            isDouble = true;
            buffer.receive(inRequests, otherInRequests, this); //把rest期间积累的receive都输出
            synchronized (doubleLock) {
                doubleLock.notifyAll(); //提醒B梯线程可以开始动了
            }
            inRequests.notifyAll();
        }
        synchronized (requestPool) {
            requestPool.notifyAll(); //提醒distributor注意看看有没有结束
        }
    }

    public void open() throws InterruptedException {
        printOpen(pos, elevatorID);//开始开门
        opened = true;
        sleep(openTime);
    }

    public void close() throws InterruptedException {
        sleep(closeTime);
        printClose(pos, elevatorID); //完成关门
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
            synchronized (outRequests) {
                if (!outRequests.containsKey(sameDirectPerson.getToFloor())) {
                    outRequests.put(sameDirectPerson.getToFloor(), new ArrayList<>());
                    //假如outRequests里没有到toFloor的人，那么把这个地方初始化
                }
                outRequests.get(sameDirectPerson.getToFloor()).add(sameDirectPerson);
            }
            passengerNum++;
            int passengerID = sameDirectPerson.getID();
            printIn(passengerID, pos, elevatorID);
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
                    printOut(passengerID, pos, elevatorID);
                    it.remove();
                    passengerNum--;
                    requestsCounter.subCount();
                }
            }
        }
    }

    public void outFurther() throws InterruptedException {
        synchronized (outRequests) {
            for (Map.Entry<Integer, ArrayList<Person>> entry : outRequests.entrySet()) {
                Integer floor = entry.getKey();
                ArrayList<Person> value = entry.getValue();
                if ((floor > transferFloor && doubleID == false) ||
                        (floor < transferFloor && doubleID == true)) { //送不到目的地
                    Iterator it = value.iterator();
                    while (it.hasNext()) {
                        Person nextPerson = (Person) it.next();
                        nextPerson.setFromFloor(pos);
                        Integer passengerID = nextPerson.getID();
                        printOut(passengerID, pos, elevatorID);
                        requestPool.addRequest(nextPerson);
                        it.remove();
                        passengerNum--;
                    }
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
        if (passengerNum != 0) {
            if (!opened) {
                open();
            }
            synchronized (outRequests) {
                for (Map.Entry<Integer, ArrayList<Person>> entry : outRequests.entrySet()) {
                    ArrayList<Person> value = entry.getValue();

                    Iterator it = value.iterator();
                    while (it.hasNext()) {
                        Person nextPerson = (Person) it.next();
                        nextPerson.setFromFloor(pos);
                        Integer passengerID = nextPerson.getID();
                        printOut(passengerID, pos, elevatorID);
                        requestPool.addRequest(nextPerson);
                        it.remove();
                        passengerNum--;
                    }
                }
            }
        }
        if (opened) {
            close();
        }
    }

    public void move() throws InterruptedException {
        int derta = (direction) ? 1 : -1;
        pos = pos + derta;
        try {
            sleep(moveTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (isDouble && pos == transferFloor) {
            flag.setOccupied();
        }
        printArrive(pos, elevatorID);
        if (isDouble && (pos - derta == transferFloor)) {
            flag.setRelease();
        }
    }

    public void openAndClose() throws InterruptedException {
        //让到达目的地的人出电梯，再让送不到的人出电梯，再让想上电梯的、且目的地方向和电梯方向相同的乘客上电梯
        if (!opened) {
            open();
        }
        if (outRequests.get(pos).isEmpty() == false) {
            out(outRequests.get(pos));
            //让达到目的地的人全部出电梯
        }
        if (isDouble && (pos == transferFloor)) {
            outFurther(); //这个时候还在电梯里的人，应该都是要往远处走的人？
        }
        //有同方向的、在这层门口等候的旅客，让这层楼等待的【ta们】都上
        inSameDirectPerson();
        if (opened) {
            close();
        }
    }

    public void initialize() throws InterruptedException {
        if (doubleID == true) {
            synchronized (doubleLock) {
                try {
                    doubleLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            initialize();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            if (log) {
                System.out.println("【elevator】");
            }
            try {
                Advice advice = strategy.getAdvice(pos, passengerNum, direction,
                        wantToReset, wantToDoubleReset, capacity,
                        this, doubleID);
                if (log) {
                    if (!isDouble) {
                        TimableOutput.println(advice.toString() + ":" + elevatorID);
                    }
                    if (isDouble) {
                        char aorb = (!doubleID) ? 'A' : 'B';
                        TimableOutput.println(advice.toString() + ":" + elevatorID + "-" + aorb);
                    }
                }
                if (advice == Advice.DOUBLERESET) {
                    doubleReset();
                }
                if (advice == Advice.RESET) {
                    reset();
                }
                if (advice == Advice.REVERSEANDMOVE) {
                    direction = !direction;
                    move();
                }
                if (advice == Advice.OVER) {
                    break;
                } else if (advice == Advice.MOVE) {
                    move();
                } else if (advice == Advice.REVERSE) {
                    direction = !direction;
                } else if (advice == Advice.WAIT) {
                    synchronized (inRequests) {
                        inRequests.wait();
                    }
                } else if (advice == Advice.OPEN) {
                    openAndClose();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
