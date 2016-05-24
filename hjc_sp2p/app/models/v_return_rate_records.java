package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 用户投返息记录
* @author zb
* @version 8.0
* @created 2015年11月27日 下午4:14:14
 */

@Entity
public class v_return_rate_records extends Model{
	
	public Date return_time;
	public String invest_user_name;
	public Double rate;
	public String description;
	public String getInvest_user_name() {
		return invest_user_name;
	}
	public void setInvest_user_name(String invest_user_name) {
		this.invest_user_name = invest_user_name;
	}
	
}
