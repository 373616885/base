### 强制覆盖本地分支

**第一种方式: reset --hard 参数**

```shell
git fetch --all
git reset --hard origin/main
git pull origin main
```

**第二种方式：pull --force参数**

$ git pull --force  <远程主机名> <远程分支名>:<本地分支名>

```shell
$ git pull --force origin main:main
$ git pull --force origin master:master
```



### **软重置**

删除 Merge … to

```shell
git reset --soft HEAD~
```



### 硬重置

```shell
git reset --hard HEAD~
```





### 变基

**只对尚未推送或未分享给别人的本地修改执行变基操作，清理历史 **

**不要对已推送至别处的提交执行变基操作**

**变基虽然会减少 Merge … to …记录，但会改变别人的基底**

```shell
##先变基
# feature分支
git fetch origin
git rebase main
## 合并命令
git pull --rebase main

##然后push远程
git push origin main
```





### stash

```shell
//stash指令将其缓存了
git stash
//查看当前文件状态，系统提示没有需要提交的内容
git status 
//查看本地当前的缓存列表
git stash list
//恢复指定id的stash内容,不会删除恢复的缓存条目。
git stash apply stash@{id}
//恢复最近的缓存到当前文件中，同时删除恢复的缓存条目
git stash pop
```

