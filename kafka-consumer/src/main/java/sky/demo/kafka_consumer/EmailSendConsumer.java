package sky.demo.kafka_consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmailSendConsumer {

    @KafkaListener(
            topics = "email.send",
            groupId = "email-send-group"
    )
    public void consume(String message) {
        System.out.println("message = " + message);

        EmailSendMessage emailSendMessage = EmailSendMessage.fromJson(message);


        System.out.println("이메일 발송 완료");
    }
}
