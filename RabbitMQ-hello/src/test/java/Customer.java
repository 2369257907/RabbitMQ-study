import com.rabbitmq.client.*;
import org.junit.jupiter.api.Test;
import utils.rabbitMQUtills;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Customer {

    @Test
    public void test() throws IOException, TimeoutException {

        //获取rabbitMQUtills工具类创建的连接对象
        Connection connection = rabbitMQUtills.getConnection();
        //创建通道
        Channel channel = connection.createChannel();

        //通道绑定对象
        channel.queueDeclare("hello",true,false,false,null);


        //消费消息
        //参数1：消费队列的名称
        //参数2：开始消息时的自动确认机制
        //参数3：消费时的回调接口
        channel.basicConsume("hello",true,new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("new String(body) = " + new String(body));
            }
        });

        rabbitMQUtills.closeConnectionAndChannel(channel,connection);
    }
}
