import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.time.TimeCategory;
import groovy.json.JsonSlurper;
import groovy.util.XmlSlurper;
import groovy.json.JsonOutput;

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

/*Get TaskIDs Details*/
def Message getTaskIDDetails(Message message) {
    
    def TaskMembersResponsePayload = message.getProperty("TaskMembersResponsePayload");
    if (TaskMembersResponsePayload != '{"results":[]}')
    {
        def jsonSlurper = new JsonSlurper()
        Object obj = jsonSlurper.parseText(TaskMembersResponsePayload);
        
        def TaskIDCount = obj.results.size()
        message.setProperty("TaskIDCount",TaskIDCount);
    }
    else
    {
        message.setProperty("TaskIDCount",0);
        message.setProperty("NoTaskID","NoTasktoUpdate");
    }
   return message;
}

/*Get TaskIDs Details*/
def Message getTaskIDDetailsDsphere(Message message) {
    
    def TaskMembersResponsePayload = message.getProperty("TaskMembersResponsePayload");
    def root = new XmlSlurper().parseText(TaskMembersResponsePayload)
    def taskRows = root.select_response.row
    def TaskIDCount = taskRows.size()
    
   // Set properties based on count
    if (TaskIDCount > 0) {
        message.setProperty("TaskIDCount", TaskIDCount)
    } else {
        message.setProperty("TaskIDCount", 0)
        message.setProperty("NoTaskID", "NoTasktoUpdate")
    }
    
   return message;
}

/*Get Next TaskIDs Details*/
def Message checkNextTaskID(Message message) {
    
    def TaskIDCurrentIndex = message.getProperty("vendor_id_index").toInteger();
    def TotalTaskIDCount = message.getProperty("TaskIDCount").toInteger();
    
    message.setProperty("nextid","false");
    if(TaskIDCurrentIndex < TotalTaskIDCount-1)
    {
        TaskIDNextIndexValue = TaskIDCurrentIndex+1;
        message.setProperty("vendor_id_index",TaskIDNextIndexValue);
        message.setProperty("nextid","true");
        message.setProperty("continue_vendor","true");
    }
    else
	{
        message.setProperty("continue_vendor","false");
    }
    
    
    return message;
}

/*Check Data Details*/
def Message checkData(Message message) {
    def body = message.getBody(java.lang.String) as String;
    sleep(1000);
    if(body.length()>2)
    {
        message.setProperty("noData","false");
    }
    else
    {
        message.setProperty("noData","true");
    }
	return message;
}

/*Assign Retryable Property Details*/
def Message assignRetryableDetails(Message message) {
        def map = message.getProperties();
        
        def ex = map.get("CamelExceptionCaught");
        def IsRetryable = "TRUE";
        if (ex!=null)
        {
            def httpStatus = ex.getStatusCode();
            def httpStatusNotRetryable = message.getProperty("httpStatusNotRetryable");
            
            if (httpStatusNotRetryable.contains(httpStatus.toString()))
            {
                message.setProperty("IsRetryable","FALSE");
            }
            else
            {
                message.setProperty("IsRetryable","TRUE");
            }
        }
        else
        {
            message.setProperty("IsRetryable","TRUE");
        }

       return message;
}

/*Check Task Members Response*/
def Message checkHANADBResponse(Message message) {
    def boby = message.getBody(java.lang.String) as String;
    def jsonSlurper = new JsonSlurper()
    Object obj = jsonSlurper.parseText(boby);
    message.setProperty("status",obj.status);
	message.setProperty("newTaskMembers",obj.taskMembers);
	return message;
}


/*Update TaskMembers Details*/
def Message updateTaskMembersIndex(Message message) {
	def body = message.getBody(java.lang.String)as String;
	//def index = message.getProperty("index");
	//def newIndex=index.toInteger()+1;
	//message.setProperty("index",newIndex.toString());
	
	
	def newTaskMembers = message.getProperty("newTaskMembers");
	def TaskMembersCount = message.getProperty("TaskMembersCount");
	
	message.setProperty("TaskMembersCount",newTaskMembers.toInteger()+TaskMembersCount.toInteger());
	return message;
}

/*Check Retry Interval Details*/
def Message checkRetryInterval(Message message) {
	def body = message.getBody(java.lang.String)as String;
	def CurrentRetryCount = message.getProperty("CurrentRetryCount");
	def DSRetryCount = message.getProperty("DSRetryCount");
	def ApproverRecordCount = "0";
	ApproverRecordCount = message.getProperty("ApproverRecordCount");

	if (CurrentRetryCount.toInteger() < DSRetryCount.toInteger() && ApproverRecordCount.toInteger() > 0)
	{
    	def newCurrentRetryCount=CurrentRetryCount.toInteger()+1;
    	message.setProperty("CurrentRetryCount",newCurrentRetryCount);
    	message.setProperty("isDSRetryAllowed","TRUE");
	}
	else
	{
	    message.setProperty("isDSRetryAllowed","FALSE");
	}
	return message;
}

/*Update DateTime*/
def Message updateDateTime(Message message) {
    def dateTo = message.getProperty("dateTo");
    def dateToDT = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'",dateTo);
    
    message.setProperty("dateTo", dateToDT.format("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    
    return message;
}

/*taskMembersMapping*/
def Message taskMembersMapping(Message message) {
	def body = message.getBody(java.lang.String)as String;
	def dateTo = message.getProperty("dateTo") ?: "";
	def approverId = message.getProperty("ApproverID") ?: "";
	def approverName = message.getProperty("ApproverName") ?: "";
	def taskId = message.getProperty("TaskID") ?: "";
	def parsed = new JsonSlurper().parseText(body)
    def users = parsed.data
   
    def outputRecords = users.collect { user ->
        return [
            dateTo         : dateTo,
            ApproverID     : approverId,
            ApproverName   : approverName,
            TaskID         : taskId,
            UserUniqueName : user.uniqueName,
            UserName       : user.name,
            EmailAddress   : user.emailAddress,
            PasswordAdapter: user.passwordAdapter,  // Overwriting for all
            deleteFlag     : "True"
        ]
    }
    
	
	def result = [record: outputRecords]
	message.setBody(JsonOutput.toJson(result))
// 	message.setBody(JsonOutput.prettyPrint(JsonOutput.toJson(finalJson)))
    message.setProperty("recordCount", outputRecords.size())
	return message;
}

