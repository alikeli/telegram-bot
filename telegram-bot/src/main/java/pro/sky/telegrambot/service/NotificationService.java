package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
//import pro.sky.telegrambot.exeption.TextPatternDoesNotMatchException;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;


    public NotificationService(TelegramBot telegramBot, NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;

    }

    private final Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([a-zA-Z0-9\\W+]+)");

    //response to /start command
    public void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name + "! Nice to meet you!" + " I'm waiting for format like this: " + " DD.MM.YYYY HH:MM TEXT_NOTIFICATION";

        log.info("Replied to user " + name);
        sendMessage(chatId, answer);

    }


    // send message
    public void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage(chatId, textToSend);
        telegramBot.execute(message);

    }

    // save reminder to db
    public NotificationTask saveNotificationToDB(Update update)
    //  throws DateTimeParseException, TextPatternDoesNotMatchException
    {
        NotificationTask notificationTask = parsMessage(update);
        LocalDateTime current = LocalDateTime.now();

        if (notificationTask != null) {
            // check that the date is no later than now
            boolean timeIsCorrect = notificationTask.getDateToSend().isAfter(current);
            if (notificationIsUnique(notificationTask) && timeIsCorrect) {
                log.info("выполнился метод saveNotificationToDB, сохранили {}", notificationTask);
                return notificationTaskRepository.save(notificationTask);
            } else {
                return null;
            }
        }
        return null;
    }

    //check the same reminder in the database
    public boolean notificationIsUnique(NotificationTask notificationTask) {
        if (notificationTaskRepository.findAllByChatId(notificationTask.getChatId()).contains(notificationTask)) {
            return false;
        }
        return true;
    }

    // the list of notification by chatId

    public List<SendMessage> makeNotification(List<NotificationTask> notificationsList) {
        List<SendMessage> sendMessageList = notificationsList.stream().map(n -> new SendMessage(n.getChatId(), n.getMessage())).collect(Collectors.toList());
        log.info("выполнился метод makeNotification");
        return sendMessageList;
    }

    //the list of all notification
    public List<NotificationTask> getListOfAllNotification(Update update) {
        long chatId = update.message().chat().id();
        LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        log.info(" метод getListOfAllNotification выполнился");
        return notificationTaskRepository.findAllByChatId(chatId).stream().filter(n -> Objects.equals(n.getChatId(), chatId)).filter(n -> n.getDateToSend().truncatedTo(ChronoUnit.DAYS).equals(today)).collect(Collectors.toList());
    }


    // send message about save message

    public SendMessage giveReport(Update update) {
        long idChat = update.message().chat().id();
        SendMessage message = new SendMessage(idChat, "Notification added");
        SendMessage negativeMessage = new SendMessage(idChat, "Notification didn't add. \nWrong format. \n Notification already exists or" + "\n check your time");
        if (saveNotificationToDB(update) != null) {
            log.info("метод giveReport выполнен");
            return message;
        } else {
            log.info("отправка сообщения об ошибке");
            return negativeMessage;
        }
    }

    // extract date and text from message
    public NotificationTask parsMessage(Update update) {
        NotificationTask notificationTask = new NotificationTask();
        String messageText = update.message().text();
        //use chatId for saving to db
        Long messageId = update.message().messageId().longValue();
        Long chatId = update.message().chat().id();


        Matcher matcher = pattern.matcher(messageText);
        // check format of message
        if (matcher.matches()) {
            String date = matcher.group(1);


            notificationTask.setDateToSend(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            String message = matcher.group(3);
            notificationTask.setMessage(message);
            notificationTask.setChatId(chatId);
            notificationTask.setUserName(update.message().chat().username());
            return notificationTask;
        }
        log.info("parsMessage успешно выполнен");
        return null;

    }

    // checking reminders at the moment
    public List<NotificationTask> checkCurrentNotifications() {

        LocalDateTime currentMoment = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        // get reminders and filter by date and time

        return notificationTaskRepository.findByDateToSendEquals(currentMoment);

    }


    public void deleteNotifications(Update update) {
        long idChat = update.message().chat().id();

       notificationTaskRepository.deleteNotificationTasksByChatId(idChat);




    }


}
