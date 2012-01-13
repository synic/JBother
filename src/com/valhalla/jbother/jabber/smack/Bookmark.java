package com.valhalla.jbother.jabber.smack;

import java.util.Vector;
import java.util.Iterator;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.packet.*;
/**
* Represents jabber bookmarks (JEP-0048)
* @author George Weinberg
*
*
* To create bookmarks on the server, create the bookmark object,
* use the addURL and addConference methods to add all the bookmarks,
* and call the PrivateDataManager.setData method on the bookmark.
*
* to retrieve bookmarks from the server, register the BookmarkProvider
* class with a PrivateDataManager, then callthe getPrivateData method.
*
** There are two types of bookmarks, urls and conferences.
* This class will not allow you to add two urls or two conferences with
* the same name, but an url may have the same name as a conference,
* and bookmarks created with other apps might have duplicate names, the
* spec doesn't seem to address the point of whether such duplicates should be allowed.
*/

public class Bookmark implements PrivateData {
	private Vector vUrls;
	private Vector vConferences;
/**
*Constructor
*/
	public Bookmark(){
		vUrls = new Vector();
		vConferences = new Vector();
	}
/**
* adds a URL bookmark
* @param name a name which will represent the url bookmark
* @param value a string representing the url
* @return whether or not the url was successfully added to the Bookmark object
*/
	public boolean addURL(String name,String value){
		URL url;
		Iterator it = vUrls.iterator();
		while (it.hasNext()){
			url = (URL)it.next();
			if (url.name.equals(name)) return false;
		}
		url = new URL(name,value);
		vUrls.add(url);
		return true;
	}

    public void removeConference(Conference c)
    {
        vConferences.remove(c);
        com.valhalla.Logger.debug("removing conference");
    }
/**
* adds a Conference (jabber conference room) bookmark.
* nick and pasword may be null
* @param name a name which will represent the conference bookmark
* @param jid the jid of the conference
* @param nick a nickname representing the participant which will be displyed to the other participants in the conference
* @param password password to join the conference
* @param autojoin indicates whether the client should attempt to join the conference immediately upon connection
* @return whether or not the bookmark was successfully added to the Bookmark object
*/
	public boolean addConference(String name, String jid, String nick, String password, boolean autojoin){
		Conference conference;
		Iterator it = vConferences.iterator();
		while (it.hasNext()){
			conference = (Conference)it.next();
			if (conference.name.equals(name)) return false;
		}
		conference = new Conference(name,jid);
		conference.setNick(nick);
		conference.setPassword(password);
		conference.setAutojoin(autojoin);
		vConferences.add(conference);
		return true;
	}
/**
* Returns an iterator over the URL bookmarks
*/
	public Iterator getURLs(){
		return vUrls.iterator();
	}
/**
* @return an iterator over the Conference bookmarks
*/
	public Iterator getConferences(){
		return vConferences.iterator();
	}
/**
* @param name the name of the url
* @return the first url with the given name, or null if no url with that name exists
*/
	public URL getURL(String name){
		URL url;
		Iterator it = vUrls.iterator();
		while (it.hasNext()){
			url = (URL)it.next();
			if (url.name.equals(name)) return url;
		}
		return null;
	}
/**
* @param name the name of the url
* @return the first url with the given name, or null if no conferenence with that name exists
*/
	public Conference getConference(String name){
		Conference conference;
		Iterator it = vConferences.iterator();
		while (it.hasNext()){
			conference = (Conference)it.next();
			if (conference.name.equals(name)) return conference;
		}
		return null;
	}

/**
* Returns the name of the XML element representing the bookmark (query)
*/
	public String getElementName(){
		return "query";
	}
/**
* Returns the string "jabber:iq:private"
*/
	public String getNamespace(){
		return "jabber:iq:private";
	}
/**
* Returns an XML representaion of the bookmark, according to JEP-48
*/
	public String toXML(){
		String[] s;
		StringBuffer buf = new StringBuffer();
		URL url;
		Conference conference;
		buf.append("<storage xmlns=\"storage:bookmarks\"");
		if ((vUrls.size() == 0) && vConferences.size() == 0){
			buf.append("/>\n");
		} else {
			buf.append(">\n");
			Iterator it = vUrls.iterator();
			while (it.hasNext()){
				url = (URL)it.next();
				buf.append("<url name=\"");
				buf.append(url.name);
				buf.append("\" url=\"");
				buf.append(url.url);
				buf.append("\"/>\n");
			}
			it = vConferences.iterator();
			while (it.hasNext()){
				conference = (Conference)it.next();
				buf.append("<conference name=\"");
				buf.append(conference.name);
				buf.append("\" autojoin=\"");
				if (conference.autojoin){
					buf.append("1");
				} else {
					buf.append("0");
				}
				buf.append("\" jid=\"");
				buf.append(conference.jid);
				buf.append("\">\n");
				if (conference.nick != null){
					buf.append("<nick>");
					buf.append(conference.nick);
					buf.append("</nick>\n");
				}
				if (conference.password != null){
					buf.append("<password>");
					buf.append(conference.password);
					buf.append("</password>\n");
				}
				buf.append("</conference>\n");

			}

			buf.append("</storage>\n");
		}
		return buf.toString();

	}
/**
* Represents an URL bookmark. The bookmark consists of the name of bookmark and a string representing the url.
*/
	public class URL {
		String name;
		String url;
		public URL (String name, String url){
			this.name = name;
			this.url = url;
		}
		public String getName(){
			return name;
		}
		public String getUrl(){
			return url;
		}
	}

	public class Conference {
		private String name;
		private String jid;
		private String nick;
		private String password;
		private boolean autojoin;
		public Conference(String name, String jid){
			this.name = name;
			this.jid = jid;
			autojoin = false;
		}
		public void setNick(String nick){
			this.nick = nick;
		}
		public void setPassword(String password){
			this.password = password;
		}
		public void setAutojoin(boolean autojoin){
			this.autojoin = autojoin;
		}
		public String getName(){
			return this.name;
		}
		public String getJid(){
			return this.jid;
		}
		public String getNick(){
			return this.nick;
		}
		public String getPassword(){
			return this.password;
		}
		public boolean getAutojoin(){
			return this.autojoin;
		}
	}
}
