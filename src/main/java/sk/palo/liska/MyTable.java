package sk.palo.liska;

import java.math.BigDecimal;
import javax.persistence.*;

/**
 * @author pavol.liska
 * @date 6/10/2018
 */
@Entity(name = "MYTABLE")
public class MyTable {

    private static final long serialVersionUID = -5829007942440551969L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String reqType;

    @Column(length = 40)
    private String applicationSender = null;

    @Column(length = 40)
    private String workstationID = null;

    @Column(length = 40)
    private String popID = null;

    @Column(length = 40)
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
}