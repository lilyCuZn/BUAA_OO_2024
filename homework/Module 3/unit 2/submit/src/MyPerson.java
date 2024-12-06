import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyPerson implements Person {
    private final int id;
    private final String name;
    private final int age;
    private final HashMap<Integer, Person> acquaintance;
    private final HashMap<Integer, Integer> values;
    private HashMap<Integer, Tag> tags;
    private int bestAcqId;
    private int bestAcqValue;

    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.acquaintance = new HashMap<>();
        this.values = new HashMap<>();
        this.tags = new HashMap<>();
        bestAcqId = -1;
        bestAcqValue = -1;
    }

    public boolean strictEquals(Person person) {
        return true;
    }

    public ArrayList<Person> getArrayAcq() {
        return new ArrayList<>(acquaintance.values());
    }

    public ArrayList<Tag> getArrayTags() {
        return new ArrayList<>(tags.values());
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getAge() {
        return this.age;
    }

    @Override
    public boolean containsTag(int id) {
        //@ ensures \result == (\exists int i; 0 <= i && i < tags.length; tags[i].getId() == id);
        return this.tags.containsKey(id);
    }

    @Override
    public Tag getTag(int id) {
        if (tags.containsKey(id)) {
            return tags.get(id);
        }
        return null;
    }

    @Override
    public void addTag(Tag tag) {
        /*@ public normal_behavior
      @ requires !containsTag(tag.getId());
      @ assignable tags;
      @ ensures containsTag(tag.getId());
      @*/
        this.tags.put(tag.getId(), tag);
    }

    @Override
    public void delTag(int id) {
        this.tags.remove(id);
    }

    /*@ public normal_behavior
      @ requires obj != null && obj instanceof Person;
      @ assignable \nothing;
      @ ensures \result == (((Person) obj).getId() == id);
      @ also
      @ public normal_behavior
      @ requires obj == null || !(obj instanceof Person);
      @ assignable \nothing;
      @ ensures \result == false;
      @*/
    public boolean equals(MyPerson myPerson) {
        return id == myPerson.getId();
    }

    @Override
    public boolean isLinked(Person person) {
        if (acquaintance.containsKey(person.getId())) {
            return true;
        } else if (person.getId() == id) {
            return true;
        }
        return false;
    }

    @Override
    public int queryValue(Person person) {
        //这个人的acquaintance中有没有这个person？
        if (acquaintance.containsKey(person.getId())) {
            return values.get(person.getId());
        } else {
            return 0;
        }
    }

    public void linkWith(Person person, int value) {
        int flag = -1;
        if (acquaintance.containsKey(person.getId())) {
            flag = 1;
            //说明是modify
        }
        acquaintance.put(person.getId(), person);
        values.put(person.getId(), value);
        if (flag == -1) {
            if (value > bestAcqValue) {
                bestAcqId = person.getId();
                bestAcqValue = value;
            } else if (value == bestAcqValue) {
                bestAcqId = Math.min(person.getId(), bestAcqId);
            }
        } else if (flag == 1) {
            bestAcqId = findBestAcq(values);
            bestAcqValue = values.get(bestAcqId);
        }
    }

    public void deleteLink(Person person) {
        int id = person.getId();
        int value = values.get(id);
        acquaintance.remove(person.getId());
        values.remove(person.getId());
        //假如删掉的不是bestAcquaintance的那个id，那么无事发生
        if (id == bestAcqId) {
            //找一个新的bestAcq
            bestAcqId = findBestAcq(values);
            if (bestAcqId == -1) {
                //System.out.println("findBestAcq得到的bestAcqId为-1.很诡异");
                //说明此时values和acq都为0
                bestAcqValue = -1;
            } else {
                bestAcqValue = values.get(bestAcqId);
            }
        }
    }

    private int findBestAcq(HashMap<Integer, Integer> values) {
        int maxValue = -1;
        int minId = -1;
        //System.out.println("values的长度为：" + values.size());
        for (Map.Entry<Integer, Integer> entry : values.entrySet()) {
            int id = entry.getKey();
            int value = entry.getValue();
            if (value > maxValue) {
                maxValue = value;
                minId = id;
            } else if (value == maxValue) {
                minId = Math.min(minId, id);
            }
        }
        return minId;
    }

    public int getBestAcq() {
        return bestAcqId;
    }
}
