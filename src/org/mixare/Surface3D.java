package org.mixare;

import javax.microedition.khronos.opengles.GL;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.gui.DataViewInterface;
import org.mixare.lib.gui.GLParameters;
import org.mixare.lib.gui.MatrixTrackingGL;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.model3d.text.TextBox;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class Surface3D extends GLSurfaceView {

	private PaintScreen screen;
	private Context context;
	private MixStateInterface state;
	private MixContextInterface mxContext;

	@TargetApi(13)
	public Surface3D(Context context, DataViewInterface data,
			MixStateInterface state, MixContextInterface mxContext) {
		super(context);

		this.mxContext = mxContext;
		this.context = context;
		this.state = state;
		this.setGLWrapper(new GLWrapper() {

			@Override
			public GL wrap(GL gl) {
				return new MatrixTrackingGL(gl);
			}
		});

		Display display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point p = new Point();
		display.getSize(p);

		GLParameters.WIDTH = p.x;
		GLParameters.HEIGHT = p.y;

		screen = new PaintScreen(context, data);
		MixView.setdWindow(screen);

		// setDebugFlags(DEBUG_LOG_GL_CALLS);
		setPreserveEGLContextOnPause(true); // werkt niet
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);

		setRenderer(MixView.getdWindow());
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		float x = e.getX();
		float y = e.getY();
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			for (TextBox t : screen.getBoundingBoxes()) {
				if (t.isTouchInside(x, y)) {
					state.handleEvent(mxContext, t.getUrl());
				}
			}
		}
		return true;
	}
	
	@Override
	public void onPause() {
		Log.i("Mixare", "3D Pauze");
	}

	@Override
	public void onResume() {
		Log.i("Mixare", "3D Resume");
	}

}