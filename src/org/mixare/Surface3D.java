package org.mixare;

import org.mixare.MixView;
import org.mixare.lib.gui.DataViewInterface;
import org.mixare.lib.gui.GLParameters;
import org.mixare.lib.gui.PaintScreen;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.view.Display;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class Surface3D extends GLSurfaceView {

	PaintScreen screen;
	
	@TargetApi(13)
	public Surface3D(Context context, DataViewInterface data) {
		super(context);

		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		Point p = new Point();
		display.getSize(p);
		
		GLParameters.WIDTH = p.x;
		GLParameters.HEIGHT = p.y;
		
		screen = new PaintScreen(context, data);
		MixView.setdWindow(screen);
		
		//setDebugFlags(DEBUG_LOG_GL_CALLS);
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		
		setRenderer(MixView.getdWindow());			
		//setRenderMode(RENDERMODE_WHEN_DIRTY);
		//screen.setCanvas(c);		
	}
	
}