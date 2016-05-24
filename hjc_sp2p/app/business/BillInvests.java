package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_bill_invests;
import models.t_wealthcircle_invite;
import models.v_bill_invest;
import models.v_bill_invest_detail;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.ErrorInfo;
import utils.PageBean;
import utils.QueryUtil;
import constants.Constants;
import constants.SQLTempletes;

public class BillInvests implements Serializable{

	public long id;
	private long _id;
	
	public long userId;
	public long bidId;
	public int period;
	public String title;
	public Date receiveTime;
	public double receiveCorpus;
	public double receiveInterest;
	public int status;
	public double overdueFine;
	public Date realReceiveTime;
	public double realReceiveCorpus;
	public double realReceiveInterest;
	
	public Bid bid;
	
	public void setId(long id){
		t_bill_invests invest = t_bill_invests.findById(id);
		
		if(invest.id < 0 || invest == null){
			this._id = -1;
			return;
		}
		
		this._id = invest.id;
		this.userId = invest.user_id;
		this.bidId = invest.bid_id;
		this.period = invest.periods;
		this.title = invest.title;
		this.receiveTime = invest.receive_time;
		this.receiveCorpus = invest.receive_corpus;
		this.receiveInterest = invest.receive_interest;
		this.status = invest.status;
		this.overdueFine = invest.overdue_fine;
		this.realReceiveCorpus = invest.real_receive_corpus;
		this.realReceiveInterest = invest.real_receive_interest;
		
		bid = new Bid();
   	    bid.id = invest.bid_id;
	}
	
	public long getId(){
		
		return this._id;
	}
	
	/**
	 * 查询我所有的理财账单
	 * @param error
	 * @return
	 */
	public static List<v_bill_invest> queryMyAllInvestBills(ErrorInfo error) {
		error.clear();
		
		List<v_bill_invest> bills = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST);
		sql.append(" and c.id = ? group by a.id");
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest.class);
            query.setParameter(1, User.currUser().id);
            bills = query.getResultList();
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询我所有的理财账单:" + e.getMessage());
			error.code = -1;
			error.msg = "查询我所有的理财账单失败!";
			
			return null;
		}
		
		return bills;
	}
	
	/**
 	 * 查询我的理财账单
 	 * @param userId
 	 * @param info
 	 * @param currPage
 	 * @return
 	 */
 	public static PageBean<v_bill_invest> queryMyInvestBills(int payType, int isOverType,
			int keyType, String keyStr, int currPageStr, long userId, ErrorInfo error){
        error.clear();
		
 		int count = 0;
 		int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
 		
        Map<String,Object> conditionMap = new HashMap<String, Object>();
 		List<v_bill_invest> bills = new ArrayList<v_bill_invest>();
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST);

		List<Object> params = new ArrayList<Object>();
 		
 		if((payType < 0) || (payType > 2)){
 			payType = 0;
 		}
 		
 		if((isOverType < 0) || (isOverType > 2)){
 			isOverType = 0;
 		}
 		
 		if((keyType < 0) || (keyType > 3)){
 			keyType = 0;
 		}
 		
 		if(currPageStr != 0){
 			currPage = currPageStr;
 		}
 		
 		if(StringUtils.isNotBlank(keyStr)) {
 			if(keyType == 2){  //微信查询用到了三个字段
 				sql.append(SQLTempletes.LOAN_INVESTBILL_ALL[keyType]);
 				params.add("%"+keyStr.trim()+"%");
 				params.add("%"+keyStr.trim()+"%");
 			}else{
 				sql.append(SQLTempletes.LOAN_INVESTBILL_ALL[keyType]);
 				params.add("%"+keyStr.trim()+"%");
 			}
		}
 		
 		sql.append(SQLTempletes.LOAN_INVESTBILL_RECEIVE[payType]);
 		sql.append(SQLTempletes.LOAN_INVESTBILL_OVDUE[isOverType]);
 		sql.append("and c.id = ?");
 		params.add(userId);
 		
 		conditionMap.put("payType", payType);
		conditionMap.put("isOverType", isOverType);
		conditionMap.put("keyType", keyType);
		conditionMap.put("key", keyStr);
		
		try {
			sql.append(" group by id");
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bills = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单失败";
			
			return null;
		}
		
		PageBean<v_bill_invest> page = new PageBean<v_bill_invest>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		page.page = bills;
		
		return page;
 	}
 	
 	/**
 	 * 我的账单详情
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static v_bill_invest_detail queryMyInvestBillDetails(long id, long userId, ErrorInfo error){
 		error.clear();
		
 		v_bill_invest_detail investDetail = new v_bill_invest_detail();
 		List<v_bill_invest_detail> v_bill_invest_detail_list = null;
 		
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST_DETAIL);
		sql.append(" and a.id = ? and a.user_id = ?");

 		try {
 			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest_detail.class);
            query.setParameter(1, id);
            query.setParameter(2, userId);
            query.setMaxResults(1);
            v_bill_invest_detail_list = query.getResultList();
            
            if(v_bill_invest_detail_list.size() > 0){
            	investDetail = v_bill_invest_detail_list.get(0);
            }
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单详情时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
		}
		 
		if(null == investDetail){
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
			
		}
		
		error.code = 1;
 		return investDetail;
 	}
 	
 	/**
 	 * 我的账单详情
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static v_bill_invest_detail queryMyInvestBillDetails(long id, ErrorInfo error){
 		error.clear();
		
 		v_bill_invest_detail investDetail = new v_bill_invest_detail();
 		List<v_bill_invest_detail> v_bill_invest_detail_list = null;
 		
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST_DETAIL);
		sql.append(" and a.id = ?");
		
 		try {
 			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest_detail.class);
            query.setParameter(1, id);
            query.setMaxResults(1);
            v_bill_invest_detail_list = query.getResultList();
            
            if(v_bill_invest_detail_list.size() > 0){
            	investDetail = v_bill_invest_detail_list.get(0);
            }
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单详情时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
		}
		 
		if(null == investDetail){
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
			
		}
		
		error.code = 1;
 		return investDetail;
 	}
 	
 	/**
 	 * 我的理财账单——-历史收款情况
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static PageBean<t_bill_invests> queryMyInvestBillReceivables(long bidId,long userId, long investId, int currPage, int pageSize, ErrorInfo error){
 		error.clear();
 		
 		String sql = "select new t_bill_invests(id as id,title as title, SUM(receive_corpus + receive_interest + ifnull(overdue_fine,0)) as receive_amount, " +
 				"status as status, receive_time as  receive_time, real_receive_time as real_receive_time )" +
 				"from t_bill_invests where bid_id = ? and user_id = ? and invest_id = ? group by id";
 		
		List<t_bill_invests> investBills = new ArrayList<t_bill_invests>();
		PageBean<t_bill_invests> page = new PageBean<t_bill_invests>();
		page.pageSize = Constants.FIVE;
		page.currPage = Constants.ONE;
		
		if(currPage != 0){
			page.currPage = currPage;
		}
		
		if(pageSize != 0){
			page.pageSize = pageSize;
		}
		
		try {
			page.totalCount = (int) t_bill_invests.count("bid_id = ? and user_id = ? and invest_id = ?", bidId, userId, investId);
			investBills = t_bill_invests.find(sql, bidId, userId, investId).fetch(page.currPage, page.pageSize);
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单收款情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单收款情况失败";
			
			return null;
		}
		
		page.page = investBills;

		return page;
 	}
 	
 	/**
 	 * 我的理财账单——-根据标的ID和投资人ID查询还款记录
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static List<t_bill_invests> queryMyInvestBillReceivablesBid(long bidId,long userId, ErrorInfo error){
 		error.clear();
 		String sql = "SELECT new t_bill_invests(id AS id,title AS title,status AS status, receive_time AS  receive_time,(receive_corpus+receive_interest) AS receive_amount,real_receive_time AS real_receive_time)" +
 				" FROM t_bill_invests WHERE bid_id = ? AND user_id = ? order by receive_time asc";
 		
		List<t_bill_invests> investBills = new ArrayList<t_bill_invests>();

		try {
			investBills = t_bill_invests.find(sql, bidId, userId).fetch();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单收款情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单收款情况失败";
			
			return null;
		}
		

		return investBills;
 	}
 	
 	/**
 	 * 查询累计收益
 	 * @param userId
 	 * @return
 	 */
 	public static double querySumIncome(long userId){
 		String sql ="select sum(real_receive_interest) + sum(overdue_fine) from t_bill_invests where user_id=? and  status  =0";
        Query query = JPA.em().createNativeQuery(sql);
        query.setParameter(1, userId);
        List<Object> list = query.getResultList();
        if(list.size() == 0 )
        	return 0d;
        if(list.get(0) == null){
        	return 0d;
        }
        return  Double.valueOf(list.get(0).toString());
 	}
 	
 	/**
 	 * 近一月累计收益
 	 * @param userId
 	 * @return
 	 */
 	public static double queryMonthSumIncome(long userId){
 		String sql ="select  sum(real_receive_interest) + sum(overdue_fine)  from t_bill_invests where user_id=? and  status  =0  and receive_time >date_add(now(), interval -1 month)" ;
        Query query = JPA.em().createNativeQuery(sql);
        query.setParameter(1, userId);
        List<Object> list = query.getResultList();
        if(list.size() == 0 )
        	return 0d;
        if(list.get(0) == null){
        	return 0d;
        }
        return  Double.valueOf(list.get(0).toString());
 	}
 	
 	/**
 	 * 近一年累计收益
 	 * @param userId
 	 * @return
 	 */
 	public static double queryYearSumIncome(long userId){
 		String sql ="select  sum(real_receive_interest) + sum(overdue_fine) from t_bill_invests where user_id=? and  status  =0  and receive_time >date_add(now(), interval -1 year)";
        Query query = JPA.em().createNativeQuery(sql);
        query.setParameter(1, userId);
        List<Object> list = query.getResultList();
        if(list.size() == 0 )
        	return 0d;
        if(list.get(0) == null){
        	return 0d;
        }
        return  Double.valueOf(list.get(0).toString());
 	}
 	
 	/**
 	 * 计算理财管理费
 	 * @param receiveInterest  本期应收利息
 	 * @param managementRate 费率
 	 * @param investUserId  投资者id
 	 * @return
 	 */
 	public static double getInvestManagerFee(double receiveInterest, double managementRate, long investUserId){
 		
 		double manageFee = Arith.round(Arith.mul(receiveInterest, managementRate), 2);  //投资管理费;
 		
 		//财富圈，被邀请人享受服务费
		t_wealthcircle_invite invite = t_wealthcircle_invite.find("invited_user_id = ?", investUserId).first();
	
		if (null != invite){
			manageFee = manageFee * invite.invited_user_discount / 100;
		}
		
		return manageFee;
		
 	}
 	
 	/**
 	 * 获取投资账单列表
 	 * @param bidId		借款标ID
 	 * @param periods	期数
 	 * @return
 	 */
 	public static List findBillInvestsByBidIdAndPeriods(long bidId, int periods) {
 		List<Map<String, Object>> billInvestList = null;
 		
 		String sql = " select new Map(invest.id as id, invest.invest_id as investId, "
 				+ " invest.receive_corpus as receive_corpus,invest.receive_interest as " +
 				" receive_interest, invest.overdue_fine as overdue_fine, invest.user_id as user_id, "
 				+ " invest.overdue_fine) "
				+ " from t_bill_invests as invest where invest.bid_id = ? and invest.periods = ? "
				+ "and invest.status not in (?,?,?,?)";
 		try {
 			billInvestList = t_bill_invests.find(sql, bidId, periods, 
 					Constants.FOR_DEBT_MARK, 
 					Constants.NORMAL_RECEIVABLES, 
 					Constants.ADVANCE_PRINCIIPAL_RECEIVABLES, 
 					Constants.OVERDUE_RECEIVABLES).fetch();
 			
		} catch (Exception e) {
			Logger.error("------- 获取投资账单列表失败：", e);
			e.printStackTrace();
			return null;
		}
 		
		return billInvestList;
 	}
 	
}
