package sky.demo.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class EmailService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public EmailService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEmail(SendEmailRequestDto requestDto) {
        EmailSendMessage emailSendMessage = new EmailSendMessage(
                requestDto.getFrom(), requestDto.getTo(), requestDto.getSubject(), requestDto.getBody()
        );

        kafkaTemplate.send("email.send", toJsonString(emailSendMessage));
    }

    private String toJsonString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

}
