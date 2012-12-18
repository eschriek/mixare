package org.mixare.lib.model3d.text;

import android.graphics.PointF;

public class TextBox {

	private String tekst;
	private String url;
	private PointF loc;
	private float rotation;
	private int blockW;
	private int blockH;

	public TextBox(String tekst, String url, PointF loc, float rotation) {
		this.tekst = tekst;
		this.url = url;
		this.loc = loc;
		this.rotation = rotation;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getBlockW() {
		return blockW;
	}

	public void setBlockW(int blockW) {
		this.blockW = blockW;
	}

	public int getBlockH() {
		return blockH;
	}

	public void setBlockH(int blockH) {
		this.blockH = blockH;
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

	@Override
	public boolean equals(Object o) {
		if (((TextBox) o).getTekst().equalsIgnoreCase(this.getTekst())) {
			return true;
		}
		return false;
	}

	public void update(Object o) {
		TextBox t = ((TextBox) o);

		this.setLoc(t.getLoc());
		this.setRotation(t.getRotation());

	}

	public boolean isTouchInside(float x, float y) {
		if (x > loc.x && y > loc.y && x < (loc.x + blockW)
				&& y < (loc.y + blockH)) {
			return true;
		}
		return false;
	}

}
