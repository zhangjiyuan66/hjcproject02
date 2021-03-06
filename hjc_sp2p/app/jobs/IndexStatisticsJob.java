package jobs;

import java.util.Calendar;
import java.util.Date;

import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;
import reports.StatisticBorrow;
import reports.StatisticPlatformFloat;
import utils.ErrorInfo;
import business.Bid;
import business.Bill;
import business.User;

/**
 * 首页统计分析
 * 
 * @author fei
 *
 */
@On("0 0 3 * * ?")
@OnApplicationStart
public class IndexStatisticsJob extends Job {

	// 累计投资
	public static double sumInvest;
	// 累计收益
	public static double sumIncome;
	// 注册总人数
	public static long regCount;
	
	// 成功借款标数量
	public static int releasedBidsNum;
	
	// 成功借款总额
	public static double totalBorrowAmount;
	
	// 还款中的借款总额
	public static double repayingBorrowAmount;
	
	// 账户可用余额浮存
	public static double balanceFloatSum;
	
	//  更新时间
	public static Date date;

	public void doJob() {
		statistics();
	}

	private static void statistics() {
		ErrorInfo error = new ErrorInfo();
		
		// 注册总人数
		regCount = User.queryTotalRegisterUserCount(error);

		// 累计投资
		sumInvest = Bid.sumInvest().doubleValue();

		// 累计收益
		sumIncome = Bill.sumIncome();
		
		// 成功借款标数量
		releasedBidsNum = StatisticBorrow.queryReleasedBidsNum(error);
		
		//成功借款总额
		totalBorrowAmount = StatisticBorrow.queryTotalBorrowAmount(error);
		
		//还款中的借款总额
		repayingBorrowAmount = StatisticBorrow.queryAllRepayingBorrowAmount(error);
		
		//账户可用余额浮存
		balanceFloatSum = StatisticPlatformFloat.queryBalanceFloatsum();
		
		//更新时间
		date = new Date();

	}

}
