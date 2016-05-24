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
public class t_bid_loantime_record extends Model {
	public Long bid_id;
	public Long invest_id;
	public Date invest_time;
	public Date loan_time;
	public Date deadline_time;

	
	public t_bid_loantime_record(){

	}
	
	public t_bid_loantime_record(Long bid_id ,Long invest_id,Date invest_time,Date loan_time,Date deadline_time) {
             this.bid_id = bid_id;
             this.invest_time = invest_time;
             this.loan_time = loan_time;
             this.deadline_time  =deadline_time;
	}

}
