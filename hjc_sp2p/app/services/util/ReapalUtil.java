package services.util;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import play.Logger;
import play.Play;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class ReapalUtil {

	protected static String privateKey = Play.configuration.getProperty("quickDebit_privateKey");// 私钥
	protected static String password = Play.configuration.getProperty("quickDebit_password");// 密码
	protected static String key = Play.configuration.getProperty("quickDebit_user_key");// 用户key
	protected static String merchant_id = RealPalConstanst.QUICKDEBIT_MERCHANT_ID;// 商户ID
	protected static String pubKeyUrl = Play.configuration.getProperty("quickDebit_pubKeyUrl");// 公钥
	protected static String url = RealPalConstanst.QUICKDEBIT_URL;
	protected static String notify_url = RealPalConstanst.REALPAL_NOTIFY_URL;// 回调地址

	public static String getKey() {

		return key;
	}
	public static String getUrl(){
		return url;
	}
	public static String getNotify_url(){
		return notify_url;
	}
	 /**
     * 参数加密
     * @return
     */
    public static Map<String, String> addkey(String json)throws Exception{

    	Logger.info("###addkey-> 数据 [%s]", json);

        //商户获取融宝公钥
        PublicKey pubKeyFromCrt = RSA.getPubKeyFromCRT(pubKeyUrl);
        //随机生成16数字
        String key = RandomUtil.getRandom(16);
        Logger.info("###addkey-> 随机生成16位key [%s]", key);
        //对随机数进行加密
        String encryptKey = RSA.encrypt(key, pubKeyFromCrt);
        String encryData = AES.encryptToBase64(json, key);
        Logger.info("###addkey-> 密文key [%s]", encryptKey);
        Logger.info("###addkey-> 密文数据 [%s]", encryData);

        Map<String, String> map = new HashMap<String, String>();
        map.put("data", encryData);
        map.put("encryptkey", encryptKey);

        return map;
    }

    /**
     * 解密
     * @param post
     * @return
     * @throws Exception
     */
    public static String pubkey(String post)throws Exception{

    	Logger.info("###pubkey-> 密文 [%s]", post);
        // 将返回的json串转换为map

        TreeMap<String, String> map = JSON.parseObject(post,
                new TypeReference<TreeMap<String, String>>() {
                });
        String encryptkey = map.get("encryptkey");
        String data = map.get("data");
        
        Logger.info("###pubkey-> data [%s]", data);

        //获取自己私钥解密
        PrivateKey pvkformPfx = RSA.getPvkformPfx(privateKey, password);
        String decryptData = RSA.decrypt(encryptkey, pvkformPfx);

        post = AES.decryptFromBase64(data, decryptData);
        
        Logger.info("###pubkey-> 明文 [%s]", post);

        return post;
    }
    
    public static String getMapOrderStr(Map<String,Object> request){
        List<String> fieldNames = new ArrayList<String>(request.keySet());
        Collections.sort(fieldNames);
        StringBuffer buf = new StringBuffer();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = String.valueOf(request.get(fieldName));
            if ((fieldValue != null) && (fieldValue.length() > 0)){
                buf.append(fieldName+"="+fieldValue+"&");
            }
        }
        if(buf.length()>1) buf.setLength(buf.length()-1);
        return buf.toString(); //去掉最后&

    }



    /**
     * 生成订单号
     * @return
     */
    public static String no(){
        String code = "10" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "01" ;
        return code;
    }

    /**
     *
     * @param sArray
     * @param key
     * @return
     */
    public static String BuildMysign(Map sArray, String key) {
        if(sArray!=null && sArray.size()>0){
            StringBuilder prestr = CreateLinkString(sArray);
            Logger.info("###BuildMysign-> prestr [%s]", prestr);
            //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
            return Md5Encrypt.md5(prestr.toString()+key,"UTF-8");
        }
        return null;
    }

    /**
     * 拼装参数
     * @param params
     * @return
     */
    public static StringBuilder CreateLinkString(Map<String,String> params){
        List<String> keys = new ArrayList<String>(params.keySet());
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
}
