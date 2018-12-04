package com.unityexport.ian.unitylibrary;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

public class UnityGeckoView extends GeckoView{


    public UnityGeckoView(Context context) {
        super(context);
    }
    public UnityGeckoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public MainGL mainGL;
    public GeckoSession session;
    public GeckoRuntime runtime;
    public boolean canGoBack(){
        return true;
    }
    public boolean canGoForward() {
        return true;
    }
    boolean shouldBeDrawing = true;

    // default constructors
//   public void init(android.content.Context context){
//       session = new GeckoSession();
//       runtime = GeckoRuntime.create(context);
//
//       session.open(runtime);
//       setSession(session);
//   }

   public void loadUrl(String url){
       session.loadUri(url);

   }
   public void goBack(){
       session.goBack();
   }
   public void goForward(){
       session.goForward();
   }

   public void reload(){
       session.reload();
   }

    // so we can add an on touch listener
    @Override
    public boolean performClick(){
        super.performClick();
//        Log.d("AndroidUnity","performing click!");
        return false;
    }


    @Override
    public void draw( Canvas canvas ) {
        Log.d("AndroidUnity", "canvas draw called!");
        Log.d("AndroidUnity", "should be drawing is: "+ shouldBeDrawing);

        if (shouldBeDrawing) {
            super.draw(mainGL.bigcanvas);
            mainGL.convertWebviewToBitmapForUnity();
        }
    }
}
