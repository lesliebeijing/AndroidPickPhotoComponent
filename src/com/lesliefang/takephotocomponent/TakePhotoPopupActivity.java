package com.lesliefang.takephotocomponent;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import de.greenrobot.event.EventBus;

/**
 * <pre>
 * 拍照及从相册选择弹出 activity 
 * 成功后会发送 TakePhotoOutputEvent 事件，返回图片路径
 * </pre>
 */
public class TakePhotoPopupActivity extends Activity {
	public static final int REQUEST_CODE_CAMERA = 110;
	public static final int REQUEST_CODE_ALBUM = 111;
	public static final int REQUEST_CODE_CROP = 112;

	@ViewInject(R.id.takephoto_popup_layout)
	private RelativeLayout outContainer;
	@ViewInject(R.id.take_photo)
	private TextView takePhoto;
	@ViewInject(R.id.select_from_album)
	private TextView openAlbum;
	@ViewInject(R.id.cancel_photo)
	private TextView cancel;

	private String photoFileSavePath;
	private String croppedFileSavePath;
	private boolean isCrop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_takephoto_popup);
		ViewUtils.inject(this);

		Intent intent = getIntent();
		if (intent != null) {
			photoFileSavePath = intent.getStringExtra("photoFileSavePath");
			croppedFileSavePath = intent.getStringExtra("croppedFileSavePath");
			isCrop = intent.getBooleanExtra("isCrop", false);
		}
	}

	@OnClick({ R.id.take_photo, R.id.select_from_album, R.id.cancel_photo, R.id.takephoto_popup_layout })
	public void buttonOnclick(View v) {
		switch (v.getId()) {
		case R.id.take_photo:
			openCamera(photoFileSavePath);
			break;
		case R.id.select_from_album:
			openAlbum();
			break;
		case R.id.cancel_photo:
			finish();
			break;
		case R.id.takephoto_popup_layout:
			finish();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		try {
			if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) { // 相机
				if (isCrop) {
					// 裁剪
					cropImage(photoFileSavePath, croppedFileSavePath);
				} else {
					// 不裁剪
					EventBus.getDefault().post(new TakePhotoOutputEvent(requestCode, resultCode, photoFileSavePath));
					finish();
				}
			} else if (requestCode == REQUEST_CODE_ALBUM && resultCode == Activity.RESULT_OK) { // 相册
				try {
					// 得到图片路径
					Uri selectedImage = data.getData();
					String[] filePathColumn = { MediaStore.Images.Media.DATA };
					Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
					cursor.moveToFirst();
					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					String picPath = cursor.getString(columnIndex);
					cursor.close();

					if (isCrop) {
						cropImage(picPath, croppedFileSavePath);
					} else {
						EventBus.getDefault().post(new TakePhotoOutputEvent(requestCode, resultCode, picPath));
						finish();
					}
				} catch (Exception e) {

				}
			} else if (requestCode == REQUEST_CODE_CROP && resultCode == Activity.RESULT_OK) { // 裁剪回来
				EventBus.getDefault().post(new TakePhotoOutputEvent(requestCode, resultCode, croppedFileSavePath));
				finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openAlbum() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
		startActivityForResult(intent, REQUEST_CODE_ALBUM);
	}

	/**
	 * 裁剪相册照片
	 * 
	 */
	public void cropImage(String originFilePath, String croppedFilePath) {
		File originFile = new File(originFilePath);
		if (originFile == null || !originFile.exists()) {
			return;
		}

		try {
			if (croppedFilePath == null) {
				croppedFilePath = getExternalFilesDir(null) + "/" + "tmpcropped.png";
				this.croppedFileSavePath = croppedFilePath;
			}

			Uri originUri = Uri.fromFile(new File(originFilePath));
			Uri croppedFileUri = Uri.fromFile(new File(croppedFilePath));
			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(originUri, "image/*");
			intent.putExtra("crop", true);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 120);
			intent.putExtra("outputY", 120);
			intent.putExtra("return-data", true);
			intent.putExtra("output", croppedFileUri);
			startActivityForResult(intent, REQUEST_CODE_CROP);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openCamera(String filePath) {
		try {
			if (filePath == null) {
				filePath = getExternalFilesDir(null) + "/" + "tmpcamera.png";
				this.photoFileSavePath = filePath;
			}

			Uri uri = Uri.fromFile(new File(filePath));
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			startActivityForResult(intent, REQUEST_CODE_CAMERA);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
