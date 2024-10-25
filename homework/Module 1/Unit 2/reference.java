// 处理接口
interface Parser {
    public ParseResult parse(); // 返回解析的字符串
}

// 缓存解析结果
class ParseResult {
    // 缓存parse()方法的处理结果，可以是化简后的表达式。
    ArrayList<...> result; // 这里的数据类型仍需要斟酌，主要是为了方便合并左右的表达式。甚至可以存一棵化简之后的抽象语法树

    // 处理result, 返回解析结果
    public String toString(){ ... } 
}


// 操作符
interface Op {
    public ParseResult operate(Parser param1, Parser param2);  // 操作方法
}

// 以下为操作符示例
// +
class Add implements Op {
    @Override
    public ParseResult operate(Parser param1, Parser param2){ 
        // 此处要显示调用 参数的parse方法
        ParseResult result1 = param1.parse();
        ParseResult result2 = param2.parse();
        // 按加法针对 result1 和 result2 进行处理 ... 
        ParseResult result = add(result1, result2); // 省略实现
        return result;
    }
}

// -
class Sub implements Op {
    @Override
    public ParseResult operate(Parser param1, Parser param2){ 
        // 此处要显示调用 参数的parse方法
        ParseResult result1 = param1.parse();
        ParseResult result2 = param2.parse();
        // 按减法针对 result1 和 result2 进行处理 ... 
        ParseResult result = sub(result1, result2); // 省略实现
        return result;
     }
}



// 表示： 常量因子、变量因子
class Factor implements Parser {

    public String NumOrVar; // 常量因子 / 变量因子，变量类型暂时取String

    public Op op;

    @Override
    public ParseResult parse(){
        // 在这里添加操作符功能实现
        // 也可添加 消去前缀0 和 + 等简化逻辑
        return  op.operate(NumOrVar); 
    }
}

// 表示：幂函数、指数函数、表达式因子、项
class Expr implements Parser {
    public Parser factor1;  // 左操作数
    public Op op;           // 操作符
    public Parser factor2;  // 右操作数

    @Override
    public ParseResult parse(){
        // 化简等其它处理

        // [注意]这里比较麻烦的是 ：当左操作数和右操作数都是一个带有变量因子的表达式，需要遍历两侧操作数的全部因子，逐个变量因子执行合并
        return op.operate(factor1, factor2);
    }
    
}

// 表示：自定义函数
class Function implements Parser{
    public String param1;
    public String param2;
    public String param3;

    @Override
    public ParseResult parse(){
        // 化简处理
        Parser p =  new Expr(....); // 将param1 、 param2 、 param3 塞到一个Expr里
        return p.parse();
    }
}

// 抽象语法树
class AST {

    public Parser root; // 根节点

    public AST(){ ... } // 构造函数

    // 解析，并返回转化结果
    public String parse(
        ParseResult result = root.parse();
        return result.toString()
    )
}