##### 模块介绍

- distributed-id-client

```
客户端，包含distributed-id-common模块
```

- distributed-id-client-base
```
客户端基础借口，用来拓展
```

- distributed-id-common

```
公共包
```

- distributed-id-server
  - distributed-id-server-base
    ```
    基础服务端,包含distributed-id-common模块
    ```
  - distributed-id-server-zk
    ```
    包含zk服务端,包含distributed-id-server-base、distributed-id-zk-common模块
    ```
- distributed-id-zk-common
```
zk公共模块
```
- id-dynamic-spring-boot-starter
    - id-dynamic-spring-boot-starter-common
      ```
      动态获取服务基础模块，包含distributed-id-client-base模块
      ```
    - id-zk-spring-boot-starter
      ```
      zk动态服务模块 包含id-dynamic-spring-boot-starter-common模块
      ```
- id-server

```
基于Spring-boot-starter模块，提供HTTP方式获取ID的应用，包含distributed-id-common、distributed-id-client、id-spring-boot-starter模块
```

- id-spring-boot-starter

```
Spring-boot-starter模块，包含distributed-id-client、distributed-id-common模块
```



##### 如何集成

- install id-spring-boot-starter模块到本地Maven仓库
- pom中引入如下依赖 (服务端用distributed-id-server-base)

```
<dependency>
    <groupId>com.geega.cloud</groupId>
    <artifactId>id-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
- pom中增加引用如下模块(服务端用distributed-id-server-zk)
```
<dependency>
    <groupId>com.geega.cloud</groupId>
    <artifactId>id-zk-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

- 使用IdClient客户端

```
@Autowired
private IdClient idClient;
```

##### 服务端参数详解
#####  distributed-id-server-base

```
# 服务IP
bind.ip=127.0.0.1
# 服务端口
bind.port=9999
# SnowFlake算法中的数据中心号
id.datacenter=1
# 基于NIO实现的Reactor模式时，Processor数量
nio.processor=3
```
#####  distributed-id-server-zk
多添加一下参数
```
# zk的连接，集群时，例子：127.0.0.1:2181，127.0.0.1:2182
zk.connection=127.0.0.1:2181
# zk命名空间
zk.namespace=id
# zk会话超时
zk.sessionTimeoutMs=10000
# zk连接超时
zk.connectionTimeoutMs=10000
```

##### 客户端参数详解
##### id-spring-boot-starter
```
# ID缓存容量个数
id.cache.capacity=40
# ID缓存少于triggerExpand时，会触发拉取操作，拉取数量为capacity - triggerExpand
id.cache.triggerExpand=20
# 服务端节点，多个节点用逗号隔开 
node.address: 192.168.1.228:9999,192.168.1.228:9998,192.168.1.228:9997
```
##### id-zk-spring-boot-starter
多添加一下参数
```
# zk命名空间
id.zk.namespace=id
# zk的连接，集群时，例子：127.0.0.1:2181，127.0.0.1:2182
id.zk.connection=127.0.0.1:2181
# zk会话超时
id.zk.sessionTimeoutMs=10000
# zk连接超时
id.zk.connectionTimeoutMs=10000
```
##### 待办事项

- ~~当客户端主动关闭连接时，服务端检测主动关闭该连接~~
- ~~第一次启动客户端时，先获取一批缓存，超时后，还未获取到数据，直接抛异常~~
- ~~新增一个spring-boot-starter，集成方便~~
- ~~服务端的可配置项，通过application.properties配置~~
- ~~客户端连接不上zk时，抛出异常，服务无法启动~~
- ~~增加spring-boot-starter测试模块~~
- ~~优化代码结构，结构更清晰~~
- ~~客户端拿到服务端的地址列表时，选择服务，并创建连接，创建连接后，以后每次调用都使用该连接，直到该连接断开~~
- ~~编写使用文档，参数详解~~
- ~~如何解决频繁启动后，zk自增的id达到snowflake中machineId的最大值，如何处理~~
- ~~优化客户端连接释放时，资源的释放~~
- ~~实现id server服务，提供http接口方式获取ID~~
- ~~优化返回给前端id数据不能为null~~
- ~~优化无需强依赖服务端启动~~
- ~~当客户端与服务端出现网络分区时的应对措施，重试？抛异常？~~
- 当获取不到ID时，想一下如何处理
- 日志打印级别设置
- 根据不同网络，硬件资源，输出QPS压测数据表格
- 目前是使用ZK实现服务注册以及自增ID的获取，也可以使用MySQL、Redis使用，支持降级的方案配置
- 日志优化
- 配置多个zk，客户端配置多个zk集群，服务端自己去配置datacenter
- 基于Netty、rpc框架实现
- 服务上下线监听
- @ConditionalOnMissingBean失效问题
- 基于nacos动态配置模块

