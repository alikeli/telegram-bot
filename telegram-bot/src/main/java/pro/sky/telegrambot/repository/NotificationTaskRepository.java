package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.telegrambot.entity.NotificationTask;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {
    Collection<NotificationTask> findAllByChatId(long chatId);
    List<NotificationTask> findByDateToSendEquals(LocalDateTime localDateTime);



}
