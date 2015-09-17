package com.happy.mobileproject.domain;

import java.io.Serializable;

/**
 * 代码一个视频和音频的信息
 *
 */
public class MediaItem implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String title;
	
	private long duration;
	
	private long size;
	
	private String data;
	
	private String artist;
	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "VideoItem [title=" + title + ", duration=" + duration
				+ ", size=" + size + ", data=" + data + "]";
	}
	
	
	
	

}
