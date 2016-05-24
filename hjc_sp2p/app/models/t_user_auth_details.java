package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 实名认证记录
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:24:42
 */
@Entity
public class t_user_auth_details extends Model {
	 
	public String ip; 
	public Date auth_time;
	public int auth_count;
	

	public t_user_auth_details(){ 
		
	}
	public t_user_auth_details(String ip,int auth_count){
		this.ip = ip;
		this.auth_count = auth_count;
	}
}
