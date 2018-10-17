# dapeng-mesh

[![Language](https://img.shields.io/badge/language-Java-orange.svg)](https://www.oracle.com)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.dapeng-soa/dapeng-parent/badge.svg)](https://search.maven.org/search?q=com.github.dapeng-soa)
[![GitHub release](https://img.shields.io/github/release/dapeng-soa/dapeng-soa.svg)](https://github.com/dapeng-soa/dapeng-soa/releases)
[![DockerHub](https://img.shields.io/badge/docker-dapengsoa-yellow.svg)](https://hub.docker.com/r/dapengsoa/dapeng-container/)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

基于`netty`的异步网关和`service-mesh`方案

### dapeng-mesh 开启或者关闭鉴权的环境变量
> 默认是开启鉴权，可以使用如下环境变量关闭 `API` 接口鉴权。

```
soa.open.auth.enable=false

//env
soa_open_auth_enable=false
```
### 选择 zookeeper 连接

```
soa.zookeeper.host=192.168.10.12:2181

```
