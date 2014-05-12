package com.minimv.senselockscreen;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
//import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

public class XposedResHooks implements IXposedHookZygoteInit, IXposedHookInitPackageResources {

	//private static String MODULE_PATH = null;
	private XSharedPreferences prefs;
	private boolean panelAlignBottom;
	private String hintText;
	
	public XposedResHooks() {
		prefs = new XSharedPreferences("com.minimv.senselockscreen");
	}

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        //MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
	    if (!resparam.packageName.equals("com.htc.lockscreen"))
	        return;
	    
	    resparam.res.hookLayout("com.htc.lockscreen", "layout", "main_lockscreen_keyguard_widget_pager", new XC_LayoutInflated() {
	        @Override
	        public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				View widgetPager = (View) liparam.view;
				widgetPager.setPadding(0, widgetPager.getPaddingTop(), 0, widgetPager.getPaddingBottom() - 81);
	        }
	    }); 

	    resparam.res.hookLayout("com.htc.lockscreen", "layout", "specific_lockscreen_buttonfooter", new XC_LayoutInflated() {
	        @Override
	        public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
            	prefs.reload();
            	panelAlignBottom = prefs.getBoolean("panelAlignBottom", false);
            	hintText = prefs.getString("hintText", "");
            	RelativeLayout panel = (RelativeLayout) liparam.view;
                View panelBg = (View) panel.getChildAt(0);
                if (panelAlignBottom) {
                	FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams) panel.getLayoutParams();
                	RelativeLayout.LayoutParams localLayoutParamsBg = (RelativeLayout.LayoutParams) panelBg.getLayoutParams();
                	//if (localLayoutParams.bottomMargin == 0 || localLayoutParams.bottomMargin > 100) {
	                	//XposedBridge.log("Shortcut panel original bottom margin: " + localLayoutParams.bottomMargin);
	                	localLayoutParams.bottomMargin -= 81;
	                	panel.setLayoutParams(localLayoutParams);
	                	localLayoutParamsBg.bottomMargin = 81;
	                	panelBg.setLayoutParams(localLayoutParamsBg);
                	//}
                }
            	if (!hintText.equals("")) {
	                int count = panel.getChildCount();
	                TextView mHint = (TextView) panel.getChildAt(count-2);
	                mHint.setText(hintText);
	                mHint.setVisibility(View.VISIBLE);
	                mHint.setAlpha(1.0f);
            	}
	        }
	    });
	}
}