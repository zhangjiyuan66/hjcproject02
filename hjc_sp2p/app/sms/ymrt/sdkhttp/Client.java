package sms.ymrt.sdkhttp;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import business.BackstageSet;
public class Client {
	private String softwareSerialNo;
	private String key;

	private static Client client=null;
	
	public Client(String sn,String key){
		this.softwareSerialNo=sn;
		this.key=key;
		init();
	}
	
	public synchronized static Client getClient(String softwareSerialNo,String key){
		if(client==null || softwareSerialNo == null || key == null || StringUtils.isBlank(client.softwareSerialNo) || 
				!client.softwareSerialNo.equals(softwareSerialNo) ||
				StringUtils.isBlank(client.key) || 
				!client.key.equals(key) ){
			try {
				client=new Client(softwareSerialNo,key);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return client;
	}
	
	
	SDKServiceBindingStub binding;
	
	
	public void init(){
		 try {
            binding = (SDKServiceBindingStub)
                          new SDKServiceLocator().getSDKService();
            this.registEx(key);
		 }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
        } catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public int chargeUp(  String cardNo,String cardPass)
			throws RemoteException {
		int value=-1;
		value=binding.chargeUp(softwareSerialNo, key, cardNo, cardPass);
		return value;
	}

	public double getBalance() throws RemoteException {
		double value=0.0;
		value=binding.getBalance(softwareSerialNo, key);
		return value;
	}

	public double getEachFee( ) throws RemoteException {
		double value=0.0;
		value=binding.getEachFee(softwareSerialNo, key);
		return value;
	}
	public List<Mo> getMO( ) throws RemoteException {
		Mo[] mo=binding.getMO(softwareSerialNo, key);
		
		if(null == mo){
			return null;
		}else{
			List<Mo> molist=Arrays.asList(mo);
		    return molist;
		}
	}
	

	public List<StatusReport> getReport( )
			throws RemoteException {
		StatusReport[] sr=binding.getReport(softwareSerialNo, key);
		if(null!=sr){
			return Arrays.asList(sr);
		}else{
			return null;
		}
	}


	public int logout( ) throws RemoteException {
		int value=-1;
		value=binding.logout(softwareSerialNo, key);
		return value;
	}

	public int registDetailInfo(
			String eName, String linkMan, String phoneNum, String mobile,
			String email, String fax, String address, String postcode
) throws RemoteException {
		int value=-1;
		value=binding.registDetailInfo(softwareSerialNo, key, eName, linkMan, phoneNum, mobile, email, fax, address, postcode);
		return value;
	}

	public int registEx(String password)
			throws RemoteException {
		int value=-1;
		value=binding.registEx(softwareSerialNo, key, password);
		return value;
	}

	public int sendSMS( String[] mobiles, String smsContent, String addSerial,int smsPriority)
			throws RemoteException {
		int value=-1;
		value=binding.sendSMS(softwareSerialNo, key,"", mobiles, "【"+BackstageSet.getCurrentBackstageSet().platformName+"】"+smsContent, addSerial, "gbk", smsPriority,0);
		Logger.info("*****发短信返回值：%s",value);
		return value;
	}
	
	public int sendScheduledSMSEx(String[] mobiles, String smsContent,String sendTime,String srcCharset)
	throws RemoteException {
      int value=-1;
      value=binding.sendSMS(softwareSerialNo, key, sendTime, mobiles, smsContent, "", srcCharset, 3,0);
      return value;
	}
	public int sendSMSEx(String[] mobiles, String smsContent, String addSerial,String srcCharset, int smsPriority,long smsID)
	throws RemoteException {
      int value=-1;
      value=binding.sendSMS(softwareSerialNo, key,"", mobiles, smsContent,addSerial, srcCharset, smsPriority,smsID);
      return value;
	}

	public String sendVoice(String[] mobiles, String smsContent, String addSerial,String srcCharset, int smsPriority,long smsID)
			throws RemoteException {
		     String value=null;
		      value=binding.sendVoice(softwareSerialNo, key,"", mobiles, smsContent,addSerial, srcCharset, smsPriority,smsID);
		      return value;
	}
	
	public int serialPwdUpd( String serialPwd, String serialPwdNew)
			throws RemoteException {
		int value=-1;
		value=binding.serialPwdUpd(softwareSerialNo, key, serialPwd, serialPwdNew);
		return value;
	}
}
