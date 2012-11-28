/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare.lib.marker.draw;

import org.mixare.lib.gui.Model3D;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.render.MixVector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcel;
import android.util.Log;

/**
 * A draw command that can be send by a plugin marker to draw an image on the
 * client. This class extends the DrawCommand, that stores the properties, so
 * that it can be transfered to the client.
 * 
 * @author A. Egal
 */
public class DrawObj extends DrawCommand {

	private static String CLASS_NAME = DrawObj.class.getName();

	private static String PROPERTY_NAME_VISIBLE = "visible";
	private static String PROPERTY_NAME_SIGNMARKER = "signMarker";
	private static String PROPERTY_NAME_OBJ = "obj";

	public static DrawObj init(Parcel in) {
		Boolean visible = Boolean.valueOf(in.readString());
		ParcelableProperty signMarkerHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		ParcelableProperty bitmapHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		return new DrawObj(visible, (MixVector) signMarkerHolder.getObject(),
				(Model3D) bitmapHolder.getObject());
	}

	public DrawObj(boolean visible, MixVector signMarker, Model3D obj) {
		super(CLASS_NAME);
		setProperty(PROPERTY_NAME_VISIBLE, visible);
		setProperty(PROPERTY_NAME_SIGNMARKER, new ParcelableProperty(
				"org.mixare.lib.render.MixVector", signMarker));
		setProperty(PROPERTY_NAME_OBJ, new ParcelableProperty("org.mixare.lib.gui.Model3D", obj));
	}

	@Override
	public void draw(PaintScreen dw) {
		if (getBooleanProperty(PROPERTY_NAME_VISIBLE)) {
			MixVector signMarker = getMixVectorProperty(PROPERTY_NAME_SIGNMARKER);
			Model3D object = getModel3DProperty(PROPERTY_NAME_OBJ);

			dw.setColor(Color.argb(155, 255, 255, 255));
			if (object == null || object.getObj() == null) {
				Log.e("mixare-lib", "object = null");
				return;
			}
			
			object.setRot_x(signMarker.x);
			object.setRot_y(signMarker.y - (35));
			dw.paint3DModel(object);
			// dw.paintBitmap(bitmap, signMarker.x - (bitmap.getWidth()/2),
			// signMarker.y - (bitmap.getHeight() / 2));
		}
	}
}
