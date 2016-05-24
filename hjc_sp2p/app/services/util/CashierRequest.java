/*
 * Web Site: http://www.reapal.com
 * Since 2014 - 2015
 */
package services.util;

import java.io.Serializable;

public class CashierRequest implements Serializable {
    private static final long serialVersionUID = 3148176768559230877L;
    //接口名称
    private String service;		//online_pay，表示网上支付； 	N
    //商户ID
    private String merchant_id;	//合作伙伴在融宝的用户ID	N
    //通知URL
    private String notify_url;	//针对该交易的交易状态同步通知接收URL。（URL里不要带参数）	N
    //返回URL
    private String return_url;	//结果返回URL，仅适用于立即返回处理结果的接口。融宝处理完请求后，立即将处理结果返回给这个URL。（URL里不要带参数）	Y
    //签名
    private String sign;	//见签名机制
    //签名方式
    private String sign_type;	//见签名方式 N
    //参数编码字符集
    private String charset; //（默认为utf-8）	合作伙伴系统与融宝系统之间交互信息时使用的编码字符集。合作伙伴可以通过该参数指定使用何种字符集对传递参数进行编码。同时，融宝系统也会使用该字符集对返回参数或通知参数进行编码。注：该参数在POST方式发送请求时要附在action后。如：http://epay.reapal.com/portal?charset=gbk	N
    //商品名称
    private String title;	 //商品的名称	N
    //商品描述
    private String body;	//商品的具体描述	N
    //商户订单号
    private String order_no;	//String(64)	商户订单号（确保在合作伙伴系统中唯一），譬如：HJKM2011051189234	N
    //交易金额
    private Double total_fee;	//Number(13,2)	total_fee:单位为RMB Yuan	N

    //支付类型：跳转方式时，根据后端商户配置决定是否支持借记还是贷记；直列方式时，不填写按商户配置，填写按商户提供（需要校验是否开通）
    private String payment_type;	//1借记卡、2贷记卡
    //支付方式
    private String paymethod;	  //网银支付（跳转收银台）bankPay、directPay银行直连
    //网银代码：根据地址判断是手机还是Pc收银台
    private String defaultbank;	//请参考银行代码，例如：ICBC（网银B2C、融宝快捷）、ICBC_B2B（网银B2B）、ICBCLITE（网银快捷）
    //付款客户号
    private String pay_cus_no;	//银行之列时，民生、浦发、交通B2B时需要提交该字段
    //卖家Email
    private String seller_email;	//String(100)	卖家在融宝的注册Email. 	N
    //卖家
    private String seller_id;	//String(100)	卖家在融宝的注册Email. 	N
    //买家Email
    private String buyer_email;	//String(100)	买家在融宝的注册Email。	Y
    //买家Id
    private String buyer_id;	//String(100)	买家在融宝的注册Email。	Y
    //手续费
    private Double service_fee;	//Number(13,2)	单位为RMB Yuan，教育商户填此字段	Y
    //移动标示（不建议使用）
    private String terminal_type;	//String	固定值：mobile	N
    //IMEI号
    private String terminal_info;	//String	手机硬件IMEI号	N
    //格式
    private String format;  //xml    json
    //银行卡号
    private String card_no;
    //姓名
    private String owner;
    //证件类型
    private String cert_type;
    //证件号
    private String cert_no;
    //手机号
    private String phone;
    //短信验证码
    private String check_code;
    //金额
    private double amount;

    //鉴权标志
    private String identifyNo;

    //会员Id
    private String member_id;

    //交易流水号
    private String trade_no;


    private String isBase;

    //商户名称
    private String customerName;

    private String validthru;
    private String cvv2;

    //交易时间
    private int transtime;

    //用户IP
    private String member_ip;


    private String currency;
    
    
    private String orig_order_no;
    
    private String note;
    
    private String bind_id;
    
    private String bank_card_type;
    
    private String version;
    
    
    
    public String getBank_card_type() {
		return bank_card_type;
	}

	public void setBank_card_type(String bank_card_type) {
		this.bank_card_type = bank_card_type;
	}

	public String getBind_id() {
		return bind_id;
	}

	public void setBind_id(String bind_id) {
		this.bind_id = bind_id;
	}

	public String getOrig_order_no() {
		return orig_order_no;
	}

	public void setOrig_order_no(String orig_order_no) {
		this.orig_order_no = orig_order_no;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }


    public int getTranstime() {
        return transtime;
    }

    public void setTranstime(int transtime) {
        this.transtime = transtime;
    }

    public String getValidthru() {
        return validthru;
    }

    public void setValidthru(String validthru) {
        this.validthru = validthru;
    }

    public String getCvv2() {
        return cvv2;
    }

    public void setCvv2(String cvv2) {
        this.cvv2 = cvv2;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getIsBase() {
        return isBase;
    }

    public void setIsBase(String isBase) {
        this.isBase = isBase;
    }

    public String getTrade_no() {
        return trade_no;
    }

    public void setTrade_no(String trade_no) {
        this.trade_no = trade_no;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String getIdentifyNo() {
        return identifyNo;
    }

    public void setIdentifyNo(String identifyNo) {
        this.identifyNo = identifyNo;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCheck_code() {
        return check_code;
    }

    public void setCheck_code(String check_code) {
        this.check_code = check_code;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    //以下属性暂时没有：amount、pay_cus_no
    private Double price;       //单价
    private Integer quantity;   //数量

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMerchant_id() {
        return merchant_id;
    }

    public void setMerchant_id(String merchant_id) {
        this.merchant_id = merchant_id;
    }

    public String getNotify_url() {
        return notify_url;
    }

    public void setNotify_url(String notify_url) {
        this.notify_url = notify_url;
    }

    public String getReturn_url() {
        return return_url;
    }

    public void setReturn_url(String return_url) {
        this.return_url = return_url;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSign_type() {
        return sign_type;
    }

    public void setSign_type(String sign_type) {
        this.sign_type = sign_type;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public Double getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(Double total_fee) {
        this.total_fee = total_fee;
    }

    public String getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(String payment_type) {
        this.payment_type = payment_type;
    }

    public String getPaymethod() {
        return paymethod;
    }

    public void setPaymethod(String paymethod) {
        this.paymethod = paymethod;
    }

    public String getDefaultbank() {
        return defaultbank;
    }

    public void setDefaultbank(String defaultbank) {
        this.defaultbank = defaultbank;
    }

    public String getSeller_email() {
        return seller_email;
    }

    public void setSeller_email(String seller_email) {
        this.seller_email = seller_email;
    }

    public String getBuyer_email() {
        return buyer_email;
    }

    public void setBuyer_email(String buyer_email) {
        this.buyer_email = buyer_email;
    }

    public Double getService_fee() {
        return service_fee;
    }

    public void setService_fee(Double service_fee) {
        this.service_fee = service_fee;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public String getBuyer_id() {
        return buyer_id;
    }

    public void setBuyer_id(String buyer_id) {
        this.buyer_id = buyer_id;
    }

    public String getPay_cus_no() {
        return pay_cus_no;
    }

    public void setPay_cus_no(String pay_cus_no) {
        this.pay_cus_no = pay_cus_no;
    }

    public String getTerminal_type() {
        return terminal_type;
    }

    public void setTerminal_type(String terminal_type) {
        this.terminal_type = terminal_type;
    }

    public String getTerminal_info() {
        return terminal_info;
    }

    public void setTerminal_info(String terminal_info) {
        this.terminal_info = terminal_info;
    }

    public String getCard_no() {
        return card_no;
    }

    public void setCard_no(String card_no) {
        this.card_no = card_no;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCert_type() {
        return cert_type;
    }

    public void setCert_type(String cert_type) {
        this.cert_type = cert_type;
    }

    public String getCert_no() {
        return cert_no;
    }

    public void setCert_no(String cert_no) {
        this.cert_no = cert_no;
    }

    public String getMember_ip() {
        return member_ip;
    }

    public void setMember_ip(String member_ip) {
        this.member_ip = member_ip;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

