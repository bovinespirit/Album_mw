/*
 *  Photo Album
 *
 *  Copyright (C) Matthew West 2002
 *
 *  $Id: IndexImage.java,v 1.3 2002/08/24 17:10:56 mfw Exp $	
 *
 * @author <a href="mailto:album@matthewwest.co.uk">Matthew West</a>
 * @version RCS:$Revision: 1.3 $ $Date: 2002/08/24 17:10:56 $ Dist:0.1 12-July-2002 10:38
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

/**
 * This creates a class representing the image to be used in an index page
 */
public class IndexImage {
    private String title = null;
    private String description = null;
    private String filename = null;
    private String href = null;
    private String prefix = "";
    private ImagePart ip = null;

    /**
     * Default constructer does nothing
     */
    public IndexImage() {
    }

    /**
     * Creates new IndexImage from an image part
     * @param ip ImagePart to get info from
     */
    public IndexImage(ImagePart ip) {
        title = ip.getTitle();
	if(title == null)
	    title = ip.getName();
        description = ip.getDescription();
        this.ip = ip;
    }

    /** @param s new title */
    public void setTitle(String s) {
        title = s;
    }

    /** @param s new description */
    public void setDescription(String s) {
        description = s;
    }

    /** @param s prefix to be added to filename */
    public void addToPrefix(String s) {
        prefix = s + prefix;
    }

    /** @param href Where the link will point to */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * Creates XML element for this index image
     * @param xo target xml file object
     */
    public void createElement(XmlOutput xo) throws InternalException {
        String hs = "";
        if(href != null)
            xo.element("indeximage", new String[][] {new String[] {"href", href}});
        else
            xo.element("indeximage", (String[][])null);
	if(title != null)
	    xo.textElement("title", title);
        String des = "";
        if(description != null)
            des = description;
        xo.textElement("description", des);
        if(ip != null)
            ip.makeSmallImageElement(xo, prefix);
        xo.closeElement();
    }
}
