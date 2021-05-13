package Chat.client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BotClient extends Chat.client.Client {

    public static void main(String[] args){
        BotClient bot = new BotClient();
        bot.run();
    }
    @Override
    protected String getUserName() {
        int a = 0;
        int X = a + (int) (Math.random() * 100);
        return "date_bot_" + X;
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            System.out.println(message);
            if (message.contains(":")) {
           String name = message.replaceAll("\\:.+", "").trim();
                   String text = message.replaceAll(".+\\:", "").trim();
                SimpleDateFormat formatForDateNow =null;

                if (text.equals("дата")){
                     formatForDateNow = new SimpleDateFormat("d.MM.YYYY");
                   }
           else if (text.equals("день")){
              formatForDateNow = new SimpleDateFormat("d");
            }
           else if (text.equals("месяц")){
                formatForDateNow = new SimpleDateFormat("MMMM");
            }
                else if (text.equals("год")){
               formatForDateNow = new SimpleDateFormat("YYYY");
            }
                else if (text.equals("время")){
                 formatForDateNow = new SimpleDateFormat("H:mm:ss");
            }
                else  if (text.equals("час")){
                formatForDateNow = new SimpleDateFormat("H");
            }
                else  if (text.equals("минуты")){
               formatForDateNow = new SimpleDateFormat("m");
            }
                else  if (text.equals("секунды")){ formatForDateNow = new SimpleDateFormat("s");

            }
               if (formatForDateNow != null) {
                   Calendar calendar = new GregorianCalendar();
                   Date date = calendar.getTime();
                   sendTextMessage("Информация для " + name + ":" + " " + formatForDateNow.format(date));
               }
        }

        }
    }
}
