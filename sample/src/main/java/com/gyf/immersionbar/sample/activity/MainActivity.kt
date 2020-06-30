package com.gyf.immersionbar.sample.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.gyf.immersionbar.*
import com.gyf.immersionbar.annotation.BarHide
import com.gyf.immersionbar.helper.BarProperties
import com.gyf.immersionbar.helper.PropertiesHelper
import com.gyf.immersionbar.sample.AppManager
import com.gyf.immersionbar.sample.BuildConfig
import com.gyf.immersionbar.sample.R
import com.gyf.immersionbar.sample.adapter.BannerAdapter
import com.gyf.immersionbar.sample.adapter.MainAdapter
import com.gyf.immersionbar.sample.bean.FunBean
import com.gyf.immersionbar.sample.event.NetworkEvent
import com.gyf.immersionbar.sample.fragment.SplashFragment
import com.gyf.immersionbar.sample.model.DataUtils
import com.gyf.immersionbar.sample.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * @author geyifeng
 */
class MainActivity : BaseActivity(), DrawerListener {

    @BindView(R.id.drawer)
    lateinit var drawer: DrawerLayout

    @BindView(R.id.toolbar)
    lateinit var mToolbar: Toolbar

    @BindView(R.id.iv_bg)
    lateinit var ivBg: ImageView

    @BindView(R.id.tv_version)
    lateinit var tvVersion: TextView

    @BindView(R.id.mRv)
    lateinit var mRv: RecyclerView

    /**
     * splash页面
     */

    private lateinit var mMainAdapter: MainAdapter
    private lateinit var mBannerAdapter: BannerAdapter

    private var mLayoutManager: LinearLayoutManager? = null
    private var mNetworkView: View? = null
    private var mBannerHeight: Int = 0
    private var mBannerPosition = -1

    private lateinit var mIvBanner: ImageView
    private var mMainData: ArrayList<FunBean>? = null

    private var propertiesHelper: PropertiesHelper? = null
    private var fullscreen = false

    override val layoutId: Int get() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        propertiesHelper = null
        drawer.removeDrawerListener(this)
        EventBus.getDefault().unregister(this)
    }

    override fun initImmersionBar() {
        super.initImmersionBar()
        setTitleBar(findViewById(R.id.toolbar))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        propertiesHelper?.onConfigurationChanged(newConfig)
    }

    override fun initData() {
        super.initData()
        mMainData = DataUtils.getMainData(this)
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        showSplash()
        GlideUtils.loadBlurry(ivBg, Utils.getPic())
        mMainAdapter = MainAdapter()
        tvVersion.text = "当前版本：" + BuildConfig.VERSION_NAME
        mMainAdapter.openLoadAnimation(BaseQuickAdapter.SCALEIN)
        mMainAdapter.isFirstOnly(false)
        mRv.adapter = mMainAdapter
        mMainAdapter.setNewData(mMainData)
        addHeaderView()
        mBannerHeight = DensityUtil.dip2px(this, 180f) - statusBarHeight
        propertiesHelper = PropertiesHelper(this, this::adjustView)
    }

    override fun setListener() {
        super.setListener()
        drawer.addDrawerListener(this)
        mRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var totalDy = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(this@MainActivity).resumeRequests()
                } else {
                    Glide.with(this@MainActivity).pauseRequests()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalDy += dy
                if (totalDy < 0) {
                    totalDy = 0
                }
                if (totalDy < mBannerHeight) {
                    val alpha = totalDy.toFloat() / mBannerHeight
                    mToolbar.alpha = alpha
                } else {
                    mToolbar.alpha = 1f
                }
            }
        })
        mMainAdapter.setOnItemClickListener { adapter: BaseQuickAdapter<*, *>, view: View?, position: Int ->
            val funBean = adapter.data[position] as FunBean
            var intent: Intent? = null
            when (position) {
                0 -> {
                    intent = Intent(this, ParamsActivity::class.java)
                    intent.putExtra("title", funBean.name)
                }
                1 -> {
                    intent = Intent(this, KotlinActivity::class.java)
                    intent.putExtra("title", funBean.name)
                }
                2 -> intent = Intent(this, PicAndColorActivity::class.java)
                3 -> intent = Intent(this, PicActivity::class.java)
                4 -> intent = Intent(this, ColorActivity::class.java)
                5 -> intent = Intent(this, ShapeActivity::class.java)
                6 -> intent = Intent(this, BackActivity::class.java)
                7 -> intent = Intent(this, FragmentActivity::class.java)
                8 -> intent = Intent(this, DialogActivity::class.java)
                9 -> intent = Intent(this, PopupActivity::class.java)
                10 -> drawer.openDrawer(GravityCompat.START)
                11 -> intent = Intent(this, CoordinatorActivity::class.java)
                12 -> intent = Intent(this, TabLayoutActivity::class.java)
                13 -> intent = Intent(this, TabLayout2Activity::class.java)
                14 -> intent = Intent(this, WebActivity::class.java)
                15 -> intent = Intent(this, ActionBarActivity::class.java)
                16 -> intent = Intent(this, FlymeActivity::class.java)
                17 -> intent = Intent(this, OverActivity::class.java)
                18 -> intent = Intent(this, KeyBoardActivity::class.java)
                19 -> intent = Intent(this, AllEditActivity::class.java)
                20 -> intent = Intent(this, LoginActivity::class.java)
                21 -> intent = Intent(this, WhiteBarActivity::class.java)
                22 -> intent = Intent(this, AutoDarkModeActivity::class.java)
                23 -> {
                    immersionBar {
                        hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
                    }
                }
                24 -> {
                    if (hasNavigationBar) {
                        immersionBar {
                            hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
                        }
                    } else {
                        toast("当前设备没有导航栏或者导航栏已经被隐藏或者低于4.4系统")
                    }
                }
                25 -> {
                    immersionBar {
                        hideBar(BarHide.FLAG_HIDE_BAR)
                    }
                }
                26 -> {
                    immersionBar {
                        hideBar(BarHide.FLAG_SHOW_BAR)
                    }
                }
                27 -> {
                    if (hasNavigationBar) {
                        immersionBar {
                            if (fullscreen) {
                                fullscreen = false
                                fullScreen(fullscreen)
                                navigationBarColor(getResColor(R.color.colorPrimary))
                            } else {
                                fullscreen = true
                                fullScreen(fullscreen)
                                transparentNavigationBar()
                            }
                        }
                    } else {
                        toast("当前设备没有导航栏或者导航栏已经被隐藏或者低于4.4系统")
                    }
                }
                28 -> {
                    if (isSupportStatusBarDarkFont) {
                        immersionBar {
                            statusBarDarkFont(true)
                        }
                    } else {
                        toast("当前设备不支持状态栏字体变色")
                    }
                }
                29 -> {
                    immersionBar {
                        statusBarDarkFont(false)
                    }
                }
                else -> {
                }
            }
            intent?.let { startActivity(it) }
        }
    }

    @OnClick(R.id.ll_github, R.id.ll_jianshu)
    fun onClick(view: View) {
        var intent: Intent? = null
        when (view.id) {
            R.id.ll_github -> {
                intent = Intent(this, BlogActivity::class.java)
                val bundle = Bundle()
                bundle.putString("blog", "github")
                intent.putExtra("bundle", bundle)
            }
            R.id.ll_jianshu -> {
                intent = Intent(this, BlogActivity::class.java)
                val bundle2 = Bundle()
                bundle2.putString("blog", "jianshu")
                intent.putExtra("bundle", bundle2)
            }
            else -> {
            }
        }
        intent?.let { startActivity(it) }
    }

    /**
     * 展示Splash
     */
    private fun showSplash() {
        val transaction = supportFragmentManager.beginTransaction()
        var mSplashFragment = supportFragmentManager
            .findFragmentByTag(SplashFragment::class.java.simpleName) as SplashFragment?
        if (mSplashFragment != null) {
            if (mSplashFragment.isAdded) {
                transaction.show(mSplashFragment).commitAllowingStateLoss()
            }
            else {
                transaction.remove(mSplashFragment)
                mSplashFragment = SplashFragment.newInstance()
                transaction
                    .add(R.id.fl_content, mSplashFragment, SplashFragment::class.java.simpleName)
                    .commitAllowingStateLoss()
            }
        } else {
            mSplashFragment = SplashFragment.newInstance()
            transaction
                .add(R.id.fl_content, mSplashFragment, SplashFragment::class.java.simpleName)
                .commitAllowingStateLoss()
        }
        mSplashFragment!!.setOnSplashListener { time: Long, totalTime: Long ->
            if (time != 0L) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
    }

    private fun addHeaderView() {
        addBannerView()
        addNetworkView()
    }

    private fun addBannerView() {
        val bannerView = LayoutInflater.from(this).inflate(R.layout.item_main_banner, mRv, false)
        mIvBanner = bannerView.findViewById(R.id.iv_banner)
        val recyclerView: RecyclerView = bannerView.findViewById(R.id.rv_content)
        ViewUtils.increaseViewHeightByStatusBarHeight(this, mIvBanner)
        setTitleBarMarginTop(recyclerView)

        mLayoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerView.layoutManager = mLayoutManager
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
        mBannerAdapter = BannerAdapter(Utils.getPics())
        recyclerView.adapter = mBannerAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (mBannerPosition != mLayoutManager!!.findFirstVisibleItemPosition()) {
                    mBannerPosition = mLayoutManager!!.findFirstVisibleItemPosition()
                    val data = mBannerAdapter.data
                    val s = data[mBannerPosition % data.size]
                    GlideUtils.loadBlurry(mIvBanner, s)
                }
            }
        })
        mMainAdapter.addHeaderView(bannerView)
    }

    private fun addNetworkView() {
        mNetworkView = LayoutInflater.from(this).inflate(R.layout.item_network, mRv, false)
        if (!Utils.isNetworkConnected(this)) {
            mMainAdapter.addHeaderView(mNetworkView)
        }
    }

    /**
     * 适配刘海屏遮挡数据问题
     * Adjust view.
     *
     * @param barProperties the bar properties,ImmersionBar#setOnBarListener
     */
    private fun adjustView(barProperties: BarProperties) {
        if (barProperties.notchScreen) {
            if (mMainData != null) {
                for (funBean in mMainData!!) {
                    when {
                        barProperties.portrait -> {
                            funBean.marginStart = DensityUtil.dip2px(this, 8f)
                            funBean.marginEnd = DensityUtil.dip2px(this, 8f)
                        }
                        barProperties.landscapeLeft -> {
                            funBean.marginStart = DensityUtil.dip2px(this, 8f) + barProperties.notchHeight
                            funBean.marginEnd = DensityUtil.dip2px(this, 8f)
                        }
                        else -> {
                            funBean.marginStart = DensityUtil.dip2px(this, 8f)
                            funBean.marginEnd = DensityUtil.dip2px(this, 8f) + barProperties.notchHeight
                        }
                    }
                }
            }
            mMainAdapter.notifyDataSetChanged()
        }
    }

    override fun onDrawerSlide(view: View, v: Float) {}
    override fun onDrawerOpened(view: View) {}
    override fun onDrawerClosed(view: View) {
        GlideUtils.loadBlurry(ivBg, Utils.getPic())
    }

    override fun onDrawerStateChanged(i: Int) {}

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
            AppManager.getInstance().removeAllActivity()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNetworkEvent(networkEvent: NetworkEvent) {
        if (mNetworkView != null) {
            if (networkEvent.isAvailable) {
                if (mNetworkView!!.parent != null) {
                    mMainAdapter.removeHeaderView(mNetworkView)
                }
                if (mBannerPosition != -1) {
                    mBannerAdapter.notifyDataSetChanged()
                    val data = mBannerAdapter.data
                    val s = data[mBannerPosition % data.size]
                    GlideUtils.loadBlurry(mIvBanner, s)
                }
            } else {
                if (mNetworkView!!.parent == null) {
                    mMainAdapter.addHeaderView(mNetworkView)
                }
            }
        }
    }
}