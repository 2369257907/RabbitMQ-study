package utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;

/**
 * RabbitMQ连接工具类
 *
 * @author guoheng
 * @date 2021/11/24
 */
public class rabbitMQUtills {

    public static Connection getConnection(){
        try{
            //创建mq的连接工厂对象
            ConnectionFactory connectionFactory = new ConnectionFactory();
            //设置连接的RabbitMQ主机
            connectionFactory.setHost("127.0.0.1");
            //设置端口号,需要注意端口号是5672而非15672
            connectionFactory.setPort(5672);
            //设置连接的虚拟主机
            connectionFactory.setVirtualHost("/guo");
            //设置访问虚拟主机的用户名和密码
            connectionFactory.setUsername("user");
            connectionFactory.setPassword("123");

            //创建并返回连接对象
            return connectionFactory.newConnection();
        }catch (Exception e){
                e.printStackTrace();
        }
            return null;
    }

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
}
