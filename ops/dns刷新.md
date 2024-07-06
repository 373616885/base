然后按住Windows +X，选择命令提示符（管理员）：

1 清除ARP缓存，输入命令arp -d *代替执行。
2 清除NETBT，输入命令nbtstat -R代替执行。
3 清除DNS缓存，输入命令ipconfig /flushdns代替执行。



**重置网络**

netsh int ip reset 

netsh winsock reset