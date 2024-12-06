## OO-M3-U2-log

> error on line 931: we got '2' when we expect '0'

全都是qtvs的错误，expect是0但是我输出了一个非零值

把qtvs的实现直接改成了遍历，发现还是一样

说明不是tag里实现的问题

本该输出pinf-54, 85-1的地方我输出了OK（第2350行，dft 84 44 1

之后的pinf计数都是错的（）

发现自己对jml的理解错了。。。具体是在modifyRelation那里



现在的问题是qsp老是多输出1.

改成单向BFS就过了。。不懂为什么



qsp 1 1会输出-1.。



现在有一些点生成的shortestPath会短



qcs coupleSum错误

> ap 1 DPO-bot-1 110
> ap 2 DPO-bot-2 162
> ap 3 DPO-bot-3 76
> ar 1 2 184 //
> ap 4 DPO-bot-4 120
> at 4 51
> ap 5 DPO-bot-5 65
> mr 1 2 -171 //
> at 2 1
> at 3 1
> at 5 29
> att 1 2 1
> ar 2 5 15 //
> ar 4 5 110 //
> ap 7 DPO-bot-7 176
> qcs

一点一点de，要有耐心，总能找到问题的，我相信你。

修改了person中addLink中的逻辑，假如是modify的话直接找bestAcq。



> error on line 9847: we got '0' when we expect '290'

错在qtvs tagValueSum

笑死了改成遍历就对了



没懂啊……为什么把bestAcq改对了之后，shortestPath就不会抛出奇怪的只差1的值了……好奇怪呜呜呜呜呜



因为担心超时，还是想把valuesum改成动态的

但是会出错

expect-360，实际-0

错在qtvs 4 1,wd8中两个错误点都在这里



我悟了 在中间两点联系发生改变的时候，valuesum也需要更新。。

tle的点：给一个person的一个tag加上五千多个人，他们都有关系；然后一直qtvs



不同的tag可能有相同的id

那要怎么保证它们能够不重复地添加进tags里呢？

!arraylist.contains(tag)