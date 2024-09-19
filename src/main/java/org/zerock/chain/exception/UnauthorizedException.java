package org.zerock.chain.exception;

public class UnauthorizedException extends RuntimeException{
    // 권한이 없는 사용자가 접근하려 할 때 발생시킬 UnauthorizedException 예외 클래스를 추가합니다.
    public UnauthorizedException(String message) {
        super(message);
    }
}
