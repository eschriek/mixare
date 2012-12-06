package org.mixare.lib.model3d.parsers;

import java.io.InputStream;

import org.mixare.lib.model3d.Mesh;
import org.mixare.lib.model3d.ModelLoadException;

import android.content.Context;

public abstract class ModelReader {
	public static final int BUFFER_SIZE = 8192;
	
	protected Context context;
	
	public ModelReader(Context context) {
		this.context = context;
	}
	
	public abstract Mesh readMesh(InputStream is) throws ModelLoadException;
}
