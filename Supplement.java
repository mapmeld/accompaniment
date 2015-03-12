//all Add-Ons extend Supplement

import javax.swing.JFrame;

public abstract class Supplement {
	
	private String name;

	//getType returns the Add-On's type (e.g. Outline, Slides, Map)
	public String getType(){
		return this.getClass().getName();
	}
	
	public String toString(){ return getName(); }
	
	//getDescription returns a short explanation of what the addon will provide
	//for example, outline organizes the video and lets users jump to a point in the video
	public abstract String getDescription();
	
	//getMiniView returns a String (you may use HTML) to place in a preview cell
	public String getMiniView(){
		return "<html><b>" + this.getType() + ": " + this.getName() + "</b><br><br>" + this.toString() + "</html>";
	}
	
	//setWindow uses the JFrame parameter to set up with the layout
	public abstract void setWindow(JFrame j);
	
	//getClassScripts returns the JavaScript needed for this addon type
	//it is called once for each class of addon used
	public String getClassScripts(){
		return null;
	}
	
	//getIndividualScripts returns the JavaScript needed for the particular addon instance
	//scripts will be reviewed by the user
	public String getIndividualScripts(String myID){
		return null;
	}
	
	//getLoadScript is JavaScript run when the video player loads
	//it is inserted directly into a function, so do not include <script> or function{
	public String getLoadScript(String myID){
		return null;
	}
	
	//getHTML returns the Add-On's JavaScript/HTML representation on the page
	//myID is the name that should be used for the all-encompassing div element of this supplement
	public abstract String getHTML(String myID);
	
	//saveToString returns all necessary information to reconstruct the Add-On
	//for consistency, please consider using XML data in this String
	public abstract String saveToString();
	
	//initFromString sets the Add-On's properties from its saved information
	public abstract void initFromString(String saveData);
	
	//refresh type follows one of the constants
	public static final int NO_REFRESHES = 0;
	public static final int TYPE_REFRESHES = 1;
	public static final int EACH_REFRESHES = 2;
	public int refreshType(){
		return NO_REFRESHES;
	}
	
	//The name is an identifier for the user's convenience
	public void setName(String n){ name = n; }
	public String getName(){ return name; }
}