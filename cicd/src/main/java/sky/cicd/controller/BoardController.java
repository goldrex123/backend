package sky.cicd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sky.cicd.common.api.ApiResponse;
import sky.cicd.common.api.SuccessCode;
import sky.cicd.dto.BoardCreateRequest;
import sky.cicd.dto.BoardResponse;
import sky.cicd.dto.BoardUpdateRequest;
import sky.cicd.entity.Board;
import sky.cicd.service.BoardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;
 
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BoardResponse>>> getAll(@PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, boardService.getAll(pageable)));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> getBoardById(@PathVariable Long boardId) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, boardService.getBoardById(boardId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponse>> createBoard(@RequestBody BoardCreateRequest boardCreateRequest) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CREATED, boardService.createBoard(boardCreateRequest)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<BoardResponse>> updateBoard(@RequestBody BoardUpdateRequest boardUpdateRequest) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.UPDATED, boardService.updateBoard(boardUpdateRequest)));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoardById(@PathVariable Long boardId) {
        boardService.deleteBoardById(boardId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.DELETED, null));
    }
 }
