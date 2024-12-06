import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.main.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class MyNetworkTest {
    private MyNetwork myNetwork1;

    public MyNetworkTest(MyNetwork myNetwork1) {
        this.myNetwork1 = myNetwork1;
    }

    @Parameters
    public static Collection prepareData() throws EqualPersonIdException {
        Object[][] object = new Object[1][];
        MyNetwork myNetwork1 = createNetwork();
        object[0] = new Object[]{myNetwork1};
        return Arrays.asList(object);
    }

    @Test
    public void testQueryCoupleSum() throws PersonIdNotFoundException, EqualRelationException, EqualPersonIdException, RelationNotFoundException {
        myNetwork1.addRelation(1, 2, 100);
        assertEquals(1, myNetwork1.queryCoupleSum());
        myNetwork1.addRelation(3, 4, 100);
        assertEquals(2, myNetwork1.queryCoupleSum());
        myNetwork1.addRelation(5, 6, 100);
        Person[] oldPersons = myNetwork1.getPersons();
        assertEquals(3, myNetwork1.queryCoupleSum());
        Person[] newPersons = myNetwork1.getPersons();
        for (int i = 0; i < oldPersons.length; i++) {
            assertTrue(((MyPerson) oldPersons[i]).strictEquals(newPersons[i]));
        } //pure检测
        assertEquals(oldPersons.length == newPersons.length, true);


        myNetwork1.addRelation(2, 3, 214);
        assertEquals(2, myNetwork1.queryCoupleSum()); // >bestValue
        myNetwork1.addRelation(3, 6, 345);
        assertEquals(1, myNetwork1.queryCoupleSum()); // >bestValue
        myNetwork1.addPerson(new MyPerson(7, "7", 1));
        myNetwork1.addPerson(new MyPerson(8, "8", 1));
        myNetwork1.addRelation(1, 7, 1);
        myNetwork1.addRelation(7, 8, 50);
        assertEquals(2, myNetwork1.queryCoupleSum());
        myNetwork1.addRelation(1, 8, 1);
        myNetwork1.modifyRelation(1, 8, 80); // mr -> >bestValue
        assertEquals(1, myNetwork1.queryCoupleSum());


        myNetwork1.modifyRelation(8, 1, -600);
        myNetwork1.modifyRelation(2, 1, -600);
        myNetwork1.modifyRelation(1, 7, -600);
        myNetwork1.modifyRelation(7, 8, -600);
        myNetwork1.modifyRelation(2, 3, -500);
        myNetwork1.modifyRelation(3, 4, -600);
        myNetwork1.modifyRelation(3, 6, -600);
        myNetwork1.modifyRelation(6, 5, -600);
        assertEquals(0, myNetwork1.queryCoupleSum()); //no relations

    }

    public static MyNetwork createNetwork() throws EqualPersonIdException {
        MyNetwork myNetwork1 = new MyNetwork();
        myNetwork1.addPerson(new MyPerson(1, "1", 1));
        myNetwork1.addPerson(new MyPerson(2, "2", 1));
        myNetwork1.addPerson(new MyPerson(3, "3", 1));
        myNetwork1.addPerson(new MyPerson(4, "4", 1));
        myNetwork1.addPerson(new MyPerson(5, "5", 1));
        myNetwork1.addPerson(new MyPerson(6, "6", 1));
        return myNetwork1;
    }
}