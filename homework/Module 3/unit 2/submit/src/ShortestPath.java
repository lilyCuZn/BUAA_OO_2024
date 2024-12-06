import com.oocourse.spec2.main.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class ShortestPath {
    private MyNetwork network;
    private HashMap<Integer, Person> block;
    private Person start;
    private Person end;
    private int globalLength;
    private boolean globalFlag;

    public ShortestPath(MyNetwork network, HashMap<Integer, Person> block,
                        Person start, Person end) {
        this.network = network;
        this.block = block;
        this.start = start;
        this.end = end;
        globalLength = 0;
        globalFlag = false;
    }

    public int findShortestPath1() {
        Queue<Integer> queue = new LinkedList<>();
        HashMap<Integer, Integer> visited = new HashMap<>();
        queue.add(start.getId());
        visited.put(start.getId(), 0);
        while (!queue.isEmpty()) {
            update1(queue, visited);
            if (globalFlag) {
                break;
            }
        }
        return globalLength;
    }

    public void update1(Queue<Integer> queue, HashMap<Integer, Integer> visited) {
        int poll = queue.poll();
        Person person = network.getPerson(poll);
        ArrayList<Person> acqs = ((MyPerson) person).getArrayAcq();
        for (Person p : acqs) {
            if (!visited.containsKey(p.getId())) {
                queue.add(p.getId());
                visited.put(p.getId(), visited.get(poll) + 1);
                //当前节点作为这个person节点的前驱节点
            }
            if (p.getId() == end.getId()) {
                //两边交汇了
                globalFlag = true;
                globalLength = visited.get(end.getId());
                queue.clear();
                return;
            }
        }
    }

    public int findShortestPath() {
        //dijstra(start);
        Queue<Integer> queue1 = new LinkedList<>();
        Queue<Integer> queue2 = new LinkedList<>();
        queue1.add(start.getId());
        queue2.add(end.getId());

        HashMap<Integer, Integer> visited1 = new HashMap<>();
        HashMap<Integer, Integer> visited2 = new HashMap<>();
        visited1.put(start.getId(), 0);
        visited2.put(end.getId(), 0);

        while (!queue1.isEmpty() && !queue2.isEmpty()) {
            if (queue1.size() <= queue2.size()) {
                update(queue1, visited1, visited2);
            } else {
                update(queue2, visited2, visited1);
            }
            if (globalFlag) {
                break;
            }
        }
        return globalLength;
    }

    public void update(Queue<Integer> queue, HashMap<Integer, Integer> cur,
                       HashMap<Integer, Integer> other) {
        //globalLength++;
        //不能盲目的先加globalLength，因为可能有的点延伸出去的并不在最短路径里
        int poll = queue.poll();
        Person person = network.getPerson(poll);
        ArrayList<Person> acqs = ((MyPerson) person).getArrayAcq();
        for (Person p : acqs) {
            if (!cur.containsKey(p.getId())) {
                queue.add(p.getId());
                cur.put(p.getId(), cur.get(poll) + 1);
                //当前节点作为这个person节点的前驱节点
            }
            if (other.containsKey(p.getId())) {
                //两边交汇了
                globalFlag = true;
                globalLength = cur.get(p.getId()) + other.get(p.getId());
                return;
            }
        }
    }
}
