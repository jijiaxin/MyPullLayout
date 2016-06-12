package com.mi.mypulllayout;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.mi.mypulllayout.adaper.BaseAdapterHelper;
import com.mi.mypulllayout.adaper.QuickAdapter;

import java.util.ArrayList;

public class MainActivity extends Activity implements MCPullToRefreshView.OnHeaderRefreshListener {


    public String tag = "MainActivity";
    public static Context ctxt = null;
    public static Handler handler = null;

    private MCPullToRefreshView mGridView;
    private QuickAdapter<BaseBoardEntity> adapter;

    public static final int UPDATE_GRIDVIEW_LOAD_MORE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        init();

        initData();
    }


    public void init() {
        ctxt = getApplicationContext();

        mGridView = (MCPullToRefreshView) findViewById(R.id.gridview);
        mGridView.setOnHeaderRefreshListener(this);

        adapter = new QuickAdapter<BaseBoardEntity>(this, R.layout.card_item, null) {
            @Override
            protected void convert(BaseAdapterHelper helper, BaseBoardEntity item) {
                helper.setText(R.id.base_board_item_name, item.getName());
                helper.setImageResource(R.id.base_board_item_icon, item.getImageResId());
//                view.setTag(item);
            }
        };

        mGridView.setDataAdapter(adapter);

    }

    private void initData() {
        mGridView.setAdapterViewWhenHasData();
//        if (mCurrentPage == 1) {
//            adapter.clear();
//        }

        initBoard();
        adapter.addAll(boardArray);
        adapter.notifyDataSetChanged();

    }

    /*单元格内容list*/
    private ArrayList<BaseBoardEntity> boardArray;
    //流程列表
    private static final int TEST1 = 1;
    //委托审批
    private static final int TEST2 = 2;
    //会议室预订
    private static final int TEST3 = 3;
    //租房
    private static final int TEST4 = 4;

    private void initBoard() {
        boardArray = new ArrayList<>();
        boardArray.add(new BaseBoardEntity(MainActivity.TEST1, "蜡笔小新", R.mipmap.app_refresh_people_1));
        boardArray.add(new BaseBoardEntity(MainActivity.TEST2, "樱桃丸子", R.mipmap.app_refresh_people_2));
        boardArray.add(new BaseBoardEntity(MainActivity.TEST3, "巴拉魔仙", R.mipmap.app_refresh_people_3));
        boardArray.add(new BaseBoardEntity(MainActivity.TEST4, "你好你好", R.mipmap.app_refresh_people_0));
    }

    @Override
    public void onHeaderRefresh(MCPullToRefreshView view) {
//        initData();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                mGridView.onHeaderRefreshComplete();
            }
        }, 2000);
    }
}
