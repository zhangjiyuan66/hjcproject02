package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 标的图片列表
 * 
 * @author zb
 * @version 8.0
 * @created 2015-11-30 上午011:19:49
 */
@Entity
public class t_bid_imagefile_name extends Model {
	public Long bid_id;
	public String image_file_name;
	public Date upload_time;

	
	public t_bid_imagefile_name(){

	}
	
	public t_bid_imagefile_name(Long bid_id ,String image_file_name,Date upload_time) {
             this.bid_id = bid_id;
             this.image_file_name = image_file_name;
             this.upload_time = upload_time;
	}

}
