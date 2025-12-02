package sky.cicd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sky.cicd.common.domain.BaseTimeEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Board extends BaseTimeEntity {

    @Id @GeneratedValue
    @Column(name = "board_id")
    private Long id;

    private String title;

    private String content;

}
