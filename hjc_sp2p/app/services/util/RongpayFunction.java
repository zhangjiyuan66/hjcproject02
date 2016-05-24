package services.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import play.Logger;
import utils.ErrorInfo;

/**
 *功能：融宝支付接口公用函数
 *详细：该页面是请求、通知返回两个文件所调用的公用函数核心处理文件，不需要修改
 *版本：1.0
 *修改日期：2012-05-01
 '说明：
 '以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 '该代码仅供学习和研究融宝支付接口使用，只是提供一个参考。
*/
public class RongpayFunction {
	
	/** 
	 * 功能：生成签名结果
	 * @param sArray 要签名的数组
	 * @param key 安全校验码
	 * @return 签名结果字符串
	 */
	public static String BuildMysign(Map sArray, String key) {
		if(sArray!=null && sArray.size()>0){
			StringBuilder prestr = CreateLinkString(sArray);  //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
			Logger.info("###MD5之前参数:%s", prestr);
			return Md5Encrypt.md5(prestr.append(key).toString());//把拼接后的字符串再与安全校验码直接连接起来,并且生成加密串
		}
		return null;
	}
	
	/** 
	 * 功能：把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
	 * @param params 需要排序并参与字符拼接的参数组
	 * @return 拼接后字符串
	 */
	public static StringBuilder CreateLinkString(Map params){
			List keys = new ArrayList(params.keySet());
			Collections.sort(keys);
	
			StringBuilder prestr = new StringBuilder();
			String key="";
			String value="";
			for (int i = 0; i < keys.size(); i++) {
				key=(String) keys.get(i);
				value = (String) params.get(key);
				if("".equals(value) || value == null || 
						key.equalsIgnoreCase("sign") || key.equalsIgnoreCase("sign_type")){
					continue;
				}
				prestr.append(key).append("=").append(value).append("&");
			}
			return prestr.deleteCharAt(prestr.length()-1);
	}
	
	/** 
	 * 功能：把字符串封装成map
	 * @param params 需要排序并参与字符拼接的字符串
	 * @return 拼接后字符串
	 * @throws UnsupportedEncodingException 
	 */
	public static  Map<String, Object> CreateLinkString(String params) throws UnsupportedEncodingException{
		  //融宝post过来的body url解码
		  params =URLDecoder.decode(params, "UTF-8");
	   	  Map<String, Object> maps = new HashMap<String, Object>(0); 
	  	  String []str = params.split("&");
	  	  for(int i=0;i<str.length;i++){
	  		  String [] str_index = str[i].split("=");
	  		  if(str_index.length>=2){
	  			 maps.put(str_index[0], str_index[1]);
	  		  }
	  		 
	  	  }
			return maps;
	}
	
	/**
	 * 将融宝支付POST过来反馈信息转换一下
	 * @param requestParams 返回参数信息
	 * @return Map 返回一个只有字符串值的MAP
	 * */
	public static Map transformRequestMap(Map requestParams){
		Map params = null;
		if(requestParams!=null && requestParams.size()>0){
			params = new HashMap();
			String name ="";
			String[] values =null;
			for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
				name= (String) iter.next();
				values= (String[]) requestParams.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				params.put(name, valueStr);
			}
		}
		return params;
	}
	
	/**
	* *功能：获取远程服务器ATN结果,验证返回URL
	* @param notify_id 通知校验ID
	* @return 服务器ATN结果
	* 验证结果集：
	* invalid命令参数不对 出现这个错误，请检测返回处理中merchant_ID和key是否为空 
	* true 返回正确信息
	* false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
	*/
	public static String Verify(String notify_id){
		
		//获取远程服务器ATN结果，验证是否是融宝支付服务器发来的请求
		String transport = RongpayConfig.transport;
		
		String merchant_ID = RongpayConfig.merchant_ID;
		
		StringBuilder veryfy_url = new StringBuilder();
		if(transport.equalsIgnoreCase("https")){
			veryfy_url.append(RongpayConfig.verify_https);
		} else{
			veryfy_url.append(RongpayConfig.verify_url);
		}
		veryfy_url.append("merchant_ID=").append(merchant_ID).append("&notify_id=").append(notify_id);
		System.out.println(veryfy_url);
		
		String responseTxt = CheckUrl(veryfy_url.toString());
		
		return responseTxt;

	}
	
	/**
	* *功能：获取远程服务器ATN结果
	* @param urlvalue 指定URL路径地址
	* @return 服务器ATN结果
	* 验证结果集：
	* invalid命令参数不对 出现这个错误，请检测返回处理中merchant_ID和key是否为空 
	* true 返回正确信息
	* false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
	*/
	private static String CheckUrl(String urlvalue){
		String inputLine = "";
		try {
			URL url = new URL(urlvalue);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			if(in!=null){
				inputLine = in.readLine().toString();
			}
			in.close();
			urlConnection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inputLine;
	}
	
	/**
	 * 功能：XML报文解析函数
	 * */
	public static HashMap GetMessage(String url){
		SAXReader reader = new SAXReader();
		Document doc=null;
		HashMap hm=null;
		try {
			InputStream in=new URL(url).openStream();
			if (in != null) {
				doc = reader.read(in);
				hm = new HashMap();
				Element root = doc.getRootElement();
				for (Iterator it = root.elementIterator(); it.hasNext();) {
					Element e = (Element) it.next();
					if (e.nodeCount() > 1) {
						HashMap hm1 = new HashMap();
						for (Iterator it1 = e.elementIterator(); it1.hasNext();) {
							Element e1 = (Element) it1.next();
							hm1.put(e1.getName(), e1.getText());
						}
						hm.put(e.getName(), hm1);
					} else {
						hm.put(e.getName(), e.getText());
					}
				}
			}
			doc.clearContent();
			in.close();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hm;
	}
	/**
	* *功能：验签
	*/
	public static int  checkSign(String bodyValue,String sign,ErrorInfo error){
		error.clear();
		Map<String, Object> maps1 = null;
		try {
			maps1 =CreateLinkString(bodyValue);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String mysign =BuildMysign(maps1,RealPalConstanst.REALPAL_MD5KEY);
		if(StringUtils.isBlank(mysign)){
			error.code = -1;
			error.msg = "同步验签失败,请查看是否支付成功，若没有支付成功，请联系客服!";
			return error.code;
		}
		if(!mysign.equals(sign)){
			error.code = -1;
			error.msg = "验签失败,支付失败!";
			return error.code;
		}
		
		error.code=0;
		return error.code;
	}
	
}
