//vpDocLoader loads and saves XML documents
//use reread() and rewrite() to store data in escaped characters

import java.io.File;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public final class vpDocLoader {
	
	static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	static DocumentBuilder db = null;
	
	public static Document getXMLfromString(String xml){
		InputSource inStream;
		Document doc = null;
		try{
			if(db == null){ db = factory.newDocumentBuilder(); }
			inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(xml));
			doc = db.parse(inStream);
			return doc;
		}
		catch(Exception e){
			return null;
		}
		finally{
			inStream = null;
		}
	}
	
	public static Document getXMLfromFile(File f){
		try{
			if(db == null){ db = factory.newDocumentBuilder(); }
			Document doc = db.parse(f);
			return doc;
		}
		catch(Exception e){
			return null;
		}
	}
	
	public static Document getXMLfromURL(String u){
		try{
			if(db == null){ db = factory.newDocumentBuilder(); }
			return db.parse(new URL(u).openStream());
		}
		catch(Exception e){
			return null;
		}
	}
	
	//rewrite returns the passed String with escaped characters
	public static String rewrite(String w){
		if(w == null){
			return "";
		}
		else{
			return w.replace("<", "(speciallt)").replace(">", "(specialgt)").replace("\"", "(;quote;)").replace("&", "{;and;}").replace("\\","(;bslash;)").replace("“","(;quote;)").replace("”", "(;quote;)").replace(",", ",");
		}
	}
	//reread returns the passed String after replacing the escaped characters
	public static String reread(String r){
		if(r == null){
			return "";
		}
		else{
			return r.replace("(speciallt)", "<").replace("(specialgt)", ">").replace("(;quote;)", "\"").replace("{;and;}", "&").replace("(;bslash;)", "\\");
		}
	}
}