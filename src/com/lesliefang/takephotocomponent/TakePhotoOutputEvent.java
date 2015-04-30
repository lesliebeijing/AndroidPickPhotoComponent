package com.lesliefang.takephotocomponent;


/**
 * 拍照输出参数
 */
public class TakePhotoOutputEvent {
	public int requestCode;
	public int resultCode;
	public String filePath;

	public TakePhotoOutputEvent(int requestCode, int resultCode, String filePath) {
		this.requestCode = requestCode;
		this.resultCode = resultCode;
		this.filePath = filePath;
	}
}
