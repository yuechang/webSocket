package com.yc.chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;

/**
 * 聊天服务器类
 * 
 * @author yuechang
 *
 */
@ServerEndpoint("/websocket")
public class ChatServer {

	private Session session;
	private static final Map<ChatServer, String> connections = new ConcurrentHashMap<ChatServer, String>();

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // 日期格式化

	/**
	 * @category 添加初始化操作
	 * @param session
	 */
	@OnOpen
	public void open(Session session) {

		// 开启会话，将session存入map中，不过此时的nikename存入的为空字符串
		this.session = session;
		connections.put(this, "");
	}

	/**
	 * @category 接受客户端的消息，并把消息发送给所有连接的会话
	 * @param message 客户端发来的消息
	 * @param session 客户端的会话
	 */
	@OnMessage
	public void getMessage(String message, Session session) {

		// 把客户端的消息解析为JSON对象
		JSONObject jsonObject = JSONObject.fromObject(message);
		// 获得昵称
		String nikename = (String) jsonObject.get("nickname");

		connections.put(this, nikename);
		broadcast(message);
	}

	/**
	 * @category 添加关闭会话时的操作
	 * @param reason
	 */
	@OnClose
	public void close(CloseReason reason) {

		String nikename = connections.get(this);
		// 下线时，得从总人数中移除，否则信息公布时找不到下线的session报错的
		connections.remove(this);
		// 如果这个人有在聊天室中发过言，则向聊天室中发送nikename已下线消息
		if (StringUtils.isNotBlank(nikename)) {
			String msg = "{'content':'<p>用户[ ".concat(nikename).concat(" ]下线了！<br/></p>','nickname':'系统消息'}");
			broadcast(msg);// 这是告知所还在线聊天的人下线了
		}
	}

	/**
	 * @category 添加处理错误的操作
	 * @param t
	 */
	@OnError
	public void error(Throwable t) {
		// 添加处理错误的操作
	}

	/**
	 * @category 广播消息
	 * @param msg 消息JSON串
	 */
	private void broadcast(String msg) {

		Iterator<ChatServer> iterator = connections.keySet().iterator();
		while (iterator.hasNext()) {
			ChatServer client = iterator.next();
			synchronized (client) {

				// 把客户端的消息解析为JSON对象
				JSONObject jsonObject = JSONObject.fromObject(msg);
				// 在消息中添加发送日期
				jsonObject.put("date", DATE_FORMAT.format(new Date()));

				// 添加本条消息是否为当前会话本身发的标志
				jsonObject.put("isSelf", client.session.equals(session));
				// 发送JSON格式的消息
				client.session.getAsyncRemote().sendText(jsonObject.toString());
			}
		}
	}
}