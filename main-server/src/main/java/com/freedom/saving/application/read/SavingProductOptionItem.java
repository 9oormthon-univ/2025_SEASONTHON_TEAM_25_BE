package com.freedom.saving.application.read;

import java.math.BigDecimal;

/**
 * 상세 옵션(기간/금리) dto
 */
public class SavingProductOptionItem {

    private Integer termMonths;
    private BigDecimal rate;
    private BigDecimal ratePreferential;
    private String rateType;
    private String rateTypeName;

    public Integer getTermMonths() { return termMonths; }
    public BigDecimal getRate() { return rate; }
    public BigDecimal getRatePreferential() { return ratePreferential; }
    public String getRateType() { return rateType; }
    public String getRateTypeName() { return rateTypeName; }

    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public void setRatePreferential(BigDecimal ratePreferential) { this.ratePreferential = ratePreferential; }
    public void setRateType(String rateType) { this.rateType = rateType; }
    public void setRateTypeName(String rateTypeName) { this.rateTypeName = rateTypeName; }
}
