package telegram.bot.reminder;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.telegram.telegrambots.meta.api.objects.Message;
import telegram.bot.reminder.enums.Stage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public final class AnswersCreator {

    public static String getMessage(Message message, Client client) {
        switch (client.getStage()) {
            case WAIT :{
                return  ("Для начала работы Вы можете воспользоваться одной из дефолтных команд.");
            }
            case CREATE:{
                client.setStage(Stage.WRITE);
                return ("Для создания новой задачи введите её время в формате \"yyyy.MM.dd HH:mm\" и её текст с новой строки. \nНапример:\n2019.12.31 23:59\nВстретить Новый год.");
            }
            case WRITE:{
                String[] strings = message.getText().split("\n");
                SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                try {
                    Date date = format.parse (strings[0]);
                    String reminder = new String();
                    for (String string : strings) {
                        reminder = reminder + string + "\n";
                    }
                    client.putToReminders(date, reminder);
                    client.setStage(Stage.WAIT);
                    return "Ваша задача успешно создана!";
                } catch (ParseException e) {
                    e.printStackTrace();
                    return "Неправильный формат даты! Попробуйте ещё раз!";
                }
            }
            case WATCH:{
                if (!client.getReminders().isEmpty()) {
                    String answer = "Ваши текущие задачи:\n\n";
                    for (String value : client.getReminders().values()) {
                        answer = answer + value + "\n";
                    }
                    client.setStage(Stage.WAIT);
                    return answer;
                } else {
                    client.setStage(Stage.WAIT);
                    return "У вас пока нет задач. Чтобы создать задачу наберите \"Создать задачу\"";
                }
            }
            case DELETE:{
                if (!client.getReminders().isEmpty()) {
                    String answer = "Ваши текущие задачи:\n\n";
                    for (String value : client.getReminders().values()) {
                        answer = answer + value + "\n";
                    }
                    client.setStage(Stage.CONFIRM_DELETE);
                    return answer + "\nВведите дату задачи, которую хотите удалить или используйте \"Отмена\"";
                } else {
                    client.setStage(Stage.WAIT);
                    return "У вас пока нет задач. Чтобы создать задачу наберите \"Создать задачу\"";
                }

            }
            case CONFIRM_DELETE:{
                SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");

                String dateString = format.format(new Date());
                try {
                    Date date = format.parse (message.getText());
                    if (client.hasDate(date)) {
                        client.getReminders().remove(date);
                        client.setStage(Stage.WAIT);
                        return "Ваша задача успешно удалена!";
                    } else {
                        return "Задач на данное число не имеется!";
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    return "Неправильный формат даты! Попробуйте ещё раз!";
                }
            }
            default:{
                return ("Я ничего не понимаю! ><");
            }
        }
    }
}
