import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;

def Message log01(Message message) {processData("log01", message);}
def Message log02(Message message) {processData("log02", message);}
def Message log03(Message message) {processData("log03", message);}
def Message log04(Message message) {processData("log04", message);}
def Message log05(Message message) {processData("log05", message);}
def Message log06(Message message) {processData("log06", message);}
def Message log07(Message message) {processData("log07", message);}

def Message processData(String prefix, Message message) {
	def headers = message.getHeaders();
	def body = message.getBody(java.lang.String) as String;
	def messageLog = messageLogFactory.getMessageLog(message);
	
	def properties = message.getProperties();
	//Read logger from the Message Properties
    //String logger = propertyMap.get("debug");
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