package sky.cicd.dto;

import lombok.AccessLevel;
import lombok.Builder;
import sky.cicd.entity.Board;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record BoardResponse(
        Long boardId,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static BoardResponse of(Board board) {
        return BoardResponse.builder()
                .boardId(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}
