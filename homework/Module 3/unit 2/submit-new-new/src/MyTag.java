import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.ArrayList;
import java.util.HashMap;

public class MyTag implements Tag {
    private int id;
    private HashMap<Integer, Person> persons;
    private int valueSum;
    private int ageMean;
    private int ageSum;
    private int ageVar;

    public MyTag(int id) {
        this.id = id;
        valueSum = 0;
        ageMean = 0;
        ageSum = 0;
        ageVar = 0;
        persons = new HashMap<>();
    }

    @Override
    public int getId() {
        return id;
    }

    public ArrayList<Person> getArrayPersons() {
        return new ArrayList<>(persons.values());
    }

    @Override
    public void addPerson(Person person) {
        if (!persons.containsKey(person.getId())) {
            persons.put(person.getId(), person);
        }
        ageSum += person.getAge();
        /*if (id == 1 && (person.getId() == 2 || person.getId() == 51 || person.getId() == 24 ||
                person.getId() == 9 || person.getId() == 31)) {
            System.out.println("tag1加入了一个人" + person.getId());
        }
        if (id == 1 && (person.getId() == 2 || person.getId() == 51 || person.getId() == 24 ||
                person.getId() == 9 || person.getId() == 31)) {
            System.out.println("原来的valueSum：" + valueSum);
        }*/
        manageAge();
        manageValue(0, person, 0);
        /*if (id == 1 && (person.getId() == 2 || person.getId() == 51 || person.getId() == 24 ||
                person.getId() == 9 || person.getId() == 31)) {
            System.out.println("加了人之后的valueSum:" + valueSum);
        }*/

    }

    @Override
    public boolean hasPerson(Person person) {
        if (persons.containsKey(person.getId())) {
            return true;
        }
        return false;
    }

    @Override
    public int getValueSum() {
        /*@ ensures \result == (\sum int i; 0 <= i && i < persons.length;
      @          (\sum int j; 0 <= j && j < persons.length &&
      @           persons[i].isLinked(persons[j]); persons[i].queryValue(persons[j])));
      @*/
        /*
        int count = 0;
        for (Person person : persons.values()) {
            for (Person person1 : persons.values()) {
                if (person.isLinked(person1)) {
                    //System.out.println(person.getId() + "islinked" + person1.getId());
                    //System.out.println("value:" + person.queryValue(person1));
                    count += person.queryValue(person1);
                }
            }
        }
        //return count;
        if (id == 1) {
            System.out.println("tag1里有：");
            for (Person person : persons.values()) {
                System.out.print(person.getId() + " ");
            }
            //System.out.println(" ");
        }
        */
        return valueSum;
    }

    @Override
    public int getAgeMean() {
        return ageMean;
    }

    @Override
    public int getAgeVar() {
        /*@ ensures \result == (persons.length == 0? 0 : ((\sum int i; 0 <= i && i < persons.length;
      @          (persons[i].getAge() - getAgeMean()) * (persons[i].getAge() - getAgeMean())) /
      @           persons.length));
      @*/
        return ageVar;
    }

    @Override
    public void delPerson(Person person) {
        this.persons.remove(person.getId());
        ageSum -= person.getAge();
        /*if (id == 1) {
            System.out.println("tag1删除了一个人" + person.getId());
        }
        if (id == 1) {
            System.out.println("原来的valueSum：" + valueSum);
        }
         */
        manageAge();
        manageValue(1, person, 0);
        /*
        if (id == 1) {
            System.out.println("删掉人之后的valueSum：" + valueSum);
        }
        */
    }

    public void manageAge() {
        //agemean && agevar
        if (persons.size() == 0) {
            ageMean = 0;
            ageVar = 0;
        } else {
            ageMean = ageSum / persons.size();
            int ageVarSum = 0;
            for (int id : persons.keySet()) {
                Person p = persons.get(id);
                ageVarSum += (p.getAge() - ageMean) * (p.getAge() - ageMean);
            }
            ageVar = ageVarSum / persons.size();
        }
    }

    public void manageValue(int op, Person person, int value) {
        /*@ ensures \result == (persons.length == 0? 0 : ((\sum int i; 0 <= i && i < persons.length;
      @          (persons[i].getAge() - getAgeMean()) * (persons[i].getAge() - getAgeMean())) /
      @           persons.length));
      @*/
        if (op == 0) { //add
            //valueSum
            MyPerson myPerson = (MyPerson) person;
            ArrayList<Person> acq = myPerson.getArrayAcq();
            for (Person person1 : acq) {
                if (persons.containsKey(person1.getId())) {
                    valueSum += 2 * myPerson.queryValue(person1);
                }
            }
        } else if (op == 1) { //delete
            //valueSum
            MyPerson myPerson1 = (MyPerson) person;
            ArrayList<Person> acq1 = myPerson1.getArrayAcq();
            for (Person person1 : acq1) {
                if (persons.containsKey(person1.getId())) {
                    valueSum -= 2 * myPerson1.queryValue(person1);
                }
            }
        } else if (op == 2) { //直接改变两点relation
            valueSum += 2 * value;
        }
    }

    @Override
    public int getSize() {
        return persons.size();
    }
}
