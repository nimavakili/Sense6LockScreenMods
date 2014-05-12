package com.minimv.senselockscreen;

import android.view.View;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

public class XposedResHooks implements IXposedHookZygoteInit, IXposedHookInitPackageResources {
    private static String MODULE_PATH = null;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
	    if (!resparam.packageName.equals("com.htc.lockscreen"))
	        return;
	    
	    resparam.res.hookLayout("com.htc.lockscreen", "layout", "main_lockscreen_keyguard_widget_pager", new XC_LayoutInflated() {
	        @Override
	        public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				//param.args[0] = 1640 + 81;
				View parent = (View) liparam.view;
				//widgetFrame.setBackgroundColor(Color.RED);
				//View parent = (View) widgetFrame.getParent();
				parent.setPadding(0, parent.getPaddingTop(), 0, parent.getPaddingBottom() - 81);
            	//XposedBridge.log("TP: " + parent.getPaddingTop());
            	XposedBridge.log("BP: " + parent.getPaddingBottom());
				//parent.setBackgroundColor(Color.YELLOW);
            	//ViewGroup.LayoutParams localLayoutParams = (ViewGroup.LayoutParams) widgetFrame.getLayoutParams();
            	//localLayoutParams.height += 81;
            	//widgetFrame.setLayoutParams(localLayoutParams);
				//XposedBridge.log("setFrameHeight" + param.args[0]);
				//XposedBridge.log("getFrameHeight: " + widgetFrame.getHeight());
	        }
	    }); 
	}
}