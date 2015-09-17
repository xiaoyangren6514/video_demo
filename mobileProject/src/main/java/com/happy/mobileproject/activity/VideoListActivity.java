package com.happy.mobileproject.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.happy.mobileproject.R;
import com.happy.mobileproject.domain.MediaItem;
import com.happy.mobileproject.utils.Utils;

import java.util.ArrayList;


public class VideoListActivity extends BaseActivity {
	
	private ListView list_video;
	private TextView tv_novideo;
	
	private ArrayList<MediaItem> videoItems;
	
	private Utils utils;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			if(videoItems.size()>0){
				//有视频
				tv_novideo.setVisibility(View.GONE);
				//设置适配器
				VideoListAdapter adapter = new VideoListAdapter();
				list_video.setAdapter(adapter);
			}else{
				//没有找到视频
				tv_novideo.setVisibility(View.VISIBLE);
			}

		};
	};


	class VideoListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return videoItems.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			if(convertView != null){
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}else{
				//把布局文件---》View对象
				view = View.inflate(VideoListActivity.this, R.layout.videolist_item, null);
				holder = new ViewHolder();
				holder.tv_title = (TextView) view.findViewById(R.id.tv_title);
				holder.tv_duration = (TextView) view.findViewById(R.id.tv_duration);
				holder.tv_size = (TextView) view.findViewById(R.id.tv_size);
				//View和容器关联起来
				view.setTag(holder);
			}

			//根据位置得到具体的数据
			MediaItem item = videoItems.get(position);
			holder.tv_title.setText(item.getTitle());
			holder.tv_duration.setText(utils.stringForTime((int)item.getDuration()));
			holder.tv_size.setText(Formatter.formatFileSize(VideoListActivity.this, item.getSize()));
			return view;
		}

	}

	static class ViewHolder{
		TextView tv_title;
		TextView tv_duration;
		TextView tv_size;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("本地视频");
		setRightButton(View.GONE);//隐藏右边按钮
		utils = new Utils();
		initView();
		//加载本地视频，有可能视频很多，在子线中加载视频
		getVideoData();

	}

	/**
	 * 加载视频的数据
	 */
	private void getVideoData() {
		new Thread(){
			public void run() {
				ContentResolver resolver = getContentResolver();
				//读取视频的uri
				Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				String[] projection = {
						MediaStore.Video.Media.TITLE,//视频名称
						MediaStore.Video.Media.DURATION,//视频的长度
						MediaStore.Video.Media.SIZE,//视频的大小
						MediaStore.Video.Media.DATA//视频的播放地址
				};
				Cursor cursor = resolver.query(uri, projection , null, null, null);
				videoItems = new ArrayList<MediaItem>();
				while(cursor.moveToNext()){

					long size = cursor.getLong(2);
					//屏幕小3MB的小视频
					if(size > 3*1024*1024){
						//一个视频信息
						MediaItem item = new MediaItem();

						String title = cursor.getString(0);
						item.setTitle(title);
						long duration = cursor.getLong(1);
						item.setDuration(duration);

						item.setSize(size);
						String data = cursor.getString(3);
						item.setData(data);

						//添加视频
						videoItems.add(item);
					}

				}
				cursor.close();
				handler.sendEmptyMessage(0);

			};
		}.start();

	}

	/**
	 * 初始化View
	 */
	private void initView() {
		list_video = (ListView) findViewById(R.id.list_video);
		tv_novideo = (TextView) findViewById(R.id.tv_novideo);
		list_video.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				//传入单个播放地址
//				VideoItem item = videoItems.get(position);
////				Toast.makeText(getApplicationContext(), "播放地址是："+item.getData(), 1).show();
//				Intent intent = new Intent(VideoListActivity.this,VideoPlayerActivity.class);
//				intent.setData(Uri.parse(item.getData()));//把播放地址传给视频播放器
//				startActivity(intent);

				//当点击的时候传播放列表和当前点击的位置
				Intent intent = new Intent(VideoListActivity.this,VideoPlayerActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("videolist", videoItems);//视频列表
				intent.putExtras(bundle);
				intent.putExtra("position", position);

				startActivity(intent);


			}
		});
	}

	@Override
	public void rightButtonClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void leftButtonClick() {
		// TODO Auto-generated method stub
		//退出本地视频
		finish();

	}

	@Override
	public View setContentView() {
		return View.inflate(this, R.layout.activity_video_list, null);
	}

}
