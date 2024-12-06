import java.util.ArrayList;

public class Entry {
    private int passengerNum;
    private ArrayList<ArrayList<Integer>> toFloorList = new ArrayList<>(); //目的地为指定楼层的人的id

    public ArrayList<ArrayList<Integer>> getToFloorList() {
        return toFloorList;
    }

    public Entry(int floorNum) {
        passengerNum = 0;
        toFloorList.add(0, new ArrayList<>());
        for (int i = 1; i <= floorNum; i++) {
            ArrayList<Integer> personIDs = new ArrayList<>();
            toFloorList.add(i, personIDs);
        }
    }

    public void addPassenger(int toFloor, int personID) {
        toFloorList.get(toFloor).add(Integer.valueOf(personID));
        passengerNum++;
    }

    public int getNum() { //有多少人想从这层楼上电梯？
        return passengerNum;
    }

    public ArrayList<ArrayList<Integer>> getRequests() {
        return toFloorList;
    }

    public void deletePassenger(int toFloor, int personID) { //删去一个要去toFloor的person
        toFloorList.get(toFloor).remove(Integer.valueOf(personID));
        passengerNum--;
    }

    public void deleteNum() {
        passengerNum--;
    }

    public void addNum() {
        passengerNum++;
    }
}
