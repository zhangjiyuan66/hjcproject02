package jobs;

import javax.persistence.EntityManager;

import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

/**
 * 每天5点统计CPS用户信息
 * 
 * @author fei
 *
 */
//@On("0 0 5 * * ?")
@Every("10min")
public class CpsUserInfoJob extends Job {
	@Override
	public void doJob() {

		EntityManager em = JPA.em();

		String deleteSql = "delete from  t_cps_info";
		String insertSql = "INSERT INTO t_cps_info ( id, NAME, time,  register_length,credit_level_image_filename, recommend_count, recharge_count, bid_amount, commission_amount ) SELECT `t_users`.`id` AS `id`, `t_users`.`name` AS `name`, `t_users`.`time` AS `time`, timestampdiff(DAY, `t_users`.`time`, now()) AS `register_length`, ( SELECT t_credit_levels.image_filename AS credit_level_image_filename FROM t_credit_levels WHERE ( t_credit_levels.id = t_users.credit_level_id ) ) AS credit_level_image_filename, ( SELECT count(`user`.`id`) AS `COUNT(user.id)` FROM `t_users` `user` WHERE ( `user`.`recommend_user_id` = `t_users`.`id` ) ) AS `recommend_count`, ( SELECT count(`user`.`id`) AS `COUNT(user.id)` FROM `t_users` `user` WHERE ( ( `user`.`recommend_user_id` = `t_users`.`id` ) AND (`user`.`is_active` = 1) ) ) AS `recharge_count`, ( SELECT ifnull(sum(`t_bids`.`amount`), 0) AS `bid_amount` FROM `t_bids` WHERE ( `t_bids`.`user_id` IN ( SELECT DISTINCT `user`.`id` AS `id` FROM `t_users` `user` WHERE `user`.`recommend_user_id` IN ( SELECT DISTINCT `users`.`id` AS `id` FROM `t_users` `users` WHERE ( `users`.`recommend_user_id` = `t_users`.`id` ) ) ) AND (`t_bids`.`status` IN(4, 5)) ) ) AS `bid_amount`,  ( SELECT ifnull(sum(`ucp`.`cps_reward`), 0) AS `cps_reward` FROM `t_user_cps_income` `ucp` WHERE ( `ucp`.`user_id` = `t_users`.`id` ) ) AS `commission_amount` FROM `t_users` WHERE `t_users`.`id` IN ( SELECT DISTINCT `t_users`.`recommend_user_id` AS `recommend_user_id` FROM `t_users` WHERE ( `t_users`.`recommend_user_id` > 0 ) ) ";
		String updateRate = "update t_cps_info t set t.active_rate = t.recharge_count /t.recommend_count";
		String updateInvestAmount = "UPDATE t_cps_info t SET t.invest_amount = ( SELECT ifnull( sum(`t_invests`.`amount`), 0 ) AS `IFNULL(sum(t_invests.amount),0)` FROM (`t_invests`, `t_bids`) WHERE (  `t_invests`.`bid_id` = `t_bids`.`id`  AND `t_bids`.`status` IN(3, 4, 5) AND `t_invests`.`user_id` IN ( SELECT `user`.`id` AS `id` FROM `t_users` `user` WHERE  `user`.`recommend_user_id` = t.id  ) ) )";
		
		try {
			em.createNativeQuery(deleteSql).executeUpdate();
			em.createNativeQuery(insertSql).executeUpdate();
			em.createNativeQuery(updateRate).executeUpdate();
			em.createNativeQuery(updateInvestAmount).executeUpdate();
		} catch (Exception e) {
			Logger.error("update t_cps_info exception", e);
			JPA.setRollbackOnly();
		}

	}
}
