package Factor;

public class Var implements Factor {
    private String name;
    
    public Var(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public Factor derive() {
        Term term = new Term();
        Number num = new Number("1");
        term.addFactor(num);// TODO 2
        return term;
    }
    
    @Override
    public Factor clone() {
        return new Var(name);
    }
}
