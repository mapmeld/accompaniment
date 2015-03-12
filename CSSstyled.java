
//Implementing this interface allows you to use a CSSEditor
public interface CSSstyled {
	
	//this requests the name of what is being edited, for a specific window title
	//returning null will use a generic name (NOT helpful)
	public String getName();
	
	//this requests the CSS currently in use
	//returning null will hide the "current" option until CSS is set
	public String getCurrentCSS();
	
	//this sends the CSS entered by the user
	public void setCurrentCSS(String set);
	
	//this requests the CSS for each preset option
	//returning null will open just the "custom" option
	public String[] getCSSOptions();
	
	//this requests names for each preset option
	//returning null is only acceptable if getCSSOptions() returns null
	public String[] getOptionNames();
}