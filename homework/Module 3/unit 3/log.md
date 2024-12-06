## unit 3 log

> sm 228

error on line 1497: we got 'Ok' when we expect 'minf-16, 228-1'

> sm 220
> at 8 1
> anm 402 homo-35739 0 56 8
> qrm 52
> sei 23
> sm 221
> sm 222
> qrm 25
> am 403 695 0 12 1
> qm 64
> sm 223
> dce 1
> ar 218 89 75
> qrm 97
> aem 404 14 0 14 59
> mr 86 99 -91
> ap 12 DPO-bot-12 134
> qsv 80
> anm 405 homo-2930 0 87 19
> qm 96
> qrm 71

这一段里，存在着一个删掉了228号message的指令。

>am 403 695 0 12 1
>qm 64
>sm 223
>dce 1
>ar 218 89 75
>qrm 97

应该是dce的问题。deletecoldemoji，因为这个方法被我移到了methods类，在methods类中并没有对network里的emojiList和messages做修改。