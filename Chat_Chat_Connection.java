package Chat;
/*класс соединения между клиентом и сервером*/

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection implements Closeable {

    final private Socket socket;
    final private ObjectOutputStream out;
    final private ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }
   public void send (Message message) throws IOException {      //отправляем сообщение
      synchronized (out) {
          out.writeObject(message);
      }
   }
   public Message receive() throws IOException, ClassNotFoundException{     //получаем сообщение
       Message message;
        synchronized (in) {
         message = (Message) in.readObject();
}
        return message;
    }

   public SocketAddress getRemoteSocketAddress(){
        return socket.getRemoteSocketAddress();
   }

   public void close() throws IOException{
socket.close();
out.close();
in.close();
   }
}
