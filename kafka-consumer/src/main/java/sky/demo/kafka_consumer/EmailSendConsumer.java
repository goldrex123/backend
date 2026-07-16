package sky.demo.kafka_consumer;

import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;

@Service
public class EmailSendConsumer {

    @KafkaListener(
            topics = "email.send",
            groupId = "email-send-group"
    )
    @RetryableTopic(
            attempts = "5",
            backOff = @BackOff(delay = 1000, multiplier = 2), // interval 설정 : 1초 단위로 *2하며 재시도
            //email.send.dlt
            dltTopicSuffix = ".dlt"
    )
    public void consume(String message) {
        System.out.println("message = " + message);

        EmailSendMessage emailSendMessage = EmailSendMessage.fromJson(message);


        if (emailSendMessage.getTo().equals("fail@naver.com")) {
            System.out.println("잘못된 이메일 주소로 인해 발송 실패");
            throw new RuntimeException("잘못된 이메일 주소로 인해 발송 실패");
        }

        System.out.println("이메일 발송 완료");
    }
}
