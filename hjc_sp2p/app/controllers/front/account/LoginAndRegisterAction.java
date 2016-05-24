package controllers.front.account;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.t_users;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Scope;
import play.mvc.Scope.Session;
import play.mvc.With;
import utils.CaptchaUtil;
import utils.DateUtil;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.JSONUtils;
import utils.MobileUtil;
import utils.RegexUtils;
import utils.SMSUtil;
import utils.Security;
import annotation.RealNameCheck;
import business.BackstageSet;
import business.Bbin;
import business.BottomLinks;
import business.News;
import business.TemplateEmail;
import business.User;
import business.Wealthcircle;

import com.shove.security.Encrypt;

import constants.Constants;
import controllers.BaseController;
import controllers.DSecurity;
import controllers.front.home.HomeAction;

/**
 * 
 * @author liuwenhui
 * 
 */
@With(DSecurity.class)
public class LoginAndRegisterAction extends BaseController {

	/**
	 * 跳转到登录页面
	 */
	public static void login() {
//		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		String encryString = Session.current().getId();
		Integer  loginCount= (Integer)Cache.get("loginCount" + encryString);
		flash.put("loginCount", loginCount == null ? 0 : loginCount);
	    
		flash.put("parms", "0");
		
		HomeAction.home(); 
	}

	public static void logout() {
		User user = User.currUser();
		
		if (user == null) {
			flash.put("parms", "0");
			
			HomeAction.home(); 
		}

		ErrorInfo error = new ErrorInfo();

		user.logout(error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		flash.put("name",user.name); 
		
		flash.put("parms", "0");
		
		HomeAction.home(); 
	}

	/**
	 * 首页ajax登录
	 */
	public static void loginNow(String name,String password,String code ,String randomID) {
//		String encryString = Session.current().getId();
//		Integer loginCount = (Integer )Cache.get("loginCount" + encryString);
//		loginCount = loginCount == null ? 0 : loginCount;
//		Cache.set("loginCount"+encryString, ++loginCount, Constants.CACHE_TIME_MINUS_30);
//		
//		Logger.info("logincount=="+loginCount);
		business.BackstageSet  currBackstageSet = business.BackstageSet.getCurrentBackstageSet();
		Map<String,java.util.List<business.BottomLinks>> bottomLinks = business.BottomLinks.currentBottomlinks();
		   
		if(null != currBackstageSet){
		  Cache.delete("backstageSet");//清除系统设置缓存
		}
		   
		if(null != bottomLinks){
		  Cache.delete("bottomlinks");//清除底部连接缓存
		}
	   
		ErrorInfo error = new ErrorInfo();
        JSONObject json = new  JSONObject();
        
		flash.put("name", name);
		flash.put("password", password);
		flash.put("randomID", randomID);
		flash.put("code", code);
	
		
		if (StringUtils.isBlank(name)) {
			error.code=-1;
			error.msg="请输入用户名";
			
			json.put("error", error);
			renderJSON(json);
		}

		if (StringUtils.isBlank(password)) {
			error.code=-1;
			error.msg="请输入密码";
			
			json.put("error", error);
			renderJSON(json);
		}

		if (StringUtils.isBlank(code)) {
			error.code=-1;
			error.msg="请输入验证码";
			
			json.put("error", error);
			renderJSON(json);
		}

		if ( StringUtils.isBlank(randomID)) {
			error.code=-1;
			error.msg="请刷新验证码";
			
			json.put("error", error);
			renderJSON(json);
		}
		
		User user = new User();

		name = StringUtils.trimToEmpty(name);
		if(RegexUtils.isEmail(name)){
			user.findUserByEmail(name);
		}else if(RegexUtils.isMobileNum(name)){
			user.findUserByMobile(name);
		}else{
			user.name = name;
		}

		if (user.id < 0) {
			error.code=-1;
			error.msg="该用户名不存在";
			
			json.put("error", error);
			renderJSON(json);
		}
		
		if (Constants.CHECK_CODE) {
			
			if(!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
				error.code=-1;
				error.msg="验证码错误";
				
				json.put("error", error);
				renderJSON(json);
			}
		}
		
		

		if (user.login(password,false, Constants.CLIENT_PC, error) < 0) {
			json.put("error", error);
			renderJSON(json);
		}
		
//		flash.put("loginCount", ++loginCount);
//		Cache.delete("loginCount"+encryString);
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 登录
	 */
	public static void logining() {
		String encryString = Session.current().getId();
		Integer loginCount = (Integer )Cache.get("loginCount" + encryString);
		loginCount = loginCount == null ? 0 : loginCount;
		Cache.set("loginCount"+encryString, ++loginCount, Constants.CACHE_TIME_MINUS_30);
		loginCount = 1;
		
		business.BackstageSet  currBackstageSet = business.BackstageSet.getCurrentBackstageSet();
		Map<String,java.util.List<business.BottomLinks>> bottomLinks = business.BottomLinks.currentBottomlinks();
		   
		if(null != currBackstageSet){
		  Cache.delete("backstageSet");//清除系统设置缓存
		}
		   
		if(null != bottomLinks){
		  Cache.delete("bottomlinks");//清除底部连接缓存
		}
	   
		ErrorInfo error = new ErrorInfo();

		String name = params.get("name");
		String password = params.get("password");
		String code = params.get("code");
		String randomID = params.get("randomID");
		flash.put("name", name);
		flash.put("code", code);

		if (StringUtils.isBlank(name)) {
			flash.error("请输入用户名");
			
			flash.put("parms", "0");
			
			HomeAction.home(); 		
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");
			
			flash.put("parms", "0");
			
			HomeAction.home(); 
		}

		if (loginCount > 3 && StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			
			flash.put("parms", "0");
			
			HomeAction.home(); 		}

		if (loginCount > 3 && StringUtils.isBlank(randomID)) {
			flash.error("请刷新验证码");
			
			flash.put("parms", "0");
			
			HomeAction.home(); 		}

		if (Constants.CHECK_CODE) {
			
			if(loginCount > 3 && !code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
				flash.error("验证码错误");
				
				flash.put("parms", "0");
				
				HomeAction.home(); 
		  }
		}
		
		User user = new User();

		name = StringUtils.trimToEmpty(name);
		if(RegexUtils.isEmail(name)){
			user.findUserByEmail(name);
		}else if(RegexUtils.isMobileNum(name)){
			user.findUserByMobile(name);
		}else{
			user.name = name;
		}

		if (user.id < 0) {
			flash.error("该用户名不存在");
			flash.put("parms", "0");
			
			HomeAction.home(); 
		}
		
		

		if (user.login(password,false, Constants.CLIENT_PC, error) < 0) {
			flash.error(error.msg);
			
			flash.put("parms", "0");
			
			HomeAction.home(); 
		}
		
		Cache.delete("loginCount"+encryString);
		
		AccountHome.home();
	}

	/**
	 * 登录页面登录logining
	 */
	public static void topLogin() {
		ErrorInfo error = new ErrorInfo();

		String userName = params.get("name");
		String password = params.get("password");
		String code = params.get("code");
		String randomID = params.get("randomID");
		
		flash.put("name", userName);
		flash.put("code", code);

		if (StringUtils.isBlank(userName)) {
			flash.error("请输入用户名");
			flash.put("parms", "0");
			
			HomeAction.home(); 
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");
			flash.put("parms", "0");
			
			HomeAction.home(); 
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			flash.put("parms", "0");
			
			HomeAction.home(); 

		}

		if (StringUtils.isBlank(randomID)) {
			flash.error("请刷新验证码");
			flash.put("parms", "0");
			
			HomeAction.home(); 
		}

		if (!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
			flash.error("验证码错误");
			flash.put("parms", "0");
			
			HomeAction.home(); 
		}

		User user = new User();
		user.name = userName;

		if (user.login(password,false, Constants.CLIENT_PC, error) < 0) {
			flash.error(error.msg);
			flash.put("parms", "0");
			
			HomeAction.home(); 
		}
		AccountHome.home();
	}

	/**
	 * 用户注册协议
	 */
	public static void registerContent() {

		ErrorInfo error = new ErrorInfo();
		String content = News.queryContent(Constants.NewsTypeId.REGISTER_AGREEMENT2, error);
		JSONObject json = new JSONObject();
		error.code = 1;
		json.put("error", error);
        json.put("content", content);
		renderJSON(json);
	}
	/**
	 * 邮箱注册页面
     *un 推荐用户名称加密串
	 */
	public static void register(String un, String recommendUserName) {
		/*
		 * //		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
//
//		ErrorInfo error = new ErrorInfo();
//		String content = News.queryContent(Constants.NewsTypeId.REGISTER_AGREEMENT2, error);
		String name = "";
		String parms="";
		if(!StringUtils.isBlank(un)){
			name = Encrypt.decrypt3DES(un, Constants.ENCRYPTION_KEY);
			
		}
		
		if(!StringUtils.isBlank(recommendUserName)){
			name = recommendUserName;
			
		}
		if(StringUtils.isBlank(un)){
			parms = "0";
		}else{
			parms = "1";
		}
		flash.put("recommendUserName", name);
        flash.put("parms", parms);
        session.put("recommendUserName",name);
        session.put("parms",parms);
        render("@front.home.HomeAction.home",parms,name);
//        HomeAction.home();
 */
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;

		ErrorInfo error = new ErrorInfo();
		String content = News.queryContent(Constants.NewsTypeId.REGISTER_AGREEMENT2, error);
		String name = "";
		
		if(!StringUtils.isBlank(un)){
			name = Encrypt.decrypt3DES(un, Constants.ENCRYPTION_KEY);
			
		}
		
		if(!StringUtils.isBlank(recommendUserName)){
			name = recommendUserName;
			
		}
		
		flash.put("recommendUserName", name);

		render(loginOrRegister, content);
	}
	
	/**
	 * 手机注册页面
	 */
	public static void registerMobile(String un) {
//		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
//		
//		ErrorInfo error = new ErrorInfo();
//		String content = News.queryContent(Constants.NewsTypeId.REGISTER_AGREEMENT2, error);
		String recommendUserName = "";
		String name = "";
		if(!StringUtils.isBlank(un)){
			name = Encrypt.decrypt3DES(un, Constants.ENCRYPTION_KEY);
			
		}
		
		if(!StringUtils.isBlank(name)){
			recommendUserName = name;
			
		}
		
		flash.put("recommendUserName", recommendUserName);
		
		flash.put("parms", "1");
		
		HomeAction.home(); 
	}
	
	/**
	 * 获取验证码并返回页面
	 */
	public static void codeReturn(String codeImg) {
		String randomID = (String) Cache.get(codeImg);

		JSONObject json = new JSONObject();
		json.put("randomID", randomID);

		renderJSON(json);
	}

	/**
	 * 验证注册
	 * 
	 * @param t_users
	 */
	public static void registering() {
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();
		String name ="";
		String email ="";
		String mobile ="";
		String smsCode = "";
		String password ="";
		String password2 ="";
		String randomID ="";
		String code = "";
		String recommendUserName ="";
		String recommendName = "";
        String regist_type = params.get("regist_type");
     
		
        if(regist_type != null && regist_type !=""){
        	 name = params.get("mobile");
			 email = params.get("email1");
			
			if (null != email) {
				email = email.toLowerCase();
			}
			
			 mobile = params.get("mobile");
			 smsCode = params.get("smsCode");
			 password = params.get("password1");
			 password2 = params.get("password2");
			 randomID = (String) Cache.get(params.get("randomID1"));
			 code = params.get("code1");
			 recommendUserName = params.get("recommended1");
			 recommendName = params.get("recommendName1");
			flash.put("userName", name);
			flash.put("email", email);
			flash.put("mobile", mobile);
			flash.put("smsCode", smsCode);
			flash.put("recommendUserName", recommendUserName);
			flash.put("code", code);
        }else{
			 name = params.get("email");
			 email = params.get("email");
			
			if (null != email) {
				email = email.toLowerCase();
			}
			
			 mobile = params.get("mobile");
			 smsCode = params.get("smsCode");
			 password = params.get("password");
			 password2 = params.get("password3");
			 randomID = (String) Cache.get(params.get("randomID"));
			 code = params.get("code");
			 recommendUserName = params.get("recommended");
			 recommendName = params.get("recommendName");
			flash.put("userName", name);
			flash.put("email", email);
			flash.put("mobile", mobile);
			flash.put("smsCode", smsCode);
			flash.put("recommendUserName", recommendUserName);
			flash.put("code", code);
        }
		String NameForReccommend = "";
		
		if(!StringUtils.isBlank(recommendUserName)){
			NameForReccommend = recommendUserName;
		}
		
		if(!StringUtils.isBlank(recommendName)){
			NameForReccommend = recommendName;
		}
		
		boolean isMobileRegister = StringUtils.isNotBlank(mobile) ? true : false;
		
		if (StringUtils.isBlank(name)) {
			flash.error("请填写用户名");
			
			if (isMobileRegister) {
				registerMobile(NameForReccommend);
			} else {
				register("",NameForReccommend);
			}
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");

			if (isMobileRegister) {
				registerMobile(NameForReccommend);
				
			} else {
				register("",NameForReccommend);
			}
		}

		if (StringUtils.isBlank(password2) || (!password2.equals(password))) {
			flash.error("请确认密码");

			if (isMobileRegister) {
				registerMobile(NameForReccommend);
			} else {
				register("",NameForReccommend);
			}
		}
		
		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");

			if (isMobileRegister) {
				registerMobile(NameForReccommend);
				
			} else {
				register("",NameForReccommend);
			}
		}

		/*if (!RegexUtils.isValidUsername(name)) {
			flash.error("请填写符合要求的用户名");

			if (isMobileRegister) {
				registerMobile(NameForReccommend);
				
			} else {
				register("",NameForReccommend);
			}
		}*/

		if (isMobileRegister) {
			if (!RegexUtils.isMobileNum(mobile)) {
				flash.error("请填写正确的手机号码");
				registerMobile(NameForReccommend);
			}
			
			if (StringUtils.isBlank(smsCode)) {
				flash.error("请输入短信校验码");
				registerMobile(NameForReccommend);
			}
			
			if(Constants.CHECK_MSG_CODE) {
				String cCode = (String) Cache.get(mobile);
				
				if(cCode == null) {
					flash.error("短信校验码已失效，请重新点击发送校验码");
					registerMobile(NameForReccommend);
				}
				
				if(!smsCode.equals(cCode)) {
					flash.error("短信校验码错误");
					registerMobile(NameForReccommend);
				}
			}
			User.isMobileExist(mobile, null, error);

			if (error.code < 0) {
				flash.error(error.msg);
				registerMobile(NameForReccommend);
			}
		}
		
		if (!isMobileRegister) {
			
			if (!RegexUtils.isEmail(email)) {
				flash.error("请填写正确的邮箱地址");
				
				register("",NameForReccommend);
			}
			User.isEmailExist(email, null, error);

			if (error.code < 0) {
				flash.error(error.msg);
				register("",NameForReccommend);
			}
		}
		
		if (!RegexUtils.isValidPassword(password)) {
			flash.error("请填写符合要求的密码");

			if (isMobileRegister) {
				registerMobile(NameForReccommend);
			} else {
				register("",NameForReccommend);
			}
		}

		if(Constants.CHECK_CODE && !isMobileRegister){
			
			if(!code.equalsIgnoreCase(randomID)) {
				flash.error("图片校验码错误");
				register("",NameForReccommend);
			}
		}
		
		User.isNameExist(name, error);

		if (error.code < 0) {
			flash.error(error.msg);
			register("",NameForReccommend);
		}
		
		String recoName = "";
		
		if(StringUtils.isNotBlank(NameForReccommend)) {
			User.isNameExist(NameForReccommend, error);
			if (error.code == -2) {
				recoName = NameForReccommend;
		    }
		}
		
		if (isMobileRegister) {
			User.isMobileExist(email, null, error);
			
			if (error.code < 0) {
				flash.error(error.msg);
				registerMobile(NameForReccommend);
			}
		}
		if (StringUtils.startsWith(NameForReccommend, Constants.NORMAL_CODE_PREFIX)){
			recoName = NameForReccommend;
		}
		
		User user = new User();
		user.time = new Date();
		user.name = name;
		user.password = password;
		
		if (isMobileRegister) {
			user.mobile = mobile;
			user.isMobileVerified = true;
		}
		
		user.email = email;
		user.recommendUserName = recoName;
		user.register(Constants.CLIENT_PC, error);

		if (error.code < 0) {
			flash.error(error.msg);
			register("",recoName);
		}else{
			//注册成功送体验金
			Bbin bbin=new Bbin(user.getId(),Constants.BBIN_ACCOUNT,new Date(),true,0.0,0.0);
			bbin.presentedBbin();
		}
		//非邮箱注册
		if (!isMobileRegister) {
			TemplateEmail.activeEmail(user, error);  
		}
		
		//资金托管模式跳到开户页面
		if (Constants.IPS_ENABLE) {
			CheckAction.trustAccount();
		}else {
			AccountHome.home();
		}
		
		
		
	}
	
	/**
	 * 发送短信验证码
	 * @param mobile
	 */
	public static void sendSmsCode(String mobile,boolean send) {
		Logger.debug("\n类：%s\n方法：%s\n参数：%s", Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getMethodName(), JSONObject.fromObject(params.data));
		ErrorInfo error = new ErrorInfo();
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		if(StringUtils.isBlank(mobile) ) {
			error.code = -1;
			error.msg = "手机号码不能为空";
			
			renderJSON(error);
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "手机号格式有误";
			
			renderJSON(error);
		}
		
		if(User.isMobileExist(mobile, null, error) < 0){
			renderJSON(error);
		}
		if(send){
			if(backstageSet.is_jwconfirm ==1){
				SMSUtil.jwsendCode(mobile);	
			}else{
				SMSUtil.sendCode(mobile, error);
			}
			
		}
		
		renderJSON(error);
	}

	/**
	 * 生成验证码图片
	 * 
	 * @param id
	 */
	public static void getImg(String id) {
		Images.Captcha captcha = CaptchaUtil.CaptchaImage(id);
		if(captcha == null){
			return;
		}

		renderBinary(captcha);
	}

	/**
	 * 刷新验证码
	 */
	public static void setCode() {
		String randomID = CaptchaUtil.setCaptcha();

		JSONObject json = new JSONObject();
		json.put("img", randomID);
		renderJSON(json.toString());
	}

	/**
	 * 校验验证码
	 */
	public static void checkCode(String randomId, String code) {

		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();

		if (StringUtils.isBlank(code)) {
			error.code = -1;
			error.msg = "请输入验证码";

			json.put("error", error);
			renderJSON(json);
		}

		if (StringUtils.isBlank(randomId)) {
			error.code = -1;
			error.msg = "请刷新验证码";

			json.put("error", error);
			renderJSON(json);
		}

		String radomCode = CaptchaUtil.getCode(randomId);

		if (!code.equalsIgnoreCase(radomCode)) {
			error.code = -1;
			error.msg = "验证码错误";

			json.put("error", error);
			renderJSON(json);
		}

		json.put("error", error);
		renderJSON(json);
	}

	/**
	 * 验证邮箱是否已存在
	 * 
	 * @param email
	 */
	public static void hasEmailExist(String email) {
		ErrorInfo error = new ErrorInfo();
		
		email = email.toLowerCase();
		User.isEmailExist(email, null, error);

		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json.toString());

	}

	/**
	 * 验证用户名是否已存在
	 * 
	 * @param name
	 */
	public static void hasNameExist(String name) {
		ErrorInfo error = new ErrorInfo();
		User.isNameExist(name, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json.toString());  //code>0 用户名不存在。code<0用户名已存在或验证异常
	}
	
	/**
	 * 验证推荐人信息
	 * 
	 * @param name
	 */
	public static void checkRecommendName(String name) {
		ErrorInfo error = new ErrorInfo();
		
		//财富圈邀请码
		if (StringUtils.startsWith(name, Constants.NORMAL_CODE_PREFIX)){
			Wealthcircle.isInvitationExist(name, error);
			
			renderJSON(error);  //code==-2 验证通过。
		}
		
		//cps推广
		User.isNameExist(name, error);
		if(error.code != -2){
			
			renderJSON(error);  //用户名不存在或异常，不通过
		}
		
		//资金托管模式下，是否开户
//		String ipsAccpunt = User.queryIpsAcctNoByName(name, error);
//		if(Constants.IPS_ENABLE && StringUtils.isBlank(ipsAccpunt)){
//			error.code = -1;
//			error.msg = "该用户未开通托管账户";
//			
//			renderJSON(error);  //托管账户不存在或异常，不通过
//		}
		
		error.code = -2;  //验证通过
		
		renderJSON(error);
	}

	/**
	 * 验证手机号码是否已存在
	 * 
	 * @param name
	 */
	public static void hasMobileExist(String telephone) {
		ErrorInfo error = new ErrorInfo();

		int nameIsExist = User.isMobileExist(telephone, null, error);

		JSONObject json = new JSONObject();
		json.put("result", nameIsExist);

		renderJSON(json.toString());
	}

	/**
	 * 底部链接信息查询
	 * 
	 * @param name
	 */
	public static void buttomLinks(String num) {
		/* 初始化底部链接 */
		List<BottomLinks> result = BottomLinks.queryFrontBottomLinks(num);
		JSONObject json = new JSONObject();
		json.put("result", result);
		renderJSON(json.toString());
	}

	/**
	 * 激活帐号
	 * 
	 * @param sign
	 */
	public static void accountActivation(String sign) {
		ErrorInfo error = new ErrorInfo();

		long id = Security.checkSign(sign, Constants.ACTIVE, Constants.VALID_TIME, error);

		if (error.code < 0) {
			flash.error(error.msg);
			login();
		}

		User user = new User();
		user.id = id;

		user.activeEmail(error);
		
		if (Constants.IPS_ENABLE) {
			if (error.code < 0) {
				flash.error(error.msg);

				login();
			}
			
			
			if(StringUtils.isNotBlank(user.ipsAcctNo)) {
				user.logout(error);
				flash.error(error.msg);

				login();
			}
			
			User.setCurrUser(user);
			CheckAction.trustAccount();
		}

//		user.logout(error);
//		flash.error(error.msg);
		
		user.login(user.password,true, Constants.CLIENT_PC, error);
		if(error.code<0){
			flash.error("error", error.msg);
		}
		
	    if (StringUtils.isBlank(user.realityName)) {  //未实名认证
			
			BasicInformation.certification();
		}
	    
        AccountHome.home();
	}

	/**
	 * 通过邮箱找回用户名
	 */
	public static void findBackUsernameByEmail() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister);
	}

	/**
	 * 通过邮箱找回用户名后跳转到成功页面
	 */
	public static void emailSuccess() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;

		String email = flash.get("email");
		if (email == null) {

			login();
		}

		String emailUrl = EmailUtil.emailUrl(email);
		render(loginOrRegister, emailUrl);
	}

	/**
	 * 发送找回用户名邮件
	 */
	public static void saveUsernameByEmail(String email, String code,
			String randomID) {
		ErrorInfo error = new ErrorInfo();

		flash.put("email", email);

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			findBackUsernameByEmail();
		}

		if (StringUtils.isBlank(email)) {
			flash.error("请输入邮箱地址");
			findBackUsernameByEmail();
		}

		if (!RegexUtils.isEmail(email)) {
			flash.error("请输入正确的邮箱地址");
			findBackUsernameByEmail();
		}

		if (!code.equalsIgnoreCase(Cache.get(randomID).toString())) {
			flash.error("验证码错误");
			findBackUsernameByEmail();
		}

		User.isEmailExist(email, null , error);

		if (error.code != -2) {
			flash.error("对不起，该邮箱没有注册");
			findBackUsernameByEmail();
		}

		t_users user = User.queryUserByEmail(email, error);

		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByEmail();
		}

		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 1;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();

		String content = new String(tEmail.content);
		
		String url = Constants.LOGIN ;
		
		content = content.replace("<p","<div");
		content = content.replace("</p>","</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_EMAIL, email);
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(0, email, tEmail.title, content, error);

		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByEmail();
		}
		
		flash.error("邮件发送成功");
		flash.put("emailUrl", EmailUtil.emailUrl(email));

		login();
	}

	/**
	 * 通过手机找回用户名
	 */
	public static void findBackUsernameByTele() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister);
	}

	/**
	 * 通过手机找回用户名后跳转到成功页面
	 */
	public static void teleSuccess() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister);
	}

	/**
	 * 发送找回用户的短信
	 */
	public static void saveUsernameByTele(String mobile, String code,
			String randomID) {
		ErrorInfo error = new ErrorInfo();

		flash.put("mobile", mobile);
		flash.put("code", code);

		if (StringUtils.isBlank(mobile)) {
			flash.error("请输入手机号码");
			findBackUsernameByTele();
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			findBackUsernameByTele();
		}

		if(Cache.get(randomID) == null) {
			flash.error("请刷新验证码");
			findBackUsernameByTele();
		}

		if (StringUtils.isBlank(randomID)) {
			flash.error("请刷新验证码");
			findBackUsernameByTele();
		}

		if (!RegexUtils.isMobileNum(mobile)) {
			flash.error("请输入正确的手机号码");
			findBackUsernameByTele();
		}

		if (!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
			flash.error("验证码错误");
			findBackUsernameByTele();
		}

		User.isMobileExist(mobile, null, error);

		if (error.code != -2) {
			flash.error("该手机号码不存在或未绑定");
			findBackUsernameByTele();
		}

		MobileUtil.mobileFindUserName(mobile, error);

		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByTele();
		}
		
		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByTele();
		}

		flash.put("code", "");
		flash.error(error.msg);

		login();
	}

	/**
	 * 通过手机重置密码
	 */
	public static void resetPasswordByMobile() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		String randomId = Codec.UUID();
		boolean checkMsgCode = Constants.CHECK_PIC_CODE;
		
		render(loginOrRegister, randomId, checkMsgCode);
	}

	/**
	 * 保存重设的密码
	 */
	public static void savePasswordByMobile(String mobile, String code,
			String password, String confirmPassword, String randomIDD, String captcha) {
		ErrorInfo error = new ErrorInfo();
        
		User.updatePasswordByMobile(mobile, code, password, confirmPassword,
				randomIDD, captcha, error);

		if (error.code < 0) {
			flash.put("mobile", mobile);
			flash.put("code", code);
			flash.put("captcha", captcha);

			flash.error(error.msg);
			
			resetPasswordByMobile();
		}

		flash.error(error.msg);
		
		flash.put("parms", "0");
		HomeAction.home();
	}

	/**
	 * 通过邮箱重置密码
	 */
	public static void resetPasswordByEmail() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister);
	}

	/**
	 * 发送重置密码邮件
	 */
	public static void sendResetEmail(String email, String code, String randomID) {
		ErrorInfo error = new ErrorInfo();

		flash.put("email", email);

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			resetPasswordByEmail();
		}

		if (StringUtils.isBlank(email)) {
			flash.error("请输入邮箱地址");
			resetPasswordByEmail();
		}

		if (!RegexUtils.isEmail(email)) {
			flash.error("请输入正确的邮箱地址");
			resetPasswordByEmail();
		}

		if (!code.equalsIgnoreCase(Cache.get(randomID).toString())) {
			flash.error("验证码错误");
			resetPasswordByEmail();
		}

		User.isEmailExist(email, null, error);

		if (error.code != -2) {
			flash.error("对不起，该邮箱没有注册");
			resetPasswordByEmail();
		}
		
		t_users user = User.queryUserByEmail(email, error);

		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByEmail();
		}
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 3;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.PASSWORD);
		String url = Constants.RESET_PASSWORD_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace("<p","<div");
		content = content.replace("</p>","</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_LOGIN, "<a href = "+Constants.LOGIN+">登录</a>");
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(0, email, tEmail.title, content, error);

		if (error.code < 0) {
			flash.error(error.msg);
			resetPasswordByEmail();
		}
		
//		EmailUtil.emailFindUserName(email, error);
//
//		if (error.code < 0) {
//			flash.error("邮件发送失败，请重新发送");
//			resetPasswordByEmail();
//		}

		flash.put("email", "");
		flash.put("code", "");
		flash.error("邮件发送成功");
		flash.put("emailUrl", EmailUtil.emailUrl(email));

		flash.put("parms", "0");
		
		HomeAction.home();
	}
	
	/**
	 * 跳转到重置密码页面
	 */
	public static void resetPassword(String sign) {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			login();
		}
		
		String name = User.queryUserNameById(id, error);
		
		render(loginOrRegister, name,sign);
	}
	
	/**
	 * 保存重置密码
	 */
	public static void savePasswordByEmail(String sign, String password, String confirmPassword) {
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			flash.put("parms", "0");
			
			HomeAction.home();
		}
		
		User user = new User();
		user.id = id;
		user.updatePasswordByEmail(password, confirmPassword, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			resetPassword(sign);
		}
		
		flash.error(error.msg);
	    flash.put("parms", "0");
		
		HomeAction.home();
	}
	
	/**
	 * 发送手机校验码
	 * @param code
	 */
    public static void verifyMobile(String mobile, String captcha, String randomID) {
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();
        
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";

            json.put("error", error);

            renderJSON(json);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";

            json.put("error", error);

            renderJSON(json);
        }
        
        if (Constants.CHECK_PIC_CODE) {
			if (StringUtils.isBlank(captcha)) {
				
				error.code = -1;
				error.msg = "请输入图形验证码";
				
				json.put("error", error);
				
				renderJSON(json);
			}
			
			if (StringUtils.isBlank(randomID)) {
				
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        
	        if(StringUtils.isBlank(codec)){
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
	        }
	        
	        if (!codec.equalsIgnoreCase(captcha)) {
				
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
  		}
        
    	/**
    	 * 设置手机号码60S内无法重复发送验证短信
    	 */        
        if(Constants.CHECK_MSG_CODE){
        	Object cache = Cache.get("mobile");
        	if(null == cache){
        		Cache.set("mobile", mobile,Constants.CACHE_TIME_MINUS_2);
        	}else{
        		String isOldMobile = cache.toString();
        		if (isOldMobile.equals(mobile)) {
        			error.code = -1;
        			error.msg = "短信已发送";
        			renderJSON(error);
        		}
        	}
        }

        User user = User.currUser();

        if (user == null || StringUtils.isBlank(user.mobile) || !user.mobile.equals(mobile)) {
            User.isMobileExist(mobile, null, error);

            if (error.code != 0) {

                json.put("error", error);

                renderJSON(json);
            }
        }
        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		if(backstageSet.is_jwconfirm==1){
			SMSUtil.jwsendCode(mobile);
		}else{
			SMSUtil.sendCode(mobile, error);
		}
       // SMSUtil.sendCode(mobile, error); 

        json.put("error", error);

        renderJSON(json);
    }
    
    
    /**
     * <Description functions in a word>
     * 手机密码找回
     * <Detail description>
     * @author  ChenZhipeng
     * @param mobile
     * @param captcha
     * @param randomID [Parameters description]
     * @return void [Return type description]
     * @exception throws [Exception] [Exception description]
     * @see [Related classes#Related methods#Related properties]
     */
    public static void findPasswordByMobile(String mobile, String captcha, String randomID) {
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();
        
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";

            json.put("error", error);

            renderJSON(json);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";

            json.put("error", error);

            renderJSON(json);
        }
        
        if (Constants.CHECK_PIC_CODE) {
			if (StringUtils.isBlank(captcha)) {
				
				error.code = -1;
				error.msg = "请输入图形验证码";
				
				json.put("error", error);
				
				renderJSON(json);
			}
			
			if (StringUtils.isBlank(randomID)) {
				
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        if (!codec.equalsIgnoreCase(captcha)) {
				
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
  		}
        
    	/**
    	 * 设置手机号码60S内无法重复发送验证短信
    	 */        
        if(Constants.CHECK_MSG_CODE){
        	Object cache = Cache.get("mobile");
        	if(null == cache){
        		Cache.set("mobile", mobile,Constants.CACHE_TIME_MINUS_2);
        	}else{
        		String isOldMobile = cache.toString();
        		if (isOldMobile.equals(mobile)) {
        			error.code = -1;
        			error.msg = "短信已发送";
        			json.put("error", error);
        			renderJSON(json);
        		}
        	}
        }

        User user = User.currUser();

        if (user == null || StringUtils.isBlank(user.mobile) || !user.mobile.equals(mobile)) {
            User.isMobileExist(mobile, null, error);

            if (error.code != -2) {
            	error.code = -1;
            	error.msg= "手机号码不存在";
            	
                json.put("error", error);

                renderJSON(json);
            }
        }
        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		if(backstageSet.is_jwconfirm==1){
			SMSUtil.jwsendCode(mobile);
		}else{
			SMSUtil.sendCode(mobile, error);
		}
       // SMSUtil.sendCode(mobile, error);
        
        json.put("error", error);

        renderJSON(json);
    }
    
    /**
	 * APP手机号码注册页面
	 */
	public static void appReg() {
		render();
	}
	
	 /**
     * 注册成功
     */
    public static void zccg(){
    	render();
    }
	/**
	 * APP手机号码注册
	 * @param parameters
	 * @return
	 * @throws IOException 
	 */
	public static String appRegMobile(String mobile,String smsCode,String password,String recommendName) throws IOException {
		ErrorInfo error = new ErrorInfo();

		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", "-2");
		String name = mobile;
		if (StringUtils.isBlank(name)) {
			map.put("msg", "请填写用户名");
			return JSONUtils.printObject(map);
		}

		if (StringUtils.isBlank(password)) {
			map.put("msg", "请输入密码");
			return JSONUtils.printObject(map);
		}

		if (!RegexUtils.isValidUsername(name)) {
			map.put("msg", "请填写符合要求的用户名");
			return JSONUtils.printObject(map);
		}

		if (!RegexUtils.isMobileNum(mobile)) {
			map.put("msg", "请填写正确的手机号码");
			return JSONUtils.printObject(map);
		}
		
		if (StringUtils.isBlank(smsCode)) {
			map.put("msg", "请输入短信校验码");
			return JSONUtils.printObject(map);
		}
		
		if(Constants.CHECK_MSG_CODE) {
			String cCode = (String) Cache.get(mobile);
			
			if(cCode == null) {
				map.put("msg", "校验码已失效，请重新点击发送校验码");
				return JSONUtils.printObject(map);
			}
			
			if(!smsCode.equals(cCode)) {
				map.put("msg", "校验码错误");
				return JSONUtils.printObject(map);
			}
		}
		
		User.isMobileExist(mobile, null, error);

		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}
		
		if (!RegexUtils.isValidPassword(password)) {
			map.put("msg", "请填写符合要求的密码");
			return JSONUtils.printObject(map);
		}
		
		User.isMobileExist(mobile, null, error);

		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}
		
		User.isNameExist(name, error);

		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}
		
		String recoName = "";
		
		if(StringUtils.isNotBlank(recommendName)) {
			User.isNameExist(recommendName, error);
			if (error.code == -2) {
				recoName = recommendName;
		    }
		}
		
		User user = new User();
		user.time = new Date();
		user.name = name;
		user.password = password;
		user.mobile = mobile;
		user.isMobileVerified = true;
		user.recommendUserName = recoName;
		user.register(Constants.CLIENT_PC, error);
		
		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}else{
			//注册成功送体验金
			Bbin bbin=new Bbin(user.getId(),Constants.BBIN_ACCOUNT,new Date(),true,0.0,0.0);
			bbin.presentedBbin();
		}

		map.put("msg", "注册成功");
		map.put("code", "0");
		
		return JSONUtils.printObject(map);
	}
	
	
	/**
	 * 手机注册（用户名为手机号）
	 */
	public static String appRegMobile2(String mobile,String password2,String password) throws IOException {
		ErrorInfo error = new ErrorInfo();
		
		String name = mobile;
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", "-2");
		
		if (StringUtils.isBlank(name)) {
			map.put("msg", "请填写用户名");
			return JSONUtils.printObject(map);
		}

		if (StringUtils.isBlank(password)) {
			map.put("msg", "请输入密码");
			return JSONUtils.printObject(map);
		}
		 if(!password.equals(password2)){
			 map.put("msg", "两次输入密码不相同");
			return JSONUtils.printObject(map);
		 }
		

		if (!RegexUtils.isMobileNum(mobile)) {
			map.put("msg", "请填写正确的手机号码");
			return JSONUtils.printObject(map);
		}
		
		
		
		if(Constants.CHECK_MSG_CODE) {
			String cCode = (String) Cache.get(mobile);
			
			if(cCode == null) {
				map.put("msg", "校验码已失效，请重新点击发送校验码");
				return JSONUtils.printObject(map);
			}
		
		}
		
		User.isMobileExist(mobile, null, error);

		
		User.isNameExist(name, error);

		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}
		
	
		
		User user = new User();
		user.time = new Date();
		user.name = name;
		user.password = password;
		user.mobile = mobile;
		user.isMobileVerified = true;
		user.register2(Constants.CLIENT_PC, error);
		
		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}else{
			//注册成功送体验金
			Bbin bbin=new Bbin(user.getId(),Constants.BBIN_ACCOUNT,new Date(),true,0.0,0.0);
			bbin.presentedBbin();
		}

		map.put("msg", "注册成功");
		map.put("code", "0");
		return JSONUtils.printObject(map);
	}
	/**
	 * 发送手机校验码
	 * @param code
	 */
    public static void verifyMobileForReg(String mobile, String code, String randomID) {
        ErrorInfo error = new ErrorInfo();
        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";
            renderJSON(error);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";
            renderJSON(error);
        }
        
        if (Constants.CHECK_PIC_CODE) {
			if (StringUtils.isBlank(code)) {
				error.code = -1;
				error.msg = "请输入图形验证码";
				renderJSON(error);
			}
			
			if (StringUtils.isBlank(randomID)) {
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	renderJSON(error);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        if (!codec.equalsIgnoreCase(code)) {
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	renderJSON(error);
			}
  		}
    	
    	/**
    	 * 设置手机号码60S内无法重复发送验证短信
    	 */        
        if(Constants.CHECK_MSG_CODE){
        	
        	if(User.isMobileExist(mobile, null, error) < 0){
      			renderJSON(error);
      		}
        	
        	Object cache = Cache.get("mobile");
        	if(null == cache){
        		Cache.set("mobile", mobile,Constants.CACHE_TIME_MINUS_2);
        	}else{
        		String isOldMobile = cache.toString();
        		if (isOldMobile.equals(mobile)) {
        			error.code = -1;
        			error.msg = "短信已发送";
        			renderJSON(error);
        		}
        	}
        	if(backstageSet.is_jwconfirm==1){
        		SMSUtil.jwsendCode(mobile);	
    		}else{
    		    SMSUtil.sendCode(mobile, error);
    		}
        }
        
        renderJSON(error);
    }
    
    
    
    /**
     * 发送手机验证码不带验证码的
     */
 	public static void verifyMobileForReg2(String mobile,String smsCode) {
 		ErrorInfo error = new ErrorInfo();
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";
            renderJSON(error);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";
            renderJSON(error);
        }
        String cCode = (String) Cache.get(mobile);
        System.out.print(smsCode);
        System.out.print(cCode);
        if(!StringUtils.isBlank(cCode)){
        	if(!smsCode.equals(cCode)) {
    			error.code = -1;
    			error.msg = "短信验证码输入错误";
    			renderJSON(error);
    		}
        }
		
		renderJSON(error);
     }
     
 	 /**
     * App第二页
     */
    /*public static void szmm(){
    	String mobile = params.get("mobile");
    	flash.put("mobile",mobile);
    	render();
    }*/
 	public static void szmm(String mobile){
 		render(mobile);
 	}
	/**
	 * 检验邮件注册图形验证码
	 * @param code
	 * @param randomID
	 */
    public static void verifyEmailForReg(String code, String randomID) {
    	ErrorInfo error = new ErrorInfo();
    	
    	if (Constants.CHECK_PIC_CODE) {
			if (StringUtils.isBlank(code)) {
				error.code = -1;
				error.msg = "请输入图形验证码";
				renderJSON(error);
			}
			
			if (StringUtils.isBlank(randomID)) {
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	renderJSON(error);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        if (!codec.equalsIgnoreCase(code)) {
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	renderJSON(error);
			}
  		}
    	renderJSON(error);
    }
    

    /***
     * <Description functions in a word>
     * 短信校验码检验
     * <Detail description>
     * @author  ChenZhipeng
     * @param mobile 手机号码
     * @param smsCode 短信校验码
     * @return void [Return type description]
     * @exception throws [Exception] [Exception description]
     * @see [Related classes#Related methods#Related properties]
     */
    public static void verifySmsCodeForReg(String mobile, String smsCode) {
    	ErrorInfo error = new ErrorInfo();
    	
    	if (Constants.CHECK_MSG_CODE) {
	        String codec = (String) Cache.get(mobile);
	        if (!smsCode.equalsIgnoreCase(codec)) {
	        	error.code = -1;
	        	error.msg = "短信校验码输入错误";
	        	renderJSON(error);
			}
  		}
    	renderJSON(error);
    }
    
    
    /**
	 * 验证手机号码是否已存在
	 * 
	 * @param mobile
	 */
	public static void hasMobileExists(String mobile) {
		ErrorInfo error = new ErrorInfo();
		if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";
            renderJSON(error);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "手机号格式有误";
            renderJSON(error);
        }
		User.isMobileExist(mobile, null, error);
		renderJSON(error);
	}
	
}
