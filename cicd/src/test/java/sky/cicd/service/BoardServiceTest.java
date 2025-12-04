package sky.cicd.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sky.cicd.dto.BoardCreateRequest;
import sky.cicd.dto.BoardResponse;
import sky.cicd.dto.BoardUpdateRequest;
import sky.cicd.entity.Board;
import sky.cicd.repository.BoardRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;


class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardService boardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAll_returnPageOfBoards() {
        //given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Board> boards = new PageImpl<>(sampleList(10), pageable, 1);

        given(boardRepository.findAll(pageable)).willReturn(boards);

        //when
        Page<BoardResponse> result = boardService.getAll(pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getContent().get(0))
                .extracting(BoardResponse::title)
                .isEqualTo("title0");

        verify(boardRepository).findAll(pageable);
    }

    @Test
    void getBoardById_shouldReturnBoard() {
        //given
        Board board = sampleBoard("테스트");

        given(boardRepository.findById(1L)).willReturn(Optional.of(board));

        //when
        BoardResponse result = boardService.getBoardById(1L);

        //then
        assertThat(result)
                .isNotNull()
                .extracting(BoardResponse::title)
                .isEqualTo("title테스트");

        verify(boardRepository).findById(1L);
    }

    @Test
    void getBoardById_whenNotFound_shouleThrowException() {
        //given
        given(boardRepository.findById(1L)).willReturn(Optional.empty());
        //when & then
        assertThatThrownBy(() -> boardService.getBoardById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 게시글 정보가 없습니다");

        verify(boardRepository).findById(1L);
    }

    @Test
    void createBoard_shouldSaveAndReturnResponse() {
        //given
        BoardCreateRequest request = new BoardCreateRequest("title", "content");
        Board board = sampleBoard("123");

        given(boardRepository.save(any(Board.class))).willReturn(board);

        //when
        BoardResponse response = boardService.createBoard(request);

        //then
        assertThat(response)
                .isNotNull()
                .extracting(o -> o.title())
                .isEqualTo("title123");

        verify(boardRepository).save(any(Board.class));
    }

    @Test
    void updateBoard_shouldUpdateAndReturnResponse() {
        //given
        BoardUpdateRequest request = new BoardUpdateRequest(1L, "수정title", "수정content");
        Board board = sampleBoard("123");

        given(boardRepository.findById(1L)).willReturn(Optional.of(board));

        //when
        BoardResponse response = boardService.updateBoard(request);

        //then
        assertThat(response)
                .isNotNull()
                .extracting(BoardResponse::title)
                .isEqualTo("수정title");

        verify(boardRepository).findById(1L);
    }

    private Board sampleBoard(String text) {
        return Board.builder()
                .title("title" + text)
                .content("content" + text)
                .build();
    }

    private List<Board> sampleList(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> sampleBoard(String.valueOf(i)))
                .collect(Collectors.toList());
    }

}