import javax.swing.JFrame;

//StackPanel derives from Outline
//second-level items are hidden in menus below the top-level items

public class StackPanel extends Outline implements CSSstyled {
		
	public String getDescription() {
		return "<html><p>The <b>StackPanel</b> add-on organizes the video with a structured outline.  Lists of bullets are compacted into categories</p><br><p>An outline is a useful addition to long videos or for jumping to important information.</p></html>";
	}
	
	//gets the JavaScript for showing submenus
	public String getIndividualScripts(String myID){
		String buildScript =
		"<script type=\"text/javascript\">\r\n" +
		"	var " + myID + "_openMenu = -1;\r\n" +
		"function " + myID + "_loadItems(menu){\r\n" +
		"	if(menu == " + myID + "_openMenu){\r\n" +
		"		" + myID + "_openMenu = -1;\r\n" +
		"		document.getElementById(\"" + myID + "_menu\" + menu).style.display = \"none\";\r\n" +
		"	}\r\n" +
		"	else{\r\n" +
		"		document.getElementById(\"" + myID + "_menu\" + menu).style.display = \"block\";\r\n" +
		"		if(" + myID + "_openMenu != -1){\r\n" +
		"			document.getElementById(\"" + myID + "_menu\" + " + myID + "_openMenu).style.display = \"none\";\r\n" +
		"		}\r\n" +
		"		" + myID + "_openMenu = menu;\r\n" +
		"	}\r\n" +
		"}\r\n" +
		"</script>\r\n";
		return buildScript;
	}

	//Will send the HTML for setting up this AddOn
	public String getHTML(String myID) {
		int menuNum = 0;
		String outputHTML = "<div id=\"" + myID + "_StackPanel\">\r\n";
		outputHTML += "<style type=\"text/css\">\r\n" + myCSS.replace("#outline", "#" + myID + "_StackPanel").replace("firstlevel", myID + "_firstlevel").replace("secondlevel", myID + "_secondlevel") + "\r\n</style>\r\n";
		outputHTML += "<ul id=\"" + myID + "outline\">\r\n";
		for(int i=0; i<outlineData.size(); i++){
			OutlineItem v = outlineData.elementAt(i);
			if(!defaultItem.equals(v.getLabel())){
				if(!v.isChild()){
					if(i < (outlineData.size() - 1)){
						if(outlineData.elementAt(i+1).isChild()){
							outputHTML += "<li class=\"" + myID + "_firstlevel\" onclick=\"" + myID + "_loadItems(" + menuNum + ");\">&rarr; ";
						}
						else{
							outputHTML += "<li class=\"" + myID + "_firstlevel\" onclick=\"" + myID + "_loadItems(" + menuNum + ");\">";
						}
					}
					else{
						outputHTML += "<li class=\"" + myID + "_firstlevel\" onclick=\"" + myID + "_loadItems(" + menuNum + ");\">";
					}
					if((v.getVideo() != null)&&(!vpVideoSelector.blankVideo.equals(v.getVideo()))){
						outputHTML += "<a href=\"javascript:void(0);\" onclick=\"loadYT('" + v.getVideo().getURL() + "', " + v.getStart() + ");\">" + v.getLabel() + "</a></li>\r\n";
					}
					else{
						outputHTML += v.getLabel() + "</li>\r\n";
					}
					outputHTML += "<!--Hidden SubMenu--><div id=\"" + myID + "_menu" + menuNum + "\" style=\"display: none;\"><ul>\r\n";
					i++;
					while((i < outlineData.size()) && (outlineData.elementAt(i).isChild())){
						v = outlineData.elementAt(i);
						outputHTML += "<li class=\"" + myID + "_secondlevel\">";
						if(!v.getVideo().equals(vpVideoSelector.blankVideo)){
							outputHTML += "<a href=\"javascript:void(0);\" onclick=\"loadYT('" + v.getVideo().getURL() + "', " + v.getStart() + ");\">" + v.getLabel() + "</a></li>\r\n";
						}
						else{
							outputHTML += v.getLabel() + "</li>\r\n";
						}
						i++;
					}
					i--;
					outputHTML += "</ul></div>\r\n";
					menuNum++;
				}
			}
		}
		outputHTML += "</ul>\r\n</div>";
		return outputHTML;
	}

	public void setWindow(JFrame j) {
		super.setWindow(j);
		myWindow.setTitle("StackPanel: " + this.getName());
	}
}