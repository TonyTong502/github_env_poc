import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.JsonSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.MarkupBuilder;
import java.text.SimpleDateFormat;

def Message totalFailedCount(Message message) {
	try
	{
		def totalSuccessCount = message.getProperty("Prop_DRM_SuccessCount") as Integer;
		def totalCount = message.getProperty("Prop_DataCount") as Integer;
		message.setProperty("Prop_DRM_FailedCount",totalCount-totalCount);
	}
	catch(Exception ex)
	{
		def map =  message.getProperties();
		java.lang.String logger = map.get("logFile");
		if (logger == null){
			logger = new String();
		}
		logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
		logger += ">>>>>>Logger Script Exception:${ex}" + System.lineSeparator();
		message.setProperty("logFile",logger);
		throw ex;
	}
	return message;
}

def Message SetDateRange(Message message) { 
    try
    {   
        def dataReload = message.getProperty("Prop_DataReload");
		def dataReload_DateFrom = message.getProperty("Prop_DataReload_DateFrom");
		def dateFrom = message.getProperty("Prop_DateFrom");  
		def dateTo = message.getProperty("Prop_DateTo"); 
		def offset = message.getProperty("Prop_DateTime_Offset").toInteger();

        if (dataReload.toUpperCase() == 'TRUE')
        {
			message.setProperty("Prop_DateFrom", dataReload_DateFrom);
        }
        else
        {
            dateFrom = Date.parse("yyyy-MM-dd HH:mm:ss",dateFrom);
            dateFrom = dateFrom.plus(-offset);
            message.setProperty("Prop_DateFrom", dateFrom.format("yyyy-MM-dd HH:mm:ss"));
        }
     }
    catch(Exception ex)
    {
        def map =  message.getProperties();
        java.lang.String logger = map.get("logFile");
        if (logger == null){
            logger = new String();
        }
        logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
        logger += ">>>>>>Logger Script Exception:${ex}" + System.lineSeparator();
        message.setProperty("logFile",logger);
        throw ex;
    }
    return message;
}

def Message HttpRetry(Message message) {
   def map = message.getHeaders();
   def httpStatusCode = map.get("CamelHttpResponseCode") as Integer;
   def _loopIndex=message.getProperty("CamelLoopIndex") as Integer;
   def _propRetryLimit = message.getProperty("Prop_MaxRetryCount") as Integer;
   def _propRetryInterval=message.getProperty("Prop_RetryInterval") as Integer;
   def _retryFlag="TRUE";
   
   switch(httpStatusCode>=400)
   {
       case true:
       switch(_loopIndex+1<=_propRetryLimit) {
           case true:
               _retryFlag="TRUE";
               sleep(_propRetryInterval);
               break;
           case false:
               _retryFlag="FALSE";
               throw new Exception("HTTP request failed after exausting configured retry limit with status code:"+map.get("CamelHttpResponseCode")+" response body:"+ message.getBody(java.lang.String));
               break;
       }
       break;
       case false:
       _retryFlag="FALSE";
       break;
    }
    message.setProperty("Prop_IsRetry",_retryFlag)
    return message;
}

def Message LogDBResponse(Message message) {WriteLogFile("DBResponse", message);}
def Message LogDRMRequest(Message message) {WriteLogFile("DRMRequest", message);}
def Message LogDRMResponse(Message message) {WriteLogFile("DRMResponse", message);}
def Message LogDeltaFunctionResponse(Message message) {WriteLogFile("DeltaFunctionResponse", message);}

def Message WriteLogFile(String prefix, Message message) {
    def enableTracing = message.getProperty("Prop_Enable_Tracing");
    if(enableTracing.toUpperCase().equals("TRUE"))
    {
        def headers = message.getHeaders();
        def body = message.getBody(java.lang.String) as String;
        def messageLog = messageLogFactory.getMessageLog(message);
        def properties = message.getProperties();
        def logString="";
        for (header in headers) {
           logString = logString+"header:" + header.getKey().toString()+"\t"+header.getValue().toString()+"\r\n";
        }
        for (property in properties) {
           logString = logString+"property:" + property.getKey().toString()+"\t"+property.getValue().toString()+"\r\n"
        }
        if(messageLog != null){
            messageLog.addAttachmentAsString(prefix, logString+body, "text/plain");
        }
    }
    return message;
}

def Message RemoveXMLDeclaration(Message message) {
    def boby = message.getBody(java.lang.String) as String;
    def newBody = boby.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>","");
    message.setBody(newBody);
    return message;
}