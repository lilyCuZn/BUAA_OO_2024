public class Person {
    private final int id;

    private int from;

    private int to;

    public Person(int id, int from, int to) {
        this.id = id;
        this.from = from;
        this.to = to;
    }

    public int getID() {
        return id;
    }

    public int getFromFloor() {
        return from;
    }

    public int getToFloor() {
        return to;
    }

    public Person createSame() {
        return new Person(id, from, to);
    }

    public void setFromFloor(int pos) {
        this.from = pos;
    }
}
