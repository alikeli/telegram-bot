package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;

import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeDefault;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TelegramBotUpdatesListener implements UpdatesListener {
    static final String HELP_TEXT = "Telegram-bot for remind about your homework.\n" + "The format is 01.01.2022 20:00 You have to do homework";
    private final NotificationTaskRepository notificationTaskRepository;  //проверить

    private final TelegramBot telegramBot;//проверить
    private final NotificationService notificationService;

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot, NotificationService notificationService) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
        this.notificationService = notificationService;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/deletemessage", "delete my message"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));

//        try {
//            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
//        } catch (TelegramApiException e) {
//            log.error("Error setting bot's command list :" + e.getMessage());
//        }

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


//    private void sendMessage(NotificationTask notificationTask) {
//        sendMessage(notificationTask.getChatId(), notificationTask.getMessage());
//    }


//    @Scheduled(cron = "0 0/1 * * * *")
//    //repeat once a minute
//    public int notifies() {
//        // сначала проверяет имеются ли в БД напоминания на эту минуту - вернет список напоминаний,
//        // если такие есть
//        List<NotificationTask> notificationsTaskList = notificationService.checkCurrentNotification();
//        // если список непустой - вызывается метод makeNotification, который возвращает List<SendMessage>
//        // который несет id чата (кому это сообщение нужно отправить) и текст сообщения,
//        // который нужно отправить
//        if (!notificationList.isEmpty()) {
//            notificationService.makeNotification(notificationsList)
//                    .forEach(n -> {
//                        // передаю SendMessage в .execute() - метод = сообщение отправлено в нужный чат
//                        SendResponse response = telegramBot.execute(n);
//                        System.out.println(response.isOk());
//                        System.out.println(response.errorCode());
//                    });
//            log.info("выполнился метод notifies");
//        }
//        return UpdatesListener.CONFIRMED_UPDATES_ALL;
//    }

    @Scheduled(cron = "0 43 17 * * *")
    // метод удаляет устаревшие напоминания из БД
    public void deleteOldNotifications() {
        notificationService.deleteOldNotification();
        log.info("выполнился метод deleteOldNotifications - устаревшие напоминания удалены");
    }


}
