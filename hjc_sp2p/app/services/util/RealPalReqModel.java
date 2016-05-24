package services.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import play.Logger;
import play.libs.Codec;

/**
 * 融宝支付请求Model
 * @author yangxuan
 * @date 2015年10月13日
 */
public class RealPalReqModel extends HashMap<String, Object> implements Serializable{
	
	public RealPalReqModel putValue(String key ,String value){
		put(key, value);
		return this;
	}
	
	public void setSign(){
		String mysign = RongpayFunction.BuildMysign(this, RealPalConstanst.REALPAL_MD5KEY);//生成签名结果
		Logger.info("###融宝网关支付数据加密后setSign###"+mysign);

		this.put("sign", mysign);
	}
}
