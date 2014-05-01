package com.minimv.senselockscreen;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RemoveCarrier implements IXposedHookLoadPackage {

	XSharedPreferences prefs;
	boolean removeCarrier, panelAlignBottom, nukeSphereDrag, nukeHidePanel, nukeHorizontalArrows;
	String carrierText, hintText;
	
	public RemoveCarrier() {
		prefs = new XSharedPreferences("com.minimv.senselockscreen");
	}
	
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.htc.lockscreen")) {

	    	Class<?> ShortcutSphere = findClass("com.htc.lockscreen.ui.footer.ShortcutSphere", lpparam.classLoader);
	
	        findAndHookMethod("com.htc.lockscreen.ui.OperatorView", lpparam.classLoader, "updateOperatorName", new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	            	prefs.reload();
	            	removeCarrier = prefs.getBoolean("removeCarrier", false);
	            	carrierText = prefs.getString("carrierText", "");
	            	TextView carrier = (TextView) param.thisObject;
	            	if (removeCarrier) {
	            		carrier.setVisibility(View.GONE);
	            	}
	            	else if (!carrierText.equals("")) {
	            		carrier.setText(carrierText);
	            	}
	            }
	        });
	
	        findAndHookMethod("com.htc.lockscreen.ui.footer.ButtonFooter", lpparam.classLoader, "init", new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	            	prefs.reload();
	            	panelAlignBottom = prefs.getBoolean("panelAlignBottom", false);
	            	hintText = prefs.getString("hintText", "");
	            	RelativeLayout panel = (RelativeLayout) param.thisObject;
	                if (panelAlignBottom) {
	                	FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams) panel.getLayoutParams();
	                	localLayoutParams.bottomMargin = -81;
	                	panel.setLayoutParams(localLayoutParams);
	                }
	                int count = panel.getChildCount();
	            	if (!hintText.equals("")) {
		                TextView mHint = (TextView) panel.getChildAt(count-2);
		                mHint.setText(hintText);
		                mHint.setVisibility(View.VISIBLE);
		                mHint.setAlpha(1.0f);
	            	}
	            }
	        });
	
	        findAndHookMethod("com.htc.lockscreen.ui.footer.ButtonFooter", lpparam.classLoader, "updateShortcutVisible", boolean.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	prefs.reload();
	            	nukeHidePanel = prefs.getBoolean("nukeHidePanel", false);
	            	hintText = prefs.getString("hintText", "");
	            	RelativeLayout panel = (RelativeLayout) param.thisObject;
	            	boolean visibility = (Boolean) param.args[0];
	                int count = panel.getChildCount();
	                TextView mHint = (TextView) panel.getChildAt(count-2);
	                if (visibility && !hintText.equals("")) {
	                	mHint.setText(hintText);
	                	mHint.setVisibility(View.VISIBLE);
	                	mHint.setAlpha(1.0f);
	                }
	                else {
	                	mHint.setAlpha(0.0f);
	                }
	            	if (nukeHidePanel) {
	            		param.setResult(null);
	            	}
	            }
	        });
	
	        findAndHookMethod("com.htc.lockscreen.ui.footer.ButtonFooter", lpparam.classLoader, "onBeginDrag", ShortcutSphere, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	prefs.reload();
	            	//nukeSphereDrag = prefs.getBoolean("nukeSphereDrag", false);
	            	hintText = prefs.getString("hintText", "");
	            	if (!hintText.equals("")) {
	            		param.setResult(null);
	            	}
	            }
	        });
	
	        findAndHookMethod("com.htc.lockscreen.ui.footer.ButtonFooter", lpparam.classLoader, "isShowAllDirectArrow", new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	prefs.reload();
	            	nukeHorizontalArrows = prefs.getBoolean("nukeHorizontalArrows", false);
	            	if (nukeHorizontalArrows) {
	            		param.setResult(false);
	            	}
	            }
	        });
		}
    }
}