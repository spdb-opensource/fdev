# ebank-helper 打包步骤

1.登录到打包机

2.clone ebank-helper源码，并切换到需要打包的分支

```shell
git clone git地址
cd ebank-helper
git checkout 分支名
```

3.登录镜像仓库

```shell
docker login 镜像仓库地址 -u ${user} -p ${password}
```

4.镜像打包

```shell
# 测试镜像tag建议命名为"当天日期_001"，例如"20210826_001"；正式版本tag为"latest"
docker build . -t 镜像仓库地址/mbpe-public-docker-local/library/ebank-helper:20210826_001 
```

5.推送镜像至镜像仓库

```shell
docker push 镜像仓库地址/mbpe-public-docker-local/library/ebank-helper:20210826_001
```

6.退出登录

```shell
docker logout 镜像仓库地址  
```

7.登录JFROG镜像仓库(迁移后)

```shell
docker login 镜像仓库地址 -u 用户名-mbpe -p 密码
```

8.JFROG镜像打包(迁移后)

```shell
# 测试镜像tag建议命名为"当天日期_001"，例如"20210826_001"；正式版本tag为"latest"
docker build . -t 镜像仓库地址/mbpe-public-docker-local/library/ebank-helper:20210826_001
```

9.推送镜像至JFROG镜像仓库(迁移后)

```shell
docker push 镜像仓库地址/mbpe-public-docker-local/library/ebank-helper:20210826_001
```

10.退出登录JFROG镜像仓库

```shell
docker logout 镜像仓库地址
```
