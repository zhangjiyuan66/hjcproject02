package utils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import business.BackstageSet;



import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import sms.ymrt.sdkhttp.Client;

public class SMSUtil {

	/**
	 * 发送短信
	 * @param mobile
	 * @param content
	 * @param error
	 */
	public static void sendSMS(String mobile,String content, ErrorInfo error) {
		if(StringUtils.isBlank(content)) {
			error.code = -1;
			error.msg = "请输入短信内容";
			
			return;
		}
		
		BackstageSet backstageSet  = BackstageSet.getCurrentBackstageSet();
		int value=-1;
		try {
			value = Client.getClient(backstageSet.smsAccount, backstageSet.smsPassword).sendSMS(new String[]{mobile}, content, "", 5);
			Logger.info("*****SmsUtil返回值===：%s",value);

			if(value != 0){
				error.code = -3;
				error.msg = "短信验证码发送失败!";
				return;
			}
		} catch (RemoteException e) {
			error.code = -3;
			error.msg = "短信验证码发送失败!";
			return;
		}
		error.msg = "短信发送成功";
	}
	
	/**
	 * 发送校验码
	 * @param mobile
	 * @param error
	 */
	public static void sendCode(String mobile, ErrorInfo error) {
		error.clear();
		
		BackstageSet backstageSet  = BackstageSet.getCurrentBackstageSet();
		int randomCode = (new Random()).nextInt(8999) + 1000;// 最大值位9999
		String content = randomCode+"(动态验证码)。工作人员不会向您索要，请勿向任何人泄露";
		Logger.info(content);
		int value=-1;
		try {
			value = Client.getClient(backstageSet.smsAccount, backstageSet.smsPassword).sendSMS(new String[]{mobile}, content, "", 5);
			Logger.info("*****SmsUtil返回值===：%s",value);

			if(value != 0){
				error.code = -3;
				error.msg = "短信验证码发送失败!";
				return;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			error.code = -3;
			error.msg = "短信验证码发送失败!";
			return;
		}
		
		play.cache.Cache.set(mobile, randomCode + "", "2min");
		error.msg = "短信验证码发送成功";
	}
	
	
	public static final String url = Play.configuration.getProperty("jwURL", "");
	public static final String productid = Play.configuration.getProperty("jw_productID", ""); 
	/** 
	* @MethodName: sendSMS 
	* @Param: SMSUtil 
	* @Return: 
	* @Descb:九维 发送短信 
	* @Throws: 
	*/ 
	public static String jwsendSMS(String mobile,String content) { 
	BackstageSet backstageSet  = BackstageSet.getCurrentBackstageSet();
	try{ 
	 
	Map<String, String> map = new HashMap<String, String>(); 
	map.put("username",backstageSet.jwsmsAccount); 
	map.put("password",backstageSet.jwsmsPassword); 
	map.put("mobile",mobile);
	map.put("content", content); 
	map.put("productid", productid); 
	String data = MmmUtil.byPostMethodToHttpEntity(url, MmmUtil.putParams(map), "UTF-8"); 
	Logger.info("=========九维返回=========:" + data);
	String status = data.split(",")[0]; 
	if(status.equals("1")){ 
	return "Success"; 
	} 
	new Exception(); 
	}catch (Exception e) { 
	return "Fail"; 
	}
	return "Success"; 
	 
	}
	
	
	
	/**
	 * 九维发送校验码
	 * @param mobile
	 * @param error
	 */
	public static String jwsendCode(String mobile) {
		BackstageSet backstageSet  = BackstageSet.getCurrentBackstageSet();
		int randomCode = (new Random()).nextInt(8999) + 1000;// 最大值位9999
		String content = randomCode+"(动态验证码)。工作人员不会向您索要，请勿向任何人泄露";
		Logger.info(content);
		try{ 
			 
			Map<String, String> map = new HashMap<String, String>(); 
			map.put("username",backstageSet.jwsmsAccount); 
			map.put("password",backstageSet.jwsmsPassword); 
			map.put("mobile",mobile);
			map.put("content", content); 
			map.put("productid", productid); 
			String data = MmmUtil.byPostMethodToHttpEntity(url, MmmUtil.putParams(map), "UTF-8"); 
			Logger.info("=========九维返回=========:" + data);
			String status = data.split(",")[0]; 
			if(status.equals("1")){ 
			play.cache.Cache.set(mobile, randomCode + "", "2min");
			return "Success"; 
			} 
			new Exception(); 
			}catch (Exception e) { 
			return "Fail"; 
			}
			play.cache.Cache.set(mobile, randomCode + "", "2min");
			return "Success"; 
			
			
	}
		
}
