package sky.cicd.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.cicd.dto.BoardResponse;
import sky.cicd.entity.Board;
import sky.cicd.repository.BoardRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public Page<BoardResponse> getAll(Pageable pageable) {
        Page<Board> boardList = boardRepository.findAll(pageable);
        return boardList.map(BoardResponse::of);
    }

    public BoardResponse getBoardById(Long boardId) {
        Board board = findBoard(boardId);
        return BoardResponse.of(board);
    }

    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글 정보가 없습니다"));
    }


}
