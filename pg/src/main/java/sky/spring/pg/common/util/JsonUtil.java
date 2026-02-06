package sky.spring.pg.common.util;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JSON 직렬화 유틸리티
 *
 * JsonMapper를 주입받아 객체를 JSON 문자열로 변환합니다.
 * 직렬화 실패 시 빈 JSON 객체를 반환하여 애플리케이션 안정성을 보장합니다.
 *
 * Jackson 3.0에서 JacksonException은 RuntimeException을 상속합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JsonUtil {

  private final JsonMapper jsonMapper;

  /**
   * 객체를 JSON 문자열로 변환
   *
   * @param obj 직렬화할 객체
   * @return JSON 문자열 (실패 시 "{}")
   */
  public String toJson(Object obj) {
    try {
      return jsonMapper.writeValueAsString(obj);
    } catch (JacksonException e) {
      log.error("JSON 직렬화 실패", e);
      return "{}";
    }
  }
}
