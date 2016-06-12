package com.mi.mypulllayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 上拉刷新。 把ListView、GridView封装在里面，通过从外部传入Adapter来提供数据。 同时提供没有数据时，引导页的显示。
 *
 * @author jijiaxin
 */
public class MCPullToRefreshView extends LinearLayout {
    private static final String TAG = "PullToRefreshView";
    public static final int TIME = 1400;

    /**
     * refresh状态
     */
    private static final int DEFAULT_REFRESH = -1;
    private static final int PULL_TO_REFRESH = 2;
    private static final int RELEASE_TO_REFRESH = 3;
    private static final int REFRESHING = 4;
    /**
     * 上拉还是下拉
     */
    private static final int PULL_UP_STATE = 0;
    private static final int PULL_DOWN_STATE = 1;

    /**
     * 上一次的Y值
     */
    private int mLastMotionY;

    /**
     * lock
     */
    private boolean mLock;

    /**
     * 顶部的下啦布局
     */
    private View mHeaderView;

    /**
     * 底部的上拉布局
     */
    private View mFooterView;

    /**
     * ListView or GridView
     */
    private AdapterView<?> mAdapterView;

    /**
     * 用于显示空数据状态的父容器
     */
    private ScrollView mScrollView;

    private int mHeaderViewHeight;

    private int mFooterViewHeight;

    private int mLastTotalItemCount = 0;
    private int mFirstVisibleItem;

    /**
     * 底部显示的文字
     */
    private TextView mFooterTextView;

    /**
     * 底部的更新进度条
     */
    private ProgressBar mFooterProgressBar;

    private LayoutInflater mInflater;

    /**
     * 顶部布局当前的状态 ,正在刷新或者准备刷新等
     */
    private int mHeaderState = DEFAULT_REFRESH;

    /**
     * 底部布局当前的状态 ,正在刷新或者准备刷新等
     */
    private int mFooterState = DEFAULT_REFRESH;

    /**
     * 当前处于的状态上拉 或者 下拉PULL_UP_STATE or PULL_DOWN_STATE
     */
    private int mPullState;

    /**
     * 上拉的监听器
     */
    private OnFooterRefreshListener mOnFooterRefreshListener;

    /**
     * 下拉的监听器
     */
    private OnHeaderRefreshListener mOnHeaderRefreshListener;

    private OnTouchViewListener mOnTouchViewListener;

    /**
     * 数据显示的列数
     */
    private int rowCount = 1;

    /**
     * 是否允许顶部刷新
     */
    private boolean allowHeaderPull = false;

    /**
     * 是否允许底部刷新
     */
    private boolean allowFooterPull = false;

    /**
     * 是否支持加载更多
     */
    private boolean isSupportLoadMore = false;

    /**
     * 是否显示纵向滚动条
     */
    private boolean hasVerticalScrollbars = false;

    /**
     * AdapterView的item点击事件
     */
    private OnItemClickListener mOnItemClickListener;

    /**
     * AdapterView的item长按事件
     */
    private OnItemLongClickListener mOnItemLongClickListener;

    /**
     * 提示文字
     */
    private TextView mRefreshTextView;

    private FirstSetpView firstSetpView;
    private SecondStepView secondStepView;
    private AnimationDrawable secondAnimation;


    private boolean isPaged = true;

    public MCPullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MCPullToRefreshView, 0, 0);

        rowCount = a.getInteger(R.styleable.MCPullToRefreshView_rowCount, 1);
        allowHeaderPull = a.getBoolean(R.styleable.MCPullToRefreshView_allowHeaderPull, true);
        allowFooterPull = a.getBoolean(R.styleable.MCPullToRefreshView_allowFooterPull, true);
        isSupportLoadMore = a.getBoolean(R.styleable.MCPullToRefreshView_supportLoadMore, true);
        hasVerticalScrollbars = a.getBoolean(R.styleable.MCPullToRefreshView_hasVerticalScrollbars, false);
        a.recycle();

        init();
    }

    public MCPullToRefreshView(Context context) {
        super(context);
        init();
    }

    /**
     * 初始化，在初始化的同时,加载顶部
     */
    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        mInflater = LayoutInflater.from(getContext());
        // header view 在此添加,保证是第一个添加到linearlayout的最上端
        addHeaderView();
    }

    private void addHeaderView() {
        // header view
        mHeaderView = mInflater.inflate(R.layout.refresh_header, this, false);
        mRefreshTextView = (TextView) mHeaderView.findViewById(R.id.refresh_text);
        firstSetpView = (FirstSetpView) mHeaderView.findViewById(R.id.first_step_view);
        secondStepView = (SecondStepView) mHeaderView.findViewById(R.id.second_step_view);
        secondStepView.setBackgroundResource(R.drawable.second_step_animation);
        secondAnimation = (AnimationDrawable) secondStepView.getBackground();

        measureView(mHeaderView);
        mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mHeaderViewHeight);
        // 设置topMargin的值为负的header View高度,即将其隐藏在最上方
        params.topMargin = -(mHeaderViewHeight);
        addView(mHeaderView, params);
    }

    private void addFooterView() {
//        mFooterView = mInflater.inflate(R.layout.refresh_footer, this, false);
//        mFooterTextView = (TextView) mFooterView.findViewById(R.id.pull_to_load_text);
//        mFooterProgressBar = (ProgressBar) mFooterView.findViewById(R.id.pull_to_load_progress);
//        measureView(mFooterView);
//
//        mFooterViewHeight = mFooterView.getMeasuredHeight();
//        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mFooterViewHeight);
//        // int top = getHeight();
//        // params.topMargin
//        // =getHeight();//在这里getHeight()==0,但在onInterceptTouchEvent()方法里getHeight()已经有值了,不再是0;
//        // getHeight()什么时候会赋值,稍候再研究一下
//        // 由于是线性布局可以直接添加,只要AdapterView的高度是MATCH_PARENT,那么footer view就会被添加到最后,并隐藏
//        addView(mFooterView, params);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // footer view 在此添加保证添加到linearlayout中的最后
        initAdapterView();
        // 以后需要的话可以补充
        addFooterView();
        initContentAdapterView();
    }

    /**
     * 根据所要显示的rowCount初始化AdapterView。
     */
    public void initAdapterView() {
        FrameLayout frame = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.refresh_framelayout, null);
        if (rowCount == 1) {
            ListView listview = (ListView) LayoutInflater.from(getContext()).inflate(R.layout.listview, null);

            if (hasVerticalScrollbars) {
                listview.setVerticalScrollBarEnabled(true);
            } else {
                listview.setVerticalScrollBarEnabled(false);
            }
            listview.setOnItemClickListener(mOnItemClickListener);
            listview.setOnItemLongClickListener(mOnItemLongClickListener);
            listview.setOnScrollListener(mOnScollListener);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                listview.setOverScrollMode(OVER_SCROLL_NEVER);
            }
            FrameLayout.LayoutParams listViewParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            frame.addView(listview, 0, listViewParams);
        } else {
            GridView gridview = (GridView) LayoutInflater.from(getContext()).inflate(R.layout.gridview, null);
            gridview.setOnItemClickListener(mOnItemClickListener);
            gridview.setOnItemLongClickListener(mOnItemLongClickListener);
            gridview.setOnScrollListener(mOnScollListener);
            FrameLayout.LayoutParams gridViewParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            gridview.setNumColumns(rowCount);
            frame.addView(gridview, 0, gridViewParams);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                gridview.setOverScrollMode(OVER_SCROLL_NEVER);
            }
        }
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(frame, 1, lp);
    }

    private OnScrollListener mOnScollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mFirstVisibleItem = firstVisibleItem;
            if (!isRefresh() && isSupportLoadMore) {
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    if (mFooterView != null && mFooterView.getVisibility() == View.VISIBLE) {
                        View childView = view.getChildAt(0);
                        int height = 0;
                        int countHeight = 0;
                        if (childView != null) {
                            height = childView.getHeight();
                            countHeight = height * totalItemCount;
                        } else {
                            return;
                        }
                        if (mLastTotalItemCount == totalItemCount || countHeight <= view.getMeasuredHeight())
                            return;
                        if (isPaged) {
                            footerRefreshing(true);
                        }
                        mLastTotalItemCount = totalItemCount;
                    }
                }
            }
        }
    };

    /**
     * 为AdapterView添加数据。 此数据adapter由外部封装传入。
     */
    public void setDataAdapter(BaseAdapter adapter) {
        if (rowCount == 1) {
            ((ListView) mAdapterView).setAdapter(adapter);
        } else {
            ((GridView) mAdapterView).setAdapter(adapter);
        }

    }

    /**
     * 为内置listview添加header view。<br/>
     * gridview 暂不支持。
     *
     * @param v
     */
    public void addAdapterHeaderView(View v) {
        if (rowCount == 1) {
            ((ListView) mAdapterView).addHeaderView(v);
        } else {
            // ((GridView) mAdapterView).add
        }
    }


    /**
     * 内置listview移出header view。<br/>
     * gridview 暂不支持。
     *
     * @param v
     */
    public void removeAdapterHeaderView(View v) {
        if (rowCount == 1) {
            ((ListView) mAdapterView).removeHeaderView(v);
        } else {
            // ((GridView) mAdapterView).add
        }
    }

    /**
     * 当是griview时，设置行间距
     *
     * @param verticalSpacing 行间距
     */
    public void setAdapterViewVerticalSpacing(int verticalSpacing) {
        if (rowCount > 1) {
            ((GridView) mAdapterView).setVerticalSpacing(verticalSpacing);
        }
    }

    /**
     * 当是griview时，设置列间距
     *
     * @param horizontalSpacing 列间距
     */
    public void setAdapterViewHorizontalSpacing(int horizontalSpacing) {
        if (rowCount > 1) {
            ((GridView) mAdapterView).setHorizontalSpacing(horizontalSpacing);
        }
    }

    /**
     * 设置内置listview、gridview的padding
     */
    public void setAdapterViewPadding(int left, int top, int right, int bottom) {
        mAdapterView.setPadding(left, top, right, bottom);
    }

    /**
     * 设置数据的显示列数
     *
     * @param rowCount 列数
     */
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * 设置是否允许顶部刷新
     *
     * @param allowHeaderPull
     */
    public void setAllowHeaderPull(boolean allowHeaderPull) {
        this.allowHeaderPull = allowHeaderPull;
    }

    /**
     * 设置是否允许底部刷新
     *
     * @param allowFooterPull
     */
    public void setAllowFooterPull(boolean allowFooterPull) {
        this.allowFooterPull = allowFooterPull;
    }

    /**
     * 设置item点击事件
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
        mAdapterView.setOnItemClickListener(mOnItemClickListener);
    }

    /**
     * 设置item长按事件
     *
     * @param listener
     */
    public void setOnItemLongLitener(OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
        mAdapterView.setOnItemLongClickListener(mOnItemLongClickListener);
    }

    /**
     * 设置当ListView、GridView没有数据时，显示的引导页。
     */
    public void setGuidanceViewWhenNoData(View view) {
        FrameLayout frame = (FrameLayout) getChildAt(1);
        try {
            View guidanceView = frame.getChildAt(1);
            if (guidanceView == null) {
                FrameLayout.LayoutParams listview_params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                frame.addView(view, 1, listview_params);
            } else {
                guidanceView.setVisibility(View.VISIBLE);
            }
            View adapterview = frame.getChildAt(0);
            if (adapterview != null) {
                adapterview.setVisibility(View.GONE);
            }
        } catch (Exception e) {
        }

    }

    // 用来补充不满一屏的时候底部颜色不好改变
    public void setFootPoorColor(View view) {
        FrameLayout frame = (FrameLayout) getChildAt(1);
        try {
            View emptyView = frame.getChildAt(1);
            if (emptyView == null) {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                frame.addView(view, 1, params);
            } else {
                emptyView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
        }
    }

    /**
     * 设置当ListView、GridView有数据时，显示listview、gridiew等。
     */
    public void setAdapterViewWhenHasData() {
        FrameLayout frame = (FrameLayout) getChildAt(1);
        try {
            View adapterView = frame.getChildAt(0);
            if (adapterView == null) {
                initAdapterView();
            } else {
                adapterView.setVisibility(View.VISIBLE);
            }
            View guideView = frame.getChildAt(1);
            if (guideView != null) {
                guideView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
        }
    }

    /**
     * 上拉无更多内容是，不出现loading布局， 显示没有数据提示。
     */
    public void setNoContentVisibility() {
        mFooterView.setVisibility(View.GONE);
    }

    /**
     * 初始化AdapterView，ListView or GridView；或者是空布局的父容器
     */
    private void initContentAdapterView() {
        int count = getChildCount();
        if (count < 2) {
            throw new IllegalArgumentException(
                    "this layout must contain 3 child views,and AdapterView or ScrollView must in the second position!");
        }
        FrameLayout frame = (FrameLayout) getChildAt(1);
        View view = frame.getChildAt(0);
        if (view instanceof AdapterView<?>) {
            mAdapterView = (AdapterView<?>) view;
        }
        if (mAdapterView == null && mScrollView == null) {
            throw new IllegalArgumentException("must contain a AdapterView or ScrollView in this layout!");
        }
    }

    /**
     * 处理控件的宽高
     *
     * @param child
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 首先拦截down事件,记录y坐标
                mLastMotionY = (int) e.getRawY();
                if (mOnTouchViewListener != null) {
                    mOnTouchViewListener.onTouchView(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // deltaY > 0 是向下运动,< 0是向上运动
                int deltaY = (int) e.getRawY() - mLastMotionY;
                if (Math.abs(deltaY) < 10) {
                    return false;
                }
                if (isRefreshViewScroll(deltaY)) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return false;
    }

    private boolean isRecord;
    private float startY;
    private float offsetY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLock) {
            return true;
        }
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mFirstVisibleItem == 0 && !isRecord) {
                    isRecord = true;
                    startY = event.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaY = y - mLastMotionY;

                float tempY = event.getY();
                if (mFirstVisibleItem == 0 && !isRecord) {
                    isRecord = true;
                    startY = tempY;
                }
                float currentProgress;
                if (mPullState != REFRESHING && isRecord) {
                    offsetY = tempY - startY;
                    float currentHeight = (-mHeaderViewHeight + offsetY / 3);
                    // currentHeight从负数变为0. 0开始逐渐增大到1
                    currentProgress = 1 + currentHeight / mHeaderViewHeight;
                    if (currentProgress >= 1) {
                        currentProgress = 1;
                    }
                    if (mPullState == PULL_DOWN_STATE) {
                        // PullToRefreshView执行下拉
                        firstSetpView.setCurrentProgress(currentProgress);
                        firstSetpView.postInvalidate();
                    }
                }

                if (mPullState == PULL_DOWN_STATE) {
                    // PullToRefreshView执行下拉
                    headerPrepareToRefresh(deltaY);
                } else if (mPullState == PULL_UP_STATE) {
                    // PullToRefreshView执行上拉
//                    footerPrepareToRefresh(deltaY);
                }
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int topMargin = getHeaderTopMargin();
                if (mPullState == PULL_DOWN_STATE) {
                    if (topMargin >= 0) {
                        // 开始刷新
                        headerRefreshing();
                    } else {
                        // 还没有执行刷新，重新隐藏
                        setHeaderTopMargin(-mHeaderViewHeight);
                        mHeaderState = DEFAULT_REFRESH;
                        mFooterState = DEFAULT_REFRESH;
                    }
                } else if (mPullState == PULL_UP_STATE) {
                    if (mFooterState == PULL_TO_REFRESH || mFooterState == RELEASE_TO_REFRESH) {
                        if (Math.abs(topMargin) >= mHeaderViewHeight + mFooterViewHeight) {
                            // 开始执行footer 刷新
                            footerRefreshing(false);
                        } else {
                            // 还没有执行刷新，重新隐藏
                            setHeaderTopMargin(-mHeaderViewHeight);
                        }
                    }
                }
                isRecord = false;
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 是否应该到了父View,即PullToRefreshView滑动
     *
     * @param deltaY , deltaY > 0 是向下运动,< 0是向上运动
     */
    private boolean isRefreshViewScroll(int deltaY) {
        if (mHeaderState == REFRESHING || mFooterState == REFRESHING) {
            return false;
        }
        FrameLayout frame = (FrameLayout) getChildAt(1);
        // 对于ListView和GridView
        if (mAdapterView != null && (frame.getChildAt(0).getVisibility() == View.VISIBLE)) {
            // 子view(ListView or GridView)滑动到最顶端
            if (deltaY > 0) {

                View child = mAdapterView.getChildAt(0);
                if (child == null) {
                    // 如果mAdapterView中没有数据,不拦截
                    return false;
                }
                if (mAdapterView.getFirstVisiblePosition() == 0 && child.getTop() == 0) {
                    mPullState = PULL_DOWN_STATE;
                    return true;
                }
                int top = child.getTop();
                int padding = mAdapterView.getPaddingTop();
                if (mAdapterView.getFirstVisiblePosition() == 0 && Math.abs(top - padding) <= 20) {// 这里之前用3可以判断,但现在不行,还没找到原因;ListView用8可以判断，GridView须11，魅族得14以上，不明白原因。
                    mPullState = PULL_DOWN_STATE;
                    return true;
                }

            } else if (deltaY < 0) {
                View lastChild = mAdapterView.getChildAt(mAdapterView.getChildCount() - 1);
                if (lastChild == null) {
                    // 如果mAdapterView中没有数据,不拦截
                    return false;
                }
                // 最后一个子view的Bottom小于父View的高度说明mAdapterView的数据没有填满父view,
                // 等于父View的高度说明mAdapterView已经滑动到最后
                if (lastChild.getBottom() <= getHeight()
                        && mAdapterView.getLastVisiblePosition() == mAdapterView.getCount() - 1) {
                    mPullState = PULL_UP_STATE;
                    return true;
                }
            }
        } else if (mScrollView != null && (frame.getChildAt(1).getVisibility() == View.VISIBLE)) {// 对于ScrollView
            // 子scroll view滑动到最顶端
            View child = mScrollView.getChildAt(0);
            if (deltaY > 0 && mScrollView.getScrollY() == 0) {
                mPullState = PULL_DOWN_STATE;
                return true;
            } else if (deltaY < 0 && child.getMeasuredHeight() <= getHeight() + mScrollView.getScrollY()) {
                mPullState = PULL_UP_STATE;
                return true;
            }
        } else {
            if (getChildAt(1) != null) {
                if (deltaY > 0) {
                    mPullState = PULL_DOWN_STATE;
                    return true;
                } else {
                    mPullState = PULL_UP_STATE;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * header 准备刷新,手指移动过程,还没有释放
     *
     * @param deltaY 手指滑动的距离
     */
    private void headerPrepareToRefresh(int deltaY) {
        if (allowHeaderPull) {
            int newTopMargin = changingHeaderViewTopMargin(deltaY);
            // 当header view的topMargin>=0时，说明已经完全显示出来了,修改header view 的提示状态
            if (newTopMargin >= 0 && mHeaderState != RELEASE_TO_REFRESH) {
                mRefreshTextView.setText(R.string.pull_to_rlease_text);

                firstSetpView.setVisibility(View.VISIBLE);
                secondAnimation.stop();
                secondStepView.setVisibility(View.GONE);

                mHeaderState = RELEASE_TO_REFRESH;
            } else if (newTopMargin < 0 && newTopMargin > -mHeaderViewHeight) {// 拖动时没有释放
                mRefreshTextView.setText(R.string.pull_to_refresh_text);

                firstSetpView.setVisibility(View.VISIBLE);
                secondAnimation.stop();
                secondStepView.setVisibility(View.GONE);

                mHeaderState = PULL_TO_REFRESH;
            }
        }
    }

    /**
     * footer 准备刷新,手指移动过程,还没有释放 移动footer view高度同样和移动header view
     * 高度是一样，都是通过修改header view的topmargin的值来达到
     *
     * @param deltaY 手指滑动的距离
     */
    private void footerPrepareToRefresh(int deltaY) {
        if (allowFooterPull) {
            int newTopMargin = changingHeaderViewTopMargin(deltaY);
            // 如果header view topMargin 的绝对值大于或等于header + footer 的高度
            // 说明footer view 完全显示出来了，修改footer view 的提示状态
            if (Math.abs(newTopMargin) >= (mHeaderViewHeight + mFooterViewHeight) && mFooterState != RELEASE_TO_REFRESH) {
                mFooterTextView.setText("");
                mFooterState = RELEASE_TO_REFRESH;
            } else if (Math.abs(newTopMargin) < (mHeaderViewHeight + mFooterViewHeight)) {
                mFooterTextView.setText("");
                mFooterState = PULL_TO_REFRESH;
            }
            if (!isSupportLoadMore) {
                mFooterProgressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 修改Header view top margin的值
     *
     * @param deltaY
     */
    private int changingHeaderViewTopMargin(int deltaY) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        float newTopMargin = params.topMargin + deltaY * 0.3f;
        // 这里对上拉做一下限制,因为当前上拉后然后不释放手指直接下拉,会把下拉刷新给触发了
        // 表示如果是在上拉后一段距离,然后直接下拉
        if (deltaY > 0 && mPullState == PULL_UP_STATE && Math.abs(params.topMargin) <= mHeaderViewHeight) {
            return params.topMargin;
        }
        // 同样地,对下拉做一下限制,避免出现跟上拉操作时一样的bug
        if (deltaY < 0 && mPullState == PULL_DOWN_STATE && Math.abs(params.topMargin) >= mHeaderViewHeight) {
            // return params.topMargin;
        }
        params.topMargin = (int) newTopMargin;
        mHeaderView.setLayoutParams(params);
        invalidate();
        return params.topMargin;
    }

    /**
     * 顶部的刷新
     */
    public void headerRefreshing() {
        mHeaderState = REFRESHING;
        setHeaderTopMargin(0);

        firstSetpView.setVisibility(View.GONE);
        secondStepView.setVisibility(View.VISIBLE);
        secondAnimation.stop();
        secondAnimation.start();

        mRefreshTextView.setText(R.string.pull_to_refreshing_text);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(), TIME);
        mLastTotalItemCount = 0;
    }

    /**
     * 底部的刷新 若是存在更多数据，则正常走流程；若是显示的是没有更多数据，则直接走完成方法。
     */
    private void footerRefreshing(boolean showProgressBar) {
        if (mFooterView.getVisibility() == View.VISIBLE) {
            mFooterState = REFRESHING;
            int top = mHeaderViewHeight + mFooterViewHeight;
            setHeaderTopMargin(-top);
            if (showProgressBar) {
                mFooterView.setVisibility(View.VISIBLE);
                mFooterProgressBar.setVisibility(View.VISIBLE);
                mFooterTextView.setVisibility(View.VISIBLE);
                mFooterTextView.setText(R.string.pull_to_refresh_loading_more);
                if (mOnFooterRefreshListener != null) {
                    mOnFooterRefreshListener.onFooterRefresh(this);
                }
            } else {
                onFooterRefreshComplete();
                mFooterView.setVisibility(View.GONE);
                setHeaderTopMargin(-mHeaderViewHeight);
            }
        } else {
            setHeaderTopMargin(-mHeaderViewHeight);
        }
    }

    /**
     * 设置header view 的topMargin的值
     *
     * @param topMargin 为0时，说明header view 刚好完全显示出来； 为-mHeaderViewHeight时，说明完全隐藏了
     */
    private void setHeaderTopMargin(int topMargin) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        params.topMargin = topMargin;
        mHeaderView.setLayoutParams(params);
        invalidate();
    }

    /**
     * header view 完成更新后恢复初始状态
     */
    public void onHeaderRefreshComplete() {
        mHeaderState = DEFAULT_REFRESH;
        setHeaderTopMargin(-mHeaderViewHeight);

        firstSetpView.setVisibility(View.VISIBLE);
        secondAnimation.stop();
        secondStepView.setVisibility(View.GONE);

//        if (mFooterView.getVisibility() != View.VISIBLE) {
//            mFooterView.setVisibility(View.VISIBLE);
//        }
//        mFooterProgressBar.setVisibility(View.VISIBLE);
//        mFooterTextView.setVisibility(View.VISIBLE);
    }

    /**
     * footer view 完成更新后恢复初始状态
     */
    public void onFooterRefreshComplete() {
        mFooterTextView.setText("");
        mFooterProgressBar.setVisibility(View.GONE);
        mFooterState = DEFAULT_REFRESH;
    }

    /**
     * 获取当前header view 的topMargin
     */
    private int getHeaderTopMargin() {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        return params.topMargin;
    }

    /**
     * 判断当前顶部和底部View的状态是否是默认状态
     *
     * @return
     */
    private boolean isRefresh() {
        return isHeaderRefresh() || isFooterRefresh();
    }

    /**
     * 判断是否正在执行下拉刷新
     *
     * @return
     */
    public boolean isHeaderRefresh() {
        return mHeaderState != DEFAULT_REFRESH;
    }

    /**
     * 判断是否正在执行加载更多
     *
     * @return
     */
    public boolean isFooterRefresh() {
        return mFooterState != DEFAULT_REFRESH;
    }

    /**
     * 设置顶部刷新监听器
     */
    public void setOnHeaderRefreshListener(OnHeaderRefreshListener headerRefreshListener) {
        mOnHeaderRefreshListener = headerRefreshListener;
    }

    /**
     * 设置底部刷新监听器
     */
    public void setOnFooterRefreshListener(OnFooterRefreshListener footerRefreshListener) {
        mOnFooterRefreshListener = footerRefreshListener;
    }

    public void setOnTouchViewListener(OnTouchViewListener touchViewListener) {
        mOnTouchViewListener = touchViewListener;
    }

    /**
     * Interface definition for a callback to be invoked when list/grid footer
     * view should be refreshed.
     */
    public interface OnFooterRefreshListener {
        public void onFooterRefresh(MCPullToRefreshView view);
    }

    /**
     * Interface definition for a callback to be invoked when list/grid header
     * view should be refreshed.
     */
    public interface OnHeaderRefreshListener {
        public void onHeaderRefresh(MCPullToRefreshView view);
    }

    /**
     * Interface definition for a callback to be invoked when list/grid view
     * touched.
     */
    public interface OnTouchViewListener {
        public void onTouchView(MCPullToRefreshView view);
    }

    /**
     * 设置图片的背景
     *
     * @param drawableId
     */
//    private void setBackground(int drawableId) {
//        if (VERSION.SDK_INT < 16) {
//            mHeaderImageView.setBackgroundDrawable(getResources().getDrawable(drawableId));
//        } else {
//            mHeaderImageView.setBackground(getResources().getDrawable(drawableId));
//        }
//    }

    /**
     * 设置滚动到指定的位置
     *
     * @param position
     */
    public void setSelection(int position) {
        if (mAdapterView != null) {
            if (mAdapterView instanceof ListView) {
                mAdapterView.setSelection(position);
            }
        }
    }

    /**
     * 设置ListView的自动滚动模式
     *
     * @param mode
     */
    public void setTranscriptMode(int mode) {
        if (mAdapterView != null) {
            if (mAdapterView instanceof ListView) {
                ((ListView) mAdapterView).setTranscriptMode(mode);
            }
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mOnHeaderRefreshListener != null) {
                mOnHeaderRefreshListener.onHeaderRefresh(MCPullToRefreshView.this);
            }
        }
    };

    public void setHeaderAndFooterColor(int color) {
        setHeaderBackground(color);
        setFooterBackground(color);
    }

    public void setHeaderBackground(int color) {
        mHeaderView.setBackgroundColor(color);
    }

    public void setFooterBackground(int color) {
        mFooterView.setBackgroundColor(color);
    }

    public void setIsPage(boolean pageable) {
        isPaged = pageable;
    }
}
