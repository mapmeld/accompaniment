//If you are making your own Add-Ons, your Add-On must extend Supplement.
//To insert your Add-On as an option, go to each comment in this file with the term UserAddOn

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;


public class vpAddOnsTab extends JPanel implements ActionListener {

	//each supplement option is represented with a JRadioButton
	// UserAddOn : make sure to add "protected JRadioButton NAME = new JRadioButton(); "
	protected final JRadioButton outlineOpt = new JRadioButton();
	protected final JRadioButton slidesOpt = new JRadioButton();
	protected final JRadioButton autoOpt = new JRadioButton();
	protected final JRadioButton mapOpt = new JRadioButton();
	protected final JRadioButton visitOpt = new JRadioButton();
	protected final JRadioButton passportOpt = new JRadioButton();
	protected final JRadioButton profilesOpt = new JRadioButton();
	protected final JRadioButton stackPanelOpt = new JRadioButton();
	protected final JRadioButton storylinesOpt = new JRadioButton();
	
	protected JRadioButton currentSelection;
	protected Panel optPanel = new Panel();
	protected ButtonGroup b = new ButtonGroup();
	
	//defining AddOn information box
	protected Label addonType;
	protected JLabel addonDescription;
	protected final JButton selectButton = new JButton("Add Outline");	
	protected static Font itemFont = new Font("Arial", Font.BOLD, 14);
	protected final BorderLayout addonsLayout = new BorderLayout(3,3);
	
	//defining the AddOn list
	protected final JTextField addonName = new JTextField("");
	protected final JList addonList = new JList(vpMain.addonData);
	
	//the candidate object is the currently-selected Add-On
	protected Supplement candidate;
	
	public vpAddOnsTab(){
		//defining the tab layout
		JPanel selectPanel = new JPanel();
		selectPanel.setBorder(vpMain.labelBorder);
		addonsLayout.setHgap(20);
		addonsLayout.setVgap(15);
		selectPanel.setLayout(addonsLayout);
		
		//defining the AddOn-naming box
		Panel titlePanel = new Panel();
		titlePanel.setLayout(new GridLayout(1,2));
		addonName.setFont(vpMain.messageFont);
		titlePanel.add(addonName);
		selectButton.addActionListener(this);
		titlePanel.add(selectButton);
		selectPanel.add(titlePanel, BorderLayout.NORTH);

		//defining the AddOn option selection
		addOption(outlineOpt, "Outline");		
		addOption(slidesOpt, "Slides");
		addOption(autoOpt, "AutoComplete");	
		addOption(mapOpt, "Maps");
		addOption(visitOpt, "FirstVisit");
		addOption(passportOpt, "Passport");
		addOption(profilesOpt, "Profiles");
		addOption(stackPanelOpt, "StackPanel");
		addOption(storylinesOpt, "Storylines");
		
		// UserAddOn : add your option
		// addOption(NAME, "CLASS_NAME");
		
		//The Outline is selected by default
		outlineOpt.setSelected(true);
		currentSelection = outlineOpt;
		candidate = new Outline();
		
		optPanel.setLayout(new BoxLayout(optPanel, BoxLayout.Y_AXIS));
		selectPanel.add(optPanel, BorderLayout.WEST);
		
		//defining the panel for information on the Add-On
		Panel addmePanel = new Panel();
		BorderLayout addmeLayout = new BorderLayout();
		addmePanel.setLayout(addmeLayout);
		addonType = new Label(candidate.getType());
		addmePanel.add(addonType, BorderLayout.NORTH);
		addonDescription = new JLabel(candidate.getDescription());
		addmePanel.add(addonDescription, BorderLayout.CENTER);
		selectPanel.add(addmePanel, BorderLayout.CENTER);
		this.add(selectPanel);

		//defining the list of created Add-Ons
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
    	addonList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    	addonList.setLayoutOrientation(JList.VERTICAL_WRAP);
    	addonList.setVisibleRowCount(6);
    	addonList.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent arg0) {
				vpMain.addonData.elementAt(addonList.getSelectedIndex()).setWindow(new JFrame());
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
    	});
		listPanel.add(addonList, BorderLayout.CENTER);
		this.add(listPanel);
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}
	
	//addOption standardizes option lists
	public void addOption(JRadioButton opt, String name){
		opt.setText(name);
		opt.setSelected(false);
		opt.setFont(itemFont);
		b.add(opt);
		optPanel.add(opt);
		opt.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == selectButton){
			String name = addonName.getText().trim();
			if(name.equals("") || name == null){
				candidate.setName(candidate.getType() + " " + vpMain.addonData.size());
			}
			else{
				candidate.setName(name);
			}
			candidate.setWindow(new JFrame());
			addSupplement(candidate);
			return;
		}
		
		//these should be at the end of the event handler
		if(e.getSource() != currentSelection){
			currentSelection = (JRadioButton) e.getSource();
			candidate = initSupplement(currentSelection.getText());
			addonType.setText(candidate.getType());
			addonDescription.setText(candidate.getDescription());
			selectButton.setText("Add " + candidate.getType());
		}
		//do not place any other event sources after the options
	}
	
	
	//a supplement is added to the list, and then the list is updated
	public void addSupplement(Supplement hs){
		vpMain.addonData.add(hs);
		addonList.setListData(vpMain.addonData);
		candidate = initSupplement(currentSelection.getText());
	}
	
	//Making an Supplement of the requested type
	public Supplement initSupplement(String type){
		if(type.equals("Outline")){
			return new Outline();
		}
		if(type.equals("Slides")){
			return new Slides();
		}
		if(type.equals("AutoComplete")){
			return new AutoComplete();
		}
		if(type.equals("Maps")){
			return new Maps();
		}
		if(type.equals("FirstVisit")){
			return new FirstVisit();
		}
		if(type.equals("Passport")){
			return new Passport();
		}
		if(type.equals("Profiles")){
			return new Profiles();
		}
		if(type.equals("StackPanel")){
			return new StackPanel();
		}
		if(type.equals("Storylines")){
			return new Storylines();
		}
		
		// UserAddOn : Using the default constructor
		//if(type.equals("CLASS_NAME")){
		//   return new CLASS_NAME();
		//}
		
		//returns null if the type was not recognized...
		return null;
	}
}