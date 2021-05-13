package Chat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static Chat.ConsoleHelper.readInt;

public class Server {
    static private Map<String, Chat.Connection> connectionMap = new ConcurrentHashMap();     //Карта подключенных пользвателей,ключ - имя клиента,
                                                                                    // значение - соединение с ним
    public static void sendBroadcastMessage(Chat.Message message){
        for (Chat.Connection connection: connectionMap.values()
        ) {
            try {
                connection.send(message);
            } catch (Exception e) {
                System.out.println("Не удалось отправить сообщение");
            }
        }
    }

    public static void main(String[] args){

        try (ServerSocket server = new ServerSocket(readInt()))
        {
            System.out.println("Сервер запущен");

            while (true){
                Socket socket = server.accept();
                Handler newThread = new Handler(socket);
                newThread.start();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

   private static class Handler extends Thread{
       private Socket socket;

       public Handler(Socket socket) {
           this.socket = socket;
       }

       private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message message;
          while (true){
              connection.send(new Message(MessageType.NAME_REQUEST," Введите имя:"));   //запрашиваем имя клиента
               message = connection.receive();      //получаем сообщение
               String username = message.getData();
               if (message.getType()==MessageType.USER_NAME && !message.getData().isEmpty() //проверяем полученное сообщение
                       && !username.equals("")
                       && !connectionMap.containsKey(username)){
                   connectionMap.put(username,connection);          //если такого пользователя нет, добавляем в карту
                 break;
               }
           }
           connection.send(new Message(MessageType.NAME_ACCEPTED, " Вы добавлены в чат. Добро пожаловать!"));
                   return message.getData();

       }
       private void notifyUsers(Connection connection, String userName) throws IOException{
           for (Map.Entry<String, Connection> entry : connectionMap.entrySet())
           {
               String name = entry.getKey();
               if (!name.equals(userName)){
                   connection.send(new Message(MessageType.USER_ADDED, name));
               }
           }
       }
       private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
           while (true){
             Message message = connection.receive();
               if (message.getType() == MessageType.TEXT) {
                   String messageText = userName + ":" + " " + message.getData();
                   Message messageNew = new Message(MessageType.TEXT, messageText);
                   sendBroadcastMessage(messageNew);
               }
               else ConsoleHelper.writeMessage("Нe правильный формат сообщения!");
           }
       }
      public void run() {
          ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом " + socket.getRemoteSocketAddress());
          try (Connection connect = new Connection(socket)) {
              //serverHandshake(connect);
              String userName =  serverHandshake(connect);
              sendBroadcastMessage(new Message (MessageType.USER_ADDED, userName));
              notifyUsers(connect, userName);
              serverMainLoop(connect, userName);
              for (Map.Entry<String, Connection> entry : connectionMap.entrySet())
              {
                  String name = entry.getKey();
                  if (name.equals(userName)){
                      connectionMap.remove(name);
                      sendBroadcastMessage(new Message (MessageType.USER_REMOVED, userName));
                  }
              }
          }
          catch (IOException | ClassNotFoundException e){
              ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
          }
       }
   }


}
