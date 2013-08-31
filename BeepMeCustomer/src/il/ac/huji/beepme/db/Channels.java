package il.ac.huji.beepme.db;

public class Channels {
	
	public static String getBusinessChannel(String businessID){
		return businessID.replace(" ", "_");
	}
	
	public static String getQueueChannel(String businessID, String queueName){
		return businessID.replace(" ", "_") + "_" + queueName.replace(" ", "_");		
	}

	public static String[] getAllChannels(String businessID, String queueName){		
		return new String[]{getBusinessChannel(businessID), getQueueChannel(businessID, queueName)};
	}
}
