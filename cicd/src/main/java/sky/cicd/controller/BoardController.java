package sky.cicd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sky.cicd.common.api.ApiResponse;
import sky.cicd.common.api.SuccessCode;
import sky.cicd.dto.BoardResponse;
import sky.cicd.service.BoardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BoardResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, boardService.getAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardResponse>> getBoardById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, null));
    }







 }
