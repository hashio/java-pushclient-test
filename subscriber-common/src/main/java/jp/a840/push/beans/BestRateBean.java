package jp.a840.push.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class BestRateBean implements Serializable {
	/** */
	private static final long serialVersionUID = 1L;

	/** �Ɩ����t */
	private String businessDate;

	/** �����R�[�h */
	private Integer productCode;

	/** �����ʔ� */
	private Long processSeq;

	/** ������X�V���� */
	private Date marketUpdateDatetime;

	/** ���ŗǋC�z�l */
	private BigDecimal bid;

	/** ���ŗǋC�z���� */
	private Integer bidSize;

	/** �������t���O */
	private boolean bidInvalidFlag;

	/** ���ŐV�L���ŗǋC�z�l */
	private BigDecimal bidLatestValid;

	/** ���ŗǋC�z�l */
	private BigDecimal ask;

	/** ���ŗǋC�z���� */
	private Integer askSize;

	/** �������t���O */
	private boolean askInvalidFlag;

	/** ���ŐV�L���ŗǋC�z�l */
	private BigDecimal askLatestValid;

	/** ����i */
	private BigDecimal limitCheckPrice;


	/**
	 * @return productCode
	 */
	public Integer getProductCode() {
		return productCode;
	}

	/**
	 * @param productCode
	 *            �Z�b�g���� productCode
	 */
	public void setProductCode(Integer productCode) {
		this.productCode = productCode;
	}

	/**
	 * @return marketUpdateDatetime
	 */
	public Date getMarketUpdateDatetime() {
		return marketUpdateDatetime;
	}

	/**
	 * @param marketUpdateDatetime
	 *            �Z�b�g���� marketUpdateDatetime
	 */
	public void setMarketUpdateDatetime(Date marketUpdateDatetime) {
		this.marketUpdateDatetime = marketUpdateDatetime;
	}

	/**
	 * @return bid
	 */
	public BigDecimal getBid() {
		return bid;
	}

	/**
	 * @param bid
	 *            �Z�b�g���� bid
	 */
	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	/**
	 * @return bidSize
	 */
	public Integer getBidSize() {
		return bidSize;
	}

	/**
	 * @param bidSize
	 *            �Z�b�g���� bidSize
	 */
	public void setBidSize(Integer bidSize) {
		this.bidSize = bidSize;
	}

	/**
	 * @return bidInvalidFlag
	 */
	public boolean isBidInvalidFlag() {
		return bidInvalidFlag;
	}

	/**
	 * @param bidInvalidFlag
	 *            �Z�b�g���� bidInvalidFlag
	 */
	public void setBidInvalidFlag(boolean bidInvalidFlag) {
		this.bidInvalidFlag = bidInvalidFlag;
	}

	/**
	 * @return askSize
	 */
	public Integer getAskSize() {
		return askSize;
	}

	/**
	 * @param askSize
	 *            �Z�b�g���� askSize
	 */
	public void setAskSize(Integer askSize) {
		this.askSize = askSize;
	}

	/**
	 * @return ask
	 */
	public BigDecimal getAsk() {
		return ask;
	}

	/**
	 * @param ask
	 *            �Z�b�g���� ask
	 */
	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	/**
	 * @return askInvalidFlag
	 */
	public boolean isAskInvalidFlag() {
		return askInvalidFlag;
	}

	/**
	 * @param askInvalidFlag
	 *            �Z�b�g���� askInvalidFlag
	 */
	public void setAskInvalidFlag(boolean askInvalidFlag) {
		this.askInvalidFlag = askInvalidFlag;
	}

	/**
	 * @return limitCheckPrice
	 */
	public BigDecimal getLimitCheckPrice() {
		return limitCheckPrice;
	}

	/**
	 * @param limitCheckPrice
	 *            �Z�b�g���� limitCheckPrice
	 */
	public void setLimitCheckPrice(BigDecimal limitCheckPrice) {
		this.limitCheckPrice = limitCheckPrice;
	}

	/**
	 * @return serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return businessDate
	 */
	public String getBusinessDate() {
		return businessDate;
	}

	/**
	 * @param businessDate
	 *            �Z�b�g���� businessDate
	 */
	public void setBusinessDate(String businessDate) {
		this.businessDate = businessDate;
	}

	/**
	 * @return bidLatestValid
	 */
	public BigDecimal getBidLatestValid() {
		if (bid != null && (bidLatestValid == null || bidLatestValid.doubleValue() == 0)) {
			throw new RuntimeException("���蓾�Ȃ��G���[ bid:" + bid + " bidLatestValid:" + bidLatestValid);
		}

		return bidLatestValid;
	}

	/**
	 * @param bidLatestValid
	 *            �Z�b�g���� bidLatestValid
	 */
	public void setBidLatestValid(BigDecimal bidLatestValid) {
		this.bidLatestValid = bidLatestValid;
	}

	/**
	 * @return askLatestValid
	 */
	public BigDecimal getAskLatestValid() {
		if (ask != null && (askLatestValid == null || askLatestValid.doubleValue() == 0)) {
			throw new RuntimeException("���蓾�Ȃ��G���[ ask" + ask + " askLatestValid:" + askLatestValid);
		}

		return askLatestValid;
	}

	/**
	 * @param askLatestValid
	 *            �Z�b�g���� askLatestValid
	 */
	public void setAskLatestValid(BigDecimal askLatestValid) {
		this.askLatestValid = askLatestValid;
	}

	/**
	 * @return processSeq
	 */
	public Long getProcessSeq() {
		return processSeq;
	}

	/**
	 * @param processSeq
	 *            �Z�b�g���� processSeq
	 */
	public void setProcessSeq(Long processSeq) {
		this.processSeq = processSeq;
	}
}
