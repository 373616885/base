![](img\2022-07-24 042053.png)





![](img\2022-07-24 042054.png)



![](img\2022-07-24 042055.png)



![](img\2022-07-24 042058.png)





Hyper-V的虚拟目录机（C:\ProgramData\Microsoft\Windows\Hyper-V\Virtual Machines）中自动生成了一个与虚拟机内存一样大小的VMRS文件。



VMRS文件是Hyper-V用来存储虚拟机状态的文件。在虚拟机运行过程中，Hyper-V自动将虚拟机的状态保存在这个文件中，当物理机异常关机，再次启动后，我们任然可以获得虚拟机中当时的状态，不会出现数据丢失的情况。



### 方案一：

关闭VMRS文件

将虚拟机的选项设置为“**关闭来宾操作系统**”，则系统不会自动生成VMRS文件。

![](img\2022-07-24 042056.png)



### 方案二：

转移VMRS的位置

![](img\2022-07-24 042057.png)