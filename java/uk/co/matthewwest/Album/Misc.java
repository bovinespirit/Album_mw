/*
 *  Photo Album
 *
 *  Copyright (C) Matthew West 2002
 *
 *  $Id: Misc.java,v 1.8 2002/08/24 17:15:50 mfw Exp $	
 *
 * @author <a href="mailto:album@matthewwest.co.uk">Matthew West</a>
 * @version RCS:$Revision: 1.8 $ $Date: 2002/08/24 17:15:50 $ Dist:0.1 12-July-2002 10:38
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
import org.w3c.dom.NamedNodeMap;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Various handy functions, all as static functions, including 
 * some very paranoid file checking
 */
public class Misc {

    /**
     * Searches Node's children for an element and returns it's node
     * @param n - node to be searched
     * @param s - name of element being searched for
     * @return either node, or null
     */
    static public Node getElementFromNode(final Node n, final String s) {
	for (Node i = n.getFirstChild(); i != null; i = i.getNextSibling()) {
	    if((i.getNodeType() == Node.ELEMENT_NODE) && (i.getNodeName() == s))
		return i;
	}
	return null;
    }

    /**
     * Searches Nodes children for an element, returns the text
     * found within.
     * @param n Node to be searched
     * @param s Name of element
     * @return String, or null if element not found
     */
    static public String getTextFromElement(final Node n, final String s) {
	Node c = getElementFromNode(n, s);
	if(c == null)
	    return null;
	Node f = c.getFirstChild();
	if(f == null)
	    return null;
	String t = f.getNodeValue();
	return t;
    }

    /**
     * Return contents of attribute
     * @param n Element node
     * @param s name of attribute
     * @return contents of attribute, or null
     */
    static public String getAttributeFromNode(final Node n, final String s) {
        Element e = (Element) n;
        return e.getAttribute(s);
    }

    /**
     * Checks that a directory exists and that it is a directory
     * @param dir Directory to be created
     */
    static public void checkDir(final File dir) throws FatalFileException {
//      System.out.println(" Checking directory: " + dir.toString());
	if(!dir.exists()) 
	    throw new FatalFileException("Directory does not exist", dir);
	if(!dir.isDirectory())
	    throw new FatalFileException("The directory appears to be a file", dir);
    }

    /**
     * Checks that a directory exists and that it is a directory
     * @param dir Directory to be created
     * @param s Sub directories
     */
    static public File checkDir(final File dir, final String s) throws FatalFileException {
        File f = new File(dir, s);
        checkDir(f);
        return f;
    }

    /**
     * Checks that a directory exists and that it is a directory
     * @param s Name of directory to be created
     * 'return New File object
     */
    static public File checkDir(final String s) throws FatalFileException {
	File f = new File(s);
	checkDir(f);
	return f;
    }

    /**
     * Checks that a file exists and is a file
     * @param f file to be checked
     */
    static public void checkFile(final File f) throws FatalFileException {
	if(!f.exists()) 
	    throw new FatalFileException("File does not exist", f);
	if(!f.isFile())
	    throw new FatalFileException("The specified is not a file", f);
    }
    
    /**
     * Checks that a file exists and is a file
     * @param s filename to be checked
     * @return File object
     */
    static public File checkFile(final String s) throws FatalFileException {
	File f = new File(s);
	checkFile(f);
	return f;
    }
    

    /**
     * Creates a directory if it doesn't exist, throws errors if it
     * cannot create it, or if the directory is a file
     * @param dir Directory to be created
     */
    static public void makeDir(final File dir) throws FatalFileException {
//          System.out.println(" Creating directory: " + dir.toString());
	if(!dir.exists()) {
	    if(!dir.mkdir())
		throw new FatalFileException("Cannot create directory", dir);
	}
	checkDir(dir);
    }
    
    /**
     * Creates a directory if it doesn't exist, throws errors if it
     * cannot create it, or if the directory is a file
     * @param s Name of directory to be created
     * @return New file object
     */
    static public File makeDir(final String s)  throws FatalFileException {
	File f = new File(s);
	makeDir(f);
	return f;
    }

    /**
     * Creates a directory if it doesn't exist, throws errors if it
     * cannot create it, or if the directory is a file
     * @param dir Parent directory
     * @param s Name of directory to be created
     * @return New file object
     */
    static public File makeDir(final File dir, final String s) throws FatalFileException {
	File f = new File(dir, s);
	makeDir(f);
	return f;
    }

    /**
     * Creates a new file, and checks that parent directory is OK
     * @param dir Parent directory
     * @param s filename
     * @return File object
     */
    static public File makeFile(final File dir, final String s) throws FatalFileException {
	checkDir(dir);
	File f = new File(dir, s);
	if(f == null)
	    throw new FatalFileException("Could not create File object", s);
	if(!f.exists()) {
	    try { 
		f.createNewFile();
	    } catch(IOException ioe) {
		throw new FatalFileException("Could not create file. " + ioe.getMessage(), f);
	    }
	}
	if(!f.isFile())
	    throw new FatalFileException("File appears to be a directory", f);
	return f;
    }    

    /**
     * Removes extraneous ../'s, i.e root/dir/../x becomes root/x <br />
     * Also gets turns a/b//c info a/b/c
     * @param s path
     * @return rational path
     */
    static public String stripPath(final String s) {
        LinkedList sl = new LinkedList();
        int pos = 0;
        StringBuffer sb = new StringBuffer();
        int i;
        ListIterator l;
        ListIterator ol;
        boolean out = false;
        
        for(i = 0; i < s.length(); i++) {
            if(s.charAt(i) == '/') {
                if(pos != i)
                    sl.add(s.substring(pos, i));
                pos = i + 1;
            }
        }
        if(i > pos)
            sl.add(s.substring(pos, i));
        for(int j = 0; j < sl.size(); j++) {
            String c = (String) sl.get(j);
            if(c.equals("..")) {
                if(out) {
                    sl.remove(j - 1);
                    sl.remove(j - 1);
                }
            } else
                out = true;
        }
        l = sl.listIterator();
        while(l.hasNext()) {
            sb.append((String)l.next());
            if(l.hasNext()) 
                sb.append("/");
        }
        if(i == pos)
            sb.append("/");
        return sb.toString();
    }

    /**
     * Tests stripPath() function
     */
    static void main(String[] args) {
        String[] t = {"a/b/c", "a/b/c/", "../a/b", "a/../b", "a/../b/", "../a/b", 
                      "root/dir/../sub/file.html", 
                      "a//b/c/",
                      "../../../a/b/c",
                      "../../../a//b/z/../c/" };
        for(int i = 0; i < t.length; i++) {
            System.out.println(t[i] + " : " + Misc.stripPath(t[i]));
        }
    }
}



