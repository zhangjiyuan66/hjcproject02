package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
/**
 * 体验金
 * @author Administrator
 *
 */
@Entity
public class t_bbin extends Model{
	public Long user_id; //用户id
	public Double account; //体验金数量
	public Date time;     //有效期
	public boolean status; //状态:可以使用/过期
	public Double invested_account; //已投金额
	public Double return_account; //返回金额(回收)
	public Long getUser_id() {
		return user_id;
	}
	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}
	
	
	
	
}

