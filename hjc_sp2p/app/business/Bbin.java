package business;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import models.t_bbin;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;

/**
 * 体验金业务实体
 * 
 * @author Administrator
 * 
 */
public class Bbin implements Serializable {
	public Long user_id;
	public Double account;
	public Date time;
	public boolean status;
	public Double invested_account;
	public Double return_account;

	public Bbin() {
		super();
	}

	public Bbin(Long user_id, Double account, Date time, boolean status,
			Double invested_account, Double return_account) {
		super();
		this.user_id = user_id;
		this.account = account;
		this.time = time;
		this.status = status;
		this.invested_account = invested_account;
		this.return_account = return_account;
	}

	// 注册赠送体验金
	public void presentedBbin() {
		t_bbin tbbin = new t_bbin();
		tbbin.user_id = this.user_id;
		tbbin.account = this.account;
		tbbin.invested_account = this.invested_account;
		tbbin.return_account = this.return_account;
		tbbin.status = this.status;
		tbbin.time = this.time;
		tbbin.save();
	}

	// 查询体验金详情
	public static t_bbin queryBbinforInvest(long userId, ErrorInfo error) {
		error.clear();
		
		String sql = "select id ,user_id , account ,status, invested_account, return_account from t_bbin  where user_id = ?";
		Object[] obj = null;
		try {
			obj = (Object[]) JPA.em().createNativeQuery(sql)
					.setParameter(1, userId).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;
			return null;
		}
		if (null == obj) {
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;
			return null;
		}
		
		t_bbin tbbin = new t_bbin();
		tbbin.id = null == obj[0] ? 0 : Long.parseLong(obj[0].toString());
		tbbin.user_id = null == obj[1] ? 0 : Long.parseLong(obj[0].toString());
		tbbin.account = null == obj[2] ? 0 : Double.parseDouble(obj[2]
				.toString());
		tbbin.status = null == obj[3] ? false : Boolean.parseBoolean(obj[3]
				.toString());
		tbbin.invested_account = null == obj[4] ? 0 : Double.parseDouble(obj[4]
				.toString());
		tbbin.return_account = null == obj[5] ? 0 : Double.parseDouble(obj[5]
				.toString());
		error.code = 1;
		return tbbin;
	}
	//回收体验金
	public static void returnBbin(long userId,Double invested_account,ErrorInfo error){
		error.clear();
		String sql ="UPDATE t_bbin SET invested_account =invested_account - ?,return_account=return_account+? WHERE user_id=? and invested_account >=?";
		int row =0;
		Query query = JPA.em().createQuery(sql);
		query.setParameter(1,invested_account);
		query.setParameter(2,invested_account);
		query.setParameter(3,userId);
		query.setParameter(4,invested_account);
		try{
			query.executeUpdate();
		}catch(Exception e){
			error.code = -1;
			error.msg = "收回用户体验金时异常";
			Logger.error("收回用户体验金:" + e.getMessage());
			JPA.setRollbackOnly();
			return ;
		}
		if(row == 0){
			error.code = -2;
			error.msg = "收回用户体验金失败";
			Logger.error("收回用户体验金失败，可能是已投资体验金余额不足");
			JPA.setRollbackOnly();
		}
	}
	
	/**
	 * 查询用户是否有体验金
	 * @param id
	 * @return
	 */
	public boolean queryid(long id){
		String sql = "from t_bbin";
		Query query = JPA.em().createQuery(sql);
		boolean flag = false;
		List<t_bbin> bbin = query.getResultList();
		t_bbin bb = null;
		for (int i = 0; i < bbin.size(); i++) {
			bb = bbin.get(i);
			if(id == bb.getUser_id()){
				flag = true;
			}
		}
		return flag;
		
	}
}
