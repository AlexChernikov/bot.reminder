package telegram.bot.reminder;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ReplicatedMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.bot.reminder.enums.Stage;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BotReminder extends TelegramLongPollingBot {

    private static ExecutorService executorService = Executors.newWorkStealingPool();

    public void sendMessage (Message message) {

        Client client = checkStageOnChatId(message.getChatId());

        switch (message.getText()) {
            case "Создать задачу":{
                client.setStage(Stage.CREATE);
                break;
            }
            case "Просмотреть все задачи":{
                client.setStage(Stage.WATCH);
                break;
            }
            case "Редактировать задачи":{
                client.setStage(Stage.DELETE);
                break;
            }
            case "Отмена":{
                client.setStage(Stage.WAIT);
                break;
            }
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(AnswersCreator.getMessage(message, client));

        synchronized (chatIdAndWorkStage) {
            chatIdAndWorkStage.put(message.getChatId(), client);
        }

        try {
            createButtons(sendMessage);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Client checkStageOnChatId(Long chatId) {
        synchronized (chatIdAndWorkStage) {
            if (chatIdAndWorkStage.containsKey(chatId)) {
                return chatIdAndWorkStage.get(chatId);
            } else {
                Client client = new Client(chatId);
                client.setStage(Stage.WAIT);
                System.out.println("POINT1");
                //chatIdAndWorkStage.put(chatId, client);
                putClient(chatId, client);
                System.out.println("POINT2");
                return client;
            }
        }
    }

    void createButtons (SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setResizeKeyboard(true); //Авто-масшабирование.
        replyKeyboardMarkup.setSelective(true); //Видна всем пользователям.
        replyKeyboardMarkup.setOneTimeKeyboard(false); //Чтоб клавиатура не скрывалась.

        List<String> buttons = List.of(
                "Создать задачу",
                "Просмотреть все задачи",
                "Редактировать задачи",
                "Отмена");
        ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();

        for (String button : buttons) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(new KeyboardButton(button));
            keyboardRows.add(keyboardRow);
        }

        replyKeyboardMarkup.setKeyboard(keyboardRows);
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if ((message != null) && (message.hasText())) {
            executorService.submit(() -> {
                sendMessage(message);
            });
        }
    }

    @Override
    public String getBotUsername() {
        return "StankinReminderBot";
    }

    @Override
    public String getBotToken() {
        return "1069727783:AAHWiu6g8ogvkCW2EtdMzKvZQf_qeA-Gu-c";
    }

    final HazelcastInstance hazelcastInstance;
    public static ReplicatedMap<Long, Client> chatIdAndWorkStage;
    public BotReminder(@Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.chatIdAndWorkStage = hazelcastInstance.getReplicatedMap("chatIdAndWorkStage");
    }

    private void putClient(Long chatId, Client client) {
        chatIdAndWorkStage.put(chatId, client);
        //hazelcastInstance.<Long, Client>getTopic("productAdd").publish(chatId, client);
    }
}
