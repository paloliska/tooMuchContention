package sk.palo.liska;

import java.math.BigDecimal;
import javax.persistence.*;
import com.sun.istack.internal.NotNull;
import org.joda.time.DateTime;

/**
 * @author pavol.liska
 * @date 6/10/2018
 */
@Entity(name = "MYTABLE")
public class MyTable {

    private static final long serialVersionUID = -5829007942440551969L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    @NotNull
    private String reqType;

    @Column(length = 40)
    private String applicationSender = null;

    @Column(length = 40)
    @NotNull
    private String workstationID = null;

    @Column(length = 40)
    private String popID = null;

    @Column(length = 40)
    @NotNull
    private String requestID = null;

    @Column(length = 20)
    private String shiftNumber = null;

    @Column(length = 20)
    private String clerkID = null;

    @Column(precision = 19, scale = 3)
    private BigDecimal amount = null;

    @Column
    private String terminalId = null;

    @Column
    private Long batch;

    @Column
    private Long trnSecureDataId;

    @Column(length = 8)
    private Integer stan;

    @Column
    private Long hostConfigId;

    @Column
    private String referenceNumber;

    @Column
    private String transactionNumber;

    @Column
    private BigDecimal cashBackAmount;

    @Column
    private String paymentMethod;

    @Column
    private String originalInvoiceNumber = null;

    @Column(precision = 19, scale = 3)
    private BigDecimal originalAmount = null;

    @Column
    private boolean resendFlag;

    @Column
    private Boolean wasPrepayCardInBasket;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public String getApplicationSender() {
        return applicationSender;
    }

    public void setApplicationSender(String applicationSender) {
        this.applicationSender = applicationSender;
    }

    public String getWorkstationID() {
        return workstationID;
    }

    public void setWorkstationID(String workstationID) {
        this.workstationID = workstationID;
    }

    public String getPopID() {
        return popID;
    }

    public void setPopID(String popID) {
        this.popID = popID;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(String shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public String getClerkID() {
        return clerkID;
    }

    public void setClerkID(String clerkID) {
        this.clerkID = clerkID;
    }


    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public Long getBatch() {
        return batch;
    }

    public void setBatch(Long batch) {
        this.batch = batch;
    }

    public Integer getStan() {
        return stan;
    }

    public void setStan(Integer stan) {
        this.stan = stan;
    }

    public Long getHostConfigId() {
        return hostConfigId;
    }

    public void setHostConfigId(Long hostConfigId) {
        this.hostConfigId = hostConfigId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public BigDecimal getCashBackAmount() {
        return cashBackAmount;
    }

    public void setCashBackAmount(BigDecimal cashBackAmount) {
        this.cashBackAmount = cashBackAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }


    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getOriginalInvoiceNumber() {
        return originalInvoiceNumber;
    }

    public void setOriginalInvoiceNumber(String originalInvoiceNumber) {
        this.originalInvoiceNumber = originalInvoiceNumber;
    }


    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }


    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public boolean isResendFlag() {
        return resendFlag;
    }

    public void setResendFlag(boolean resendFlag) {
        this.resendFlag = resendFlag;
    }

    /**
     * Methods returns total requested amount
     */
    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        sum = sum.add(amount == null ? BigDecimal.ZERO : amount);
        sum = sum.add(cashBackAmount == null ? BigDecimal.ZERO : cashBackAmount);
        return sum;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public Boolean getWasPrepayCardInBasket() {
        return wasPrepayCardInBasket;
    }

    public void setWasPrepayCardInBasket(Boolean wasPrepayCardInBasket) {
        this.wasPrepayCardInBasket = wasPrepayCardInBasket;
    }

    public Long getTrnSecureDataId() {
        return trnSecureDataId;
    }

    public void setTrnSecureDataId(Long trnSecureDataId) {
        this.trnSecureDataId = trnSecureDataId;
    }

    @Override
    public String toString() {
        return "PosRequest{" +
                "id=" + id +
                ", reqType='" + reqType + '\'' +
                ", applicationSender='" + applicationSender + '\'' +
                ", workstationID='" + workstationID + '\'' +
                ", popID='" + popID + '\'' +
                ", requestID='" + requestID + '\'' +
                ", shiftNumber='" + shiftNumber + '\'' +
                ", clerkID='" + clerkID + '\'' +
                ", amount=" + amount +
                ", terminalId='" + terminalId + '\'' +
                ", batch=" + batch +
                ", trnSecureDataId=" + trnSecureDataId +
                ", stan=" + stan +
                ", hostConfigId=" + hostConfigId +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", transactionNumber='" + transactionNumber + '\'' +
                ", cashBackAmount=" + cashBackAmount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", originalInvoiceNumber='" + originalInvoiceNumber + '\'' +
                ", originalAmount=" + originalAmount +
                ", resendFlag=" + resendFlag +
                ", wasPrepayCardInBasket=" + wasPrepayCardInBasket +
                '}';
    }
}