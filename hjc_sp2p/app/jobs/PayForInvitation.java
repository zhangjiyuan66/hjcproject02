package jobs;

import business.Invest;
import business.User;
import play.jobs.Every;
import play.jobs.Job;

/**
 * 批量发放佣金（财富圈）
 * 
 * @author hys
 *
 */
@Every("2h")
public class PayForInvitation extends Job{
	
	public void doJob() {
		User.payForInvitation();  
	}

}
