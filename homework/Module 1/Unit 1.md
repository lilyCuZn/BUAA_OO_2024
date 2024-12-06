# 第一单元

## Processor

功能：处理输入字符串，去掉空格和重复的加减号

易错点1：java中字符串不可变

> 可以将字符串处理为可变的字符数组，然后通过修改数组元素来实现所需的更改。然后，你可以将修改后的字符数组转换回字符串。

````java
char[] processedChars = processedPoly.toCharArray();

for (int i = 0; i < processedPoly.length() - 1; i++) {
    char ch1 = processedChars[i];
    char ch2 = processedChars[i + 1];
    if (ch1 == '+' || ch1 == '-') {
        if (ch2 == '+' || ch2 == '-') {
            if (ch1 == ch2) {
                processedChars[i] = ' ';
                processedChars[i + 1] = '+';
            }
        }
    }
}
// 将修改后的字符数组转换回字符串
String modifiedPoly = new String(processedChars);

/*在上述示例中，我们将 processedPoly 转换为一个可变的字符数组 processedChars。然后，我们使用字符数组进行遍历和修改。最后，我们将修改后的字符数组转换回字符串并打印输出。*/

````

## Variable类的方法toString

> 要输出Variable类的实例的var属性，你需要确保Variable类正确实现了自定义的toString()方法。默认情况下，如果没有为类提供自定义的toString()方法，Java将使用默认的toString()方法，它返回类的名称和哈希码。

错误示范：自作聪明把toString改名成了getVar，导致print的时候呈现`expr.Variable@66048bfd`而不是想要的var的字符。

## 带有减号的后缀表达式实现

不能用Hashset！因为Hashset是乱序，分不清被减数和减数；要用ArrayList

## 无法对Hashmap.get(i)直接赋值

> 在你提供的代码中，报错是因为你尝试直接对 `newForm.contents.get(i)` 进行赋值操作。然而，`newForm.contents.get(i)` 返回的是一个值，而不是一个可修改的引用。
>
> 要修正这个问题，你需要使用 `newForm.contents.put(i, value)` 方法来将新的值存储到 `newForm.contents` 中，而不是尝试直接修改返回的值。
>
> 以下是修正后的代码示例：
>
> ```java
> public Formula add(Formula other) {
>     int a = this.contents.size();
>     int b = other.contents.size();
>     Formula newForm = new Formula();
>     for (int i = 0; (i < a) && (i < b); i++) {
>         BigInteger sum = this.contents.get(i).add(other.contents.get(i));
>         newForm.contents.put(i, sum);
>     }
>     return newForm;
> }
> ```

## 验证方法

```java
public static void main(String[] args) {
    String str = new String("+(-10)");
    Processor processor = new Processor();
    str = processor.addZero(str);
    System.out.println(str);
}
```

在类的内部直接写main入口，则可以直接运行测试

## String的比较不能写==，要写equals

`==` 运算符用于比较两个对象的引用是否相同，即判断两个对象是否指向同一个内存地址。而 `equals()` 方法用于比较两个对象的内容是否相等，即判断两个对象的字符序列是否相同。

在 Java 中，可以使用 `==` 运算符来比较两个 `char` 类型的变量是否相等。

> `char` 类型是基本数据类型，用于表示单个 Unicode 字符。因为 `char` 是一个原始类型，而不是一个对象，所以没有 `equals()` 方法可用于 `char` 类型的比较。

## toString（）里改变对象属性，会在调试时出现“闹鬼”现象

