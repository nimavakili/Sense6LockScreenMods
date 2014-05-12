package com.minimv.senselockscreen;

import java.lang.reflect.Method;
import android.widget.ViewFlipper;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class XposedHooks implements IXposedHookLoadPackage {

	private XSharedPreferences prefs;
	private boolean removeCarrier, panelAlignBottom, nukeHidePanel, nukeHorizontalArrows;
	private String carrierText, hintText;
	
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

	        findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardWidgetFrame", lpparam.classLoader, "drawBg", Canvas.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            		param.setResult(null);
	            }
	        });

	        findAndHookMethod("com.htc.lockscreen.keyguard.HtcKeyguardViewStateManager", lpparam.classLoader, "updateBackground", new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	        		param.setResult(null);
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
	        
	    	/*Class<?> KeyguardWidgetPager = findClass("com.htc.lockscreen.keyguard.KeyguardWidgetPager", lpparam.classLoader);
	        hookAllConstructors(KeyguardWidgetPager, new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                	//XposedBridge.log("KeyguardWidgetPager Construct");
                	//setIntField(param.thisObject, "mEdgeSwipeRegionSize", 0);
	            }
	        });*/

	        findAndHookMethod("com.htc.lockscreen.keyguard.PagedView", lpparam.classLoader, "startScrolling", MotionEvent.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	Object mViewStateManager = getObjectField(param.thisObject, "mViewStateManager");
	            	ViewFlipper mKeyguardSecurityContainer = (ViewFlipper) getObjectField(mViewStateManager, "mKeyguardSecurityContainer");
	            	View security = mKeyguardSecurityContainer.getChildAt(mKeyguardSecurityContainer.getDisplayedChild());
	            	Method isChallengeShowing = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.KeyguardViewStateManager", lpparam.classLoader), "isChallengeShowing");
	            	boolean mChallengeShowing = (boolean) isChallengeShowing.invoke(mViewStateManager, (Object[]) null);
	            	int[] loc = new int[2];
	            	security.getLocationOnScreen(loc);
	            	MotionEvent me = (MotionEvent) param.args[0];
	            	if (security.getClass().getName().contains("HtcPatternUnlockView") && mChallengeShowing && me.getRawY() > loc[1]) {
	            		param.setResult(null);
	            	}
	            }
        	});

	        findAndHookMethod("com.htc.lockscreen.unlockscreen.HtcPatternUnlockView", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	            	boolean result = true;
	            	MotionEvent me = (MotionEvent) param.args[0];
	            	if (!me.isFromSource(66))
	            		result = false;
	            	param.setResult(result);
	            }
	        });

	        findAndHookMethod("com.htc.lockscreen.keyguard.SlidingChallengeLayout", lpparam.classLoader, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
				@Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	            	View mChallengeView = (View) getObjectField(param.thisObject, "mChallengeView");
	            	Method getFlipper = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.KeyguardSecurityContainer", lpparam.classLoader), "getFlipper");
	            	ViewFlipper flipper = (ViewFlipper) getFlipper.invoke(mChallengeView, (Object[]) null);
	            	View security = flipper.getChildAt(flipper.getDisplayedChild());
	            	Method isChallengeShowing = findMethodBestMatch(findClass("com.htc.lockscreen.keyguard.SlidingChallengeLayout", lpparam.classLoader), "isChallengeShowing");
	            	boolean mChallengeShowing = (boolean) isChallengeShowing.invoke(param.thisObject, (Object[]) null);
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
	        });

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

			findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardHostView", lpparam.classLoader, "onScreenTurnedOn", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) {
					//Object object = getObjectField(param.thisObject, "mSlidingChallengeLayout");
					//callMethod(object, "showChallenge", false);
				}
			});

			findAndHookMethod("com.htc.lockscreen.keyguard.SlidingChallengeLayout", lpparam.classLoader, "onMeasure", int.class, int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
	            	View mChallengeView = (View) getObjectField(param.thisObject, "mChallengeView");
	            	mChallengeView.setPadding(0, mChallengeView.getPaddingTop() - 81, 0, 0);
				}
			});
		}
    }
}