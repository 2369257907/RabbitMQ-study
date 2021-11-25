
1.什么是Rabbitmq
  MQ全称为Message Queue, 消息队列（MQ）是一种应用程序对应用程序的通信方法。应用程序通过读写出入队列的消息（针对应用程序的数据）来通信，而无需专用连接来链接它们。消息传递指的是程序之间通过在消息中发送数据进行通信，而不是通过直接调用彼此来通信，直接调用通常是用于诸如远程过程调用的技术。排队指的是应用程序通过 队列来通信。队列的使用除去了接收和发送应用程序同时执行的要求。
2.快速开始
  由于 Rabbitmq 安装时需要 erlang 等环境，并且需要保持版本对应，初学者安装时常常会遇到一些问题。如果使用docker安装则可以很好的避免这些问题，快速启动Rabbitmq。

使用 docker安装只需要以下几条命令。

2.1下载镜像
docker pull rabbitmq:3-management
2.2启动
docker run -d --hostname my-rabbit --name rabbit -p 15672:15672 -p 5672:5672 rabbitmq:3-management
● -d 后台运行
● -name 指定RabbitMQ名称
● -p 映射端口 （如a:b，表示将容器端口b映射到宿主机端口a上）
  这样启动就可以通过 https://ip:15672 访问登陆页面，默认的用户名和密码是guest。

  或者我们可以像以下这样设置用户名密码。这样用户名设置为 user，密码为123 。
docker run -d --hostname my-rabbit --name rabbit -e RABBITMQ_DEFAULT_USER=user -e RABBITMQ_DEFAULT_PASS=123 -p 15672:15672 -p 5672:5672 rabbitmq:3-management
● RABBITMQ_DEFAULT_USER 指定用户账号
● RABBITMQ_DEFAULT_PASS 指定用户密码

  如果防火墙没有启动端口，则访问不了登陆页面。使用以下方式启动 15672 端口。如果使用腾讯云或者阿里云服务器，还需要开放对应安全组。
sudo firewall-cmd --permanent --zone=public --add-port=15672/tcp
systemctl reload firewalld
  经过上述操作，可以正常访问以下页面。使用默认的 guest/guset 账号密码或者自己设置的账号密码登录即可。
  如果您在使用默认的 guest/guset 进行远程登陆管理页面时，出现问题，请不要担心。这是因为guest具有所有操作权限，并且是默认的账号，出于安全考虑，该账号默认只能在localhost中登录。使用自定义的账号、密码登陆即可。

3.Hello RabbitMQ
3.1创建项目
  
首先创建一个 Maven项目 ，导入以下依赖。
    <dependency>
        <groupId>com.rabbitmq</groupId>
        <artifactId>amqp-client</artifactId>
        <version>5.14.0</version>
    </dependency>

    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <scope>test</scope>
    </dependency>
3.2 创建RabbitMQ连接
  在连接 RabbitMQ 前，我们需要了解虚拟主机的概念。每一个RabbitMQ服务器都能创建虚拟的消息服务器，我们称之为虚拟主机（virtual host)，简称vhost.每一个vhost是一个独立的小型服务器，拥有自己独立的队列，交换器等。它拥有自己独立的权限。我们可以把它理解成类似于 MYSQL 中的数据库（DataBase）的概念。我们要在RabbitMQ中创建一个虚拟主机，才能与它进行连接。
  
  登陆进入 RabbitMQ 管理页面，上方导航选择 Admin ，右侧导航选择 Virtual Hosts ，即可看见创建虚拟主机的页面。在 Name 中输入要创建的虚拟主机的名称，一般虚拟主机名以"/"开头 ，点击 Add Virtual Hosts 即可创建虚拟主机。本教程中创建了 "/guo" 虚拟主机。

  在创建了虚拟主机后，我们就可以在项目中测试连接。本教程中整体的项目结构如下。

首先在Test包中创建 TestConnection测试类，如下。测试类的作用是用于测试是否与 RabbitMQ 队列正常连接。
public class TestConcentration {
    @Test
    public void Test() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        
      	//更换为自己的ip
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/guo");
        connectionFactory.setUsername("user");
        connectionFactory.setPassword("123");

        Connection connection = connectionFactory.newConnection();
        System.out.println(connection);
    }
}
● ConnectionFactory ：连接工厂
● setHost：设置连接的RabbitMQ主机
● setPort：设置端口号,注意端口号是5672而非15672
● setVirtualHost：设置连接的虚拟主机
● setUsername、setPassword：设置访问虚拟主机的用户名和密码

  运行测试，控制台打印出连接信息，说明连接成功。

3.3构建数据库连接工具类
  由于在使用 RabbitMQ时都需要连接操作，如果不使用工具类的话，会造成代码的冗余。因此，我们可以对上面的代码稍作修改，构建一个工具类。这里创建了一个名为 rabbitMQUtills工具类，该类中有 getConnection 和 closeConnectionAndChannel 两个静态方法。分别用于创建并返回连接对象和关闭连接和通道。

getConnection 类：
  用于创建与RabbitMQ的连接。和上面测试连接的代码大致是相同的，加了个异常处理。
    public static Connection getConnection(){
        try{
            
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("127.0.0.1");
            connectionFactory.setPort(5672);
            connectionFactory.setVirtualHost("/guo");
            connectionFactory.setUsername("user");
            connectionFactory.setPassword("123");

            //创建并返回连接对象
            return connectionFactory.newConnection();
        }catch (Exception e){
                e.printStackTrace();
        }
            return null;
    }


closeConnectionAndChannel 类：
    //关闭通道和连接的方法
    public static void closeConnectionAndChannel(Channel channel ,Connection connection){
        try{
            if (channel!=null){
                channel.close();
            }
            if (connection !=null){
                connection.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
3.4创建生产者程序
  所谓生产者程序就是往消息队列中添加数据的程序。
  这里创建了首先通过工具类获取连接对象。再通过连接对象调用创建连接通道方法，通过连接通道发布消息。
public class Provider {

    @Test
    public void testSendMessage() throws IOException, TimeoutException {

        //获取rabbitMQUtills工具类创建的连接对象
        Connection connection = rabbitMQUtills.getConnection();

        Channel channel = connection.createChannel();
		
        channel.queueDeclare("hello",true,false,false,null);


        //发布消息
        //参数1： 交换机名称 参数2：队列名称 参数3：传递消息额外设置(这里设置了消息持久化) 参数4：消息的具体内容
        channel.basicPublish("","hello", MessageProperties.PERSISTENT_TEXT_PLAIN,"hello RabbitMQ".getBytes());
		//关闭连接
        rabbitMQUtills.closeConnectionAndChannel(channel,connection);
    }
    
}

● createChannel: 创建连接通道
● queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String,Object> arguments) throws IOException：声明队列
参数：
1. queue - 队列的名称
2. durable  - 如果为 true 则表示创建持久化队列(服务器重启，队列依然存在)
3. exclusive  - 如果为 true 则表示创建独占队列（仅限于在本次连接中使用，断开连接后自动删除）
4. autoDelete - 如果为 true 则表示创建一个会自动删除的队列（服务关闭或所有消费者断连时删除队列）
5. arguments  - 队列的其它属性（构造参数）
● basicPublish(String exchange, String routingKey, BasicProperties props, byte[] body) throws IOException：发布消息
1. exchange - 交换机名称
2. routingKey - 直译做 "路由键" ，理解为使用的消息队列的名称
3. props - 传递消息额外设置 （详见官方文档）
● closeConnectionAndChannel：关闭连接通道和连接
3.5创建消费者程序
  所谓消费者程序就是从消息队列中获取消息的程序。
  这里创建了首先通过工具类获取连接对象。再通过连接对象调用创建连接通道方法，最后通过连接通道获取消息。
public class Customer {

    @Test
    public void test() throws IOException, TimeoutException {
        //获取rabbitMQUtills工具类创建的连接对象
        Connection connection = rabbitMQUtills.getConnection();
        Channel channel = connection.createChannel();
        
        //通道绑定对象
        channel.queueDeclare("hello",true,false,false,null);
        //消费消息
        channel.basicConsume("hello",true,new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("new String(body) = " + new String(body));
            }
        });
		//关闭连接
        rabbitMQUtills.closeConnectionAndChannel(channel,connection);
    }
}

●  basicConsume(QUEUE_NAME, true, new DefaultConsumer(channel){});使用接口com.rabbitmq.client.Consumer的实现类com.rabbitmq.client.DefaultConsumer实现自定义消息监听器，接口中有多个不同的方法可以根据自己系统的需要实现；
3.6启动并测试程序

  3.6.1启动生产者程序
  首先，我们启动 Provider 生产者测试类。之后，进入管理页面。可以看到 Queue 中有一个名为 "hello" 的队列，并且队列中存在一条消息。而且 Features 里有一个蓝色背景的 "D" ，这是 "Durable"（持久化）的缩写，表示服务器重启时该队列依然存在。

  这样，我们就往消息队列中加入了一条消息，下面我们尝试从队列中读取这条消息。

3.6.2启动消费者
   我们首先启动 Customer 消费者测试类。之后可以看到控制台输出。这样就获取了我们在生产者程序中创建并放入消息队列的信息。

   再进入控制台，可以看到消息的数量为0。这说明消息队列中的数据确实被消费了。

  到这里，我们就完成了一个简单的RabbitMQ程序。
  4.总结
  本文介绍了什么是RabbitMQ以及如何安装并快速启动RAbbitMQ。

源码：GitHub

参考：
● 【编程不良人】MQ消息中间件
● Docker安装rabbitmq 原
● RabbitMQ官方文档

