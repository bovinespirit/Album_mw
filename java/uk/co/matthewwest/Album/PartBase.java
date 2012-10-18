/*
 *  Photo Album
 *
 *  Copyright (C) Matthew West 2002-2005
 *
 *  $Id: PartBase.java,v 1.20 2002/08/24 17:40:31 mfw Exp $	
 *
 * @author <a href="mailto:album@matthewwest.co.uk">Matthew West</a>
 * @version RCS:$Revision: 1.20 $ $Date: 2002/08/24 17:40:31 $ Dist:0.1 12-July-2002 10:38
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
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.File;

/** 
 * <p>
 *  This forms the base class from which all the 'part' classes are derived.
 *  PartBase defines a factory function which creates whatever part is appropriate
 *  for the node it is processing.  Each part forms a link in a linked list.
 * </p><p>
 *  Most parts are HTML pages, apart from SectionParts, which are directories.
 *  Sections can contain pages, images and other sections.  A section can also generate
 *  an index.  Every part has a title, a description, a name(used in filenames) and a filename.
 *  These are used differently depending on the part.  Parts also have a menu, although this is empty
 *  in sections.
 * </p><p>
 *  All parts have a series of pointers, which are used to iterate through the structure.  Each part creates a
 *  linked list of children, and is a part of a linked list itself.  There is one section node at the top of
 *  the structure that has no parent, and no siblings.  Each stage of the creation is done by calling a function
 *  of the parent node, and relying on that to call the function in it's child nodes.  These functions can be
 *  overridden to perform appropriate tasks.
 * </p><p>
 *  The program creates a DOM object, and then iterates through it.  A SAX object would be more efficient
 *  but I already had experience of DOM objects.
 * </p>
 */
public abstract class PartBase {
    /** This is the name of the page, used to create the filename, and title if none is given */
    protected String name;
    /** This represents the filename, usually dirived from the name, but with white space removed */
    protected String filename;
    /** Tis is the longer title, used as the page heading, and the page title */
    protected String title;
    /** This is a short description, used in menus, meta-tags and so on */
    protected String description;
    /** This is a longer text description, shown with the images, indexs etc */
    protected String text;
    /** This is the node in the source document that describes this part */
    protected Element node;
    /** This points to the object that represents the next part in the source document */
    private PartBase next;
    /** This points to the object that represents the preivious part in the source document */
    private PartBase prev;
    /** This points to the 'parent' object */
    private PartBase up;
    /** This represents the number of steps to the root directory */
    protected int depth = 0;
    /** This points to the 'index' object */
    private PartBase index = null;
    /** Menu for this object */
    protected Menu menu = null;
    /** Is this the part that contains the index image? */
    private boolean index_image = false;
    /** Location of this part */
    protected File thisdir = null;
    /** Location of first child */
    private PartBase firstchild = null;

    /**
     * Factory function creates the appropriate object from a node
     * @param n Node to process
     * @param p Parent object
     * @return Newly created object
     */
    static public PartBase createPart(final Node n, final PartBase p) throws FatalException {
	if(n.getNodeType() != Node.ELEMENT_NODE)
	    return null;
        Element e = (Element) n;
	if(n.getNodeName() == "section")
	    return new SectionPart(e, p);
	if(n.getNodeName() == "page")
	    return new PagePart(e, p);
	if(n.getNodeName() == "image")
	    return new ImagePart(e, p);
	return null;
    }

    /**
     * Read the values of name, title and description attributes.
     * name is the short name used in the window title bar and the filename
     * title can be a longer name, is required
     * description is a quick description displayed in the menu etc
     * This function is supposed to be called by the subclasses constructor
     */
    protected void readValues() {
	name = node.getAttribute("name");
	title = Misc.getTextFromElement(node, "title");
	description = Misc.getTextFromElement(node, "description");
	text = Misc.getTextFromElement(node, "text");
    }

    /**
     * If title is null, set it to equal name
     */
    protected void fixTitle() {
	if (title == null)
	    title = name;
    }

    /**
     * Takes all the whitespace out the name to create a filename
     * @param suffix is the suffix to be appended to the filename
     */
    protected void setFilename(final String suffix) {
	StringBuffer sb = new StringBuffer();
	for(int i = 0; i < name.length(); i++) {
	    char c = name.charAt(i);
	    if((c != ' ') && (c != '?') && (c != '&'))
		sb.append(c);
	}
	filename = sb.toString() + suffix;
    }

    /**
     * Creates the partlist from the nodes children
     * Also sets their next, prev and index pointers
     */
    public void createPartList() throws FatalException {
	PartBase pbprev = firstchild;
	PartBase pbcur = null;
        boolean found_index_image = false;

	for (Node i = node.getFirstChild(); i != null; i = i.getNextSibling()) {
	    pbcur = PartBase.createPart(i, this);
	    if(pbcur != null) {
                Element e = (Element) i;
                String iis = e.getAttribute("index-image");
                if(iis.equals("yes")) {
                    pbcur.setIndexImage(true);
                    found_index_image = true;
                }
                if(firstchild == null)
                    firstchild = pbcur;
                pbcur.setIndex(firstchild);
                pbcur.setPrev(pbprev);
                if(pbprev != null)
                    pbprev.setNext(pbcur);
		pbprev = pbcur;
	    }
	}

        if(!found_index_image && (firstchild != null)) {
            firstchild.setIndexImage(true);
        }
    }

    /**
     * Gives user feedback as the album is created
     * @param s Type of object being created
     */
    public void feedback(final String s) {
//	for(int i = 0; i < depth; i++)
//	    System.out.print(" ");
//	System.out.println(s+":"+name);
    }

    /**
     * Returns the prefix needed to get to root directory <br />
     * i.e. ../../../../ <br />
     * It is assumed that the host OS is UNIX.
     * @return String with path
     */
    public String getPrefix() {
	StringBuffer sb = new StringBuffer();
	for(int i = 0; i < depth; i++)
	    sb.append("../");
	return sb.toString();
    }

    /** @return Name of the file that is being created */
    public String getFilename() {
	return filename;
    }

    /** @return Document title */
    public String getTitle() {
	return title;
    }

    /** @return Document description */
    public String getDescription() {
	return description;
    }

    /** @return Document text */
    public String getText() {
	return text;
    }

    /** @return Document name */
    public String getName() {
	return name;
    }

    /** @param n Next part */
    public void setNext(final PartBase n) {
	next = n;
    }

    /** @param n Previous part */
    public void setPrev(final PartBase n) {
	prev = n;
    }

    /** @return Next part */
    public PartBase getNext() {
	return next;
    }

    /** @return Previous part */
    public PartBase getPrev() {
	return prev;
    }

    /** @return Depth of directory */
    public int getDepth() {
	return depth;
    }

    /** @return Pointer to the 'parent' object */
    public PartBase getUp() {
	return up;
    }

    /** @param up Parent part */
    public void setUp(final PartBase up) {
        this.up = up;
    }

    /** @return Pointer to index part for this object */
    public PartBase getIndex() {
	return index;
    }

    /** @param i Object that is the index part for this object */
    public void setIndex(final PartBase i) {
	index = i;
    }

    /** @return true if this object is an index page */
    public boolean isIndex() {
	return (this == index);
    }

    /** @param p pointer to firstchild */
    public void setFirstChild(final PartBase p) {
        firstchild = p;
    }

    /** @return First child */
    public PartBase getFirstChild() {
        return firstchild;
    }

    /**
     * Determines whether this part ios providing an image for
     * the index
     * @param b true if this part is providing an image
     */
    private void setIndexImage(final boolean b) {
        index_image = b;
    }

    /** @return true if this part has the index image */
    public boolean hasIndexImage() {
        return index_image;
    }

    /**
     * This method is overridden to either add infomation in the case
     * of sections and pages, or create the object in the case of an image
     * @return the IndexImage for this part
     */
    public IndexImage getIndexImage() {
        return getChildrenIndexImage();
    }

    /**
     * Sorts through the list of child parts and finds the index image.
     * This can be called by the overridden getIndexImage method.
     * @return the IndexImage created by the index child part
     */
    protected IndexImage getChildrenIndexImage() {
	PartBase i;
	for(i = firstchild; i != null; i = i.getNext()) {
            if(i.hasIndexImage())
                return i.getIndexImage();
        }
        return null;
    }

    /**
     * Creates the menu for this object
     * and all it's children
     */
    public void makeMenu () {
	PartBase i;
	for(i = firstchild; i != null; i = i.getNext()) {
	    i.makeMenu();
	}
	menu = new Menu(this);
    }

    /**
     * Create the links
     */
    public void makeLinks(XmlOutput xo) throws InternalException {
	xo.link("prev", getPrev());
	xo.link("next", getNext());
	if(this instanceof ImagePart)
	    xo.link("index", getUp());
	else {
	    if((getIndex() != getPrev()) && (getIndex() != this))
		xo.link("index", getIndex());
	    if(getUp() != null) {
		xo.uplink("nextpart", getUp().getNext());
		xo.uplink("prevpart", getUp().getPrev());
		xo.uplink("uppart", getUp().getIndex());
	    }
	}
    }

    /**
     * Recursive function to make directories
     * @param targetdir Base directory
     * @param imagedir Base image directory
     */
    public void createDirectories(final File targetdir, final String imagedir) throws FatalException {
        thisdir = targetdir;
	PartBase i;
	for(i = firstchild; i != null; i = i.getNext()) {
	    i.createDirectories(makeNewDirectory(targetdir), makeNewDirectory(imagedir));
	}
    }

    /**
     * Overridden by SectionPart to create directory
     */
    public File makeNewDirectory(final File targetdir) throws FatalException {
        return targetdir;
    }

    /**
     * Overridden by SectionPart to create directory
     */
    public String makeNewDirectory(final String dir) throws FatalException {
        return dir;
    }

    /**
     * This is overridden by ImagePart. The files need to be
     * created first so that the sizes are set for the index pages
     * to use.
     */
    public void createImageFiles() throws FatalException, IOException {
	PartBase i;
	for(i = firstchild; i != null; i = i.getNext()) {
	    i.createImageFiles();
	}
    }

    /**
     * This is overridden by the subclasses to generate the object's files
     */
    abstract public void createFiles() throws FatalException, IOException;

    /**
     * This is overridden by the subcless to generate the correct file to link to
     * @return text suitable for a link, i.e. "../file.html"
     */
    public String getLink() { return null; }

}







