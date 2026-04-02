package com.swmanager.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "tb_infra_memo")
@Data
public class InfraMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infra_id")
    private Infra infra;

    private String memoContent;
    
    private String memoWriter;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate memoDate;
}