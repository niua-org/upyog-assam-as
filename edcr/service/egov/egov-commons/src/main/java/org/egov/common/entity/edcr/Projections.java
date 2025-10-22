package org.egov.common.entity.edcr;

import java.math.BigDecimal;

public class Projections extends Measurement{
    private static final long serialVersionUID = 80L;
    private String number;
    private BigDecimal length;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public BigDecimal getLength() {
        return length;
    }

    @Override
    public void setLength(BigDecimal length) {
        this.length = length;
    }
}
