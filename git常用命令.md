### 强制覆盖本地分支

**第一种方式: reset --hard 参数**

```shell
git fetch --all
git reset --hard origin/dev (这里dev要修改为对应的分支名)
git pull origin dev
```

**第二种方式：pull --force参数**

$ git pull --force  <远程主机名> <远程分支名>:<本地分支名>

```shell
$ git pull --force origin dev:dev
```



### **软重置**

```shell
git reset --soft HEAD~
```



### 变基

**只对尚未推送或未分享给别人的本地修改执行变基操作，清理历史 **

**不要对已推送至别处的提交执行变基操作**

**变基虽然会减少 Merge … to …记录，但会改变别人的基底**

```shell
##先变基
# feature分支
git fetch origin
git rebase develop
## 合并命令
git pull main --rebase

##然后push远程
git push origin main
```







