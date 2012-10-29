package org.mixare;

import junit.framework.Assert;

import org.mixare.lib.gui.PaintScreen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;
import android.view.View;

public class AugmentedView extends GLSurfaceView {
	MixView app;
	int xSearch = 200;
	int ySearch = 10;
	int searchObjWidth = 0;
	int searchObjHeight = 0;
	private int width;
	private int height;

	Paint zoomPaint = new Paint();

	public AugmentedView(Context context) {
		super(context);

		try {
			//MixView.getdWindow().setCanvas(new Canvas());

			setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			getHolder().setFormat(PixelFormat.TRANSLUCENT);

			Assert.assertNotNull(MixView.getdWindow().getCanvas());
			
			setRenderer(MixView.getdWindow());
			//setRenderMode(RENDERMODE_CONTINUOUSLY);
			
			app = (MixView) context;

			app.killOnError();
		} catch (Exception ex) {
			app.doError(ex, app.GENERAL_ERROR);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		width = h;
		height = h;
	}

	public void draw() {
		MixView.getDataView().draw(MixView.getdWindow());
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		super.surfaceDestroyed(holder);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			app.killOnError();

			MixView.getdWindow().setWidth(width);
			MixView.getdWindow().setHeight(height);

			// MixView.getdWindow().setCanvas(this.canvas);

			if (!MixView.getDataView().isInited()) {
				MixView.getDataView().init(width, height);
			}
			if (app.isZoombarVisible()) {
				zoomPaint.setColor(Color.WHITE);
				zoomPaint.setTextSize(14);
				String startKM, endKM;
				endKM = "80km";
				startKM = "0km";
				/*
				 * if(MixListView.getDataSource().equals("Twitter")){ startKM =
				 * "1km"; }
				 */
				MixView.getdWindow().paintText(canvas.getWidth() / 100 * 4,
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

			MixView.getDataView().draw(MixView.getdWindow());
		} catch (Exception ex) {
			app.doError(ex, app.GENERAL_ERROR);
		}
	}
}