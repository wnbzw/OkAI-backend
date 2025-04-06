# AI智能答题平台

1.要懂得画流程图

## 第二期 小程序开发

### 1.MBTI实现方案

#### 1.题目结构

json格式的,更加的灵活,适合排序缺点是占用空间

#### 2.用户答案结构

直接有选项组成就可以了

优点: 不用再完整传递题目的结构，节省传输体积，提高性能。

```json
["A","A","A","B","A","A","A","B","B","A"]
```

#### 3.评分规则

就是通过给每一个答案设置属性,然后对答案进行遍历算出属性的分数,对应参考答案的比例选出最合适的结果

### 2.Taro初步开发

学会了基本的taro开发,可以看官方文档初始化,

查了nvm切换版本

用了微信开发者工具

ts看懂一点!

### 3.小程序开发常用解决方案

#### 1.网络请求

主流的是 `Axios`

文档: [The Axios Instance | Axios Docs (axios-http.com)](https://axios-http.com/docs/instance)

## 第三期 平台-后端开发

### 1.需求分析:

先做出我们想要做的基本功能,然后完善更细的功能

**我们得对功能进行分级, 分清楚优先级!!!!**

### 2.库表设计

**设定要用的基本表,表中的基本字段是必不可少的,比如时间三件套**

为什么要有冗余字段?

因为回答记录一旦设置，几乎不会更改，便于查询，不用联表，节约开发成本。

### 3. 后端核心业务流程开发

### 1.审核业务

通用写一个reviewRequest,然后审核!!

步骤:

1. 首先是管理员
2. 判断是否存在
3. 判断他的状态然后更新即可

### 2.评分模块

运用策略模式,定一个总的接口

新的评分模式就实现这个接口

比如自定义评分,自定义测试等评分模块

**怎么运用呢?**

定义一个工具类 管理所有的策略

1. 引入所有的评分策略,然后执行方法的时候Switch()判断他的app类型和评分策略,**优点是 直观清晰,缺点是不利于扩展和维护**
2. 定义一个注解给每种策略加上,然后遍历策略判断是否相等执行评分方法, **优点是扩展方便,不要修改代码**

## 第四期 前端开发

## 第五期 平台智能化

调用智普ai,首先就是要看文档!!然后创建单元测试一下

#### `引入AI模块:`

1.首先导入pom包

2.创建config类注册调用`AI的客户端(client)`

3.然后建立一个`manager`,封装通用的方法,提供不同参数默认值的简化调用方法

#### `生成题目功能`

AI生成内容的核心是编写 Prompt(指导), 需要精准!!

我们首先要明确输入给AI的参数,然后构建Prompt输入给AI

**输入参数:**

1.应用信息: 应用名称, 描述, 类别 (得分/测评)

2.题目信息: 题数, 每题选项数

~~~json
你是一位严谨的出题专家，我会给你如下信息：
```
应用名称，
【【【应用描述】】】，
应用类别，
要生成的题目数，
每个题目的选项数
```

请你根据上述信息，按照以下步骤来出题：
1. 要求：题目和选项尽可能地短，题目不要包含序号，每题的选项数以我提供的为主，题目不能重复
2. 严格按照下面的 json 格式输出题目和选项
```
[{"options":[{"value":"选项内容","key":"A"},{"value":"","key":"B"}],"title":"题目标题"}]
```
title 是题目，options 是选项，每个选项的 key 按照英文字母序（比如 A、B、C、D）以此类推，value 是选项内容
3. 检查题目是否包含序号，若包含序号则去除序号
4. 返回的题目列表格式必须为 JSON 数组
~~~



![image-20250125232904548](C:\Users\16247\AppData\Roaming\Typora\typora-user-images\image-20250125232904548.png)

##### `后端开发`

1.首先建立AI生成题目请求类(AppId, 题目数量,题目选项)

2.定义模版常量和构造用户模版的方法:

```java
private static final String GENERATE_QUESTION_SYSTEM_MESSAGE = "你是一位严谨的出题专家，我会给你如下信息：\n" +
        "```\n" +
        "应用名称，\n" +
        "【【【应用描述】】】，\n" +
        "应用类别，\n" +
        "要生成的题目数，\n" +
        "每个题目的选项数\n" +
        "```\n" +
        "\n" +
        "请你根据上述信息，按照以下步骤来出题：\n" +
        "1. 要求：题目和选项尽可能地短，题目不要包含序号，每题的选项数以我提供的为主，题目不能重复\n" +
        "2. 严格按照下面的 json 格式输出题目和选项\n" +
        "```\n" +
        "[{\"options\":[{\"value\":\"选项内容\",\"key\":\"A\"},{\"value\":\"\",\"key\":\"B\"}],\"title\":\"题目标题\"}]\n" +
        "```\n" +
        "title 是题目，options 是选项，每个选项的 key 按照英文字母序（比如 A、B、C、D）以此类推，value 是选项内容\n" +
        "3. 检查题目是否包含序号，若包含序号则去除序号\n" +
        "4. 返回的题目列表格式必须为 JSON 数组";

private String getGenerateQuestionUserMessage(App app, int questionNumber, int optionNumber) {
    StringBuilder userMessage = new StringBuilder();
    userMessage.append(app.getAppName()).append("\n");
    userMessage.append(app.getAppDesc()).append("\n");
    userMessage.append(AppTypeEnum.getEnumByValue(app.getAppType()).getText() + "类").append("\n");
    userMessage.append(questionNumber).append("\n");
    userMessage.append(optionNumber);
    return userMessage.toString();
}
//使用StringBuilder 构建
```

3.AI生成接口:

```java
@PostMapping("/ai_generate")
public BaseResponse<List<QuestionContentDTO>> aiGenerateQuestion(@RequestBody AiGenerateQuestionRequest aiGenerateQuestionRequest) {
    ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
    // 获取参数
    Long appId = aiGenerateQuestionRequest.getAppId();
    int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
    int optionNumber = aiGenerateQuestionRequest.getOptionNumber();
    App app = appService.getById(appId);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
    // 封装 Prompt
    String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);
    // AI 生成
    String result = aiManager.doSyncUnstableRequest(GENERATE_QUESTION_SYSTEM_MESSAGE, userMessage);
    // 结果处理
    int start = result.indexOf("[");
    int end = result.lastIndexOf("]");
    String json = result.substring(start, end + 1);
    List<QuestionContentDTO> questionContentDTOList = JSONUtil.toList(json, QuestionContentDTO.class);
    return ResultUtils.success(questionContentDTOList);
}

```

#### `AI 智能评分`

自己设置得分规则比较麻烦, 可以使用AI 根据应用信息,题目和用户答案进行评分,直接返回结果

**比较适合测评类应用**

**输入参数**:

1.应用信息

2.题目信息

3.用户答案

**编写的Prompt:**

~~~json
你是一位严谨的判题专家，我会给你如下信息：
```
应用名称，
【【【应用描述】】】，
题目和用户回答的列表：格式为 [{"title": "题目","answer": "用户回答"}]
```

请你根据上述信息，按照以下步骤来对用户进行评价：
1. 要求：需要给出一个明确的评价结果，包括评价名称（尽量简短）和评价描述（尽量详细，大于 200 字）
2. 严格按照下面的 json 格式输出评价名称和评价描述
```
{"resultName": "评价名称", "resultDesc": "评价描述"}
```
3. 返回格式必须为 JSON 对象

~~~

##### `后端开发`

1.编写题目答案封装类

```java
public class QuestionAnswerDTO {

    /**
     * 题目
     */
    private String title;

    /**
     * 用户答案
     */
    private String userAnswer;
}

```

2.编写AI测评类应用评分策略类,需要指定对应的注解

```java
@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)

```

定义模版常量和构造用户模版的方法:

```java
private static final String AI_TEST_SCORING_SYSTEM_MESSAGE = "你是一位严谨的判题专家，我会给你如下信息：\n" +
        "```\n" +
        "应用名称，\n" +
        "【【【应用描述】】】，\n" +
        "题目和用户回答的列表：格式为 [{\"title\": \"题目\",\"answer\": \"用户回答\"}]\n" +
        "```\n" +
        "\n" +
        "请你根据上述信息，按照以下步骤来对用户进行评价：\n" +
        "1. 要求：需要给出一个明确的评价结果，包括评价名称（尽量简短）和评价描述（尽量详细，大于 200 字）\n" +
        "2. 严格按照下面的 json 格式输出评价名称和评价描述\n" +
        "```\n" +
        "{\"resultName\": \"评价名称\", \"resultDesc\": \"评价描述\"}\n" +
        "```\n" +
        "3. 返回格式必须为 JSON 对象";

private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
    StringBuilder userMessage = new StringBuilder();
    userMessage.append(app.getAppName()).append("\n");
    userMessage.append(app.getAppDesc()).append("\n");
    List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
    for (int i = 0; i < questionContentDTOList.size(); i++) {
        QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
        questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
        questionAnswerDTO.setUserAnswer(choices.get(i));
        questionAnswerDTOList.add(questionAnswerDTO);
    }
    userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
    return userMessage.toString();
}

```

实现应用评分策略:

```java
@Override
public UserAnswer doScore(List<String> choices, App app) throws Exception {
    Long appId = app.getId();
    // 1. 根据 id 查询到题目
    Question question = questionService.getOne(
            Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
    );
    QuestionVO questionVO = QuestionVO.objToVo(question);
    List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
    // 2. 调用 AI 获取结果
    // 封装 Prompt
    String userMessage = getAiTestScoringUserMessage(app, questionContent, choices);
    // AI 生成
    String result = aiManager.doSyncStableRequest(AI_TEST_SCORING_SYSTEM_MESSAGE, userMessage);
    // 结果处理
    int start = result.indexOf("{");
    int end = result.lastIndexOf("}");
    String json = result.substring(start, end + 1);
    // 3. 构造返回值，填充答案对象的属性
    UserAnswer userAnswer = JSONUtil.toBean(json, UserAnswer.class);
    userAnswer.setAppId(appId);
    userAnswer.setAppType(app.getAppType());
    userAnswer.setScoringStrategy(app.getScoringStrategy());
    userAnswer.setChoices(JSONUtil.toJsonStr(choices));
    return userAnswer;
}

```



## 第六期 性能优化

### 0.RxJava 响应式编程

**背景：**

之前 AI 生成相关的功能是等所有内容全部生成后，再返回给前端，用户可能要等待较长的时间。

仔细阅读智谱 AI 的官方文档，提供了 **流式** 接口调用方式：

**流式数据（Streaming Data）是一种数据传输方式，它允许数据连续、实时地从一个地方传送到另一个地方，而不需要等待整个数据集的下载完成。在流式数据传输中，数据被分成小的、连续的块或包进行传输，这些块在传输过程中可以被实时处理和分析。**

由于调用流式接口返回的是 Flowable对象，而Flowable对象又是 RxJava 响应式编程库中定义的类

#### 什么是响应式编程？

响应式编程（Reactive Programming）是一种编程范式，它专注于 **异步数据流** 和 **变化传播**。

#### RxJava

`RxJava` 是一个基于事件驱动的、利用可观测序列来实现异步编程的类库，是响应式编程在 Java 语言上的实现。

##### 1、事件驱动

在 RxJava 中，事件可以被看作是数据流中的数据项，称为“事件流”或“数据流”。每当一个事件发生，这个事件就会被推送给那些对它感兴趣的观察者（Observers）。

##### 2、可观测序列

可观测序列是指一系列按照时间顺序发出的数据项，可以被观察和处理。可观测序列提供了一种将数据流和异步事件建模为一系列可以订阅和操作的事件的方式。

可以理解为在数据流的基础上封装了一层，多加了一点方法。

应用场景：UI场景，像 Android 开发都会用到 RxJava。

而 RxJava 给予我们一个统一的异步接口形式，提供链式编程、丰富的操作符让我们在面对复杂的业务场景编写的代码也异常简单。

##### 核心知识点：

**观察者模式**

RxJava 是基于 **观察者模式** 实现的，分别有观察者和被观察者两个角色，被观察者会实时传输数据流，观察者可以观测到这些数据流。

基于传输和观察的过程，用户可以通过一些操作方法对数据进行转换或其他处理。

在 RxJava 中，观察者就是 Observer，被观察者是 Observable 和 Flowable。

##### 常用操作符

前面提到用户可以通过一些方法对数据进行转换或其他处理，RxJava 提供了很多操作符供我们使用，这块其实和 Java8 的 Stream 类似，概念上都是一样的。

操作符主要可以分为以下几大类：

1）变换类操作符，对数据流进行变换，如 map、flatMap 等。

比如利用 map 将 int 类型转为 string

```java
Flowable<String> flowable = Flowable.range(0, Integer.MAX_VALUE)
        .map(i -> String.valueOf(i))
```

2）聚合类操作符，对数据流进行聚合，如 toList、toMap 等。

将数据转成一个 list

```java
Flowable.range(0, Integer.MAX_VALUE).toList()
```

3）过滤操作符，过滤或者跳过一些数据，如 filter、skip 等。

将大于 10 的数据转成一个 list

```java
Flowable.range(0, Integer.MAX_VALUE).filter(i -> i > 10).toList();
```

4）连接操作符，将多个数据流连接到一起，如 concat、zip 等。

创建两个 Flowable，通过 concat 连接得到一个被观察者，进行统一处理

```java
// 创建两个 Flowable 对象
Flowable<String> flowable1 = Flowable.just("A", "B", "C");
Flowable<String> flowable2 = Flowable.just("D", "E", "F");

// 使用 concat 操作符将两个 Flowable 合并
Flowable<String> flowable = Flowable.concat(flowable1, flowable2);
```

5）排序操作符，对数据流内的数据进行排序，如 sorted

```java
Flowable<String> flowable = Flowable.concat(flowable1, flowable2).sorted();
```

##### 事件

RxJava 也是一个基于事件驱动的框架，我们来看看一共有哪些事件，分别在什么时候触发：

1）onNext，被观察者每发送一次数据，就会触发此事件。

2）onError，如果发送数据过程中产生意料之外的错误，那么被观察者可以发送此事件。

3）onComplete，如果没有发生错误，那么被观察者在最后一次调用 onNext 之后发送此事件表示完成数据传输。

### 1.AI生成题目优化

#### 需求分析

原先 AI 生成题目的场景响应较慢，如果题目数过多，容易产生请求超时；并且界面上没有响应，用户体验不佳。

需要 **流式化改造** AI 生成题目接口，一道一道地实时返回已生成题目给前端，而不是让前端请求一直阻塞等待，最后一起返回，提升用户体验且避免请求超时。

首先智谱 AI 为我们提供了流式响应的支持，数据已经可以一点一点地返回给后端了，那么我们要思考的问题是如何让后端接收到的一点一点的内容实时返回给前端？

#### 前后端实时通讯方案：

几种主流的实现方案：

1）轮询（前端主动去要）

前端间隔一定时间就调用后端提供的结果接口，比如 200ms 一次，后端处理一些结果就累加放置在缓存中。

2）SSE（后端推送给前端）

前端发送请求并和后端建立连接后，后端可以实时推送数据给前端，无需前端自主轮询。

3）WebSocket

全双工协议，前端能实时推送数据给后端（或者从后端缓存拿数据），后端也可以实时推送数据给前端。

#### SSE技术：

服务器发送事件（Server-Sent Events）是一种用于从服务器到客户端的 **单向、实时** 数据传输技术，基于 HTTP协议实现。

它有几个重要的特点：

1. 单向通信：SSE 只支持服务器向客户端的单向通信，客户端不能向服务器发送数据。

2. 文本格式：SSE 使用 **纯文本格式** 传输数据，使用 HTTP 响应的 `text/event-stream` MIME 类型。

3. 保持连接：SSE 通过保持一个持久的 HTTP 连接，实现服务器向客户端推送更新，而不需要客户端频繁轮询。

4. 自动重连：如果连接中断，浏览器会自动尝试重新连接，确保数据流的连续性。

   **应用场景**

   由于现代浏览器普遍支持 SSE，所以它的应用场景非常广泛，AI 对话就是 SSE 的一个典型的应用场景。

   再举一些例子：

   1. 实时更新：股票价格、体育比赛比分、新闻更新等需要实时推送的应用。
   2. 日志监控：实时监控服务器日志或应用状态。
   3. 通知系统：向客户端推送系统通知或消息。

**方案对比**

熟悉了 SSE 技术后，对比上述前后端实时通讯方案。

1）主动轮询其实是一种伪实时，比如每 3 秒轮询请求一次，结果后端在 0.1 秒就返回了数据，还要再等 2.9 秒，存在延迟。

2）WebSocket 和 SSE 虽然都能实现服务端推送，但 Websocket 会更复杂些，且是二进制协议，调试不方便。AI 对话只需要服务器单向推送即可，不需要使用双向通信，所以选择文本格式的 SSE。

**最终方案**

回归到本项目，具体实现方案如下：

1）前端向后端发送普通 HTTP 请求

2）后端创建 SSE 连接对象，为后续的推送做准备

3）后端流式调用智谱 AI，获取到数据流，使用 RxJava 订阅数据流

4）以 SSE 的方式响应前端，至此接口主流程已执行完成

5）异步：基于 RxJava 实时获取到智谱 AI 的数据，并持续将数据拼接为字符串，当拼接出一道完整题目时，通过 SSE 推送给前端。

6）前端每获取一道题目，立刻插入到表单项中

### 2.AI 答题优化

对答题结果使用缓存,使用caffeine本地缓存

为了防止缓存击穿,使用锁来保护,抢到锁然后保存缓存后面的请求就可以利用缓存了

### 3.分库分表

sharding JDBC 已实现

### 4.app搜索优化

根据app描述来搜索!!

添加ES来实现

**步骤**:

1.建立es表()

2.建立EsDao(),自己写方法名,具体代码自动生成,(适合简单查询)

复杂查询用Spring提供的操作es的客户端对象

### 5.AppIcon实现上传功能

后端使用minio实现upload功能

前端使用uploader组件时，需要有回调函数，我们先得等图片上传完成，然后file作为参数传到后端执行upload函数

## 第七期 系统优化

### 1.幂等性

查看用户答案的时候可以连续点击几次,为了防止连续调用AI可以使用幂等性来保存

1.数据库唯一索引

创建`唯一索引ID`即可

2.分布式锁

使用Redisson来实现

### 2.线程池隔离

## 总结：

### 1.SpringBoot

是一个简化Spring程序开发的框架，减少了Spring应用程序的配置和开发复杂性，是我们能够更快的构建，测试和部署Spring应用

SpringBoot的主要特点：

1.简化配置：通过自动配置（@EnableAutoConfiguration），根据项目中的类路径依赖和环境变量等自动为应用配置适当的Spring模块，避免大量的XML配置

2.内置服务器：SpringBoot内置了Tomcat，Jetty，Undertow等服务器，应用程序可以直接通过Java -jar 方式启动，而不需要部署到外部的Web服务器

3.快速开发：提供了开箱即用的项目结构和依赖管理

4.独立运行：打包之后可以直接通过命令行运行，简化了部署流程

**核心注解：**

**@SpringBootApplicatioin** 是以下三个注解的组合

@Configuration：表示该类是 Spring配置类。
@EnableAutoConfiguration：启用Spring Boot的自动配置功能。
@ComponentScan：自动扫描当前包及其子包中带有 Spring注解的类(如@Controller、@Service等）。

**@EnableAutoConfiguration** 自动配置核心注解，他会根据类路径中的依赖自动配置Spring应用中的各种组件

比如：如果应用中引I入了spring-boot-starter-web依赖，SpringBoot会自动为应用配置嵌入式Tomcat服务器、MVC 框架等。

#### 启动器（Starters）

启动器是预定义的依赖包的集合，涵盖了常用的Spring和第三方库，通过引入一个简单的启动器依赖，开发者可以快速整合需要的功能

![image-20250404232437631](C:\Users\16247\AppData\Roaming\Typora\typora-user-images\image-20250404232437631.png)

### 2.Mysql

为什么使用？低成本，开放源代码，易于维护，支持多种语言！

在这个项目中使用Mysql主要是存储数据嘛，在ai答题平台主要有用户表，应用表，题目表，用户答案表，评分表 五个表。

在题目表中题目使用JSON来存储的！

### 3.Redis

在这个项目中做了什么？

1.使用Redisson分布式限流器RateLimiter对用户调用AI的频率进行控制

也可以使用Redis + Lua脚本来实现

2.使用分布式锁对缓存失效进行唯一控制，只需要一个线程查数据放到缓存中即可

分布式锁就是解决分布式环境下单机锁无法覆盖的跨节点互斥问题！

场景：电商秒杀时，多个服务器实例可能处理同一商品的库存扣减，需确保同一时刻只有一个实例能操作库存

实现方式：通过Redis，ZooKeeper等中间件提供全局可见的锁状态

**单机环境使用分布式锁：**

1.性能损耗：

分布式锁依赖网络通信，相比本地锁延迟更高。例如：

Redis单节点锁的P99 延迟约1ms，而本地锁延迟仅0.1us

2.功能冗余！

**分布式锁应用场景：**

1.多节点部署

2.跨服务器协作

3.高并发控制

![image-20250405230100165](C:\Users\16247\AppData\Roaming\Typora\typora-user-images\image-20250405230100165.png)

**zookeeper实现分布式锁：**

```json
zab：为了保证分布式一致性，zk 实现了 zab（Zk Atomic Broadcast，zk 原子广播）协议，在 zab 协议下，zk集群分为 Leader 节点及  Follower 节点，其中，负责处理写请求的 Leader 节点在集群中是唯一的，多个 Follower 则负责同步 Leader 节点的数据，处理客户端的读请求。同时，zk 处理写请求时底层数据存储使用的是 ConcurrentHashMap，以保证并发安全；
```

```
临时顺序节点：zk 的数据呈树状结构，树上的每一个节点为一个基本数据单元，称为 Znode。zk 可以创建一类临时顺序

（EPHEMERAL_SEQUENTIAL）节点，在满足一定条件时会可以自动释放；同时，同一层级的节点名称会按节点的创建顺序进行命名，第一个节点为xxx-0000000000，第二个节点则为xxx-0000000001，以此类推；
```

```
session：zk 的服务端与客户端使用 session 机制进行通信，简单来说即是通过长连接来进行交互，zk 服务端会通过心跳来监控客户端是否处于活动状态。若客户端长期无心跳或断开连接，则 zk 服务端会定期关闭这些 session，主动断开与客户端的通信。
```

