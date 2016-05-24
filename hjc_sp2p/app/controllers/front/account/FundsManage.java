package controllers.front.account;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.t_bbin;
import models.t_content_news;
import models.t_dict_audit_items;
import models.t_dict_payment_gateways;
import models.t_user_over_borrows;
import models.t_user_recharge_details;
import models.v_credit_levels;
import models.v_user_account_statistics;
import models.v_user_audit_items;
import models.v_user_detail_credit_score_audit_items;
import models.v_user_detail_credit_score_invest;
import models.v_user_detail_credit_score_loan;
import models.v_user_detail_credit_score_normal_repayment;
import models.v_user_detail_credit_score_overdue;
import models.v_user_detail_score;
import models.v_user_details;
import models.v_user_withdrawals;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import payment.PaymentProxy;
import payment.hf.util.HfConstants;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.mvc.With;
import services.util.RealPalConstanst;
import services.util.RongpayConfig;
import services.util.RongpayFunction;
import utils.CharUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.GopayUtils;
import utils.IDCardValidate;
import utils.JsonDateValueProcessor;
import utils.PageBean;
import utils.RegexUtils;
import utils.Security;
import utils.ServiceFee;
import annotation.InactiveUserCheck;
import annotation.RealNameCheck;
import annotation.SubmitCheck;
import annotation.SubmitOnly;
import business.AgentPayment;
import business.AuditItem;
import business.BackstageSet;
import business.Bbin;
import business.CreditLevel;
import business.DictBanksDate;
import business.News;
import business.Optimization.UserOZ;
import business.OverBorrow;
import business.User;
import business.UserAuditItem;
import business.UserBankAccounts;
import business.Vip;

import com.shove.Convert;

import constants.Constants;
import constants.Constants.RechargeType;
import constants.OptionKeys;
import controllers.BaseController;
import controllers.SubmitRepeat;
import controllers.interceptor.AccountInterceptor;
import controllers.wechat.account.WechatAccountHome;

@With({AccountInterceptor.class, SubmitRepeat.class})
public class FundsManage extends BaseController {
	
	//普通key
	private static final String NORMAL_KEY = "name";
	
	//普通value
	private static final String NORMAL_VALUE = "value";

	//-------------------------------资金管理-------------------------
	/**
	 * 账户信息
	 */
	@InactiveUserCheck
	@RealNameCheck
	public static void accountInformation(){
		User user = User.currUser();
		long userId = user.id;
		
		ErrorInfo error = new ErrorInfo();
		v_user_account_statistics accountStatistics = User.queryAccountStatistics(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		UserOZ accountInfo = new UserOZ(userId);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<v_user_details> userDetails = User.queryUserDetail(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<UserBankAccounts> userBanks = UserBankAccounts.queryUserAllBankAccount(userId);
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		
		List<t_content_news> news = News.queryNewForFront(Constants.NewsTypeId.MONEY_TIPS, 3,error);

		boolean isIps = Constants.IPS_ENABLE;
		Map<String,String> bankCodeNameTable = DictBanksDate.bankCodeNameTable;
		Map<String,String> provinceCodeNameTable = DictBanksDate.provinceCodeNameTable;
	    
		//总利息
        double sumInterest=User.profit(user.id,error);
        //推广费
        double sumPromotion= User.queryTotalCpsIncome(user.id);
        //佣金
        double sumyj = User.queryTotalCircleIncome(user.id);
        double total =sumPromotion+sumyj ;
      //根据用户id查询用户的体验金余额
        Bbin bb = new Bbin();
        boolean flag=  bb.queryid(user.id);
        t_bbin bb2 = null;
        if(flag == false){
        	
        }else{
            bb2 = bb.queryBbinforInvest(user.id, error);
        }
		render(bb2,user, accountStatistics, accountInfo, userDetails, userBanks, backstageSet, content, news, isIps,bankCodeNameTable,provinceCodeNameTable,sumInterest,total);
	}
	
	/**
	 * 添加银行账号
	 */
	public static void addBank(int addBankCode, int addProviceCode, int addCityCode, String addBranchBankName, String addAccount, String addAccountName){
		User user = User.currUser();

		ErrorInfo error = new ErrorInfo();
		
		String bankName = DictBanksDate.queryBankByCode(addBankCode);
		String provice = DictBanksDate.queryProvinceByCode(addProviceCode);
		String city = DictBanksDate.queryCityByCode(addCityCode);
		
		UserBankAccounts bankUser =  new UserBankAccounts();
		
		bankUser.userId = user.id;
		bankUser.bankName = bankName;
		bankUser.bankCode = addBankCode;
		bankUser.provinceCode = addProviceCode;
		bankUser.cityCode = addCityCode;
		bankUser.branchBankName = addBranchBankName;
		bankUser.province = provice;
		bankUser.city = city;
		bankUser.account = addAccount;
		bankUser.accountName = addAccountName;
		
		bankUser.addUserBankAccount(error, true);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	//保存银行账号
	public static void saveBank(){
		render();
	}
	
	/**
	 * 编辑银行账号
	 */

	public static void editBank(long editAccountId, int eidtBankCode, int eidtProviceCode, int eidtCityCode, String eidtBranchBankName, String editAccount, String editAccountName){
		ErrorInfo error = new ErrorInfo();
		
		String bankName = DictBanksDate.queryBankByCode(eidtBankCode);
		String provice = DictBanksDate.queryProvinceByCode(eidtProviceCode);
		String city = DictBanksDate.queryCityByCode(eidtCityCode);

		User user = User.currUser();
		UserBankAccounts userAccount = new UserBankAccounts();
		
		userAccount.bankName = bankName;
		userAccount.bankCode = eidtBankCode;
		userAccount.provinceCode = eidtProviceCode;
		userAccount.cityCode = eidtCityCode;
		userAccount.branchBankName = eidtBranchBankName;
		userAccount.province = provice;
		userAccount.city = city;
		userAccount.account = editAccount;
		userAccount.accountName = editAccountName;

		userAccount.editUserBankAccount(editAccountId, user.id, true, error);

		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 删除银行账号
	 */
	public static void deleteBank(long accountId){
		ErrorInfo error = new ErrorInfo();
		
		UserBankAccounts.deleteUserBankAccount(User.currUser().id, accountId, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 我的信用等级
	 */
	public static void myCredit(){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		v_user_detail_score creditScore = User.queryCreditScore(user.id);

		List<t_user_over_borrows> overBorrows = OverBorrow.queryUserOverBorrows(user.id, error);
		
		if(error.code < 0) {
			render(user, Constants.ERROR_PAGE_PATH_FRONT);
		}

//		double creditInitialAmount = BackstageSet.queryCreditInitialAmount();
		double creditInitialAmount = BackstageSet.getCurrentBackstageSet().initialAmount;
		
		
		render(user,creditScore,overBorrows,creditInitialAmount);
	}
	
	/**
	 * 信用积分明细(成功借款)
	 */
	public static void creditDetailLoan(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_loan> page = User.queryCreditDetailLoan(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(审核资料)
	 */
	public static void creditDetailAuditItem(String key, int currPage){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_audit_items> page = User.queryCreditDetailAuditItem(user.id, currPage, 0, key, error);
		
//		if(error.code < 0){
//			renderJSON(error);
//		}
		
		render(page);
	}
	
	/**
	 * 信用积分明细(成功投标)
	 */
	public static void creditDetailInvest(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_invest> page = User.queryCreditDetailInvest(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(正常还款)
	 * @param key
	 */
	public static void creditDetailRepayment(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_normal_repayment> page = User.queryCreditDetailRepayment(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(逾期扣分)
	 * @param key
	 */
	public static void creditDetailOverdue(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_overdue> page = User.queryCreditDetailOverdue(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 查看信用等级规则
	 */
	public static void viewCreditRule(){
		ErrorInfo error = new ErrorInfo();
		List<v_credit_levels> CreditLevels = CreditLevel.queryCreditLevelList(error);
		
		render(CreditLevels);
	}
	
	/**
	 * 查看信用积分规则
	 */
	public static void creditintegral(){
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		long auditItemCount = AuditItem.auditItemCount();
		
		ErrorInfo error = new ErrorInfo();

		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); 
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value); // 积分对应额度
		
		render(backstageSet, auditItemCount, amountKey);
	}
	
	/**
	 * 查看科目积分规则
	 */
	public static void creditItem(String key, int currPage){
		ErrorInfo error = new ErrorInfo();
		
		PageBean<t_dict_audit_items> page = AuditItem.queryEnableAuditItems(key, currPage, 0, error); // 审核资料
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); 
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value); // 积分对应额度
		
		render(page, amountKey);
	}
	
	/**
	 * 审核资料
	 */
	
	/**
	 * 审核资料积分明细（信用积分规则弹窗）
	 */
	public static void auditItemScore(String keyword, String currPage, String pageSize) {
		ErrorInfo error = new ErrorInfo();
		PageBean<AuditItem> page = AuditItem.queryAuditItems(currPage, pageSize, keyword, true, error);
		
		render(page, error);
	}
	
	//申请超额借款
	public static void applyOverBorrow(){
		render();
	}

	//提交申请
	public static void submitApply(){
		render();
	}
	
	/**
	 * 查看超额申请详情
	 */
	public static void viewOverBorrow(long overBorrowId){
		ErrorInfo error = new ErrorInfo();
		List<v_user_audit_items> auditItems = OverBorrow.queryAuditItems(overBorrowId, error);
		t_user_over_borrows overBorrows = OverBorrow.queryOverBorrowById(overBorrowId, error);
		render(overBorrows, auditItems);
	}
	
	/**
	 * 查看超额申请详情(IPS)
	 */
	public static void viewOverBorrowIps(long overBorrowId){
		ErrorInfo error = new ErrorInfo();
		List<v_user_audit_items> auditItems = OverBorrow.queryAuditItems(overBorrowId, error);
		t_user_over_borrows overBorrows = OverBorrow.queryOverBorrowById(overBorrowId, error);
		render(overBorrows, auditItems);
	}
	
	/**
	 * 提交资料
	 */  
	public static void userAuditItem(long overBorrowId, long useritemId, long auditItemId, String filename){
		
		ErrorInfo error = new ErrorInfo();

		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = User.currUser().id;
		item.id = useritemId;
		item.auditItemId = auditItemId;
		item.imageFileName = filename;
		item.overBorrowId = overBorrowId;
		item.createUserAuditItem(error);

		JSONObject json = new JSONObject();
		
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 充值
	 */
	@InactiveUserCheck
	@RealNameCheck
	@SubmitCheck
	public static void recharge(){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		int rechargeLowest = backstageSet.rechargeLowest; //最低充值金额
		int rechargeHighest = backstageSet.rechargeHighest; //最高充值金额
		if (Constants.IPS_ENABLE) {
			
			//是否需要选择银行，环讯，富友
			boolean isNeedSelectBank = false;
			List<Map<String, Object>> bankList = null;
			if(Constants.TRUST_FUNDS_TYPE.equals(Constants.TRUST_FUNDS_HX) 
					|| Constants.TRUST_FUNDS_TYPE.equals(Constants.TRUST_FUNDS_FY)){
				
				isNeedSelectBank = true;
				bankList = PaymentProxy.getInstance().queryBanks(error, 0);
			}
			
			//是否支持闪电快充功能,环讯不支持
			boolean isFastRecharge = true;
			if(Constants.TRUST_FUNDS_TYPE.equals(Constants.TRUST_FUNDS_HX)){
				isFastRecharge = false;
			}
			
			render("@front.account.FundsManage.rechargeIps",user, isNeedSelectBank, bankList, isFastRecharge, rechargeLowest,rechargeHighest);
		}
		
		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
		
		render(user, payType, rechargeLowest,rechargeHighest);
	}
	
	/**
	 * app充值
	 */
	public static void rechargeApp(){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		if (Constants.IPS_ENABLE) {
			
			List<Map<String, Object>> bankList = PaymentProxy.getInstance().queryBanks(error, Constants.APP); 
			
			render("@front.account.FundsManage.rechargeIps",user, bankList);
		}
		
		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
		
		render(user, payType);
	}
	
	/**
	 * app确认充值
	 */
	public static void submitRechargeApp(int type, double money, int bankType){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		if(!user.isEmailVerified){
			flash.error("尊敬的用户您好,您的邮箱未激活，请先激活邮箱");
			rechargeApp();
		}
		
		if (Constants.IPS_ENABLE) {
			String bankCode = params.get("bankCode");
			
			if (money <= 0) {
				flash.error("请输入正确的充值金额");
				rechargeApp();
			}
			
			if (StringUtils.isBlank(bankCode) || bankCode.equals("0")) {
				flash.error("请选择充值银行");
				rechargeApp();
			}
			PaymentProxy.getInstance().recharge(error, Constants.APP, null); 
		}
		
		flash.put("type", type);
		flash.put("money", money);
		flash.put("bankType",bankType);
		
		if(type<1 || type >2) {
			flash.error("请选择正确的充值方式");
			rechargeApp();
		}
		
		if(money == 0) {
			flash.error("请输入正确的充值金额");
			rechargeApp();
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.02")) < 0) {
			flash.error("请输入正确的充值金额");
			rechargeApp();
		}
		
		if(type == 2) {
			Map<String, String> args = User.ipay(moneyDecimal, bankType, RechargeType.Normal, Constants.RECHARGE_APP, Constants.CLIENT_APP, error);
			
			if(error.code < 0) {
				flash.error(error.msg);
				rechargeApp();
			}
			
			render("@front.account.FundsManage.submitRecharge",args);
		}
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, RechargeType.Normal, Constants.RECHARGE_APP, Constants.CLIENT_APP, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}
		
	}
	
	/**
	 * 支付vip，资料审核,投标奖励等服务费
	 */
	public static void rechargePay() {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
		
		Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay"+user.id);
		
		if(null == map) 
			renderText("请求过时或已提交!");
			
		double fee = (Double) map.get("fee");
		double amount = 0;
		boolean isPay = false;
		amount = user.balanceDetail.user_amount;
		
		render(user, payType, fee, amount, isPay);
	}
	
	/**
	 * 支付发标保证金
	 */
	@SubmitCheck
	public static void rechargePayIps(){
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		Map<String, Object> map = (Map<String, Object>)Cache.get("rechargePayIps"+user.id);
		
		if(null == map || map.size() == 0)
			renderText("请求超时!");
		
		double fee = (Double) map.get("fee");		
		List<Map<String, Object>> bankList = null; 
		bankList = PaymentProxy.getInstance().queryBanks(error, 0); 		
		render("@front.account.FundsManage.rechargePayIps",user, bankList, fee);
	}
	
	/**
	 * 确认充值
	 */
	@InactiveUserCheck
	@RealNameCheck
	@SubmitOnly
	public static void submitRecharge(int type, double money, int bankType){
		ErrorInfo error = new ErrorInfo();
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		if (Constants.IPS_ENABLE) {
			String bankCode = params.get("bankCode");
			String rechargeType = params.get("rechargeType");
			if(money < backstageSet.rechargeLowest){
				flash.error("最低充值" + backstageSet.rechargeLowest + "元");
				recharge();
			}
			
			if (money > Constants.MAX_VALUE) {
				flash.error("充值金额范围需在[" + backstageSet.rechargeLowest + "~" + Constants.MAX_VALUE + "]之间");
				recharge();
			}
			
			//需要选择银行:环讯托管、富友托管
			if ((Constants.TRUST_FUNDS_TYPE.equals(Constants.TRUST_FUNDS_HX) || Constants.TRUST_FUNDS_TYPE.equals(Constants.TRUST_FUNDS_FY)) 
					&& (StringUtils.isBlank(bankCode) || bankCode.equals("0"))) {
				
				flash.error("请选择充值银行");
				recharge();
			}
			
			//  闪电快充（代理充值）
			if ("1".equals(rechargeType)) {
				
				if((StringUtils.isBlank(bankCode) || bankCode.equals("0"))){
					flash.error("请选择充值银行");
					recharge();
				}
				
				AgentPayment.pay(bankCode, money, error);
				
				if(error.code < 0){
					return;
				}
			}

			PaymentProxy.getInstance().recharge(error, 0, money, bankCode);
			flash.error(error.msg);
			FundsManage.recharge();
		}
		
		flash.put("type", type);
		flash.put("money", money);
		flash.put("bankType",bankType);		
		
		if(type<1 || type >4) {
			flash.error("请选择正确的充值方式");
			recharge();
		}
		
		if(money < backstageSet.rechargeLowest){
			flash.error("最低充值" + backstageSet.rechargeLowest + "元");
			recharge();
		}
		
		if (money > Constants.MAX_VALUE) {
			flash.error("充值金额范围需在[" + backstageSet.rechargeLowest + "~" + Constants.MAX_VALUE + "]之间");
			recharge();
		}
		
	
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.02")) < 0) {
			flash.error("请输入正确的充值金额");
			recharge();
		}
		
		if(type == 2) {
			Map<String, String> args = User.ipay(moneyDecimal, bankType, RechargeType.Normal, Constants.RECHARGE_PC, Constants.CLIENT_PC, error);
			
			if(error.code < 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render(args);
		}
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, RechargeType.Normal, Constants.RECHARGE_PC, Constants.CLIENT_PC, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}
		
		//融宝网关支付
		if(type == 3) {
			
			if(bankType<0 || bankType> 17){
				error.code = -1;
				error.msg = "传入参数有误";
				recharge();
			}
			String b2cCode = getStrStatus(bankType);
			String  p_type = Constants.PAYMENT_TYPE;
			Map<String, Object> args = User.rbay(moneyDecimal, p_type, b2cCode ,RechargeType.Normal, Constants.RECHARGE_PC, Constants.CLIENT_PC, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				recharge();
			}
			String url=RealPalConstanst.REALPAL_URL;
			render("@front.account.FundsManage.submitRecharge3",args,url);
		}
		//融宝快捷支付
		if(type == 4) {
			
			String order_no=params.get("order_no");
			String check_code=params.get("check_code");
			if(StringUtils.isBlank(order_no)){
				error.msg="订单号不能为空";
				flash.error(error.msg);
				
				recharge();
			}
			if(StringUtils.isBlank(check_code)){
				error.msg="短信验证码不能为空";
				flash.error(error.msg);
				
				recharge();
			}
			
			JSONObject args = User.rbShortcutSB(order_no,check_code, RechargeType.Normal, Constants.RECHARGE_PC, Constants.CLIENT_PC, error);
			
			System.out.println(args.get("result_code") + "------------------" + args.get("order_no"));
			if("0000".equals(args.get("result_code"))){
				User.recharge(args.get("order_no")+"", money, error);
			}
			error.msg=args.get("result_msg")+"";
			flash.error(error.msg);
			
			dealRecord(0,null,null,0,0);
		}
	}
	public static  String getStrStatus(int num) {
		String str="";
		switch (num) {
			case Constants.ZERO: str = "ICBC"; break;
			case Constants.ONE: str = "CMB"; break;
			case Constants.TWO: str = "CCB"; break;
			case Constants.THREE: str ="ABC"; break;
			case Constants.FOUR: str = "BOC"; break;
			case Constants.FIVE: str = "SPDB"; break;
//			case Constants.SIX: str = "CMB"; break;
			case Constants.SEVEN: str = "BCCB"; break;
			case Constants.EIGHT: str = "CEB"; break;
			case Constants.NINE: str = "BOCM"; break; 
			case Constants.TEN: str = "CMBC"; break; 
			case Constants.ELEVN: str ="CITIC"; break;
			case Constants.TWEL: str = "GDB"; break;
			case Constants.THRTEEN:str ="SDB"; break;
			case Constants.FOURTEEN:str = "PSBC"; break;
//			case Constants.FIFTEEN: str = "SHBANK"; break;
			case Constants.SIXTEEN: str = "SHBANK"; break;
//			case Constants.SEVENTEEN: str = "HXB"; break;
			default : str = null; break;
		}
	
		return str;
	}
	
	
	/**
	 * 5.1 储蓄卡签约接口
	 * @param  card_no  银行账号
	 * @param  owner    用户姓名
	 * @param  cert_no  身份证号
	 * @param  phone    手机号
	 * @param  money    
	 */
	@InactiveUserCheck
	@RealNameCheck
	public static void kuaijie(String card_no,String owner,String cert_no,String phone,double money){
		ErrorInfo error = new ErrorInfo();
		
		if (!CharUtil.isChinese(owner)) {
			error.msg = "真实姓名必须是中文";
			flash.error(error.msg);
			recharge();
		}
		
		if(!"".equals(IDCardValidate.chekIdCard(0, cert_no))) {
			error.msg = "请输入正确的身份证号";
			flash.error(error.msg);
			recharge();
		}
		
		if(!RegexUtils.isMobileNum(phone)) {
			error.msg = "请输入正确的手机号";
			flash.error(error.msg);
			recharge();
		}
		if(money <= 0) {
			error.msg = "请输入充值金额";
			flash.error(error.msg);
			recharge();
		}
	
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		JSONObject args = User.rbShortcut(card_no,owner,cert_no,phone,moneyDecimal, Constants.RECHARGE_PC, Constants.CLIENT_PC, error);
		JSONObject json = new JSONObject();
		json.put("args", args);
		renderJSON(json.toString());
	}
	/**
	 * 5.5重新发送短信验证码接口
	 */
	@InactiveUserCheck
	@RealNameCheck
	public static void rbShortcutDX(String order_no){
		ErrorInfo error = new ErrorInfo();
		JSONObject args = User.rbShortcutDX(order_no,error);
		if(args==null){
			args = new JSONObject();
			args.put("result_code", 1);
			args.put("result_msg", "短信发送成功");
		}
		JSONObject json = new JSONObject();
		json.put("args", args);
		renderJSON(json.toString());
	}
	/**
	 * 融宝快捷支付回调
	 */
	public static void callbackRbSHSys(String res){
		ErrorInfo error = new ErrorInfo();
		JSONObject resultJson = JSONObject.fromObject(res);
		String order_no=resultJson.get("order_no").toString();
		
		String sql = "select user_id from t_user_recharge_details where pay_number = ? ";
		Object obj = null;
		try {
			obj = t_user_recharge_details.find(sql, order_no).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询用户ID出现错误!";
			
			return ;
		}
		
		if(null == obj) {
			error.code = -1;
			Logger.info("根据pay_number查询用户ID为null");
			
			return ;
		}
		String sql1 = "select amount from t_user_recharge_details where pay_number = ? and is_completed = 0 ";
		Object obj1 = null;
		
		try {
			obj1 = t_user_recharge_details.find(sql1, order_no).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询交易金额出现错误!";
			
			return ;
		}
		
		if(null == obj1) {
			error.code = -1;
			Logger.info("根据pay_number查询交易金额为null");
			
			return ;
		}

		String money = obj1.toString();
		long userId = Long.parseLong(obj.toString());
		User user = new User();
		user.id = userId;
		
		User.recharge(order_no, Double.parseDouble(money), error);
		
		if(error.code < 0) {
			return;
		}
	}
	
	/**
	 * 确认支付
	 */
	public static void submitRechargePay(int type, int bankType, boolean isUse){
		ErrorInfo error = new ErrorInfo();
		flash.put("type", type);
		flash.put("bankType",bankType);
		
		if(type<1 || type >2) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		User user = User.currUser();
		Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
		double fee = (Double) map.get("fee");
		int rechargeType = (Integer) map.get("rechargeType");
		double amount = 0;
		amount = user.balanceDetail.user_amount;
		double money = isUse ? (fee - amount) : fee;
		
		if(money <= 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.02")) < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		if(type == 2) {
			Map<String, String> args = User.ipay(moneyDecimal, bankType, rechargeType, Constants.RECHARGE_PC, Constants.CLIENT_PC, error);
			
			if(error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			render("@front.account.FundsManage.submitRecharge",args);
		}
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, rechargeType, Constants.RECHARGE_PC, Constants.CLIENT_PC, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				rechargePay();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}
		
	}
	/**
	 * 融宝网关回调
	 * @throws UnsupportedEncodingException 
	 */
	public static void callbackRbAy(){
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> maps = new HashMap<String, Object>(0);
		maps.put("body", params.get("body"));
		
		//验证参数
		maps.put("notify_id", params.get("notify_id"));
		maps.put("notify_time", params.get("notify_time"));
		maps.put("notify_type", params.get("notify_type"));
		//交易相关
		maps.put("trade_no", params.get("trade_no"));  //商户提交给融宝的订单号
		maps.put("order_no", params.get("order_no"));//商户提交给融宝的订单号
		maps.put("payment_type", params.get("payment_type"));//订单支付类型
		maps.put("title", params.get("title"));//商品描述
		maps.put("total_fee", params.get("total_fee"));//交易金额
		maps.put("trade_status", params.get("trade_status"));//交易状态枚举
		maps.put("seller_email", params.get("seller_email"));//卖家Email
		maps.put("seller_id", params.get("seller_id"));//卖家ID
		
		maps.put("gmt_create", params.get("gmt_create"));//该笔交易创建的时间
		maps.put("gmt_payment", params.get("gmt_payment"));//该交易买家的付款时间
		maps.put("is_success", params.get("is_success")); //该次操作是否成功
		maps.put("out_trade_no", params.get("out_trade_no"));
	
		maps.put("sign", params.get("sign"));		   //返回的body加密串
		maps.put("sign_type", params.get("sign_type"));//签名类型
		maps.put("subject", params.get("subject"));

		Logger.info("######返回参数列表：%s\n",maps.toString());
		//防重复
		int  num = User.repeatControl(maps.get("order_no").toString(),error);
		if(num < 0){
			
			flash.error(error.msg);
			dealRecord(0,null,null,0,0);
		}
		
		int signNum = RongpayFunction.checkSign(maps.get("body").toString(),maps.get("sign").toString(),error);
		if(signNum<0){
			flash.error(error.msg);
			dealRecord(0,null,null,0,0);
		}
		if(StringUtils.isBlank(maps.get("is_success").toString())){
			error.code = -1;
			error.msg = "支付失败,联系客服!";
			flash.error(error.msg);
			 dealRecord(0,null,null,0,0);
		}
	
		if(!"T".equals(maps.get("is_success").toString())){
			error.code = -1;
			error.msg = "支付失败,联系客服!";
			flash.error(error.msg);
			 dealRecord(0,null,null,0,0);
		}
		if("T".equals(maps.get("is_success").toString()) &&  "WAIT_BUYER_PAY".equals(maps.get("trade_status").toString()) ){
			error.code = -1;
			error.msg = "订单处理中";
			flash.error(error.msg);
			 dealRecord(0,null,null,0,0);
		}
		
		if("TRADE_FAILURE".equals(maps.get("trade_status").toString()) ){
			error.code = -1;
			error.msg = "支付失败,联系客服!";
			flash.error(error.msg);
			 dealRecord(0,null,null,0,0);
		}
		//对融宝返回的notify_id进行通知验证
		String result = RongpayFunction.Verify(params.get("notify_id"));
		
		if(StringUtils.isBlank(result)){
			Logger.info("######notify_id进行通知验证返回结果：%s",result);
			error.code = -1;
			error.msg = "支付失败,联系客服!";
			flash.error(error.msg);
			 dealRecord(0,null,null,0,0);
		}
		if("invalid".equals(result)){
			Logger.info("######notify_id进行通知验证返回结果：%s",result);
			error.code = -1;
			error.msg = "验证订单失败,支付失败!";
			flash.error(error.msg);
			 dealRecord(0,null,null,0,0);
		}
		if("false".equals(result)){
			Logger.info("######notify_id进行通知验证返回结果：%s",result);
			error.code = -1;
			error.msg = "验证订单失败,支付失败!";
			flash.error(error.msg);
			dealRecord(0,null,null,0,0);
		}
		
	
		String sql1 = "update t_user_recharge_details set order_no = ? where pay_number = ? and is_completed = 0";
		
		try{
			JPA.em().createQuery(sql1).setParameter(1, maps.get("trade_no").toString()).setParameter(2, maps.get("order_no").toString()).executeUpdate();
		}catch(Exception e) {
			JPA.setRollbackOnly();
			Logger.error("数据库异常:%s",e.getMessage());
			
			error.code = -1;
			error.msg = "数据库异常,请联系管理员";
			flash.error(error.msg);
			dealRecord(0,null,null,0,0);
		}
		
		User.recharge(maps.get("order_no").toString(), Double.parseDouble(maps.get("total_fee").toString()), error);
		flash.error(error.msg);
		dealRecord(0,null,null,0,0);
	
		
	}	
	/**
	 * 融宝网关异步回调
	 * @throws Exception 
	 */
	//14:54:47,643 INFO  ~ ###MD5之前参数:body=buyer_id=&trade_no=101511034469796&body=%E5%85%85%E5%80%BC&notify_time=2015-11-03+14%3A49%3A01&sign_type=MD5&is_total_fee_adjust=0&notify_type=WAIT_TRIGGER&trade_status=TRADE_FINISHED&gmt_payment=2015-11-03+14%3A49%3A01&order_no=0X120151103145249601111&discount=0.00&sign=e89d79e002536e760c775b78a77e7403&title=%E5%85%85%E5%80%BC&buyer_email=&gmt_create=2015-11-03+14%3A48%3A07&is_success=T&price=0.10&total_fee=0.10&seller_actions=SEND_GOODS&seller_id=100000000001486&quantity=1&notify_id=ab9be37bbe7b457eabb980aae264ad25&seller_email=604258288@qq.com&gmt_logistics_modify=2015-11-03+14%3A52%3A59&payment_type=1&gmt_create=2015-11-03 14:48:07&gmt_payment=2015-11-03 14:49:01&is_success=T&notify_id=ab9be37bbe7b457eabb980aae264ad25&notify_time=2015-11-03 14:49:01&notify_type=WAIT_TRIGGER&order_no=0X120151103145249601111&payment_type=1&seller_email=604258288@qq.com&seller_id=100000000001486&title=充值&total_fee=0.10&trade_no=101511034469796&trade_status=TRADE_FINISHED
	public static void callbackRbAySys(){
		
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> maps = new HashMap<String, Object>(0);
		maps.put("body", params.get("body"));
		
		//验证参数
		maps.put("notify_id", params.get("notify_id"));
		maps.put("notify_time", params.get("notify_time"));
		maps.put("notify_type", params.get("notify_type"));
		//交易相关
		maps.put("trade_no", params.get("trade_no"));  //商户提交给融宝的订单号
		maps.put("order_no", params.get("order_no"));//商户提交给融宝的订单号
		maps.put("payment_type", params.get("payment_type"));//订单支付类型
		maps.put("title", params.get("title"));//商品描述
		maps.put("total_fee", params.get("total_fee"));//交易金额
		maps.put("trade_status", params.get("trade_status"));//交易状态枚举
		maps.put("seller_email", params.get("seller_email"));//卖家Email
		maps.put("seller_id", params.get("seller_id"));//卖家ID
		
		maps.put("gmt_create", params.get("gmt_create"));//该笔交易创建的时间
		maps.put("gmt_payment", params.get("gmt_payment"));//该交易买家的付款时间
		maps.put("is_success", params.get("is_success")); //该次操作是否成功
		maps.put("out_trade_no", params.get("out_trade_no"));
	
		maps.put("sign", params.get("sign"));		   //返回的body加密串
		maps.put("sign_type", params.get("sign_type"));//签名类型
		maps.put("subject", params.get("subject"));

		Logger.info("######异步返回参数列表：%s\n",maps.toString());
		//防重复
		int  num = User.repeatControl(maps.get("order_no").toString(),error);
		if(num < 0){
			renderText("success");
		}
		int signNum = RongpayFunction.checkSign(maps.get("body").toString(),maps.get("sign").toString(),error);
		if(signNum<0){
			return;
		}
		
		if(StringUtils.isBlank(maps.get("is_success").toString())){
			error.code = -1;
			error.msg = "支付失败!";
			return;
		}
	
		if(!"T".equals(maps.get("is_success").toString())){
			error.code = -1;
			error.msg = "支付失败!";
			return;
		}
		if("T".equals(maps.get("is_success").toString()) &&  "WAIT_BUYER_PAY".equals(maps.get("trade_status").toString()) ){
			error.code = -1;
			error.msg = "异步订单处理中";
			return;
		}
		
		if("TRADE_FAILURE".equals(maps.get("trade_status").toString()) ){
			error.code = -1;
			error.msg = "支付失败";
			return;
		}
		//对融宝返回的notify_id进行通知验证
		String result = RongpayFunction.Verify(params.get("notify_id"));
		
		if(StringUtils.isBlank(result)){
			error.code = -1;
			error.msg = "异步验证URL返回参数出错!";
			return;
		}
		if("invalid".equals(result)){
			error.code = -1;
			error.msg = "异步验证返回URL参数出错!";
			return;
		}
		
		if("false".equals(result)){
			error.code = -1;
			error.msg = "异步验证返回URL失败!";
			return;
		}
		
		String sqls = "update t_user_recharge_details set order_no = ? where pay_number = ? and is_completed = 0";
		
		try{
			
			JPA.em().createQuery(sqls).setParameter(1, maps.get("trade_no").toString()).setParameter(2, maps.get("order_no").toString()).executeUpdate();
		}catch(Exception e) {
			
			JPA.setRollbackOnly();
			Logger.error("###更新平台交易记录在融宝的交易流水号时出错"+e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			renderText("fail");
		}
		
//		long userId = Long.parseLong(obj.toString());
//		User user = new User();
//		user.id = userId;
//		
		User.recharge(maps.get("order_no").toString(), Double.parseDouble(maps.get("total_fee").toString()), error);
		
		if(error.code < 0) {
			return;
		}
		
		renderText("success");
	
	}
	/**
	 * 环迅回调
	 */
	public static void callback(String billno, String mercode, String Currency_type, String amount, String date, String succ,
			String msg, String attach, String ipsbillno, String retencodetype, String signature) {
		ErrorInfo error = new ErrorInfo();
		
		//返回订单加密的明文:billno+【订单编号】+currencytype+【币种】+amount+【订单金额】+date+【订单日期】+succ+【成功标志】+ipsbillno+【IPS订单编号】+retencodetype +【交易返回签名方式】+【商户内部证书】 
		String content="billno"+billno + "currencytype"+Currency_type+"amount"+amount+"date"+date+"succ"+succ+"ipsbillno"+ipsbillno+"retencodetype"+retencodetype;  //明文：订单编号+订单金额+订单日期+成功标志+IPS订单编号+币种
		boolean verify = false;

		//验证方式：16-md5withRSA  17-md5
		if(retencodetype.equals("16")) {
			cryptix.jce.provider.MD5WithRSA a=new cryptix.jce.provider.MD5WithRSA();
			a.verifysignature(content, signature, "D:\\software\\publickey.txt");

			//Md5withRSA验证返回代码含义
			//-99 未处理
			//-1 公钥路径错
			//-2 公钥路径为空
			//-3 读取公钥失败
			//-4 验证失败，格式错误
			//1： 验证失败
			//0: 成功
			if (a.getresult() == 0){
				verify = true;
			}	
		} else if(retencodetype.equals("17")) {
			User.validSign(content, signature, error);
			
			if(error.code == 0) {
				verify = true;
			}
		}
		String info = "";
		if(!verify) {
			info = "验证失败";
			render(info);
		}
		
		if (succ == null) {
			info = "交易失败";
			render(info);
		}
		
		if(!succ.equalsIgnoreCase("Y")) {
			info = "交易失败";
			render(info);
		} 
		
		User.recharge(billno, Double.parseDouble(amount), error);
		int rechargeType = Convert.strToInt(billno.split("X")[0], RechargeType.Normal);
		
		if(error.code < 0 && error.code != Constants.ALREADY_RUN) {
			flash.error(error.msg);
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}		
		User user = User.currUser();		
		if (Constants.IPS_ENABLE) {
			
			if (rechargeType == RechargeType.VIP) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请vip成功！");
					
					AccountHome.home();
				}				
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请vip成功！");
				}			
				Cache.delete("rechargePay" + user.id);			
				AccountHome.home();
			}
		} else {	
			
			//申请vip
			if (rechargeType == RechargeType.VIP) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请vip成功！");
					
					AccountHome.home();
				}
				
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请vip成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("提交成功！");
					
					AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
				}
				
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("提交成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请超额借款成功！");
					
					AccountHome.home();
				}
				
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请超额借款成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		}
		 
		if(error.code < 0) {
			if (Constants.RECHARGE_WECHAT.equals(attach)) {
				WechatAccountHome.recharge();

			}else if(Constants.RECHARGE_APP.equals(attach)) {
				rechargeApp();
				
			}else {
				info = error.msg;
				int code = error.code;
				render(info, code);	
			}
		}
		
		if(Constants.RECHARGE_APP.equals(attach)) {
			rechargeApp();
		}
		
		if(Constants.RECHARGE_WECHAT.equals(attach)) {
			WechatAccountHome.recharge();
		}
		
		info = "交易成功";
		render(info);
	}	
	
	/**
	 * 环迅回调（异步）
	 */
	public static void callbackSys(String billno, String mercode, String Currency_type, String amount, String date, String succ,
			String msg, String attach, String ipsbillno, String retencodetype, String signature) {
		ErrorInfo error = new ErrorInfo();
		
		//返回订单加密的明文:billno+【订单编号】+currencytype+【币种】+amount+【订单金额】+date+【订单日期】+succ+【成功标志】+ipsbillno+【IPS订单编号】+retencodetype +【交易返回签名方式】+【商户内部证书】 
		String content="billno"+billno + "currencytype"+Currency_type+"amount"+amount+"date"+date+"succ"+succ+"ipsbillno"+ipsbillno+"retencodetype"+retencodetype;  //明文：订单编号+订单金额+订单日期+成功标志+IPS订单编号+币种

		boolean verify = false;

		//验证方式：16-md5withRSA  17-md5
		if(retencodetype.equals("16")) {
			cryptix.jce.provider.MD5WithRSA a=new cryptix.jce.provider.MD5WithRSA();
			a.verifysignature(content, signature, "D:\\software\\publickey.txt");

			//Md5withRSA验证返回代码含义
			//-99 未处理
			//-1 公钥路径错
			//-2 公钥路径为空
			//-3 读取公钥失败
			//-4 验证失败，格式错误
			//1： 验证失败
			//0: 成功
			if (a.getresult() == 0){
				verify = true;
			}	
		} else if(retencodetype.equals("17")) {
			User.validSign(content, signature, error);
			
			if(error.code == 0) {
				verify = true;
			}
		}
		String info = "";
		if(!verify) {
			info = "验证失败";
			render(info);
		}
		
		if (succ == null) {
			info = "交易失败";
			render(info);
		}
		
		if(!succ.equalsIgnoreCase("Y")) {
			info = "交易失败";
			render(info);
		} 
		
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;
		
		try {
			obj = t_user_recharge_details.find(sql, billno).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询用户ID出现错误!";
			
			return ;
		}
		
		if(null == obj) {
			error.code = -1;
			Logger.info("根据pay_number查询用户ID为null");
			
			return ;
		}

		long userId = Long.parseLong(obj.toString());
		User user = new User();
		user.id = userId;
		
		User.recharge(billno, Double.parseDouble(amount), error);
		
		if(error.code < 0) {
			return;
		}

	}
	
	/**
	 * 国付宝回调
	 */
	public static void gCallback(String version,String charset,String language,String signType,String tranCode
			,String merchantID,String merOrderNum,String tranAmt,String feeAmt,String frontMerUrl,String backgroundMerUrl
			,String tranDateTime,String tranIP,String respCode,String msgExt,String orderId
			,String gopayOutOrderId,String bankCode,String tranFinishTime,String merRemark1,String merRemark2,String signValue) {
		ErrorInfo error = new ErrorInfo();
		String info = "";
		
		t_dict_payment_gateways gateway = User.gateway(Constants.GO_GATEWAY, error);
		
		if(GopayUtils.validateSign(version,tranCode, merchantID, merOrderNum,
	    		tranAmt, feeAmt, tranDateTime, frontMerUrl, backgroundMerUrl,
	    		orderId, gopayOutOrderId, tranIP, respCode,gateway._key, signValue)) {
			
			info = "验证失败，支付失败！";
			render(info);
		}
		
		Logger.info("respCode:"+respCode);
		
		if (!"0000".equals(respCode) && !"9999".equals(respCode)) {
			info = "支付失败！";
			render(info);
		}
		
		if ("9999".equals(respCode)) {
			info = "订单处理中，请耐心等待！";
			render(info);
		}
		
		User.recharge(merOrderNum, Double.parseDouble(tranAmt), error);
		int rechargeType = Convert.strToInt(merOrderNum.split("X")[0], RechargeType.Normal);
		
		if(error.code < 0 && error.code != Constants.ALREADY_RUN) {
			flash.error(error.msg);
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		User user = User.currUser();
		
		if (Constants.IPS_ENABLE) {
			if (rechargeType == RechargeType.VIP) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请vip成功！");
					
					AccountHome.home();
				}
				
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请vip成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}						
		} else {
			
			if (rechargeType == RechargeType.VIP) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请vip成功！");
					
					AccountHome.home();
				}
				
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请vip成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("提交成功！");
					
					AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
				}
				
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("提交成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请超额借款成功！");
					
					AccountHome.home();
				}			
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请超额借款成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		}		 
		if(error.code < 0) {
			 flash.error(error.msg);
			 if (Constants.RECHARGE_WECHAT.equals(merRemark1)) {
					WechatAccountHome.recharge();

				}else if(Constants.RECHARGE_APP.equals(merRemark1)) {
					rechargeApp();
					
				}else {

					render(error);
				}
		}
		
		if(Constants.RECHARGE_APP.equals(merRemark1)) {
			rechargeApp();
		}
		
		if(Constants.RECHARGE_WECHAT.equals(merRemark1)) {
			WechatAccountHome.recharge();
		}
		
		info = "交易成功";
		render(info);
	}
	
	/**
	 * 国付宝回调（异步）
	 */
	public static void gCallbackSys(String version,String charset,String language,String signType,String tranCode
			,String merchantID,String merOrderNum,String tranAmt,String feeAmt,String frontMerUrl,String backgroundMerUrl
			,String tranDateTime,String tranIP,String respCode,String msgExt,String orderId
			,String gopayOutOrderId,String bankCode,String tranFinishTime,String merRemark1,String merRemark2,String signValue) {
		ErrorInfo error = new ErrorInfo();
		t_dict_payment_gateways gateway = User.gateway(Constants.GO_GATEWAY, error);
		
		if(GopayUtils.validateSign(version,tranCode, merchantID, merOrderNum,
	    		tranAmt, feeAmt, tranDateTime, frontMerUrl, backgroundMerUrl,
	    		orderId, gopayOutOrderId, tranIP, respCode,gateway._key, signValue)) {
			Logger.info("---------------验证失败，支付失败！------------");
			return ;
		}
		
		Logger.info("respCode:"+respCode);
		
		if (!"0000".equals(respCode) && !"9999".equals(respCode)) {
			Logger.info("---------------支付失败！------------");
			return ;
		}
		
		if ("9999".equals(respCode)) {
			Logger.info("---------------订单处理中，请耐心等待！------------");
			return ;
		}
		
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;
		
		try {
			obj = t_user_recharge_details.find(sql, merOrderNum).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询用户ID出现错误!";
			
			return ;
		}
		
		if(null == obj) {
			error.code = -1;
			Logger.info("根据pay_number查询用户ID为null");
			
			return ;
		}

		long userId = Long.parseLong(obj.toString());
		User user = new User();
		user.id = userId;
		
		User.recharge(merOrderNum, Double.parseDouble(tranAmt), error);
		
		if(error.code < 0) {
			return;
		}
		
		int rechargeType = Convert.strToInt(merOrderNum.split("X")[0], RechargeType.Normal);
		
		if (Constants.IPS_ENABLE) {
			if (rechargeType == RechargeType.VIP) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}	
			if (rechargeType == RechargeType.UploadItemsOB) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		} else {
						
			if (rechargeType == RechargeType.VIP) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		}
	}
	
	/**
	 * 提现
	 */
	@InactiveUserCheck
	@RealNameCheck
	@SubmitCheck
	public static void withdrawal(){
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		
		//add 是否绑定银行卡判断	begin
		boolean bindedBankCard = true;
		if(Constants.IPS_ENABLE){
			Map<String, Object> map = PaymentProxy.getInstance().queryBindedBankCard(error, Constants.PC, user.ipsAcctNo);
			if(map != null && StringUtils.isBlank(map.get("result").toString())){
				bindedBankCard = false;  //未绑卡
			}
		}
		//add 是否绑定银行卡判断	end		version:8.0.2
		
		String type = params.get("type");
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String beginTime = params.get("startDate");
		String endTime = params.get("endDate");
		
		double amount = User.queryRechargeIn(user.id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		double withdrawalAmount = user.balance - amount;
		
		if (!Constants.IS_WITHDRAWAL_INNER || (!Constants.IPS_ENABLE && Constants.WITHDRAWAL_PAY_TYPE == Constants.ONE)) {
			
			withdrawalAmount = ServiceFee.maxWithdralAmount(withdrawalAmount);
		}
		
		//资金托管模式下平台账户可提现金额
		double withdrawalAmount2 = ServiceFee.maxWithdralAmount(user.balance);
		
		//最多提现金额上限
		double maxWithDrawalAmount = Constants.MAX_TIXIAN_VALUE;
		
		if(withdrawalAmount < 0) {
			withdrawalAmount = 0;
		}
		
		List<UserBankAccounts> banks = UserBankAccounts.queryUserAllBankAccount(user.id);
		
		PageBean<v_user_withdrawals> page = User.queryWithdrawalRecord(user.id, type, 
				beginTime, endTime, currPage, pageSize, error);
		boolean ipsEnable = Constants.IPS_ENABLE;
		
		render(user, withdrawalAmount, maxWithDrawalAmount, withdrawalAmount2, banks, page, ipsEnable, bindedBankCard);
	}
	
	/**
	 * 根据选择的银行卡id查询其信息
	 */
	public static void QueryBankInfo(long id){
		JSONObject json = new JSONObject();
		
		UserBankAccounts bank = new UserBankAccounts();
		bank.setId(id);
		
		json.put("bank", bank);
		
		renderJSON(json);
	}
	
	
//	/**
//	 * 提现记录
//	 */
//	public static void withdrawalRecord() {
//		User user = User.currUser();
//		
//		String type = params.get("type");
//		String currPage = params.get("currPage");
//		String pageSize = params.get("pageSize");
//		String beginTime = params.get("startDate");
//		String endTime = params.get("endDate");
//		
//		ErrorInfo error = new ErrorInfo();
//		PageBean<v_user_withdrawals> page = User.queryWithdrawalRecord(user.id, type, 
//				beginTime, endTime, currPage, pageSize, error);
//		
//		render(page);
//	}
	
	//申请提现
	public static void applyWithdrawal(){
		render();
	}
	
	//申请提现
	public static void ipsWithDrawApply(){
		render();
	}
	
	/**
	 * 确认提现
	 */
	@SubmitOnly
	public static void submitWithdrawal(double amount, long bankId, String payPassword, int type, String ipsSelect){
		ErrorInfo error = new ErrorInfo();
		boolean flag = false;
		
		if(StringUtils.isNotBlank(ipsSelect) && ipsSelect.equals("1")) {
			flag = true;
		}
		
		if(amount <= 0) {
			flash.error("请输入提现金额");
			
			withdrawal();
		}
		
		if(amount > Constants.MAX_VALUE) {
			flash.error("已超过最大充值金额" +Constants.MAX_VALUE+ "元");
			
			withdrawal();
		}
		if(amount < 200) {
			flash.error("提现金额不能小于200");
			
			withdrawal();
		}
		if (!(Constants.IPS_ENABLE && flag)) {
			if(StringUtils.isBlank(payPassword)) {
				flash.error("请输入交易密码");
				
				withdrawal();
			}
			
			if(type !=1 && type != 2) {
				flash.error("传入参数有误");
				
				withdrawal();
			}
			
			if(bankId <= 0) {
				flash.error("请选择提现银行");
				
				withdrawal();
			}
		}
		
		User user = new User();
		user.id = User.currUser().id;
		
		long withdrawalId = user.withdrawal(amount, bankId, payPassword, type, flag, error);
		
		if(error.code  < 0){
			flash.error(error.msg);			
			withdrawal();
		}
		if(Constants.IPS_ENABLE && flag) {
			PaymentProxy.getInstance().withdraw(error, 0, withdrawalId, amount);
		}
		
		flash.error(error.msg);
		
		withdrawal();
	}
	
	//转账
	public static void transfer(){
		render();
	}
	
	//确认转账
	public static void submitTransfer(){
		render();
	}
	
	/**
	 * 交易记录
	 */
	@InactiveUserCheck
	@RealNameCheck
	public static void dealRecord(int type, String beginTime, String endTime, int currPage, int pageSize){
	
		User user = User.currUser();
		PageBean<v_user_details> page = User.queryUserDetails(user.id, type, beginTime, endTime,currPage, pageSize);
		
		render(page);
	}
	
	//交易详情
	public static void dealDetails(){
		render();
	}
	
	/**
	 * 导出交易记录
	 */
	public static void exportDealRecords(){
		ErrorInfo error = new ErrorInfo();
		
    	List<v_user_details> details = User.queryAllDetails(error);
    	
    	if (error.code < 0) {
			renderText("下载数据失败");
		}
    	
    	JsonConfig jsonConfig = new JsonConfig();
    	jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
    	JSONArray arrDetails = JSONArray.fromObject(details, jsonConfig);
    	
    	for (Object obj : arrDetails) {
			JSONObject detail = (JSONObject)obj;
			int type = detail.getInt("type");
			double amount = detail.getDouble("amount");
			
			switch (type) {
			case 1:
				detail.put("inAmount", amount);
				detail.put("outAmount", "");
				break;
			case 2:
				detail.put("inAmount", "");
				detail.put("outAmount", amount);
				break;
			default:
				detail.put("inAmount", "");
				detail.put("outAmount", "");
				break;
			}
		}
    	
    	File file = ExcelUtils.export(
    			"交易记录", 
    			arrDetails,
				new String[] {"时间", "收入", "支出", "账户总额", "可用余额", "冻结金额", "待收金额", "科目", "明细"}, 
				new String[] {"time", "inAmount", "outAmount", "user_balance", "balance", "freeze", "recieve_amount", "name", "summary"});
    	
    	renderBinary(file, "交易记录.xls");
	}
	
	/**
	 * 支付账号登录
	 */
	public static void loginAccount() {
		ErrorInfo error = new ErrorInfo();
		PaymentProxy.getInstance().loginAccount(error, Constants.PC, User.currUser());
	}
	
	/**
	 * 查看(异步)
	 */
	public static void showitem(String mark, String signUserId){
		/* 解密userId */
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1){
			renderText(error.msg);
		}
		
		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = userId;
		item.mark = mark;
		
		render(item);
	}
	
	/**
	 * 通过省份code获取城市code-name
	 */
	public static void queryCityCode2NameByProvinceCode(int provinceCode){
		ErrorInfo error = new ErrorInfo();
		Map<String,String> cityMaps = DictBanksDate.queryCityCode2NameByProvinceCode(provinceCode, error);
		JSONArray array = buildJSONArrayByMaps(cityMaps);
		renderJSON(array);
	}
	
	/**
	 * 通过组合条件搜索银行支行名称(条件中最少需要提供银行code,否则数据库数据量查询缓慢,耗时会在6S左右检索出数据)
	 * @param cityCode
	 * @param bankCode
	 * @param searchValue
	 */
	public static void queryBankCode2NameByCondition(int cityCode,int bankCode,String searchValue){
		if (0 == cityCode || 0 == bankCode){
			return;
		}
		Map<String,Object> maps = new HashMap<String, Object>();
		maps.put("cityCode", cityCode);
		maps.put("bankCode", bankCode);
		maps.put("searchValue", searchValue);
		ErrorInfo error = new ErrorInfo();
		Map<String,String> bankMaps  = DictBanksDate.queryBankCode2NameByCondition(maps, error);
		JSONObject object = buildJSONObject(bankMaps);
		renderJSON(object);
	}
	
	private static JSONObject buildJSONObject(Map<String,String> maps){
		Set<Entry<String, String>> set = maps.entrySet();
		JSONArray array = new JSONArray();
		for(Entry<String, String> entry : set){
			JSONObject obj = new JSONObject();
			obj.put("title", entry.getValue());
			array.add(obj);
		}
		JSONObject object = new JSONObject();
		object.put("data", array);
		return object;
	}
	
	/**
	 * Map集合组装公用JSONArray(name-value键值对)
	 * @param maps
	 * @return
	 */
	private static JSONArray buildJSONArrayByMaps(Map<String,String> maps){
		Set<Entry<String, String>> set = maps.entrySet();
		JSONArray array = new JSONArray();
		for(Entry<String, String> entry : set){
			JSONObject obj = new JSONObject();
			obj.put(NORMAL_KEY,  entry.getKey());
			obj.put(NORMAL_VALUE, entry.getValue());
			array.add(obj);
		}
		return array;
	}
	
	/**
	 * 用户绑卡
	 */
	public static void userBindCard() {		
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		PaymentProxy.getInstance().userBindCard(error, Constants.PC, user.ipsAcctNo);
	}

}