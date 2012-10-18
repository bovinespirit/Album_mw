/*
 *  Photo Album
 *
 *  Copyright (C) Matthew West 2002-2005
 *
 *  $Id: Album.java,v 1.11 2005/02/06 03:26:38 mfw Exp $
 *
 * @author <a href="mailto:album@matthewwest.co.uk">Matthew West</a>
 * @version RCS:$Revision: 1.11 $ $Date: 2005/02/06 03:26:38 $ Dist:0.1 12-July-2002 10:38
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

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

import java.net.*;
import java.io.*;

/**
 * Creates HTML photo album from an XML file
 * and some high-res photo images
 */
public class Album {
    /** Name of XML file */
    private String xmlin = null;
    /** Name of XSL file, or null if none is provided */
    private String xslin = null;
    /** Name of direcotry in which the Photo Album is to be created */
    private String targetdirname = null;
    /** Prefix to be added to locations */
    private String hrefprefix = null;
    /** Location of image directory */
    private String imageloc = null;
    /** File object representing target directory */
    private File targetdir = null;
    /** If this == true then a help message is printed, but nothing else is done */
    private boolean help;
    /** DOM object representing the source XML file */
    private org.w3c.dom.Document indoc = null;

    /**
     * Reads args[] and assigns variables
     * If the arguments are invalid then help will equal
     * true on exit
     */
    public Album(String[] args) {
	int i;
        help = false;
        String opt = new String();
	for(i = 0; i < args.length; i++) {
	    if(args[i].startsWith("-")) {
		if(args[i].charAt(1) == 'h') {
		    help = true;
		    break;
		}
		opt = args[i];
		opt.trim();
	    } else {
		if(opt.compareTo("-f") == 0) {
		    xmlin = args[i];
		}
		else if(opt.compareTo("-x") == 0)
		    xslin = args[i];
		else if(opt.compareTo("-t") == 0)
		    targetdirname = args[i];
		else if(opt.compareTo("-p") == 0)
		    hrefprefix = args[i];
		else if(opt.compareTo("-i") == 0)
		    imageloc = args[i];
		opt = "";
	    }
	}

	if(xmlin == null) {
	    System.err.println("You must specify an input file.");
	    help = true;
	}

	if(targetdirname == null) {
	    System.err.println("You must specify where you want the photo album to go.");
	    help = true;
	}

	if(help) {
	    System.out.println("Photo Album Creator");
	    System.out.println("(C) Matthew West 2002-2005  www.matthewwest.co.uk\n");
	    System.out.println("Options:");
	    System.out.println("  -f filename   XML file describing the album.");
	    System.out.println("  -x filename   XSL template.");
	    System.out.println("  -t directory  Target directory for files.");
	    System.out.println("  -p prefix     Prefix to be added to href's.");
	    System.out.println("  -i directory  Location of image/ directory");
	    System.out.println("  -h            Print this message and exit.");
	}
    }

    /**
     * Reads the input file and creates a DOM object
     */
    private org.w3c.dom.Document readInFile(String filename) {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
	org.w3c.dom.Document doc = null;

        dbf.setValidating(false);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setCoalescing(true);
        dbf.setExpandEntityReferences(true);
        try {
            db = dbf.newDocumentBuilder();
	    OutputStreamWriter errorWriter =
		new OutputStreamWriter(System.err, "UTF-8");
	    db.setErrorHandler(
		new SaxErrorHandler(new PrintWriter(errorWriter, true)));
        } catch (ParserConfigurationException pce) {
            System.err.println("Parser Config : " + pce.getMessage());
            System.exit(1);
        } catch (java.io.UnsupportedEncodingException uee) {
            System.err.println("Unsupported Encoding : "+uee.getMessage());
            System.exit(1);
	}
        try {
	    doc = db.parse(new File(filename));
        } catch (SAXException se) {
	    System.err.println("SAX Error : " + se.getMessage());
	    System.exit(1);
	} catch (IOException ioe) {
            System.err.println("I/O Error : " + ioe.getMessage());
            System.exit(1);
        }
	return doc;
    }

    /**
     * Returns true if the inititaliser set help, i.e. the
     * command line arguments were invalid
     */
    public boolean isHelp() {
	return help;
    }

    /**
     * This does all the work
     */
    public void run() throws FatalException, IOException {
        String imagedir;

        System.out.println("Read document, create DOM");
	indoc = readInFile(xmlin);

	targetdir = Misc.makeDir(targetdirname);
	if(imageloc)
	    imagedir = imageloc;
	else
	    imagedir = "images/";

	if(xslin != null) {
            System.out.println("Read xsl file");
	    XmlOutput.setXslFile(xslin);
        }

        System.out.println("Create data structure");
	SectionPart root = new SectionPart((Element) indoc.getDocumentElement(), null);
        System.out.println("Create menus");
	root.makeMenu();
        System.out.println("Create directories");
        root.createDirectories(targetdir, imagedir);
        System.out.println("Create images");
        root.createImageFiles();
        System.out.println("Create files");
	root.createFiles();
    }

    /**
     * Creates an Album object, then call run() to create the Album
     */
    public static void main(String[] args) {
	Album al = new Album(args);
	if(al.isHelp())
	    return;
	try {
	    al.run();
	} catch(FatalException fe) {
	    fe.printStackTrace();
	} catch(IOException ioe) {
	    ioe.printStackTrace();
	}
    }

    /** 
     * readInFile() wants one of these
     */
    private static class SaxErrorHandler implements ErrorHandler {
        private PrintWriter out;

        SaxErrorHandler(PrintWriter out) {
            this.out = out;
        }

        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                " Line=" + spe.getLineNumber() +
                ": " + spe.getMessage();
            return info;
        }

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }
        
        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}










