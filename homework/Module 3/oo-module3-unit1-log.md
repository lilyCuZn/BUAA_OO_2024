## oo-module3-unit1-log

- 错在一个qts，本来应该输出9102，我输出了54612

  9102*6 = 54612

  所以我会把所有三角形算六遍？

  querytriplesum: sum/6

- 发现输入大数据时会卡死，不知道是不是因为modifyTriple太久的原因

  把三重循环删了之后，立刻就结束了，所以就是这个原因

- 更新了tripleSum的计算，每次都进行加和减，但是仍然出现了问题：

  error on line 385: we got '82238' when we expect '82160'

  error on line 105: we got '1680010' when we expect '1679580'

  ````
  qts //1679580
  ar 358 402 191
  ap 80 DPO-bot-80 177
  mr 264 319 28
  ap 181 DPO-bot-181 72
  ap 187 DPO-bot-187 53
  mr 372 358 65
  mr 265 426 64
  mr 399 266 -134
  ap 202 DPO-bot-202 177
  ap 221 DPO-bot-221 90
  mr 382 380 -152
  ar 94 428 47
  ar 395 441 10
  mr 157 37 -1
  ap 222 DPO-bot-222 62
  mr 152 157 69
  qts //1680010，wronganser
  ````

  问题出在：在modifyRelation中，会有本来就存在的relation，这个时候并不需要添加tripleSum。

  ![image-20240426181957197](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240426181957197.png)

- error on line 3537: we got '2246839' when we expect '2246602'

还是qts的问题

````
qts
ap 380 DPO-bot-380 167
ap 381 DPO-bot-381 5
ap 382 DPO-bot-382 125
ap 383 DPO-bot-383 161
mr 49 59 -183
ap 384 DPO-bot-384 174
qts
````

有一个modifyRelation，但是没有减少triple

成功进入了modify-delete，但是在deleteTriples中并没有找到size大于等于3，既包含id1又包含id2的block。

甚至并没有既包含id1又包含id2的block

原来是break放错位置了，导致遍历到第一个block就退出了

- error on line 4680: we got '1521104' when we expect '1521105'

  ````
  qts //1521104
  ar 11 244 39
  ap 432 DPO-bot-432 31
  qts //1521104(expect 1521105)
  ````

  因为是复制粘贴，所以同样的一个break错误在addTriples和deleteTriples中犯了两次