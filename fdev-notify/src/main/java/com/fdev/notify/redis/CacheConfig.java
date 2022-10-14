package com.fdev.notify.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@SuppressWarnings({"all"})
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

        @Autowired
        ChannelListener channelListener;
        @Bean
        RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                MessageListenerAdapter ChannelAdapter) {
            RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            //订阅了一个叫 sendNotify 的通道
            container.addMessageListener(ChannelAdapter, new PatternTopic("sendNotify"));
            //订阅了一个叫 sendMessage 的通道
            container.addMessageListener(ChannelAdapter, new PatternTopic("sendMessage"));
            //订阅了一个叫 sendAnnounceRealTime 的通道
            container.addMessageListener(ChannelAdapter, new PatternTopic("sendAnnounceRealTime"));
            return container;
        }

    /**
     * 消息监听器适配器，绑定消息处理器
     *
     * @param receiver
     * @return
     */
    @Bean
    MessageListenerAdapter ChannelAdapter() {

        return new MessageListenerAdapter(channelListener);
    }

}
