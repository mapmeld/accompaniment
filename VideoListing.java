//a VideoListing represents a video in the project

public class VideoListing{
	//there is currently only service, youtube.com
	private String service;
	private String uniqueURL;
	private String name;
	
	public VideoListing(String n, String url){
		name = n;
		uniqueURL = url;
		service = "youtube.com";
	}
	
	public String toString(){
		return this.getName() + " (" + this.getURL() + ")";
	}
	
	public void setName(String n){ name = n; }
	public String getName(){ return name; }
		
	public void setURL(String u){ uniqueURL = u; }
	public String getURL(){ return uniqueURL; }
	
	public void setService(String s){ service = s; }
	public String getService(){ return service; }
}