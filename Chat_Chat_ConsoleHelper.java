package Chat;
/*вспомогательный класс, для чтения или записи в консоль.*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
   private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    static void writeMessage(String message) {
        System.out.println(message);
    }

   public static String readString(){
        String message = "";
       boolean stroka = true;

       while (stroka) {
           try {
               message = reader.readLine();
               stroka = false;
           } catch (IOException e) {
               System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
               stroka = true;
           }
       }
return message;
    }

   public static int readInt(){
       int number =0;
        try {
            number = Integer.parseInt(readString());

        } catch (NumberFormatException e) {
            System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            number = readInt();
        }
            return number;
   }
}
