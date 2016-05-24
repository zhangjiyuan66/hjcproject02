package jobs;

import business.User;
import play.jobs.Job;
import play.jobs.On;
import utils.ErrorInfo;
/**
 * 每天0：00删除实名认证的统计次数
 * @author zkai
 */
@On("0 00 00 * * ?")
public class DelAuthenticationCounts extends Job{

	public void doJob() {
		ErrorInfo error = new ErrorInfo();
		User.delAuthenticationCounts(error);
	}
}
