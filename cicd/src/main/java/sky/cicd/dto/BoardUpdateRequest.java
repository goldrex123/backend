package sky.cicd.dto;

public record BoardUpdateRequest (
        Long boardId,
        String title,
        String content
){
}
