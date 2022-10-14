package com.spdb.fdev.fdevfootprint.spdb.netty.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.fdevfootprint.base.dict.Constants;
import com.spdb.fdev.fdevfootprint.base.dict.Dict;
import com.spdb.fdev.fdevfootprint.base.utils.CommonUtil;
import com.spdb.fdev.fdevfootprint.spdb.entity.SwitchModel;
import com.spdb.fdev.fdevfootprint.spdb.service.SwitchFlagQueryService;
import com.spdb.fdev.fdevfootprint.spdb.task.Save2Kafka;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Sharable
@RefreshScope
@Service(Dict.HTTP_SERVER_INBOUND_HANDLER)
public class HttpServerInboundHandler extends ChannelInboundHandlerAdapter {
    protected Logger logger = LoggerFactory.getLogger(HttpServerInboundHandler.class);

    @Autowired
    private SwitchFlagQueryService switchFlagQuery;

    @Autowired
    private Save2Kafka task;

    @Resource(name = Dict.KAFKA_PRODUCER_EXECUTOR)
    private ThreadPoolTaskExecutor executor;

    @Value("${footprint.white.sites}")
    private List<String> whiteList;

    @Value("${kafka.fdev.topic}")
    private String fdevTopic;

    @Value("${kafka.ftms.topic}")
    private String ftmsTopic;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        HttpRequest request = null;
        Boolean fdevChannel = false;
        Boolean ftmsChannel = false;

        if (msg instanceof HttpRequest) {
            request = (FullHttpRequest) msg;

            logger.info("parameters:{}\nreferer:{}", new QueryStringDecoder(request.getUri()).parameters(),
                    request.headers().get(Dict.REFERER));

            // 日志中打印User-Agent信息
            String userAgent = request.headers().get(Constants.USER_AGENT);
            MDC.put("User_Agent", "UserAgent:" + userAgent);

            // 日志中打印IP
            String ip = CommonUtil.getIpAddr(request, ctx.channel().remoteAddress());
            MDC.put("IP", "IP:" + ip);

            String jsonMsg = "";
            String channel = "";
            String masterId = "";
            // 是否放开请求
            boolean open = false;
            // 是否为自动采集
            boolean auto = false;
            if (request.getMethod().equals(HttpMethod.POST)) {
                logger.info("http method : POST");
                ByteBuf jsonBuf = ((FullHttpRequest) request).content();

                jsonMsg = jsonBuf.toString(CharsetUtil.UTF_8);
                jsonBuf.release();
                JSONObject json = JSON.parseObject(jsonMsg);
                // 判断渠道
                channel = json.getString(Dict.CHANNEL);
                if (Dict.FDEV.equals(channel) || Dict.FDEV_NEW.equals(channel)) {
                    fdevChannel = true;
                } else if (Dict.FTMS.equals(channel)) {
                    ftmsChannel = true;
                }
                if (json != null) {
                    // 将上送的json字符串转换为map，给time重新赋值
                    Map<String, Object> map = JSONObject.parseObject(jsonMsg);
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (key.equals(Dict.TIME)) {
                            map.put(key, CommonUtil.getCurrentTime());
                        } else if (key.equals(Dict.USER)) {
                            Map<String, String> user = (Map<String, String>) value;
                            masterId = user.get(Dict.MASTER_ID);
                        } else if (key.equals(Dict.CLIENT)) {
                            Map<String, String> client = (Map<String, String>) value;
                            client.put(Dict.IP, ip);
                            map.put(key, client);
                        } else if (key.equals(Dict.EVENT_CONTEXT)) {
                            Map<String, String> eventContext = (Map<String, String>) value;
                            if (eventContext != null && eventContext.containsKey(Dict.AUTO_FLAG)) {
                                auto = true;
                            }
                        }
                    }

                    // 将给time重新赋值后重新给采点json信息赋值
                    jsonMsg = JSON.toJSONString(map).toString();
                    // 根据渠道获取对应的足迹采集开关状态̬
                    SwitchModel switchModel = switchFlagQuery.getSwitchFlag(channel);
                    if (auto) {
                        if (CommonUtil.checkAutoSwitchViaIP(ip, switchModel)) {
                            open = true;
                        }
                    } else {
                        if (CommonUtil.checkHandSwitchViaIP(ip, switchModel)) {
                            open = true;
                        }
                    }
                    if (!open) {
                        jsonMsg = "";
                    }
                }
            } else if (request.getMethod().equals(HttpMethod.GET)) {
                logger.info("http method : GET");
                QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
                Map<String, List<String>> parameters = decoder.parameters();
                // 获取渠道号
                List<String> channelPara = parameters.get(Dict.CHANNEL);
                if (channelPara != null && channelPara.size() > 0) {
                    channel = channelPara.get(0);
                }
                logger.info("channel:{}", channel);
                if (Dict.FDEV.equals(channel) || Dict.FDEV_NEW.equals(channel)) {
                    fdevChannel = true;
                } else if (Dict.FTMS.equals(channel)) {
                    ftmsChannel = true;
                }
                // 根据渠道号获取对应的足迹采集开关状态̬
                SwitchModel switchModel = switchFlagQuery.getSwitchFlag(channel);
                // 渠道对应的开关状态为开时，开始封装请求数据并上传至Kafka
                if (switchModel != null && Integer.parseInt(switchModel.getStatus()) > 0) {
                    String referer = request.headers().get(Dict.REFERER);
                    // 判断请求来源是否在白名单内
                    if (CommonUtil.checkRefererInWhiteList(referer, whiteList)) {
                        Map<String, Object> jsonMap = new HashMap<>();
                        Map<String, String> browseMap = new HashMap<>();
                        Map<String, String> userMap = new HashMap<>();
                        Map<String, String> clientMap = new HashMap<>();
                        Map<String, String> positionMap = new HashMap<>();
                        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue().get(0);
                            if (key.contains(Dict.BROWSE)) {
                                browseMap.put(key, value);
                            } else if (key.equals(Dict.MASTER_ID)) {
                                userMap.put(key, value);
                                masterId = value;
                            } else if (key.equals(Dict.USER_MARK)) {
                                userMap.put(key, value);
                            } else if (key.equals(Dict.OS)) {
                                clientMap.put(key, value);
                            } else if (key.equals(Dict.DATA)) {
                                Map dataMap = (Map) JSON.parse(value);
                                if (dataMap != null && dataMap.containsKey(Dict.AUTO_FLAG)) {
                                    auto = true;
                                }
                                jsonMap.put(Dict.EVENT_CONTEXT, dataMap);
                            } else if (key.equals(Dict.TIME)) {
                                jsonMap.put(key, CommonUtil.getCurrentTime());
                            } else if (!key.equals(Dict.RANDOM) && !key.equals(Dict.T)) {
                                jsonMap.put(key, value);
                            }
                        }
                        jsonMap.put(Dict.BROWSER, browseMap);
                        jsonMap.put(Dict.USER, userMap);

                        // 兼容手机端模型
                        jsonMap.put(Dict.VIEW, "");
                        jsonMap.put(Dict.EVENT_OBJECT, "");

                        clientMap.put(Dict.IP, ip);
                        clientMap.put(Dict.MAC, "");
                        clientMap.put(Dict.IMEI, "");

                        jsonMap.put(Dict.CLIENT, clientMap);

                        // 采集插件无法获取经纬度，手动补全字段
                        positionMap.put(Dict.LONGITUDE, "");
                        positionMap.put(Dict.LATITUDE, "");
                        jsonMap.put(Dict.POSITION, positionMap);

                        jsonMsg = JSON.toJSONString(jsonMap).toString();
                    }
                }

                if (auto) {//请求中携带auto_flag
                    if (CommonUtil.checkAutoSwitchViaIP(ip, switchModel)) {
                        open = true;
                    }
                } else {
                    if (CommonUtil.checkHandSwitchViaIP(ip, switchModel)) {
                        open = true;
                    }
                }
                if (!open) {
                    jsonMsg = "";
                }
            }

            MDC.put("CHANNEL", "CHANNEL:" + channel);
            MDC.put("MID", "MID:" + masterId);

            logger.info("track,len = " + jsonMsg.getBytes(CharsetUtil.UTF_8).length + ",AUTO=" + auto);
            logger.debug(jsonMsg + ",AUTO=" + auto);
            if (!jsonMsg.isEmpty()) {
                if (fdevChannel) {
                    task.setTopicName(fdevTopic);
                } else if (ftmsChannel) {
                    task.setTopicName(ftmsTopic);
                }
                task.setMessage(jsonMsg);
                if (null != task.getTopicName()) {
                    executor.execute(task);
                }
            }
        }

        if (msg instanceof HttpContent) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
            response.headers().set(CONTENT_TYPE, Constants.TEXT_PLAIN);
            if (HttpHeaders.isKeepAlive(request)) {
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                response.headers().set(CONNECTION, Values.KEEP_ALIVE);
                ctx.writeAndFlush(response);
            } else {
                response.headers().set(CONNECTION, Values.CLOSE);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
