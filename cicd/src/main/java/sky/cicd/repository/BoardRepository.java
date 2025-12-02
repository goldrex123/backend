package sky.cicd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.cicd.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
