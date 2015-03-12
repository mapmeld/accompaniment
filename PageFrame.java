import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;


public class PageFrame extends JLabel implements CSSstyled {

	protected static Font displayFont = new Font("Arial", Font.BOLD, 12);
	protected static LineBorder defaultBorder = new LineBorder(Color.BLACK, 2, true);
	protected static LineBorder highlightBorder = new LineBorder(Color.ORANGE, 4, true);
	protected static LineBorder selectBorder = new LineBorder(Color.GREEN, 4, true);
	
	protected static final String blankStyle =
	"/* Blank Style */\r\n" +
	"#thisDiv {\r\n" +
	"/* Margins (separation from other blocks) */\r\n" +
	"	margin-left: 20px;\r\n" +
	"	margin-right: 20px;\r\n" +
	"	margin-top: 10px;\r\n" +
	"	margin-bottom: 5px;\r\n" +
	"}\r\n";
	protected static final String greenStyle =
	"/* \"Green\" Style */\r\n\r\n" +
	"#thisDiv {\r\n" +
	"	/* Font style */\r\n" +
	"	color: black;\r\n" +
	"	/* Background and border style */\r\n" +
	"	background-color: PaleGoldenrod;\r\n" +
	"	border: 3px solid DarkGreen;\r\n" +
	"	/* Padding (separation from borders) */\r\n" +
	"	padding: 8px;" +
	"	/* Margins (separation from other blocks) */\r\n" +
	"	margin-left: 20px;\r\n" +
	"	margin-right: 20px;\r\n" +
	"	margin-top: 10px;\r\n" +
	"	margin-bottom: 5px;\r\n\r\n" +
	"}";
	protected static String[] styleOptions = { blankStyle, greenStyle };
	protected static String[] styleNames = { "Blank", "Green" };
	
	protected Supplement mySupplement;
    protected String cssStyle = blankStyle;
	protected CSSEditor cssWindow;
    
    protected String playerName = "player";
    protected String header = "";
	
	public PageFrame(){
		setText("[Open Cell]");
		setHorizontalTextPosition(SwingConstants.CENTER);
		setFont(displayFont);
		setBorder(defaultBorder);
		mySupplement = null;
	}
	
	public boolean hasSupplement(){
		if(mySupplement == null){ return false; }
		return true;
	}
	
	public Supplement getSupplement(){
		return mySupplement;
	}
	
	public void setSupplement(Object h){
		if(h == null){
			mySupplement = null;
			this.setText("<html><b>" + getHeader() + "</b><br/>[Open Cell]</html>");
			return;
		}
		if(h.equals(vpPageTab.playerOption)){
			mySupplement = null;
			this.setText("<html><b>" + getHeader() + "</b><br/>[Video Player]</html>");
			vpPageTab.playerOption.setPlaced(this);
			return;
		}
		mySupplement = (Supplement) h;
		this.setText("<html><b>" + getHeader() + "</b><br/>" + mySupplement.getMiniView() + "</html>");
	}
	
	public void openWindow(){
		if(hasSupplement()){
			mySupplement.setWindow(new JFrame());
		}
	}
	
	public boolean setPlayerName(String name){
		if(mySupplement == null){
			playerName = name;
			return true;
		}
		return false;
	}
	
	public void setCSS(String edits){
		cssStyle = edits;
	}
	
	public String getHeader(){
		return header;
	}
	public void setHeader(String h){
		header = h;
		if(this.equals(vpPageTab.playerOption.getPlace())){
			this.setText("<html><b>" + getHeader() + "</b><br/>[Video Player]</html>");
		}
		else{
			if(mySupplement != null){
				this.setText("<html><b>" + getHeader() + "</b><br/>" + mySupplement.getMiniView() + "</html>");
			}
			else{
				this.setText("<html><b>" + getHeader() + "</b><br/>[Open Cell]</html>");
			}
		}
	}

	public void select(boolean selectState){
		if(selectState){
			this.setBorder(selectBorder);
		}
		else{
			this.setBorder(defaultBorder);			
		}
	}
	
	public void setHoverState(boolean hoverState){
		//true if mouse or other selection is on, false if being un-highlighted
		if(hoverState){
			//trying a border highlight
			this.setBorder(highlightBorder);
		}
		else{
			//undoing any highlighting
			this.setBorder(defaultBorder);
		}
	}

	public String[] getCSSOptions() { return styleOptions; }	
	public String[] getOptionNames() { return styleNames; }

	public String getCurrentCSS() { return cssStyle; }
	public void setCurrentCSS(String set) { cssStyle = set; }
}