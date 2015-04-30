package com.lesliefang.takephotocomponent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import de.greenrobot.event.EventBus;

public class MainActivity extends Activity {
	private Button btn;
	private ImageView img;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btn = (Button) findViewById(R.id.btn_takephoto);
		img = (ImageView) findViewById(R.id.img);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				/**
				 * 1 不裁剪，默认存储路径
				 */
				startTakePhotoActivity(null, null, false);

				/**
				 * 2 裁剪，默认存储路径
				 */
				// startTakePhotoActivity(null, null, true);

				/**
				 * 3 裁剪，指定存储路径
				 */
			/*	String photoSavePath = getExternalFilesDir(null).getAbsoluteFile() + "/tmpcamera.png";
				String cropFileSavePath = getExternalFilesDir(null).getAbsolutePath() + "/tmpcrop.png";
				startTakePhotoActivity(photoSavePath, cropFileSavePath, true); */
			}
		});
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	/**
	 * 调起拍照和从相册选择界面
	 * 
	 * @param photoFileSavePath
	 *            拍照保存文件路径,若为 null 图片会存储在默认路径
	 * @param croppedFileSavePath
	 *            裁剪图片保存文件路径，若为 null 图片会存储在默认路径
	 * @param isCrop
	 *            是否需要裁剪
	 */
	public void startTakePhotoActivity(String photoFileSavePath, String croppedFileSavePath, boolean isCrop) {
		Intent intent = new Intent(this, TakePhotoPopupActivity.class);
		intent.putExtra("photoFileSavePath", photoFileSavePath);
		intent.putExtra("croppedFileSavePath", croppedFileSavePath);
		intent.putExtra("isCrop", isCrop);
		startActivity(intent);
	}

	/**
	 * 拍照或从相册选择成功回调事件
	 * 
	 * @param event
	 */
	public void onEvent(TakePhotoOutputEvent event) {
		if (event != null && !TextUtils.isEmpty(event.filePath)) {
			Bitmap bitmap = decodeSampledBitmapFromResource(event.filePath, 300);
			img.setImageBitmap(bitmap);
		}
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
		// 源图片的宽度
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width > reqWidth) {
			// 计算出实际宽度和目标宽度的比率
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = widthRatio;
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}
}
