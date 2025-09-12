package com.freedom.saving.application.read;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 상세 옵션(기간/금리) dto
 */
@Getter
@Setter
public class SavingProductOptionItem {

    private Integer termMonths;
    private BigDecimal rate;
    private BigDecimal ratePreferential;
    private String rateType;
    private String rateTypeName;

}
