import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

//You are encouraged to change the program to fit your needs, and then credit yourself for these changes.
//Guides are available at http://advocatesforscience.wiki.zoho.com/VP-Mods.html
/********************************************************************************************/
//Creative Commons Attribution-Share Alike license 3.0: http://creativecommons.org/licenses/by-sa/3.0/
//What you should know about the license:
//Student Advocates for Science must be credited in the interface and code of every version of this program.
//Also mention http://advocatesforscience.wiki.zoho.com/ , unless the official site has been moved.
//This license must be applied to future versions.  Changing this file must not change the terms of the license.
/********************************************************************************************/
//If you make translations, please ensure this message encourages participation and giving due credit
//also include a link to Creative Commons in a language users will understand

public class vpAboutTab extends JPanel {

	private static Font aboutFont = new Font("Arial", Font.PLAIN, 14);
	
	public vpAboutTab(){
		JTextArea aboutText = new JTextArea(
		"**Information for Users**\n" +
		"For Help, Tutorials, and Examples, go to:\n\n" +
		"http://advocatesforscience.wiki.zoho.com/VP-Help.html\n\n" +
		"This program is a resource from \"Student Advocates for Science\".\n" +
		"This program is NOT affiliated with YouTube.\n\n" +
		"**Information for Programmers**\n" +
		"Accompaniment is an open-source project written in the Java\ncomputer language." +
		"Resources for programming new AddOns or \nmodifying the current ones are available online at \nhttp://advocatesforscience.wiki.zoho.com/VP-Mods.html\n" +
		"\nIf you make changes to the program, update vpAboutTab.java");
		aboutText.setFont(aboutFont);
		aboutText.setEditable(false);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(aboutText);
	}
}