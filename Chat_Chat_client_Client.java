package Chat.client;

import Chat.Connection;
import Chat.ConsoleHelper;
import Chat.Message;
import Chat.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected;

    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }

    protected String getServerAddress(){
        System.out.println("Введите адрес сервера:");
       return ConsoleHelper.readString();
    }

   protected int getServerPort(){
       System.out.println("Введите порт сервера:");
      return ConsoleHelper.readInt();
   }

   protected String getUserName(){
       System.out.println("Введите имя:");
       return ConsoleHelper.readString();
   }

   protected boolean shouldSendTextFromConsole(){
        return true;
   }

   protected SocketThread getSocketThread(){
       SocketThread socket = new SocketThread();
       return socket;
   }

   protected void sendTextMessage(String text){
       try {
           connection.send(new Message(MessageType.TEXT, text));
       } catch (IOException e) {
           System.out.println("Произошла ошибка!");
           clientConnected = false;
       }
   }

    public void run(){
       SocketThread socket = getSocketThread();
       socket.setDaemon(true);
       socket.start();
        try {
            synchronized (this) {
                this.wait();
            } } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("При работе клиента возникла ошибка");
        }

        if (clientConnected){
            System.out.println("Соединение установлено. Для выхода наберите команду ‘exit’.");}
        else {
                 System.out.println("Произошла ошибка во время работы клиента.");}

        String str = "";

        while(clientConnected){
            str = ConsoleHelper.readString();
            if(shouldSendTextFromConsole()) {
                // отправь считанный текст с помощью метода sendTextMessage()
                sendTextMessage(str);
            }
            if (str.equals("exit"))
                break;
        }
    }

    /*Отвечает за сокетное соединение и читает сообщения от сервера*/
    public class SocketThread extends Thread{
       protected void processIncomingMessage(String message){
           System.out.println(message);
       }

       protected void informAboutAddingNewUser(String userName){
           System.out.println("Участник " + userName + " присоединился к чату.");
       }

        protected void informAboutDeletingNewUser(String userName){
            System.out.println("Участник " + userName + " покинул чат.");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected){
           Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            Message message;
            while (true) {
                try {
                    message = connection.receive();
                } catch (Exception e) {
                    break;
                }
                if (message != null) {
                    if (message.getType() == MessageType.NAME_REQUEST) {
                        connection.send(new Message(MessageType.USER_NAME, getUserName()));
                    } else {
                        if (message.getType() == MessageType.NAME_ACCEPTED) {
                            notifyConnectionStatusChanged(true);
                            return;
                        } else {
                            throw new IOException("Unexpected MessageType");
                        }
                    }
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            Message message;

            while (true) {
                try {
                    message = connection.receive();
                } catch (Exception e) {
                    break;
                }
                if (message != null) {
                    if (message.getType() == MessageType.TEXT) {
                        processIncomingMessage(message.getData());
                    } else {
                        if (message.getType() == MessageType.USER_REMOVED) {
                            informAboutDeletingNewUser(message.getData());
                        } else {
                            if (message.getType() == MessageType.USER_ADDED) {
                                informAboutAddingNewUser(message.getData());
                            } else {
                                connection.close();
                                throw new IOException("Unexpected MessageType");
                            }
                        }
                    }
                }
            }
        }
       public void run(){
          String host =  getServerAddress();
          int port = getServerPort();
           try {
               Socket socket= new Socket(host, port);
              connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
           } catch (IOException | ClassNotFoundException e) {
               e.printStackTrace();
               notifyConnectionStatusChanged(false);
           }

       }
    }
}
