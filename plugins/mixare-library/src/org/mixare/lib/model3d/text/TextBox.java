package org.mixare.lib.model3d.text;

import android.graphics.PointF;

public class TextBox {

	private String tekst;
	private PointF loc;
	private float rotation;

	public TextBox(String tekst, PointF loc, float rotation) {
		this.tekst = tekst;
		this.loc = loc;
		this.rotation = rotation;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public String getTekst() {
		return tekst;
	}

	public void setTekst(String tekst) {
		this.tekst = tekst;
	}

	public PointF getLoc() {
		return loc;
	}

	public void setLoc(PointF loc) {
		this.loc = loc;
	}

}
