package com.cms.player;

import android.content.Context;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.C.ContentType;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.ExoplayerLogEx;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.zte.ZteIptvMediaInfo;
import com.google.android.exoplayer2.source.zte.ZteMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.zte.ZteDataSourceFactory;
import com.google.android.exoplayer2.upstream.zte.ZteLibVodJni;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;

//封装了ExoPlayer，兼容MediaPlayer调用的播放器
public class ZTEExoPlayer extends MediaPlayer implements
		ExoPlayer.EventListener, VideoRendererEventListener {
	private static final String TAG = "TAGV_ZTEExoPlayer";
	private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    public static final int MEDIA_TYPE_UNKNOWN = 0;
    public static final int MEDIA_TYPE_CHANNEL = 1;
    public static final int MEDIA_TYPE_VOD = 2;
    public static final int MEDIA_TYPE_TVOD = 3;
    public static final int MEDIA_TYPE_OTT_STATIC = 4;
    public static final int MEDIA_TYPE_OTT_DYNAMIC = 5;
	
	private SimpleExoPlayer myPlayer;
	private Context context;
	private DataSource.Factory mediaDataSourceFactory;
	private DefaultTrackSelector trackSelector;
	//private DefaultTrackSelector.Parameters trackSelectorParameters;
	private Handler mainHandler = new Handler();
	private MediaSource mediaSource;
	private SurfaceHolder surfaceHolder;
	private Surface surface;

	// 是否设置自动播放
	private boolean isAutoPlaying;
	private boolean isMediaReady;

	private static ZteLibVodJni zteLibVodJni;

	// 轨道信息
	private ArrayList<MyTrack> listMediaTracks;

	// 播放器参数
	private int iVideoWidth;
	private int iVideoHeight;
	private boolean isBuffering;// 用于上报701/702事件

	private ZTEExoPlayer(Context context) {
		Log.i(TAG, "Create ZTEExoPlayer: " + context);

		this.context = context;
		this.mediaDataSourceFactory = buildDataSourceFactory(true);
		if(null == zteLibVodJni) zteLibVodJni = new ZteLibVodJni();
	}

	// 创建EXOPlayer需要Context对象
	public static ZTEExoPlayer getPlayer(Context context) {
		return new ZTEExoPlayer(context);
	}

	// 重置播放器参数，每次setDataSource会重新创建播放器
	private void resetPlayerParames() {
		iVideoWidth = 0;
		iVideoHeight = 0;
		isBuffering = false;
		listMediaTracks = null;
		isMediaReady = false;
	}

	@Override
	public void setDataSource(String path) throws IOException,
			IllegalArgumentException, SecurityException, IllegalStateException {
		if (null != myPlayer) {
			Log.i(TAG, "Release ExoPlayer Before Next Play");
			myPlayer.release();
		}
		resetPlayerParames();

		if (null == path) {
			Log.e(TAG, "setDataSource NULL");
			return;
		}

		// 1.创建一个默认TrackSelector
		TrackSelection.Factory adaptiveTrackSelectionFactory =
		          new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
		trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
		
		// 2.创建播放器
		DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context,
		          null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
		myPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);//DRM NULL

		// 3.创建DataSource
		Uri uri = Uri.parse(path);
		mediaSource = buildMediaSource(uri);
		Log.i(TAG, "setDataSource: " + path);

		myPlayer.addListener(this);
		myPlayer.setVideoDebugListener(this);
		myPlayer.setPlayWhenReady(true);

		if (null != surfaceHolder) {
			myPlayer.setVideoSurfaceHolder(surfaceHolder);
		} else if (null != surface) {
			myPlayer.setVideoSurface(surface);
		}
	}

	private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
		HttpDataSource.Factory httpFactory = new DefaultHttpDataSourceFactory(
				Util.getUserAgent(context, "ExoPlayerDemo"), null);
		return new DefaultDataSourceFactory(context, null, httpFactory);
	}

	private MediaSource buildMediaSource(Uri uri) {
		com.google.android.exoplayer2.source.zte.MediaInfo mediaInfo = new com.google.android.exoplayer2.source.zte.MediaInfo();
		mediaInfo.uri = uri;

		if (isIPTVUrl(mediaInfo.uri.toString())) {
			Log.i(TAG, "USE ZTE ExoPlayer");
			ZteIptvMediaInfo zteIptvMediaInfo = new ZteIptvMediaInfo();
			zteIptvMediaInfo.exoMediaInfo.mediaUrl = new String(
					mediaInfo.uri.toString());
			zteIptvMediaInfo.exoMediaInfo.mediaType = MEDIA_TYPE_CHANNEL;

			zteIptvMediaInfo.uri = Uri
					.parse(zteIptvMediaInfo.exoMediaInfo.mediaUrl);
			ExoplayerLogEx.d(TAG, "MediaUrl: "
					+ zteIptvMediaInfo.exoMediaInfo.mediaUrl);
			ExoplayerLogEx.d(TAG, "TimeshiftUrl: "
					+ zteIptvMediaInfo.exoMediaInfo.timeshiftUrl);
			ExoplayerLogEx.d(TAG, "MediaType: "
					+ zteIptvMediaInfo.exoMediaInfo.mediaType);
			ExoplayerLogEx.d(TAG, "FccServerAddr: "
					+ zteIptvMediaInfo.exoMediaInfo.fccServerAddr);
			return buildMediaSource(zteIptvMediaInfo);
		} else {
			Log.i(TAG, "Use Normal ExoPlayer");
		}
		
		@ContentType
		int type = TextUtils.isEmpty(mediaInfo.extension) ? Util
				.inferContentType(mediaInfo.uri) : Util.inferContentType("."
				+ mediaInfo.extension);
		switch (type) {
		case C.TYPE_SS:
			return new SsMediaSource(mediaInfo.uri, mediaDataSourceFactory, new DefaultSsChunkSource.Factory(
					mediaDataSourceFactory), mainHandler, null);
		case C.TYPE_DASH:
			return new DashMediaSource(mediaInfo.uri, mediaDataSourceFactory, new DefaultDashChunkSource.Factory(
					mediaDataSourceFactory), mainHandler, null);  
		case C.TYPE_HLS:
			return new HlsMediaSource(mediaInfo.uri, mediaDataSourceFactory,
					mainHandler, null);
		case C.TYPE_OTHER:
			return new ExtractorMediaSource(mediaInfo.uri,
					mediaDataSourceFactory, new DefaultExtractorsFactory(),
					mainHandler, null);
		default: {
			throw new IllegalStateException("Unsupported type: " + type);
		}
		}
	}

	//ZTE ExoPlayer构造
	private MediaSource buildMediaSource(ZteIptvMediaInfo zteIptvMediaInfo) {
		DataSource.Factory dataSourceFactory = buildDataSourceFactory(false, zteLibVodJni);
		return new ZteMediaSource(zteIptvMediaInfo, dataSourceFactory,
				new DefaultExtractorsFactory(), mainHandler, null);//EVENT LOGGER NULL
	}

	private DataSource.Factory buildDataSourceFactory(
			boolean useBandwidthMeter, ZteLibVodJni zteLibVodJni) {
		return new DefaultDataSourceFactory(context, BANDWIDTH_METER,
				new ZteDataSourceFactory(zteLibVodJni));
	}
	
	//是否是IPTV
	private static boolean isIPTVUrl(String mediaUrl) {
		boolean ret = false;
		if (mediaUrl.startsWith("igmp://")) {
			ret = true;
		} else if (mediaUrl.startsWith("rtp://")) {
			ret = true;
		} else if (mediaUrl.startsWith("rtsp://")) {
			ret = true;
		}else if (mediaUrl.startsWith("udp://")) {
			ret = true;
		}else if (mediaUrl.startsWith("rtp://")) {
			ret = true;
		} else {
			ret = false;
		}
		return ret;
	}

	@Override
	public void setDisplay(SurfaceHolder sh) {
		this.surfaceHolder = sh;
		Log.e(TAG, "setDisplay");
	}

	@Override
	public void setSurface(Surface surface) {
		this.surface = surface;
		Log.e(TAG, "setSurface");
	}

	@Override
	public void prepare() throws IOException, IllegalStateException {
		this.prepareAsync();
	}

	@Override
	public void prepareAsync() throws IllegalStateException {
		if (null == myPlayer) {
			Log.e(TAG, "prepareAsync Return, ExoPlayer is Null");
			return;
		}

		myPlayer.prepare(mediaSource);
		Log.e(TAG, "prepareAsync");
	}

	@Override
	public void start() throws IllegalStateException {
		if (null == myPlayer) {
			Log.e(TAG, "start Return, ExoPlayer is Null");
			return;
		}

		myPlayer.setPlayWhenReady(true);
		Log.e(TAG, "start");
	}

	@Override
	public int getDuration() {
		if (null == myPlayer) {
			Log.e(TAG, "getDuration Return -1, ExoPlayer is Null");
			return -1;
		}

		int iDuration = (int) myPlayer.getDuration();
		if (iDuration < 0) {
			iDuration = 0;
		}
		Log.i(TAG, "getDuration: " + iDuration);
		return iDuration;
	}

	@Override
	public TrackInfo[] getTrackInfo() throws IllegalStateException {
		if (null == listMediaTracks || listMediaTracks.size() == 0) {
			Log.i(TAG, "getTrackInfo Null");
			return null;
		}

		TrackInfo[] arrTrackInfos = new TrackInfo[listMediaTracks.size()];
		for (int i = 0; i < arrTrackInfos.length; i++) {
			MyTrack myTrack = listMediaTracks.get(i);
			MediaFormat format = new MediaFormat();
			format.setString(MediaFormat.KEY_LANGUAGE, myTrack.strLanguage);

			try {
				Class cls = TrackInfo.class;
				Class[] paramTypes = { int.class, MediaFormat.class };
				Object[] params = { myTrack.iMediaType, format }; // 方法传入的参数
				Constructor con = cls.getDeclaredConstructor(paramTypes); // 主要就是这句了
				con.setAccessible(true);
				Object object = con.newInstance(params);// 创建一个对象
				arrTrackInfos[i] = (TrackInfo) object;
			} catch (Exception e) {
				Log.e(TAG, "Get Track Error");
				e.printStackTrace();
				return null;
			}
		}

		Log.i(TAG, "getTrackInfo");
		return arrTrackInfos;
	}

	@Override
	public int getVideoHeight() {
		if (null == myPlayer) {
			Log.e(TAG, "getVideoHeight Return -1, ExoPlayer is Null");
			return -1;
		}

		Log.i(TAG, "getVideoHeight: " + iVideoHeight);
		return iVideoHeight;
	}

	@Override
	public int getVideoWidth() {
		if (null == myPlayer) {
			Log.e(TAG, "getVideoWidth Return -1, ExoPlayer is Null");
			return -1;
		}

		Log.i(TAG, "getVideoWidth: " + iVideoWidth);
		return iVideoWidth;
	}

	@Override
	public int getCurrentPosition() {
		if (null == myPlayer) {
			Log.e(TAG, "getCurrentPosition Return -1, ExoPlayer is Null");
			return -1;
		}

		int iPos = (int) myPlayer.getCurrentPosition();
		if (iPos < 0) {
			iPos = 0;
		}
		Log.i(TAG, "getCurrentPosition: " + iPos);
		return iPos;
	}

	@Override
	public void seekTo(int iPos) throws IllegalStateException {
		if (null == myPlayer) {
			Log.e(TAG, "seekTo Return -1, ExoPlayer is Null");
			return;
		}

		if (iPos > myPlayer.getDuration()) {
			Log.e(TAG, "seekTo Out of Range!");
			return;
		}

		myPlayer.seekTo(iPos);
		Log.i(TAG, "seekTo: " + iPos);

		// SEEK后马上上报SEEK COMPLETE事件
		if (null != onSeekCompleteListener) {
			onSeekCompleteListener.onSeekComplete(this);
		}
	}

	@Override
	public boolean isPlaying() {
		return isMediaReady ? isAutoPlaying : false;
	}

	@Override
	public void pause() throws IllegalStateException {
		if (null == myPlayer) {
			Log.e(TAG, "pause Return -1, ExoPlayer is Null");
			return;
		}

		myPlayer.setPlayWhenReady(false);
		Log.i(TAG, "pause");
	}

	@Override
	public void stop() throws IllegalStateException {
		if (null == myPlayer) {
			Log.e(TAG, "stop Return -1, ExoPlayer is Null");
			return;
		}

		myPlayer.stop();
		Log.i(TAG, "stop");
	}

	@Override
	public void release() {
		if (null == myPlayer) {
			Log.e(TAG, "release Return -1, ExoPlayer is Null");
			return;
		}

		myPlayer.release();
		Log.i(TAG, "release");
	}

	@Override
	public void reset() {
		this.release();
	}

	// -------------------------------监听器------------------------------------
	private OnBufferingUpdateListener onBufferingUpdateListener;
	private OnCompletionListener onCompletionListener;
	private OnErrorListener onErrorListener;
	private OnInfoListener onInfoListener;
	private OnPreparedListener onPreparedListener;
	private OnSeekCompleteListener onSeekCompleteListener;

	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		this.onBufferingUpdateListener = listener;
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		this.onCompletionListener = listener;
	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		this.onErrorListener = listener;
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		this.onInfoListener = listener;
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		this.onPreparedListener = listener;
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		this.onSeekCompleteListener = listener;
	}

	// -------------------------------监听器END----------------------------------

	// -------------------------------播放状态------------------------------------
	@Override
	public void onLoadingChanged(boolean isLoading) {
		// Log.i(TAG, "onLoadingChanged: " + isLoading);//和下载有关
	}

	@Override
	public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
		Log.i(TAG, "onPlayerStateChanged, playWhenReady: " + playWhenReady);
		isAutoPlaying = playWhenReady;
		switch (playbackState) {

		case ExoPlayer.STATE_IDLE:
			Log.i(TAG, "Player STATE_IDLE");
			break;

		case ExoPlayer.STATE_BUFFERING:
			Log.i(TAG, "Player STATE_BUFFERING");// 这里要上报701事件
			isBuffering = true;

			// 上报701事件
			if (null != onInfoListener && isMediaReady) {
				onInfoListener.onInfo(this, MEDIA_INFO_BUFFERING_START, 0);
			}

			if (null != onBufferingUpdateListener) {
				onBufferingUpdateListener.onBufferingUpdate(this, 0);
			}
			break;

		case ExoPlayer.STATE_READY:
			Log.i(TAG, "Player STATE_READY");
			readTracks();// 读取音视频轨道信息

			// 上报702事件
			if (isMediaReady && isBuffering) {
				if (null != onInfoListener) {
					onInfoListener.onInfo(this, MEDIA_INFO_BUFFERING_END, 100);
				}

				if (null != onBufferingUpdateListener) {
					onBufferingUpdateListener.onBufferingUpdate(this, 100);
				}
			}
			isBuffering = false;

			// 上报准备好事件
			if (null != onPreparedListener && !isMediaReady) {
				onPreparedListener.onPrepared(this);
				long iDuration = myPlayer.getDuration();
				Log.i(TAG, "Duration: " + iDuration);
			}

			// 媒体信息已经准备好
			isMediaReady = true;

			break;

		case ExoPlayer.STATE_ENDED:
			Log.i(TAG, "Player STATE_ENDED");// 这里上报播放完毕事件

			// 上报播放完毕
			if (null != onCompletionListener) {
				onCompletionListener.onCompletion(this);
			}
			break;
		}
	}

	// 读取音视频轨道信息
	private void readTracks() {
		// 读取轨道信息
		if (null != listMediaTracks || null == myPlayer) {
			return;
		}
		MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
		if (mappedTrackInfo == null) {
			Log.i(TAG, "Track NULL");
			return; 
		}

		listMediaTracks = new ArrayList<MyTrack>();
		int rendererCount = mappedTrackInfo.length;
		for (int i = 0; i < rendererCount; i++) {
			TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
			
			if (trackGroups.length != 0) {
				TrackGroup trackGroup = trackGroups.get(0);
				switch (myPlayer.getRendererType(i)) {
				case C.TRACK_TYPE_VIDEO:
					for(int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++){
						Format vFormat = trackGroup.getFormat(trackIndex);
						Log.i(TAG, "TRACK_TYPE_VIDEO: " + vFormat);
						MyTrack vTrack = new MyTrack();
						vTrack.iMediaType = TrackInfo.MEDIA_TRACK_TYPE_VIDEO;
						vTrack.setLanguage(vFormat.language);
						listMediaTracks.add(vTrack);
					}
					break;

				case C.TRACK_TYPE_AUDIO:
					for(int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++){
						Format aFormat = trackGroup.getFormat(trackIndex);
						Log.i(TAG, "TRACK_TYPE_AUDIO: " + aFormat);

						MyTrack aTrack = new MyTrack();
						aTrack.iMediaType = TrackInfo.MEDIA_TRACK_TYPE_AUDIO;
						aTrack.setLanguage(aFormat.language);
						listMediaTracks.add(aTrack);
					}
					break;

				case C.TRACK_TYPE_TEXT:
					for(int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++){
						Format tFormat = trackGroup.getFormat(trackIndex);
						Log.i(TAG, "TRACK_TYPE_TEXT: " + tFormat);

						MyTrack tTrack = new MyTrack();
						tTrack.iMediaType = TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT;
						tTrack.setLanguage(tFormat.language);
						listMediaTracks.add(tTrack);
					}
					break;
				default:
					continue;
				}
			}
		}
	}
	

	@Override
	public void onPlayerError(ExoPlaybackException error) {
		Log.i(TAG, "onPlayerError: " + error.getMessage());

		// 上报错误事件
		if (null != onErrorListener) {
			onErrorListener.onError(this, MEDIA_ERROR_UNKNOWN, -99);
		}
	}

	// -------------------------------播放状态END---------------------------------

	// -------------------------------视频信息监听---------------------------------
	@Override
	public void onVideoEnabled(DecoderCounters counters) {

	}

	@Override
	public void onVideoDecoderInitialized(String decoderName,
			long initializedTimestampMs, long initializationDurationMs) {
		Log.i(TAG, "onVideoDecoderInitialized: Codec: " + decoderName);
	}

	@Override
	public void onVideoInputFormatChanged(Format format) {

	}

	@Override
	public void onDroppedFrames(int count, long elapsedMs) {

	}

	@Override
	public void onVideoSizeChanged(int width, int height,
			int unappliedRotationDegrees, float pixelWidthHeightRatio) {
		Log.i(TAG, "onVideoSizeChanged: " + width + " x " + height);
		this.iVideoWidth = width;
		this.iVideoHeight = height;
	}

	@Override
	public void onRenderedFirstFrame(Surface surface) {
		Log.i(TAG, "onRenderedFirstFrame！！！");

		// 上报第一帧事件
		if (null != onInfoListener) {
			onInfoListener.onInfo(this, MEDIA_INFO_VIDEO_RENDERING_START, 0);
		}
	}

	@Override
	public void onVideoDisabled(DecoderCounters counters) {

	}

	// -------------------------------视频信息监听END---------------------------------

	// ------------------------------空实现--------------------------
	@Override
	public int getAudioSessionId() {
		Log.e(TAG, "getAudioSessionId not implemented");
		return -1;
	}

	@Override
	public boolean isLooping() {
		Log.i(TAG, "isLooping not implemented");
		return false;
	}

	@Override
	public void setScreenOnWhilePlaying(boolean screenOn) {

	}

	@Override
	public void selectTrack(int index) throws IllegalStateException {

	}

	@Override
	public void setAudioSessionId(int arg0) throws IllegalArgumentException,
			IllegalStateException {

	}

	@Override
	public void setAudioStreamType(int arg0) {

	}

	@Override
	public void setAuxEffectSendLevel(float arg0) {

	}

	@Override
	public void setDataSource(Context context, Uri uri,
			Map<String, String> headers) throws IOException,
			IllegalArgumentException, SecurityException, IllegalStateException {

	}

	@Override
	public void setDataSource(Context context, Uri uri) throws IOException,
			IllegalArgumentException, SecurityException, IllegalStateException {

	}

	@Override
	public void setDataSource(FileDescriptor fd, long offset, long length)
			throws IOException, IllegalArgumentException, IllegalStateException {

	}

	@Override
	public void setDataSource(FileDescriptor fd) throws IOException,
			IllegalArgumentException, IllegalStateException {

	}

	@Override
	public void setLooping(boolean arg0) {

	}

	@Override
	public void setNextMediaPlayer(MediaPlayer arg0) {

	}

	@Override
	public void setVideoScalingMode(int mode) {

	}

	@Override
	public void setVolume(float arg0, float arg1) {

	}

	@Override
	public void setWakeMode(Context context, int mode) {

	}

	@Override
	public void addTimedTextSource(Context context, Uri uri, String mimeType)
			throws IOException, IllegalArgumentException, IllegalStateException {

	}

	@Override
	public void addTimedTextSource(FileDescriptor fd, long offset, long length,
			String mimeType) throws IllegalArgumentException,
			IllegalStateException {

	}

	@Override
	public void addTimedTextSource(FileDescriptor fd, String mimeType)
			throws IllegalArgumentException, IllegalStateException {

	}

	@Override
	public void addTimedTextSource(String path, String mimeType)
			throws IOException, IllegalArgumentException, IllegalStateException {

	}

	@Override
	public void attachAuxEffect(int arg0) {

	}

	@Override
	public void deselectTrack(int index) throws IllegalStateException {

	}

	@Override
	public void setOnTimedTextListener(OnTimedTextListener listener) {

	}

	@Override
	public void setOnVideoSizeChangedListener(
			OnVideoSizeChangedListener listener) {

	}
	
	// ------------------------------空实现 END--------------------------

	@Override
	public void onPlaybackParametersChanged(PlaybackParameters arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPositionDiscontinuity(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRepeatModeChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSeekProcessed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShuffleModeEnabledChanged(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTracksChanged(TrackGroupArray arg0, TrackSelectionArray arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTimelineChanged(Timeline arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
}

// 对接MeidaPlayer Track info
class MyTrack {
	// 媒体类型，以MediaPlayer.java的定义（Video、Audio、Subtitle）
	public int iMediaType;

	// 语言
	public String strLanguage;

	// 设置语言，最多3个字符
	public void setLanguage(String strLan) {
		if (strLan == null) {
			this.strLanguage = "und";
		} else if (strLan.length() > 3) {
			this.strLanguage = strLan.substring(0, 3);
		} else {
			this.strLanguage = strLan;
		}
	}
}


