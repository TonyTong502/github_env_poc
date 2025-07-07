import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;

def Message log01(Message message) {processData("log01", message);}
def Message log02(Message message) {updateMPLLog("MPL_Log", message);}

def Message processData(String prefix, Message message) {
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
    return message;
}

def Message updateMPLLog(String prefix, Message message) {
    
	def body = message.getProperty("isRetryable");
	
	def messageLog = messageLogFactory.getMessageLog(message);
	
	if(messageLog != null)
	{
                messageLog.addAttachmentAsString(prefix, body, "text/plain");
    }
    return message;
}