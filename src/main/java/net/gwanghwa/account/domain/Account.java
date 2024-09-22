package net.gwanghwa.account.domain;

import lombok.*;
import net.gwanghwa.account.type.AccountStatus;

import java.time.LocalDateTime;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Account {
	// PK, Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소유자 정보
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "account_user") // FK 컬럼 명
    private AccountUser accountUser;
    
    // 계좌 번호
    private String accountNumber;

    // 계좌 상태
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    
    // 계좌 잔액
    private Long balance;
    
    // 계좌 등록일시
    private LocalDateTime registeredAt;

    // 계좌 해지일시
    private LocalDateTime unregistedAt;

    // 생성일시
    private LocalDateTime createdAt;

    // 최종 수정일시
    private LocalDateTime updatedAt;
}
