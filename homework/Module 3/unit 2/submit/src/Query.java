import com.oocourse.spec2.exceptions.PathNotFoundException;
import com.oocourse.spec2.main.Network;
import com.oocourse.spec2.main.Person;

import java.util.ArrayList;
import java.util.HashMap;

public class Query {
    private MyNetwork network;

    public Query(Network network) {
        this.network = (MyNetwork) network;
    }

    public int shortestPath(int id1, int id2) throws PathNotFoundException {
        HashMap<Integer, Person> theBlock = null;
        ArrayList<HashMap<Integer, Person>> blocks = network.getBlocks();
        for (HashMap<Integer, Person> block: blocks) {
            if (block.containsKey(id1) && block.containsKey(id2)) {
                theBlock = block;
                break;
            }
        }
        if (theBlock == null) {
            throw new MyPathNotFoundException(id1, id2);
        }
        ShortestPath shortestPath = new
                ShortestPath(network, theBlock, network.getPerson(id1), network.getPerson(id2));
        int length = shortestPath.findShortestPath1();
        if (length <= 1) {
            length = 0;
        } else {
            length = length - 1;
        }
        return length;
    }
}
