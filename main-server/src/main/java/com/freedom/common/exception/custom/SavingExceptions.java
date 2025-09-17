package com.freedom.common.exception.custom;

public class SavingExceptions {

    public static class SavingSubscriptionNotFoundException extends RuntimeException {
        public SavingSubscriptionNotFoundException() {
            super("적금 구독을 찾을 수 없거나 권한이 없습니다.");
        }
    }

    public static class SavingSubscriptionInvalidStateException extends RuntimeException {
        public SavingSubscriptionInvalidStateException(String current) {
            super("해당 상태에선 수행할 수 없습니다. 현재=" + current);
        }
    }

    public static class SavingNoNextPlannedPaymentException extends RuntimeException {
        public SavingNoNextPlannedPaymentException() {
            super("다음 납입 계획이 없습니다.");
        }
    }

    public static class SavingInvalidPaymentAmountException extends RuntimeException {
        public SavingInvalidPaymentAmountException() {
            super("납입 금액이 유효하지 않습니다.");
        }
    }

    public static class SavingInvalidDatesException extends RuntimeException {
        public SavingInvalidDatesException() {
            super("시작일/만기일이 유효하지 않습니다.");
        }
    }

    public static class SavingPaymentInvalidParamsException extends RuntimeException {
        public SavingPaymentInvalidParamsException(String message) {
            super(message);
        }
    }

    public static class SavingPolicyInvalidException extends RuntimeException {
        public SavingPolicyInvalidException(String message) {
            super(message);
        }
    }

    public static class SavingSnapshotIdentifiersInvalidException extends RuntimeException {
        public SavingSnapshotIdentifiersInvalidException() {
            super("스냅샷 식별자가 유효하지 않습니다.");
        }
    }

    public static class SavingDuplicateSubscriptionException extends RuntimeException {
        public SavingDuplicateSubscriptionException() {
            super("이미 가입한 적금에 또 가입할 수 없습니다.");
        }
    }

    public static class SavingMaturityNotReachedException extends RuntimeException {
        public SavingMaturityNotReachedException(String maturityDate) {
            super("아직 만기일이 도래하지 않았습니다. 만기일: " + maturityDate);
        }
    }
}
