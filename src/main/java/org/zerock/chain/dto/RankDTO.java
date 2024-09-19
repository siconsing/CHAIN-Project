    package org.zerock.chain.dto;

    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class RankDTO {

        private Long rankNo;       // 직급 고유 번호
        private String rankName;   // 직급명

    }
