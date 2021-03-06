package com.minimv.senselockscreen;

import java.lang.reflect.Method;
import android.widget.ViewFlipper;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class XposedHooks implements IXposedHookLoadPackage {

	private XSharedPreferences prefs;
	private boolean hideCarrier, panelAlignBottom, nukeHidePanel, nukeHorizontalArrows, hideWidgetFrame, maximizeWidget, disablePatternScroll, improvePattern, hidePanel, forceDoubleTap, improveUnlock;
	private String carrierText, hintText, bgDimming, movePattern, defaultWidget, unlockSensitivity;
	
	public XposedHooks() {
		prefs = new XSharedPreferences("com.minimv.senselockscreen");
	}
	
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		
		if (!lpparam.packageName.equals("com.htc.lockscreen")) {
        	return;
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.ui.OperatorView", lpparam.classLoader, "updateOperatorName", new XC_MethodHook() {
        		@Override
        		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			hideCarrier = prefs.getBoolean("hideCarrier", false);
        			panelAlignBottom = prefs.getBoolean("panelAlignBottom", false);
        			carrierText = prefs.getString("carrierText", "");
        			TextView carrier = (TextView) param.thisObject;
        			if (hideCarrier || panelAlignBottom) {
        				carrier.setVisibility(View.GONE);
        			}
        			else if (!carrierText.equals("")) {
        				carrier.setText(carrierText);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.ui.footer.ButtonFooter", lpparam.classLoader, "adjustPosition", new XC_MethodHook() {
        		@Override
        		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			panelAlignBottom = prefs.getBoolean("panelAlignBottom", false);
        			hintText = prefs.getString("hintText", "");
        			RelativeLayout panel = (RelativeLayout) param.thisObject;
        			View panelBg = (View) panel.getChildAt(0);
        			if (panelAlignBottom) {
        				FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams) panel.getLayoutParams();
        				RelativeLayout.LayoutParams localLayoutParamsBg = (RelativeLayout.LayoutParams) panelBg.getLayoutParams();
        				if (localLayoutParams.bottomMargin == 0 || localLayoutParams.bottomMargin > 63) {
        					//XposedBridge.log("Shortcut panel original bottom margin: " + localLayoutParams.bottomMargin);
        					localLayoutParams.bottomMargin -= 81;
        					panel.setLayoutParams(localLayoutParams);
        					localLayoutParamsBg.bottomMargin = 81;
        					panelBg.setLayoutParams(localLayoutParamsBg);
        				}
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.ui.footer.ButtonFooter", lpparam.classLoader, "updateShortcutVisible", boolean.class, new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			hidePanel = prefs.getBoolean("hidePanel", false);
        			nukeHidePanel = prefs.getBoolean("nukeHidePanel", false);
        			hintText = prefs.getString("hintText", "");
        			RelativeLayout panel = (RelativeLayout) param.thisObject;
        			boolean visibility = (Boolean) param.args[0];
        			int count = panel.getChildCount();
        			TextView mHint = null;
        			try {
        				mHint = (TextView) panel.getChildAt(count-2);
        			}
        			catch (Exception e) {
        				mHint = (TextView) ((FrameLayout) panel.getChildAt(count-2)).getChildAt(0);
        			}
        			if (visibility && !hintText.equals("")) {
        				mHint.setText(hintText);
        				mHint.setVisibility(View.VISIBLE);
        				mHint.setAlpha(1.0f);
        			}
        			else {
        				mHint.setAlpha(0.0f);
        			}
        			if (hidePanel) {
        				param.args[0] = false;
        			}
        			else if (nukeHidePanel) {
        				param.setResult(null);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	Class<?> ShortcutSphere = findClass("com.htc.lockscreen.ui.footer.ShortcutSphere", lpparam.classLoader);
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
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
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
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader, "drawBg", Canvas.class, new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			hideWidgetFrame = prefs.getBoolean("hideWidgetFrame", false);
        			if (hideWidgetFrame) {
        				param.setResult(null);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	/*findAndHookMethod("com.htc.lockscreen.keyguard.HtcKeyguardViewStateManager", lpparam.classLoader, "updateBackground", new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			nukeBackground = prefs.getBoolean("nukeBackground", false);
        			if (nukeBackground) {
        				param.setResult(null);
        			}
        		}
        	});*/
        	findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardViewManager", lpparam.classLoader, "updateBackgroundColor", int.class, new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			bgDimming = prefs.getString("bgDimming", "default");
        			//XposedBridge.log("BG: " + param.args[0]);
        			if (bgDimming.equals("noDim")) {
        				param.args[0] = Color.TRANSPARENT;
        			}
        			else if (bgDimming.equals("dim")) {
        				param.args[0] = 2130706432;
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        /*findAndHookMethod("com.htc.lockscreen.keyguard.HtcKeyguardViewStateManager", lpparam.classLoader, "onPageBeginMoving", new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	XposedBridge.log("HtcKeyguardViewStateManager onPageBeginMoving");
	            	//Class<?> KeyguardSecurityContainer = findClass("com.htc.lockscreen.keyguard.KeyguardSecurityContainer", lpparam.classLoader);
	            	mKeyguardSecurityContainer = (ViewFlipper) getObjectField(param.thisObject, "mKeyguardSecurityContainer");
	            	XposedBridge.log(mKeyguardSecurityContainer.toString());
	            	Object child = ((FrameLayout) mKeyguardSecurityContainer).getChildAt(0);
	            	XposedBridge.log(child.toString());

	            	Object mChallengeLayout = getObjectField(param.thisObject, "mChallengeLayout");
	            	XposedBridge.log(mChallengeLayout.toString());
	            	Object child2 = ((ViewGroup) mChallengeLayout).getChildAt(0);
	            	XposedBridge.log(child2.toString());
	            	Object security = mKeyguardSecurityContainer.getChildAt(mKeyguardSecurityContainer.getDisplayedChild());
	            	//XposedBridge.log(security.getClass().getName());
	            	if (security.getClass().getName().contains("HtcPatternUnlockView")) {
	            		((LinearLayout) security).setBackgroundColor(Color.RED);
	            		param.setResult(null);
	            	}
	            }
        	});*/

        /*Class<?> KeyguardWidgetPager = findClass("com.htc.lockscreen.keyguard.KeyguardWidgetPager", lpparam.classLoader);
	        hookAllConstructors(KeyguardWidgetPager, new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                	//XposedBridge.log("KeyguardWidgetPager Construct");
                	//setIntField(param.thisObject, "mEdgeSwipeRegionSize", 0);
	            }
	        });*/

        try {
        	findAndHookMethod("com.htc.lockscreen.keyguard.PagedView", lpparam.classLoader, "startScrolling", MotionEvent.class, new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			disablePatternScroll = prefs.getBoolean("disablePatternScroll", false);
        			if (disablePatternScroll) {
        				Object mViewStateManager = getObjectField(param.thisObject, "mViewStateManager");
        				ViewFlipper mKeyguardSecurityContainer = (ViewFlipper) getObjectField(mViewStateManager, "mKeyguardSecurityContainer");
        				View security = mKeyguardSecurityContainer.getChildAt(mKeyguardSecurityContainer.getDisplayedChild());
        				Method isChallengeShowing = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.KeyguardViewStateManager", lpparam.classLoader), "isChallengeShowing");
        				boolean mChallengeShowing = (Boolean) isChallengeShowing.invoke(mViewStateManager, (Object[]) null);
        				int[] loc = new int[2];
        				security.getLocationOnScreen(loc);
        				MotionEvent me = (MotionEvent) param.args[0];
        				//XposedBridge.log("startScrolling: " + mChallengeShowing);
        				if (security.getClass().getName().contains("HtcPatternUnlockView") && mChallengeShowing && me.getRawY() > loc[1]) {
        					param.setResult(null);
        				}
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.unlockscreen.HtcPatternUnlockView", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			improvePattern = prefs.getBoolean("improvePattern", false);
        			if (improvePattern) {
	        			boolean result = true;
	        			MotionEvent me = (MotionEvent) param.args[0];
	        			if (!me.isFromSource(66))
	        				result = false;
	        			param.setResult(result);
        			}
        		}
        	});

        	findAndHookMethod("com.htc.lockscreen.keyguard.SlidingChallengeLayout", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
        		@Override
        		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			improvePattern = prefs.getBoolean("improvePattern", false);
        			if (improvePattern) {
	        			View mChallengeView = (View) getObjectField(param.thisObject, "mChallengeView");
	        			Method getFlipper = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.KeyguardSecurityContainer", lpparam.classLoader), "getFlipper");
	        			ViewFlipper flipper = (ViewFlipper) getFlipper.invoke(mChallengeView, (Object[]) null);
	        			View security = flipper.getChildAt(flipper.getDisplayedChild());
	        			Method isChallengeShowing = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.SlidingChallengeLayout", lpparam.classLoader), "isChallengeShowing");
	        			boolean mChallengeShowing = (Boolean) isChallengeShowing.invoke(param.thisObject, (Object[]) null);
	        			if (security.getClass().getName().contains("HtcPatternUnlockView") && mChallengeShowing) {
	        				ViewGroup parent = (ViewGroup) ((ViewGroup) ((ViewGroup) ((ViewGroup) security).getChildAt(0)).getChildAt(0)).getChildAt(0);
	        				View mLockPatternView = parent.getChildAt(parent.getChildCount() - 1);
	        				int[] loc = new int[2];
	        				mLockPatternView.getLocationOnScreen(loc);
	        				MotionEvent me = (MotionEvent) param.args[0];
	        				me.offsetLocation(-loc[0], -loc[1]);
	        				me.setSource(66);
	        				mLockPatternView.dispatchTouchEvent(me);
	        			}
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        /*Class<?> HtcLockPatternView = findClass("com.htc.lockscreen.view.HtcLockPatternView", lpparam.classLoader);
	        Method[] touchEvents = HtcLockPatternView.getDeclaredMethods();
	        //Method[] touchEvents = findMethodsByExactParameters(HtcLockPatternView, boolean.class, MotionEvent.class);
	        for (int i = 0; i < touchEvents.length; i++) {
		        XposedBridge.log("HtcLockPatternView Methods: " + touchEvents[i].getName());
	        }

	        Class<?> LockPatternView = findClass("com.android.internal.widget.LockPatternView", lpparam.classLoader);
	        touchEvents = LockPatternView.getDeclaredMethods();
	        //Method[] touchEvents = findMethodsByExactParameters(HtcLockPatternView, boolean.class, MotionEvent.class);
	        for (int i = 0; i < touchEvents.length; i++) {
		        XposedBridge.log("LockPatternView Methods: " + touchEvents[i].getName());
	        }*/

        try {
        	findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardHostView", lpparam.classLoader, "onScreenTurnedOn", new XC_MethodHook() {
        		@Override
        		protected void afterHookedMethod(MethodHookParam param) {
        			prefs.reload();
        			maximizeWidget = prefs.getBoolean("maximizeWidget", false);
        			hidePanel = prefs.getBoolean("hidePanel", false);
        			boolean isByPassLockScreen = (Boolean) callMethod(param.thisObject, "isByPassLockScreen");
        			if (maximizeWidget && !isByPassLockScreen) {
        				Object object = getObjectField(param.thisObject, "mSlidingChallengeLayout");
        				callMethod(object, "showChallenge", false);
        			}
        			if (hidePanel) {
        				Object object = getObjectField(param.thisObject, "mFooterPanel");
        				callMethod(object, "updateShortcutVisible", false);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.keyguard.SlidingChallengeLayout", lpparam.classLoader, "onMeasure", int.class, int.class, new XC_MethodHook() {
        		@Override
        		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
        			prefs.reload();
        			//movePattern = prefs.getBoolean("movePattern", false);
        			movePattern = prefs.getString("movePattern", "default");
        			if (movePattern.equals("up")) {
        				View mChallengeView = (View) getObjectField(param.thisObject, "mChallengeView");
            			//XposedBridge.log("TP: " + mChallengeView.getPaddingTop());
        				mChallengeView.setPadding(0, mChallengeView.getPaddingTop() - 81, 0, 0);
        			}
        			else if (movePattern.equals("middle")) {
        				View mChallengeView = (View) getObjectField(param.thisObject, "mChallengeView");
            			//XposedBridge.log("TP: " + mChallengeView.getPaddingTop());
        				mChallengeView.setPadding(0, mChallengeView.getPaddingTop() + 144 - 81, 0, 0);
        			}
        			else if (movePattern.equals("down")) {
        				View mChallengeView = (View) getObjectField(param.thisObject, "mChallengeView");
            			//XposedBridge.log("TP: " + mChallengeView.getPaddingTop());
        				mChallengeView.setPadding(0, mChallengeView.getPaddingTop() + 144, 0, 0);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.keyguard.PagedView", lpparam.classLoader, "isDefaultPage", int.class, new XC_MethodHook() {
        		@Override
        		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
        			if ((Boolean) param.getResult()) {
            			prefs.reload();
            			defaultWidget = prefs.getString("defaultWidget", "default");
            			if (defaultWidget.equals("invisible")) {
    	        			ViewGroup defaultWidget = (ViewGroup) param.thisObject;
    	        			View localView = defaultWidget.getChildAt((Integer) param.args[0]);
    	            		localView.setAlpha(0);
            			}
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardHostView", lpparam.classLoader, "addDefaultStatusWidget", int.class, new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
        			prefs.reload();
        			defaultWidget = prefs.getString("defaultWidget", "default");
        			if (defaultWidget.equals("gone")) {
        				param.setResult(null);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.drag.SpeedRecorder", lpparam.classLoader, "getUnlockDistance", new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
        			prefs.reload();
        			unlockSensitivity = prefs.getString("unlockSensitivity", "default");
        			// stock value: 200
        			if (unlockSensitivity.equals("more")) {
        				param.setResult(100);
        			}
        			else if (unlockSensitivity.equals("less")) {
        				param.setResult(300);
        			}
        		}
        		/*@Override
        		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
        			XposedBridge.log("getUnlockDistance: " + param.getResult());
        		}*/
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.ctrl.EasyAccessCtrl", lpparam.classLoader, "isSupportDoubleTap", new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
        			prefs.reload();
        			forceDoubleTap = prefs.getBoolean("forceDoubleTap", false);
        			if (forceDoubleTap) {
        				param.setResult(true);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.ui.EasyAccessLayout", lpparam.classLoader, "canStartDrag", int.class, int.class, new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
        			prefs.reload();
        			improveUnlock = prefs.getBoolean("improveUnlock", false);
        			if (improveUnlock) {
	    				//Object mViewStateManager = getObjectField(param.thisObject, "mViewStateManager");
	    				//ViewFlipper mKeyguardSecurityContainer = (ViewFlipper) getObjectField(mViewStateManager, "mKeyguardSecurityContainer");
	    				//View security = mKeyguardSecurityContainer.getChildAt(mKeyguardSecurityContainer.getDisplayedChild());
	    				//int[] loc = new int[2];
	    				//security.getLocationOnScreen(loc);
            			//XposedBridge.log("canStartDrag: " + loc[1]);
	    				if ((Integer) param.args[1] < 960) {
	    					param.setResult(false);
	            			//XposedBridge.log("canStartDrag before: false");
	    				}
        			}
        		}
        		/*@Override
        		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
        			prefs.reload();
        			improveUnlock = prefs.getBoolean("improveUnlock", false);
        			if (improveUnlock) {
            			XposedBridge.log("canStartDrag after: " + param.getResult());
        			}
        		}*/
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.ui.EasyAccessLayout", lpparam.classLoader, "checkDragCondition", new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
        			prefs.reload();
        			improveUnlock = prefs.getBoolean("improveUnlock", false);
        			if (improveUnlock) {
	    				Object mViewStateManager = getObjectField(param.thisObject, "mViewStateManager");
	    				Object mKeyguardWidgetPager = getObjectField(mViewStateManager, "mKeyguardWidgetPager");
	    			    int curPage = (Integer) callMethod(mKeyguardWidgetPager, "getCurrentPage");
	    			    boolean isSmall = (Boolean) callMethod(callMethod(mKeyguardWidgetPager, "getWidgetPageAt", curPage), "isSmall");
	    			    //boolean isChallengeShowing = (Boolean) callMethod(mViewStateManager, "isChallengeShowing");
	    			    //boolean isChallengeViewDragging = (Boolean) callMethod(mViewStateManager, "isChallengeViewDragging");
	    			    boolean isPageViewInIdle = (Boolean) callMethod(mViewStateManager, "isPageViewInIdle");
	    			    boolean isSeurityModeWithFooter = (Boolean) callMethod(mViewStateManager, "isSeurityModeWithFooter", callMethod(mViewStateManager, "getCurSecurityMode"));
	    			    boolean bool = isPageViewInIdle;
	    			    if (!isSmall) {
	    	        		//XposedBridge.log("checkDragCondition isSmall: " + isSmall);
		    			    param.setResult(bool);
		    			    return;
	    			    }
	    			    if (!isSeurityModeWithFooter) {
	    	        		bool = false;
	    			    }
    	        		//XposedBridge.log("checkDragCondition: " + bool);
	    			    param.setResult(bool);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }
	}
}