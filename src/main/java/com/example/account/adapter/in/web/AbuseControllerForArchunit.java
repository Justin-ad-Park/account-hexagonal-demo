package com.example.account.adapter.in.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

//@DomainCommand
@RestController
@RequestMapping("/accounts")
@Validated
public class AbuseControllerForArchunit {

    /**
     * DomainCommands를 강제 사용했을 때 ArchUnit에서 검출이 되는지 테스트용
     */
//    @PostMapping("/{accountNumber}/depositAbuse")
//    public ResponseEntity<AccountResponse>  depositAbuse(@PathVariable @NotBlank String accountNumber, @RequestParam @PositiveOrZero long amount) {
//        var acc = Account.of("test-001", "Abuser", 999);
//
//        // 이 부분에서 AccountCommands를 강제로 사용
//        AccountCommands.deposit(acc, new Amount(100));
//
//        var body = AccountResponse.of(acc);
//        return ResponseEntity.ok(body); // 200 OK
//    }
}
