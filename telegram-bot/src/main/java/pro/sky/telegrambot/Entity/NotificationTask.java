package pro.sky.telegrambot.Entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter

@Entity(name = "tasksTable")
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    private Long chatId;
    private String userName;
    private String message;
    private LocalDateTime DateToSend;

    @Override
    public String toString() {
        return "NotificationTask{" +
                "Id=" + Id +
                ", chatId=" + chatId +
                ", userName='" + userName + '\'' +
                ", message='" + message + '\'' +
                ", DateToSend=" + DateToSend +
                '}';
    }
}