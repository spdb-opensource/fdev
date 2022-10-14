package com.fdev.notify.websocket;

import com.fdev.notify.dict.Dict;
import com.fdev.notify.entity.Message;
import com.fdev.notify.entity.Notify;
import com.fdev.notify.service.MessageService;
import com.fdev.notify.util.SpringUtil;
import com.spdb.fdev.common.util.Util;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;


@Component
@ServerEndpoint("/websocket/{sid}/{stime}")
public class WebSocketServer {
    private static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);// 日志打印

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocketServer> userWebSocketSet = new CopyOnWriteArraySet<WebSocketServer>();
    private static CopyOnWriteArraySet<WebSocketServer> taskWebSocketSet = new CopyOnWriteArraySet<WebSocketServer>();
    private static CopyOnWriteArraySet<WebSocketServer> appWebSocketSet = new CopyOnWriteArraySet<WebSocketServer>();
    private static CopyOnWriteArraySet<WebSocketServer> newAppWebSocketSet = new CopyOnWriteArraySet<WebSocketServer>();
    private static CopyOnWriteArraySet<WebSocketServer> releaseWebSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //接收sid
    private String sid = "";

    //接收stime
    private String stime = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid, @PathParam("stime") String stime) throws Exception {
        this.session = session;
        this.sid = sid;
        this.stime = stime;
        String[] split = sid.split("-");
        switch (split[0]) {
            case Dict.FUSER:
                userWebSocketSet.add(this);
                MessageService messageService = SpringUtil.getBean(MessageService.class);
                List<Message> announces = messageService.queryNewAnnounce();
                if (!Util.isNullOrEmpty(announces)) {
                    JSONArray jsonArray = JSONArray.fromObject(announces);
                    this.sendMessage(jsonArray.toString());
                }
                List<Message> newMessages = messageService.queryNewMessage();
                if (!Util.isNullOrEmpty(newMessages)) {
                    JSONArray jsonArray = JSONArray.fromObject(newMessages);
                    this.sendMessage(jsonArray.toString());
                }
                break;
            case Dict.FTASK:
                taskWebSocketSet.add(this);
                break;
            case Dict.FAPP:
                appWebSocketSet.add(this);
                break;
            case Dict.FSERVICE:
                newAppWebSocketSet.add(this);
                break;
            case Dict.FRELEASE:
                releaseWebSocketSet.add(this);
                break;
            default:
                this.session.close();
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() throws Exception {
        String[] split = this.sid.split("-");
        switch (split[0]) {
            case Dict.FUSER:
                userWebSocketSet.remove(this);
                this.session.close();
                break;
            case Dict.FTASK:
                taskWebSocketSet.remove(this);
                this.session.close();
                break;
            case Dict.FAPP:
                appWebSocketSet.remove(this);
                this.session.close();
                break;
            case Dict.FSERVICE:
                newAppWebSocketSet.remove(this);
                this.session.close();
                break;
            case Dict.FRELEASE:
                releaseWebSocketSet.remove(this);
                this.session.close();
                break;
            default:
                this.session.close();
        }
    }


    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("创建websocket连接失败！", error);
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    private static List<WebSocketServer> getWebSocketSet(String sid) {
        List<WebSocketServer> result = new ArrayList<>();
        String[] split = sid.split("-");
        logger.info("fservice,split[0]:"+split[0]);
        switch (split[0]) {
            case Dict.FUSER:
                for (WebSocketServer item : userWebSocketSet) {
                    result.add(item);
                }
                break;
            case Dict.FTASK:
                for (WebSocketServer item : taskWebSocketSet) {
                    result.add(item);
                }
                break;
            case Dict.FAPP:
                for (WebSocketServer item : appWebSocketSet) {
                    result.add(item);
                }
                break;
            case Dict.FSERVICE:
                logger.info("fservice,newAppWebSocketSet:"+newAppWebSocketSet+"--size:"+newAppWebSocketSet.size());
                for (WebSocketServer item : newAppWebSocketSet) {
                    logger.info("fservice,"+item.sid+"--"+item.stime);
                    result.add(item);
                }
                break;
            case Dict.FRELEASE:
                for (WebSocketServer item : releaseWebSocketSet) {
                    result.add(item);
                }
                break;
        }
        return result;
    }

    /**
     * 发送通知Message
     *
     * @param message
     * @throws IOException
     */
    public static boolean sendMessage(Message message) {
        String sid = message.getTarget();
        List<WebSocketServer> webSocketServers = getWebSocketSet(sid);
        WebSocketServer webSocketServer = null;
        boolean flag = false;
        for (int i = 0; i < webSocketServers.size(); i++) {
            webSocketServer = webSocketServers.get(i);
            if (!Util.isNullOrEmpty(webSocketServer) && webSocketServer.sid.equals(sid)) {
                try {
                    webSocketServer.sendMessage(message.toString());
                    flag = true;
                } catch (IOException e) {
                    logger.warn("发送消息至 " + webSocketServer.sid + "失败！");
                }
            }
        }
        return flag;
    }

    /**
     * 发送通知Message
     *
     * @param notify
     * @throws IOException
     */
    public static boolean sendNotify(Notify notify) {
        String sid = notify.getTarget();
        List<WebSocketServer> webSocketServers = getWebSocketSet(sid);
        WebSocketServer webSocketServer = null;
        boolean flag = false;
        for (int i = 0; i < webSocketServers.size(); i++) {
            webSocketServer = webSocketServers.get(i);
            if (!Util.isNullOrEmpty(webSocketServer) && webSocketServer.sid.equals(sid)) {
                try {
                    webSocketServer.sendMessage(notify.toString());
                    flag = true;
                } catch (IOException e) {
                    logger.warn("发送通知至 " + webSocketServer.sid + "失败！");
                }
            }
        }
        return flag;
    }

    /**
     * 实时发送系统公告
     *
     * @param message
     * @throws IOException
     */
    public static void sendAnnounceRealTime(Message message) {
        for (WebSocketServer item : userWebSocketSet) {
            try {
                item.sendMessage(message.toString());
            } catch (IOException e) {
                logger.warn("发送公告至 " + item.sid + "失败！");
            }
        }
    }
}
