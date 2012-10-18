/*
 *  Photo Album
 *
 *  Copyright (C) Matthew West 2002
 *
 *  $Id: XmlOutput.java,v 1.8 2002/08/28 23:43:33 mfw Exp $	
 *
 * @author <a href="mailto:album@matthewwest.co.uk">Matthew West</a>
 * @version RCS:$Revision: 1.8 $ $Date: 2002/08/28 23:43:33 $ Dist:0.1 12-July-2002 10:38
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package uk.co.matthewwest.Album;

import java.io.*;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * This generates a DOM document which can then be fed to the optional
 * XSL template to be transformed into an HTML page.  It originally generated
 * plain text.  
 */
public class XmlOutput {
    static private File xslin = null;
    static private javax.xml.transform.Transformer transformer = null;    

    static private DocumentBuilder db = null;
    private org.w3c.dom.Document doc = null;
    private org.w3c.dom.Element curnode = null;

    public XmlOutput() throws InternalException {
        if(db == null) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setIgnoringComments(true);
                dbf.setNamespaceAware(false);
                dbf.setValidating(false);
                db = dbf.newDocumentBuilder();
            } catch (FactoryConfigurationError fce) {
                throw new InternalException(fce.getMessage());
            } catch (ParserConfigurationException pce) {
                throw new InternalException(pce.getMessage());
            }
        }

        try {
            DOMImplementation di = db.getDOMImplementation();
            doc = di.createDocument("", "albumpage", null);
        } catch (DOMException de) {
            throw new InternalException(de.getMessage());
        }
            
        curnode = doc.getDocumentElement();
    }

    public void element(final String element, final ArrayList sl) {
        String at[][] = new String[sl.size()][2];
        for(int i = 0; i<sl.size(); i++) {
            at[i] = (String[]) sl.get(i);
        }
        element(element, at);
    }

    public void element(final String element, final String[][] attr) {
        int i;
        org.w3c.dom.Element parent = curnode;
        curnode = doc.createElement(element);
        parent.appendChild(curnode);
        if(attr != null)
            for(i = 0; i<attr.length; i++) {
                curnode.setAttribute(attr[i][0], attr[i][1]);
            }
    }

    /**
     * Close an element
     */
    public void closeElement() throws InternalException {
        curnode = (Element)curnode.getParentNode();
    }

    public void elementClosed(final String e, final String[][] attr) throws InternalException {
        element(e, attr);
        closeElement();
    }

    /**
     * Create a link element
     * @param type type of link
     * @param href destination
     * @param title name of destination
     * @param description text to go after link
     */
    public void link(final String type, 
		     final String href, 
		     final String title, 
		     final String description) throws InternalException {
        ArrayList sl = new ArrayList();
	String tmptitle = title;
	if(tmptitle == null)
	    tmptitle = type;
	if(tmptitle.equals("prev"))
	    tmptitle = "Previous";
	if(tmptitle.equals("next"))
	    tmptitle = "Next";
        sl.add(new String[] {"type", type});
        sl.add(new String[] {"href", href});
	if(description != null)
	    sl.add(new String[] {"description", description});
        element("link", sl);
	curnode.appendChild(doc.createTextNode(tmptitle));
	closeElement();
    }

    /**
     * Create a link element if part != null
     * @param type type of link
     * @param part part object to be used
     */
    public void link(final String type, final PartBase part) throws InternalException {
	if(part != null) 
	    link(type, part.getLink(), part.getTitle(), part.getDescription());
    }

    /**
     * Creates a link element that links to the
     * directory above it if part != null
     * @param type type of link
     * @param part part object to be used
     */
    public void uplink(final String type, final PartBase part) throws InternalException {
	if(part != null)
	    link(type, "../" + part.getLink(), part.getTitle(), part.getDescription());
    }

    /**
     * Creates an image tag
     * @param src Image filename
     * @param w Width
     * @param h Height
     */
    public void image(final String src, final int w, final int h) throws InternalException {
        elementClosed("image", new String[][] {new String[] {"src", src},
                                               new String[] {"height", String.valueOf(h)},
                                               new String[] {"width", String.valueOf(w)}});
    }

    /**
     * Creates an element with some text in it
     * If the text points at null an empty element is created
     * @param e Element name
     * @param t Text
     */
    public void textElement(final String e, final String t) throws InternalException {
	element(e, (String[][])null);
	if(t != null)
	    curnode.appendChild(doc.createTextNode(t));
	closeElement();
    }

    public void addNodeChildren(final Node t) throws InternalException {
        for(Node n = t.getFirstChild(); n != null; n = n.getNextSibling()) {
            curnode.appendChild(n.cloneNode(true));
        }
    }

    /**
     * Sets xsl sheet
     * @param s Filename of xsl file
     */
    static public void setXslFile(final String s) throws FatalException {
	xslin = new File(s);
	if(!xslin.exists())
	    throw new FatalFileException("Xsl file does not appear to exist", xslin);
	if(!xslin.isFile())
	    throw new FatalFileException("Xsl file is not a file", xslin);
	javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
	try {
            //	    tf.setAttribute("http://xml.apache.org/xalan/features/optimize", java.lang.Boolean.FALSE);
	    transformer = tf.newTransformer(new javax.xml.transform.stream.StreamSource(xslin));
	} catch(javax.xml.transform.TransformerConfigurationException e) {
	    throw new InternalException(e.getMessageAndLocation());
	}
    }

    /**
     * Creates file and transforms it if there is an xsl file specified
     * @param f File to be saved to
     */
    public void saveFile(final File f) throws IOException, InternalException {
        if(transformer == null) {
            try {
                javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
                transformer = tf.newTransformer();
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            } catch (javax.xml.transform.TransformerException e) {
                throw new InternalException(e.getMessageAndLocation());
            }
        }

        try {
            transformer.transform(new DOMSource(doc), new StreamResult(f));
        } catch (javax.xml.transform.TransformerException e) {
            throw new InternalException(e.getMessageAndLocation());
	}
	try {
	    Thread.sleep(5);
	} catch(InterruptedException ie) { }
    }
}


