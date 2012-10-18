/*
 *  Photo Album
 *
 *  Copyright (C) Matthew West 2002
 *
 *  $Id: FatalFileException.java,v 1.4 2002/08/24 17:08:43 mfw Exp $	
 *
 * @author <a href="mailto:album@matthewwest.co.uk">Matthew West</a>
 * @version RCS:$Revision: 1.4 $ $Date: 2002/08/24 17:08:43 $ Dist:0.1 12-July-2002 10:38
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

/**
 * This program won't run if certain files cannot be read, or accessed.
 * This exception is thrown, and displays its errors in a (vaguely)
 * consistant manner.
 */
public class FatalFileException extends FatalException {
    FatalFileException(final String s, final String f) {
	super("File Error : " + s + " - " + f);
    }
    FatalFileException(final String s, final File f) {
	super("File Error : " + s + " - " + f.getName());
    }
}
