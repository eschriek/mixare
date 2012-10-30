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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.mixare.MixContext;
import org.mixare.MixView;

import com.example.objLoader.OBJParser;
import com.example.objLoader.TDModel;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * This class has the ability to set up the main view and it paints objects on
 * the screen
 */

public class PaintScreen implements Parcelable, GLSurfaceView.Renderer {
	private final String TAG = this.getClass().getName();
	private Canvas canvas;
	private Bitmap canvasMap;
	private int mWidth, mHeight;
	private Paint paint = new Paint();
	private Square window;
	private Cube cube;
	private float rotation;
	private boolean debug;
	private long startTime;
	private long endTime;
	private long dt;
	private String info;
	private OBJParser parser;
	private TDModel model;
	
	public PaintScreen() {
		Log.i(TAG, "Super");
		parser = new OBJParser();
		model = parser.parseOBJ("/sdcard/untitled.obj");
		debug = false;
		info = "";
		canvasMap = Bitmap.createBitmap(1280, 752, Config.ARGB_4444);
		cube = new Cube();
		window = new Square(paint, 0f, 0f, 1280, 752);

		canvas = new Canvas(canvasMap);

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
		if (debug)
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
		if (debug)
			Log.i(TAG, "Ready2D " + width + " " + height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		// GLU.gluOrtho2D(gl, 0.0f, (float) width, 0f, (float) height);
		gl.glOrthof(0, width, 0, height, -1f, 1f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0.375f, 0.375f, 0.0f);

		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_TEXTURE_2D);
	}

	// TODO: Memory leak fixen
	@SuppressLint("NewApi")
	public void draw2D(GL10 gl) {
		if (window != null && canvasMap != null) {
			paintText(mWidth-(getTextWidth(info)+70), mHeight-(mHeight-100), info + " FPS : " + (1000 / dt), false);
			MixView.getDataView().draw(MixView.getdWindow());
			
			window.draw(gl, Util.loadGLTexture(gl, canvasMap));
		}
	}
	
	public void draw3D(GL10 gl) {
		rotation += 1.50;

		gl.glTranslatef(0.0f, 0.0f, -10.0f);
		gl.glRotatef(rotation, 1f, 1f, 1f);

		model.draw(gl);
		//cube.draw(gl);
	}

	@SuppressLint("NewApi")
	public void onDrawFrame(GL10 gl) {
		long time1 = System.currentTimeMillis();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		canvasMap.eraseColor(0);

		// FPS LIMIT
		// endTime = System.currentTimeMillis();
		// dt = endTime - startTime;
		// if (dt < 33)
		// try {
		// Thread.sleep(33 - dt);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		//
		// startTime = System.currentTimeMillis();

		ready2D(gl, mWidth, mHeight);	
		draw2D(gl);

		gl.glPushMatrix();

		ready3D(gl, mWidth, mHeight);
		draw3D(gl);

		gl.glPopMatrix();
		dt = System.currentTimeMillis() - time1;
		// Log.i(TAG, "" + time2);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.i(TAG, "onSurfaceChanged " + width + " " + height);

		mWidth = width;
		mHeight = height;

		if (!MixView.getDataView().isInited()) {
			MixView.getDataView().init(width, height);
		}
		
		gl.glViewport(0, 0, width, height);
		gl.glLoadIdentity();

		// Coordinaten mappen naar scherm, bottom left georienteerd
		gl.glOrthof(0, width, 0, height, -1, 1);

	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.i(TAG, "onSurfaceCreated");
		// Niet meer nodig
		dt = 1;
		startTime = System.currentTimeMillis();

		String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
		String version = gl.glGetString(GL10.GL_VERSION);
		String renderer = gl.glGetString(GL10.GL_RENDERER);
		boolean isSoftwareRenderer = renderer.contains("PixelFlinger");
		boolean isOpenGL10 = version.contains("1.0");
		boolean supportsDrawTexture = extensions.contains("draw_texture");
		// VBOs are standard in GLES1.1
		// No use using VBOs when software renderering, esp. since older
		// versions of the software renderer
		// had a crash bug related to freeing VBOs.
		boolean supportsVBOs = !isSoftwareRenderer
				&& (!isOpenGL10 || extensions.contains("vertex_buffer_object"));
		// canvas.drawText(version, 0, 20, 5, 5, paint);

		info = ("Graphics Support " +version + " (" + renderer + "): "
				+ (supportsDrawTexture ? "draw texture," : "")
				+ (supportsVBOs ? "vbos" : ""));

	}

	public Canvas getCanvas() {
		return canvas;
	}

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
		//float w = paint.measureText(txt);
		//Log.i(TAG, "" + w);
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
		dest.writeInt(mWidth);
		dest.writeInt(mHeight);
	}

	public void readFromParcel(Parcel in) {
		mHeight = in.readInt();
		mWidth = in.readInt();
		canvas = new Canvas();
	}

}