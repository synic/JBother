package com.valhalla.jbother.jabber.smack.provider;

import java.util.ArrayList;
import java.util.Vector;

import com.valhalla.jbother.jabber.smack.*;
import org.jivesoftware.smackx.packet.PrivateData;
import org.jivesoftware.smackx.provider.PrivateDataProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * The BookmarkProvider parses Bookmark Storage packets (JEP-48).
 *
 * @author George Weinberg
 * a bookmark can either be a jabber conference or an url
 */
public class BookmarkProvider implements PrivateDataProvider {
	Vector vUrls;
	Vector vConferences;
    /**
     * Creates a new BookmarkProvider.
     * PrivateDataManager requires that every PrivateDataProvider has a public, no-argument constructor
     */

	public BookmarkProvider(){
	}
	/**
	*Called by the ProvidermManager when when the server returns a packet containimg PrivateData
	*/
    public PrivateData parsePrivateData(XmlPullParser parser) throws Exception {
        boolean done = false;
        boolean done2;
        StringBuffer buffer = null;
        String name;
        String sUrl;
        String jid;
        boolean autojoin;
        String nick;
        String password;
        Bookmark bookmark = new Bookmark();
        Bookmark.Conference conference;
        while (!done) {
			name = null;
			sUrl = null;
			autojoin = false;
			nick = null;
			password = null;
			jid = null;

            int eventType = parser.next();
            if (eventType == XmlPullParser.TEXT){
				if (parser.getText().trim().equals(""))continue;
			}
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("url")) {
					for (int j1 = 0; j1 < parser.getAttributeCount(); j1++){
						if (parser.getAttributeName(j1).equals("name")) name = parser.getAttributeValue(j1);
						if (parser.getAttributeName(j1).equals("url")) sUrl = parser.getAttributeValue(j1);
						if ((name != null) && (sUrl != null)) bookmark.addURL(name,sUrl);
					}
				} else if (parser.getName().equals("conference")){
					for (int j1 = 0; j1 < parser.getAttributeCount(); j1++){
						if (parser.getAttributeName(j1).equals("name")) {
							name = parser.getAttributeValue(j1);
						}
						if (parser.getAttributeName(j1).equals("jid")) jid = parser.getAttributeValue(j1);
						if (parser.getAttributeName(j1).equals("autojoin")){
							if (parser.getAttributeValue(j1).equals("1")) autojoin = true;
						}
					}
					done2 = false;
					while (!done2){
						eventType = parser.next();
						if (eventType == XmlPullParser.START_TAG){
							if (parser.getName().equals("nick")){
								eventType = parser.next();
								nick = parser.getText();
							} else if (parser.getName().equals("password")){
								if( parser.next() != XmlPullParser.END_TAG )
                                {
                                    password = parser.getText();
                                }
							}
						}

						if (eventType == XmlPullParser.END_TAG){
							if (parser.getName().equals("conference")) done2 = true;
						}
					}
					if (name != null && jid != null){
						bookmark.addConference(name,jid,nick,password,autojoin);
					}


				}
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("storage")) {
                    done = true;
				}
            }

		}
		return bookmark;
	}
}
