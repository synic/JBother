package com.valhalla.jbother.jabber.smack.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import com.valhalla.jbother.jabber.smack.*;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.provider.*;

import org.xmlpull.v1.XmlPullParser;
import com.valhalla.jbother.jabber.smack.*;

import java.util.HashMap;

/**
 * Created by luke on Apr 1, 2005 1:11:23 PM
 */

/**
 * provider class for parsing Search packages
 */
public class SearchProvider implements IQProvider
{
  private static String fId = "$Id$";

  public static final String NAMESPACE = "jabber:iq:search";

  public IQ parseIQ(XmlPullParser parser) throws Exception
  {
    Search search = new Search();

    boolean done = false;
    HashMap searchFields = new HashMap();

    while(!done)
    {
      int eventType = parser.next();
      String elementName = parser.getName();
      String namespace = parser.getNamespace();

      if (eventType == XmlPullParser.START_TAG)
      {
        if(elementName.equals("instructions")) {
          search.setInstructions( parser.nextText() );
        }
        else if(elementName.equals("item")) {
          search.addItem(parseItem(parser));
        }
        else if(elementName.equals("x") && namespace.equals("jabber:x:data")) {
          DataFormProvider dataFormProvider = new DataFormProvider();
          search.setSearchDataForm((DataForm)dataFormProvider.parseExtension(parser));
        }
        else
        {
          if( ! searchFields.containsKey(elementName)) {
            searchFields.put(elementName,parser.nextText());
          }
        }
      }
      else if (eventType == XmlPullParser.END_TAG && elementName.equals("query")) {
          done = true;
      }
    }
    if(searchFields.size() > 0) {
      search.setSearchFields(searchFields);
    }
    return search;
  }

  /**
   * tries to parse <code><item></code> messages and return Saech.Item objects
   * @param parser XmlPullParser providing the XML data
   * @return @link{String.Item} object representing search result
   */
  private Search.Item parseItem(XmlPullParser parser) throws Exception
  {
    boolean done = false;
    HashMap attributes = new HashMap();
    Search.Item item = new Search.Item(parser.getAttributeValue("","jid"));
    while (!done) {
        int eventType = parser.next();
        if (eventType == XmlPullParser.START_TAG) {
          attributes.put(parser.getName(),parser.nextText());
        }
        else if (eventType == XmlPullParser.END_TAG) {
            if (parser.getName().equals("item")) {
                done = true;
            }
        }
    }
    item.setAttributes(attributes);
    return item;
  }
}
