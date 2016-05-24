/**
 * SDKService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package sms.ymrt.sdkhttp;

public interface SDKService extends javax.xml.rpc.Service {
    public java.lang.String getSDKServiceAddress();

    public sms.ymrt.sdkhttp.SDKClient getSDKService() throws javax.xml.rpc.ServiceException;

    public sms.ymrt.sdkhttp.SDKClient getSDKService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
