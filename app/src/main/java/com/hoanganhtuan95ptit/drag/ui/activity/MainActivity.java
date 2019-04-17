package com.hoanganhtuan95ptit.drag.ui.activity;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.hoanganhtuan95ptit.drag.App;
import com.hoanganhtuan95ptit.drag.R;
import com.hoanganhtuan95ptit.drag.data.model.Channel;
import com.hoanganhtuan95ptit.drag.data.model.Video;
import com.hoanganhtuan95ptit.drag.ui.adapter.BaseAdapter;
import com.hoanganhtuan95ptit.drag.ui.adapter.HomeAdapter;
import com.hoanganhtuan95ptit.drag.ui.fragment.DetailFragment;
import com.hoanganhtuan95ptit.drag.ui.fragment.PlayFragment;
import com.hoanganhtuan95ptit.drag.utils.Utils;
import com.hoanganhtuan95ptit.drag.utils.images.ImageUtils;
import com.hoanganhtuan95ptit.drag.ui.widget.drag.DragFrame;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements DragFrame.OnDragListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.iv_avatar)
    ImageView ivAvatar;
    @BindView(R.id.rv_home)
    RecyclerView rvHome;
    @BindView(R.id.sr_home)
    SwipeRefreshLayout srHome;
    @BindView(R.id.v_background)
    View vBackground;
    @BindView(R.id.drag_frame)
    DragFrame dragFrame;

    public static float ratios[] = {16f / 9, 1 / 1, 9f / 16, 6f / 8, 8f / 6};

    private ArrayList<Object> feeds;
    private ArrayList<Object> trending;

    private HomeAdapter homeAdapter;

    private float ratio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ImageUtils.showImage(App.self().getApplicationContext(), ivAvatar, R.drawable.test_0, new CenterCrop(), new CircleCrop());

        feeds = new ArrayList<>();
        trending = new ArrayList<>();
        homeAdapter = new HomeAdapter(this);

        rvHome.setLayoutManager(new LinearLayoutManager(this));
        rvHome.setAdapter(homeAdapter);

        dragFrame.setBottomFragment(getSupportFragmentManager(), DetailFragment.newInstance());
        dragFrame.setTopFragment(getSupportFragmentManager(), PlayFragment.newInstance());
        dragFrame.setOnDragListener(this);
        dragFrame.close();

        srHome.setOnRefreshListener(this::onClosed);
        srHome.post(() -> {
            srHome.setRefreshing(true);
            fetchData();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoEvent(Video video) {
        play(video.getRatio());
    }


    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRefresh() {
        srHome.postDelayed(() -> srHome.setRefreshing(false), 1000);
    }

    @Override
    public void onDragProcess(float percent) {
        vBackground.setAlpha(1 - percent);
    }

    @Override
    public void onMaximized() {
        play();
    }

    @Override
    public void onMinimized() {
    }

    @Override
    public void onClosed() {

    }

    @OnClick(R.id.iv_pause)
    public void onIvPauseClicked() {
    }

    @OnClick(R.id.iv_play)
    public void onIvPlayClicked() {
    }

    @OnClick(R.id.iv_close)
    public void onIvCloseClicked() {
        dragFrame.close();
    }

    private void fetchData() {
        TypedArray videoTitleArray = getResources().obtainTypedArray(R.array.video_title);
        TypedArray videoThumbArray = getResources().obtainTypedArray(R.array.video_thumb);
        TypedArray videoTitle2Array = getResources().obtainTypedArray(R.array.video_title_2);
        TypedArray videoThumb2Array = getResources().obtainTypedArray(R.array.video_thumb);
        TypedArray channelTitleArray = getResources().obtainTypedArray(R.array.channel_title);
        TypedArray channelThumbArray = getResources().obtainTypedArray(R.array.video_thumb);

        for (int i = 0; i < 10; i++) {
            Channel channel = new Channel(String.valueOf(i), channelThumbArray.getResourceId(i, -1), channelTitleArray.getResourceId(i, -1));

            Video video = new Video(String.valueOf(i), videoThumbArray.getResourceId(i, -1), videoTitleArray.getResourceId(i, -1));
            video.setRatio(ratios[i % ratios.length]);
            video.setChannel(channel);

            feeds.add(video);
        }

        trending.add(BaseAdapter.SPACE_TYPE);
        for (int i = 0; i < 6; i++) {
            Channel channel = new Channel(String.valueOf(i), channelThumbArray.getResourceId(i, -1), channelTitleArray.getResourceId(i, -1));

            Video video = new Video(String.valueOf(i), videoThumb2Array.getResourceId(i, -1), videoTitle2Array.getResourceId(i, -1));
            video.setRatio(ratios[i % ratios.length]);
            video.setChannel(channel);

            trending.add(video);
        }
        trending.add(BaseAdapter.SPACE_TYPE);

        videoTitleArray.recycle();
        videoThumbArray.recycle();
        videoTitle2Array.recycle();
        videoThumb2Array.recycle();
        channelTitleArray.recycle();
        channelThumbArray.recycle();

        feeds.add(1, trending);
        feeds.add(BaseAdapter.END_TYPE);
        homeAdapter.bindData(feeds);

        srHome.setRefreshing(false);
    }


    public void play(float r) {
        ratio = r;
        if (dragFrame.isMaximized()) {
            play();
        } else {
            dragFrame.maximize();
        }
    }

    private void play() {
        dragFrame.postDelayed(delayRunnable, 200L);
    }

    private Runnable delayRunnable = new Runnable() {
        @Override
        public void run() {
            if (dragFrame == null) return;
            int heightNew = (int) Math.min(Utils.getScreenWidth() / ratio, Utils.getScreenHeight() - Utils.getScreenHeight() / 3f);
            if (dragFrame.isMaximized()) {
                dragFrame.setHeight(heightNew);
            } else {
                dragFrame.setHeightWaiting(heightNew);
            }
        }
    };
}
