package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    private TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;



    public NotificationService(TelegramBot telegramBot, NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;

    }

    public void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name + "! Nice to meet you!" +
                " I'm waiting for format like this: " +
                " DD.MM.YYYY HH:MM TEXT_NOTIFICATION";

        log.info("Replied to user " + name);
        sendMessage(chatId, answer);

    }

    public void registerUser(Message message) {
        if (notificationTaskRepository.findById(message.chat().id()).isEmpty()) {
            var chatId = message.chat().id();
            var chat = message.chat();
            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setChatId(Math.toIntExact(chatId)); //разобраться
            notificationTask.setUserName(chat.username());
            notificationTask.setMessage(String.valueOf(chat.pinnedMessage()));  //непонятно
            notificationTask.setDateToSend(LocalDateTime.now());
            notificationTaskRepository.save(notificationTask);
            log.info("message saved : " + chat.username());
        }
    }

    public void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage(chatId, textToSend);
        telegramBot.execute(message);

    }
    // сохраняю напоминание в БД
    public NotificationTask saveNotificationToDB(Update update) {
        NotificationTask notifications = parsMessage(update);
        LocalDateTime current = LocalDateTime.now();
        // проверяю, что вернулся не null
        if (notifications != null) {
            // так проверяю задана ли дата корректно = не указано ли прошедшее время.
            boolean timeIsCorrect = notifications.getDateToSend().isAfter(current);
            //  и уникальное напоминание
            if (notificationIsUnique(notification) && timeIsCorrect) {
                log.info("выполнился метод saveNotificationToDB, сохранили {}", notifications);
                return notificationTaskRepository.save(notifications);
            } else {
                return null;
            }
        }
        return null;
    }

    // проверяет на наличие точно такого же напоминания в БД
    public Boolean notificationIsUnique(NotificationTask notificationTask) {
        if (notificationTaskRepository.findAll().contains(notificationTask)) {
            return false;
        }
        return true;
    }

    // из списка напоминаний достает ID чата и сообщение, формирую сущность SendMessage,
    // и собирает их в список
    public List<SendMessage> makeNotification(List<NotificationTask> notificationsList) {
        List<SendMessage> sendMessageList = notificationsList
                .stream()
                .map(n ->
                        new SendMessage(n.getChatId(), n.getMessage())
                )
                .collect(Collectors.toList());
        log.info("выполнился метод makeNotification");
        return sendMessageList;
    }

    // отправляет в чат отчет добавлено напоминание или нет
    public SendMessage giveReport(Update update) {
        Long idChat = update.message().chat().id();
        SendMessage message = new SendMessage(idChat, "Notification added");
        SendMessage negativeMessage = new SendMessage(idChat, "Notification didn't add. \nWrong format or \nNotification already exists or" +
                "\nPast time");
        if (saveNotificationToDB(update) != null) {
            log.info("метод giveReport выполняется успешно");
            return message;
        } else {
            log.info("метод giveReport завершается провалом");
            return negativeMessage;
        }
    }

    // парсю текс полученного сообещния, вычленяю дату, время и текст напоминания
    public NotificationTask parsMessage(Update update) {
        NotificationTask notificationTask = new NotificationTask();
        String messageText = update.message().text();
        //использую ID сообщения из чата для сохранения в БД
        Long messageId = update.message().messageId().longValue();
        Long chatId = update.message().chat().id();



        Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([a-zA-Z0-9\\W+]+)");
        Matcher matcher = pattern.matcher(messageText);
        // если формат верный, вставляю полученные значения в поля нового Напоминиая созданного выше
        if (matcher.matches()) {
            String date = matcher.group(1);

            notificationTask.setDateToSend(
                    LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            String message= matcher.group(3);
            notificationTask.setMessage(message);
            notificationTask.setChatId(chatId);
            notificationTask.setId(messageId);
            notificationTask.setUserName(update.message().chat().username());
            log.info("выполнился метод parsMessage");
            return notificationTask;
        }
        log.info("выполнился метод parsMessage, вернул null");
        return null;
    }


    // проверяет есть ли напоминимня на данную минуту
    public List<NotificationTask> checkCurrentNotifications() {
        // вычисляю текущую минуту. Точность до минут!
        LocalDateTime currentMoment = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        // иду в репозиторий и достаю все напоминания и фильтрую их по дате и времени,
        // собираю подходящие напоминания в список
        return notificationTaskRepository.findAll()
                .stream()
                .filter(n -> n.getDateToSend().equals(currentMoment))
                .collect(Collectors.toList());
    }

    // из списка напоминаний достает ID чата и сообщение, формирую сущность SendMessage,
    // и собирает их в список
    public List<SendMessage> makeNotification(List<NotificationTask> notificationsList) {
        List<SendMessage> sendMessageList = notificationsList
                .stream()
                .map(n ->
                        new SendMessage(n.getChatId(), n.getMessage())
                )
                .collect(Collectors.toList());
        log.info("выполнился метод makeNotification");
        return sendMessageList;
    }


}
