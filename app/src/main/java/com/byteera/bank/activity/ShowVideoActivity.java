package com.byteera.bank.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.byteera.bank.utils.LogUtil;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.byteera.R;
import com.byteera.hxlib.utils.ActivityUtil;
import com.easemob.chat.EMChatConfig;
import com.easemob.chat.EMChatManager;
import com.easemob.cloud.CloudOperationCallback;
import com.easemob.cloud.HttpFileManager;
import com.easemob.util.PathUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 展示视频内容
 * 
 * @author Administrator
 * 
 */
public class ShowVideoActivity extends BaseActivity implements OnTouchListener {

	private RelativeLayout loadingLayout;
	private ProgressBar progressBar;
	private String localFilePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.showvideo_activity);
		loadingLayout = (RelativeLayout) findViewById(R.id.loading_layout);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		localFilePath = getIntent().getStringExtra("localpath");
		String remotepath = getIntent().getStringExtra("remotepath");
		String secret = getIntent().getStringExtra("secret");
		System.err.println("show video view file:" + localFilePath
				+ " remotepath:" + remotepath + " secret:" + secret);
		if (localFilePath != null && new File(localFilePath).exists()) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(localFilePath)),
					"video/mp4");
			startActivity(intent);
			ActivityUtil.finishActivity(baseContext);
		} else if (!TextUtils.isEmpty(remotepath) && !remotepath.equals("null")) {
			System.err.println("download remote video file");
			Map<String, String> maps = new HashMap<String, String>();
			maps.put("Authorization", "Bearer "
					+ EMChatManager.getInstance().getAccessToken());
			if (!TextUtils.isEmpty(secret)) {
				maps.put("share-secret", secret);
			}
			maps.put("Accept", "application/octet-stream");
			downloadVideo(remotepath, maps);
		} else {

		}

	}

	/**
	 * 下载视频文件
	 */
	private void downloadVideo(final String remoteUrl,
			final Map<String, String> header) {

		if (TextUtils.isEmpty(localFilePath)) {
			localFilePath = PathUtil.getInstance().getVideoPath()
					.getAbsolutePath()
					+ "/" + remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1)+".mp4";
		}

		if (new File(localFilePath).exists()) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(localFilePath)),
					"video/mp4");
			startActivity(intent);
			ActivityUtil.finishActivity(baseContext);
			return;
		}

		System.err.println("download view file ...");
		loadingLayout.setVisibility(View.VISIBLE);

		final HttpFileManager httpFileMgr = new HttpFileManager(this,
				EMChatConfig.getInstance().getStorageUrl());
		final CloudOperationCallback callback = new CloudOperationCallback() {

			@Override
			public void onSuccess(String result) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						loadingLayout.setVisibility(View.GONE);
						progressBar.setProgress(0);
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(
								Uri.fromFile(new File(localFilePath)),
								"video/mp4");
						startActivity(intent);
						ActivityUtil.finishActivity(baseContext);
					}
				});

			}

			@Override
			public void onProgress(final int progress) {
				LogUtil.d("ease", "video progress:" + progress);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						progressBar.setProgress(progress);

					}
				});

			}

			@Override
			public void onError(String msg) {
				LogUtil.e("###", "offline file transfer error:" + msg);
				File file = new File(localFilePath);
				if (file.exists()) {
					file.delete();
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

					}
				});

			}
		};

		new Thread(new Runnable() {

			@Override
			public void run() {
				httpFileMgr.downloadFile(remoteUrl, localFilePath,
						EMChatConfig.getInstance().APPKEY, header, callback);
			}
		}).start();

	}

	@Override
	public void onBackPressed() {
		ActivityUtil.finishActivity(baseContext);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

}
