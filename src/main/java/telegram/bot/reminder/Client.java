package telegram.bot.reminder;

import telegram.bot.reminder.enums.Stage;

import java.util.Date;
import java.util.HashMap;

public class Client {

    private Stage stage;
    private HashMap<Date,String> Reminders = new HashMap<>();
    private long chatId;

    public Client(long chatId) {
        this.chatId = chatId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public HashMap<Date, String> getReminders() {
        return Reminders;
    }

    public void setReminders(HashMap<Date, String> reminders) {
        Reminders = reminders;
    }

    public void putToReminders(Date date, String reminder) {
        Reminders.put(date, reminder);
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public boolean hasDate(Date date) {
        if (Reminders.keySet().contains(date)) {
            return true;
        } else {
            return false;
        }
    }
}
