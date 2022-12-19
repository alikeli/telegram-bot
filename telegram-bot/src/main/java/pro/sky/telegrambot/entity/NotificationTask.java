package pro.sky.telegrambot.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter

@Entity(name = "tasksTable")
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    private  long chatId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationTask)) return false;
        NotificationTask that = (NotificationTask) o;
        return Objects.equals(chatId, that.chatId) && Objects.equals(message, that.message) && Objects.equals(DateToSend, that.DateToSend) && Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, chatId, userName, message, DateToSend);
    }
}