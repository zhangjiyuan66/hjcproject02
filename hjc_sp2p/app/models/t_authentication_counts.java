package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 用户当日实名认证次数记录
 * @author zkai
 */
@Entity
public class t_authentication_counts extends Model{

	public long user_id;
	public Date time;
	public int count;
}
