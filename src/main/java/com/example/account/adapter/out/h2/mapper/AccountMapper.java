package com.example.account.adapter.out.h2.mapper;

import com.example.account.adapter.out.h2.mybatis.AccountRow;
import com.example.account.domain.model.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {
    AccountRow findByAccountNumber(@Param("accountNumber") String accountNumber);
    int upsert(AccountRow account);
}
