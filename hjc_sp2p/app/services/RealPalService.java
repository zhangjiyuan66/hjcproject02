package services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;

import bean.QualityBid;
import business.User;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import constants.Constants;
import controllers.front.account.BasicInformation;
import models.t_user_auth_details;
import net.sf.json.JSONObject;
import services.util.CashierRequest;
import services.util.HttpClientUtil;
import services.util.RealPalConstanst;
import services.util.RealPalReqModel;
import services.util.ReapalUtil;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import payment.hf.service.HfPaymentCallBackService;
import play.Logger;
import play.db.jpa.GenericModel.JPAQuery;
import play.db.jpa.JPA;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.ws.WSAsync;
import play.libs.ws.WSAsync.WSAsyncRequest;
import play.mvc.Scope.Params;

/**
 * 融宝支付业务
 * 
 * @author yangxuan
 * @date 2015年10月13日
 */
public class RealPalService {

	/**
	 * 网关支付
	 */
	public static Map<String, Object> gateWayPayment(String order_no,
			String total_fee,String b2cCode ,String payment_type) {
		RealPalReqModel model = new RealPalReqModel();
		model.putValue("service", RealPalConstanst.REALPAL_SERVICE)
				.putValue("merchant_ID", RealPalConstanst.REALPAL_MERCHANT_ID)
				.putValue("notify_url", RealPalConstanst.REALPAL_NOTIFY_URL)
				.putValue("return_url", RealPalConstanst.REALPAL_RETURN_URL)
				.putValue("charset", RealPalConstanst.REALPAL_CHARSET)
				.putValue("title", RealPalConstanst.REALPAL_TITLE)
				.putValue("body", RealPalConstanst.REALPAL_BODY)
				.putValue("payment_type", payment_type)
				.putValue("paymethod", RealPalConstanst.REALPAL_PAYMETHOD)
				.putValue("defaultbank", b2cCode)
				.putValue("seller_email", RealPalConstanst.REALPAL_SELLER_EMAIL)
				.putValue("sign_type", RealPalConstanst.REALPAL_SIGN_TYPE)
				.putValue("order_no", order_no)
				.putValue("total_fee", total_fee).setSign();
		Logger.info("###request : %s\n", model.toString());
		return model;
	}
	/**
	 * 网关支付(查询)
	 */
	public static Map<String, Object> gateWayPaymentNo(String order_no,
			String trade_no) {
		RealPalReqModel model = new RealPalReqModel();
		model.putValue("merchant_ID", RealPalConstanst.REALPAL_MERCHANT_ID)
				.putValue("charset", RealPalConstanst.REALPAL_CHARSET)
				.putValue("sign_type", RealPalConstanst.REALPAL_SIGN_TYPE)
				.putValue("order_no", order_no)
				.putValue("trade_no", trade_no).setSign();
		Logger.info("###req : %s", model.toString());
		return model;
	}
	/**
	 * 快捷支付-储蓄卡签约接口
	 */
	public static JSONObject quickDebit(CashierRequest cashierRequest)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>(0);
		map.put("merchant_id", RealPalConstanst.QUICKDEBIT_MERCHANT_ID);
		map.put("card_no", cashierRequest.getCard_no());
		map.put("owner", cashierRequest.getOwner());
		map.put("cert_type", RealPalConstanst.QUICKDEBIT_CERT_TYPE);
		map.put("cert_no", cashierRequest.getCert_no());
		map.put("phone", cashierRequest.getPhone());
		map.put("order_no", cashierRequest.getOrder_no());
		map.put("transtime",
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		map.put("currency", RealPalConstanst.QUICKDEBIT_CURRENCY);
		map.put("title", RealPalConstanst.QUICKDEBIT_TITLE);
		map.put("body", RealPalConstanst.QUICKDEBIT_BODY);
		map.put("member_id", cashierRequest.getMember_id());
		map.put("terminal_type", RealPalConstanst.QUICKDEBIT_TERMINAL_TYPE);
		map.put("terminal_info", RealPalConstanst.QUICKDEBIT_TERMINAL_INFO);
		map.put("member_ip", cashierRequest.getMember_ip());
		map.put("seller_email", RealPalConstanst.QUICKDEBIT_SELLER_EMAIL);
		map.put("version", RealPalConstanst.QUICKDEBIT_VERSION);
		BigDecimal total_fee = new BigDecimal(cashierRequest.getTotal_fee()
				.toString()).movePointRight(2);
		map.put("total_fee", total_fee.toString());

		String mysign = ReapalUtil.BuildMysign(map, ReapalUtil.getKey());// 生成签名结果

		map.put("sign", mysign);

		String json = JSON.toJSONString(map);
        Logger.info("#######储蓄卡签约-验签之前  map参数####%s\n 生成签名结果:%s \n",map.toString(),mysign);
        
		Map<String, String> maps = ReapalUtil.addkey(json);
		maps.put("merchant_id", RealPalConstanst.QUICKDEBIT_MERCHANT_ID);
		
        Logger.info("#######maps请求参数####%s\n ",maps);

		String post = HttpClientUtil.post(RealPalConstanst.QUICKDEBIT_URL
				+ "/fast/debit/portal", maps);
		
		Logger.info("###储蓄卡签约订单号[%s]\n###返回结果[%s]",
				cashierRequest.getOrder_no(), post);
		
		if(StringUtils.isBlank(post)){
			JSONObject resultJson = new JSONObject();
			resultJson.put("result_code", -1);
			resultJson.put("result_msg", "网络异常，请稍后再试！");
			return resultJson;
		}
		
		String res = ReapalUtil.pubkey(post);
		JSONObject resultJson = JSONObject.fromObject(res);
		return resultJson;
	}
	
	/**
	 * 快捷支付接口
	 * @param order_no 订单号
	 * @param check_code 短信验证码
	 * @throws Exception
	 */
	//{"bank_card_type":"0","bank_name":"农业银行","card_last":"3179","merchant_id":"100000000009085","order_no":"1445060173124","phone":"18627540357","result_code":"0000","result_msg":"支付成功","trade_no":"101510170240453"}
	public static JSONObject quickPay(String order_no, String check_code) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>(0);
		
		map.put("merchant_id", RealPalConstanst.QUICKDEBIT_MERCHANT_ID);
		map.put("order_no", order_no);
		map.put("check_code", check_code);
		map.put("version", RealPalConstanst.QUICKDEBIT_VERSION);
		map.put("notify_url", RealPalConstanst.QUICKDEBIT_NOTIFY_URL);
		String mysign = ReapalUtil.BuildMysign(map, ReapalUtil.getKey());// 生成签名结果
		Logger.info("###quickPay - 验签之前 : [%s]", mysign);
		map.put("sign", mysign);
		
		String json = (String) JSON.toJSONString(map);
		Map<String, String> maps = ReapalUtil.addkey(json);
		maps.put("merchant_id", RealPalConstanst.QUICKDEBIT_MERCHANT_ID);
		
	    Logger.info("#######maps请求参数####%s\n ",maps);
	      
		String post = HttpClientUtil.post(ReapalUtil.getUrl() + "/fast/pay",
				maps);
		
		if(StringUtils.isBlank(post)){
			JSONObject resultJson = new JSONObject();
			resultJson.put("result_code", -1);
			resultJson.put("result_msg", "网络异常，请稍后再试！");
			return resultJson;
		}
		String res = ReapalUtil.pubkey(post);
		JSONObject resultJson = JSONObject.fromObject(res);
		return resultJson;
	}
	public static JSONObject quickPayDX(String order_no) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>(0);
		map.put("merchant_id", RealPalConstanst.QUICKDEBIT_MERCHANT_ID);
		map.put("order_no", order_no);
		map.put("version", RealPalConstanst.QUICKDEBIT_VERSION);
		String mysign = ReapalUtil.BuildMysign(map, ReapalUtil.getKey());// 生成签名结果
		Logger.info("###quickPay - 验签之前 : [%s]", mysign);
		map.put("sign", mysign);
		String json = (String) JSON.toJSONString(map);
		Map<String, String> maps = ReapalUtil.addkey(json);
		String post = HttpClientUtil.post(ReapalUtil.getUrl() + "/fast/sms",
				maps);
		if(!"".equals(post)&&post!=null){
			String res = ReapalUtil.pubkey(post);
			JSONObject resultJson = JSONObject.fromObject(res);
			return resultJson;
		}else{
			return null;
		}
	}
	/**
	 * 实名认证
	 * 
	 * @param userName
	 *            用户姓名
	 * @param userIdentity
	 *            用户身份证号
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static void auth(String userName, String userIdentity , ErrorInfo error) throws ClientProtocolException, IOException {
		error.clear();
		//防刷
		String ip = DataUtil.getRealIp();
	    t_user_auth_details auth = User.querryAuthRecords(ip,error);
		int value = User.jugeAuthCount(auth, ip, error);
		if(value < 0){
			return;
		}
		
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("version", RealPalConstanst.REALPAL_AUTH_VERSION);
		maps.put("service", RealPalConstanst.REALPAL_AUTH_SERVICE);
		maps.put("partner", RealPalConstanst.REALPAL_AUTH_PARTNER);
		maps.put("signType", RealPalConstanst.REALPAL_AUTH_SIGNTYPE);
		JSONObject reqJson = new JSONObject();
		reqJson.put("userName", userName);
		reqJson.put("userIdentity", userIdentity);
		reqJson.put("applyTime", getTimes(new Date())); 
		String reqDate = reqJson.toString();
		maps.put("reqData",reqDate);
		String sign = hexMD5(reqDate + RealPalConstanst.REALPAL_AUTH_MD5KEY);
		maps.put("sign", sign);
		String result = HttpClientUtil.post(RealPalConstanst.REALPAL_AUTH_URL, maps);
		Logger.info(
				"### auth  parameters : userName[%s], userIdentity[%s] ; -result : %s",
				userName, userIdentity, result);
		
	   Map<String,String> paramMap = getRespParams(result);
	   
	   error.clear();
	   if(StringUtils.isBlank(result)){
		     error.code = -1;
			 error.msg = "网络异常,请稍后再试！";
			 JPA.setRollbackOnly();
			 return;
	   }
	   if(paramMap.get("resData") !=null){
		   JSONObject  rspJson = JSONObject.fromObject(paramMap.get("resData"));
		   
		   Logger.info("#json: rspJson[%s]",rspJson);
		   if(rspJson.getString("resultCode").equals("0000")){
				error.code = 1;
		  }else{
			    error.code = -1;
			
		  }
			error.msg = rspJson.getString("resultMsg");
			return;
	   }
	   
	   if(paramMap.get("resError") !=null && !"".equals(paramMap.get("resError"))){
		   
			 JsonObject json = new JsonParser().parse(paramMap.get("resError")).getAsJsonObject();
			 Logger.info("#json: json[%s]",json);
			 String msg = json.get("errorMsg").getAsString();
	    	
	    	 error.code = -1;
			 error.msg = msg;
			 return;
	    }
	 
	}
	/**
	 * 获取实名认证参数map
	 * @param String 
	 * @return
	 */
	public static Map<String,String> getRespParams(String params){
		String reqparams = null;
		try {
		 
			//防止出现乱码
			reqparams = URLDecoder.decode(URLDecoder.decode(params,"UTF-8"),"UTF-8");
			
		} catch (UnsupportedEncodingException e1) {
			
			Logger.error("回调UrlDecode时 : %s " ,e1.getMessage());
		}
		
		Map<String,String> paramMap = null;
		if (null != reqparams) {
			paramMap = new HashMap();
			String param[] = reqparams.split("&");
			for (int i = 0; i < param.length; i++) {
				String content = param[i];
				String key = content.substring(0, content.indexOf("="));
				String value = content.substring(content.indexOf("=") + 1,
						content.length());
				try {
					paramMap.put(key, URLDecoder.decode(value,"UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					
					Logger.error("实名认证调构造参数UrlDecode时%s", e1.getMessage());
				}
			}
		}
		
		return paramMap;
	}
	
	/**
	 * 快捷支付查询
	 * @param order_no 订单号
	 * @throws Exception
	 */
	//{"result_code":"0000","timestamp":"2015-10-19 08:05:35","trade_no":"101510170240453","total_fee":150,"status":"completed"}
	public static JSONObject quickSearch(String order_no) throws Exception{
		Map<String, String> map = new HashMap<String, String>(0);
		map.put("merchant_id", RealPalConstanst.QUICKDEBIT_MERCHANT_ID);
		map.put("charset", RealPalConstanst.REALPAL_CHARSET);
		map.put("order_no", order_no);

		String mysign = ReapalUtil.BuildMysign(map, ReapalUtil.getKey());// 生成签名结果

		map.put("sign", mysign);

		String json = JSON.toJSONString(map);

		Map<String, String> maps = ReapalUtil.addkey(json);
		maps.put("merchant_id", RealPalConstanst.QUICKDEBIT_MERCHANT_ID);

		String post = HttpClientUtil.post(ReapalUtil.getUrl() + "/fast/search",
				maps);
		
		if(StringUtils.isBlank(post)){
			JSONObject resultJson = new JSONObject();
			resultJson.put("result_code", -1);
			resultJson.put("result_msg", "网络异常，请稍后再试！");
			return resultJson;
		}
		String res = ReapalUtil.pubkey(post);
		Logger.info("###融宝快捷支付查询返回结果->%s", res);
		JSONObject resultJson = JSONObject.fromObject(res);
		return resultJson;
	}

	private static String getTimes(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}

	public static String hexMD5(String value) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(value.getBytes("utf-8"));
			byte[] digest = messageDigest.digest();
			return byteToHexString(digest);
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.error("###融宝 - MD5加密异常:%s", ex.getMessage());
			return "";
		}
	}

	public static String byteToHexString(byte[] bytes) {
		return String.valueOf(Hex.encodeHex(bytes));
	}
	
	/**
	 * 悦园数据查询身份证接口
	 * @author zkai
	 * @param name
	 * @param idCard
	 * @param error
	 */
	public static void authentication(String realName,String idCard,ErrorInfo error) {
		
		error.clear();
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("appkey", Constants.YYSJ_APPKEY);
		maps.put("name", realName);
		maps.put("cardno", idCard);
		maps.put("output", "json");
		String result = "";
		
		try {
			result = WS.url(Constants.YYSJ_URL).setParameters(maps).post().getString();
//			result = HttpClientUtil.post(Constants.YYSJ_URL, maps);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("请求实名认证数据异常_zkai_403");
			error.code = -1;
			error.msg = "实名认证失败,请重新认证!";
			return;
		} 
		
		Logger.info("查询身份证接口:%s", result);
		
		try {
			result = URLDecoder.decode(URLDecoder.decode(result,"UTF-8"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Logger.info("请求实名认证数据异常_zkai_415");
			error.code = -1;
			error.msg = "实名认证失败,请重新认证!";
			return;
		}
		
		JSONObject json = JSONObject.fromObject(result);
		if(json == null || json.get("isok") == null || json.get("code") == null){
			Logger.info("请求实名认证数据异常_zkai_423");
			error.code = -1;
			error.msg = "实名认证失败,请重新认证!";
			return;
		}
		
		String code = json.get("code").toString();
		
		if("1".equals(json.get("isok").toString())){
			//查询成功
			if("1".equals(code)){
				error.code = 1;
				error.msg = "身份证合法";
				return;
			}else if("2".equals(code)){
				error.code = -100;
				error.msg = "身份证号码和真实姓名不符";
				return;
			}else{
				error.code = -100;
				error.msg = "身份证号码不正确";
				return;
			}
		}else{
			//查询失败
			if("12".equals(code)){
				Logger.info("查询身份证接口商户余额不足");
				/**
				 * 通知商户
				 */
				error.code = -1;
				error.msg = "实名认证失败,请重新认证!";
				return;
			}else{
				Logger.info("请求实名认证数据异常_zkai_457");
				error.code = -1;
				error.msg = "实名认证失败,请重新认证!";
				return;
			}
		}
	}

}
