package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NotificationService {

    private TelegramBot telegramBot;
    private final NotificationTaskRepository notificationsRepository;


    public NotificationService(TelegramBot telegramBot, NotificationTaskRepository notificationsRepository) {
        this.telegramBot = telegramBot;
        this.notificationsRepository = notificationsRepository;
    }
    public void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name + "! Nice to meet you!";
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);

    }

   public void registerUser(Message message) {
        if (notificationsRepository.findById(message.chat().id()).isEmpty()) {
            var chatId = message.chat().id();
            var chat = message.chat();
            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setChatId(Math.toIntExact(chatId));
            notificationTask.setUserName(chat.username());
            notificationTask.setMessage(String.valueOf(chat.pinnedMessage()));  //непонятно
            notificationTask.setDateToSend(LocalDateTime.now());
            notificationsRepository.save(notificationTask);
            log.info("message saved : " + chat.username());
        }
    }

    public void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage(chatId, textToSend);
        telegramBot.execute(message);

    }

}
