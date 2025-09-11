package com.freedom.common.exception.custom;

/**
 * 적금 스냅샷 식별자(공시월, 금융회사코드, 상품코드)가 유효하지 않을 때 발생하는 예외
 */
public class SavingSnapshotIdentifiersInvalidException extends RuntimeException {
    
    public SavingSnapshotIdentifiersInvalidException() {
        super("적금 스냅샷 식별자가 유효하지 않습니다. 공시월, 금융회사코드, 상품코드는 필수입니다.");
    }
    
    public SavingSnapshotIdentifiersInvalidException(String message) {
        super(message);
    }
}
