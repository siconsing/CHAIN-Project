package org.zerock.chain.aspect;

import jakarta.persistence.EntityNotFoundException;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;
import org.zerock.chain.exception.EmployeeNotFoundException;

@Aspect
@Component
public class RepositoryExceptionAspect {


    private static final Logger logger = LoggerFactory.getLogger(RepositoryExceptionAspect.class);

    @AfterThrowing(pointcut = "execution(* org.zerock.chain.imjongha.repository.*.*(..))", throwing = "ex")
    public void handleEntityNotFoundException(EntityNotFoundException ex) {
        logger.error("EntityNotFoundException occurred: {}", ex.getMessage(), ex);
        throw new EmployeeNotFoundException("사원을 찾을 수 없습니다.", ex);
    }

    @AfterThrowing(pointcut = "execution(* org.zerock.chain.imjongha.repository.*.*(..))", throwing = "ex")
    public void handleEmptyResultDataAccessException(EmptyResultDataAccessException ex) {
        logger.error("EmptyResultDataAccessException occurred: {}", ex.getMessage(), ex);
        throw new EmployeeNotFoundException("삭제할 사원을 찾을 수 없습니다.", ex);
    }

    @AfterThrowing(pointcut = "execution(* org.zerock.chain.imjongha.repository.*.*(..))", throwing = "ex")
    public void handleTransactionSystemException(TransactionSystemException ex) {
        logger.error("TransactionSystemException occurred: {}", ex.getMessage(), ex);
        throw new EmployeeNotFoundException("트랜잭션 오류로 사원을 처리할 수 없습니다.", ex);
    }
}
