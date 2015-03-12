
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

//this is a simple form for editing CSS
//the user can select from options, or directly edit the Custom option

public class CSSEditor extends JFrame {
	protected static Dimension windowSize = new Dimension(600,400);
	
	protected CSSstyled editItem;
	protected JTextArea cssText;
	protected String customCSS;
	protected boolean hasbeenSeen = false;
	protected CSSOption currentOpt;
	protected CSSOption customOpt;
	protected JButton copyToCustom;
	
	protected ActionListener optionListener = new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			try{
				CSSOption firedOption = (CSSOption) e.getSource();
				if(firedOption.isSelected()){
					cssText.setText(firedOption.getCSS());
				}
				onCustomOpt(firedOption.equals(customOpt));
			}catch(Exception q){}
		}
	};
	
	public void onCustomOpt(boolean yes){
		if(yes){
			cssText.setEditable(true);
			copyToCustom.setEnabled(false);
		}else{
			cssText.setEditable(false);
			copyToCustom.setEnabled(true);			
		}
	}
	
	public CSSEditor(CSSstyled e){
		String name = e.getName();
		if(name == null){ name = e.getClass().toString(); }
		this.setTitle("Style Editor: " + name);
		editItem = e;
	}
	
	public void editStyle() {
		String[] css = editItem.getCSSOptions();
		String[] options = editItem.getOptionNames();
		String current = editItem.getCurrentCSS();
		if((!this.isVisible())&&(!hasbeenSeen)){
			this.setVisible(true);
			hasbeenSeen = true;
			this.setSize(windowSize);
			this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
			JPanel allOptions = new JPanel();
			ButtonGroup b = new ButtonGroup();
			allOptions.setLayout(new FlowLayout());
			
			String initialCSS = "";
			currentOpt = new CSSOption("Current", current);
			b.add(currentOpt);
			allOptions.add(currentOpt);
			if(current == null){
				currentOpt.setVisible(false);
			}
			else{
				currentOpt.setSelected(true);
				initialCSS = current;
			}
			
			CSSOption setOption;
			String thisCSS;
			String thisName;
			if(css != null){
				for(int i=0; i<options.length; i++){
					thisCSS = css[i];
					if(thisCSS == null){ continue; }
					thisName = null;
					if(options != null){ thisName = options[i]; }
					if(thisName == null){ thisName = "" + i; }
					setOption = new CSSOption(thisName, thisCSS);
					b.add(setOption);
					allOptions.add(setOption);
					if(initialCSS == null){
						setOption.setSelected(true);
						initialCSS = thisCSS;
					}
				}
			}
			
			customOpt = new CSSOption("Custom", "/* Copy from another option or edit here */\r\n");
			b.add(customOpt);
			allOptions.add(customOpt);
			if(initialCSS == null){ initialCSS = customOpt.getCSS(); }
			
			this.add(allOptions);
			cssText = new JTextArea(initialCSS);
			cssText.addKeyListener(new KeyListener(){
				public void keyPressed(KeyEvent arg0) {}
				public void keyReleased(KeyEvent arg0) {
					if(cssText.isEditable()){
						customOpt.setCSS(cssText.getText());
					}
				}
				public void keyTyped(KeyEvent arg0) {}
			});
			cssText.setEditable(false);
			cssText.setLineWrap(true);
			JScrollPane scrollCSS = new JScrollPane(cssText);
			this.add(scrollCSS);
			
			JPanel commandPanel = new JPanel();
			commandPanel.setLayout(new FlowLayout());
			
			copyToCustom = new JButton("Copy to Custom");
			copyToCustom.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					customOpt.setCSS("/* Copied into Custom */\r\n\r\n" + cssText.getText());
					customOpt.setSelected(true);
					cssText.setText(customOpt.getCSS());
					onCustomOpt(true);
				}
			});
			commandPanel.add(copyToCustom);
			
			JButton useThisCSS = new JButton("Use this CSS");
			useThisCSS.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					currentOpt.setVisible(true);
					currentOpt.setCSS(cssText.getText());
					currentOpt.setSelected(true);
					editItem.setCurrentCSS(cssText.getText());
					onCustomOpt(false);
				}
			});
			commandPanel.add(useThisCSS);
			
			this.add(commandPanel);
			this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		}
		else{
			if(hasbeenSeen){
				this.setVisible(true);
				if(current == null){
					currentOpt.setVisible(false);
					if(currentOpt.isSelected()){
						cssText.setText("");
					}
				}
				else{
					currentOpt.setVisible(true);
					currentOpt.setSelected(true);
					currentOpt.setCSS(current);
					onCustomOpt(false);
				}
			}
		}
	}
	
	protected class CSSOption extends JRadioButton{
		private String cssOfOption = "/* no CSS */";
		
		protected CSSOption(String label, String css){
			super(label, false);
			if(css != null){
				cssOfOption = css;
			}
			this.addActionListener(optionListener);
		}
		
		public String getCSS(){ return cssOfOption; }
		public void setCSS(String c){ cssOfOption = c; }
	}
}