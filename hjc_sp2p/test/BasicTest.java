import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.t_bills;
import models.t_user_auth_details;
import models.t_user_recharge_details;
import models.v_user_for_details;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.poi.hssf.util.HSSFColor.DARK_TEAL;
import org.joda.time.DateTime;
import org.junit.Test;

import com.shove.Convert;

import constants.Constants;
import constants.Constants.RechargeType;
import controllers.front.account.AccountHome;
import controllers.front.account.BasicInformation;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.mvc.Http.Request;
import play.test.UnitTest;
import reports.StatisticInvitation;
import services.RealPalService;
import services.util.RealPalConstanst;
import services.util.RongpayFunction;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.DataSafety;
import business.DealDetail;
import business.EChartsData;
import business.Invest;
import business.OverBorrow;
import business.StationLetter;
import business.User;
import business.UserAuditItem;
import business.Vip;
import business.Wealthcircle;

public class BasicTest extends UnitTest {
	
	
	@Test
	public void ttt(){
		Bill bill = new Bill();
		bill.systemMakeOverdue(new ErrorInfo());
	}
	
	@Test
	public void aaa(){
		ErrorInfo error = new ErrorInfo();
		System.out.println("======"+StationLetter.queryWaitReplyMessageCount(error));
	}
	
	@Test
	public void testInviteIncome(){
		
		
		Wealthcircle.addInviteIncome(9);
		
	}
	
	@Test
	public void testqueryPeriod(){
		Bid.queryPeriodByBidId(1);
	}
	
	@Test
	public void testPayforInvataion(){
		
		
		User.payForInvitation();
	}
	
	@Test
	public void testCheskSign(){
		
		DataSafety da = new DataSafety();
		da.id = 14;
		da.signCheck(new ErrorInfo());
	}
	
	@Test
	public void testupSign(){
		
		DataSafety da = new DataSafety();
		da.updateSignWithLock(14, new ErrorInfo());
	}
	
	@Test
	public void testStatisInvitation(){
		Calendar cal=Calendar.getInstance();//使用日历类  
	    
		int year = cal.get(Calendar.YEAR);// 得到年
		int month = cal.get(Calendar.MONTH) + 1;// 得到月，因为从0开始的，所以要加1
		
		StatisticInvitation.saveOrUpdateRecord(year, month);
		StatisticInvitation.saveOrUpdateDetailRecord(year, month);
	}
	
	@Test
	public void testStatis(){
		String sql = "SELECT DISTINCT(user_id), user_name FROM t_wealthcircle_invite";
		
		List<Object[]> list = null;
		
		try {
			list = JPA.em().createNativeQuery(sql).getResultList();
		} catch (Exception e) {
			Logger.error("查询所有拥有邀请码的会员时，%s", e.getMessage());
			
			return ;
		}
		
		for(Object[] o : list){
			System.out.println(Long.parseLong(o[0].toString()));
			System.out.println(o[1].toString());

		}
		
		System.out.println(list);
	}
	
	@Test
	public void testThread(){
		
		ErrorInfo error = new ErrorInfo();
		t_user_recharge_details tusers = t_user_recharge_details.find(" pay_number = ? ", "0X720160104124143614681").first();
		User.rbShSingleAY("0X720160104124143614681",tusers,error);
	}

	
	@Test
	public  void  onesss() throws ClientProtocolException, IOException{
		ErrorInfo error = new  ErrorInfo();
	   	String ip="127.0.0.1";
		
		  t_user_auth_details auth = User.querryAuthRecords(ip,error);
			
			if(auth != null){
				if(auth.auth_count < 10){
					User.updateAuthIp(ip,error);
					
				}else{ 
					
					if(DateUtil.diffMinutes(auth.auth_time, new Date())< 24*60){
						error.code=-1;
						error.msg="对不起,您暂时不能进行认证";
						BasicInformation.certification();
					}
					
					error.code=-1;
					error.msg="你太累了，明天再来认证吧！";
					BasicInformation.certification();
				}
				
			}else{
				
				User.saveAuthRecords(ip,error);
			}
	}
	
	
	@Test
	public  void doQuery() throws UnsupportedEncodingException{
		String body="buyer_id=&trade_no=101512281783753&body=%E7%BD%91%E5%85%B3%E5%85%85%E5%80%BC&notify_time=2015-12-28+16%3A11%3A32&sign_type=MD5&is_total_fee_adjust=0&notify_type=WAIT_TRIGGER&trade_status=TRADE_FINISHED&gmt_payment=2015-12-28+16%3A11%3A32&order_no=0X220151228161104248326&discount=0.00&sign=09770b9aedecc0a68b6ed5e242669d37&title=%E5%85%85%E5%80%BC&buyer_email=&gmt_create=2015-12-28+16%3A10%3A59&is_success=T&price=0.10&total_fee=0.10&seller_actions=SEND_GOODS&seller_id=100000000073653&quantity=1&notify_id=604ba9efeaaf4a258c7c862abeb4430c&seller_email=294254328%40qq.com&gmt_logistics_modify=2015-12-28+16%3A12%3A00&payment_type=1";
		Map<String, Object> maps = new HashMap<String, Object>(0);
		maps.put("trade_no", "101512281783753");
		maps.put("notify_time", "2015-12-28 16:11:32");
		maps.put("sign_type", "MD5");
		maps.put("notify_type", "WAIT_TRIGGER");
		maps.put("gmt_payment", "2015-12-28 16:11:32");
		maps.put("order_no", "0X220151228161104248326");
		maps.put("trade_status", "TRADE_FINISHED");
		maps.put("sign", "09770b9aedecc0a68b6ed5e242669d37");
		maps.put("is_success", "T");
		maps.put("title", "充值");
		maps.put("gmt_create", "2015-12-28 16:10:59");
		maps.put("total_fee", "0.10");
		maps.put("seller_id", "100000000073653");
		maps.put("notify_id", "604ba9efeaaf4a258c7c862abeb4430c");
		maps.put("seller_email", "294254328@qq.com");
		maps.put("payment_type", "1");
		maps.put("body", body);
		
		Logger.info("######返回参数列表：%s\n",maps.toString());

		Map<String, Object> maps1 = RongpayFunction.CreateLinkString(maps.get("body").toString());
		String mysign = RongpayFunction.BuildMysign(maps1,RealPalConstanst.REALPAL_MD5KEY);
		Logger.info("###融宝网关支付回调本地mysign###"+mysign);
		
		if(!mysign.equals(maps.get("sign").toString())){
			Logger.info("###融宝网关支付回调sign加密数据验签失败###");
			return;
		}
		if(StringUtils.isBlank(maps.get("is_success").toString()) && !"T".equals(maps.get("is_success").toString())){
			Logger.info("###融宝网关支付回调is_success校验失败###");
			return;
		}
	
		if("T".equals(maps.get("is_success").toString()) &&  "WAIT_BUYER_PAY".equals(maps.get("trade_status").toString()) ){
			Logger.info("###订单处理中，请耐心等待###");
			return;
		}
		
		if("TRADE_FAILURE".equals(maps.get("trade_status").toString()) ){
			Logger.info("###交易失败###");
			return;
		}
//		//对融宝返回的notify_id进行通知验证
//		String result = RongpayFunction.Verify(maps.get("notify_id").toString());
//		
//		if("invalid".equals(result)){
//			Logger.info("###验证返回URL参数出错###");
//			return;
//		}
//		if("false".equals(result)){
//			Logger.info("###验证返回URL失败###");
//			return;
//		}
		
		
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;
		
		try {
			obj = t_user_recharge_details.find(sql, maps.get("order_no").toString()).first();
		} catch (Exception e) {
			Logger.info("######融宝网关回调 根据pay_number查询用户ID出错"+e.getMessage());
			
			return ;
		}
		
		if(null == obj) {
			Logger.info("###融宝网关回调 根据pay_number查询用户ID为null");
			
			return ;
		}
		String info="";
		String sql1 = "update t_user_recharge_details set order_no = ? where pay_number = ? and is_completed = 0";
		
		try{
			JPA.em().createQuery(sql1).setParameter(1, maps.get("trade_no").toString()).setParameter(2, maps.get("order_no").toString()).executeUpdate();
		}catch(Exception e) {
			JPA.setRollbackOnly();
			Logger.info(e.getMessage());
			
			return;
		}
		ErrorInfo error = new ErrorInfo();
		User.recharge(maps.get("trade_no").toString(), Double.parseDouble(maps.get("total_fee").toString()), error);
		int rechargeType = Convert.strToInt(maps.get("order_no").toString().split("X")[0], RechargeType.Normal);
		
		
		 info = "交易成功";

	}
	
	
	public void main(String[] args){
		testThread();
	}
}
