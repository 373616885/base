# 指定基础镜像，必须为第一个命令
FROM ubuntu

# 维护者信息
MAINTAINER qinjp <373616885@qq.com>

# 构建镜像时执行的命令
RUN echo Asia/Shanghai > /etc/timezone 

RUN apt-get -y update 

RUN apt-get -y install vim

RUN apt-get -y install openssh-server openssh-client

RUN apt-get -y install curl

RUN apt-get -y install net-tools

RUN apt-get -y install tcpdump

RUN apt-get -y install netcat

RUN apt-get -y install iputils-ping

RUN apt-get -y install htop

ADD sshd_config /etc/ssh/sshd_config

## 设置root密码为373616885
RUN echo root:373616885 | chpasswd


# 指定于外界交互的端口
EXPOSE 22

# CMD ["/etc/init.d/ssh","start"]

CMD service ssh restart -D

# 工作目录，类似于cd命令
WORKDIR /usr/local/

