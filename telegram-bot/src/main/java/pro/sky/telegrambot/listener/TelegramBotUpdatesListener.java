package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.service.NotificationService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class TelegramBotUpdatesListener implements UpdatesListener {
    static final String HELP_TEXT = "Telegram-bot for remind about your homework.\n" + "The format is 01.01.2022 20:00 You have to do homework";
   // private final NotificationTaskRepository notificationTaskRepository;  //проверить

    private final TelegramBot telegramBot;
    private final NotificationService notificationService;

    public TelegramBotUpdatesListener( TelegramBot telegramBot, NotificationService notificationService) {

        this.telegramBot = telegramBot;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            log.info("Processing update: {}", update);
            String messageText = update.message().text();
            long chatId = update.message().chat().id();

            // check users message for compliance from list of commands

            switch (messageText) {
                case "/start":
                    notificationService.registerUser(update.message());
                    notificationService.startCommandReceived(chatId, update.message().chat().firstName());
                    break;
                case "/help":
                    notificationService.sendMessage(chatId, HELP_TEXT);
                    break;
                case "/deletemessage":
                    //  deleteMessage(chatId);
                    break;

                default:
                    notificationService.sendMessage(chatId, "Sorry, command wasn't recognized");

            }


        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    // раз в минуту выполнятеся
    public int notifies() {
        // сначала проеряет имеются ли в БД напоминания на эту минуту - вернет список напоминаний,
        // если такие есть
        List<NotificationTask> notificationsList = notificationService.checkCurrentNotifications();
        // если список непустой - вызывается метод makeNotification, который возвращает List<SendMessage>
        // который несет id чата (кому это сообщение нужно отправить) и текст сообщения,
        // который нужно отправить
        if (!notificationsList.isEmpty()) {
            notificationService.makeNotification(notificationsList)
                    .forEach(n -> {
                        // передаю SendMessage в .execute() - метод = сообщение отправлено в нужный чат
                        SendResponse response = telegramBot.execute(n);
                        System.out.println(response.isOk());
                        System.out.println(response.errorCode());
                    });
            log.info("выполнился метод notifies");
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


}
