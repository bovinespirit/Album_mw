/*
 *  Photo Album
 *
 *  Copyright (C) Matthew West 2002
 *
 *  $Id: Menu.java,v 1.6 2002/08/24 17:14:29 mfw Exp $	
 *
 * @author <a href="mailto:album@matthewwest.co.uk">Matthew West</a>
 * @version RCS:$Revision: 1.6 $ $Date: 2002/08/24 17:14:29 $ Dist:0.1 12-July-2002 10:38
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

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * This creates the XML that describes the menu
 * One is created for each part by PartBase
 */
public class Menu {
    ArrayList menulist = new ArrayList();
    int depth;
    
    /**
     * Create a menu using the last, next and up pointers in the object
     * @param pb Object for which the menu is to be created
     */
    Menu(final PartBase pb) {
	if(pb instanceof SectionPart) // Sections don't produce files, don't need menus
	    return;
	ArrayList parents = new ArrayList();
	PartBase i;
	PartBase j;
	PartBase k;
	ListIterator l;
	PartBase par;
	PartBase ind;
	depth = pb.getDepth();
	int d;

//  	System.out.print(pb.getName()+" : ");
	par = pb.getUp();
	if(par != null) {
//  	    System.out.println("up:" + par.getName());
	    for(i = par.getUp(); i != null; i = i.getUp()) {
//  		System.out.println("   - Add parent:" + i.getName());
		parents.add(i);
	    }
	}

//  	System.out.println("  Creating ancester items");
	l = parents.listIterator(parents.size());
	if(l.hasPrevious())
	    l.previous();
	d = parents.size();
	if(pb instanceof ImagePart)
	    d--;
	while(l.hasPrevious()) {
	    i = (PartBase)l.previous();
	    d--;
	    addToMenu(new Item(i, d, false, !((i == par.getUp()) && (i instanceof SectionPart)) ));
	}

//  	System.out.println(" d = " + d);
	if(par.getIndex() != null) {
//  	    System.out.println("  Creating parent and sibling items - 1st:" + par.getIndex().getName());
	    for(i = par.getIndex(); i != null; i = i.getNext()) {
		addToMenu(new Item(i, d, false, !((i == par) && (i instanceof SectionPart)) ));
		if(i == par)
		    for(j = pb.getIndex(); j != null; j = j.getNext())
			addToMenu(new Item(j, 0, true, (j != pb)));
	    }
	} else {
//  	    System.out.println("  Creating sibling items - 1st:" + pb.getIndex().getName());
	    for(j = pb.getIndex(); j != null; j = j.getNext())
		addToMenu(new Item(j, 0, false, (j != pb)));
	}
    }

    private void addToMenu(Item i) {
//  	System.out.println("   - AddToMenu:" + i.title);
	menulist.add(i);
    }

    public void createXml(XmlOutput xo) throws InternalException {
	ListIterator l;
	Item i;
	String type;
	String href;

	xo.element("menu", (String[][])null);

	l = menulist.listIterator(0);
	while(l.hasNext()) {
	    i = (Item) l.next();
	    if(i.ispage)
		type = "page";
	    else
		type = "section";
            ArrayList sl = new ArrayList();
            sl.add(new String[] {"type", type});
            sl.add(new String[] {"title", i.title});
            sl.add(new String[] {"description", i.description});
	    if(i.href != "")
		sl.add(new String[] {"href",i.href});
            xo.element("item", sl);
            xo.closeElement();
	}
	xo.closeElement();
    }

    private class Item {
	public String title;
	public String description;
	public String href;
	public boolean ispage;
	/**
	 * Create an item
	 * @param pb Object that this will represent
	 * @param distance How many directories up
	 * @param ispage Section = false, page = true
	 * @param withLink whether the item is to be a link
	 */
	Item(final PartBase pb, final int distance, final boolean ispage, final boolean withLink) {
	    int d = distance;
	    title = pb.getTitle();
	    if(title == null)
		title = pb.getName();
	    if(pb instanceof IndexPart)
		title="Index";
	    description = pb.getDescription();
	    this.ispage = ispage;

	    if(withLink) {
		StringBuffer sb = new StringBuffer();
		if(pb.getUp() == null)
		    d--;
		for(int i = 0; i < d; i++) {
		    sb.append("../");
		}
		sb.append(pb.getLink());
		href = sb.toString();
	    } else 
		href = "";
	}
    }
}
