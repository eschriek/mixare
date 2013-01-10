package org.mixare;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL;

import org.mixare.lib.DataViewInterface;
import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.gui.GLParameters;
import org.mixare.lib.gui.MatrixTrackingGL;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.model3d.text.TextBox;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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
	private DataViewInterface data;
	private MixContextInterface mxContext;

	@TargetApi(13)
	public Surface3D(Context context, DataViewInterface data,
			MixStateInterface state, MixContextInterface mxContext) {
		super(context);

		this.mxContext = mxContext;
		this.context = context;
		this.state = state;
		this.data = data;
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
		setEGLConfigChooser(8, 8, 8, 8, 16, 0); // ARGB 8888 ,D 16
		getHolder().setFormat(PixelFormat.TRANSLUCENT);

		setRenderer(MixView.getdWindow());
		
		//spawnThread();
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		float x = e.getX();
		float y = e.getY();
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			List<TextBox> results = new ArrayList<TextBox>();
			for (TextBox t : screen.getBoundingBoxes()) {
				if (t.isTouchInside(x, y)) {
					results.add(t);

				}
			}

			if (results.size() == 1) {
				state.handleEvent(mxContext, results.get(0).getUrl());
			} else if (results.size() > 1) {
				List<String> strings = new ArrayList<String>();
				List<String> links = new ArrayList<String>();
				for (TextBox t : results) {
					strings.add(t.getTekst());
					links.add(t.getUrl());
				}

				final CharSequence[] items = strings.toArray(new String[strings
						.size()]);
				final CharSequence[] urls = links.toArray(new String[links
						.size()]);

				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Make your selection");
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						state.handleEvent(mxContext, urls[item].toString());
						dialog.dismiss();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
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