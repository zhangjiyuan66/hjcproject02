package controllers.supervisor.bidManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import models.t_bids;
import models.v_agencies;
import models.v_bids;
import models.v_receiving_invest_bids;
import models.v_user_info;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import payment.PaymentProxy;
import play.Logger;
import play.cache.Cache;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;
import bean.AgencyBid;
import bean.RecevingInvest;
import bean.RegularBid;
import business.Agency;
import business.Bid;
import business.Invest;
import business.Optimization;
import business.Product;
import business.Supervisor;
import business.User;
import business.Bid.Purpose;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 定期标管理
 * @author zb
 * @version 8.0
 
 * @created 2015-10-17 下午01:52:09
 */
public class BidRegularAction extends SupervisorController{
	
	/**
	 * 定期标列表
	 * @param isExport  布尔值，是否导出为excel表格
	 */
	public static void regularBidList(int isExport){
		ErrorInfo error = new ErrorInfo();
		
		 //删除定期标的缓存信息 
		String key = "regularBid_" + session.getId();
		Cache.delete(key);
		
		PageBean<RegularBid> pageBean = new PageBean<RegularBid>();
		pageBean.page = Optimization.BidOZ.queryRegularBids(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, error, BidPlatformAction.getParameter(pageBean, null));

		if(isExport == Constants.IS_EXPORT){
			
			List<RegularBid> list = pageBean.page;

			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bid = (JSONObject)obj;
				
				String showPeriod = "";
				int period = bid.getInt("period");
				int period_unit = bid.getInt("period_unit");
				if(period_unit == -1){
					showPeriod = period + "[年 ]";
				}else if(period_unit == 1){
					showPeriod = period + "[日]";
				}else{		
					showPeriod = period + "[月]";
				}
				
				DecimalFormat df = new DecimalFormat("#0.0");
				double percent = 0.0;
				int productItem = bid.getInt("product_item_count");
				int userItem = bid.getInt("user_item_count_true");
				if(productItem == 0 || userItem / productItem >= 1){
					percent = 100.0;
				}else{
					percent = (userItem * 100.0 ) / productItem;
				}
				String auditStatus = df.format(percent) + "%";
				
				String productName = Product.queryProductNameByImage(bid.getString("small_image_filename"));
				String creditLevel = User.queryCreditLevelByImage(bid.getString("credit_level_image_filename"));
				String investPeriod = bid.getString("invest_period");
				bid.put("period", showPeriod);
				bid.put("small_image_filename", productName);
				bid.put("credit_level_image_filename", creditLevel);
				bid.put("invest_period", investPeriod);
				bid.put("apr", String.format("%.1f", bid.getDouble("apr")) + "%");
				
				bid.put("loan_schedule", String.format("%.1f", bid.getDouble("loan_schedule")) + "%");
				bid.put("audit_status", auditStatus);
			}
			
			File file = ExcelUtils.export("定期标列表",
			arrList,
			new String[] {
			"编号", "标题", "发布者", "信用等级", "借款标金额", "借款标类型", "年利率",
			"借款期限", "锁定期限","发布时间",  "借款进度",
			"借款状态", "审核状态"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "amount", "small_image_filename", "apr",
			"period","invest_period", "time",
			"loan_schedule", "strStatus", "audit_status"});
			   
			renderBinary(file, "定期标列表.xls");
		}
		
		render(pageBean);
	}
	
	
	/**
	 * 定期标详情(审核操作等,需要去借款标管理中进行)
	 * @param bidId
	 * @param flag
	 */
	public static void detail(long bidid, int flag) { 
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = flag;
		bid.id = bidid;
		
		render(bid, flag);
	}
	
	/**
	 * 发布定期标 页面跳转
	 */
	public static void addRegularBid(){
		ErrorInfo error = new ErrorInfo();
		
		/* 得到所有借款用途  */
		List<Purpose> purpose = Purpose.queryLoanPurpose(error, true);
		
		/* 定期标产品 */
		Product product = Product.queryRegularProduct(error);
		
		if(null == product){
			flash.error(error.msg);
			
			regularBidList(0);
		}
		
		String key = "regularBid_" + session.getId();
		Bid loanBid = (Bid) Cache.get(key);  // 获取用户输入的临时数据
		Cache.delete(key); // 删除缓存中的bid对象
		String uuid = CaptchaUtil.getUUID(); // 防重复提交UUID
		int once_repayment = Constants.ONCE_REPAYMENT;  //一次性还款方式

		render(purpose, product, uuid, loanBid, once_repayment);
	}
	
	/**
	 * 发布定期标
	 * @param bid
	 * @param productId
	 * @param uuid
	 * 
	 */
	public static void addingRegularBid(Bid bid, long productId, String uuid){
		/* 有效表单验证  */
		checkAuthenticity(); 

		/* 将定期标信息放在cache中,如果错误带会到页面中 */
		Cache.set("regularBid_" + session.getId(), bid);
		
		if(!CaptchaUtil.checkUUID(uuid)){
			flash.error("请求已提交或请求超时!");
			
			addRegularBid();
		}
		String userName = params.get("userName");
		String signUserId = params.get("sign");
		String b_types = params.get("b_types");
		String quality = params.get("quality");
		String photos = params.get("photos");
		if(StringUtils.isBlank(signUserId) && StringUtils.isBlank(userName)){
			flash.error("直接借款人有误!");
			
			addRegularBid();
		}
		
		ErrorInfo error = new ErrorInfo();
		long userId = 0;
		
		if(StringUtils.isNotBlank(userName)){
			userId = User.queryIdByUserName(userName, error);
		}else{
			userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		}
		
		if(userId < 1){
			flash.error(error.msg);
			
			addRegularBid();
		}
		
		bid.createBid = true;
		bid.productId = productId;  // 填充产品对象
		bid.userId = userId;

		/* 非友好提示 */
		if( bid.user.id < 1 ||
			null == bid.product || 
			!bid.product.isUse || 
			!bid.product.isRegular 
		){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR); 
		}
		
		if(!(bid.user.isEmailVerified || bid.user.isMobileVerified)){
			flash.error("借款人未激活!");
			
			addRegularBid();
		}
		
		/* 需要填写基本资料 */
		if(!bid.user.isAddBaseInfo) {
			flash.error("借款人未填写基本资料!");
			
			addRegularBid();
		}
		/* 未实名认证 */
		if(StringUtils.isBlank(bid.user.realityName)) {
			flash.error("借款人未实名认证!");
			
			addRegularBid();
		}
		/* 秒还标未进行自动还款签约 */
		if (Constants.IPS_ENABLE && bid.product.loanType == Constants.S_REPAYMENT_BID && StringUtils.isBlank(bid.user.ipsRepayAuthNo)) {
			flash.error("直接借款人未自动还款签约!");
			
			addRegularBid();
		}
		
		/* 发布借款 */
		t_bids tbid = new t_bids();
		
		if(StringUtils.isNotBlank(b_types)){
			tbid.is_regular=true;
			bid.isRegular = true;
		}
		if(StringUtils.isNotBlank(quality)){
			bid.isQuality = true;
			tbid.is_quality = true;
		}
		//标的发布前校验， 及组装标的信息，不插入数据库
		bid.createBid(Constants.CLIENT_PC, tbid, error);
		if(error.code < 0){
			flash.error(error.msg);
			addRegularBid();
		}
		
		if(Constants.IPS_ENABLE){
			
			PaymentProxy.getInstance().bidCreate(error, Constants.CLIENT_PC, tbid, bid);
			
			flash.error(error.msg);				
			addRegularBid();
		}
		/* 发布借款 */
		bid.afterCreateBid(tbid, null,photos, Constants.CLIENT_PC, Supervisor.currSupervisor().getId(), error);		
		if(error.code < 0) {
			flash.error(error.msg);
			
			addRegularBid();
		}		
		flash.error(tbid.id > 0 ? "发布成功!" : error.msg);
		
		regularBidList(0);
	}
	
	/**
	 * 异步选择用户
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 */
	public static void selectUsersInit(String currPage, String pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_info> pageBean = User.queryActiveUser(null, null, null, null, keyword, "0", currPage, Constants.PAGE_SIZE_EIGHT+"", error);
		
		if(error.code < 0){ 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(pageBean);
	}
	
	/**
	 * 提前结束
	 * @param id 标的id
	 */
	public static void regularStopInAdvance(Long id){
		
		ErrorInfo error = new ErrorInfo();
		
		Bid bid = new Bid();
		bid.id = id;  
		if(bid.productId != 7){
			 error.msg = "借款产品错误";
			 flash.error(error.msg);
			 regularBidList(0);
		}
		if(bid.hasInvestedAmount >= bid.amount || bid.status < Constants.BID_ADVANCE_LOAN || bid.status>Constants.BID_FUNDRAISE){
			 error.msg = "定期标不在借款状态中，不需要提前结束";
			 flash.error(error.msg);
			 regularBidList(0);
		 }
		if(bid.hasInvestedAmount < 1){
			 error.msg = "定期标当前无人投资";
			 flash.error(error.msg);
			 regularBidList(0);
		}
		
		//提前结束
		bid.regularStopInAdvance(error); 
		
		if(error.code < 0) {
			flash.error(error.msg);
			
			addRegularBid();
		}		
		flash.error(error.msg);
		
		
		regularBidList(0);
	}
	
	
	/**
	 * 赎回审核
	 */
	public static void RedeemAudit(int isExport){
		
		ErrorInfo error = new ErrorInfo();
		PageBean<RecevingInvest> pageBean = new PageBean<RecevingInvest>();
		pageBean.page = Optimization.BidOZ.queryRegularAudit(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, error, BidPlatformAction.getParameter(pageBean, null));

		if(isExport == Constants.IS_EXPORT){
			
			List<RecevingInvest> list = pageBean.page;

			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bid = (JSONObject)obj;

				bid.put("apr", String.format("%.1f", bid.getDouble("apr")) + "%");		
				bid.put("loan_schedule", String.format("%.1f", bid.getDouble("loan_schedule")) + "%");
			}
			
			File file = ExcelUtils.export("赎回申请列表",
			arrList,
			new String[] {
			"标题", "投资人", "借款金额","借款进度","投资金额", "年利率", "申请赎回时间", "审核人", "审核状态"},
			new String[] {  "title", "name",
			"bidAmount",  "loan_schedule","amount", "apr",
			"redeem_time","redeem_audit_id", "redeem_status",
			});
			 
			renderBinary(file, "赎回申请列表.xls");
		}
		
		render(pageBean);
	}
	
	/**
	 * 赎回审核操作-通过
	 */
	public static void redeemAuditSuccess(Long id, boolean flag){
		
		ErrorInfo error = new ErrorInfo();
		Invest invest = new Invest();
		invest.id = id;
		Bid bid = new Bid();
		bid.id = invest.bidId;
		
		if(new Date().compareTo(bid.investExpireTime)<0){
			error.msg="定期标未过锁定期，不能赎回";
	        flash.error(error.msg);
			RedeemAudit(0);
		}
		
		if(bid.status != Constants.BID_COMPENSATE_REPAYMENT && bid.status != Constants.BID_REPAYMENT){
			error.msg="定期标状态有误";
	        flash.error(error.msg);
			RedeemAudit(0);
		}
		
		if(invest.redeemStatus != Constants.ONE){
			error.msg="定期标赎回审核状态有误";
	        flash.error(error.msg);
			RedeemAudit(0);
		 }
		
		//赎回成功-操作
		invest.redeemSuccess(invest,flag,Supervisor.currSupervisor().id,error);   
		
		if(error.code < 0) {
			flash.error(error.msg);
			
			RedeemAudit(0);
		}		
		flash.error( "赎回操作成功!");
		
		RedeemAudit(0);
	}
	
	/**
	 * 赎回审核操作-不通过
	 */
	public static void redeemAuditFail(Long id){
		
		ErrorInfo error = new ErrorInfo();
		Invest invest = new Invest();
		invest.id = id;
		
		//赎回失败-操作
		invest.redeemFail(invest,Supervisor.currSupervisor().id,error); 
		
		if(error.code < 0) {
			flash.error(error.msg);
			
			RedeemAudit(0);
		}		
		flash.error( "拒绝赎回成功!");
		
		RedeemAudit(0);
	}
	
}
