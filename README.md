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

>鉴权方案见: [服务网关鉴权方案](https://github.com/dapeng-soa/dapeng-soa/wiki/dapeng-mesh%E9%89%B4%E6%9D%83%E6%96%B9%E6%A1%88)

> 如果开启鉴权,需要启动 dapeng-mesh-auth 服务进行认证
> 详情请见: [dapeng-mesh-auth](https://github.com/dapeng-soa/dapeng-mesh-auth)

### 选择 zookeeper 连接

```
soa.zookeeper.host=192.168.10.12:2181

```

## 请求示例，不携带API Key
```
curl 'https://127.0.0.1:800/api/com.to.serviceName/1.0.0/methodName.html?cookieStoreId=1234' \
--data 'parameter={"body":{"code":"SKU_FINANCE_TYPE"}}'

-- 返回包：
{"success":  -- 对应服务返回的数据
	[ {	"id":40894,"parentCode":"", ......},
		......
	]
,"status":1  -- status 为1 表示请求成功
}

失败返回包：
{"responseCode":"error-code",
 "responseMsg":"error-message",
 "success": {},
 "status":0	-- status 为 0 表示请求失败
 }
```

## 携带 APIKey
```
curl 'http://gateway.xxx.cn/api/{serviceName}/{version}/{methodName}/{apikey}?timestamp=1525946628000&secret2=xxxxxx'
--data 'parameter={"body":{"code":"SKU_FINANCE_TYPE"}}'

secret2=MD5(apikey+tmiestamp+password+parameter)
```
