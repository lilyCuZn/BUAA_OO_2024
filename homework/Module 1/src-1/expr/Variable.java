package expr;

public class Variable implements Factor {
    private final String var;

    public Variable(String varName)
    {
        this.var = varName;
    }

    public String toString() {
        return this.var;
    }
}

