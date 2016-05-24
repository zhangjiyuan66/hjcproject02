package jobs;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.zxing.BarcodeFormat;
import com.shove.code.Qrcode;
import com.shove.security.Encrypt;

import business.TemplateSms;
import business.TemplateStation;
import models.t_users;
import play.Logger;
import play.db.jpa.Blob;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;
import constants.Constants;
/**
 * 每5分钟扫描一次站短信缓存表，并发送短信
 *
 * @author hys
 * @createDate  2015年5月26日 下午3:36:03
 *
 */
@Every("5min")
public class SmsSend extends Job {
	@Override
	public void doJob() {
		
		TemplateSms.dealSmsTask();
		//重新生成二维码
		/*EntityManager em = JPA.em();
		List<t_users> users = t_users.findAll();
		for(int i = 0;i < users.size();i++)
		{
			String uuid = UUID.randomUUID().toString();
			Qrcode code = new Qrcode();
			
			try {
				Blob blob = new Blob();
				code.create(Constants.BASE_URL + "registerMobile?un=" + Encrypt.encrypt3DES(users.get(i).name, Constants.ENCRYPTION_KEY), BarcodeFormat.QR_CODE, 100, 100, new File(blob.getStore(), uuid).getAbsolutePath(), "png");
			} catch (Exception e) {
				Logger.info("创建二维码图片失败"+e.getMessage());
			}
			String sql = "update t_users set qr_code = ? where id = ?" ;
			Query updateBill = em.createNativeQuery(sql).setParameter(1, uuid).setParameter(2, i+1);
			updateBill.executeUpdate();
		}*/
	}
	
}