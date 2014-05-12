package com.minimv.senselockscreen;

//import java.lang.reflect.Method;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import android.view.View.OnLongClickListener;
import android.widget.ViewFlipper;
//import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.hookLoadPackage;
import static de.robv.android.xposed.XposedBridge.invokeOriginalMethod;
import static de.robv.android.xposed.XposedHelpers.callMethod;
//import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;
import static de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters;
import static de.robv.android.xposed.XposedHelpers.setAdditionalStaticField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
//import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;
//import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
//import static de.robv.android.xposed.XposedHelpers.setIntField;
//import static de.robv.android.xposed.XposedHelpers.findField;
//import static de.robv.android.xposed.XposedHelpers.setIntField;
//import static de.robv.android.xposed.XposedHelpers.setStaticIntField;
import android.view.HapticFeedbackConstants;
//import java.lang.reflect.Field;
//import static de.robv.android.xposed.XposedHelpers.findConstructorExact;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
//import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
//import android.graphics.Color;
import de.robv.android.xposed.XposedBridge;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.ViewGroup;
//import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class XposedHooks implements IXposedHookLoadPackage {

	public boolean hasKeyguardWidgetPagerRun = false;
	XSharedPreferences prefs;
	boolean removeCarrier, panelAlignBottom, nukeSphereDrag, nukeHidePanel, nukeHorizontalArrows;
	String carrierText, hintText;
	//ViewFlipper mKeyguardSecurityContainer = null;
	
	public XposedHooks() {
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
	            	panelAlignBottom = prefs.getBoolean("panelAlignBottom", false);
	            	carrierText = prefs.getString("carrierText", "");
	            	TextView carrier = (TextView) param.thisObject;
	            	if (removeCarrier || panelAlignBottom) {
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
	                View panelBg = (View) panel.getChildAt(0);
	                if (panelAlignBottom) {
	                	FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams) panel.getLayoutParams();
	                	RelativeLayout.LayoutParams localLayoutParamsBg = (RelativeLayout.LayoutParams) panelBg.getLayoutParams();
	                	if (localLayoutParams.bottomMargin == 0 || localLayoutParams.bottomMargin > 100) {
		                	//XposedBridge.log("Shortcut panel original bottom margin: " + localLayoutParams.bottomMargin);
		                	localLayoutParams.bottomMargin -= 81;
		                	panel.setLayoutParams(localLayoutParams);
		                	localLayoutParamsBg.bottomMargin = 81;
		                	panelBg.setLayoutParams(localLayoutParamsBg);
	                	}
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
	                	if (localLayoutParams.bottomMargin == 0 || localLayoutParams.bottomMargin > 100) {
		                	//XposedBridge.log("Shortcut panel original bottom margin: " + localLayoutParams.bottomMargin);
		                	localLayoutParams.bottomMargin -= 81;
		                	panel.setLayoutParams(localLayoutParams);
		                	localLayoutParamsBg.bottomMargin = 81;
		                	panelBg.setLayoutParams(localLayoutParamsBg);
	                	}
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

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader, "fadeFrame", Object.class, boolean.class, float.class, int.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	//prefs.reload();
            		param.setResult(null);
	            }
	        });*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader, "setBackgroundAlpha", float.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                	//XposedBridge.log("setBackgroundAlpha");
	            	param.args[0] = 0.0f;
            		//param.setResult(null);
	            }
	        });*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader, "drawPageOutLine", Canvas.class, int.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                	XposedBridge.log("drawPageOutLine");
            		//param.setResult(null);
	            	param.args[1] = Color.TRANSPARENT;
	            }
	        });*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader, "setIsEnableOutLine", boolean.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                	XposedBridge.log("setIsEnableOutLine");
	            	param.args[0] = false;
            		//param.setResult(null);
	            	//param.args[1] = Color.TRANSPARENT;
	            }
	        });*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader, "isDefaultWdidget", new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                	XposedBridge.log("isDefaultWdidget");
	            	param.setResult(false);
	            }
	        });*/

	        findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader, "drawBg", Canvas.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                	//XposedBridge.log("drawBg");
            		param.setResult(null);
	            	//param.args[1] = Color.TRANSPARENT;
	            }
	        });

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader, "setWidgetHeight", int.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	XposedBridge.log("WidgetHeight: " + param.args[0].toString());
	        		//param.setResult(null);
	            	//param.args[1] = Color.TRANSPARENT;
	            }
        	});

	        findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader, "setFrameHeight", int.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	XposedBridge.log("FrameHeight: " + param.args[0].toString());
	        		//param.setResult(null);
	            	//param.args[1] = Color.TRANSPARENT;
	            }
        	});*/
	        
	    	/*Class<?> KeyguardWidgetPager = findClass("com.htc.lockscreen.keyguard.KeyguardWidgetPager", lpparam.classLoader);
	    	//Field WIDGET_OUTLINE_COLOR = findField(KeyguardWidgetFrame, "WIDGET_OUTLINE_COLOR");
	        hookAllConstructors(KeyguardWidgetPager, new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                	ViewGroup widgetP = (ViewGroup) param.thisObject;
                	widgetP.setBackgroundColor(Color.MAGENTA);
        	    	FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams) widgetP.getLayoutParams();
                	XposedBridge.log("KeyguardWidgetPager Height: " + localLayoutParams.height);
                	XposedBridge.log("KeyguardWidgetPager Bottom: " + widgetP.getBottom());
                	localLayoutParams.bottomMargin = -81;
                	widgetP.setLayoutParams(localLayoutParams);
	            }
	        });*/
	        
	    	/*Class<?> KeyguardWidgetFrame = findClass("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader);
	    	//Field WIDGET_OUTLINE_COLOR = findField(KeyguardWidgetFrame, "WIDGET_OUTLINE_COLOR");
	        hookAllConstructors(KeyguardWidgetFrame, new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                	XposedBridge.log("KeyguardWidgetFrame Construct");
            		//param.setResult(null);
                	FrameLayout widget = (FrameLayout) param.thisObject;
                	//widget.setClickable(false);
        	    	//setIntField(param.thisObject, "WIDGET_OUTLINE_COLOR", Color.TRANSPARENT);
        	    	setIntField(param.thisObject, "mFrameStrokeAdjustment", 0);
        	    	//widget.setBackgroundColor(Color.RED);
        	    	FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams) widget.getLayoutParams();
                	localLayoutParams.bottomMargin = -81;
                	widget.setLayoutParams(localLayoutParams);
                	//widget.setEnabled(false);
                	//widget.setAlpha(0);
                	//widget.setVisibility(View.INVISIBLE);
	            }
	        });*/

	    	/*Class<?> HtcKeyguardViewStateManager = findClass("com.htc.lockscreen.keyguard.HtcKeyguardViewStateManager", lpparam.classLoader);
	    	//setStaticIntField(HtcKeyguardViewStateManager, "COLOR_FULL_BACKGROUND", Color.TRANSPARENT);
	    	//Field WIDGET_OUTLINE_COLOR = findField(KeyguardWidgetFrame, "WIDGET_OUTLINE_COLOR");
	        hookAllConstructors(HtcKeyguardViewStateManager, new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                	XposedBridge.log("HtcKeyguardViewStateManager Construct");
            		//param.setResult(null);
                	//FrameLayout widget = (FrameLayout) param.thisObject;
                	//widget.setClickable(false);
        	    	setIntField(param.thisObject, "COLOR_FULL_BACKGROUND", Color.TRANSPARENT);
        	    	//setIntField(param.thisObject, "mFrameStrokeAdjustment", 0);
        	    	//widget.setBackgroundColor(Color.RED);
                	//widget.setEnabled(false);
                	//widget.setAlpha(0);
                	//widget.setVisibility(View.INVISIBLE);
	            }
	        });*/

	    	/*Class<?> KeyguardSecurityContainer = findClass("com.htc.lockscreen.keyguard.KeyguardSecurityContainer", lpparam.classLoader);
	    	//setStaticIntField(HtcKeyguardViewStateManager, "COLOR_FULL_BACKGROUND", Color.TRANSPARENT);
	    	//Field WIDGET_OUTLINE_COLOR = findField(KeyguardWidgetFrame, "WIDGET_OUTLINE_COLOR");
	        hookAllConstructors(KeyguardSecurityContainer, new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                	FrameLayout security = (FrameLayout) param.thisObject;
                	security.setBackgroundColor(Color.RED);
	            }
	        });*/

	        findAndHookMethod("com.htc.lockscreen.keyguard.HtcKeyguardViewStateManager", lpparam.classLoader, "updateBackground", new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	//XposedBridge.log("HtcKeyguardViewStateManager");
	        		param.setResult(null);
	            	//param.args[1] = Color.TRANSPARENT;
	            }
        	});

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

	        /*Method setNextScrollDirection = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.HtcKeyguardViewStateManager", lpparam.classLoader), "setNextScrollDirection");
	        hookMethod(setNextScrollDirection, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	XposedBridge.log("setNextScrollDirection");
	        		param.setResult(null);
	            	//param.args[1] = Color.TRANSPARENT;
	            }
        	});*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetPager", lpparam.classLoader, "screenScrolled", int.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	XposedBridge.log("KeyguardWidgetPager");
	        		param.setResult(null);
	            	//param.args[1] = Color.TRANSPARENT;
	            }
        	});*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetPager", lpparam.classLoader, "onPageScoll", int.class, int.class, int.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	//XposedBridge.log("KeyguardWidgetPager onPageScoll");
                	setIntField(param.thisObject, "mEdgeSwipeRegionSize", 0);
	            	param.args[1] = param.args[0];
	        		param.setResult(null);
	            	//param.args[1] = Color.TRANSPARENT;
	            }
        	});

	        findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetPager", lpparam.classLoader, "onPageSwitching", View.class, int.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	XposedBridge.log("onPageSwitching");
                	setIntField(param.thisObject, "mEdgeSwipeRegionSize", 0);
	        		param.setResult(null);
	        		//XposedBridge.log("Edge: " + getIntField(param.thisObject, "mEdgeSwipeRegionSize"));
	            	//param.args[1] = Color.TRANSPARENT;
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

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetPager", lpparam.classLoader, "onPageBeginMoving", new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	XposedBridge.log("KeyguardWidgetPager onPageBeginMoving");
            		param.setResult(null);
	            }
        	});*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.PagedView", lpparam.classLoader, "onPageBeginWarp", new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	XposedBridge.log("PagedView onPageBeginWarp");
            		param.setResult(null);
	            }
        	});*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.PagedView", lpparam.classLoader, "init", new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	            	XposedBridge.log("init");
	            	Object mViewStateManager = getObjectField(param.thisObject, "mViewStateManager");
	            	Object mChallengeLayout = getObjectField(mViewStateManager, "mChallengeLayout");
	            	Method fadeOutChallenge = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.SlidingChallengeLayout", lpparam.classLoader), "fadeOutChallenge");
	            	fadeOutChallenge.invoke(mChallengeLayout, (Object[]) null);
	            }
        	});*/

	        findAndHookMethod("com.htc.lockscreen.keyguard.PagedView", lpparam.classLoader, "startScrolling", MotionEvent.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	//XposedBridge.log("startScrolling");
	            	//Object test = getObjectField(obj, fieldName)
	            	Object mViewStateManager = getObjectField(param.thisObject, "mViewStateManager");
	            	//XposedBridge.log(mViewStateManager.getClass().getName());
	            	ViewFlipper mKeyguardSecurityContainer = (ViewFlipper) getObjectField(mViewStateManager, "mKeyguardSecurityContainer");
	            	/*
	            	Object mChallengeLayout = getObjectField(mViewStateManager, "mChallengeLayout");
	            	Method fadeOutChallenge = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.SlidingChallengeLayout", lpparam.classLoader), "fadeOutChallenge");
	            	fadeOutChallenge.invoke(mChallengeLayout, (Object[]) null);
	            	*/
	            	View security = mKeyguardSecurityContainer.getChildAt(mKeyguardSecurityContainer.getDisplayedChild());
	            	//XposedBridge.log("V: " + mChallengeLayout.getVisibility() + ", A: " + mChallengeLayout.getAlpha());
	            	Method isChallengeShowing = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.KeyguardViewStateManager", lpparam.classLoader), "isChallengeShowing");
	            	boolean mChallengeShowing = (boolean) isChallengeShowing.invoke(mViewStateManager, (Object[]) null);
	            	//XposedBridge.log(security.getClass().getName() + ", isChallengeShowing: " + mChallengeShowing);
	            	int[] loc = new int[2];
	            	security.getLocationOnScreen(loc);
	            	MotionEvent me = (MotionEvent) param.args[0];
	            	if (security.getClass().getName().contains("HtcPatternUnlockView") && mChallengeShowing && me.getRawY() > loc[1]) {
		            	//XposedBridge.log("PagedView Y: " + me.getY());
		            	//XposedBridge.log("PagedView RawY: " + me.getRawY());
	            		//Method resetTouchState = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.PagedView", lpparam.classLoader), "resetTouchState");
	            		//resetTouchState.invoke(param.thisObject, (Object[]) null);
	            		//((LinearLayout) security).setBackgroundColor(Color.RED);
	            		//LinearLayout pattern1 = (LinearLayout) security;
	            		//LinearLayout pattern2 = (LinearLayout) pattern1.getChildAt(0);
	            		//RelativeLayout pattern3 = (RelativeLayout) pattern2.getChildAt(0);
	            		//LinearLayout pattern4 = (LinearLayout) pattern3.getChildAt(0);
	            		//int count = pattern4.getChildCount();
	            		//XposedBridge.log("Count4: " + count);
	            		//Object pattern5 = pattern4.getChildAt(count-1);
	            		//XposedBridge.log(pattern5.getClass().getName());
	            		//XposedBridge.log("Count5: " + ((LinearLayout) pattern5).getChildCount());
	            		//XposedBridge.log("Class6: " + ((LinearLayout) pattern5).getChildAt(0).getClass().getName());
	            		param.setResult(null);
	            	}
	            }
        	});

	        
	        
	        findAndHookMethod("com.htc.lockscreen.unlockscreen.HtcPatternUnlockView", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	//XposedBridge.log("dispatchTouchEvent: " + param.getResult());
	            	//View parent = (View) param.thisObject;
	            	//View mLockPatternView = (View) getObjectField(param.thisObject, "mLockPatternView");
	            	MotionEvent me = (MotionEvent) param.args[0];
	            	//XposedBridge.log("HtcPatternUnlockView X: " + me.getX());
	            	//XposedBridge.log("HtcPatternUnlockView Y: " + me.getY());
	            	//XposedBridge.log("HtcPatternUnlockView RawY: " + me.getRawY());
	            	boolean result = true;
	            	if (!me.isFromSource(66))
		            	//XposedBridge.log("HtcPatternUnlockView: " + MotionEvent.actionToString(me.getAction()));
	            		result = false;
	            	param.setResult(result);
	            	//XposedBridge.log("dispatchTouchEvent: " + (-mLockPatternView.getLeft()) + ", " + (-mLockPatternView.getTop()));
	            	//me.offsetLocation(-mLockPatternView.getLeft(), -mLockPatternView.getTop());
	            	//parent.dispatchTouchEvent(me);
	            	//XposedBridge.log("Delegate: " + ((View)param.thisObject).getTouchDelegate().onTouchEvent(me));
	            	//param.setResult(false);
	            }
	        });
	        
	        /*findAndHookMethod("com.htc.lockscreen.unlockscreen.HtcPatternUnlockView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	            	XposedBridge.log("onTouchEvent: " + param.getResult());
	            }
	        });*/
	        
	        findAndHookMethod("com.htc.lockscreen.unlockscreen.HtcPatternUnlockView", lpparam.classLoader, "onConstructor", new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	            	//View parent = (View) param.thisObject;
	            	//View mLockPatternView = (View) getObjectField(param.thisObject, "mLockPatternView");
	            	//View parent = (View) mLockPatternView.getParent();
	            	//parent.setClickable(true);
	            	//ArrayList<View> touchable = new ArrayList<View>(1);
	            	//touchable.add(parent);
	            	//parent.addTouchables(touchable);
	            	/*parent.setOnTouchListener(new View.OnTouchListener() {
	            	    @Override
	            	    public boolean onTouch(View v, MotionEvent me) {
            	        	XposedBridge.log("onTouch: " + MotionEvent.actionToString(me.getAction()));
        	            	me.offsetLocation(-mLockPatternView.getLeft(), -mLockPatternView.getTop());
        	            	XposedBridge.log("onTouch X: " + me.getX());
        	            	XposedBridge.log("onTouch Y: " + me.getY());
            	        	//mLockPatternView.dispatchTouchEvent(me);
            	        	return true;
	            	    }	            		
	            	});*/
	            	//XposedBridge.log("Clickable: " + parent.isClickable() + ", Enabled" + parent.isEnabled());	            	
            		//LinearLayout pattern1 = (LinearLayout) parent;
            		//LinearLayout pattern2 = (LinearLayout) pattern1.getChildAt(0);
            		//RelativeLayout pattern3 = (RelativeLayout) pattern2.getChildAt(0);
	            	//pattern3.setBackgroundColor(Color.MAGENTA);
	            	//mLockPatternView.setBackgroundColor(Color.RED);
            		//LinearLayout parent2 = (LinearLayout) mLockPatternView.getParent();
	            	//parent2.setBackgroundColor(Color.MAGENTA);
	            	//LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) pattern3.getLayoutParams();
	            	//localLayoutParams.height += 50;
	            	//LinearLayout.LayoutParams newLayoutParam = new LinearLayout.LayoutParams(-1, localLayoutParams.height + 200);
	            	//newLayoutParam.gravity = localLayoutParams.gravity;
	            	//pattern3.setLayoutParams(localLayoutParams);
	                /*parent.post(new Runnable() {
	                    @Override
	                    public void run() {
			            	Rect rect = new Rect();
		           	        int extraPadding = 200;
		           	        mLockPatternView.getHitRect(rect);
		           	        rect.top -= extraPadding;
		           	        rect.left -= extraPadding;
		           	        rect.right += extraPadding;
		           	        rect.bottom += extraPadding;
		           	        TouchDelegate td = new TouchDelegate(rect, mLockPatternView);
		           	        parent.setTouchDelegate(td);
			            	XposedBridge.log("touchables: " + parent.getTouchables().toString());
	                    }
	                });*/
	           	    //rect.offset(extraPadding, extraPadding);
	            	//mLockPatternView.set
	            	//ViewGroup.LayoutParams localLayoutParams = (ViewGroup.LayoutParams) mLockPatternView.getLayoutParams();
	            	//XposedBridge.log("width: " + localLayoutParams.width);
	            }
	        });

	        findAndHookMethod("com.htc.lockscreen.keyguard.SlidingChallengeLayout", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
				@Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	            	View mChallengeView = (View) getObjectField(param.thisObject, "mChallengeView");
	            	//mChallengeView.setBackgroundColor(Color.YELLOW);
	            	//((View) mChallengeView.getParent()).setBackgroundColor(Color.CYAN);
	            	Method getFlipper = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.KeyguardSecurityContainer", lpparam.classLoader), "getFlipper");
	            	ViewFlipper flipper = (ViewFlipper) getFlipper.invoke(mChallengeView, (Object[]) null);
	            	View security = flipper.getChildAt(flipper.getDisplayedChild());
	            	//security.setBackgroundColor(Color.MAGENTA);
	            	//LinearLayout.LayoutParams challengeLayoutParam = (ViewGroup.LayoutParams) security.getLayoutParams();
	            	//challengeLayoutParam.bottomMargin = 200;
	            	//security.setLayoutParams(challengeLayoutParam);
	            	Method isChallengeShowing = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.SlidingChallengeLayout", lpparam.classLoader), "isChallengeShowing");
	            	boolean mChallengeShowing = (boolean) isChallengeShowing.invoke(param.thisObject, (Object[]) null);
	            	//XposedBridge.log("Top: " + rect.top);
	            	if (security.getClass().getName().contains("HtcPatternUnlockView") && mChallengeShowing) {
	            		ViewGroup parent = (ViewGroup) ((ViewGroup) ((ViewGroup) ((ViewGroup) security).getChildAt(0)).getChildAt(0)).getChildAt(0);
		            	View mLockPatternView = parent.getChildAt(parent.getChildCount() - 1);
		            	//XposedBridge.log(mLockPatternView.getClass().getPackage().getName());
		            	//mLockPatternView.setBackgroundColor(Color.RED);
		            	//float topToChallenge = mLockPatternView.getTop();
		            	//float topToParent = mChallengeView.getTop() + topToChallenge;
		            	//Rect rect = new Rect();
		            	//mChallengeView.getWindowVisibleDisplayFrame(rect);
		            	int[] loc = new int[2];
		            	mLockPatternView.getLocationOnScreen(loc);
		            	MotionEvent me = (MotionEvent) param.args[0];
		            	//me.offsetLocation(-mLockPatternView.getLeft(), -topToParent + rect.top - mLockPatternView.getTop());
		            	me.offsetLocation(-loc[0], -loc[1]);
		            	//XposedBridge.log("SlidingChallengeLayout X: " + me.getX());
		            	//XposedBridge.log("SlidingChallengeLayout Y: " + me.getY());
		            	//XposedBridge.log("SlidingChallengeLayout RawY: " + me.getRawY());
		            	me.setSource(66);
		            	//Rect rect = new Rect();
		            	//mLockPatternView.getHitRect(rect);
		            	//if ((!rect.contains((int) me.getX(), (int) me.getX()) && me.getAction() == MotionEvent.ACTION_DOWN) || (rect.contains((int) me.getX(), (int) me.getX()) && me.getAction() != MotionEvent.ACTION_DOWN)) {
		            	//if (me.getX() < 0 || me.getY() < 0) {
		            	//XposedBridge.log("SlidingChallengeLayout: " + MotionEvent.actionToString(me.getAction()));
		            	mLockPatternView.dispatchTouchEvent(me);
		            	//}
	            	}
	            	//XposedBridge.log("dispatchTouchEvent: " + param.getResult());
	            	//View parent = (View) param.thisObject;
	            	//View mLockPatternView = (View) getObjectField(param.thisObject, "mLockPatternView");
	            	//XposedBridge.log("dispatchTouchEvent: " + (-mLockPatternView.getLeft()) + ", " + (-mLockPatternView.getTop()));
	            	//me.offsetLocation(-mLockPatternView.getLeft(), -mLockPatternView.getTop());
	            	//parent.dispatchTouchEvent(me);
	            	//XposedBridge.log("Delegate: " + ((View)param.thisObject).getTouchDelegate().onTouchEvent(me));
	            	//param.setResult(false);
	            }
	        });

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetPager", lpparam.classLoader, "handleExternalCameraEvent", MotionEvent.class, new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	            	//XposedBridge.log("dispatchTouchEvent: " + param.getResult());
	            	//View parent = (View) param.thisObject;
	            	//View mLockPatternView = (View) getObjectField(param.thisObject, "mLockPatternView");
	            	MotionEvent me = (MotionEvent) param.args[0];
	            	//XposedBridge.log("dispatchTouchEvent: " + (-mLockPatternView.getLeft()) + ", " + (-mLockPatternView.getTop()));
	            	//me.offsetLocation(-mLockPatternView.getLeft(), -mLockPatternView.getTop());
	            	//parent.dispatchTouchEvent(me);
	            	XposedBridge.log("KeyguardWidgetPager X: " + me.getX());
	            	XposedBridge.log("KeyguardWidgetPager Y: " + me.getY());
	            	//XposedBridge.log("Delegate: " + ((View)param.thisObject).getTouchDelegate().onTouchEvent(me));
	            	//param.setResult(false);
	            }
	        });*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardSecurityViewFlipper", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	//XposedBridge.log("dispatchTouchEvent: " + param.getResult());
	            	//View parent = (View) param.thisObject;
	            	//View mLockPatternView = (View) getObjectField(param.thisObject, "mLockPatternView");
	            	MotionEvent me = (MotionEvent) param.args[0];
	            	//XposedBridge.log("dispatchTouchEvent: " + (-mLockPatternView.getLeft()) + ", " + (-mLockPatternView.getTop()));
	            	//me.offsetLocation(-mLockPatternView.getLeft(), -mLockPatternView.getTop());
	            	//parent.dispatchTouchEvent(me);
	            	XposedBridge.log("KeyguardSecurityViewFlipper: " + me.getX());
	            	//XposedBridge.log("Delegate: " + ((View)param.thisObject).getTouchDelegate().onTouchEvent(me));
	            	//param.setResult(false);
	            }
	        });*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardHostView", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	//XposedBridge.log("dispatchTouchEvent: " + param.getResult());
	            	//View parent = (View) param.thisObject;
	            	//View mLockPatternView = (View) getObjectField(param.thisObject, "mLockPatternView");
	            	MotionEvent me = (MotionEvent) param.args[0];
	            	//XposedBridge.log("dispatchTouchEvent: " + (-mLockPatternView.getLeft()) + ", " + (-mLockPatternView.getTop()));
	            	//me.offsetLocation(-mLockPatternView.getLeft(), -mLockPatternView.getTop());
	            	//parent.dispatchTouchEvent(me);
	            	XposedBridge.log("KeyguardHostView: " + me.getX());
	            	//XposedBridge.log("Delegate: " + ((View)param.thisObject).getTouchDelegate().onTouchEvent(me));
	            	//param.setResult(false);
	            }
	        });*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.PagedView", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	//XposedBridge.log("dispatchTouchEvent: " + param.getResult());
	            	//View parent = (View) param.thisObject;
	            	//View mLockPatternView = (View) getObjectField(param.thisObject, "mLockPatternView");
	            	MotionEvent me = (MotionEvent) param.args[0];
	            	//XposedBridge.log("dispatchTouchEvent: " + (-mLockPatternView.getLeft()) + ", " + (-mLockPatternView.getTop()));
	            	//me.offsetLocation(-mLockPatternView.getLeft(), -mLockPatternView.getTop());
	            	//parent.dispatchTouchEvent(me);
	            	XposedBridge.log("PagedView: " + me.getX());
	            	//XposedBridge.log("Delegate: " + ((View)param.thisObject).getTouchDelegate().onTouchEvent(me));
	            	//param.setResult(false);
	            }
	        });*/

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
	        
	        /*findAndHookMethod("com.htc.lockscreen.view.HtcLockPatternView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	//View parent = (View) param.thisObject;
	            	//View mLockPatternView = (View) getObjectField(param.thisObject, "mLockPatternView");
	            	MotionEvent me = (MotionEvent) param.args[0];
	            	//XposedBridge.log("dispatchTouchEvent: " + (-mLockPatternView.getLeft()) + ", " + (-mLockPatternView.getTop()));
	            	//me.offsetLocation(-mLockPatternView.getLeft(), -mLockPatternView.getTop());
	            	//parent.dispatchTouchEvent(me);
	            	XposedBridge.log("LockPatternView: " + me.getY());
	            	//XposedBridge.log("Delegate: " + ((View)param.thisObject).getTouchDelegate().onTouchEvent(me));
	            	//param.setResult(false);
	            }
	        });*/

	        /*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardHostView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) {
					//View mSlidingChallengeLayout = (View) getObjectField(param.thisObject, "mSlidingChallengeLayout");
					//mSlidingChallengeLayout.setBackgroundColor(Color.GREEN);
					//callMethod(mSlidingChallengeLayout, "showChallenge", false);
					View mSecurityViewFlipper = (View) getObjectField(param.thisObject, "mSecurityViewContainer");
					mSecurityViewFlipper.setBackgroundColor(Color.MAGENTA);
					//FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) mSecurityViewFlipper.getLayoutParams();
					//lParams.bottomMargin = 100;
					//lParams.height = 1500;
					//XposedBridge.log("FH" + mSecurityViewFlipper.getHeight());
					//mSecurityViewFlipper.setLayoutParams(lParams);
					//mSecurityViewFlipper.setPadding(0, 0, 0, 0);
					View mSecurityViewContainer = (View) mSecurityViewFlipper.getParent();
					mSecurityViewContainer.setBackgroundColor(Color.YELLOW);
					//FrameLayout.LayoutParams lParams2 = (FrameLayout.LayoutParams) mSecurityViewContainer.getLayoutParams();
					//lParams2.height = 1500;
					//mSecurityViewContainer.setLayoutParams(lParams2);
					//mSecurityViewContainer.setPadding(0, 0, 0, 0);
					//XposedBridge.log("FH" + lParams.height);
					//XposedBridge.log("FgH" + mSecurityViewFlipper.getHeight());
					//XposedBridge.log("CH" + lParams2.height);
					//XposedBridge.log("CgH" + mSecurityViewContainer.getHeight());
				}
			});*/

			findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardHostView", lpparam.classLoader, "onScreenTurnedOn", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) {
					//Object object = getObjectField(param.thisObject, "mSlidingChallengeLayout");
					//callMethod(object, "showChallenge", false);
				}
			});

	    	//Class<?> KeyguardWidgetPager = findClass("com.htc.lockscreen.keyguard.KeyguardWidgetPager", lpparam.classLoader);
	        //hookAllConstructors(KeyguardWidgetPager, new XC_MethodHook() {
			/*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetPager", lpparam.classLoader, "onMeasure", int.class, int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) {
					//if (!hasKeyguardWidgetPagerRun) {
						//param.args[0] = 1640 + 81;
						View parent = (View) param.thisObject;
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
		            	//hasKeyguardWidgetPagerRun = true;
					//}
				}
			});*/

			/*findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader, "setWidgetHeight", int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) {
					//Object object = getObjectField(param.thisObject, "mSlidingChallengeLayout");
					//callMethod(object, "showChallenge", false);
					XposedBridge.log("setWidgetHeight" + param.args[0]);
				}
			});*/

			findAndHookMethod("com.htc.lockscreen.keyguard.SlidingChallengeLayout", lpparam.classLoader, "onMeasure", int.class, int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					//View slidingChallengeLayout = (View) param.thisObject;
	            	View mChallengeView = (View) getObjectField(param.thisObject, "mChallengeView");
	            	//XposedBridge.log("BP: " + slidingChallengeLayout.getPaddingTop());
	            	mChallengeView.setPadding(0, mChallengeView.getPaddingTop() - 81, 0, 0);
					//int i = (int) callMethod(param.thisObject, "getMaxChallengeBottom", (Object[]) null);
					//XposedBridge.log("getMaxChallengeBottom" + i);
					//i = (int) callMethod(param.thisObject, "getMaxChallengeTop", (Object[]) null);
					//XposedBridge.log("getMaxChallengeTop" + i);
					//Rect mInsets = (Rect) param.args[0];
					//mInsets.bottom = 200;
					//XposedBridge.log(mInsets.flattenToString());
				}
			});
		}
        //else if (lpparam.packageName.contains("com.android.internal.widget")) {
        	/*findAndHookMethod("com.android.internal.widget.LockPatternView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	XposedBridge.log(lpparam.packageName);
	            	//XposedBridge.log("dispatchTouchEvent: " + param.getResult());
	            	//View parent = (View) param.thisObject;
	            	//View mLockPatternView = (View) getObjectField(param.thisObject, "mLockPatternView");
	            	MotionEvent me = (MotionEvent) param.args[0];
	            	//XposedBridge.log("dispatchTouchEvent: " + (-mLockPatternView.getLeft()) + ", " + (-mLockPatternView.getTop()));
	            	//me.offsetLocation(-mLockPatternView.getLeft(), -mLockPatternView.getTop());
	            	//parent.dispatchTouchEvent(me);
	            	XposedBridge.log("LockPatternView: " + me.getY());
	            	//XposedBridge.log("Delegate: " + ((View)param.thisObject).getTouchDelegate().onTouchEvent(me));
	            	//param.setResult(false);
	            }
	        });*/
        //}
    }
}