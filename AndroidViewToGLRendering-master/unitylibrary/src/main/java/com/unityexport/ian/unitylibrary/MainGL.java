package com.unityexport.ian.unitylibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
//import com.self.viewtoglrendering.cuberenerer.CubeGLRenderer;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;

import java.io.ByteArrayOutputStream;

public class MainGL extends Fragment {


//    private BitmapWebView mWebView;
    private UnityGeckoView mWebView;

    PluginInterfaceBitmap UnityBitmapCallback;
    static String FRAGMENT_TAG = "AndroidUnityMainGL";
    View mView;
    ByteArrayOutputStream stream;
    ReadData array;
    Bitmap bm ;
    Canvas bigcanvas ;
    boolean canGoBack;
    boolean canGoForward;
    boolean shouldRestartWebview;

    // these values changed by unity:
    int outputWindowHeight = 500;
    int outputWindowWidth = 700;


    KeyCharacterMap CharMap;



    // To Call From Unity at start
     public static MainGL CreateInstance()
    {
        Log.i("AndroidUnity", "Hi, new instance created");
        // create the new fragment
        MainGL newMainGL = new MainGL();
        newMainGL.setRetainInstance(true);
        // add fragment for callbacks
        AddFragment(newMainGL);

        return newMainGL;
    }

    public void SetOutputWindowSizes(int width, int height){
        outputWindowHeight = height;
        outputWindowWidth= width;
    }

    // not tested yet
//    public void Zoom(boolean in){
//         if (in)
//            mWebView.zoomIn();
//         else
//             mWebView.zoomOut();
//    }


    // used by unity when canvas is enabled, you must call this yourself
    public void SetShouldDraw(boolean drawing){
         if (mWebView== null) {
             Log.d("AndroidUnity","webview cannot be set to should be drawing just yet, it's null");
             return;
         }
      mWebView.shouldBeDrawing= drawing;
    }

    public void SetUnityBitmapCallback(PluginInterfaceBitmap callback){
        Log.d("AndroidUnity","from android: my unity callback is set");
        UnityBitmapCallback = callback;
    }

    // accounts for the webview's scroll Y
    public void AddTap(final float x, float y){

//        Log.d("AndroidUnity","Add tap called! at x:" + x + " y: "+y);

        if (mWebView != null){
            // going down is negative in scroll but positive in coordinate system
            y-= mWebView.getScrollY();
            final float finalY = y;
             Log.d("AndroidUnity","Add tap called! at x:" + x + " y: "+finalY);

            if (getView().findFocus() != mWebView) {
                mWebView.setFocusable(true);
                mWebView.setFocusableInTouchMode(true);
                mWebView.requestFocus();

               // Log.d("AndroidUnity","view focused");
            }
            UnityPlayer.currentActivity.runOnUiThread(new Runnable() {public void run() {
                if (mWebView == null) {
                    return;
                }
                // Obtain MotionEvent object
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis() + 50;
                int source = InputDevice.SOURCE_CLASS_POINTER;

// List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
                int metaState = 0;
                MotionEvent event = MotionEvent.obtain(
                        downTime,
                        eventTime,
                        MotionEvent.ACTION_DOWN,
                        x,
                        finalY,
                        metaState
                );
                event.setSource(source);

// Dispatch touch event to view
                mWebView.dispatchTouchEvent(event);

                event = MotionEvent.obtain(
                        downTime,
                        eventTime,
                        MotionEvent.ACTION_UP,
                        x,
                        finalY,
                        metaState
                );
                event.setSource(source);
                mWebView.dispatchTouchEvent(event);
            }
            });

         }
    }

    public void AddKeys(String text, boolean functionKey){
         // if we are calling via ASCII code for function keys:
         if (functionKey){
             int keyCode = KeyEvent.KEYCODE_DPAD_LEFT;

             if (text.equals("right")) {
                 keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
             }
             if (text.equals("backspace")) {
                 keyCode = KeyEvent.KEYCODE_DEL;
             }
             Log.d("AndroidUnity", "function text is: " + text + " and code is: "+ keyCode );
             KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN,keyCode);
             mWebView.dispatchKeyEvent(event);
             return;
         }
         // otherwise we are just adding text
         char[] szRes = text.toCharArray(); // Convert String to Char array

        KeyEvent[] events = CharMap.getEvents(szRes);

        for(int i=0; i<events.length; i++)
            mWebView.dispatchKeyEvent(events[i]); // MainWebView is webview
    }

//    public void StopWebview(){
//        final Activity a = UnityPlayer.currentActivity;
//        a.runOnUiThread(new Runnable() {public void run() {
//            if (mWebView == null) {
//                return;
//            }
//            mWebView.stopLoading();
//        }});
//
//    }

    public void Scroll( final int yScrollBy){
        final Activity a = UnityPlayer.currentActivity;
        a.runOnUiThread(new Runnable() {public void run() {
            if (mWebView == null) {
                return;
            }
        mWebView.measure(View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            int measuredHeight = mWebView.getMeasuredHeight();
            int currentScroll = mWebView.getScrollY();
            int maxHeight = mWebView.getHeight();
            if (measuredHeight != maxHeight){
                mWebView.layout(0, 0, outputWindowWidth, measuredHeight);
            }
            if (measuredHeight > maxHeight){
                maxHeight = measuredHeight;
            }

            Log.d("AndroidUnity", "webview is scrolled to: " + currentScroll);
            // get whichever height is higher and set that to our max scroll height



            Log.d("AndroidUnity", "webview has height : " + mWebView.getHeight());
            Log.d("AndroidUnity", "and measured height is:" + measuredHeight);

            if (currentScroll + yScrollBy > 0 && yScrollBy > 0 ) {
                 Log.d("AndroidUnity", "rejecting scroll at: " + currentScroll);
                 Log.d("AndroidUnity", "scrolling back to top" );
                 mWebView.scrollTo(0,0);
                 // if we're too far up, go back to beginning
                 return;
             }

             // don't go too far down (3000 - 1200 - 600 ) < 600
             else if(maxHeight + currentScroll + yScrollBy < Math.abs(yScrollBy)) {
                Log.d("AndroidUnity", "maybe rejecting scroll at: " + currentScroll);
                // current scroll down will be negative
                if (-maxHeight != currentScroll) {
                    mWebView.scrollBy(0,-maxHeight +(-currentScroll -yScrollBy));
                    Log.d("AndroidUnity", "webview is now scrolled to: " + currentScroll);

                }
                return;
             }

            mWebView.scrollBy(0,yScrollBy);
            Log.d("AndroidUnity", "webview is now scrolled to: " + currentScroll);


        }});


    }

    private void initBitmapStuff(){
         stream = new ByteArrayOutputStream();
         array = new ReadData(new byte[]{});
         bm = Bitmap.createBitmap(outputWindowWidth,
                 outputWindowHeight, Bitmap.Config.ARGB_8888);
         bigcanvas = new Canvas(bm);
        Log.d("AndroidUnity","re initing bitmap stuff, shouldn't see this too often");

    }

    public void setcanGoBack(boolean truth){
        canGoBack=truth;
    }

    public void setCanGoForward(boolean truth){
        canGoForward = truth;
    }


    public void convertWebviewToBitmapForUnity(){
//        Log.d("AndroidUnity","taking a picture!");

        // we have to re init views from this thread so when you call restart webview
        // you also have to get the webview to re-draw itself, i.e. scroll or load
        if (shouldRestartWebview) {
            shouldRestartWebview = false;
            initViews(mView);
        }

        if (bm.getHeight() != outputWindowHeight)
            initBitmapStuff();

        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                mWebView.SetImage(bm); // IMAGEME
        array.Buffer = stream.toByteArray();
        UnityBitmapCallback.onFrameUpdate(array,
                bm.getWidth(),
                bm.getHeight(),
                canGoBack,
                canGoForward );
        stream.reset();
    }


    public void LoadURL(final String url) {
        final Activity a = UnityPlayer.currentActivity;
        a.runOnUiThread(new Runnable() {public void run() {
            if (mWebView == null) {
                return;
            }

            mWebView.loadUrl(url);;

        }});
    }
    public void Refresh() {
        final Activity a = UnityPlayer.currentActivity;
        a.runOnUiThread(new Runnable() {public void run() {
            if (mWebView == null) {
                return;
            }
            mWebView.reload();

        }});
    }

    public void GoBack() {
        final Activity a = UnityPlayer.currentActivity;
        a.runOnUiThread(new Runnable() {public void run() {
            if (mWebView == null) {
                return;
            }
            if ( mWebView.canGoBack()) {
                mWebView.goBack();
            }
        }});
    }

    public void GoForward() {
        final Activity a = UnityPlayer.currentActivity;
        a.runOnUiThread(new Runnable() {public void run() {
            if (mWebView == null) {
                return;
            }
            if (mWebView.canGoForward()) {
                mWebView.goForward();
            }
        }});
    }
    public boolean canGoBack(){
         return mWebView.canGoBack();
    }
    public boolean canGoForward(){
        return mWebView.canGoForward();
    }

    // we have to re init views from this thread so when you call restart webview
    // you also have to get the webview to re-draw itself, i.e. scroll or load
    public void RestartWebview(){
        shouldRestartWebview =true;
    }

    //end unity

    public void initViews(View view) {

         // useful for adding keys from unity to the webview
        CharMap = KeyCharacterMap.load(KeyCharacterMap.ALPHA);

        if (bm == null)
            initBitmapStuff();

        // testing geckoview
        mWebView = (UnityGeckoView) view.findViewById(R.id.geckoview);

        mWebView.session = new GeckoSession();
        mWebView.runtime = GeckoRuntime.create(getContext());

        mWebView.session.open(mWebView.runtime);
        mWebView.setSession(mWebView.session);

        ViewGroup.LayoutParams params = mWebView.getLayoutParams();
        params.width = outputWindowWidth;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mWebView.setLayoutParams(params);
        mWebView.mainGL=this;
        mWebView.setDrawingCacheEnabled(true);

        mWebView.setVisibility(View.VISIBLE);
        mWebView.setAlpha(0.0f);
        mWebView.setVerticalScrollBarEnabled(true);

//        GeckoSessionSettings webSettings = mWebView.getSession().getSettings();


        /*


        // if we're re-initing the view, destroy the old one
        if (mWebView != null) {
            RelativeLayout r = (RelativeLayout) mWebView.getParent();
            Log.d("AndroidUnity","removing webview now!");
            int index = r.indexOfChild(mWebView);
            r.removeViewAt(index);
            // re inflate the webview and add it
            mWebView = (BitmapWebView) View.inflate(this.getContext(),R.layout.extralayouts,null);
            r.addView(mWebView, index);
        }
        else {
            mWebView = (BitmapWebView) view.findViewById(R.id.web_view);
        }

        mWebView.mainGL = this;
        mWebView.setDrawingCacheEnabled(true);

//        ImageView imageView = (ImageView) view.findViewById(R.id.image_view); // IMAGEME
//        mWebView.setImageViewer(imageView); // IMAGEME

     // reinforcing the layout params
        ViewGroup.LayoutParams params = mWebView.getLayoutParams();
        params.width = outputWindowWidth;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mWebView.setLayoutParams(params);
        mWebView.setLongClickable(false);
        // Changes the height and width to the specified *pixels*
        mWebView.setVisibility(View.VISIBLE);
        mWebView.setAlpha(0.0f);
        mWebView.setVerticalScrollBarEnabled(true);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setDomStorageEnabled(true);


        //Add Keyboard Listener to call unity method
        mWebView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WebView.HitTestResult hr = ((BitmapWebView) v).getHitTestResult();
                Log.d("AndroidUnity","on touch called!");
                Log.d("AndroidUnity","scroll " + mWebView.getScrollY());
                Log.d("AndroidUnity","height "+ mWebView.getHeight());
                Log.d("AndroidUnity","content height "+ mWebView.getContentHeight());


                // this would in theory be nice to show the user a keyboard in unity
                // but it doesn't work in practice
                int resultType = hr.getType();
                switch (resultType){
                    case WebView.HitTestResult.EDIT_TEXT_TYPE:
                        Log.d("AndroidUnity","editing text!");
                        UnityBitmapCallback.SetKeyboardVisibility("true");
                        break;
//                    case WebView.HitTestResult.SRC_ANCHOR_TYPE:
//                        Log.d("AndroidUnity","link hit: " + hr.toString());
//                        break;
//                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
//                        Log.d("AndroidUnity","image link hit: " + hr.toString());
//                        break;
                    default:
//                        Log.d("AndroidUnity","default (multitude of other types) clicked");
                        Log.d("AndroidUnity", "type is: "+resultType);
                        break;
                }
                return false;


            }
        });

        // ignore long clicks
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("AndroidUnity","long clicked!");
                return true;
            }
        });

        // I don't think this works properly yet
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
                final String errorMessage = "Webview error: "+ error.getErrorCode()+ "\t" + error.getDescription() + "\t" + request.getUrl();
                Log.d("AndroidUnity", errorMessage);
                final Activity a = UnityPlayer.currentActivity;
                a.runOnUiThread(new Runnable() {public void run() {
                    String summary = "<html><body>" +errorMessage+"</body></html>";
                    mWebView.loadData(summary, "text/html; charset=utf-8", "UTF-8");
                }});
            }

            // Don't allow users to go to the google play store in the Oculus
            @Override
            public boolean shouldOverrideUrlLoading (WebView view,
                                                     WebResourceRequest request){
                String url = request.getUrl().toString();
                if (url.startsWith("http")) return false;
                else if (url.startsWith("market") || url.startsWith("intent")){
                    mWebView.loadUrl(url);
                }
                return true;
            }


        });


        // Update unity on our progress
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 100) {
                    // scroll to the top
                    mWebView.scrollTo(0, 0);
                    // update unity's url
                    if (UnityBitmapCallback != null )
                        try {
                            UnityBitmapCallback.updateURL(mWebView.getUrl());
                        } catch (Exception e){
                            Log.d("AndroidUnity", "Webview url isn't returning a string yet");
                            Log.d("AndroidUnity", "error is:" + e.getMessage());
                        }
                    // set can go back or forward
                    final Activity a = UnityPlayer.currentActivity;
                    a.runOnUiThread(new Runnable() {public void run() {
                        setcanGoBack( mWebView.canGoBack());
                    }});
                    a.runOnUiThread(new Runnable() {public void run() {
                        setCanGoForward( mWebView.canGoForward());
                    }});
                }
                // update unity's progress text
                if (UnityBitmapCallback != null )
                    UnityBitmapCallback.updateProgress(newProgress, canGoBack, canGoForward);
                super.onProgressChanged(view, newProgress);
            }
        });

        */
        Log.d("AndroidUnity","webview init-ed!");
    }


// utility stuff
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retain between configuration changes (like device rotation)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        Log.d("AndroidUnity","On Create View!");
        WebView.enableSlowWholeDocumentDraw();
        BitmapWebView.enableSlowWholeDocumentDraw();


        // can use this to not show window
//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.ADJUST_NOTHING|     WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mView =  inflater.inflate(R.layout.fragment_layout, parent, false);
        Log.d("AndroidUnity","On inflate  View!");

        initViews(mView);
        Log.d("AndroidUnity","On finish Create View + initViews!");

        return mView;
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
        Log.d("AndroidUnity","On View Created!");


    }

    @SuppressLint("ResourceType")
    private static void AddFragment(Fragment fragment){
        ViewGroup rootView = (ViewGroup)UnityPlayer.currentActivity.findViewById(android.R.id.content);

        // find the first leaf view (i.e. a view without children)
        // the leaf view represents the topmost view in the view stack
        View topMostView = getLeafView(rootView);

        if (topMostView != null) {

            // let's add a sibling to the leaf view
            ViewGroup leafParent = (ViewGroup)topMostView.getParent();

            if (leafParent != null) {
                leafParent.setId(0x20348);

                // fragment = new WebViewFragment();
                FragmentManager fragmentManager = UnityPlayer.currentActivity.getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(leafParent.getId(), fragment, FRAGMENT_TAG);
                fragmentTransaction.commit();
            }
        }
    }

    private static View getLeafView(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup)view;
            for (int i = 0; i < vg.getChildCount(); ++i) {
                View chview = vg.getChildAt(i);
                View result = getLeafView(chview);
                if (result != null)
                    return result;
            }
            return null;
        }
        else {
            return view;
        }
    }



}
