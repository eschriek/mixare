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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.mixare.lib.DataViewInterface;
import org.mixare.lib.MixViewInterface;
import org.mixare.lib.R;
import org.mixare.lib.model3d.Mesh;
import org.mixare.lib.model3d.ModelLoadException;
import org.mixare.lib.model3d.parsers.ObjReader;
import org.mixare.lib.model3d.parsers.OffReader;
import org.mixare.lib.model3d.text.GLText;
import org.mixare.lib.model3d.text.TextBox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Duplicate of the original Paintscreen which used a canvas. It still uses the
 * canvas for certain parts of the screen like the radar because rewriting it
 * for openGL won't increase the speed much.
 * 
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
	private long startTime;
	private long endTime;
	private String info;
	private double size;
	private float rotation;
	private MixViewInterface app;
	private DataViewInterface data;
	private Paint zoomPaint;
	private GLText text, textInfo;
	private HashMap<String, Model3D> models;
	private Set<TextBox> text3d;
	private Set<Square> images;
	private MatrixGrabber grabber;
	private Mesh poi;
	private Mesh triangle;
	private Mesh arrow;
	private float zNear;
	private float zFar;

	public PaintScreen(Context cont, DataViewInterface dat) {
		this();

		data = (DataViewInterface) dat;
		app = (MixViewInterface) cont;

		try {
			// Load default models
			InputStream in = ((Context) app).getAssets().open("poi2.obj");
			InputStream in2 = ((Context) app).getAssets().open("triangle2.obj");
			InputStream in3 = ((Context) app).getAssets().open("arrow3.obj");

			arrow = new ObjReader((Context) app).readMesh(in3);
			triangle = new ObjReader((Context) app).readMesh(in2);
			poi = new ObjReader((Context) app).readMesh(in);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ModelLoadException e) {
			e.printStackTrace();
		}
	}

	public PaintScreen() {
		Log.i(TAG, "Super");
		zoomPaint = new Paint();
		info = "";
		size = 0;

		startTime = System.currentTimeMillis();
		zNear = 0.1f;
		zFar = 100f;

		GLParameters.ENABLE3D = true;
		GLParameters.DEBUG = false;
		GLParameters.BLENDING = true;

		// 4444 Because we need the alpha, 8888 would improve quality at the
		// cost of speed
		canvasMap = Bitmap.createBitmap(110, 110, Config.ARGB_4444);
		window = new Square(paint, 0f, 0f, 110, 110);

		text3d = new HashSet<TextBox>();
		images = new HashSet<Square>();
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

	/**
	 * Puts the projection matrix in perspective based on zFar, zNear, width and
	 * height. Also enables depth and disables textures
	 * 
	 * 
	 * @param gl
	 *            GL object supplied by onDrawFrame
	 * @param width
	 *            Width of the matrix, usually screen width. Also used to
	 *            calculate aspect ratio used by gluPerspective
	 * @param height
	 *            Height of the matrix, usually screen height. Also used to
	 *            calculate aspect ratio used by gluPerspective
	 */
	public void ready3D(GL10 gl, int width, int height) {
		if (GLParameters.DEBUG)
			Log.i(TAG, "Ready3D " + width + " " + height);

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);

		gl.glLoadIdentity();

		GLU.gluPerspective(gl, 67f, (float) width / height, zNear, zFar);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

	/**
	 * Puts the projection matrix in parallel projection (ortho). Disables depth
	 * and enables textures and blending
	 * 
	 * @param gl
	 *            GL object supplied by onDrawFrame
	 * @param width
	 *            Width of the matrix, usually screen width
	 * @param height
	 *            Height of the matrix, usually screen height
	 */
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
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND); // Enable Alpha Blend
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}

	/**
	 * This method is used to draw everything without depth. This means
	 * everything but 3d models.
	 * 
	 * @param gl
	 *            GL object supplied by onDrawFrame
	 * @throws Object3DException
	 *             Throwed if anything essential went wrong.
	 */
	@SuppressLint("NewApi")
	public void draw2D(GL10 gl) throws Object3DException {
		if (window != null && canvasMap != null) {
			size = (canvasMap.getHeight() * canvasMap.getRowBytes()) / 1024;

			for (Square s : images) { // Size in kb of all bitmaps
				if (s.getImg() != null) {
					size += ((s.getImg().getHeight() * s.getImg().getRowBytes()) / 1024);
				}
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

				text.begin();
				text.draw(startKM, mWidth / 100 * 2,
						(mHeight - (mHeight / 100 * 85)));
				text.draw(endKM, mWidth / 100 * 99, mHeight
						- (mHeight / 100 * 85));

				int height = mHeight / 100 * 85;
				int zoomProgress = app.getZoomProgress();
				if (zoomProgress > 92 || zoomProgress < 6) {
					height = mHeight / 100 * 80;
				}

				text.draw(app.getZoomLevel(), mWidth / 100 * zoomProgress + 20,
						(mHeight - height));
				text.end();
			}

			data.draw(this);

			if (GLParameters.ENABLEBB) {
				textInfo.begin();
				textInfo.draw(info + " FPS : " + (1000 / dt) + " size : "
						+ size + " kb", (mWidth - (getTextWidth(info) + 210)),
						mHeight - (mHeight - 100));
				textInfo.end();
			}

			// if (text3d.size() >= GLParameters.MAX_STACK_DEPTH - 2) { //
			// throw new Object3DException("Matrix stack overflow, Depth > : "
			// + GLParameters.MAX_STACK_DEPTH);
			// }

			for (TextBox t : text3d) {

				text.begin(1.0f, 1.0f, 1.0f, 1.0f); // Begin Text Rendering
													// (Set Color WHITE), for
													// alpha

				String[] split = splitStringEvery(t.getTekst(), 10);

				t.setBlockH((int) (split.length * text.getHeight()));
				t.setBlockW((int) getTextWidth(t.getTekst()));

				gl.glLoadIdentity();
				gl.glPushMatrix();

				gl.glTranslatef(t.getLoc().x - (getTextWidth(split[0]) / 2),
						mHeight - (t.getLoc().y + (text.getHeight() / 2)), 0);
				gl.glRotatef((360 - t.getRotation()), 0f, 0f, 1f);
				gl.glTranslatef(-(getTextWidth(split[0]) / 2),
						-(text.getHeight() / 2), 0);

				int tick = -1;
				for (String s : split) {

					text.draw(s, 0, 0 - (tick * text.getHeight()));

					tick++;
				}

				text.end();
				gl.glPopMatrix();

				t.setLoc(new PointF(t.getLoc().x, t.getLoc().y
						- text.getHeight()));
				t.setBlockW(t.getBlockW() + 10); // Just a little wider

				if (GLParameters.ENABLEBB) {
					drawBB(t.getTekst() + "bb", t);
				}
			}

			window.draw(gl, Util.loadGLTexture(gl, canvasMap, "Radar"));

			for (Square s : images) {
				if (s.getTextures()[0] == 0 && s.getImg() != null) {
					s.setTextures(Util.loadGLTexture(gl, s.getImg(), "Bitmap"));
				}

				// if(s.getRotation() != 0) {
				// System.out.println(s.getRotation() + " " +
				// s.getIdentifier());
				// gl.glRotatef(s.getRotation(), 0f, 0f, 1f);
				// }

				gl.glColor4f(1f, 1f, 1f, 0.6f); // Transparant images
				s.draw(gl);

			}

		}
	}

	/**
	 * This method is the fastest way to split a string every n chars. See
	 * {@link http
	 * ://stackoverflow.com/questions/12295711/split-a-string-at-every
	 * -nth-position}
	 * 
	 * @param s
	 *            The String that should be split
	 * @param interval
	 *            How much chars per piece
	 * @return Returns array of strings
	 */
	public String[] splitStringEvery(String s, int interval) {
		int arrayLength = (int) Math.ceil(((s.length() / (double) interval)));
		String[] result = new String[arrayLength];

		int j = 0;
		int lastIndex = result.length - 1;
		for (int i = 0; i < lastIndex; i++) {
			result[i] = s.substring(j, j + interval);
			j += interval;
		} // Add the last bit
		result[lastIndex] = s.substring(j);

		return result;
	}

	/**
	 * Calculates distance in meters to a corresponding Z value in perspective
	 * view. TODO: Adjust zFar to the radius of the marker
	 * 
	 * @param distance
	 *            Dinstance in meters which should be > zNear && < zFar.
	 * @return a Z value which can be used by glTranslate
	 */
	public float distanceToDepth(float distance) {
		return ((1 / zNear) - (1 / distance)) / ((1 / zNear) - (1 / zFar));
	}

	/**
	 * Translates screen coordinates into corresponding coordinates in
	 * perspective view.
	 * 
	 * @param rx
	 *            X coordinate
	 * @param ry
	 *            Y coordinate
	 * @param rz
	 *            Z coordinate
	 * @return Returns a float array of size 3 containing the translated
	 *         coordinates
	 */
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

	/**
	 * This method draws everything which needs a depth buffer. At the moment
	 * this only concerns 3D models which can be added by
	 * {@link PaintScreen#paint3DModel(Model3D)}
	 * 
	 * @param gl
	 *            GL object supplied by onDrawFrame
	 */
	public void draw3D(GL10 gl) {
		// rotation += 2.50; Eye candy

		synchronized (models) {
			for (Model3D model : models.values()) {
				grabber.getCurrentState(gl);

				// Following lines calculate screen coordinates into projection
				// space. Also calculates the distance to the object using
				// distanceToDepth

				gl.glPushMatrix();

				float[] points = null;
				if (model.getColor() == org.mixare.lib.model3d.Color.TO_FAR) {
					points = unproject(model.getxPos(),
							(GLParameters.HEIGHT - model.getyPos()),
							distanceToDepth(50)); // If the object is to far we
													// want to see this (Arena
													// only)
				} else {
					points = unproject(model.getxPos(),
							(GLParameters.HEIGHT - model.getyPos()),
							distanceToDepth((float) model.getDistance()));
				}

				gl.glTranslatef(points[0], points[1], points[2]);
				//gl.glTranslatef(-15, 2, 0);
				// Scale
				// Scale with more then 50 causes huge objects
				if (model.getSchaal() > 50) {
					model.setSchaal(25);
				} else if (model.getSchaal() < 20) {
					model.setSchaal(25);
				}

				gl.glScalef(model.getSchaal(), model.getSchaal(),
						model.getSchaal());

				// Rotate
				gl.glRotatef(rotation, 0f, 1f, 0f);

				// If you want to rotate based on location use
				// model.getBearing()

				// gl.glRotatef((float) (model.getRot_x() +
				// model.getBearing()),
				// 1f, 0f, 0f);
				// gl.glRotatef(model.getRot_y(), 0f, 1f, 0f);
				if (model.getRot_z() != 0) {
					gl.glRotatef(model.getRot_z(), 0f, 0f, 1f);
				}

				// Tekenen
				if (model.getColor() != 0) {
					float[] rgb = Util.hexToRGB(model.getColor());
					gl.glColor4f(rgb[0], rgb[1], rgb[2], 0.4f);
				}

				// Blenderen
				if (model.isBlended() == 0) {
					gl.glEnable(GL10.GL_BLEND);
					gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
				}

				model.getModel().draw(gl);
				gl.glPopMatrix();
				gl.glColor4f(1f, 1f, 1f, 1f); // Kleur resetten

			}
		}
	}

	/**
	 * visual helper method to help debugging touch area's
	 * 
	 * @param id
	 * @param box
	 */
	public void drawBB(String id, TextBox box) {
		boolean create = images.isEmpty();

		Square tmp = new Square(paint, (box.getLoc().x - box.getBlockW() / 2),
				mHeight - (box.getLoc().y + box.getBlockH() / 2),
				box.getBlockW(), box.getBlockH());
		tmp.setIdentifier(id);

		for (Square s : images) {
			if (s.equals(tmp)) {
				s.update(tmp); // Updating will stop bitmaps from flickering and
								// increases performance
				create = false;
				break;
			} else {
				create = true;
			}
		}

		if (create) {
			images.add(tmp);
		}
	}

	/**
	 * Adds the specified bitmap to a buffer which will be drawed in the next
	 * loop.
	 * 
	 * @param id
	 *            Unique id generated by Mixare, this is used to update bitmaps
	 * @param img
	 *            The bitmap which should be drawed.
	 * @param x
	 *            X coordinate
	 * @param y
	 *            Y coordinate
	 */
	public void paintBitmapGL(String id, Bitmap img, float x, float y,
			float rotation) {
		boolean create = images.isEmpty();

		Square tmp = new Square(id, img, paint, x, (mHeight - y),
				img.getWidth(), img.getHeight());

		if (rotation != 0) {
			tmp.setRotation(rotation);
		}

		for (Square s : images) {
			if (s.equals(tmp)) {
				s.update(tmp); // Updating will stop bitmaps from flickering and
								// increases performance
				create = false;
				break;
			} else {
				create = true;
			}
		}
		if (create) {
			images.add(tmp);
		}
	}

	/**
	 * Text drawing with opengl.
	 * 
	 * @see GLText
	 * @param tekst
	 *            The string which you want to draw
	 * @param location
	 *            Where you want to draw it
	 */
	public void paintText3D(String tekst, String url, PointF location,
			float rotation) {
		boolean create = text3d.isEmpty();

		TextBox box = new TextBox(tekst, url, location, rotation);

		for (TextBox t : text3d) {
			if (t.equals(box)) {
				t.update(box);

				create = false;
				break;
			} else {
				create = true;
			}
		}

		if (create) {
			text3d.add(box);

		}
	}

	/**
	 * Puts the model in a set ready for drawing. Also makes sure that a model
	 * gets only loads once.
	 * 
	 * @param model
	 * @throws ModelLoadException
	 *             Something went wrong with loading the model, see stracktrace
	 */
	public void paint3DModel(Model3D model) throws ModelLoadException {
		Iterator<Entry<String, Model3D>> it = models.entrySet().iterator();

		boolean create = models.isEmpty();

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
			Mesh tmp = null;
			try {
				if (model.getObj().endsWith("poi")) {
					tmp = poi;
				} else if (model.getObj().endsWith("triangle")) {
					tmp = triangle;
				} else if (model.getObj().endsWith("path")) {
					tmp = arrow;
				} else {
					InputStream input = new FileInputStream(model.getObj());
					if (input != null) {
						if (model.getObj().endsWith(".txt")) { // TODO: fix file
																// store
																// (betelgeuse)
							tmp = new OffReader((Context) app).readMesh(input);
						}
						if (model.getObj().endsWith(".off")) {
							tmp = new OffReader((Context) app).readMesh(input);
						}
						if (model.getObj().endsWith(".obj")) {
							tmp = new ObjReader((Context) app).readMesh(input);
						}
					}
				}
				if (tmp != null) {
					model.setModel(tmp);
				}
			} catch (IOException ioe) {
				ModelLoadException mle = new ModelLoadException(ioe);
				mle.setPath(model.getObj());
				throw mle;
			} catch (ModelLoadException mle) {
				mle.setPath(model.getObj());
				throw mle;
			}

			models.put(model.getObj(), model);
		}

	}

	private void clearBuffers() {
		canvasMap.eraseColor(0);
		// models.clear(); // Solves the ghost markers, it has no impact on
		// performance because GC is blocking anyway. You want
		// to disable this if the blocks are gone
	}

	@SuppressLint("NewApi")
	public void onDrawFrame(GL10 gl) {
		long time1 = System.currentTimeMillis();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		clearBuffers();
		try {

			ready2D(gl, GLParameters.WIDTH, GLParameters.HEIGHT);
			draw2D(gl);

			gl.glPushMatrix();

			if (GLParameters.ENABLE3D) {
				ready3D(gl, GLParameters.WIDTH, GLParameters.HEIGHT);
				draw3D(gl);
			}

			gl.glPopMatrix();
			dt = System.currentTimeMillis() - time1;
		} catch (Object3DException e) {

		}
	}

	/**
	 * Screen rotation, will not happen in Mixare
	 */
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.i("Mixare", "onSurfaceChanged SOMETHING HAPPEND!!!");
		Log.i(TAG, "" + Thread.currentThread().getId());
		clearBuffers();

		// Hack for losing EGL context. It would be nicer to preserve the EGL
		// context but hacking GLSurfaceView will cause bugs on different
		// devices
		text3d.clear();
		images.clear();

		mWidth = width;
		mHeight = height;

		gl.glViewport(0, 0, width, height);
		gl.glLoadIdentity();

		// Map matrix to screen, bottom-left is origin.
		gl.glOrthof(0, width, 0, height, -1, 1);

	}

	/**
	 * Sets up GLText and checks for openGL extensions.
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.i(TAG, "onSurfaceCreated");
		dt = 1;

		text = new GLText(gl, ((Context) app).getAssets());
		textInfo = new GLText(gl, ((Context) app).getAssets());

		textInfo.load("Roboto-Regular.ttf", 14, 2, 2);
		text.load("Roboto-Regular.ttf", 30, 2, 2);

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

	public Set<TextBox> getBoundingBoxes() {
		return text3d;
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

	public void paintCircle(String id, float x, float y, float radius) {
		if (id.equalsIgnoreCase("radar")) {
			canvas.drawCircle(x, y, radius, paint);
		} else {
			try {
				Model3D circleModel = new Model3D();

				circleModel.setDistance(90);
				circleModel.setSchaal(20);
				circleModel.setObj(id);
				circleModel.setColor(0xFF0000);
				circleModel.setxPos(x);
				circleModel.setyPos(y);
				paint3DModel(circleModel);
			} catch (ModelLoadException e) {
				e.printStackTrace();
			}
		}
	}

	public void paintTriangle(String id, float x, float y, float radius) {
		try {
			Model3D triangleModel = new Model3D();

			triangleModel.setDistance(90);
			triangleModel.setSchaal(20);
			triangleModel.setObj(id);
			triangleModel.setColor(0x0000FF);
			triangleModel.setxPos(x);
			triangleModel.setyPos(y);
			paint3DModel(triangleModel);
		} catch (ModelLoadException e) {
			e.printStackTrace();
		}

	}

	public void paintPath(String id, Path path, float x, float y, float width,
			float height, float rotation, float scale) {
		try {
			Model3D arrowModel = new Model3D();

			arrowModel.setDistance(90);
			arrowModel.setSchaal(20);
			arrowModel.setObj(id);
			arrowModel.setColor(0xFFFF00);
			arrowModel.setRot_z((360 - rotation));
			arrowModel.setxPos(x);
			arrowModel.setyPos(y);
			paint3DModel(arrowModel);
		} catch (ModelLoadException e) {
			e.printStackTrace();
		}
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
