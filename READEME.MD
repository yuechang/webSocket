参考：
1、https://github.com/shiyanlou/java_websocket_chat.git
2、apache-tomcat-7.0.63\webapps\examples\WEB-INF\classes\websocket\chat\ChatAnnotation.java

增加：
1、在会话打开时，将对应的session添加到对应map中；
2、当发送消息时，遍历整个map，然后对每个session发送消息；
3、关闭时，将对应的session从map中删除，不再接收消息,并且如果此用户有发送过消息，系统将提示该用户已下线