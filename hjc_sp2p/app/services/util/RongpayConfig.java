/* *
 *功能：设置帐户有关信息及返回路径（基础配置页面）
 *版本：1.0
 *日期：2012-09-01
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究融宝支付接口使用，只是提供一个参考。
	
 *提示：如何获取安全校验码和合作身份者ID
 *1.访问融宝支付商户后台(m.rongpay.com.cn)，然后用您的签约融宝支付账号登陆.
 *2.点击导航栏中的“商家服务”，即可查看
	
 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 * */
package services.util;

import play.Play;

public class RongpayConfig {
	
	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// 合作身份者ID，由纯数字组成的字符串
	/**
	 * 本地测试地址
	 */
	public static final String BASE_URL = Play.configuration.getProperty("test.application.baseUrl") + Play.configuration.getProperty("http.path") + "/";
	
	public static String merchant_ID = Play.configuration.getProperty("realpal_merchant_ID");
	
	//交易安全检验码，由数字和字母组成的32位字符串
	public static String key = Play.configuration.getProperty("realpal_md5key");
	//签约融宝支付账号或卖家收款融宝支付帐户
	public static String seller_email = Play.configuration.getProperty("realpal_seller_email");
	
	
	//notify_url 交易过程中服务器通知的页面 要用 http://格式的完整路径，不允许加?id=123这类自定义参数
	public static String notify_url = BASE_URL + Play.configuration.getProperty("realpal_notify_url");
	
	//付完款后跳转的页面 要用 http://格式的完整路径，不允许加?id=123这类自定义参数
	public static String return_url =BASE_URL + Play.configuration.getProperty("realpal_return_url");
	
	//收款方名称，如：公司名称、网站名称、收款人姓名等
	public static String mainname = "融宝支付";
	
	//接口服务名称，目前固定值：online_pay（网上支付）
	public static String service="online_pay";
	
	
	//支付类型，目前固定值：1
	public static String payment_type="1";
	
	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	//支付提交地址
	//public static String rongpay_url="http://192.168.0.58:18170/portal";
	public static String rongpay_url="https://epay.reapal.com/portal";
	//返回验证订单地址
//	public static String verify_url="http://interface.reapal.com/verify/notify?";
	public static String verify_https= Play.configuration.getProperty("verify_https","https://interface.reapal.com/verify/notify?");
	public static String verify_url= Play.configuration.getProperty("verify_http","http://interface.reapal.com/verify/notify?");
	//public static String verify_url="http://192.168.0.79:8080/mapi/verify/notify?";
	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String charset = "utf-8";
	
	// 签名方式 不需修改
	public static String sign_type = "MD5";
	
	//访问模式,根据自己的服务器是否支持ssl访问，若支持请选择https；若不支持请选择http
	public static String transport = "http";
}
