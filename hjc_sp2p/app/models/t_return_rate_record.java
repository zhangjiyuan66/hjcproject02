package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 按天返息记录表
 * 
 * @author zb
 * @version 8.0
 * @created 2015-11-26 下午03:19:49
 */
@Entity
public class t_return_rate_record extends Model {
	public Long bid_id;
	public Long invest_user_id;
	public Date return_time;
	public String description;
	public double rate;

	
	public t_return_rate_record(){

	}
	
	public t_return_rate_record(Long bid_id ,Long invest_user_id,Date return_time,String description,double rate) {
             this.bid_id = bid_id;
             this.invest_user_id = invest_user_id;
             this.return_time = return_time;
             this.description  =description;
             this.rate  =rate; 
	}

}
