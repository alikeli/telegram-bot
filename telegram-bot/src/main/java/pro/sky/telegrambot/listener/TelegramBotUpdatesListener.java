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
    private static final String HELP_TEXT = "Telegram-bot for remind about your homework.\n" + "The format is 01.01.2022 20:00 You have to do homework";
    private static final String DELETE_TEXT = "Tasks deleted";
    // private final NotificationTaskRepository notificationTaskRepository;  //проверить

    private final TelegramBot telegramBot;
    private final NotificationService notificationService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationService notificationService) {

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
                    // notificationService.greetingUser(update);
                    notificationService.startCommandReceived(chatId, update.message().chat().firstName());
                    break;
                case "/help":
                    notificationService.sendMessage(chatId, HELP_TEXT);
                    break;
                case "/tasks":
                    List<NotificationTask> notificationsList2 = notificationService.getListOfAllNotification(update);
                    notificationService.makeNotification(notificationsList2).forEach(n -> {
                        SendResponse response = telegramBot.execute(n);
                        log.info("ответ успешно отправлен, {}", response.isOk());
                        log.info("Код ошибки {}", response.errorCode());
                    });
                    break;
                case "/deletemessage":
                    notificationService.deleteNotifications(update);
                    notificationService.sendMessage(chatId, DELETE_TEXT);
                    break;

                default:
                    telegramBot.execute(notificationService.giveReport(update));

            }


        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    // once a minute
    public int notifies() {

        List<NotificationTask> notificationsList = notificationService.checkCurrentNotifications();

        if (!notificationsList.isEmpty()) {
            notificationService.makeNotification(notificationsList).forEach(n -> {

                SendResponse response = telegramBot.execute(n);
                System.out.println(response.isOk());
                System.out.println(response.errorCode());
            });
            log.info("выполнился метод notifies");
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


}
