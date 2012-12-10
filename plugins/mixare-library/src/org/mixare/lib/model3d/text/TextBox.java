package org.mixare.lib.model3d.text;

import android.graphics.PointF;

public class TextBox {

	private String tekst;
	private PointF loc;
	
	public TextBox(String tekst, PointF loc) {
		this.tekst = tekst;
		this.loc = loc;
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
