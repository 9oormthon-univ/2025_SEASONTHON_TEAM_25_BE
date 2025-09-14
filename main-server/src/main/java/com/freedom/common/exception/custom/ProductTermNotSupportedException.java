package com.freedom.common.exception.custom;

import java.util.List;

public class ProductTermNotSupportedException extends RuntimeException {
    public ProductTermNotSupportedException(int termMonths) {
        super("지원하지 않는 기간입니다. termMonths=" + termMonths);
    }
    
    public ProductTermNotSupportedException(int termMonths, List<Integer> supportedTerms) {
        super(String.format("지원하지 않는 기간입니다. 요청한 기간: %d개월, 지원하는 기간: %s", 
                termMonths, supportedTerms.toString()));
    }
}
