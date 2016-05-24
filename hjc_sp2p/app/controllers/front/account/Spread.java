package controllers.front.account;

import java.util.List;

import models.t_user_cps_income;
import models.v_user_cps_user_count;
import models.v_user_cps_users;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;
import annotation.InactiveUserCheck;
import annotation.RealNameCheck;
import business.BackstageSet;
import business.News;
import business.User;
import constants.Constants;
import controllers.BaseController;
import controllers.interceptor.AccountInterceptor;

@With({AccountInterceptor.class})
public class Spread extends BaseController {

	//-------------------------------GPS推广-------------------------
	//我的GPS链接
	@InactiveUserCheck
	@RealNameCheck
	public static void spreadLink(){
		User user = User.currUser();
	
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String url = Constants.BASE_URL;
		render(user, backstageSet,url);
	}
	
	//我成功推广的会员
	@InactiveUserCheck
	@RealNameCheck
	public static void spreadUser(){
		User user = User.currUser();
		long userId = user.id;
		
		String type = params.get("type");
		String key = params.get("key");
		String year = params.get("year");
		String month = params.get("month");
		String currPage = params.get("currPage");
		String pageSize = params.get("currSize");
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_cps_users> page = User.queryCpsSpreadUsers(userId ,type, key, 
				year, month, currPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_user_cps_user_count cpsCount = User.queryCpsCount(userId, error);
		
		/*查询用户所有的CPS收入*/
		double totalCpsIncome = User.queryTotalCpsIncome(userId);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(user, page, cpsCount, totalCpsIncome);
	}
	
	//推广会员详情
	public static void userDetail(){
		render();
	}
	
	/**
	 * 我的推广会员收入
	 */
	@InactiveUserCheck
	@RealNameCheck
	public static void spreadIncome(){
		User user = User.currUser();
		long userId = user.id;
		
		String year = params.get("year");
		String month = params.get("month");
		String currPage = params.get("currPage");
		String pageSize = params.get("currSize");
		
		ErrorInfo error = new ErrorInfo();
		
		PageBean<t_user_cps_income> page = User.queryCpsSpreadIncome(userId, 
				year,month,currPage,pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_user_cps_user_count cpsCount = User.queryCpsCount(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		double totalCpsIncome = User.queryTotalCpsIncome(userId);
		
		render(user, page, cpsCount, totalCpsIncome);
	}
	
	/**
	 * 推广收入明细
	 */
	public static void incomeDetail(){
		User user = User.currUser();
		long userId = user.id;
		
		String type = params.get("type");
		String key = params.get("key");
		String year = params.get("year");
		String month = params.get("month");
		String currPage = params.get("currPage");
		String pageSize = params.get("currSize");
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_cps_users> page = User.queryCpsSpreadUsers(userId ,type, key, 
				year, month, currPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(user, page, year, month);
	}
	
	
}
