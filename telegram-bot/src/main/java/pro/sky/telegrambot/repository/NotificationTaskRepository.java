package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.entity.NotificationTask;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
@Repository
@Transactional
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {
    Collection<NotificationTask> findAllByChatId(long chatId);
    List<NotificationTask> findByDateToSendEquals(LocalDateTime localDateTime);
    Collection<NotificationTask> deleteNotificationTasksByChatId(long chatId);




}
