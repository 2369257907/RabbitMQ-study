import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TestConcentration {

    @Test
    public void Test() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        //设置连接的RabbitMQ主机
        connectionFactory.setHost("127.0.0.1");
        //设置端口号,注意端口号是5672而非15672
        connectionFactory.setPort(5672);
        //设置连接的虚拟主机
        connectionFactory.setVirtualHost("/guo");
        //设置访问虚拟主机的用户名和密码
        connectionFactory.setUsername("user");
        connectionFactory.setPassword("123");

        Connection connection = connectionFactory.newConnection();
        System.out.println(connection);

    }
}
