package jobs;





import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import business.Bid;
import constants.Constants;

/**
 * 每一天返息一次
 * 
 * @author zb
 * @version 8.0.1
 * @created 2015-11-26 上午09:32:52
 */
@On("0 10 00 * * ?")
//@Every("5min")
public class PayInvestForDay extends Job {

	@Override
	public void doJob() throws Exception {
	 
			Logger.info("--------------定时每天返息给理财人,开始---------------------");
			Bid.returnDayRateToInvestUser(); 
			Logger.info("--------------定时每天返息给理财人,结束---------------------");
	}
}
