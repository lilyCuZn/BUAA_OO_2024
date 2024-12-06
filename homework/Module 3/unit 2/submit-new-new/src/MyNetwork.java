import com.oocourse.spec2.exceptions.*;

import com.oocourse.spec2.main.Network;
import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.ArrayList;
import java.util.HashMap;

public class MyNetwork implements Network {
    private HashMap<Integer, Person> persons;
    private int tripleSum; //形成三角形的数量
    private ArrayList<HashMap<Integer, Person>> blocks; //联通的所有人都会放进一个blocks
    private int cpSum;
    private Query query;
    private ArrayList<Tag> tags; //不同的tag可能有相同的id

    public MyNetwork() {
        this.persons = new HashMap<>();
        this.tripleSum = 0;
        this.cpSum = 0;
        this.blocks = new ArrayList<>();
        query = new Query(this);
        tags = new ArrayList<>();
    }

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public Person getPerson(int id) {
        return persons.getOrDefault(id, null);
        //如果没有，返回null
    }

    public Person[] getPersons() {
        return null;
    }

    public ArrayList<HashMap<Integer, Person>> getBlocks() {
        return blocks;
    }

    @Override
    public void addPerson(Person person) throws EqualPersonIdException {
        if (containsPerson(person.getId())) {
            throw new MyEqualPersonIdException(person.getId());
        } else {
            persons.put(person.getId(), person);
            HashMap<Integer, Person> newblock = new HashMap<>();
            newblock.put(person.getId(), person);
            blocks.add(newblock);
        }
    }

    @Override
    public void addRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, EqualRelationException {
        if (!persons.containsKey(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!persons.containsKey(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (persons.get(id1).isLinked(persons.get(id2))) {
            throw new MyEqualRelationException(id1, id2);
        } else {
            MyPerson myPerson1 = (MyPerson) getPerson(id1);
            MyPerson myPerson2 = (MyPerson) getPerson(id2);
            int oldbestof1 = myPerson1.getBestAcq();
            int oldbestof2 = myPerson2.getBestAcq();
            myPerson1.linkWith(myPerson2, value);
            myPerson2.linkWith(myPerson1, value);
            int newbestof1 = myPerson1.getBestAcq();
            int newbestof2 = myPerson2.getBestAcq();
            dealWithCouples(oldbestof1, oldbestof2, newbestof1, newbestof2,
                    id1, id2);
            addBlocks(id1, id2);
            addTriples(id1, id2);
            for (Tag tag : tags) {
                if (tag.hasPerson(myPerson1) && tag.hasPerson(myPerson2)) {
                    MyTag myTag = (MyTag) tag;
                    myTag.manageValue(2, null, value);
                }
            }
        }
    }

    public void dealWithCouples(int oldbest1, int oldbest2, int newbest1, int newbest2,
                                int id1, int id2) {
        MyPerson old1 = (MyPerson) getPerson(oldbest1);
        MyPerson old2 = (MyPerson) getPerson(oldbest2);
        MyPerson new1 = (MyPerson) getPerson(newbest1);
        MyPerson new2 = (MyPerson) getPerson(newbest2);
        //System.out.println("oldbest1:" + oldbest1 + ",newbest1:" + newbest1);
        //System.out.println("oldbest2:" + oldbest2 + ",newbest2:" + newbest2);
        if (oldbest1 == newbest1 && oldbest2 == newbest2) {
            //couple没有发生变化
        } else if (oldbest1 != newbest1 || oldbest2 != newbest2) {
            //至少有一个的best发生了变化
            if (newbest1 == id2 && newbest2 == id1) {
                cpSum++;
            }
            if (oldbest1 == id2 && oldbest2 == id1) {
                cpSum--;
            }
            if (oldbest1 != newbest1) {
                //不会抛出异常，因为假如异常会返回null，后面有做判断
                //1的best发生了变化
                if (newbest1 != id2) {
                    if (new1 != null && new1.getArrayAcq().size() > 0
                        && new1.getBestAcq() == id1) {
                        cpSum++;
                    }
                } else if (newbest1 == id2) {
                    if (old1 != null && old1.getArrayAcq().size() > 0
                         && old1.getBestAcq() == id1) {
                        cpSum--;
                    }
                }
            }
            if (oldbest2 != newbest2) {
                //1的best发生了变化
                if (newbest2 != id1) {
                    if (new2 != null && new2.getArrayAcq().size() > 0
                         && new2.getBestAcq() == id2) {
                        cpSum++;
                    }
                } else if (newbest2 == id1) {
                    if (old2 != null && old2.getArrayAcq().size() > 0
                        && old2.getBestAcq() == id2) {
                        cpSum--;
                    }
                }
            }
        }
        //System.out.println("manageCoupleSum:" + cpSum);
    }

    public void addBlocks(int id1, int id2) {
        //加入了一个id1和id2的link，改变blocks
        HashMap<Integer, Person> block1 = new HashMap<>();
        HashMap<Integer, Person> block2 = new HashMap<>();
        int flag = 0;
        for (HashMap<Integer, Person> block : blocks) {
            if (block.containsKey(id1)) {
                if (!block.containsKey(id2)) {
                    block1 = block;
                    flag = 1; //id1和id2不在一个block
                    break;
                }
            }
        }
        if (flag == 1) {
            for (HashMap<Integer, Person> block : blocks) {
                if (block.containsKey(id2)) {
                    block1.putAll(block);
                    block2 = block;
                    break;
                }
            }
            blocks.remove(block2);
        }
    }

    public void deleteBlocks(int id1, int id2) {
        //删掉一个id1和id2之间的link。改变blocks
        //既然原来是link的，那么肯定在同一个block里
        for (HashMap<Integer, Person> block : blocks) {
            if (block.containsKey(id1) && block.containsKey(id2)) {
                HashMap<Integer, Person> newblock = new HashMap<>();
                getBlockFromId(id1, block, newblock);
                if (newblock.containsKey(id2)) {
                    //id1和id2还在一个block内，不用处理
                } else {
                    block.keySet().removeAll(newblock.keySet()); //block2
                    blocks.add(newblock); //block1
                }
                break;
            }
        }
    }

    public void getBlockFromId(int id, HashMap<Integer, Person> persons,
                            HashMap<Integer, Person> newblock) {
        //由id，找出其所在的block，即找出其连通的所有点
        MyPerson myperson = (MyPerson) getPerson(id);
        newblock.put(id, getPerson(id));
        for (Person person : myperson.getArrayAcq()) {
            if (persons.containsKey(person.getId()) && !newblock.containsKey(person.getId())) {
                getBlockFromId(person.getId(), persons, newblock);
            }
        }
    }

    public void addTriples(int id1, int id2) {
        int tsIncrease = 0; //tripleSumIncrease
        for (HashMap<Integer, Person> block : blocks) {
            if (block.size() >= 3 && block.containsKey(id1) && block.containsKey(id2)) {
                //找到两个link的点在的同一个block
                MyPerson p1 = (MyPerson) block.get(id1);
                MyPerson p2 = (MyPerson) block.get(id2);
                ArrayList<Person> blockArray = new ArrayList<>(block.values());
                for (Person person : blockArray) {
                    if (person.isLinked(p1) && person.isLinked(p2)
                            && person.getId() != id1 && person.getId() != id2) {
                        tsIncrease++;
                    }
                }
                break;
            }
        }
        tripleSum += tsIncrease;
    }

    public void deleteTriples(int id1, int id2) {
        //删除了id1和id2连接之后tripleSum怎么变化？
        int tsDecrease = 0; //tripleSumDecrease
        //假如id1和id2在删除关系之后不在同一个block，那么之前它们也不在一个triple里
        //System.out.println("blocks里现在有几个block：" + blocks.size());
        for (HashMap<Integer, Person> block : blocks) {
            if (block.size() >= 3 && block.containsKey(id1) && block.containsKey(id2)) {
                //System.out.println("find");
                //找到两个link的点在的同一个block
                MyPerson p1 = (MyPerson) block.get(id1);
                MyPerson p2 = (MyPerson) block.get(id2);
                ArrayList<Person> blockArray = new ArrayList<>(block.values());
                for (Person person : blockArray) {
                    if (person.isLinked(p1) && person.isLinked(p2)
                            && person.getId() != id1 && person.getId() != id2) {
                        tsDecrease++;
                    }
                }
                break;
            }
        }
        tripleSum -= tsDecrease;
    }

    @Override
    public void modifyRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (!persons.containsKey(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!persons.containsKey(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (id1 == id2) {
            throw new MyEqualPersonIdException(id1);
        } else if (!persons.get(id1).isLinked(persons.get(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
        } else {
            MyPerson myPerson1 = (MyPerson) persons.get(id1);
            MyPerson myPerson2 = (MyPerson) persons.get(id2);
            int oldbest1 = myPerson1.getBestAcq();
            int oldbest2 = myPerson2.getBestAcq();
            if (myPerson1.queryValue(myPerson2) + value > 0) {
                //System.out.println("modify-changevalue" +
                // myPerson1.queryValue(myPerson2) + value);
                myPerson1.linkWith(myPerson2, myPerson1.queryValue(myPerson2) + value);
                myPerson2.linkWith(myPerson1, myPerson2.queryValue(myPerson1) + value);
                addBlocks(id1, id2);
                for (Tag tag : tags) {
                    if (tag.hasPerson(myPerson1) && tag.hasPerson(myPerson2)) {
                        MyTag myTag = (MyTag) tag;
                        myTag.manageValue(2, null, value);
                    }
                }
                //addTriples(id1, id2);本来就link的，不用addTriple了
            } else {
                int oldvalue = -myPerson1.queryValue(myPerson2);
                myPerson1.deleteLink(myPerson2);
                myPerson2.deleteLink(myPerson1);
                for (Tag tag : tags) {
                    if (tag.hasPerson(myPerson1) && tag.hasPerson(myPerson2)) {
                        MyTag myTag = (MyTag) tag;
                        myTag.manageValue(2, null, oldvalue);
                    }
                }
                for (Tag tag : myPerson1.getArrayTags()) {
                    if (tag.hasPerson(myPerson2)) {
                        tag.delPerson(myPerson2);
                    }
                }
                for (Tag tag : myPerson2.getArrayTags()) {
                    if (tag.hasPerson(myPerson1)) {
                        tag.delPerson(myPerson1);
                    }
                }
                deleteBlocks(id1, id2);
                deleteTriples(id1, id2);
            }
            int newbest1 = myPerson1.getBestAcq();
            int newbest2 = myPerson2.getBestAcq();
            dealWithCouples(oldbest1, oldbest2, newbest1, newbest2, id1, id2);
        }
    }

    @Override
    public int queryValue(int id1, int id2)
            throws PersonIdNotFoundException, RelationNotFoundException {
        if (!persons.containsKey(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!persons.containsKey(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (!persons.get(id1).isLinked(persons.get(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
        } else {
            return persons.get(id1).queryValue(persons.get(id2));
        }
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        //在一个连通分量里
        if (!persons.containsKey(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!persons.containsKey(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            for (HashMap<Integer, Person> block : blocks) {
                if (block.containsKey(id1) && block.containsKey(id2)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public int queryBlockSum() {
        return blocks.size();
    }

    @Override
    public int queryTripleSum() {
        //三个人，两两相连
        return tripleSum;
    }

    @Override
    public void addTag(int personId, Tag tag)
            throws PersonIdNotFoundException, EqualTagIdException {
        if (!persons.containsKey(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else if (persons.get(personId).containsTag(tag.getId())) {
            throw new MyEqualTagIdException(tag.getId());
        } else {
            persons.get(personId).addTag(tag);
            if (!tags.contains(tag)) {
                tags.add(tag);
            }
        }
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, RelationNotFoundException,
            TagIdNotFoundException, EqualPersonIdException {
        if (!persons.containsKey(personId1)) {
            throw new MyPersonIdNotFoundException(personId1);
        } else if (!persons.containsKey(personId2)) {
            throw new MyPersonIdNotFoundException(personId2);
        } else if (personId1 == personId2) {
            throw new MyEqualPersonIdException(personId1);
        } else if (!persons.get(personId1).isLinked(persons.get(personId2))) {
            throw new MyRelationNotFoundException(personId1, personId2);
        } else if (!persons.get(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else if (persons.get(personId2).getTag(tagId).
                hasPerson(persons.get(personId1))) {
            throw new MyEqualPersonIdException(personId1); //不知道应该抛出哪个id的异常
        } else {
            MyTag myTag = (MyTag) persons.get(personId2).getTag(tagId);
            if (myTag.getArrayPersons().size() <= 1111) {
                myTag.addPerson(persons.get(personId1));
            }
            //否则，什么也不做
        }
    }

    @Override
    public int queryTagValueSum(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else if (!persons.get(personId).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else {
            return persons.get(personId).getTag(tagId).getValueSum();
        }
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else if (!persons.get(personId).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else {
            return persons.get(personId).getTag(tagId).getAgeVar();
        }
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId1)) {
            throw new MyPersonIdNotFoundException(personId1);
        } else if (!persons.containsKey(personId2)) {
            throw new MyPersonIdNotFoundException(personId2);
        } else if (!persons.get(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else if (!persons.get(personId2).getTag(tagId).
                hasPerson(persons.get(personId1))) {
            throw new MyPersonIdNotFoundException(personId1);
        } else {
            MyTag myTag = (MyTag) persons.get(personId2).getTag(tagId);
            myTag.delPerson(persons.get(personId1));
        }
    }

    @Override
    public void delTag(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else if (!persons.get(personId).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else {
            persons.get(personId).delTag(tagId);
        }
    }

    @Override
    public int queryBestAcquaintance(int id)
            throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!persons.containsKey(id)) {
            throw new MyPersonIdNotFoundException(id);
        } else if (((MyPerson) persons.get(id)).getArrayAcq().size() == 0) {
            throw new MyAcquaintanceNotFoundException(id);
        } else {
            MyPerson myPerson = (MyPerson) persons.get(id);
            return myPerson.getBestAcq();
        }
    }

    @Override
    public int queryCoupleSum() {
        return cpSum;
    }

    @Override
    public int queryShortestPath(int id1, int id2)
            throws PersonIdNotFoundException, PathNotFoundException {
        //找到最短路径的长度
        if (!persons.containsKey(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!persons.containsKey(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            return query.shortestPath(id1, id2);
        }
    }
}
