package net.gwanghwa.account.domain;

import lombok.*;
import net.gwanghwa.account.type.TransactionResultType;
import net.gwanghwa.account.type.TransactionType;

import java.time.LocalDateTime;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Transaction {
	// PK, Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 거래의 종류
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    // 거래 결과
    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType;
    
    // 거래 발생 계좌
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "account_number") // FK 컬럼 명
    private Account account;
    
    // 거래 금액
    private Long amount;
    
    // 거래 후 계좌 잔액
    private Long balanceSnapshot;
    
    // 거래 ID
    private String transactionId;

    // 거래일시
    private LocalDateTime transactedAt;

    // 생성일시
    private LocalDateTime createdAt;

    // 최종 수정일시
    private LocalDateTime updatedAt;
}
