package services.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *类名：rongpay_service
 *功能：融宝支付外部服务接口控制
 *详细：该页面是请求参数核心处理文件，不需要修改
 *版本：1.0
 *修改日期：2012-05-01
 *说明：
  以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
  该代码仅供学习和研究融宝支付接口使用，只是提供一个参考。
 */
public class RongpayService {

	/**
	 * 功能：构造表单提交HTML
	 * @param merchant_ID 合作身份者ID
	 * @param seller_email 签约融宝支付账号或卖家融宝支付帐户
	 * @param return_url 付完款后跳转的页面 要用 以http开头格式的完整路径，不允许加?id=123这类自定义参数
	 * @param notify_url 交易过程中服务器通知的页面 要用 以http开格式的完整路径，不允许加?id=123这类自定义参数
	 * @param order_no 请与贵网站订单系统中的唯一订单号匹配
	 * @param title 订单名称，显示在融宝支付收银台里的“商品名称”里，显示在融宝支付的交易管理的“商品名称”的列表里。
	 * @param body 订单描述、订单详细、订单备注，显示在融宝支付收银台里的“商品描述”里
	 * @param total_fee 订单总金额，显示在融宝支付收银台里的“交易金额”里
	 * @param buyer_email 默认买家融宝支付账号
	 * @param charset 字符编码格式 目前支持 GBK 或 utf-8
	 * @param key 安全校验码
	 * @param sign_type 签名方式 不需修改
	 * @return 表单提交HTML文本
	 */
	public static String BuildForm(String service,
			String payment_type,
			String merchant_ID,
			String seller_email,
			String return_url,
			String notify_url,
			String order_no,
			String title,
			String body,
			String total_fee,
            String buyer_email,
            String charset,
            String paymethod,
            String defaultbank,
            String key,
            String sign_type
            ){
		Map sPara = new HashMap();
		sPara.put("service",service);
		sPara.put("payment_type",payment_type);
		sPara.put("merchant_ID", merchant_ID);
		sPara.put("seller_email", seller_email);
		sPara.put("return_url", return_url);
		sPara.put("notify_url", notify_url);
		sPara.put("charset", charset);
		sPara.put("order_no", order_no);
		sPara.put("title", title);
		sPara.put("body", body);
		sPara.put("total_fee", total_fee);
		sPara.put("buyer_email", buyer_email);
		sPara.put("paymethod", paymethod);
		sPara.put("defaultbank", defaultbank);
		
		
		String mysign = RongpayFunction.BuildMysign(sPara, key);//生成签名结果
		
		StringBuffer sbHtml = new StringBuffer();
		List keys = new ArrayList(sPara.keySet());
		String gateway=RongpayConfig.rongpay_url;
		
		//GET方式传递
		sbHtml.append("<form id=\"rongpaysubmit\" name=\"rongpaysubmit\" action=\"").append(gateway).append("\" method=\"get\">");
		
		//post方式传递
		//sbHtml.append("<form id=\"rongpaysubmit\" name=\"rongpaysubmit\" action=\"").append(gateway).append("?charset=gbk\" method=\"post\">");
		
		String name ="";
		String value ="";
		for (int i = 0; i < keys.size(); i++) {
			 name=(String) keys.get(i);
			 value=(String) sPara.get(name);
			if(value!=null && !"".equals(value)){
				sbHtml.append("<input type=\"hidden\" name=\"").append(name).append("\" value=\"" + value + "\"/>");
			}
		}
        sbHtml.append("<input type=\"hidden\" name=\"sign\" value=\"").append(mysign).append("\"/>");
        sbHtml.append("<input type=\"hidden\" name=\"sign_type\" value=\"").append(sign_type).append("\"/>");
        
        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"融宝支付确认付款\"></form>");
		return sbHtml.toString();
	}
	
	public static void main(String[] args) {
//		Map sPara = new HashMap();
//		sPara.put("service","online_pay");
//		sPara.put("payment_type","1");
//		sPara.put("merchant_ID", "100000000060011");
//		sPara.put("seller_email", "it@3-bank.com");
//		sPara.put("return_url", "http://www.3-bank.com/pay/return_url");
//		sPara.put("notify_url", "http://www.3-bank.com/pay/notify_url");
//		sPara.put("charset", "utf-8");
//		sPara.put("order_no", "493002407599521");
//		sPara.put("title", "测试商品标题");
//		sPara.put("body", "这个是测试商品的商品描述");
//		sPara.put("total_fee", "1");
//		sPara.put("buyer_email", "");
//		sPara.put("paymethod", "bankPay");
//		sPara.put("defaultbank", "");
//		
//		String mysign = RongpayFunction.BuildMysign(sPara, "g8c718e891683g612bacgggg9981dcg4e84fe1d9463da69b3eb3g1252039b45e");//生成签名结果
//		System.out.println("===mysign="+mysign);
		String s = "_input_charset=GBK&batchAmount=5&batchBizid=100000000068109&batchBiztype=00000&batchContent=1,6222081402002512758,黄寿东,中国工商银行,分行,中国工商银行股份有限公司福州福大支行,私,3.0,CNY,福建,福州,13799990071,身份证,350122198702050134,111111111111111,20150929151522752876,代付|&batchCount=1&batchCurrnum=20150929151522752876&batchDate=20150929&batchVersion=001e9ce7e233614cfc544cgb18cbdba9g48b0351c7d65220748ad5b45698a618e1";
		String str = Md5Encrypt.md5(s);
		System.out.println(str);

	}
	

}
