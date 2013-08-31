package il.ac.huji.beepme.db;

import il.ac.huji.beepme.business.BeepMeApplication;

import com.parse.ParseObject;

public class Employee {
	
	private String username;
	private String password;
	private ParseObject object;
	
	public boolean selected = false;
	
	public Employee(String username, String password){
		object = new ParseObject("Employee");
		object.put("username", username);
		object.put("password", password);
		object.put("businessid", BeepMeApplication.businessID);
		
		this.username = username;
		this.password = password;
	}
	
	public Employee(ParseObject object){
		this.object = object;
		username = object.getString("username");
		password = object.getString("password");
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public ParseObject getParseObject(){
		return object;
	}
	
	public Employee setUsername(String username){
		this.username = username;
		
		return this;
	}
	
	public Employee setPassword(String password){
		this.password = password;
		
		return this;
	}
	
	public Employee setParseObject(ParseObject object){
		this.object = object;
		
		return this;
	}
}
