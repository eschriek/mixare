package org.mixare.lib.gui;

import org.mixare.lib.model3d.TDModel;

import android.os.Parcel;
import android.os.Parcelable;

public class Model3D implements Parcelable {

	private String obj;
	private float rot_x, rot_y, rot_z;
	private float xPos, yPos;
	private float schaal;
	private int blended;
	private double distance;
	private double bearing;
	private TDModel model;

	public static final Parcelable.Creator<Model3D> CREATOR = new Parcelable.Creator<Model3D>() {
		public Model3D createFromParcel(Parcel in) {
			return new Model3D(in);
		}

		public Model3D[] newArray(int size) {
			return new Model3D[size];
		}
	};

	@Override
	public int hashCode() {
		return obj.hashCode() + ((Float) xPos).hashCode()
				+ ((Float) yPos).hashCode() + ((Double) distance).hashCode()
				+ Model3D.class.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this.hashCode() == o.hashCode()) {
			return true;
		}
		return super.equals(o);
	}

	// Update everything but the model
	public void update(Model3D object) {
		this.bearing = object.getBearing();
		this.distance = object.getDistance();
		this.xPos = object.getxPos();
		this.yPos = object.getyPos();
		this.rot_x = object.getRot_x();
		this.rot_y = object.getRot_y();
		this.rot_z = object.getRot_z();
		this.blended = object.isBlended();
	}

	public TDModel getModel() {
		return model;
	}

	public void setModel(TDModel model) {
		this.model = model;
	}

	public Model3D() {

	}

	public Model3D(Parcel in) {
		readParcel(in);
	}

	public String getObj() {
		return obj;
	}

	public void setObj(String obj) {
		this.obj = obj;
	}

	public float getRot_x() {
		return rot_x;
	}

	public void setRot_x(float rot_x) {
		this.rot_x = rot_x;
	}

	public float getRot_y() {
		return rot_y;
	}

	public float getxPos() {
		return xPos;
	}

	public void setxPos(float xPos) {
		this.xPos = xPos;
	}

	public float getyPos() {
		return yPos;
	}

	public void setyPos(float yPos) {
		this.yPos = yPos;
	}

	public void setRot_y(float rot_y) {
		this.rot_y = rot_y;
	}

	public float getRot_z() {
		return rot_z;
	}

	public void setRot_z(float rot_z) {
		this.rot_z = rot_z;
	}

	public int isBlended() {
		return blended;
	}

	public void setBlended(int blended) {
		this.blended = blended;
	}

	public float getSchaal() {
		return schaal;
	}

	public void setSchaal(float schaal) {
		this.schaal = schaal;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(obj);
		dest.writeFloat(rot_x);
		dest.writeFloat(rot_y);
		dest.writeFloat(rot_z);
		dest.writeFloat(xPos);
		dest.writeFloat(yPos);
		dest.writeFloat(schaal);
		dest.writeInt(blended);
		dest.writeDouble(distance);
		dest.writeDouble(bearing);
	}

	public void readParcel(Parcel in) {
		obj = in.readString();
		rot_x = in.readFloat();
		rot_y = in.readFloat();
		rot_z = in.readFloat();
		xPos = in.readFloat();
		yPos = in.readFloat();
		schaal = in.readFloat();
		blended = in.readInt();
		distance = in.readDouble();
		bearing = in.readDouble();
	}

}
