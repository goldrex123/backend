package sky.spring.pg.presentation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sky.spring.pg.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<String> health() {
        log.info("Health check 요청");
        return ApiResponse.success("OK");
    }
}
