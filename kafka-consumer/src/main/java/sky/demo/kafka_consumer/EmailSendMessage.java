package sky.demo.kafka_consumer;

import tools.jackson.databind.ObjectMapper;

public class EmailSendMessage {

    private String from;
    private String to;
    private String subject;
    private String body;


    public EmailSendMessage() {
    }

    public EmailSendMessage(String from, String to, String subject, String body) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public static EmailSendMessage fromJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, EmailSendMessage.class);
    }
}
