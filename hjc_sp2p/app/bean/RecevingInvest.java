package bean;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import constants.Constants;

/**
 * 定期标列表
 * @author zb
 * @version 6.0
 * @created 2015-10-19 晚上14:41:00
 *
 */
@Entity
public class RecevingInvest implements Serializable{
	@Id
	public Long id;
	public String title;
	public String name;
	public Double amount;
	public Double bidAmount;
	public Double apr;
	public Double loan_schedule;
	public Integer redeem_status;
	public int redeem_audit_id;
	public Date redeem_time;
	
	
}
