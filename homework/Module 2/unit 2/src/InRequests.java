import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InRequests {
    private HashMap<Integer, ArrayList<Person>> requests;

    private int requestsSum;

    private boolean isEnd;

    private int floorNum = 11;

    private boolean log = true;

    public InRequests(boolean logAll) {
        requests = new HashMap<>();
        for (int i = 1; i <= floorNum; i++) {
            requests.put(i, new ArrayList<>());
        }
        requestsSum = 0;
        isEnd = false;
        if (!logAll) {
            log = false;
        }
    }

    public InRequests(HashMap<Integer, ArrayList<Person>> requests,
                                        int requestsSum, boolean isEnd) {
        this.requests = requests;
        this.requestsSum = requestsSum;
        this.isEnd = isEnd;
    }

    public synchronized void receiveAllRequests(int elevatorID) {
        //用于receiveRequests,此时尚未删去receiveRequests
        for (Map.Entry<Integer, ArrayList<Person>> entry : requests.entrySet()) {
            Integer key = entry.getKey();
            ArrayList<Person> personList = entry.getValue();

            Iterator it = personList.iterator();
            while (it.hasNext()) {
                Person person = (Person) it.next();
                TimableOutput.println("RECEIVE-" + person.getID() +
                        "-" + elevatorID);
            }
        }
        notifyAll();
    }

    public synchronized void addRequest(Person person) {
        if (person == null) {
            return;
        }
        if (!requests.containsKey(person.getFromFloor())) {
            requests.put(person.getFromFloor(), new ArrayList<>());
        }
        requests.get(person.getFromFloor()).add(person);
        //System.out.println("inRequests被add了：他的id是" + person.getID());
        requestsSum++;
        notifyAll(); //没想懂为什么要notify
    }

    public synchronized void addManyRequests(InRequests inRequests) { //this + receiveRequests
        for (Map.Entry<Integer, ArrayList<Person>> entry : inRequests.getRequests().entrySet()) {
            Integer key = entry.getKey();
            ArrayList<Person> personList = entry.getValue();

            Iterator it = personList.iterator();
            while (it.hasNext()) {
                Person person = (Person) it.next();
                this.addRequest(person);
                it.remove();
            }
        }
        notifyAll();
    }

    public synchronized void delRequest(int floor, Person person) {
        //把在floor层要进来的某一个person乘客删除掉
        Iterator<Person> it = requests.get(floor).iterator();
        while (it.hasNext()) {
            Person personDel = it.next();
            if (personDel.getID() == person.getID()) {
                it.remove();
                break;
            }
        }
        requestsSum--;
        notifyAll();
    }

    public Person getOneSameDirectPerson(int pos, boolean direction) {
        //get一个从pos出发要去相同方向的人
        for (Person person : requests.get(pos)) { //要从pos出发的人
            int toFloor = person.getToFloor();
            if ((toFloor > pos && direction == true) || (toFloor < pos && direction == false)) {
                return person;
            }
        }
        return null;
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll(); //?是synchronized都要notifyALL吗
    }

    public boolean isEnd() {
        return isEnd;
    }

    public boolean isEmpty() {
        return (requestsSum == 0);
        //不能用requests.isEmpty，因为每层即使都是空的，也有每层的AL，不会为空
    }

    public int getRequestsSum() {
        return requestsSum;
    }

    public HashMap<Integer, ArrayList<Person>> getRequests() {
        return requests;
    }

    public InRequests createSame() {
        HashMap<Integer, ArrayList<Person>> sameRequests = new HashMap<>();

        for (Map.Entry<Integer, ArrayList<Person>> entry : requests.entrySet()) {
            Integer key = entry.getKey();
            ArrayList<Person> value = entry.getValue();

            ArrayList<Person> clonedList = new ArrayList<>();
            for (Person person : value) {
                clonedList.add(person.createSame());
            }

            sameRequests.put(key, clonedList);
        }

        InRequests sameInRequests = new InRequests(sameRequests, requestsSum, isEnd);
        return sameInRequests;
    }

    public synchronized void delAll(RequestPool requestPool) {
        for (Map.Entry<Integer, ArrayList<Person>> entry : requests.entrySet()) {
            Integer key = entry.getKey();
            ArrayList<Person> personList = entry.getValue();

            Iterator it = personList.iterator();
            while (it.hasNext()) {
                Person person = (Person) it.next();
                requestPool.addRequest(person);
                it.remove();
                requestsSum--; //不要忘啦
            }
        }
        notifyAll();
    }
}
