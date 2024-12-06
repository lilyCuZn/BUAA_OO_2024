import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Strategy {
    private boolean log = true;

    private InRequests inRequests;

    private int floorNum;

    private boolean isShadowElevator;

    private HashMap<Integer, ArrayList<Person>> outRequests;

    public Strategy(InRequests inRequests, int floorNum, boolean logall, boolean isShadowElevator,
                    HashMap<Integer, ArrayList<Person>> outRequests) {
        this.inRequests = inRequests;
        this.floorNum = floorNum;
        if (!logall) {
            log = false;
        }
        this.isShadowElevator = isShadowElevator;
        if (isShadowElevator) {
            log = false;
        }
        this.outRequests = outRequests;
    }

    public Advice getAdvice(int pos, int passengerNum, boolean direction,
                            boolean wantToReset, boolean wantToDoubleReset,
                            int capacity, Elevator elevator,
                            boolean doubleID) {
        boolean isDouble = elevator.isDouble();
        int transferFloor = elevator.getTransferFloor();
        if (log) {
            System.out.println(elevator.getElevatorID() + "号电梯正在getAdvice，它isDouble：" + isDouble
                + ",它的doubleID是：" + ((!doubleID) ? 'A' : 'B'));
            System.out.println(elevator.getElevatorID() + "号电梯的transferFloor:" +
                    transferFloor + " ,pos:" + pos);
        }
        if (wantToDoubleReset == true) {
            return Advice.DOUBLERESET;
        }
        if (wantToReset == true) {
            return Advice.RESET;
        }
        if (canOpenForOut(pos, outRequests, isDouble, doubleID, transferFloor) ||
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
                    if (isDouble && pos == transferFloor) {
                        return Advice.REVERSEANDMOVE;
                    }
                    return Advice.WAIT;
                }
            }
            if (hasReqInOriginDirection(pos, direction, inRequests, isDouble, transferFloor)) {
                return Advice.MOVE;
            } else {
                return Advice.REVERSE;
            }
        }

    }

    public boolean canOpenForOut(int pos, HashMap<Integer, ArrayList<Person>> outRequests,
                                 boolean isDouble, boolean doubleID, int transferFloor) {
        if (!outRequests.get(pos).isEmpty()) { //里面有人想要从【这一层】出去(电梯不为空）
            return true;
        } else if (isDouble && (pos == transferFloor)
                && hasPersonToGoFurther(pos, transferFloor, doubleID, outRequests)) {
            //有送不到的人，把ta放出去
            return true;
        }
        return false;
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

    public boolean hasReqInOriginDirection(int pos, boolean direction, InRequests inRequests,
        boolean isDouble, int transferFloor) {
        //有请求的发出地在运动方向前方
        boolean result = false;
        if (!isDouble) {
            for (int i = 1; i <= floorNum; i++) {
                ArrayList<Person> list = inRequests.getRequests().get(i); //想从i层出发的人们
                if (!list.isEmpty() &&
                        ((i > pos && direction == true) || (i < pos && direction == false))) {
                    //有同方向的请求
                    if (log) {
                        System.out.println("在前方有同方向的请求，在这层：" + i);
                        System.out.println("现在在" + pos + "层,运动方向为" + direction);
                    }
                    result = true;
                    break;
                }
            }
        } else {
            for (int i = 1; i <= floorNum; i++) {
                ArrayList<Person> list = inRequests.getRequests().get(i); //想从i层出发的人们
                if (log) {
                    if (!list.isEmpty()) {
                        System.out.println("inRequests中有人想从" + i + "层出发");
                    }
                    if (!list.isEmpty()
                            && ((i > pos && direction == true) || (i < pos && direction == false)))
                    {
                        System.out.println("而且想出发的人在前方同方向");
                    }
                }
                if (!list.isEmpty()
                        && ((i > pos && direction == true) || (i < pos && direction == false))) {
                    if ((i <= transferFloor && pos <= transferFloor) ||
                            (i >= transferFloor && pos >= transferFloor)) {  //有同方向的、相同范围内的请求
                        if (log) {
                            System.out.println("在前方有同方向的请求，在这层：" + i);
                            System.out.println("现在在" + pos + "层,运动方向为" + direction);
                        }
                        result = true;
                        break;
                    }
                }
            }
        }

        return result;
    }

    public boolean hasPersonToGoFurther(int pos, int transferFloor, boolean doubleID,
                                        HashMap<Integer, ArrayList<Person>> outRequests) {
        for (Map.Entry<Integer, ArrayList<Person>> entry : outRequests.entrySet()) {
            Integer key = entry.getKey();
            ArrayList<Person> personList = entry.getValue();
            for (Person person : personList) {
                if (((person.getToFloor() > transferFloor) && doubleID == false) ||
                        (person.getToFloor() < transferFloor && doubleID == true)) {
                    return true;
                }
            }
        }
        return false;
    }
}
