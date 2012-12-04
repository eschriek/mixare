/*
 * Copyright (C) 2010- Peer internet solutions
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
package org.mixare.lib.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.gui.ScreenObj;
import org.mixare.lib.model3d.OBJParser;
import org.mixare.lib.model3d.TDModel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Duplicate of the original Paintscreen which used a canvas. It still uses the
 * canvas but the actual drawing is done by opengl.
 * 
 * @author Edwin Schriek Nov 14, 2012 mixare-library
 * 
 */

public class PaintScreen implements Parcelable, GLSurfaceView.Renderer {
	private final String TAG = this.getClass().getName();
	private Canvas canvas;
	private Bitmap canvasMap;
	private int mWidth, mHeight;
	private Paint paint = new Paint();
	private Square window;
	private long dt;
	private String info;
	private double size;
	private boolean enableBlending;
	private MixViewInterface app;
	private DataViewInterface data;
	private Paint zoomPaint;
	private HashMap<String, Model3D> models;
	private MatrixGrabber grabber;

	public PaintScreen(Context cont, DataViewInterface dat) {
		this();

		data = (DataViewInterface) dat;

		assert (data) != null;

		app = (MixViewInterface) cont;
	}

	public PaintScreen() {
		Log.i(TAG, "Super");
		zoomPaint = new Paint();
		info = "";
		size = 0;

		GLParameters.ENABLE3D = true;
		GLParameters.DEBUG = false;
		GLParameters.BLENDING = true;

		canvasMap = Bitmap.createBitmap(GLParameters.WIDTH,
				GLParameters.HEIGHT, Config.ARGB_4444);
		window = new Square(paint, 0f, 0f, GLParameters.WIDTH,
				GLParameters.HEIGHT);

		canvas = new Canvas(canvasMap);
		grabber = new MatrixGrabber();
		models = new HashMap<String, Model3D>();

		paint.setTextSize(16);
		paint.setAntiAlias(true);
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.STROKE);
	}

	public PaintScreen(Parcel in) {
		readFromParcel(in);
		paint.setTextSize(16);
		paint.setAntiAlias(true);
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.STROKE);
	}

	public static final Parcelable.Creator<PaintScreen> CREATOR = new Parcelable.Creator<PaintScreen>() {
		public PaintScreen createFromParcel(Parcel in) {
			return new PaintScreen(in);
		}

		public PaintScreen[] newArray(int size) {
			return new PaintScreen[size];
		}
	};

	public void ready3D(GL10 gl, int width, int height) {
		if (GLParameters.DEBUG)
			Log.i(TAG, "Ready3D " + width + " " + height);

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);

		gl.glLoadIdentity();

		GLU.gluPerspective(gl, 67f, (float) width / height, 0.1f, 100f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		// gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

	public void ready2D(GL10 gl, int width, int height) {
		if (GLParameters.DEBUG)
			Log.i(TAG, "Ready2D " + width + " " + height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		gl.glOrthof(0, width, 0, height, -1f, 1f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0.375f, 0.375f, 0.0f);

		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_BLEND);
		gl.glEnable(GL10.GL_TEXTURE_2D);
	}

	@SuppressLint("NewApi")
	public void draw2D(GL10 gl) {
		if (window != null && canvasMap != null) {
			size = (canvasMap.getHeight() * canvasMap.getRowBytes()) / 1024;
			paintText(mWidth - (getTextWidth(info) + 150), mHeight
					- (mHeight - 100), info + " FPS : " + (1000 / dt)
					+ " size : " + size + " kb", false);
			if (enableBlending) {
				paintText(mWidth - (getTextWidth(info) + 150), mHeight
						- (mHeight - 120), "Blending is ON o.O", true);
			}

			if (!data.isInited()) {
				data.init(mWidth, mHeight);
			}

			if (app.isZoombarVisible()) {
				zoomPaint.setColor(Color.WHITE);
				zoomPaint.setTextSize(14);
				String startKM, endKM;
				endKM = "80km";
				startKM = "0km";

				paintText(canvas.getWidth() / 100 * 4,
						canvas.getHeight() / 100 * 85, startKM, false);
				canvas.drawText(endKM, canvas.getWidth() / 100 * 99 + 25,
						canvas.getHeight() / 100 * 85, zoomPaint);

				int height = canvas.getHeight() / 100 * 85;
				int zoomProgress = app.getZoomProgress();
				if (zoomProgress > 92 || zoomProgress < 6) {
					height = canvas.getHeight() / 100 * 80;
				}
				canvas.drawText(app.getZoomLevel(), (canvas.getWidth()) / 100
						* zoomProgress + 20, height, zoomPaint);
			}

			data.draw(this);

			try {
				window.draw(gl, Util.loadGLTexture(gl, canvasMap));
			} catch (GLException e) {
				// TODO: Throw this to a toast?
			}
		}
	}

	// Berekend diepte in een perspective matrix, 0.1 staat voor zNear en 100
	// staat in dit geval voor zFar; Oftewel gezichtsveld is 99.9 meter
	// TODO: zFar aanpassen aan de radius van de marker
	public float distanceToDepth(float distance) {
		return ((1 / 0.1f) - (1 / distance)) / ((1 / 0.1f) - (1 / 100f));
	}

	public float[] unproject(float rx, float ry, float rz) {// TODO Factor in
															// projection matrix
		float[] modelInv = new float[16];
		if (!android.opengl.Matrix.invertM(modelInv, 0, grabber.mModelView, 0))
			throw new IllegalArgumentException("ModelView is not invertible.");
		float[] projInv = new float[16];
		if (!android.opengl.Matrix.invertM(projInv, 0, grabber.mProjection, 0))
			throw new IllegalArgumentException("Projection is not invertible.");
		float[] combo = new float[16];
		android.opengl.Matrix.multiplyMM(combo, 0, modelInv, 0, projInv, 0);
		float[] result = new float[4];
		float vx = 0;
		float vy = 0;
		float vw = GLParameters.WIDTH;
		float vh = GLParameters.HEIGHT;
		float[] rhsVec = { ((2 * (rx - vx)) / vw) - 1,
				((2 * (ry - vy)) / vh) - 1, 2 * rz - 1, 1 };
		android.opengl.Matrix.multiplyMV(result, 0, combo, 0, rhsVec, 0);
		float d = 1 / result[3];
		float[] endResult = { result[0] * d, result[1] * d, result[2] * d };
		return endResult;
	}

	public void draw3D(GL10 gl) {
		synchronized (models) {
			for (Model3D model : models.values()) {
				grabber.getCurrentState(gl);
				// Magische lijntjes, berekend van scherm coordinaten de
				// bijbehorende projection coordinaten en vertaald ook de
				// afstand naar het object naar een waarde ten opzichte van
				// zNear en zFar
				float[] points = unproject(model.getxPos(),
						(GLParameters.HEIGHT - model.getyPos()),
						distanceToDepth((float) model.getDistance()));

				gl.glTranslatef(points[0], points[1], points[2]);

				// System.out.println(model.getDistance() + " " + points[2]
				// + model.getSchaal() + " " + model.getRot_x() + " "
				// + model.getRot_y() + " " + model.isBlended() + " "
				// + model.getBearing());

				// Scale
				// Schaalen met meer dan 5 zorgt voor enorme objecten. 2.5 keer
				// het originele formaat ziet er wel redelijk uit.
				if (model.getSchaal() > 5) {
					model.setSchaal(2.5f);
				}

				gl.glScalef(model.getSchaal(), model.getSchaal(),
						model.getSchaal());

				// // Rotate
				gl.glRotatef((float) (model.getRot_x() + model.getBearing()),
						1f, 0f, 0f);
				gl.glRotatef(model.getRot_y(), 0f, 1f, 0f);
				gl.glRotatef(model.getRot_z(), 0f, 0f, 1f);

				// Blenderen
				if (model.isBlended() == 0) {
					gl.glEnable(GL10.GL_BLEND);
					gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
				}

				// Tekenen
				model.getModel().draw(gl);

			}
		}
	}

	public void paint3DModel(Model3D model) {
		Iterator<Entry<String, Model3D>> it = models.entrySet().iterator();
		boolean create = false;

		if (models.isEmpty()) {
			create = true;
		}

		// To keep GC happy, load model once in memory.
		while (it.hasNext()) {
			Entry<String, Model3D> entry = it.next();

			if (model.getObj().equalsIgnoreCase(entry.getKey())) {
				create = false;
				// This should never be true, but who knows
				if (model.equals(entry.getValue())) {
					break;
				} else {
					entry.getValue().update(model);
					break;
				}

			} else {
				create = true;
			}
		}

		if (create) {
			Log.i(TAG, "Create!");
			// Why not one parser instance, because it segfaults.Why? No idea!
			OBJParser parser = new OBJParser((Context) app);
			try {
				model.setModel(parser.parseOBJ(model.getObj()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			models.put(model.getObj(), model);
		}

	}

	@SuppressLint("NewApi")
	public void onDrawFrame(GL10 gl) {
		long time1 = System.currentTimeMillis();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		canvasMap.eraseColor(0);

		ready2D(gl, GLParameters.WIDTH, GLParameters.HEIGHT);
		draw2D(gl);

		gl.glPushMatrix();

		if (GLParameters.ENABLE3D) {
			ready3D(gl, GLParameters.WIDTH, GLParameters.HEIGHT);
			draw3D(gl);
		}

		gl.glPopMatrix();
		dt = System.currentTimeMillis() - time1;
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.i(TAG, "onSurfaceChanged " + width + " " + height);

		mWidth = width;
		mHeight = height;

		gl.glViewport(0, 0, width, height);
		gl.glLoadIdentity();

		// Coordinaten mappen naar scherm, bottom left georienteerd
		gl.glOrthof(0, width, 0, height, -1, 1);

	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.i(TAG, "onSurfaceCreated");
		dt = 1;

		String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
		String version = gl.glGetString(GL10.GL_VERSION);
		String renderer = gl.glGetString(GL10.GL_RENDERER);
		GLParameters.SOFTWARERENDERER = renderer.contains("PixelFlinger");
		GLParameters.isOPENGL10 = version.contains("1.0");
		GLParameters.DRAWTEX = extensions.contains("draw_texture");
		// VBOs are standard in GLES1.1
		// No use using VBOs when software renderering, esp. since older
		// versions of the software renderer
		// had a crash bug related to freeing VBOs.
		GLParameters.VBO = !GLParameters.SOFTWARERENDERER
				&& (!GLParameters.isOPENGL10 || extensions
						.contains("vertex_buffer_object"));

		info = ("Graphics Support " + version + " (" + renderer + "): "
				+ (GLParameters.DRAWTEX ? "draw texture, " : "") + (GLParameters.VBO ? "vbos"
				: ""));

	}

	public Canvas getCanvas() {
		return canvas;
	}

	/**
	 * !! Using this will mess up 3D drawing.
	 * 
	 * @param canvas
	 */
	@Deprecated
	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setFill(boolean fill) {
		if (fill)
			paint.setStyle(Paint.Style.FILL);
		else
			paint.setStyle(Paint.Style.STROKE);
	}

	public void setColor(int c) {
		paint.setColor(c);
	}

	public void setStrokeWidth(float w) {
		paint.setStrokeWidth(w);
	}

	public void paintLine(float x1, float y1, float x2, float y2) {

		canvas.drawLine(x1, y1, x2, y2, paint);
	}

	public void paintRect(float x, float y, float width, float height) {

		canvas.drawRect(x, y, x + width, y + height, paint);
	}

	public void paintRoundedRect(float x, float y, float width, float height) {
		// rounded edges. patch by Ignacio Avellino

		RectF rect = new RectF(x, y, x + width, y + height);
		canvas.drawRoundRect(rect, 15F, 15F, paint);
	}

	public void paintBitmap(Bitmap bitmap, float left, float top) {

		canvas.drawBitmap(bitmap, left, top, paint);
	}

	public void paintPath(Path path, float x, float y, float width,
			float height, float rotation, float scale) {

		canvas.save();
		canvas.translate(x + width / 2, y + height / 2);
		canvas.rotate(rotation);
		canvas.scale(scale, scale);
		canvas.translate(-(width / 2), -(height / 2));
		canvas.drawPath(path, paint);
		canvas.restore();
	}

	public void paintCircle(float x, float y, float radius) {
		canvas.drawCircle(x, y, radius, paint);

	}

	public void paintText(float x, float y, String text, boolean underline) {

		paint.setUnderlineText(underline);
		canvas.drawText(text, x, y, paint);
	}

	public void paintObj(ScreenObj obj, float x, float y, float rotation,
			float scale) {

		canvas.save();
		canvas.translate(x + obj.getWidth() / 2, y + obj.getHeight() / 2);
		canvas.rotate(rotation);
		canvas.scale(scale, scale);
		canvas.translate(-(obj.getWidth() / 2), -(obj.getHeight() / 2));
		obj.paint(this);
		canvas.restore();
	}

	public float getTextWidth(String txt) {
		// float w = paint.measureText(txt);
		// Log.i(TAG, "" + w);
		return paint.measureText(txt);
	}

	public float getTextAsc() {
		return -paint.ascent();
	}

	public float getTextDesc() {
		return paint.descent();
	}

	public float getTextLead() {
		return 0;
	}

	public void setFontSize(float size) {
		paint.setTextSize(size);
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(GLParameters.WIDTH);
		dest.writeInt(GLParameters.HEIGHT);
	}

	public void readFromParcel(Parcel in) {
		GLParameters.HEIGHT = in.readInt();
		GLParameters.WIDTH = in.readInt();
		canvas = new Canvas();
	}

}
