package sky.spring.transaction_lock.fixture;

import sky.spring.transaction_lock.event.entity.Event;
import sky.spring.transaction_lock.event_participant.entity.Member;
import sky.spring.transaction_lock.event_participant.entity.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EventFixture {

    public static Event createEvent(String name, int maxParticipant) {
        return Event.builder()
                .name(name == null ? "테스트 이벤트" : name)
                .description("테스트 설명")
                .eventDate(LocalDateTime.now().plusDays(7))
                .maxParticipants(maxParticipant > 0 ? maxParticipant : 100)
                .build();
    }

    public static List<Event> createEvents(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createEvent("테스트 이벤트" + i, 100))
                .collect(Collectors.toList());
    }


    public static Member createMember(String nickname) {
        return Member.builder()
                .email("test" + UUID.randomUUID() + "@test.com")
                .password("password")
                .nickname(nickname == null ? "테스트 유저" : nickname)
                .role(Role.USER)
                .build();
    }

    public static List<Member> createMembers(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createMember("테스트유저" + i))
                .collect(Collectors.toList());
    }
}
