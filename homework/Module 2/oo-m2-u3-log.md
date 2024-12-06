## oo-m2-u3-log

啊啊啊双轿厢电梯要共享outRequests吗？？

感觉没必要……？可以共享inRequests

得想个办法把outRequests分开。

感觉可以共享诶，只是需要上锁

- ![image-20240413002824025](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413002824025.png)

![image-20240413002834788](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413002834788.png)

然后6号梯A梯就一直在REVERSE……

它的换乘楼层是4。

25号乘客要从2楼到9楼，怀疑是中途放下的逻辑出错了。

懂了，是hasReqInSameDirection中的逻辑有问题。



![image-20240413003828053](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413003828053.png)

很幽默：双轿厢电梯会到0层

？？为什么B梯也会飙到下面来

![image-20240413003956519](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413003956519.png)

当它们在1楼的时候，strategy给的指令是MOVE

![image-20240413004748278](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413004748278.png)

第一个receive的是27号，他要从3到4

![image-20240413004237717](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413004237717.png)

````
[   5.3010]OPEN-3-6-A
[   5.5020]IN-27-3-6-A
[   5.7020]ARRIVE-4-6-B
[   5.7020]OPEN-4-6-B
[   5.7020]CLOSE-3-6-A
[   5.9020]OUT-27-4-6-B
[   6.1030]CLOSE-4-6-B
[   6.1030]ARRIVE-4-6-A
[   6.5100]ARRIVE-3-6-A
[   6.5100]ARRIVE-3-6-B
````



[12.4]14-FROM-3-TO-7

![image-20240413004413826](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413004413826.png)

[12.9]57-FROM-10-TO-9

![image-20240413004507221](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413004507221.png)

[12.1]25-FROM-2-TO-9

发现B梯根本没设定transferFloor

但是还是没有解决会到达0楼和12楼的问题、、

![image-20240413010155021](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413010155021.png)

为何依然move？

![image-20240413010358241](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413010358241.png)

15从4到8

给了B，但是A梯也会移动。

修改代码架构，每个双轿厢电梯都有自己的inRequests！

遇到问题：如果电梯在doubleReset，那么buffer要怎么填？

填一个共有的buffer，填完再分配？



![image-20240413031324038](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413031324038.png)

不停止

6-B线程未停止



````
[1.4]26-FROM-10-TO-9
[1.5]28-FROM-9-TO-10
[4.7]29-FROM-8-TO-7
[4.9]63-FROM-9-TO-3
[5.8]9-FROM-4-TO-2
[8.3]50-FROM-9-TO-10
[9.6]55-FROM-3-TO-8
[11.8]47-FROM-9-TO-4
[12.4]59-FROM-7-TO-2
[15.1]40-FROM-9-TO-2
[15.8]RESET-DCElevator-1-5-7-0.2
[16.2]67-FROM-10-TO-3
[16.9]57-FROM-9-TO-4
[18.1]54-FROM-9-TO-7
[18.9]30-FROM-5-TO-7
[19.5]38-FROM-2-TO-1
[20.0]13-FROM-1-TO-8
[20.6]52-FROM-8-TO-6
[21.5]56-FROM-3-TO-8
[22.2]45-FROM-7-TO-10
[22.3]11-FROM-1-TO-5
[22.9]2-FROM-4-TO-7
[25.2]62-FROM-5-TO-7
[25.4]35-FROM-3-TO-2
[27.5]33-FROM-5-TO-3
[27.9]6-FROM-1-TO-7
[28.4]61-FROM-4-TO-9
[29.0]RESET-Elevator-2-7-0.5
````

tle

二号电梯没有OVER

不能把B号电梯设置为2*id，而应该是id+6



![image-20240413035443565](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413035443565.png)

````
[1.2]18-FROM-3-TO-11
[1.3]61-FROM-6-TO-10
[1.5]RESET-DCElevator-2-7-4-0.3
[1.6]60-FROM-8-TO-4
[1.9]59-FROM-1-TO-10
[1.9]10-FROM-1-TO-10
[2.9]48-FROM-7-TO-10
[3.5]RESET-DCElevator-5-6-8-0.2
[4.1]41-FROM-3-TO-5
[4.3]69-FROM-11-TO-4
[4.5]21-FROM-2-TO-5
[5.7]1-FROM-2-TO-1
[7.3]44-FROM-11-TO-8
[7.3]70-FROM-10-TO-4
[7.5]25-FROM-10-TO-8
[8.2]27-FROM-3-TO-9
[8.4]67-FROM-6-TO-7
[9.2]RESET-Elevator-6-4-0.5
[9.6]RESET-DCElevator-4-5-4-0.3
[11.4]11-FROM-10-TO-8
[11.6]62-FROM-1-TO-10
[12.9]8-FROM-6-TO-10
[13.6]37-FROM-11-TO-9
[14.1]51-FROM-1-TO-10
[15.1]64-FROM-4-TO-1
[16.7]9-FROM-8-TO-6
[17.0]16-FROM-8-TO-3
[17.4]RESET-Elevator-6-5-0.5
[17.6]35-FROM-2-TO-10
[18.9]23-FROM-1-TO-7
[18.9]52-FROM-5-TO-4
[19.6]49-FROM-11-TO-10
[20.7]12-FROM-6-TO-4
[20.7]30-FROM-8-TO-7
[22.4]53-FROM-10-TO-11
[22.7]42-FROM-2-TO-3
[24.1]65-FROM-10-TO-11
[24.7]63-FROM-11-TO-3
[25.4]7-FROM-8-TO-1
[25.9]15-FROM-8-TO-10
[26.3]24-FROM-4-TO-7
[26.6]2-FROM-5-TO-6
[26.7]50-FROM-7-TO-9
[26.7]31-FROM-7-TO-10
[27.1]57-FROM-6-TO-5
[28.2]5-FROM-1-TO-5
[28.5]28-FROM-9-TO-6
[29.7]54-FROM-9-TO-6
[29.7]RESET-DCElevator-1-5-5-0.2
````



````
[1.0]RESET-DCElevator-1-6-4-0.2
[1.0]RESET-DCElevator-2-6-4-0.2
[1.0]RESET-DCElevator-3-6-4-0.2
[1.0]RESET-DCElevator-4-6-4-0.2
[1.0]RESET-DCElevator-5-6-4-0.2
[1.0]RESET-DCElevator-6-6-4-0.2
[1.2]50-FROM-7-TO-9
[1.2]60-FROM-1-TO-6
[1.2]70-FROM-7-TO-2
````

又会一直reverse

两个双轿厢穿的是一个strategy？不是

inputthread里逻辑错误

新的elevator中inrequest应该为0



两个双轿厢的撞车问题……

![image-20240413043652991](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240413043652991.png)

毫秒之差



## 互测

很幽默 21：23打开电脑发现7/30

啊啊啊啊啊啊

````
[10.0]1-FROM-11-TO-7
[10.0]2-FROM-11-TO-7
[10.0]3-FROM-11-TO-7
[10.0]4-FROM-11-TO-7
[10.0]5-FROM-11-TO-7
[10.0]6-FROM-11-TO-7
[10.0]7-FROM-11-TO-7
[10.0]8-FROM-11-TO-7
[10.0]9-FROM-11-TO-7
[10.0]10-FROM-11-TO-7
[10.0]11-FROM-11-TO-7
[10.0]12-FROM-11-TO-7
[10.0]13-FROM-11-TO-7
[10.0]14-FROM-11-TO-7
[10.0]15-FROM-11-TO-7
[10.0]16-FROM-11-TO-7
[10.0]17-FROM-11-TO-7
[10.0]18-FROM-11-TO-7
[10.0]19-FROM-11-TO-7
[10.0]20-FROM-11-TO-7
[10.0]21-FROM-11-TO-7
[10.0]22-FROM-11-TO-7
[10.0]23-FROM-11-TO-7
[10.0]24-FROM-11-TO-7
[10.0]25-FROM-11-TO-7
[10.0]26-FROM-11-TO-7
[10.0]27-FROM-11-TO-7
[10.0]28-FROM-11-TO-7
[10.0]29-FROM-11-TO-7
[10.0]30-FROM-11-TO-7
[10.0]31-FROM-11-TO-7
[10.0]32-FROM-11-TO-7
[10.0]33-FROM-1-TO-7
[10.0]34-FROM-1-TO-7
[10.0]35-FROM-1-TO-7
[10.0]36-FROM-1-TO-7
[10.0]37-FROM-1-TO-7
[10.0]38-FROM-1-TO-7
[10.0]39-FROM-1-TO-7
[10.0]40-FROM-1-TO-7
[10.0]41-FROM-1-TO-7
[10.0]42-FROM-1-TO-7
[10.0]43-FROM-1-TO-7
[10.0]44-FROM-1-TO-7
[10.0]45-FROM-1-TO-7
[10.0]46-FROM-1-TO-7
[10.0]47-FROM-1-TO-7
[10.0]48-FROM-1-TO-7
[10.0]49-FROM-1-TO-7
[10.0]50-FROM-1-TO-7
[10.0]51-FROM-1-TO-7
[10.0]52-FROM-1-TO-7
[10.0]53-FROM-1-TO-7
[10.0]54-FROM-1-TO-7
[10.0]55-FROM-1-TO-7
[10.0]56-FROM-1-TO-7
[10.0]57-FROM-1-TO-7
[10.0]58-FROM-1-TO-7
[10.0]59-FROM-1-TO-7
[10.0]60-FROM-1-TO-7
[10.0]61-FROM-1-TO-7
[10.0]62-FROM-1-TO-7
[10.0]63-FROM-1-TO-7
[10.0]64-FROM-1-TO-7
[12.0]RESET-DCElevator-1-7-3-0.6
[14.0]RESET-DCElevator-2-7-3-0.6
[16.0]RESET-DCElevator-3-7-3-0.6
[18.0]RESET-DCElevator-4-7-3-0.6
[20.0]RESET-DCElevator-5-7-3-0.6
[22.0]RESET-DCElevator-6-7-3-0.6
````

````
[50.0]1-FROM-11-TO-1
[50.0]2-FROM-1-TO-11
[50.0]3-FROM-1-TO-11
[50.0]4-FROM-1-TO-11
[50.0]5-FROM-1-TO-11
[50.0]6-FROM-1-TO-11
[50.0]7-FROM-11-TO-1
[50.0]RESET-DCElevator-3-6-3-0.6
[50.0]RESET-DCElevator-5-6-3-0.6
[50.0]RESET-DCElevator-4-6-3-0.6
[50.0]RESET-DCElevator-1-6-3-0.6
[50.0]RESET-DCElevator-2-6-3-0.6
[50.0]RESET-DCElevator-6-6-3-0.6
````

很逗 这个自己hack到了自己

很诡异 在dpo上运行是accepted的，但是本地运行交到互测上会有问题



````
[2.6]RESET-DCElevator-1-3-3-0.6
[40.0]RESET-DCElevator-2-4-3-0.6
[40.0]RESET-DCElevator-3-5-3-0.6
[40.0]RESET-DCElevator-4-6-3-0.6
[40.0]RESET-DCElevator-5-7-3-0.6
[40.0]RESET-DCElevator-6-8-3-0.6
[40.6]1-FROM-1-TO-11
[40.6]2-FROM-1-TO-11
[40.6]3-FROM-1-TO-11
[40.6]4-FROM-1-TO-11
[40.6]5-FROM-1-TO-11
[40.6]6-FROM-1-TO-11
[40.6]7-FROM-1-TO-11
[40.6]8-FROM-1-TO-11
[40.6]9-FROM-1-TO-11
[40.6]10-FROM-1-TO-11
[40.6]11-FROM-1-TO-11
[40.6]12-FROM-1-TO-11
[40.6]13-FROM-1-TO-11
[40.6]14-FROM-1-TO-11
[40.6]15-FROM-1-TO-11
[40.6]16-FROM-1-TO-11
[40.6]17-FROM-1-TO-11
[40.6]18-FROM-1-TO-11
[40.6]19-FROM-1-TO-11
[40.6]20-FROM-1-TO-11
[40.6]21-FROM-1-TO-11
[40.6]22-FROM-1-TO-11
[40.6]23-FROM-1-TO-11
[40.6]24-FROM-1-TO-11
[40.6]25-FROM-1-TO-11
[40.6]26-FROM-1-TO-11
[40.6]27-FROM-1-TO-11
[40.6]28-FROM-1-TO-11
[40.6]29-FROM-1-TO-11
[40.6]30-FROM-1-TO-11
[40.6]31-FROM-1-TO-11
[40.6]32-FROM-1-TO-11
[40.6]33-FROM-1-TO-11
[40.6]34-FROM-1-TO-11
[40.6]35-FROM-1-TO-11
[40.6]36-FROM-1-TO-11
[40.6]37-FROM-1-TO-11
[40.6]38-FROM-1-TO-11
[40.6]39-FROM-1-TO-11
[40.6]40-FROM-1-TO-11
[40.6]41-FROM-1-TO-11
[40.6]42-FROM-1-TO-11
[40.6]43-FROM-1-TO-11
[40.6]44-FROM-1-TO-11
[40.6]45-FROM-1-TO-11
[40.6]46-FROM-1-TO-11
[40.6]47-FROM-1-TO-11
[40.6]48-FROM-1-TO-11
[40.6]49-FROM-1-TO-11
[40.6]50-FROM-1-TO-11
[40.6]51-FROM-1-TO-11
[40.6]52-FROM-1-TO-11
[40.6]53-FROM-1-TO-11
[40.6]54-FROM-1-TO-11
[40.6]55-FROM-1-TO-11
[40.6]56-FROM-1-TO-11
[40.6]57-FROM-1-TO-11
[40.6]58-FROM-1-TO-11
[40.6]59-FROM-1-TO-11
[40.6]60-FROM-1-TO-11
[40.6]61-FROM-1-TO-11
[40.6]62-FROM-1-TO-11
[40.6]63-FROM-1-TO-11
````

这个数据点hack到了天璇星和玉衡星



````
[2.6]RESET-DCElevator-1-4-3-0.6
[3.7]1-FROM-1-TO-11
[4.0]2-FROM-1-TO-11
[4.0]3-FROM-1-TO-11
[4.1]4-FROM-1-TO-11
[4.2]5-FROM-1-TO-11
[4.3]6-FROM-1-TO-11
[4.4]7-FROM-1-TO-11
[4.5]8-FROM-1-TO-11
[4.6]9-FROM-1-TO-11
[4.7]10-FROM-1-TO-11
[5.0]RESET-DCElevator-2-4-3-0.6
[6.0]RESET-DCElevator-3-4-3-0.6
[6.5]RESET-DCElevator-4-4-3-0.6
[6.5]RESET-DCElevator-5-4-3-0.6
[6.5]RESET-DCElevator-6-4-3-0.6
[7.0]57-FROM-4-TO-11
[7.0]58-FROM-4-TO-1
[7.0]59-FROM-4-TO-10
[7.0]60-FROM-11-TO-4
[7.0]61-FROM-2-TO-4
[7.0]62-FROM-3-TO-4
[7.0]63-FROM-1-TO-4
[7.0]64-FROM-4-TO-1
[7.0]65-FROM-4-TO-10
[7.0]66-FROM-11-TO-4
[7.0]67-FROM-2-TO-4
[7.0]68-FROM-3-TO-4
[7.0]69-FROM-1-TO-4
````

某个hack到自己的数据点……

特别诡异，又是交到dpo上运行发现没有问题、、很幽默



````
[1.0]RESET-DCElevator-1-5-4-0.6
[1.0]4-FROM-1-TO-3
[1.0]RESET-DCElevator-2-5-4-0.6
[1.0]RESET-DCElevator-3-5-4-0.6
[1.0]26-FROM-8-TO-3
[1.0]45-FROM-1-TO-5
[1.0]RESET-DCElevator-4-5-4-0.6
[1.0]RESET-DCElevator-5-5-4-0.6
[1.0]RESET-DCElevator-6-5-4-0.6
[1.0]2-FROM-11-TO-7
[3.3]43-FROM-4-TO-8
[24.3]7-FROM-3-TO-11
````

hack到了天枢星和天璇星



````
[1.0]RESET-DCElevator-1-5-4-0.6
[1.0]4-FROM-1-TO-3
[1.0]RESET-DCElevator-2-5-4-0.6
[1.0]RESET-DCElevator-3-5-4-0.6
[1.0]26-FROM-8-TO-3
[1.0]45-FROM-1-TO-5
[1.0]RESET-DCElevator-4-5-4-0.6
[1.0]RESET-DCElevator-6-5-4-0.6
[1.0]2-FROM-11-TO-7
[1.0]43-FROM-4-TO-8
[1.0]7-FROM-3-TO-11
[1.0]6-FROM-5-TO-11
[1.0]RESET-DCElevator-5-5-4-0.6
[1.0]333-FROM-4-TO-8
[1.0]444-FROM-3-TO-11
[1.0]555-FROM-5-TO-11
[1.0]666-FROM-4-TO-8
[1.0]777-FROM-3-TO-11
[1.0]888-FROM-5-TO-11
````

hack到了天璇星和洞明星



````
[1.0]1-FROM-2-TO-9
[1.0]2-FROM-8-TO-9
[1.0]3-FROM-5-TO-11
[1.0]4-FROM-6-TO-7
[1.0]5-FROM-1-TO-11
[1.0]27-FROM-2-TO-9
[1.0]333-FROM-11-TO-1
[1.0]444-FROM-5-TO-11
[1.0]555-FROM-6-TO-7
[1.0]666-FROM-1-TO-10
[1.0]RESET-Elevator-1-4-0.6
[1.0]RESET-DCElevator-2-4-5-0.6
[1.0]RESET-DCElevator-3-4-4-0.6
[1.0]RESET-DCElevator-4-3-4-0.6
[1.0]RESET-DCElevator-5-5-4-0.6
[1.0]RESET-DCElevator-6-4-4-0.6
[1.0]7-FROM-7-TO-9
[1.0]9-FROM-6-TO-9
[1.0]8-FROM-5-TO-4
[1.0]10-FROM-6-TO-7
[1.0]11-FROM-2-TO-10
[1.0]12-FROM-7-TO-9
[1.0]13-FROM-6-TO-9
[1.0]14-FROM-5-TO-4
[1.0]15-FROM-6-TO-7
[1.0]16-FROM-2-TO-10
[1.0]17-FROM-7-TO-9
[1.0]18-FROM-6-TO-9
[1.0]19-FROM-5-TO-4
[1.0]20-FROM-6-TO-7
[1.0]21-FROM-2-TO-10
[1.0]22-FROM-7-TO-9
[1.0]23-FROM-6-TO-9
[1.0]24-FROM-5-TO-4
[1.0]25-FROM-6-TO-7
[1.0]26-FROM-2-TO-10
````

嗯嗯 一个hack到自己的数据点



![image-20240415232320642](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240415232320642.png)





````
[10.0]RESET-Elevator-1-3-0.6
[10.0]RESET-Elevator-2-3-0.6
[10.0]RESET-Elevator-3-3-0.6
[10.0]RESET-Elevator-4-3-0.6
[10.0]RESET-Elevator-5-3-0.6
[10.0]RESET-Elevator-6-3-0.6
````

互测、、最经典的六个RESET没过……甚至不是DCElevator……

改成1.0s输入就不会有问题了？？

![image-20240415233410919](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240415233410919.png)

改成10.0s输入后发现的问题：elevator3没有over？？

![image-20240416000051158](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240416000051158.png)

概率触发的死锁（？不知道叫不叫死锁？）

六部电梯的其中一部最先被问到advice，在wait，其它电梯都over了。

在wait的那一部电梯getAdvice的时候，inRequests还没有被setEnd

可是setEnd函数会notifyAll啊？为什么不会退出wait的死循环呢？



发现了一个别的bug：strategy中判断用到了wantToReset，但是doubleReset（）函数里没有改变wantToReset的值，不知道会不会导致错误？



继续回到上一个问题：

print大法发现，inRequest确实empty了而且没有wantToReset，但是isEnd得到的都是false（而且还是在setEnd之后！！！）这是为什么呢………………

dbq不是在setEnd之后，是在setEnd之前。但是setEnd之后只有三个电梯get了Advice

![image-20240416164020555](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240416164020555.png)

在distributor里多加了一个循环notify，似乎就可以了？



![image-20240415232320642](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240415232320642.png)

![image-20240415232152925](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240415232152925.png)

强测WA一个数据点

，互测错的也都是同样的错误。

在普通reset的时候，会先begin再receive……很奇怪欸

![image-20240416172628702](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240416172628702.png)

![image-20240416172651276](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240416172651276.png)

![image-20240416172715530](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240416172715530.png)

调整了elevator里对isReset的限制后，竟然出现了CPUTLE？？

并且互测的先reset后receive错误还是没有解决



````
[49.9]1-FROM-5-TO-1
[49.9]2-FROM-5-TO-1
[49.9]3-FROM-5-TO-1
[49.9]4-FROM-5-TO-1
[49.9]5-FROM-5-TO-1
[49.9]6-FROM-5-TO-1
[49.9]7-FROM-5-TO-1
[49.9]8-FROM-5-TO-1
[49.9]9-FROM-5-TO-1
[49.9]10-FROM-5-TO-11
[49.9]11-FROM-5-TO-11
[49.9]12-FROM-5-TO-11
[49.9]13-FROM-5-TO-11
[49.9]14-FROM-5-TO-11
[49.9]15-FROM-5-TO-11
[49.9]16-FROM-5-TO-11
[49.9]17-FROM-5-TO-11
[49.9]18-FROM-5-TO-11
[49.9]19-FROM-5-TO-11
[49.9]20-FROM-5-TO-11
[49.9]21-FROM-5-TO-11
[49.9]22-FROM-5-TO-11
[49.9]23-FROM-5-TO-11
[49.9]24-FROM-5-TO-11
[49.9]25-FROM-5-TO-11
[49.9]26-FROM-5-TO-1
[49.9]27-FROM-5-TO-1
[49.9]28-FROM-5-TO-1
[49.9]29-FROM-5-TO-1
[49.9]30-FROM-5-TO-1
[49.9]31-FROM-5-TO-1
[49.9]32-FROM-5-TO-1
[49.9]33-FROM-5-TO-1
[49.9]34-FROM-5-TO-1
[49.9]35-FROM-5-TO-1
[49.9]36-FROM-5-TO-1
[49.9]37-FROM-5-TO-11
[49.9]38-FROM-5-TO-11
[49.9]39-FROM-5-TO-11
[49.9]40-FROM-5-TO-11
[49.9]41-FROM-5-TO-11
[49.9]42-FROM-5-TO-11
[49.9]43-FROM-5-TO-11
[49.9]44-FROM-5-TO-11
[49.9]45-FROM-5-TO-11
[49.9]46-FROM-5-TO-11
[49.9]47-FROM-5-TO-11
[49.9]48-FROM-5-TO-11
[49.9]49-FROM-5-TO-11
[49.9]50-FROM-5-TO-1
[49.9]51-FROM-5-TO-1
[49.9]52-FROM-5-TO-1
[49.9]53-FROM-5-TO-1
[49.9]54-FROM-5-TO-1
[49.9]55-FROM-5-TO-1
[49.9]56-FROM-5-TO-1
[49.9]57-FROM-5-TO-1
[49.9]58-FROM-5-TO-1
[49.9]59-FROM-5-TO-1
[49.9]60-FROM-5-TO-1
[49.9]61-FROM-5-TO-1
[49.9]62-FROM-5-TO-1
[49.9]63-FROM-5-TO-1
[49.9]64-FROM-5-TO-1
[50.0]RESET-DCElevator-1-5-3-0.6
[50.0]RESET-DCElevator-2-5-3-0.6
[50.0]RESET-DCElevator-3-5-3-0.6
[50.0]RESET-DCElevator-4-5-3-0.6
[50.0]RESET-DCElevator-5-5-3-0.6
[50.0]RESET-DCElevator-6-5-3-0.6
````

互测的WA

会出现一开始receive给6号电梯的21号乘客，在6号电梯doubleReset之后，没有再被receive一次就直接进入6-A。

![image-20240416205843769](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240416205843769.png)

RECEIVE的时间和RESET_BEGIN的时间非常接近，在我的复现中甚至会出现同时的情况。