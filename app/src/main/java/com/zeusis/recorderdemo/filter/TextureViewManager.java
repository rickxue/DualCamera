package com.zeusis.recorderdemo.filter;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zeusis.recorderdemo.CameraUtil;
import com.zeusis.recorderdemo.MainActivity;
import com.zeusis.recorderdemo.R;


public class TextureViewManager implements IFilterRenderController{
    public static final String TAG = new String("TextureViewManager");

    private static final int FILTER_ANIMATION_DURATION = 250;
    private View mRootView = null;
    private TextureView mTextureView;
    private ImageButton mFilterButton;
    private MainActivity mCameraActivity = null;
    private RelativeLayout mFilterNameLayout;
    //private CaptureLayoutHelper mCaptureLayoutHelper = null;

    public static final int FILTER_EFFECT_ROW = 3;
    public static final int FILTER_EFFECT_COL = 3;
    public static final int FILTER_EFFECT_NUM = 9;
    public static final int ORIGIN_EFFECT_INDEX = 2;

    private float mFilterButtonWidth;
    private float mFilterButtonHeight;

    private boolean mFilterSwitchFlag = false;
    private boolean mRenderCellFilter = false;
    private int mCurrentEffectIndex = ORIGIN_EFFECT_INDEX;
    
    private SurfaceTexture mSurfaceTexture = null;
    private int mWidth = 0, mHeight = 0;
    
    private static String[] sEffectFilterNames = null;
    private ArrayList<String> mEffectNameList = null;
    private static final String[] DATA_COLLECTION_FILTER_NAME = new String[] {
        "black white",
                "warm",
                "lemo",
                "nature",
                "normal",
                "smallfresh",
                "aibao",
                "boots",
                "gorgeous",
    };
    private Rect mFilterEffectRect[][] = new Rect[FILTER_EFFECT_ROW][FILTER_EFFECT_COL];
    private Rect mFilterTextNameRect[][] = new Rect[FILTER_EFFECT_ROW][FILTER_EFFECT_COL];
    
    private int mCameraPictureSizeRatio = 0;
    private int mCameraPreviewSizeRatio = 0;
    
    private int mOffsetTextureViewX = 0;
    private int mOffsetTextureViewY = 0;
    
    private int mFilterCellWidth = 0;
    private int mFilterCellHeight = 0;
    private int mTextCellWidth = 0;
    private int mTextCellHeight = 0;
    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;
    private int mFilterCellGap = 30;
    private int mEffectNameSize = 0;

    private static final double sRatio4to3 = 1.33333f;
    private static final double sRatio16to9 = 1.77778f;
    
    private TextView[][] mFilterNameTextView = new TextView[FILTER_EFFECT_ROW][FILTER_EFFECT_COL];
    
    //filter name orientation
    public final static int ORIENTATION_UNKNOWN = -1;
    public final static int ORIENTATION_LANDSCAPE = 90;
    public final static int ORIENTATION_PORTRAIT = 0;
    public final static int ORIENTATION_LAND_REV = -90;
    public final static int ORIENTATION_PORT_REV = 180;
    
    public int mCurrentOrientation = ORIENTATION_UNKNOWN;

    private HandlerThread mRenderHandlerThread;
    private ThreadHandlerLooper mThreadHandlerLooper;
    
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    

    private EGL10 mEGL;
    private EGLDisplay mEGLDisplay;
    private EGLContext mEGLContext;
    private EGLSurface mEGLSurface;
    
    private SurfaceTexture mOriginalTexture;
    private IFilterEngine mFilterEngine = null;
    private IGLRenderer mGLRenderer  = null;
    private TextureViewAnimationManager mTextureViewAnimationManager = null;
    
    private volatile boolean  mInitiFilterEngine = false;
    private volatile boolean  mInitiEgl = false;
    
    public static final int REQUEST_INIT_GLRENDER = 0;
    public static final int REQUEST_INIT_FILTER = 1;
    public static final int REQUEST_RENDER = 2;
    public static final int REQUEST_DESTROY  = 3;
   
    
    public TextureViewManager(MainActivity activity, View frame) {
        mCameraActivity = activity;
        mRootView = frame;
        initFilterUI();
        intFilterName();
        mTextureViewAnimationManager = new TextureViewAnimationManager(activity,this);
        mFilterButtonWidth = activity.getResources().getDimension(R.dimen.mode_option_width);
        mFilterButtonHeight = activity.getResources().getDimension(R.dimen.mode_option_height);
    }

    private void initFilterUI() {
        mTextureView = (TextureView) mRootView.findViewById(R.id.texture);
        mFilterButton = (ImageButton) mRootView.findViewById(R.id.filter);
        mCameraActivity.setIFilterRenderController(this);
    }

    private void intFilterName(){
        sEffectFilterNames = new String[] {
                mCameraActivity.getString(R.string.effect_filter_blackwhite),
                mCameraActivity.getString(R.string.effect_filter_warm),
                mCameraActivity.getString(R.string.effect_filter_lomo),
                mCameraActivity.getString(R.string.effect_filter_nature),
                mCameraActivity.getString(R.string.effect_filter_normal),
                mCameraActivity.getString(R.string.effect_filter_smallfresh),
                mCameraActivity.getString(R.string.effect_filter_aibao),
                mCameraActivity.getString(R.string.effect_filter_boots),
                mCameraActivity.getString(R.string.effect_filter_gorgeous),
            };
        
        mEffectNameList = new ArrayList<String>();
        for (int i = 0; i < FILTER_EFFECT_NUM; i++) {
            mEffectNameList.add(i, sEffectFilterNames[i]);
        }
        mEffectNameSize = mCameraActivity.getResources().getDimensionPixelSize(R.dimen.filter_text_name_size);
    }

    public String getCurrentFilterName(){
        if(mCameraActivity == null) return null;

        return DATA_COLLECTION_FILTER_NAME[mCurrentEffectIndex];
    }
    
    public ThreadHandlerLooper getThreadHandlerLooper(){
    	return mThreadHandlerLooper;
    }
    
    public void unInitFilter(){
        requestDestroy();
    }

/*    public void setCaptureLayoutHelper(CaptureLayoutHelper helper) {
        mCaptureLayoutHelper = helper;
    }*/

    public MainActivity getCameraActivity(){
        return mCameraActivity;
    }

    /** just init filter name text view when used. */
    private void initFilterNameTextView() {
        for (int i = 0; i < FILTER_EFFECT_ROW; i++) {
            for (int j = 0; j < FILTER_EFFECT_COL; j++) {
                if( mFilterNameTextView[i][j]==null){
                    mFilterNameTextView[i][j] = new FilterNameTextView(mCameraActivity);
                    Rect rect = indexTextFilterCell(i, j);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(rect.right-rect.left,
                            rect.bottom-rect.top);
                   lp.setMargins(rect.left,rect.top, 0, 0);
                    mFilterNameTextView[i][j].setLayoutParams(lp);
                    mFilterNameTextView[i][j].setText(mEffectNameList.get(i * 3 + j));
                    mFilterNameTextView[i][j].setTextSize(TypedValue.COMPLEX_UNIT_PX, mEffectNameSize);
                    //mFilterNameTextView[i][j].setTextAppearance(mCameraActivity, R.style.FilterTextName);
                    ((FilterNameTextView) mFilterNameTextView[i][j]).updatemOrientation(mCurrentOrientation);    
                    mFilterNameLayout.addView(mFilterNameTextView[i][j]);
                }else{
                    updateFilterNameTextView();
                }
            }
        }

        setFilterNameVisible(mFilterSwitchFlag&&mRenderCellFilter);
    }

    private void initFilterNameLayout() {
        if (mFilterNameLayout == null) {
            mFilterNameLayout = new RelativeLayout(mCameraActivity);
            mFilterNameLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mFilterNameLayout.setGravity(RelativeLayout.CENTER_IN_PARENT);
            if (mRootView instanceof ViewGroup) {
                ((ViewGroup) mRootView).addView(mFilterNameLayout);
            }
            initFilterNameTextView();
        }
    }
    
    public void setFilterNameVisible(boolean isVisible) {
        if (mFilterNameLayout == null) {
            initFilterNameLayout();
        }
        if (mFilterNameLayout != null) {
            mFilterNameLayout.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void updateOrientation(int orientation){
        int appOrientation = getAppOrientation(orientation);
        if(appOrientation!=mCurrentOrientation){
            mCurrentOrientation = appOrientation;
            updateFilterNameOrientation(mCurrentOrientation);
        }
    }
    
    private int getAppOrientation(int sysOrientation) {
        int appOrientation = mCurrentOrientation;
        if (sysOrientation >= 320 || sysOrientation < 40) {
            appOrientation = ORIENTATION_PORTRAIT;
        } else if (sysOrientation >= 50 && sysOrientation < 130) {
            appOrientation = ORIENTATION_LAND_REV;
        } else if (sysOrientation >= 140 && sysOrientation < 220) {
            appOrientation = ORIENTATION_PORT_REV;
        } else if (sysOrientation >= 230 && sysOrientation < 310) {
            appOrientation = ORIENTATION_LANDSCAPE;
        }
        return appOrientation;
    }
    
    private void updateFilterNameOrientation(int orientation) {
        for (int i = 0; i < FILTER_EFFECT_ROW; i++) {
            for (int j = 0; j < FILTER_EFFECT_COL; j++) {
                if(mFilterNameTextView[i][j]!=null){
                    ((FilterNameTextView) mFilterNameTextView[i][j]).updatemOrientation(orientation);    
                }
            }
        }
    }

    private void updateFilterNameTextView() {
        for (int i = 0; i < FILTER_EFFECT_ROW; i++) {
            for (int j = 0; j < FILTER_EFFECT_COL; j++) {
                if(mFilterNameTextView[i][j]!=null){
                    Rect rect = indexTextFilterCell(i, j);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(rect.right-rect.left,
                            rect.bottom-rect.top);
                    lp.setMargins(rect.left,rect.top, 0, 0);
                    lp.height = rect.bottom-rect.top;
                    lp.width = rect.right-rect.left;
                    mFilterNameTextView[i][j].setLayoutParams(lp);
                }
            }
        }
    }

    public boolean getFilterEffectFlag() {
        return mFilterSwitchFlag;
    }
    
    public int getCameraPictureSizeRatio(){
        return mCameraPictureSizeRatio;
    }

    public void switchFilterEffect() {
        Log.i(TAG,"switchFilterEffect");
        //first open filter,init filter engine.
        //requestInitFilter();
        if (mCurrentEffectIndex != ORIGIN_EFFECT_INDEX) {
            if (mRenderCellFilter) {
                mRenderCellFilter = false;
                mFilterSwitchFlag = true;
                mTextureViewAnimationManager.stratFilterAnimation(FILTER_ANIMATION_DURATION);
            //    mCameraActivity.getCameraAppUI().onAllFilterChange(false);
            } else {
                mFilterSwitchFlag = true;
                mRenderCellFilter = true;
                mTextureViewAnimationManager.stratFilterAnimation(FILTER_ANIMATION_DURATION);
            //    mCameraActivity.getCameraAppUI().onAllFilterChange(true);
            }
        }else{//default effect
            if(mRenderCellFilter){
                mFilterSwitchFlag = false;
                mRenderCellFilter = false;
                mTextureViewAnimationManager.stratFilterAnimation(FILTER_ANIMATION_DURATION);
             //   mCameraActivity.getCameraAppUI().onAllFilterChange(false);
            }else{
                mFilterSwitchFlag = true;
                mRenderCellFilter = true;
                mTextureViewAnimationManager.stratFilterAnimation(FILTER_ANIMATION_DURATION);
              //  mCameraActivity.getCameraAppUI().onAllFilterChange(true);
            }
        }
/*        if(CameraFeature.isOpenDataCollection() && mRenderCellFilter){
            UsageStatistics.instance().buttonFilter();
        }*/
    }

    public void initTextureView(){
/*        SettingsManager settingsManager = mCameraActivity.getSettingsManager();
        mCurrentEffectIndex = settingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_FILTER_EFFECT_INDEX);*/
        if(mCurrentEffectIndex != ORIGIN_EFFECT_INDEX){
            mFilterSwitchFlag = true;
            if(mFilterButton != null){
                //mFilterButton.setImageResource(R.drawable.ic_settings_filter_on);
            }
        }else{
            mFilterSwitchFlag = false;
            if(mFilterButton != null){
                //mFilterButton.setImageResource(R.drawable.ic_settings_filter);
            }
        }
        mRenderCellFilter = false;
    }

    public void resetTextureView() {
        mFilterSwitchFlag = false;
        mCurrentEffectIndex = ORIGIN_EFFECT_INDEX;
        mRenderCellFilter = false;
//        mCameraActivity.getCameraAppUI().onAllFilterChange(false);
    }

    public boolean chooseOneFilterOnTouch(int x, int y){
        int id = getSelectedEffectId(x, y);
        if(id == -1){
            return false;
        }
        Log.i(TAG,"chooseOneFilterOnTouch");
        mCurrentEffectIndex = id;
        if (mCurrentEffectIndex != ORIGIN_EFFECT_INDEX) {
            if(mRenderCellFilter){
                mRenderCellFilter = false;
                mFilterSwitchFlag = true;
            }
            //mFilterButton.setImageResource(R.drawable.ic_settings_filter_on);
        }else{
            if(mRenderCellFilter){
                mRenderCellFilter = false;
                mFilterSwitchFlag = false;
            }
            //mFilterButton.setImageResource(R.drawable.ic_settings_filter);
        }
 /*       mCameraActivity.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_FILTER_EFFECT_INDEX, mCurrentEffectIndex);
        mTextureViewAnimationManager.stratFilterAnimation(FILTER_ANIMATION_DURATION);
        mCameraActivity.getCameraAppUI().onAllFilterChange(false);*/
        return true;
    }

    public void pauseFilterManager(){
        if(mRenderCellFilter) {
            mRenderCellFilter = false;
            //mCameraActivity.getCameraAppUI().onAllFilterChange(false);
            setFilterNameVisible(false);
        }
    }

    public boolean onTouch(MotionEvent event) {
        boolean isDone = false;
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                isDone = chooseOneFilterOnTouch((int)event.getX(), (int)event.getY());
                break;
        }
        return isDone;
    }

    public boolean isRenderCellFilter() {
        return mRenderCellFilter;
    }
    
    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }
    
    private void initFilterCellSize(){
/*        mCameraPictureSizeRatio = mCameraActivity.getSettingsManager().getIndexOfCurrentValue(
                mCameraActivity.getCameraScope(), Keys.KEY_CAMERA_PICTURE_SIZE_RATIO);*/
        Log.i(TAG, "initFilterCellSize  mCameraPictureSizeRatio:" + mCameraPictureSizeRatio);
        mFilterCellWidth = (mSurfaceWidth - (FILTER_EFFECT_COL - 1) * mFilterCellGap) / FILTER_EFFECT_COL;
        mFilterCellHeight = (mSurfaceHeight - (FILTER_EFFECT_ROW - 1) * mFilterCellGap) / FILTER_EFFECT_ROW;
        if(mCameraPreviewSizeRatio == 0){
            mTextCellWidth = (mWidth - (FILTER_EFFECT_COL - 1) * mFilterCellGap) / FILTER_EFFECT_COL;
            mTextCellHeight = (mHeight - (FILTER_EFFECT_ROW - 1) * mFilterCellGap) / FILTER_EFFECT_ROW;
        }else{
            mTextCellWidth = (mSurfaceWidth - (FILTER_EFFECT_COL - 1) * mFilterCellGap) / FILTER_EFFECT_COL;
            mTextCellHeight = (mSurfaceHeight - (FILTER_EFFECT_ROW - 1) * mFilterCellGap) / FILTER_EFFECT_ROW;
        }
    }
    
    private void initFilterEffectRect() {
        int filterWidth = mFilterCellWidth;
        int filterHeight = mFilterCellHeight;
        for (int i = 0; i < FILTER_EFFECT_ROW; i++) {
            for (int j = 0; j < FILTER_EFFECT_COL; j++) {
                mFilterEffectRect[i][j] = new Rect(i * filterWidth, j * filterHeight, (i + 1) * filterWidth,
                        (j + 1) * filterHeight);
            }
        }
        
        //offset rect
        mOffsetTextureViewX = mTextureView.getLeft();
        mOffsetTextureViewY = mTextureView.getTop();
        for (int i = 0; i < FILTER_EFFECT_ROW; i++) {
            for (int j = 0; j < FILTER_EFFECT_COL; j++) {
                mFilterEffectRect[i][j].offset(mOffsetTextureViewX+i*mFilterCellGap,
                        mOffsetTextureViewY+j*mFilterCellGap);
            }
        }
        printFilterEffectRect();
    }

    private void initFilterTextNameRect() {
        int filterWidth = mTextCellWidth;
        int filterHeight = mTextCellHeight;
        for (int i = 0; i < FILTER_EFFECT_ROW; i++) {
            for (int j = 0; j < FILTER_EFFECT_COL; j++) {
                mFilterTextNameRect[i][j] = new Rect(i * filterWidth, j * filterHeight, (i + 1) * filterWidth,
                        (j + 1) * filterHeight);
            }
        }

        //offset rect
        int topMargin = 0;
        //if(mCaptureLayoutHelper != null) {
            topMargin = 0;//(int)mCaptureLayoutHelper.getPreviewRect().top;
       // }
        Log.i(TAG, "initFilterTextRect topMargin:"+topMargin);
        mOffsetTextureViewX = mTextureView.getLeft();
        mOffsetTextureViewY = mTextureView.getTop();
        for (int i = 0; i < FILTER_EFFECT_ROW; i++) {
            for (int j = 0; j < FILTER_EFFECT_COL; j++) {
                mFilterTextNameRect[i][j].offset(mOffsetTextureViewX+i*mFilterCellGap,
                        mOffsetTextureViewY+j*mFilterCellGap+topMargin);
            }
        }
        //printFilterTextNameRect();
    }
    
    private void printFilterEffectRect(){
        for (int i = 0; i < FILTER_EFFECT_ROW; i++) {
            for (int j = 0; j < FILTER_EFFECT_COL; j++) {
                Log.i(TAG, "mFilterEffectRect["+i+"]["+j+"] :"+mFilterEffectRect[i][j]);
            }
        }
    }

    private void printFilterTextNameRect(){
        for (int i = 0; i < FILTER_EFFECT_ROW; i++) {
            for (int j = 0; j < FILTER_EFFECT_COL; j++) {
                Log.i(TAG, "mFilterTextNameRect["+i+"]["+j+"] :"+mFilterTextNameRect[i][j]);
            }
        }
    }
    
    public int getSelectedEffectId(int x, int y) {
        //handle filter button first
        if(new Rect((CameraUtil.getScreenWidth()-(int)mFilterButtonWidth*2),0,
                CameraUtil.getScreenWidth(),(int)mFilterButtonHeight).contains(x,y)){
            return -1;
        }
        for (int i = 0; i < FILTER_EFFECT_ROW; i++) {
            for (int j = 0; j < FILTER_EFFECT_COL; j++) {
                boolean isContains = mFilterTextNameRect[i][j].contains(x, y);
                if (isContains) {
                    return (i * FILTER_EFFECT_ROW + j);
                }
            }
        }
        return -1;
    }

    public Rect indexTextFilterCell(int x, int y){
        Rect rect = new Rect();
        rect.set(mFilterTextNameRect[x][y]);
        return rect;
    }
    
    public Rect indexFilterCellInFilter(int x, int y){
        Rect rect = new Rect();
        rect.set(mFilterEffectRect[x][y]);
        rect.offset(-mOffsetTextureViewX, -mOffsetTextureViewY);
        return rect;
    }
    
    @Override
    public void destoryTexture() {
        // TODO Auto-generated method stub
            requestDestroy();
    }

    @Override
    public void createTexture(SurfaceTexture surfaceTexture, int width, int height) {
        // TODO Auto-generated method stub
        mOriginalTexture = surfaceTexture;
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        mWidth = width;
        mHeight = height;
        Log.i(TAG, "createTexture surfaceTexture width:" + width + ", height:" + height);
        mGLRenderer = new GLRendererImpl(this, width, height);
        
        mRenderHandlerThread = new HandlerThread("render_handler_thread", Thread.MAX_PRIORITY);
        mRenderHandlerThread.start();
        mThreadHandlerLooper = new ThreadHandlerLooper(mRenderHandlerThread.getLooper());

        requestInitGLRender();
        requestInitFilter();

        initFilterCellSize();
        initFilterEffectRect();
        initFilterTextNameRect();
//        initFilterNameTextView();
        setFilterNameVisible(mFilterSwitchFlag && mRenderCellFilter);
        while (mGLRenderer.getPreviewTexture() == null) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        mSurfaceTexture = getPreviewTexture();
    }

    public int getCurrentEffectIndex() {
        return mCurrentEffectIndex;
    }

    public int getCurrentEffectValueForParameter() {
        if(mCurrentEffectIndex < FILTER_EFFECT_NUM && mCurrentEffectIndex >= 0){
            return FilterUtil.indexEffectStyleId(mCurrentEffectIndex);
        }
        return FilterUtil.indexEffectStyleId(ORIGIN_EFFECT_INDEX);
    }

    @Override
    public void changeTextureSize(int width, int height) {
        Log.i(TAG, "changeTextureSize width:" + width + ", height:" + height);
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        
        if (mGLRenderer != null) {
            mGLRenderer.changeTextureSize(width, height);
        }
        initFilterCellSize();
        initFilterEffectRect();
        initFilterTextNameRect();
        updateFilterNameTextView();
    }

    public void resizeFilter(int width, int height) {
        Log.i(TAG, "resizeFilter width:" + width + ", height:" + height);

        if(CameraUtil.isPreviewRatioEquals(sRatio4to3, width, height)){
            //4:3
            mCameraPreviewSizeRatio = 0;
        } else {
            //16:9
            mCameraPreviewSizeRatio = 1;
        }
        initFilterCellSize();
        initFilterEffectRect();
        initFilterTextNameRect();
        updateFilterNameTextView();
        updateOrientation(mCurrentOrientation);
    }

    protected void initEglEnv() {
        Log.d(TAG, "TextureViewManager initEglEnv");
        mEGL = (EGL10) EGLContext.getEGL();
        mEGLDisplay = mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetdisplay failed : "
                    + GLUtils.getEGLErrorString(mEGL.eglGetError()));
        }

        int[] version = new int[2];
        if(!mEGL.eglInitialize(mEGLDisplay, version)){
            throw new RuntimeException("eglInitialize failed : "
                    + GLUtils.getEGLErrorString(mEGL.eglGetError()));
        }

        EGLConfig eglConfig = LetvGlApi.getEglConfig(mEGL, mEGLDisplay);
        Log.i(TAG,"xwf eglConfig:"+eglConfig);
        int[] attribList = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };

        mEGLContext = mEGL.eglCreateContext(mEGLDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attribList);

        mEGLSurface = mEGL.eglCreateWindowSurface(mEGLDisplay, eglConfig, mOriginalTexture, null);
        if (mEGLSurface == null || mEGLSurface == EGL10.EGL_NO_SURFACE
                || mEGLContext == EGL10.EGL_NO_CONTEXT) {
            int error = mEGL.eglGetError();
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                throw new RuntimeException("eglCreateWindowSurface returned  EGL_BAD_NATIVE_WINDOW. " );
            }
            throw new RuntimeException("eglCreateWindowSurface failed : "
                    + GLUtils.getEGLErrorString(mEGL.eglGetError()));
        }
        if (!mEGL.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext))
            throw new RuntimeException("eglMakeCurrent failed: " + GLUtils.getEGLErrorString(mEGL.eglGetError()));
    }
    
    public void setDisplaySize(int width, int height) {
        mGLRenderer.changeTextureSize(width, height);
    }

    public SurfaceTexture getPreviewTexture() {
        return mGLRenderer.getPreviewTexture();
    }
    
    protected void initFilterEngine(){
        Log.d(TAG, "TextureViewManager   initFilterEngine");
        mFilterEngine  = FilterFactory.getFilterEngine();
        if(mFilterEngine!=null){
            mFilterEngine.initFilter();
        }
    }
    
    private void detroyFilterEngine(){
        if(mFilterEngine!=null){
            mFilterEngine.destroyFilter();
            mFilterEngine = null;
        }
    }

    protected void deinitGL() {
        Log.d(TAG, "TextureViewManager   deinitGL");
        mGLRenderer.destory();
        mEGL.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        mEGL.eglDestroySurface(mEGLDisplay, mEGLSurface);
        mEGL.eglDestroyContext(mEGLDisplay, mEGLContext);
        mEGL.eglTerminate(mEGLDisplay);
    }    
    
    public void renderFilterEffectWithFbo(int effectId, int texId, Rect texRect, int texW, int texH, int fboId, Rect fboRect, int fboW, int fboH,float[] matrix){
        if(mFilterEngine!=null){
            mFilterEngine.renderFilter(effectId, texId, texRect, texW, texH, fboId, fboRect, fboW, fboH, matrix);
        }
    }

    public class ThreadHandlerLooper extends Handler {

        public ThreadHandlerLooper(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case REQUEST_INIT_GLRENDER:
                initGLRender();
                break;
            case REQUEST_INIT_FILTER:
                initFilter();
                break;
            case REQUEST_RENDER:
                render();
                break;
            case REQUEST_DESTROY:
                destroy();
                break;
            }
        }
    }
    
    private void initGLRender(){
        if(!mInitiEgl) {
            initEglEnv();
            mGLRenderer.create();
            mInitiEgl = true;
        }else{
            Log.e(TAG,"initGLRender again");
        }
    }
    
    private void initFilter(){
        if(!mInitiFilterEngine && mInitiEgl){
            initFilterEngine();
            mInitiFilterEngine = true;
        }else{
            Log.e(TAG,"initFilter fail mInitiFilterEngine="+mInitiFilterEngine+", mInitiEgl:"+mInitiEgl);
        }
    }
    
    public boolean isInitiFilterEngine(){
    	return mInitiFilterEngine&&mInitiEgl;
    }

    private void render(){
        Long start = System.currentTimeMillis();
        Long drawTime=-1L;
        if (mGLRenderer.drawFrame()) {
            drawTime = (System.currentTimeMillis()-start);
            mEGL.eglSwapBuffers(mEGLDisplay, mEGLSurface);
        }else{
            Log.i(TAG,"render  else");
        }
        Long time = (System.currentTimeMillis()-start);
        if(time>10) {
            Log.i(TAG, "render time:" + time+",  drawFrameTime:"+drawTime+",  swap time:"+(time-drawTime));
        }
    }
    
    private void destroy() {
        deinitGL();
        detroyFilterEngine();
        mInitiFilterEngine = false;
        mInitiEgl = false;
        mThreadHandlerLooper.getLooper().quit();
        mSurfaceTexture = null;
        mThreadHandlerLooper = null;
    }

    public void requestInitGLRender() {
        Message message = mThreadHandlerLooper.obtainMessage();
        message.what = REQUEST_INIT_GLRENDER;
        mThreadHandlerLooper.sendMessage(message);
    }

    public void requestInitFilter() {
        Message message = mThreadHandlerLooper.obtainMessage();
        message.what = REQUEST_INIT_FILTER;
        mThreadHandlerLooper.sendMessage(message);
    }

    public void requestRender() {
        if (mThreadHandlerLooper != null) {
            Message message = mThreadHandlerLooper.obtainMessage();
            message.what = REQUEST_RENDER;
            mThreadHandlerLooper.sendMessage(message);
        }
    }

    public void requestDestroy() {
        if(mThreadHandlerLooper != null) {
            Message message = mThreadHandlerLooper.obtainMessage();
            message.what = REQUEST_DESTROY;
            mThreadHandlerLooper.sendMessage(message);

            if(mRenderHandlerThread != null){
                try {
                    mRenderHandlerThread.quitSafely();
                    mRenderHandlerThread.join();
                }catch (Exception e){
                    e.printStackTrace();
                }
                mRenderHandlerThread = null;
            }
        }
    }
    
    public TextureViewAnimationManager getTextureViewAnimationManager(){
    	return mTextureViewAnimationManager;
    }
   
}