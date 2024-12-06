import java.util.ArrayList;
import java.util.HashMap;

public class Strategy {
    private boolean log = true;
    private InRequests inRequests;

    private int floorNum;

    private boolean isShadowElevator;

    public Strategy(InRequests inRequests, int floorNum, boolean logall, boolean isShadowElevator) {
        this.inRequests = inRequests;
        this.floorNum = floorNum;
        if (!logall) {
            log = false;
        }
        this.isShadowElevator = isShadowElevator;
        if (isShadowElevator) {
            log = false;
        }
    }

    public Advice getAdvice(int pos, int passengerNum, boolean direction, boolean wantToReset,
                            HashMap<Integer, ArrayList<Person>> outRequests, int capacity) {
        if (wantToReset == true) {
            return Advice.RESET;
        }
        if (canOpenForOut(pos, outRequests) ||
                canOpenForIn(pos, passengerNum, inRequests, direction, capacity)) {
            return Advice.OPEN;
        }
        if (passengerNum != 0) {
            return Advice.MOVE;
        } else {
            if (inRequests.isEmpty() && !wantToReset) {
                if (inRequests.isEnd()) {
                    return Advice.OVER;
                } else {
                    return Advice.WAIT;
                }
            }
            if (hasReqInOriginDirection(pos, direction, inRequests)) {
                return Advice.MOVE;
            } else {
                return Advice.REVERSE;
            }
        }
    }

    public boolean canOpenForOut(int pos, HashMap<Integer, ArrayList<Person>> outRequests) {
        if (!outRequests.get(pos).isEmpty()) { //里面有人想要从【这一层】出去(电梯不为空）
            return true;
        } else {
            return false;
        }
    }

    public boolean canOpenForIn(int pos, int passengerNum, InRequests inRequests,
                                            boolean direction, int capacity) {
        if (log) {
            System.out.println("判断这个楼层：" + pos);
        }
        //如果发现该楼层中有人想上电梯，并且目的地方向和电梯方向相同，则开门让这个乘客进入。
        if (!inRequests.getRequests().get(pos).isEmpty()) { //这层楼有人想进来
            if (log) {
                System.out.println("有人想从这里上梯：" + pos);
            }
            if (capacity <= passengerNum) { //超载了
                return false;
            }
            for (Person person : inRequests.getRequests().get(pos)) {
                if ((person.getToFloor() > pos && direction == true)
                        || (person.getToFloor() < pos && direction == false)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasReqInOriginDirection(int pos, boolean direction, InRequests inRequests) {
        //有请求的发出地在运动方向前方
        boolean result = false;
        for (int i = 1; i <= floorNum; i++) {
            ArrayList<Person> list = inRequests.getRequests().get(i); //想从i层出发的人们
            if (!list.isEmpty() &&
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
