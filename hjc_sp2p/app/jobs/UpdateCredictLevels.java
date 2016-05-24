package jobs;

import business.CreditLevel;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;
import utils.ErrorInfo;

/**
 * 更新所有用户的信用等级，每天凌晨3点0分更新
 *
 * @author hys
 * @createDate  2015年6月24日 下午8:17:05
 *
 */
@On("0 0 3 * * ?")
public class UpdateCredictLevels extends Job {

	public void doJob() {
		CreditLevel.updateAllUserCreditLevels(new ErrorInfo());
	}
}
