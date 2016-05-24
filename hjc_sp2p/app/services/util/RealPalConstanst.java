package services.util;

import play.Play;

/**
 * 融宝常量类
 * @author yangxuan
 * @date 2015年10月13日
 */
public class RealPalConstanst {
	/**
	 * 本地测试地址
	 */
	public static final String BASE_URL = Play.configuration.getProperty("test.application.baseUrl") + Play.configuration.getProperty("http.path") + "/";
	

	/**
	 * 融宝网关MD5key
	 */
	public static final String REALPAL_MD5KEY = Play.configuration.getProperty("realpal_md5key");
	
	/**
	 * 融宝收银台url
	 */
	public static final String REALPAL_URL = Play.configuration.getProperty("realpal_url");
	/**
	 * 融宝收银台查询接口url
	 */
	public static final String REALPAL_QUERRY_URL ="http://interface.reapal.com/query/payment";
	 
	/**
	 * 融宝网关接口名称
	 */
	public static final String REALPAL_SERVICE = Play.configuration.getProperty("realpal_service");
	
	/**
	 * 商户ID
	 */
	public static final String REALPAL_MERCHANT_ID = Play.configuration.getProperty("realpal_merchant_ID");
	
	/**
	 * 交易状态异步通知接收URL
	 */
	public static final String REALPAL_NOTIFY_URL =  BASE_URL + Play.configuration.getProperty("realpal_notify_url");
	
	/**
	 * 融宝处理结果返回URL
	 */
	public static final String REALPAL_RETURN_URL = BASE_URL + Play.configuration.getProperty("realpal_return_url");
	
	/**
	 * 编码字符集
	 */
	public static final String REALPAL_CHARSET = Play.configuration.getProperty("realpal_charset");
	
	/**
	 * 支付方式
	 */
	public static final String REALPAL_PAYMETHOD = Play.configuration.getProperty("realpal_paymethod");

	/**
	 * 商户email
	 */
	public static final String REALPAL_SELLER_EMAIL = Play.configuration.getProperty("realpal_seller_email");
	
	/**
	 * 商品名称
	 */
	public static final String REALPAL_TITLE = Play.configuration.getProperty("realpal_title");
	
	/**
	 * 商品描述
	 */
	public static final String REALPAL_BODY = Play.configuration.getProperty("realpal_body");
	/**
	 * 支付类型
	 */
	public static final String REALPAL_PAYMENT_TYPE = Play.configuration.getProperty("realpal_payment_type");
	/**
	 * 加密方式
	 */
	public static final String REALPAL_SIGN_TYPE = Play.configuration.getProperty("realpal_sign_type");
	
	
	/**
	 * 实名认证MD5key
	 */
	public static final String REALPAL_AUTH_MD5KEY = Play.configuration.getProperty("realpal_auth_md5key");
	public static final String REALPAL_AUTH_URL = Play.configuration.getProperty("realpal_auth_url");
	public static final String REALPAL_AUTH_VERSION = Play.configuration.getProperty("realpal_auth_version");
	public static final String REALPAL_AUTH_SERVICE = Play.configuration.getProperty("realpal_auth_service");
	public static final String REALPAL_AUTH_SIGNTYPE = Play.configuration.getProperty("realpal_auth_signType");
	public static final String REALPAL_AUTH_SIGN = Play.configuration.getProperty("realpal_auth_sign");
	public static final String REALPAL_AUTH_PARTNER = Play.configuration.getProperty("realpal_auth_partner");
	
	
	/**
	 * 快捷支付
	 */
	public static final String QUICKDEBIT_CERT_TYPE = Play.configuration.getProperty("quickDebit_cert_type","01");
	public static final String QUICKDEBIT_CURRENCY = Play.configuration.getProperty("quickDebit_currency","156");
	public static final String QUICKDEBIT_TITLE = Play.configuration.getProperty("quickDebit_title","充值");
	public static final String QUICKDEBIT_BODY = Play.configuration.getProperty("quickDebit_body","充值");
	public static final String QUICKDEBIT_TERMINAL_TYPE = Play.configuration.getProperty("quickDebit_terminal_type","mobile");
	public static final String QUICKDEBIT_TERMINAL_INFO = Play.configuration.getProperty("quickDebit_terminal_info","终端充值");
	public static final String QUICKDEBIT_SELLER_EMAIL = Play.configuration.getProperty("quickDebit_seller_email");
	public static final String QUICKDEBIT_VERSION = Play.configuration.getProperty("quickDebit_version","3.0");
	public static final String QUICKDEBIT_URL = Play.configuration.getProperty("quickDebit_url");
	public static final String QUICKDEBIT_USER_KEY = Play.configuration.getProperty("quickDebit_user_key");
	public static final String QUICKDEBIT_MERCHANT_ID = Play.configuration.getProperty("quickDebit_merchant_id");
	public static final String QUICKDEBIT_NOTIFY_URL = BASE_URL +Play.configuration.getProperty("quickDebit_notify_url"); 
	
	
	
	
	
	
}
