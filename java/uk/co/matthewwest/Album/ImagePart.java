/*
 *  Photo Album
 *
 *  Copyright (C) Matthew West 2002-2005
 *
 *  $Id: ImagePart.java,v 1.20 2005/02/06 03:34:07 mfw Exp $	
 *
 * @author <a href="mailto:album@matthewwest.co.uk">Matthew West</a>
 * @version RCS:$Revision: 1.20 $ $Date: 2005/02/06 03:34:07 $ Dist:0.1 12-July-2002 10:38
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

import org.w3c.dom.*;

import java.net.*;
import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;

import com.sun.image.codec.jpeg.*;

/**
 * This creates an HTML page for each image in the Photo Album.
 */
public class ImagePart extends PartBase {
    private final int thumblongedge = 150;
    private final int biglongedge = 600;

    private File imagesrc; 
    static private int image_number = 0;
    private Size thumbsize;
    private Size bigsize;
    private String srcfilename = null;
    private File imagedestdir = null;
    private String imagedeststr = "";

    /**
     * Creates an image object from data in the Image Element
     */
    public ImagePart(final Element n, final PartBase up) throws FatalException {
	setUp(up);
	depth = getUp().getDepth();
	node = n;
	image_number++;
	readValues();
	if((name == null) || (name.equals("")))
	    name = "image " + image_number;
	else
	    fixTitle();
	setFilename("");
	feedback("Image");

	srcfilename = n.getAttribute("src");
	if(srcfilename.equals(""))
	    throw new InternalException("No image source specified " + up.getTitle());
	imagesrc = Misc.checkFile(srcfilename);
    }

    /**
     * Overriden from PartBase
     */
    public void createDirectories(final File targetdir, final String imagedir) throws FatalException {
        this.imagedeststr = getPrefix() + imagedir;
        this.imagedestdir = Misc.checkDir(targetdir, imagedeststr);
        thisdir = targetdir;
        
    }

    /**
     * Returns html page
     */
    public String getLink() {
	return filename + ".html";
    }

    /**
     * Creates the image tag for a page
     * @param xo XmlOutput object to write to 
     */
    public void writeImageTag(final XmlOutput xo) throws InternalException {
        xo.element("thumb", new String[][] {new String[] {"href", getLink()},
                                            new String[] {"description", description},
                                            new String[] {"title", title}});
	xo.image(imagedeststr+filename+"sm.jpg", thumbsize.x, thumbsize.y); 
	xo.closeElement();
    }

    /**
     * Creates scaled version of image <br />
     * Uses non-portable Sun codec
     * @param e Length of long edge
     * @param file File to save to
     * @return Size of image that was created
     */
    public Size scaleImage(final int e, final File file) throws FatalFileException {
	Size newsize = null;
	Size oldsize;
	boolean create = true;
	try {
	    if(file.exists()) {
		if(file.lastModified() > imagesrc.lastModified()) {
		    InputStream is = new FileInputStream(file);
		    JPEGImageDecoder jid = JPEGCodec.createJPEGDecoder(is);
		    BufferedImage srcbi = jid.decodeAsBufferedImage();
		    newsize = new Size(srcbi.getWidth(), srcbi.getHeight());
		    create = false;
		}
	    }
	    if(create) { // File doesn't exist or is older then the source file
		InputStream is = new FileInputStream(imagesrc);
		JPEGImageDecoder jid = JPEGCodec.createJPEGDecoder(is);
		BufferedImage srcbi = jid.decodeAsBufferedImage();
		BufferedImage destbi = null;
		
		oldsize= new Size(srcbi.getWidth(), srcbi.getHeight());
		float factor;
		if(oldsize.y > oldsize.x)
		    factor = (float)e / (float)oldsize.y;
		else	
		    factor = (float)e / (float)oldsize.x;
		newsize = new Size((int)Math.rint(oldsize.x * factor) ,
				   (int)Math.rint(oldsize.y * factor));
	    
		AffineTransformOp at = new AffineTransformOp(AffineTransform.getScaleInstance(factor, factor), 
							     null);
		destbi = at.filter(srcbi, null);

		OutputStream os = new FileOutputStream(file);
		JPEGImageEncoder jie = JPEGCodec.createJPEGEncoder(os);
		jie.encode(destbi);
		
		is.close();
		os.close();
                try {
                    Thread.sleep(5);
                } catch(InterruptedException ie) { }
	    }
	} catch (FileNotFoundException fe) {
	    throw new FatalFileException("File not found", srcfilename);
	} catch (IOException fe) {
	    throw new FatalFileException(fe.getMessage(), srcfilename);
	} catch (com.sun.image.codec.jpeg.ImageFormatException ife) {
	    throw new FatalFileException(ife.getMessage(), srcfilename);
	}
	return newsize;
    }

    /**
     * Override. Creates a new IndexImage.
     * @return newly created IndexImage
     */
    public IndexImage getIndexImage() {
        return new IndexImage(this);
    }

    /**
     * Creates a small image element.  Called by IndexImage.
     * @param xo Target XML output
     * @param prefix extra filename prefix
     */
    public void makeSmallImageElement(XmlOutput xo, String prefix) throws InternalException {
//      System.out.println("  Creating image element : " + imagedeststr + filename + "sm.jpg");
        xo.image(Misc.stripPath(prefix + imagedeststr+filename+"sm.jpg"), 
                 thumbsize.x, 
                 thumbsize.y); 
    }

    /**
     * Creates the image files, filenamesm.jpg and filenamebg.jpg and sets the sizes
     */
    public void createImageFiles() throws FatalException, IOException {
	thumbsize = scaleImage(thumblongedge, new File(imagedestdir, filename + "sm.jpg"));
	bigsize = scaleImage(biglongedge, new File(imagedestdir,  filename + "bg.jpg"));
	try {
	    Thread.sleep(10);
	} catch(InterruptedException ie) { }
    }

    /**
     * Creates filename.html
     */
    public void createFiles() throws FatalException, IOException {
	XmlOutput xo = null;
	String tmptitle;

	xo = new XmlOutput();
	xo.textElement("prefix", getPrefix());
	menu.createXml(xo);
	if(title == null) {
	    tmptitle = getUp().getTitle();
	    xo.textElement("title", tmptitle);
	    xo.textElement("heading", tmptitle);
	} else {
	    xo.textElement("title", title + " : " + getUp().getTitle());
	    xo.textElement("heading", title);
	}

	if(description != null)
	    xo.textElement("description", description);
	if(text != null)
	    xo.textElement("text", text);
	makeLinks(xo);
	xo.image(imagedeststr+filename + "bg.jpg", bigsize.x, bigsize.y);
	xo.saveFile(Misc.makeFile(thisdir, filename + ".html"));
    }

    /**
     * Convenience class to make chucking sizes around easier
     */
    private class Size {
	public int x;
	public int y;
	Size(int x, int y) {
	    this.x = x;
	    this.y = y;
	}
    }
}
    

