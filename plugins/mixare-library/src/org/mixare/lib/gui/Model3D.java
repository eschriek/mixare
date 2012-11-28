package org.mixare.lib.gui;

import android.os.Parcel;
import android.os.Parcelable;

public class Model3D implements Parcelable {

	private String obj;
	private float rot_x, rot_y, rot_z;
	private int blended;

	public static final Parcelable.Creator<Model3D> CREATOR = new Parcelable.Creator<Model3D>() {
		public Model3D createFromParcel(Parcel in) {
			return new Model3D(in);
		}

		public Model3D[] newArray(int size) {
			return new Model3D[size];
		}
	};

	public Model3D() {
		
	}
	
	public Model3D(Parcel in){
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
		dest.writeInt(blended);
	}

	public void readParcel(Parcel in){
		obj = in.readString();
		rot_x = in.readFloat();
		rot_y = in.readFloat();
		rot_z = in.readFloat();
		blended = in.readInt();
	}
	
}
