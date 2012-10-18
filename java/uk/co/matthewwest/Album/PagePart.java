/*
 *  Photo Album
 *
 *  Copyright (C) Matthew West 2002
 *
 *  $Id: PagePart.java,v 1.13 2002/08/24 17:16:54 mfw Exp $	
 *
 * @author <a href="mailto:album@matthewwest.co.uk">Matthew West</a>
 * @version RCS:$Revision: 1.13 $ $Date: 2002/08/24 17:16:54 $ Dist:0.1 12-July-2002 10:38
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

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.DocumentFragment;

import java.io.File;
import java.io.IOException;

/**
 * This creates a page, which contains a number of thumbnail images
 */
public class PagePart extends PartBase {
    static private int part_number;
        
    public PagePart(final Element n, final PartBase up) throws FatalException {
	setUp(up);
	depth = up.getDepth();
	node = n;
	part_number++;
	readValues();
	fixTitle();
	if((name == null) || (name == ""))
            name = "part " + part_number;
        setFilename(".html");
	feedback("Page");
	createPartList();
    }

    static public void resetPartNumber() {
	part_number = 0;
    }

    public void setIndex(final PartBase i) {
        if(i == this)
            filename = "index.html";
        super.setIndex(i);
    }
    
    public String getLink() {
	return filename;
    }

    /**
     * Override.  This looks for a index image, if it can't find one it creates one.
     * The title and description of the IndexImage are set to the title and description
     * of this part.
     */
    public IndexImage getIndexImage() {
        IndexImage i = getChildrenIndexImage();
        if(i == null) 
            i = new IndexImage();
        i.setTitle(getTitle());
        i.setDescription(getDescription());
        i.setHref(getLink());
        return i;
    }

    public void createFiles() throws FatalException, IOException {
	File file = Misc.makeFile(thisdir, getFilename());
	XmlOutput xo = null;
  	PartBase p;
	
	xo = new XmlOutput();
	xo.textElement("prefix", getPrefix());
//	System.out.println("Creating menu : " + getName());
	menu.createXml(xo);
	xo.textElement("title", title + " : " + getUp().getTitle());
	xo.textElement("heading", title);
	
	if(description != null)
	    xo.textElement("description", description);
	if(text != null)
	    xo.textElement("text", text);
	makeLinks(xo);
        for(p = getFirstChild(); p != null; p = p.getNext()) {
	    p.createFiles();
	    if(p instanceof ImagePart)
		((ImagePart)p).writeImageTag(xo);
	}

	xo.saveFile(file);
    }
}
