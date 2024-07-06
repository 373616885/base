编辑/etc/default/grub文件其中的一行

修改为GRUB_CMDLINE_LINUX_DEFAULT="quiet splash video=hyperv_fb:宽x高"

最大改到1920X1080

```shell
vi /etc/default/grub


GRUB_CMDLINE_LINUX_DEFAULT="quiet splash video=hyperv_fb:1920X1080"

### 更新
sudo update-grub

```

