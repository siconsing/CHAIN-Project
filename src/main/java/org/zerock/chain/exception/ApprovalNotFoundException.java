package org.zerock.chain.exception;

public class ApprovalNotFoundException extends RuntimeException{
    public ApprovalNotFoundException(String message) {
        super(message);
    }
}
