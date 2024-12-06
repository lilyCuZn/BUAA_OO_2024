## 【BUAA-OO】M3：根据JML要求写代码

## 题目概述

第三单元的作业需要我们根据课程组提供的代码要求（由JML语言写出，称为“规格”），写出代码，实现一个模拟社交系统。

## 架构设计

由于JML中几乎完全把代码逻辑写出来了，假设没有性能要求，完全可以直接按照JML一点一点写完。但是由于多处存在二重、甚至三重循环，数据量大时可能一分钟都卡不出一个输出，所以进行优化是很必要的。

### 动态规划

优化的一方面便是动态规划。具体来说，对于每一个可能会被query到的值，我们都需要判断它是否需要动态规划。

经过写代码-评测机TLE-写代码-评测机TLE的循环，渐渐能得到需要动态规划的值：blockSum、coupleSum、tripleSum、TagAgeVar、TagValueSum等。在每一次改变有关变量时（比如addRelation、modifyRelation），我们都需要对这些变量的新值进行计算并保存，这样query的时候，直接返回已有值就好了。这里提一些我觉得比较难的动态规划：

对于blockSum，主流方法是使用**并查集**，而我在本次作业中使用的方法则是存储一个`ArrayList<HashMap<Integer, Person>>`，其中`HashMap<Integer,Person>`代表一个Block。addRelation时逻辑比较简单：

````java
//addRelation(int id1, int id2):
myPerson1.linkWith(myPerson2, value);
myPerson2.linkWith(myPerson1, value);
addBlocks(id1, id2);

//addBlocks(id1, id2):
//假如原来id1和id2就在一个block->不做操作
//假如原来两人不在一个block->将两个block合并
if (flag == 1) { //两人不在一个block：
            for (HashMap<Integer, Person> block : blocks) {
                if (block.containsKey(id2)) {
                    block1.putAll(block); //合并
                    block2 = block;
                    break;
                }
            }
            blocks.remove(block2); //删掉另一个block
        }
````

而在modifyRelation中，涉及到Relation的删除时，则更复杂一些：

````java
//delBlocks(id1, id2):
for (HashMap<Integer, Person> block : blocks) {
            if (block.containsKey(id1) && block.containsKey(id2)) {
                HashMap<Integer, Person> newblock = new HashMap<>();
                getBlockFromId(id1, block, newblock); //根据id1从block中找出id1现在所连通的所有人newBlock
                //是一个递归函数
                if (newblock.containsKey(id2)) {
                    //id1和id2还在一个block内，不用处理
                } else {
                    block.keySet().removeAll(newblock.keySet()); //block2
                    blocks.add(newblock); //block1
                }
                break;
            }
        }
````

对于valueSum，它的动态规划给我留下了深刻的心理阴影。。五一放假五天第二次作业一点没写，导致ddl前一天晚上熬到凌晨四点试图写出valueSum的动态规划，但是依然没写出来……最后还是在第二天下午四五点的时候终于搞明白了。

````java
//Tag.java
/*@ ensures \result == (\sum int i; 0 <= i && i < persons.length; 
      @          (\sum int j; 0 <= j && j < persons.length && 
      @           persons[i].isLinked(persons[j]); persons[i].queryValue(persons[j])));
      @*/
public /*@ pure @*/ int getValueSum();
````

（假如直接按照JML写，碰到几千条qtvs会直接TLE……但是有的时候不是我不想，而是我做不到啊：（

valueSum表示的是一个tag的persons中两个相连的人之间的value之和。什么时候valueSum会发生改变？给tag加人；从tag删人；没加没删但是悄悄把两个人之间的关系改变了（我绞尽脑汁也没想到的点（））。所以，在遇到这些操作时，都需要对valueSum的值进行修改。

````java
public void manageValue(int op, Person person, int value) {
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
````

### 算法

由于是维护一个社交网络，每个人就是一个点，两个人之间的联系就是一条线，所以这次作业是一个图论的问题。

在第二次作业的`queryShortestPath`中，会用到计算两点之间不加权最短路径的方法——BFS（广度优先算法）。进一步优化，有同学提出了可以使用双向BFS，但是不知道为什么我实现的会有问题……所以最后还是直接BFS了。

## 规格与实现分离

经历过TLE后，渐渐认识到JML所描述的要求与我们实际上实现的东西是要分开来看的。在研讨课中有一位同学提到，他会先对JML进行阅读，从中得到这个方法的目的是什么，然后再写一遍方法注释，最后根据这个第二次的理解来书写真正的java代码。

### JUnit

在Junit的编写中，我深刻地感受到了对JML【全方位的验证】是什么概念。我们不仅仅是对计算结果进行验证，而要对方法前后的所有相关变量都要检验。pure方法前后变量的值、数组的长度、persons中每个person的id是否相等……都需要验证。其实只要跟着JML一步一步写，基本上都能通过junit测试；但是依然存在至今看来玄学的junit点，比如作业1的case6，看起来验证的内容都相似，可是有的同学就能过，我写的就过不了（）最后转向大规模数据测试，比如使用@Parameters。

## 对测试的理解

### 黑盒测试 & 白盒测试

黑盒测试时，我们不在乎代码内部逻辑是怎么实现的；常见的黑盒测试如大神们搭的评测机（实际上是与大神的代码对拍）。白盒测试则是把测试对象看成一个透明的盒子，它允许测试人员利用程序内部的逻辑结构设计测试用例，对程序所有逻辑路径进行测试。常见的白盒测试如我们写的JUnit。

### 其他测试

1. 单元测试（Unit Testing）：

   > 单元测试是针对软件系统中最小的可测试单元（通常是函数、方法或类）进行的测试。它的目的是验证单元的行为是否符合预期，以确保各个单元在独立测试时能够正常工作。单元测试通常由开发人员编写，并可以在开发过程中频繁运行，以发现并修复代码中的问题。

   这就是Junit啦。

2. 功能测试（Functional Testing）：

   > 功能测试是对软件系统的各个功能模块进行测试，以验证其是否按照需求规格说明书或用户需求的要求正常工作。功能测试关注整个系统的功能和行为，以确保它能够按照预期执行各项任务和操作。功能测试通常由测试团队或专业测试人员执行。

   我感觉功能测试更像是黑盒测试，从最基础的指令开始测起，不断新增指令验证正确性。

3. 集成测试（Integration Testing）：

   > 集成测试是将多个独立的软件组件或模块组合在一起进行测试，以验证它们在集成后是否协同工作。集成测试的目标是检测组件之间的接口问题、数据传递问题、功能交互问题等。它有助于发现不同组件集成时可能出现的错误和故障，并确保整个系统的稳定性和一致性。

   用于netWork和Tag联动这样的测试。

4. 压力测试（Stress Testing）：

   > 压力测试是通过模拟实际使用情况下的负载和压力条件，对软件系统进行测试。其目的是评估系统在高负载情况下的性能、稳定性和鲁棒性。压力测试通常会将系统推向其极限，以检测性能瓶颈、资源耗尽、内存泄漏等问题。

   在本单元作业中，更具压力的数据是：1.不会导致异常，每条指令都有效；2.大量输入，以3000-10000条为佳。

5. 回归测试（Regression Testing）：

   > 回归测试是在对软件系统进行更改或修复后执行的测试，以确保已经修复的问题没有引入新的错误，并且旧有的功能没有受到影响。回归测试旨在验证软件的稳定性和一致性，以确保在修改过程中未引入新的缺陷或导致现有功能的退化。

   在本单元中，回归测试指的就是当修复上一次强测bug后需要重新对其正确性进行检验。否则，会出现在下一次作业中发现上一次作业的指令错误的情况。

### 数据构造策略

针对JML中有多重循环的方法，构造多次调用。典型例子：第二次作业的第十个点，在把人和关系加之后，指令全都是qtvs(query tag value sum)。

同时还需要注意到JML中一些容易遗漏的说明，如：以1111为界进行不同的操作（第二次作业）；当除以0时需要进行不同的处理（第三次作业）等。

## 本单元学习体会

这一单元的强度比起第一第二单元弱了许多，可能是因为不需要我来想要写什么方法了，毕竟JML把要写方法的底线都给我列出来了（虽然要自己改逻辑）。但是，在尝试进行动态规划时还是遇到了好些困难，点名ValueSum！！JML容易让我管中窥豹，看到的是一个又一个的细节，而形不成总体的观念，不知道写这个方法是要干什么，这就让跨多个类的动态规划变得更加困难。事实上，我现在都不太清楚第三次作业发信息的逻辑是什么，完完全全是按着JML硬写的。嗯……所以可以学习研讨课同学提出的方法，先阅读一遍JML，写下自己的注释，然后再开始写java代码。

在周三上午的上机中，书写JML也给我造成了困惑。捋清楚exist和forall还是需要一些时间。

总的来说，这一单元的JML学习对于现在的我还是挺朦胧的。不过确实捡起来了一些图论的算法哈哈哈哈 有时间再回炉重造一下吧