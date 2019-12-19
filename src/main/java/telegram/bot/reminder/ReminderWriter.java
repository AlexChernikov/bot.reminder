package telegram.bot.reminder;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static telegram.bot.reminder.Application.chatIdAndWorkStage;
import static telegram.bot.reminder.Application.hazelcastInstance;

@Service
public class ReminderWriter {
    BotReminder botReminder = new BotReminder(hazelcastInstance);
    private static ExecutorService executorService = Executors.newWorkStealingPool();

    public ReminderWriter() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (chatIdAndWorkStage) {
                    for (Client client : chatIdAndWorkStage.values()) {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                        Date dateNow = null;
                        try {
                            dateNow = format.parse(format.format(new Date()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        for (Date date : client.getReminders().keySet()) {
                            if ((date.equals(dateNow)) || (date.before(dateNow))) {
                                executorService.submit(() -> {
                                    SendMessage sendMessage = new SendMessage();
                                    sendMessage.enableMarkdown(true);
                                    sendMessage.setChatId(client.getChatId());
                                    sendMessage.setText(client.getReminders().get(date));
                                    synchronized (client.getReminders()) {
                                        client.getReminders().remove(date);
                                    }
                                    try {
                                        botReminder.execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }).start();
    }
}
