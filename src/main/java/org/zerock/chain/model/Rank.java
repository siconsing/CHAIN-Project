package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "RankParkyeongmin")
@Table(name = "ranks")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_no")
    private Long rankNo;

    @Column(name = "rank_name", nullable = false, length = 100)
    private String rankName;
}
