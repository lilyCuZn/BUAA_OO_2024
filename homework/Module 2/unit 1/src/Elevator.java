import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;

public class Elevator extends Thread {
    private boolean log = true;
    private int moveTime = 400;

    private int openTime = 200;

    private int closeTime = 200;

    private int elevatorID;

    private int floorNum;

    private int elevatorNum;

    private boolean direction;

    private int passengerNum; //现有乘客数量

    private int capacity; //容量

    private int dest; //主目标

    private int pos; //当前位置

    private boolean opened = false; //电梯门有没有打开？

    private boolean exit = false; //电梯有没有close？

    private Strategy strategy;

    private ArrayList<Entry> inRequests; //每层楼想要进来的人，是【动态的】
    //distributor随着输入把request分给各个电梯的inRequests，所以需要【上锁】

    private ArrayList<ArrayList<Integer>> outRequests; //已经进入电梯的、每层楼想要出去的人
    //似乎也需要【上锁】？

    public boolean inRequestsIsEmpty() {
        boolean isEmpty = true;
        for (Entry e : inRequests) {
            if (e.getNum() != 0) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }

    public int getPos() {
        return pos;
    }

    public int getDest() {
        return dest;
    }

    public int getPassengerNum() {
        return passengerNum;
    }

    public ArrayList<Entry> getInRequests() {
        return inRequests;
    }

    public boolean inRequestsIsOver() {
        if (inRequests.get(0).getToFloorList().get(0).get(0) == 0) {
            return false;
        } else {
            return true;
        }
    }

    public Elevator(int elevatorID, int floorNum, int elevatorNum, int capacity,
                    ArrayList<Entry> inRequests, boolean logall) {
        this.elevatorID = elevatorID;
        this.floorNum = floorNum;
        this.elevatorNum = elevatorNum;
        this.capacity = capacity;
        this.inRequests = inRequests;
        this.pos = 1;
        passengerNum = 0;
        direction = true; //初始时为上行
        opened = false;
        exit = false;
        dest = -1;
        if (!logall) {
            log = false;
        }
        strategy = new Strategy(this, logall);

        outRequests = new ArrayList<>();
        outRequests.add(0, new ArrayList<>()); //初始化第0项-无效项
        for (int i = 1; i <= floorNum; i++) {
            outRequests.add(i, new ArrayList<>()); //初始化，都是空表
        }
        //inRequest.get(0).getToFloorList()里存储了【这部电梯的四个状态】
        //不知道有什么用，先写着（）
        for (int i = 0; i < 4; i++) {
            inRequests.get(0).getToFloorList().add(i, new ArrayList<>());
            //防止IndexOutOfBoundsException
        }
        inRequests.get(0).getToFloorList().get(0).add(0, 0); //表示电梯未关闭
        inRequests.get(0).getToFloorList().get(1).add(0, 0); //表示电梯未满
        inRequests.get(0).getToFloorList().get(2).add(0, pos); //表示当前位置
        inRequests.get(0).getToFloorList().get(3).add(0, dest); //表示主目标
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

    private void in(Entry entry, int i) throws InterruptedException {
        synchronized (entry) {
            Iterator it = entry.getToFloorList().get(i).iterator();
            while (it.hasNext()) {
                Integer passengerID = (Integer)it.next();
                if (passengerNum < capacity) {
                    TimableOutput.println(
                            "IN-" + passengerID + "-" + pos + "-" + elevatorID);
                    it.remove();
                    entry.deleteNum(); //会忘记
                    // 这里应该不能使用entry.deletePassenger，会一边循环一边删除AL
                    //entry.deletePassenger(i, passengerID); //删除一个要去i的乘客
                    outRequests.get(i).add(passengerID);
                    passengerNum++;
                } else if (passengerNum >= capacity) {
                    if (log) {
                        System.out.println("超载了！这是ta的id：" + passengerID);
                    }
                    break;
                }
            }
        }
    }

    public void out(ArrayList<Integer> passengerLists) throws InterruptedException {
        synchronized (passengerLists) {
            Iterator it = passengerLists.iterator();
            while (it.hasNext()) {
                Integer passengerID = (Integer) it.next();
                TimableOutput.println(
                        "OUT-" + passengerID + "-" + pos + "-" + elevatorID);
                it.remove();
                passengerNum--;
            }
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
        }
        if (inRequests.get(pos).getNum() != 0) {
            ArrayList<ArrayList<Integer>> toFloorList =
                    inRequests.get(pos).getToFloorList();
            for (int i = 1; i <= floorNum; i++) {
                //有同方向的、在这层门口等候的旅客，让这层楼等待的【ta们】都上
                if ((!toFloorList.get(i).isEmpty() && (i > pos && direction == true))
                        || (!toFloorList.get(i).isEmpty() && (i < pos && direction == false))) {
                    in(inRequests.get(pos), i);
                    if (log) { //观察inrequests.get(pos)有没有被成功删去已上梯乘客
                        System.out.println("现在" + pos + "层里getFloorList去" + i + "层的人有：");
                        for (Integer id : inRequests.get(pos).getToFloorList().get(i)) {
                            System.out.print(id + " ");
                            System.out.println(" ");
                        }
                    }
                }
            }
        }
        close();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Advice advice = strategy.getAdvice(floorNum, pos,
                        passengerNum, direction, outRequests, capacity);
                if (log) {
                    System.out.println(advice.toString() + ":" + elevatorID);
                }

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
