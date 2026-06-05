package unit;

import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.TransferResponse;
import com.bank.entity.Account;
import com.bank.entity.User;
import com.bank.exception.CustomExceptions;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import com.bank.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransferService transferService;

    private Long fromUserId;
    private Long toUserId;
    private Account fromAccount;
    private Account toAccount;
    private User toUser;
    private TransferRequest request;

    @BeforeEach
    void setUp() {
        fromUserId = 1L;
        toUserId = 2L;

        fromAccount = Account.builder()
                .id(1L)
                .balance(new BigDecimal("1000.00"))
                .initialBalance(new BigDecimal("1000.00"))
                .build();

        toAccount = Account.builder()
                .id(2L)
                .balance(new BigDecimal("500.00"))
                .initialBalance(new BigDecimal("500.00"))
                .build();

        toUser = User.builder()
                .id(toUserId)
                .name("Test User")
                .build();

        request = new TransferRequest();
        request.setToUserId(toUserId);
        request.setAmount(new BigDecimal("200.00"));
    }

    @Test
    void transferMoney_Success_ShouldTransferCorrectly() {
        when(accountRepository.findByUserIdWithLock(fromUserId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserIdWithLock(toUserId)).thenReturn(Optional.of(toAccount));
        when(userRepository.findById(toUserId)).thenReturn(Optional.of(toUser));
        when(accountRepository.save(any(Account.class))).thenReturn(fromAccount).thenReturn(toAccount);

        TransferResponse response = transferService.transferMoney(fromUserId, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(fromAccount.getBalance()).isEqualTo(new BigDecimal("800.00"));
        assertThat(toAccount.getBalance()).isEqualTo(new BigDecimal("700.00"));

        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transferMoney_SameUser_ShouldThrowException() {
        request.setToUserId(fromUserId);

        assertThatThrownBy(() -> transferService.transferMoney(fromUserId, request))
                .isInstanceOf(CustomExceptions.SameUserTransferException.class);

        verify(accountRepository, never()).findByUserIdWithLock(any());
    }

    @Test
    void transferMoney_InsufficientFunds_ShouldThrowException() {
        request.setAmount(new BigDecimal("1500.00"));
        when(accountRepository.findByUserIdWithLock(fromUserId)).thenReturn(Optional.of(fromAccount));
        when(userRepository.findById(toUserId)).thenReturn(Optional.of(toUser));
        when(accountRepository.findByUserIdWithLock(toUserId)).thenReturn(Optional.of(toAccount));

        assertThatThrownBy(() -> transferService.transferMoney(fromUserId, request))
                .isInstanceOf(CustomExceptions.InsufficientFundsException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void transferMoney_ZeroAmount_ShouldThrowException() {
        request.setAmount(BigDecimal.ZERO);

        assertThatThrownBy(() -> transferService.transferMoney(fromUserId, request))
                .isInstanceOf(CustomExceptions.InsufficientFundsException.class)
                .hasMessageContaining("positive");

        verify(accountRepository, never()).findByUserIdWithLock(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void transferMoney_NullAmount_ShouldThrowException() {
        request.setAmount(null);

        assertThatThrownBy(() -> transferService.transferMoney(fromUserId, request))
                .isInstanceOf(CustomExceptions.InsufficientFundsException.class)
                .hasMessageContaining("positive");

        verify(accountRepository, never()).findByUserIdWithLock(any());
    }
}