/*
 *  Photo Album
 *
 *  Copyright (C) Matthew West 2002
 *
 *  $Id: SectionPart.java,v 1.9 2002/08/24 17:23:51 mfw Exp $	
 *
 * @author <a href="mailto:album@matthewwest.co.uk">Matthew West</a>
 * @version RCS:$Revision: 1.9 $ $Date: 2002/08/24 17:23:51 $ Dist:0.1 12-July-2002 10:38
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

import org.w3c.dom.Element;

import java.io.IOException;
import java.io.File;
import java.util.ListIterator;

/**
 * This defines a Section, which is a collection of pages and images in it's own
 * directory.  Sections can also contain other sections.  The section can also generate
 * an optional index page.
 */
public class SectionPart extends PartBase {
    static private int section_number;
    static private File basedir;

    public SectionPart(final Element n, final PartBase up) throws FatalException {
	setUp(up);
	section_number++;
	if(up != null)
	    depth = up.getDepth() + 1;
	node = n;
	readValues();
	if(name == null)
	    name = "section" + " "  + section_number;
	fixTitle();
	setFilename("");
	PagePart.resetPartNumber();
	feedback("Section");

        if(n.getAttribute("index").equals("yes"))
            setFirstChild(new IndexPart(this));
	createPartList();
    }

    public String getLink() {
	if(getUp() != null)
	    return filename + "/index.html";
	return "index.html";
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
        i.addToPrefix(filename + "/");
        return i;
    }

    public File makeNewDirectory(final File targetdir) throws FatalException {
        if(basedir == null)
            basedir = targetdir;
	if(getUp() == null) {
	    Misc.makeDir(targetdir);
	    thisdir = targetdir;
	} else {
	    thisdir = Misc.makeDir(targetdir, getFilename());
	}
        return thisdir;
    }

    public String makeNewDirectory(final String dir) throws FatalException {
        String n;
        if(getUp() != null) {
            n = dir + getFilename() + "/";
        } else 
            n = dir;
        Misc.makeDir(basedir, n);
        return n;
    }

    public void createFiles() throws FatalException, IOException {
	PartBase p;
        for(p = getFirstChild(); p != null; p = p.getNext()) {
	    p.createFiles();
	}
    }
}

