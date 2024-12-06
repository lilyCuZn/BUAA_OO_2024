import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;

public class RequestPool {
    private ArrayList<Person> requests;

    private int requestsSum;

    private boolean isEnd;

    private boolean log = true;

    public RequestPool(boolean logall) {
        requests = new ArrayList<>();
        requestsSum = 0;
        isEnd = false;
        if (!logall) {
            log = false;
        }
    }

    public synchronized void addRequest(Person person) {
        if (person == null) {
            return;
        }
        requests.add(person);
        requestsSum++;
        if (log) {
            TimableOutput.println("requestPool被addRequest了：" + person.getID());
        }
        notifyAll(); //没想懂为什么要notify
    }

    public synchronized void delRequest(Person person) {
        Iterator<Person> it = requests.iterator();
        while (it.hasNext()) {
            Person nextPerson = it.next();
            if (nextPerson.getID() == person.getID()) {
                it.remove();
                break;
            }
        }
        requestsSum--;
        notifyAll();
    }

    public synchronized void setEnd() {
        isEnd = true;
        if (log) {
            TimableOutput.println("requestPool被setEnd了");
        }
        notifyAll();
    }

    public boolean isEnd() {
        return isEnd;
    }

    public boolean isEmpty() {
        return (requestsSum == 0);
        //不能用requests.isEmpty，因为每层即使都是空的，也有每层的AL，不会为空
    }

    public synchronized Person getOneRequestAndRemove() {
        while (isEmpty() && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (isEmpty()) {
            return null;
        }
        Person person = requests.get(0);
        requests.remove(0);
        requestsSum--;
        return person;
    }
}
