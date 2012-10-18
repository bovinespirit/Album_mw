/*
 *  Photo Album
 *
 *  Copyright (C) Matthew West 2002
 *
 *  $Id: IndexPart.java,v 1.4 2002/08/24 17:12:18 mfw Exp $	
 *
 * @author <a href="mailto:album@matthewwest.co.uk">Matthew West</a>
 * @version RCS:$Revision: 1.4 $ $Date: 2002/08/24 17:12:18 $ Dist:0.1 12-July-2002 10:38
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

import java.io.File;
import java.io.IOException;

/**
 * This creates an index page, using the IndexImage's created by each
 * child node of the section node that is this index's parent
 */
public class IndexPart extends PartBase {
    public IndexPart(final PartBase up) throws FatalException {
        setUp(up);
        depth = up.getDepth();
        name = "index";
        setIndex(this);
        title = up.getTitle();
	if(title == null)
	    title = up.getName();
        description = up.getDescription();
	text = up.getText();
        fixTitle();
        setFilename(".html");
    }
    
    public String getLink() {
        return filename;
    }

    public void createFiles() throws FatalException, IOException {
	File file = Misc.makeFile(thisdir, getFilename());
	XmlOutput xo = null;
        PartBase p = null;
	
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
        xo.element("index", (String[][])null);
        for(p = getNext(); p != null; p = p.getNext()) {
            IndexImage ii = p.getIndexImage();
            if(ii != null)
                ii.createElement(xo);
        }
        xo.closeElement();

        xo.saveFile(file);
    }
}
