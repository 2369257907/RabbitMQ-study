import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import org.junit.jupiter.api.Test;
import utils.rabbitMQUtills;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Provider {

    @Test
    public void testSendMessage() throws IOException, TimeoutException {

        //获取rabbitMQUtills工具类创建的连接对象
        Connection connection = rabbitMQUtills.getConnection();
        //通道绑定对象
        Channel channel = connection.createChannel();


        //声明消息队列
        //参数1：队列名称 如果队列不存在自动创建
        //参数2：用来自定义队列特性是否要持久化 true 持久化队列 false 不持久化
        //参数3：exclusive 是否独占独立队列 true 独占队列 false 不独占
        //参数4：autoDelete 是否在消费完后自动删除队列 true 自动删除 false 不自动删除
        //参数5：额外附加参数
        channel.queueDeclare("hello",true,false,false,null);


        //发布消息
        //参数1： 交换机名称 参数2：队列名称 参数3：传递消息额外设置(这里设置了消息持久化) 参数4：消息的具体内容
        channel.basicPublish("","hello", MessageProperties.PERSISTENT_TEXT_PLAIN,"hello RabbitMQ".getBytes());

        rabbitMQUtills.closeConnectionAndChannel(channel,connection);
    }

}
