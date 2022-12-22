package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.service.NotificationService;

import java.util.List;
@Service
@Slf4j
public class ScheduledTasks {
    private final TelegramBot telegramBot;

    private final NotificationService notificationService;

    public ScheduledTasks(TelegramBot telegramBot, NotificationService notificationService) {
        this.telegramBot = telegramBot;
        this.notificationService = notificationService;
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
