import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.time.TimeCategory;
import groovy.json.JsonSlurper;
import groovy.xml.MarkupBuilder;
import groovy.json.JsonOutput;
import static java.lang.Math.min

/*Check for Full Load and set Offset*/
def Message checkFullLoad(Message message) {
    def DataReload = message.getProperty("DataReload");
    def DataLoadDateFrom = message.getProperty("DataLoadDateFrom");
                                                  
    if (DataReload.toUpperCase() == 'TRUE')
    {
        message.setProperty("dateFrom", DataLoadDateFrom);
    }
    
    def dateFrom = message.getProperty("dateFrom");
    def dateToDT = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'",dateFrom);
    def dateTimeOffset = message.getProperty("dateTimeOffset") as Integer;
    use(TimeCategory) 
    {
        dateToDT = dateToDT - dateTimeOffset.minutes 
    }

    message.setProperty("dateFrom", dateToDT.format("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    
    return message;
}

/*Check for count and assign iteration*/
def Message checkCountLimit(Message message) {
    
    def body = message.getBody(java.lang.String) as String;
    def jsonSlurper = new JsonSlurper()
    Object obj = jsonSlurper.parseText(body);
    message.setProperty("count",obj.count);
    
    def count = obj.count;
    def index = message.getProperty("index");
    def top = message.getProperty("top");
    
    if(body.length()>50)
    {
        message.setProperty("noData","false");
        if (index.toInteger() - top.toInteger() < count.toInteger())
        {
            message.setProperty("continue","true");
        }
        else
    	{
            message.setProperty("continue","false");
        }
    }
    else
    {
        message.setProperty("noData","true");
    }
	return message;
}

/*Check HANA DB Response*/
def Message checkResponse(Message message) {
    def boby = message.getBody(java.lang.String) as String;
    def jsonSlurper = new JsonSlurper()
    Object obj = jsonSlurper.parseText(boby);
    message.setProperty("status",obj.status);
	message.setProperty("newCommonSupplier",obj.commonSupplier);
	return message;
}

/*Update Index & Supplier Count*/
def Message updateIndex(Message message) {
	def body = message.getBody(java.lang.String)as String;
	def index = message.getProperty("index");
	def top = message.getProperty("top");
	def newIndex=index.toInteger()+top.toInteger();
	message.setProperty("index",newIndex.toString());
	
	def newCommonSupplier = message.getProperty("newCommonSupplier");
	def supplierCount = message.getProperty("supplierCount");
	
	message.setProperty("supplierCount",newCommonSupplier.toInteger()+supplierCount.toInteger());
	return message;
}

/*Check For EmailSend*/
def Message checkSendEmail(Message message) {
    
	def supplierCount = message.getProperty("supplierCount");
	def sendSuccessEmailZeroRecord = message.getProperty("sendSuccessEmailZeroRecord");
	def sendSuccessEmail = message.getProperty("sendSuccessEmail");
	
	def isSendEmail="FALSE";
	
	switch(sendSuccessEmail){
	    case "TRUE":
	        switch(sendSuccessEmailZeroRecord){
                case "TRUE":
                    isSendEmail="TRUE";
                    break;
                default:
                    if(supplierCount.toInteger()>0){
                        isSendEmail="TRUE";
                    }
                    break;
	        }
	   default:
	        break;
	}
	
	message.setProperty("isSendEmail",isSendEmail);
    return message;
}

/*Update DateTime*/
def Message updateDateTime(Message message) {
    def dateTo = message.getProperty("dateTo");
    def dateToDT = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'",dateTo);
    
    message.setProperty("dateTo", dateToDT.format("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    
    return message;
}

/*HTTP Retry*/
def Message httpRetry(Message message) {

        def map = message.getHeaders();
        def  httpStatusCode = map.get("CamelHttpResponseCode") as Integer;
        def _loopIndex=message.getProperty("CamelLoopIndex") as Integer;
        def _propRetryLimit = message.getProperty("propRetryLimit") as Integer;
        def _propRetryInterval=message.getProperty("propRetryInterval") as Integer;
        def _retryFlag=false;
        
        switch(httpStatusCode>=400)
        {
            case true:
            switch(_loopIndex+1<=_propRetryLimit) {
                case true:
                    _retryFlag=true;
                    sleep(_propRetryInterval);
                    break;
                case false:
                    _retryFlag=false;
                    //def ex = properties.get("CamelExceptionCaught");
                    throw new Exception("HTTP request failed after exausting configured retry limit with status code:"+map.get("CamelHttpResponseCode")+" response body:"+ message.getBody(java.lang.String));
                    break;
            }
            break;
            case false:
            _retryFlag=false;
            break;
        }
        
        message.setProperty("propIsRetry",_retryFlag)
       
       return message;
}


def Message onlyUpdateIndex(Message message) {
	def body = message.getBody(String);
	def index = message.getProperty("index");
	def top = message.getProperty("top");
	def newIndex=index.toInteger()+top.toInteger();
	message.setProperty("index",newIndex.toString());

	return message;
}


def Message addBatchInfo(Message message){
    def topStr = message.getProperty("top")
    def indexStr = message.getProperty("index")
    
    def top =  topStr.toInteger()
    def index = (indexStr != null) ? indexStr.toInteger() : 0
    def batchNo = (index / top) + 1

    
    def body = message.getBody(String)
    def json = new JsonSlurper().parseText(body)
    
    def count = json.count.toInteger()
    def isLastBatch = (index + top >= count)
    def valueCount = json.value.size()
    
    message.setProperty("batchNo", batchNo)
    message.setProperty("batchSize", top)
    message.setProperty("lastBatch", isLastBatch.toString())
    message.setProperty("valueCount", valueCount)

    return message
}


def Message getJMSPayload(Message message){
   
    def result = []

   def rawPayload = message.getProperty("JMS_Raw_Payload")

    if (rawPayload != null) {
        def body = rawPayload.getText()
        
        if (body?.trim()) {
            def json = new JsonSlurper().parseText(body)
            if (json?.value instanceof List) {
                json.value.each { entry ->
                    result << [
                        SystemID   : entry.SystemID,
                        TimeCreated: entry.TimeCreated,
                        ANSystemId : entry.SystemId,
                        TimeUpdated: entry.TimeUpdated,
                        Active     : entry.Active,
                        Name_en    : entry.Name_en
                    ]
                }
            }
        }
    }
    message.setBody(JsonOutput.toJson(result))
    return message
}

def Message getJDBCResponse(Message message){
    def strbody = message.getBody(String)
    def xmlBody = new XmlParser().parseText(strbody)

    def responseNode = xmlBody.'StatementName_response'[0]

    def responseBody = [
        Status      : responseNode.STATUS?.text() ?: '',
        SuccessCount: responseNode.SUCCESSCOUNT?.text() ?: '',
        ErrorMessage: responseNode.ERRORMESSAGE?.text() ?: ''
    ]

    message.setProperty('JDDBC_Response_Status', responseBody.Status)
    message.setProperty('JDDBC_Response_SuccessCount', responseBody.SuccessCount)
    message.setProperty('JDDBC_Response_ErrorMessage', responseBody.ErrorMessage)

    return message
}

def Message getJDBCUPSertResponse(Message message){
    def strbody = message.getBody(String)
    def xmlBody = new XmlParser().parseText(strbody)

    def responseNode = xmlBody.'StatementName_response'[0]

    def responseBody = [
        Status      : responseNode.OUT_STATUS?.text() ?: '',
        SuccessCount: responseNode.OUT_COUNT?.text() ?: '',
        ErrorMessage: responseNode.ERRORMESSAGE?.text() ?: ''
    ]

    message.setProperty('JDDBC_UPSERT_Response_Status', responseBody.Status)
    message.setProperty('JDDBC_UPSERT_Response_SuccessCount', responseBody.SuccessCount)
    message.setProperty('JDDBC_UPSERT_Response_ErrorMessage', responseBody.ErrorMessage)

    return message
}

  
def Message JdbcResponseError(Message message) {
    def status = message.getProperty("JDDBC_Response_Status")
    
    if (status?.toLowerCase() == "error") {
        throw new RuntimeException("Status is error. Triggering Exception Subprocess.")
    }

    return message
}  

def Message countFailedRecords(Message message){
    def body = message.getBody(String)
    def parser = new XmlParser(false, false)
    def root = parser.parseText(body)
    def count = (message.getProperty("count") ?: "0") as Integer

    Set<String> uniqueRequests = new HashSet<>()
    int totalFailedCount = 0

    root.'**'.findAll { it.name() == 'e.Record' }.each { record ->
        def request = record.find { it.name() == 'e.Request' }?.text()
        def failedCountText = record.find { it.name() == 'e.FailedCount' }?.text()
        def failedCount = (failedCountText?.isInteger()) ? failedCountText.toInteger() : 0

        if (request && !uniqueRequests.contains(request)) {
            uniqueRequests.add(request)
            totalFailedCount += failedCount
        }
    }

    def successCount = count - totalFailedCount
    
    if (successCount < 0) {
        successCount = 0
    }
    
    message.setProperty("successCount", successCount)
    message.setProperty("totalFailedCount", totalFailedCount)

    return message
}