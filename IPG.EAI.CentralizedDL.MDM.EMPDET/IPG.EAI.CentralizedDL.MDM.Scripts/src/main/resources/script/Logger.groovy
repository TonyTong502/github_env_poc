import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPathConstants;

/*
Call this method at begining to initialize log
*/
def Message initLog(Message message) {
    def map =  message.getProperties();
    // Logging
    java.lang.String logger = map.get("logFile");
        if (logger == null){
            logger = new String();
        }
    try
    {
        logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
        logger += ">>>>>>Process log Started........... " + System.lineSeparator();
        // Store the logfile as property on the message object
        message.setProperty("logFile",logger);
    }
    catch(Exception ex)
    {
        logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
        logger += ">>>>>>Logger Script Exception:${ex}" + System.lineSeparator();
        message.setProperty("logFile",logger);
    }
	return message;
}
/*
Call this method to log information  
*/
def Message logInfo(Message message) {
    def headers = message.getHeaders();
    def properties =  message.getProperties();
    java.lang.String logger = properties.get("logFile");
    if (logger == null){
    logger = new String();
        }
    try
    {
        def _logger_ProcessingStage=message.getProperty("logger_Stage");
            _logger_level=message.getProperty("logger_Level");
        def body = message.getBody(java.lang.String) as String;  
        
            def logString="";
            if(_logger_level.toUpperCase().equals("DEBUG"))
            {
                for (header in headers) {
                logString = logString+"header:" + header.getKey().toString()+"\t"+header.getValue().toString()+"\r\n";
                }
                for (property in properties) {
                    if(property.getKey().toString()!="logFile" && property.getKey().toString()!="CamelMessageHistory")
                    {
                        logString = logString+"property:" + property.getKey().toString()+"\t"+property.getValue().toString()+"\r\n"
                    }
                }
            }
            else
            {
                for (property in properties) {
                    if(property.getKey().toString().contains("logger_info"))
                    {
                       if(!logger.contains(property.getKey().toString()))
                        {

                            logString = logString+"property:" + property.getKey().toString()+"\t"+property.getValue().toString()+"\r\n"
                        }
                    }
                }
            }
        logger += CurrentDateTime() + System.lineSeparator();
        if(_logger_ProcessingStage!=null)
        {
            logger += _logger_ProcessingStage + System.lineSeparator()  ;
        }
        logger += logString + System.lineSeparator();
        // Store the logfile as property on the message object
        message.setProperty("logFile",logger); 
    }    
    catch(Exception ex)
    {
        logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
        logger += ">>>>>>Logger Script Exception:${ex}" + System.lineSeparator();
        message.setProperty("logFile",logger);
    }  
    return message;
}

/**
 * Stores the logFile as attachment of the message processing
 * It can be found in the message processing dashboard in CPI Message Monitoring
**/
def Message storeLog(Message message) {
    //Properties 
    def properties =  message.getProperties();
    java.lang.String logger = properties.get("logFile");
    if (logger == null){
    logger = new String();
    }
    try
    {
        logger += CurrentDateTime() + System.lineSeparator();
        logger += ">>>>>>Process log Completed........... " + System.lineSeparator();
        // Store the logfile as property on the message object
        message.setProperty("logFile",logger);       
        // Store LOG file on MSGLog 
        def messageLog = messageLogFactory.getMessageLog(message);
        if (messageLog != null) {
            messageLog.addAttachmentAsString("logFile" , logger, "text/plain");
        }
    }
    catch(Exception ex)
    {
        logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
        logger += ">>>>>>Logger Script Exception:${ex}" + System.lineSeparator();
        message.setProperty("logFile",logger);
    }
    return message;
}

/*
  Call this method in exception subprocess to capture exception details
*/
def Message logException(Message message) {
       //Properties 
       def properties =  message.getProperties();
        java.lang.String logger = properties.get("logFile");
        if (logger == null){
            logger = new String();
        }
       try
       {
            def ex = properties.get("CamelExceptionCaught");
            logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
            logger += "Exception Details: " + System.lineSeparator()  ;
            def exceptionMsg=ex.getMessage();
            if(exceptionMsg!=null)
            {
                    logger += exceptionMsg + System.lineSeparator()  ;
            }
            // Store the logfile as property on the message object
            message.setProperty("logFile",logger);
       }
       catch(Exception ex)
       {
        logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
        logger += ">>>>>>Logger Script Exception:${ex}" + System.lineSeparator();
        message.setProperty("logFile",logger);
       }
       // Call methode to store Log
       storeLog(message);
}
def Message logExceptionDataStore(Message message) {
       //Properties 
       def body = message.getBody(java.lang.String) as String;
       def properties =  message.getProperties();
        java.lang.String logger = properties.get("logFile");
        if (logger == null){
            logger = new String();
        }
       try
       {
            logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
            logger += "Exception Details: " + System.lineSeparator();
            //logger += body.toString() + System.lineSeparator()  ;
            message.setProperty("logFileException",body);
            if(body!=null)
            {
                    logger += body.toString() + System.lineSeparator()  ;
            }
            // Store the logfile as property on the message object
            message.setProperty("logFile",logger);
       }
       catch(Exception ex)
       {
        logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
        logger += ">>>>>>Logger Script Exception:${ex}" + System.lineSeparator();
        message.setProperty("logFile",logger);
       }
       return message;
}
def Message logExceptionAppend(Message message) {
       //Properties 
       def properties =  message.getProperties();
       java.lang.String logger = properties.get("logFile");
        if (logger == null){
            logger = new String();
        }
       try
       {
            
            def ex = properties.get("CamelExceptionCaught");
            logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
            logger += "Exception Details: " + System.lineSeparator()  ;
            def exceptionMsg=ex.getMessage();
            if(exceptionMsg!=null)
            {
                    logger += exceptionMsg + System.lineSeparator()  ;
            }
            // Store the logfile as property on the message object
            message.setProperty("logFile",logger);
       }
       catch(Exception ex)
       {
        logger += ">>>>>>"+CurrentDateTime() + System.lineSeparator();
        logger += ">>>>>>Logger Script Exception:${ex}" + System.lineSeparator();
        message.setProperty("logFile",logger);
       }
       return message;
}

/*
 Returns current datetime in UTC format   
*/
def String CurrentDateTime()
{
    TimeZone.setDefault(TimeZone.getTimeZone('UTC'))
    def now = new Date()
    return now.toString();
   
}