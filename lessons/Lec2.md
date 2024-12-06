# Lec 2

## 属性与方法的设计（类的设计与实现细节

1. 我们关心这个类的什么状态，隐藏了哪些内部状态？

- 实践中，很容易把属于其他类的属性放到当前类中

  > 图书馆系统的读者类中很容易包含借阅信息
  >
  > 应该建立借阅信息类，让读者类有查阅其的方法

- eg：学生成绩管理系统，功能包括选课、填报成绩、查询成绩、统计学分

  ![image-20240304145843945](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240304145843945.png)

  ![image-20240304145917096](C:\Users\CuZn\AppData\Roaming\Typora\typora-user-images\image-20240304145917096.png)

- 状态查询方法&状态改变方法：

  1. 信息专家
  2. 控制专家

