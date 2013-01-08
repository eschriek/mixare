package org.mixare.lib;

import org.mixare.lib.gui.PaintScreen;

/**
 * Used for calling the essential drawing methods from within the GLThread
 * @author Edwin Schriek
 * Nov 14, 2012
 * mixare-library
 *
 */
public interface DataViewInterface {
	void draw(PaintScreen p);
	void init(int width, int height);
	boolean isInited();
}
