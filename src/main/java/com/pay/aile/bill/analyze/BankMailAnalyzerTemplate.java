package com.pay.aile.bill.analyze;

import com.pay.aile.bill.exception.AnalyzeBillException;
import com.pay.aile.bill.model.AnalyzeParamsModel;

/**
 *
 * @author Charlie
 * @description 银行不同卡对应的解析模板 BankMailAnalyzer中使用具体的卡种对应模板进行解析账单内容
 */
public interface BankMailAnalyzerTemplate {

	/**
	 * 
	 * @param content
	 *            解析账单内容
	 */
	public void analyze(AnalyzeParamsModel apm) throws AnalyzeBillException;

	/**
	 * 
	 * @param apm
	 *            处理解析结果
	 */
	public void handleResult(AnalyzeParamsModel apm);

}
