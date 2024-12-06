## oo-m2-u2-log

- 电梯线程无法结束

  在inrequest\requestPool里的synchronized方法中加入notifyAll后解决

- 诡异的RESET

  ![image-20240404110730544](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240404110730544.png)

请出去的乘客应该是outRequest里的所有乘客，而不是get(pos)表示要到pos层下的乘客。

- ````
  [1.1]RESET-Elevator-1-8-0.3
  [3.0]2-FROM-5-TO-1
  [7.0]RESET-Elevator-1-5-0.4
  ````

- ![image-20240404114253426](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240404114253426.png)

  一直在REVERSE。。

​	![image-20240405001818803](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240405001818803.png)

说明在reset-end后1号电梯没有接到over，而是一直在reverse

原因：inrequests-delAll函数中忘记每删一个让requestsum--了

- 在最后一条指令reset之后，电梯给pool加了人，但是被判断为结束了

  ````
  [1.1]1-FROM-2-TO-3
  [12.0]RESET-Elevator-1-8-0.6
  ````

- wa

  ````
  [2.1]RESET-Elevator-5-6-0.5
  [3.5]1-FROM-2-TO-8
  [3.5]2-FROM-2-TO-8
  [3.5]3-FROM-8-TO-1
  [4.7]4-FROM-3-TO-9
  [4.7]5-FROM-3-TO-11
  [4.7]6-FROM-8-TO-2
  [6.1]RESET-Elevator-4-7-0.6
  [7.2]7-FROM-3-TO-9
  [7.2]8-FROM-9-TO-3
  [7.2]9-FROM-5-TO-11
  [8.6]RESET-Elevator-5-3-0.2
  [9.9]RESET-Elevator-1-6-0.2
  [11.0]RESET-Elevator-2-5-0.6
  [12.1]10-FROM-10-TO-1
  [12.1]11-FROM-9-TO-3
  [12.1]12-FROM-11-TO-4
  [13.3]RESET-Elevator-4-5-0.4
  [14.7]13-FROM-2-TO-8
  [14.7]14-FROM-11-TO-2
  [14.7]15-FROM-2-TO-11
  [15.7]16-FROM-7-TO-1
  [15.7]17-FROM-1-TO-9
  [15.7]18-FROM-5-TO-11
  [16.9]19-FROM-10-TO-2
  [16.9]20-FROM-1-TO-11
  [16.9]21-FROM-11-TO-2
  [17.9]22-FROM-4-TO-10
  [17.9]23-FROM-10-TO-4
  [17.9]24-FROM-11-TO-5
  ````

  ```
  Passenger 8 at 6 cannot enter elevator at floor 9
  ```

错因：应该是中途出人的时候忘记改这个人的起始地址了。。

- RESET响应太慢

  ![image-20240405011611456](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240405011611456.png)

28 RESET-ACCEPT

38.7BEGIN

39.92END

发现的问题：22秒6号电梯wait了，39秒才reset？？

应该是没有notify这个inrequests。。之前wait就一直让它wait了，直到添加了新的request才notified，导致elevator-run中其实一直卡在了inrequests.wait()

- 假如所有电梯都reset了，请求会被分配给谁？

- 会有乘客完全进不去电梯。。

  ````
  [1.1]RESET-Elevator-2-6-0.3
  [1.6]34-FROM-8-TO-6
  [2.5]RESET-Elevator-5-7-0.5
  [3.8]50-FROM-8-TO-11
  [4.2]51-FROM-6-TO-1
  [4.5]21-FROM-5-TO-11
  [4.6]3-FROM-1-TO-6
  [5.3]27-FROM-1-TO-11
  [5.4]42-FROM-3-TO-6
  [5.7]2-FROM-5-TO-2
  [6.4]43-FROM-3-TO-2
  [7.1]48-FROM-4-TO-2
  [7.3]RESET-Elevator-3-4-0.3
  [7.5]58-FROM-6-TO-7
  [7.9]24-FROM-6-TO-3
  [9.9]29-FROM-2-TO-10
  [10.1]70-FROM-3-TO-5
  [11.0]66-FROM-5-TO-1
  [11.6]54-FROM-7-TO-4
  [12.0]46-FROM-2-TO-11
  [12.5]60-FROM-11-TO-7
  [15.6]37-FROM-1-TO-6
  [16.6]62-FROM-10-TO-4
  [18.5]40-FROM-1-TO-2
  [18.9]17-FROM-6-TO-2
  [19.3]47-FROM-7-TO-3
  [19.6]36-FROM-6-TO-1
  [19.9]67-FROM-10-TO-6
  [20.8]13-FROM-6-TO-1
  [22.1]19-FROM-3-TO-8
  [23.0]30-FROM-6-TO-3
  [23.3]23-FROM-5-TO-7
  [23.8]26-FROM-1-TO-8
  [23.9]35-FROM-1-TO-7
  [24.4]7-FROM-1-TO-7
  [25.7]12-FROM-6-TO-2
  [27.4]63-FROM-8-TO-1
  [27.5]39-FROM-7-TO-1
  [27.8]44-FROM-8-TO-11
  [29.9]22-FROM-6-TO-7
  [31.1]6-FROM-11-TO-6
  [31.1]32-FROM-9-TO-3
  [32.4]18-FROM-5-TO-6
  [33.2]31-FROM-8-TO-4
  [34.3]8-FROM-9-TO-10
  [35.3]1-FROM-1-TO-5
  [37.4]49-FROM-7-TO-8
  [39.2]9-FROM-6-TO-7
  [39.5]5-FROM-3-TO-9
  [39.7]56-FROM-10-TO-11
  [39.7]15-FROM-1-TO-7
  [40.9]52-FROM-10-TO-3
  [41.1]10-FROM-6-TO-2
  [41.3]14-FROM-11-TO-7
  [41.4]57-FROM-6-TO-1
  [43.0]28-FROM-1-TO-2
  [43.4]53-FROM-10-TO-4
  [43.4]33-FROM-1-TO-3
  [43.9]4-FROM-8-TO-5
  [45.1]RESET-Elevator-6-5-0.6
  [46.9]64-FROM-4-TO-7
  [47.0]25-FROM-10-TO-8
  [47.5]41-FROM-9-TO-7
  [47.5]16-FROM-11-TO-1
  [47.6]69-FROM-6-TO-1
  [48.1]59-FROM-9-TO-10
  [49.0]38-FROM-5-TO-10
  [49.6]RESET-Elevator-1-4-0.5
  [49.7]11-FROM-1-TO-8
  ````

  ````
  unfinished request: [47.5]41-FROM-9-TO-7, person not even get in
  ````

  中途因为reset把41号放出去之后，就没有再管ta了……

​	distributor线程结束的逻辑有误？只要输入流解析结束，并且requestPool为空，就会判定为结束。

增加了requestsCounter，解决了这个问题

- ````
  [1.5]RESET-Elevator-3-6-0.3
  [1.7]RESET-Elevator-5-5-0.5
  [2.1]RESET-Elevator-1-5-0.3
  [2.5]RESET-Elevator-4-4-0.2
  [2.6]1-FROM-11-TO-2
  [2.6]2-FROM-5-TO-11
  [3.0]3-FROM-11-TO-3
  [3.0]4-FROM-8-TO-2
  [3.0]5-FROM-11-TO-3
  [3.2]6-FROM-7-TO-1
  ````

  ![image-20240405213900891](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240405213900891.png)

  会出现reset的时候还给4号电梯receive的情况，但是本地很复现不了（？？

- ````
  [1.5]RESET-Elevator-3-6-0.3
  [1.5]RESET-Elevator-5-5-0.5
  [1.5]RESET-Elevator-1-5-0.3
  [1.5]RESET-Elevator-4-4-0.2
  [1.5]RESET-Elevator-2-5-0.3
  [1.5]RESET-Elevator-6-4-0.3
  [2.0]1-FROM-11-TO-2
  ````

  当所有电梯都reset时，不能输出receive，要等它们某个reset结束后再receive

- distributor中，假如request'Pool-getOneAndRemove为空，requestPool.wait()，会出现无法结束的情况

  notify少了？

  经过各种debug解决了distributor中轮询的问题、、

  但是requestPool.wait需要注意，Counter减少了一个也就是完成了一个请求要notify，完成了一个reset也要notify。（取决于最后一个请求是什么）

- dpo测出的诡异tle（自己运行不会tle）

````
  [1.3]48-FROM-11-TO-8
  [2.3]RESET-Elevator-3-6-0.6
  [3.0]31-FROM-6-TO-11
  [3.3]40-FROM-3-TO-7
  [3.9]41-FROM-4-TO-6
  [4.3]11-FROM-8-TO-1
  [4.4]46-FROM-5-TO-2
  [4.7]65-FROM-2-TO-9
  [7.2]5-FROM-9-TO-8
  [7.5]53-FROM-9-TO-5
  [8.4]RESET-Elevator-6-7-0.3
  [12.0]49-FROM-9-TO-5
  [12.3]15-FROM-8-TO-11
  [12.8]8-FROM-10-TO-1
  [13.1]57-FROM-9-TO-6
  [13.3]39-FROM-9-TO-10
  [15.0]64-FROM-4-TO-7
  [15.8]55-FROM-3-TO-1
  [17.1]12-FROM-1-TO-7
  [18.8]16-FROM-8-TO-4
  [19.1]43-FROM-4-TO-1
  [20.4]59-FROM-3-TO-1
  [21.3]36-FROM-7-TO-10
  [22.3]RESET-Elevator-3-7-0.4
  [22.4]28-FROM-6-TO-7
  [22.7]9-FROM-3-TO-10
  [23.3]RESET-Elevator-5-6-0.3
  [23.9]20-FROM-3-TO-1
  [26.4]19-FROM-3-TO-10
  [26.5]RESET-Elevator-4-4-0.5
  [26.6]23-FROM-10-TO-6
  [27.4]35-FROM-4-TO-9
  [27.5]37-FROM-10-TO-7
  [27.8]44-FROM-4-TO-1
  [28.0]60-FROM-11-TO-10
  [29.2]25-FROM-6-TO-2
  [30.6]42-FROM-10-TO-2
  [30.8]32-FROM-7-TO-5
  [31.6]RESET-Elevator-4-5-0.5
  [32.9]63-FROM-1-TO-2
  [34.0]13-FROM-7-TO-3
  [35.1]33-FROM-2-TO-9
  [35.2]66-FROM-2-TO-9
  [36.2]70-FROM-4-TO-10
  [36.6]38-FROM-6-TO-2
  [38.8]27-FROM-5-TO-8
  [39.8]3-FROM-2-TO-1
  [40.6]RESET-Elevator-3-6-0.3
  [41.7]17-FROM-3-TO-5
  [42.0]6-FROM-3-TO-10
  [42.1]RESET-Elevator-2-3-0.2
  [42.3]4-FROM-9-TO-11
  [42.4]56-FROM-8-TO-10
  [42.6]10-FROM-7-TO-9
  [43.0]26-FROM-4-TO-1
  [43.6]52-FROM-6-TO-3
  [45.0]RESET-Elevator-5-5-0.4
  [45.2]54-FROM-4-TO-6
  [45.4]2-FROM-2-TO-11
  [45.7]RESET-Elevator-3-5-0.4
  [45.9]69-FROM-11-TO-2
  [46.3]68-FROM-1-TO-4
  [47.0]24-FROM-7-TO-4
  [47.1]61-FROM-2-TO-6
  [47.2]RESET-Elevator-2-5-0.2
  [47.6]34-FROM-5-TO-10
  [47.6]RESET-Elevator-6-8-0.2
  [48.2]51-FROM-2-TO-5
  [48.4]29-FROM-11-TO-2
````

- 天璇星：从1到6循环分配--看看会不会tle

  ````
  [50.0]1-FROM-11-TO-1
  [50.0]2-FROM-1-TO-11
  [50.0]3-FROM-1-TO-11
  [50.0]4-FROM-1-TO-11
  [50.0]5-FROM-1-TO-11
  [50.0]6-FROM-1-TO-11
  [50.0]7-FROM-11-TO-1
  [50.0]RESET-Elevator-3-3-0.6
  [50.0]RESET-Elevator-5-3-0.6
  [50.0]RESET-Elevator-4-3-0.6
  [50.0]RESET-Elevator-1-3-0.6
  [50.0]RESET-Elevator-2-3-0.6
  [50.0]RESET-Elevator-6-3-0.6
  ````

  这个数据点成功hack到了洞明星

- ````
  [2.6]RESET-Elevator-1-3-0.6
  [40.0]RESET-Elevator-2-3-0.6
  [40.0]RESET-Elevator-3-3-0.6
  [40.0]RESET-Elevator-4-3-0.6
  [40.0]RESET-Elevator-5-3-0.6
  [40.0]RESET-Elevator-6-3-0.6
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

  bzh老师的超级tle数据点

- 互测WA的数据点：

  `````
  [1.0]1-FROM-2-TO-9
  [1.0]2-FROM-8-TO-9
  [1.0]3-FROM-5-TO-11
  [1.0]4-FROM-6-TO-7
  [1.0]5-FROM-1-TO-10
  [1.0]RESET-Elevator-1-4-0.6
  [1.0]RESET-Elevator-2-4-0.6
  [1.0]RESET-Elevator-3-4-0.6
  [1.0]RESET-Elevator-4-4-0.6
  [1.0]RESET-Elevator-5-4-0.6
  [1.0]RESET-Elevator-6-4-0.6
  [1.0]7-FROM-7-TO-9
  [1.0]9-FROM-6-TO-9
  [1.0]8-FROM-5-TO-4
  [1.0]10-FROM-6-TO-7
  [1.0]11-FROM-2-TO-10
  `````

  出现的问题是，我会把乘客1、3、7RECEIVE两次，第一次给3，第二次给4.

  ````
  //前面所有电梯的RESET已经ACCEPT，3、4、5、6电梯已经RESET-BEGIN
  [   0.8920]RECEIVE-1-3//
  [   0.8930]RESET_ACCEPT-6-4-0.6
  [   0.8930]RESET_BEGIN-6
  [   0.8940]RECEIVE-2-4
  [   0.8950]RECEIVE-3-3//
  [   0.8970]RECEIVE-4-4
  [   0.8980]RECEIVE-5-4
  [   0.9000]RECEIVE-7-3//
  [   0.9010]RECEIVE-9-4
  [   0.9020]RECEIVE-8-4
  [   0.9030]RECEIVE-10-4
  [   0.9050]RECEIVE-11-4
  [   0.9060]RECEIVE-1-4//
  [   0.9070]RECEIVE-3-4//
  [   0.9080]RECEIVE-7-4//
  [   0.9180]RESET_BEGIN-4
  [   0.9180]RESET_BEGIN-3
  ````

  我发现1、3、7乘客都是一开始receive给3号电梯的乘客。怀疑是<u>三号电梯reset-accept时（但是还没reset-begin），把它们从列表中删除了，分配给了四号电梯</u>。

  修改了顺序后，就对了。

- 互测tle数据点1：

  ````
  [1.0]RESET-Elevator-1-4-0.6
  [1.0]4-FROM-1-TO-3
  [1.0]RESET-Elevator-2-4-0.6
  [1.0]RESET-Elevator-3-4-0.6
  [1.0]26-FROM-8-TO-3
  [1.0]45-FROM-1-TO-5
  [1.0]RESET-Elevator-4-4-0.6
  [1.0]RESET-Elevator-5-4-0.6
  [1.0]RESET-Elevator-6-4-0.6
  [1.0]2-FROM-11-TO-7
  [3.3]43-FROM-4-TO-8
  [24.3]7-FROM-3-TO-11
  ````

  也是一个很诡异的，在本地目前还没有复现的bug……

  很神奇的是，更改了上面的那个顺序之后，就对了。但是感觉非常的运气……

