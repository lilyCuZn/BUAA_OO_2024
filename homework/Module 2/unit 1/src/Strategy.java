import java.util.ArrayList;

public class Strategy {
    private boolean log = true;
    private Elevator elevator;

    public Strategy(Elevator elevator, boolean logall) {
        this.elevator = elevator;
        if (!logall) {
            log = false;
        }
    }

    public Advice getAdvice(int floorNum, int curFloor, int curNum, boolean direction,
                            ArrayList<ArrayList<Integer>> outRequests, int capacity) {
        ArrayList<Entry> inRequests = elevator.getInRequests();
        if (canOpenForOut(curFloor, outRequests) ||
                canOpenForIn(curFloor, inRequests, floorNum, direction, capacity)) {
            return Advice.OPEN;
        }
        if (curNum != 0) {
            return Advice.MOVE;
        } else {
            if (elevator.inRequestsIsEmpty()) {
                if (elevator.inRequestsIsOver()) {
                    return Advice.OVER;
                } else {
                    return Advice.WAIT;
                }
            }
            if (hasReqInOriginDirection(curFloor, direction, floorNum, inRequests)) {
                return Advice.MOVE;
            } else {
                return Advice.REVERSE;
            }
        }
    }

    public boolean canOpenForOut(int pos, ArrayList<ArrayList<Integer>> outRequests) {
        if (outRequests.get(pos).isEmpty() == false) { //里面有人想要从【这一层】出去(电梯不为空）
            return true;
        } else {
            return false;
        }
    }

    public boolean canOpenForIn(int pos, ArrayList<Entry> inRequests,
                                int floorNum, boolean direction, int capacity) {
        if (log) {
            System.out.println("判断这个楼层：" + pos);
        }
        //如果发现该楼层中有人想上电梯，并且目的地方向和电梯方向相同，则开门让这个乘客进入。
        if (inRequests.get(pos).getNum() != 0) {
            if (log) {
                System.out.println("有人想从这里上梯：" + pos);
            }
            ArrayList<ArrayList<Integer>> toFloorList =
                    inRequests.get(pos).getToFloorList();
            for (int i = 1; i <= floorNum; i++) {
                if (capacity <= elevator.getPassengerNum()) { //超载了
                    return false;
                }
                if ((!toFloorList.get(i).isEmpty() && (i > pos && direction == true))
                        || (!toFloorList.get(i).isEmpty() && (i < pos && direction == false))) {
                    //有同方向的旅客想要上梯
                    if (log) {
                        System.out.println("这个人被捎上了：" + toFloorList.get(i).get(0));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasReqInOriginDirection(int pos, boolean direction,
                                           int floorNum, ArrayList<Entry> inRequests) {
        //有请求的发出地在运动方向前方
        boolean result = false;
        for (int i = 1; i <= floorNum; i++) {
            Entry e = inRequests.get(i);
            if (e.getNum() != 0 &&
                    ((i > pos && direction == true) || (i < pos && direction == false))) { //有同方向的请求
                if (log) {
                    System.out.println("在前方有同方向的请求，在这层：" + i);
                    System.out.println("现在在" + pos + "层,运动方向为" + direction);
                }
                result = true;
                break;
            }
        }
        return result;
    }
}
