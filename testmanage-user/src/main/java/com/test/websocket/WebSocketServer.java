package com.test.websocket;

import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.test.dict.Dict;
import com.test.entity.Message;
import com.test.entity.Notify;
import com.test.service.MessageService;
import com.test.utils.MyUtils;
import com.test.utils.SpringUtil;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{sid}")
public class WebSocketServer {
	private static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);// 日志打印

	// concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
	private static CopyOnWriteArraySet<WebSocketServer> userWebSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

	// 与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;

	// 接收sid
	private String sid = "";

	/**
	 * 连接建立成功调用的方法
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("sid") String sid) throws Exception {
		this.session = session;
        this.sid = sid;
        String[] split = sid.split("-");
        switch (split[0]) {
            case Dict.TUSER:
            	userWebSocketSet.add(this);
        		MessageService messageService = SpringUtil.getBean(MessageService.class);
        		List<Message> message = messageService.queryNewAnnounce();
        		if (!MyUtils.isEmpty(message)) {
        			JSONArray jsonArray = JSONArray.fromObject(message);
        			this.sendMessage(jsonArray.toString());
        		}
        		 List<Message> newMessages = messageService.queryNewMessage();
                 if (!MyUtils.isEmpty(newMessages)) {
                     JSONArray jsonArray = JSONArray.fromObject(newMessages);
                     this.sendMessage(jsonArray.toString());
                 }
        }
	}


	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose() throws Exception {
		userWebSocketSet.remove(this);
		this.session.close();
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

	private static CopyOnWriteArraySet getWebSocketSet(String sid) {
		CopyOnWriteArraySet<WebSocketServer> webSocketServers = new CopyOnWriteArraySet<WebSocketServer>();
		webSocketServers = userWebSocketSet;
		return webSocketServers;
	}
	
	 private static Map<String, WebSocketServer> getWebSocket(String sid) {
	        HashMap<String, WebSocketServer> result = new HashMap<>();
	        String[] split = sid.split("-");
	        switch (split[0]) {
	            case Dict.TUSER:
	                for (WebSocketServer item : userWebSocketSet) {
	                    result.put(item.sid, item);
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
		Map<String, WebSocketServer> webSocketServers = getWebSocket(sid);
        WebSocketServer webSocketServer = webSocketServers.get(sid);
        if (!MyUtils.isEmpty(webSocketServer)) {
            try {
                webSocketServer.sendMessage(message.toString());
                return true;
            } catch (IOException e) {
                logger.warn("发送消息至 " + webSocketServer.sid + "失败！");
                return false;
            }
        }
        return false;
	}

	/**
	 * 发送通知Message
	 *
	 * @param notify
	 * @throws IOException
	 */
	public static boolean sendNotify(Notify notify) {
		String sid = notify.getTarget();
		CopyOnWriteArraySet<WebSocketServer> webSocketServers = getWebSocketSet(sid);
		for (WebSocketServer item : webSocketServers) {
			if (item.sid.equals(sid)) {
				try {
					item.sendMessage(notify.toString());
				} catch (IOException e) {
					logger.warn("发送通知至 " + item.sid + "失败！");
				}
				return true;
			}
		}
		return false;
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
