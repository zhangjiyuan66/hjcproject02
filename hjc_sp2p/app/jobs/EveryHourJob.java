package jobs;

import business.Invest;
import business.User;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

@Every("15min")
public class EveryHourJob extends Job{
	
	public void doJob() {
		Invest.automaticInvest();  //自动投标
		User.payForCps();  //cps奖励发放
	}
}
