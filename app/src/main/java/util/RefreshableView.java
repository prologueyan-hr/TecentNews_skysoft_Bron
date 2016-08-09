//package util;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.AsyncTask;
//import android.preference.PreferenceManager;
//import android.util.AttributeSet;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewConfiguration;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import bron.yan.tecentnews.R;
//
///**
// * Created by test on 2016/8/8.
// */
//public class RefreshableView extends LinearLayout implements View.OnTouchListener {
//
//    /**
//     * 下拉状态
//     */
//    public static final int STATUS_PULL_TO_REFRESH = 0;
//
//    /**
//     * 释放立即刷新状态
//     */
//    public static final int STATUS_RELEASE_TO_REFRESH = 1;
//
//    /**
//     * 正在刷新状态
//     */
//    public static final int STATUS_REFRESHING = 2;
//
//    /**
//     * 刷新完成或未刷新状态
//     */
//    public static final int STATUS_REFRESH_FINISHED = 3;
//
//    /**
//     * 下拉头部回滚的速度
//     */
//    public static final int SCROLL_SPEED = -20;
//
//    /**
//     * 一分钟的毫秒值，用于判断上次的更新时间
//     */
//    public static final long ONE_MINUTE = 60 * 1000;
//
//    /**
//     * 一小时的毫秒值，用于判断上次的更新时间
//     */
//    public static final long ONE_HOUR = 60 * ONE_MINUTE;
//
//    /**
//     * 一天的毫秒值，用于判断上次的更新时间
//     */
//    public static final long ONE_DAY = 24 * ONE_HOUR;
//
//    /**
//     * 一月的毫秒值，用于判断上次的更新时间
//     */
//    public static final long ONE_MONTH = 30 * ONE_DAY;
//
//    /**
//     * 一年的毫秒值，用于判断上次的更新时间
//     */
//    public static final long ONE_YEAR = 12 * ONE_MONTH;
//
//    /**
//     * 上次更新时间的字符串常量，用于作为SharedPreferences的键值
//     */
//    private static final String UPDATED_AT = "updated_at";
//
//    /**
//     * 下拉刷新的回调接口
//     */
//    private PullToRefreshListener mListener;
//
//    /**
//     * 用于存储上次更新时间
//     */
//    private SharedPreferences preferences;
//
//    /**
//     * 下拉头的View
//     */
//    private View header;
//
//    /**
//     * 需要去下拉刷新的ListView
//     */
//    private ListView listView;
//
//    /**
//     * 刷新时显示的进度条
//     */
//    private ProgressBar progressBar;
//
//    /**
//     * 指示下拉和释放的箭头
//     */
//    private ImageView arrow;
//
//    /**
//     * 指示下拉和释放的文字描述
//     */
//    private TextView description;
//
//    /**
//     * 上次更新时间的文字描述
//     */
//    private TextView updateAt;
//
//    /**
//     * 下拉头的布局参数
//     */
//    private MarginLayoutParams headerLayoutParams;
//
//    /**
//     * 上次更新时间的毫秒值
//     */
//    private long lastUpdateTime;
//
//    /**
//     * 为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，使用id来做区分
//     */
//    private int mId = -1;
//
//    /**
//     * 下拉头的高度
//     */
//    private int hideHeaderHeight;
//
//    /**
//     * 当前处理什么状态，可选值有STATUS_PULL_TO_REFRESH, STATUS_RELEASE_TO_REFRESH,
//     * STATUS_REFRESHING 和 STATUS_REFRESH_FINISHED
//     */
//    private int currentStatus = STATUS_REFRESH_FINISHED;
//    ;
//
//    /**
//     * 记录上一次的状态是什么，避免进行重复操作
//     */
//    private int lastStatus = currentStatus;
//
//    /**
//     * 手指按下时的屏幕纵坐标
//     */
//    private float yDown;
//
//    /**
//     * 在被判定为滚动之前用户手指可以移动的最大值。
//     */
//    private int touchSlop;
//
//    /**
//     * 是否已加载过一次layout，这里onLayout中的初始化只需加载一次
//     */
//    private boolean loadOnce;
//
//    /**
//     * 当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
//     */
//    private boolean ableToPull;
//
//    /**
//     * 下拉刷新控件的构造函数，会在运行时动态添加一个下拉头布局
//     *
//     * @param context
//     * @param attributeSet
//     */
//    public RefreshableView(Context context, AttributeSet attributeSet) {
//        super(context, attributeSet);
//        preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh, null, true);
//        arrow = (ImageView) header.findViewById(R.id.arrow);
//        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        super.onLayout(changed, l, t, r, b);
//        if (changed && !loadOnce) {
//            hideHeaderHeight = -header.getHeight();
//            headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
//            headerLayoutParams.topMargin = hideHeaderHeight;
//            listView = (ListView) getChildAt(1);
//            listView.setOnTouchListener(this);
//            loadOnce = true;
//        }
//    }
//
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        setIsAbleToPull(event);
//        if (ableToPull) {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    yDown = event.getRawY();
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    float yMove = event.getRawY();
//                    int distance = (int) (yMove - yDown);
//                    // 如果手指是下滑状态，并且下拉头是完全隐藏的，就屏蔽下拉事件
//                    if (distance <= 0 && headerLayoutParams.topMargin <= hideHeaderHeight) {
//                        return false;
//                    }
//                    if (distance < touchSlop) {
//                        return false;
//                    }
//                    if (currentStatus != STATUS_REFRESHING) {
//                        if (headerLayoutParams.topMargin > 0) {
//                            currentStatus = STATUS_RELEASE_TO_REFRESH;
//                        } else {
//                            currentStatus = STATUS_PULL_TO_REFRESH;
//                        }
//                        // 通过偏移下拉头的topMargin值，来实现下拉效果
//                        headerLayoutParams.topMargin = (distance / 2) + hideHeaderHeight;
//                        header.setLayoutParams(headerLayoutParams);
//                    }
//                    break;
//                case MotionEvent.ACTION_UP:
//                default:
//                    if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
//                        // 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
//                        new RefreshingTask().execute();
//                    } else if (currentStatus == STATUS_PULL_TO_REFRESH) {
//                        // 松手时如果是下拉状态，就去调用隐藏下拉头的任务
//                        new HideHeaderTask().execute();
//                    }
//                    break;
//            }
//            // 时刻记得更新下拉头中的信息
//            if (currentStatus == STATUS_PULL_TO_REFRESH
//                    || currentStatus == STATUS_RELEASE_TO_REFRESH) {
//                updateHeaderView();
//                // 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
//                listView.setPressed(false);
//                listView.setFocusable(false);
//                listView.setFocusableInTouchMode(false);
//                lastStatus = currentStatus;
//                // 当前正处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 正在刷新的任务，在此任务中会去回调注册进来的下拉刷新监听器。
//     *
//     * @author guolin
//     */
//    class RefreshingTask extends AsyncTask<Void, Integer, Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            int topMargin = headerLayoutParams.topMargin;
//            while (true) {
//                topMargin = topMargin + SCROLL_SPEED;
//                if (topMargin <= 0) {
//                    topMargin = 0;
//                    break;
//                }
//                publishProgress(topMargin);
//                sleep(10);
//            }
//            currentStatus = STATUS_REFRESHING;
//            publishProgress(0);
//            if (mListener != null) {
//                mListener.onRefresh();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... topMargin) {
////            updateHeaderView();
//            headerLayoutParams.topMargin = topMargin[0];
//            header.setLayoutParams(headerLayoutParams);
//        }
//
//    }
//
//    /**
//     * 隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏。
//     *
//     * @author guolin
//     */
//    class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {
//
//        @Override
//        protected Integer doInBackground(Void... params) {
//            int topMargin = headerLayoutParams.topMargin;
//            while (true) {
//                topMargin = topMargin + SCROLL_SPEED;
//                if (topMargin <= hideHeaderHeight) {
//                    topMargin = hideHeaderHeight;
//                    break;
//                }
//                publishProgress(topMargin);
//                sleep(10);
//            }
//            return topMargin;
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... topMargin) {
//            headerLayoutParams.topMargin = topMargin[0];
//            header.setLayoutParams(headerLayoutParams);
//        }
//
//        @Override
//        protected void onPostExecute(Integer topMargin) {
//            headerLayoutParams.topMargin = topMargin;
//            header.setLayoutParams(headerLayoutParams);
//            currentStatus = STATUS_REFRESH_FINISHED;
//        }
//    }
//
//    /**
//     * 使当前线程睡眠指定的毫秒数。
//     *
//     * @param time 指定当前线程睡眠多久，以毫秒为单位
//     */
//    private void sleep(int time) {
//        try {
//            Thread.sleep(time);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 根据当前ListView的滚动状态来设定 {@link #ableToPull}
//     * 的值，每次都需要在onTouch中第一个执行，这样可以判断出当前应该是滚动ListView，还是应该进行下拉。
//     *
//     * @param event
//     */
//    private void setIsAbleToPull(MotionEvent event) {
//        View firstChild = listView.getChildAt(0);
//        if (firstChild != null) {
//            int firstVisiblePos = listView.getFirstVisiblePosition();
//            if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
//                if (!ableToPull) {
//                    yDown = event.getRawY();
//                }
//                // 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
//                ableToPull = true;
//            } else {
//                if (headerLayoutParams.topMargin != hideHeaderHeight) {
//                    headerLayoutParams.topMargin = hideHeaderHeight;
//                    header.setLayoutParams(headerLayoutParams);
//                }
//                ableToPull = false;
//            }
//        } else {
//            // 如果ListView中没有元素，也应该允许下拉刷新
//            ableToPull = true;
//        }
//    }
//
//    /**
//     * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
//     *
//     * @author guolin
//     */
//    public interface PullToRefreshListener {
//
//        /**
//         * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。注意此方法是在子线程中调用的， 你可以不必另开线程来进行耗时操作。
//         */
//        void onRefresh();
//
//    }
//}
