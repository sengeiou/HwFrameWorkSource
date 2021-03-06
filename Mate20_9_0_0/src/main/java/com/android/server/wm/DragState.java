package com.android.server.wm;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Point;
import android.hardware.input.InputManager;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.Display;
import android.view.DragEvent;
import android.view.InputChannel;
import android.view.SurfaceControl;
import android.view.SurfaceControl.Transaction;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.internal.view.IDragAndDropPermissions;
import com.android.server.LocalServices;
import com.android.server.input.InputApplicationHandle;
import com.android.server.input.InputWindowHandle;
import com.android.server.usb.descriptors.UsbACInterface;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

class DragState {
    private static final String ANIMATED_PROPERTY_ALPHA = "alpha";
    private static final String ANIMATED_PROPERTY_SCALE = "scale";
    private static final String ANIMATED_PROPERTY_X = "x";
    private static final String ANIMATED_PROPERTY_Y = "y";
    private static final int DRAG_FLAGS_URI_ACCESS = 3;
    private static final int DRAG_FLAGS_URI_PERMISSIONS = 195;
    private static final long MAX_ANIMATION_DURATION_MS = 375;
    private static final long MIN_ANIMATION_DURATION_MS = 195;
    volatile boolean mAnimationCompleted = false;
    private ValueAnimator mAnimator;
    boolean mCrossProfileCopyAllowed;
    private final Interpolator mCubicEaseOutInterpolator = new DecelerateInterpolator(1.5f);
    float mCurrentX;
    float mCurrentY;
    ClipData mData;
    ClipDescription mDataDescription;
    DisplayContent mDisplayContent;
    private Point mDisplaySize = new Point();
    final DragDropController mDragDropController;
    boolean mDragInProgress;
    boolean mDragResult;
    int mFlags;
    InputInterceptor mInputInterceptor;
    IBinder mLocalWin;
    ArrayList<WindowState> mNotifiedWindows;
    float mOriginalAlpha;
    float mOriginalX;
    float mOriginalY;
    int mPid;
    final WindowManagerService mService;
    int mSourceUserId;
    SurfaceControl mSurfaceControl;
    WindowState mTargetWindow;
    float mThumbOffsetX;
    float mThumbOffsetY;
    IBinder mToken;
    int mTouchSource;
    int mUid;

    private class AnimationListener implements AnimatorUpdateListener, AnimatorListener {
        private AnimationListener() {
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            Throwable th;
            Transaction transaction = new Transaction();
            try {
                transaction.setPosition(DragState.this.mSurfaceControl, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_X)).floatValue(), ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_Y)).floatValue());
                transaction.setAlpha(DragState.this.mSurfaceControl, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_ALPHA)).floatValue());
                transaction.setMatrix(DragState.this.mSurfaceControl, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_SCALE)).floatValue(), 0.0f, 0.0f, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_SCALE)).floatValue());
                transaction.apply();
                transaction.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }

        public void onAnimationStart(Animator animator) {
        }

        public void onAnimationCancel(Animator animator) {
        }

        public void onAnimationRepeat(Animator animator) {
        }

        public void onAnimationEnd(Animator animator) {
            DragState.this.mAnimationCompleted = true;
            DragState.this.mDragDropController.sendHandlerMessage(2, null);
        }
    }

    class InputInterceptor {
        InputChannel mClientChannel;
        InputApplicationHandle mDragApplicationHandle = new InputApplicationHandle(null);
        InputWindowHandle mDragWindowHandle;
        DragInputEventReceiver mInputEventReceiver;
        InputChannel mServerChannel;

        InputInterceptor(Display display) {
            InputChannel[] channels = InputChannel.openInputChannelPair("drag");
            this.mServerChannel = channels[0];
            this.mClientChannel = channels[1];
            DragState.this.mService.mInputManager.registerInputChannel(this.mServerChannel, null);
            this.mInputEventReceiver = new DragInputEventReceiver(this.mClientChannel, DragState.this.mService.mH.getLooper(), DragState.this.mDragDropController);
            this.mDragWindowHandle = new InputWindowHandle(this.mDragApplicationHandle, null, null, display.getDisplayId());
            this.mDragWindowHandle.name = "drag";
            this.mDragWindowHandle.inputChannel = this.mServerChannel;
            this.mDragWindowHandle.layer = DragState.this.getDragLayerLocked();
            this.mDragWindowHandle.layoutParamsFlags = 0;
            this.mDragWindowHandle.layoutParamsType = 2016;
            this.mDragWindowHandle.dispatchingTimeoutNanos = 5000000000L;
            this.mDragWindowHandle.visible = true;
            this.mDragWindowHandle.canReceiveKeys = false;
            this.mDragWindowHandle.hasFocus = true;
            this.mDragWindowHandle.hasWallpaper = false;
            this.mDragWindowHandle.paused = false;
            this.mDragWindowHandle.ownerPid = Process.myPid();
            this.mDragWindowHandle.ownerUid = Process.myUid();
            this.mDragWindowHandle.inputFeatures = 0;
            this.mDragWindowHandle.scaleFactor = 1.0f;
            this.mDragWindowHandle.touchableRegion.setEmpty();
            this.mDragWindowHandle.frameLeft = 0;
            this.mDragWindowHandle.frameTop = 0;
            this.mDragWindowHandle.frameRight = DragState.this.mDisplaySize.x;
            this.mDragWindowHandle.frameBottom = DragState.this.mDisplaySize.y;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d("WindowManager", "Pausing rotation during drag");
            }
            DragState.this.mService.pauseRotationLocked();
        }

        void tearDown() {
            DragState.this.mService.mInputManager.unregisterInputChannel(this.mServerChannel);
            this.mInputEventReceiver.dispose();
            this.mInputEventReceiver = null;
            this.mClientChannel.dispose();
            this.mServerChannel.dispose();
            this.mClientChannel = null;
            this.mServerChannel = null;
            this.mDragWindowHandle = null;
            this.mDragApplicationHandle = null;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d("WindowManager", "Resuming rotation after drag");
            }
            DragState.this.mService.resumeRotationLocked();
        }
    }

    DragState(WindowManagerService service, DragDropController controller, IBinder token, SurfaceControl surface, int flags, IBinder localWin) {
        this.mService = service;
        this.mDragDropController = controller;
        this.mToken = token;
        this.mSurfaceControl = surface;
        this.mFlags = flags;
        this.mLocalWin = localWin;
        this.mNotifiedWindows = new ArrayList();
    }

    void closeLocked() {
        if (this.mInputInterceptor != null) {
            this.mDragDropController.sendHandlerMessage(1, this.mInputInterceptor);
            this.mInputInterceptor = null;
            this.mService.mInputMonitor.updateInputWindowsLw(true);
        }
        if (this.mDragInProgress) {
            int myPid = Process.myPid();
            Iterator it = this.mNotifiedWindows.iterator();
            while (it.hasNext()) {
                WindowState ws = (WindowState) it.next();
                float x = 0.0f;
                float y = 0.0f;
                if (!this.mDragResult && ws.mSession.mPid == this.mPid) {
                    x = this.mCurrentX;
                    y = this.mCurrentY;
                }
                DragEvent evt = DragEvent.obtain(4, x, y, null, null, null, null, this.mDragResult);
                try {
                    ws.mClient.dispatchDragEvent(evt);
                } catch (RemoteException e) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unable to drag-end window ");
                    stringBuilder.append(ws);
                    Slog.w("WindowManager", stringBuilder.toString());
                }
                if (myPid != ws.mSession.mPid) {
                    evt.recycle();
                }
            }
            this.mNotifiedWindows.clear();
            this.mDragInProgress = false;
        }
        if (isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
            this.mService.restorePointerIconLocked(this.mDisplayContent, this.mCurrentX, this.mCurrentY);
            this.mTouchSource = 0;
        }
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.destroy();
            this.mSurfaceControl = null;
        }
        if (!(this.mAnimator == null || this.mAnimationCompleted)) {
            Slog.wtf("WindowManager", "Unexpectedly destroying mSurfaceControl while animation is running");
        }
        this.mFlags = 0;
        this.mLocalWin = null;
        this.mToken = null;
        this.mData = null;
        this.mThumbOffsetY = 0.0f;
        this.mThumbOffsetX = 0.0f;
        this.mNotifiedWindows = null;
        this.mDragDropController.onDragStateClosedLocked(this);
    }

    InputChannel getInputChannel() {
        return this.mInputInterceptor == null ? null : this.mInputInterceptor.mServerChannel;
    }

    InputWindowHandle getInputWindowHandle() {
        return this.mInputInterceptor == null ? null : this.mInputInterceptor.mDragWindowHandle;
    }

    void register(Display display) {
        display.getRealSize(this.mDisplaySize);
        if (this.mInputInterceptor != null) {
            Slog.e("WindowManager", "Duplicate register of drag input channel");
            return;
        }
        this.mInputInterceptor = new InputInterceptor(display);
        this.mService.mInputMonitor.updateInputWindowsLw(true);
    }

    int getDragLayerLocked() {
        return (this.mService.mPolicy.getWindowLayerFromTypeLw(2016) * 10000) + 1000;
    }

    void broadcastDragStartedLocked(float touchX, float touchY) {
        this.mCurrentX = touchX;
        this.mOriginalX = touchX;
        this.mCurrentY = touchY;
        this.mOriginalY = touchY;
        this.mDataDescription = this.mData != null ? this.mData.getDescription() : null;
        this.mNotifiedWindows.clear();
        this.mDragInProgress = true;
        this.mSourceUserId = UserHandle.getUserId(this.mUid);
        this.mCrossProfileCopyAllowed = 1 ^ ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class)).getUserRestriction(this.mSourceUserId, "no_cross_profile_copy_paste");
        this.mDisplayContent.forAllWindows((Consumer) new -$$Lambda$DragState$-yUFIMrhYYccZ0gwd6eVcpAE93o(this, touchX, touchY), false);
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendDragStartedLocked(WindowState newWin, float touchX, float touchY, ClipDescription desc) {
        if (this.mDragInProgress && isValidDropTarget(newWin)) {
            DragEvent event = obtainDragEvent(newWin, 1, touchX, touchY, null, desc, null, null, false);
            try {
                newWin.mClient.dispatchDragEvent(event);
                this.mNotifiedWindows.add(newWin);
                if (Process.myPid() == newWin.mSession.mPid) {
                    return;
                }
            } catch (RemoteException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to drag-start window ");
                stringBuilder.append(newWin);
                Slog.w("WindowManager", stringBuilder.toString());
                if (Process.myPid() == newWin.mSession.mPid) {
                    return;
                }
            } catch (Throwable th) {
                if (Process.myPid() != newWin.mSession.mPid) {
                    event.recycle();
                }
                throw th;
            }
            event.recycle();
        }
    }

    private boolean isValidDropTarget(WindowState targetWin) {
        boolean z = false;
        if (targetWin == null || !targetWin.isPotentialDragTarget()) {
            return false;
        }
        int displayId = this.mDisplayContent == null ? -1 : this.mDisplayContent.getDisplayId();
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId) && displayId != targetWin.getDisplayId()) {
            return false;
        }
        if (((this.mFlags & 256) == 0 || !targetWindowSupportsGlobalDrag(targetWin)) && this.mLocalWin != targetWin.mClient.asBinder()) {
            return false;
        }
        if (this.mCrossProfileCopyAllowed || this.mSourceUserId == UserHandle.getUserId(targetWin.getOwningUid())) {
            z = true;
        }
        return z;
    }

    private boolean targetWindowSupportsGlobalDrag(WindowState targetWin) {
        return targetWin.mAppToken == null || targetWin.mAppToken.mTargetSdk >= 24;
    }

    void sendDragStartedIfNeededLocked(WindowState newWin) {
        if (this.mDragInProgress && !isWindowNotified(newWin)) {
            sendDragStartedLocked(newWin, this.mCurrentX, this.mCurrentY, this.mDataDescription);
        }
    }

    private boolean isWindowNotified(WindowState newWin) {
        Iterator it = this.mNotifiedWindows.iterator();
        while (it.hasNext()) {
            if (((WindowState) it.next()) == newWin) {
                return true;
            }
        }
        return false;
    }

    void endDragLocked() {
        if (this.mAnimator == null) {
            if (this.mDragResult) {
                closeLocked();
            } else {
                this.mAnimator = createReturnAnimationLocked();
            }
        }
    }

    void cancelDragLocked() {
        if (this.mAnimator == null) {
            if (this.mDragInProgress) {
                this.mAnimator = createCancelAnimationLocked();
            } else {
                closeLocked();
            }
        }
    }

    void notifyMoveLocked(float x, float y) {
        if (this.mAnimator == null) {
            int displayid = this.mDisplayContent.getDisplayId();
            float thumbCenterX = this.mThumbOffsetX;
            float thumbCenterY = this.mThumbOffsetY;
            if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayid)) {
                int mode = this.mService.getPCScreenDisplayMode();
                if (mode != 0) {
                    int dw = this.mDisplaySize.x;
                    int dh = this.mDisplaySize.y;
                    float pcDisplayScale = mode == 1 ? 0.95f : 0.9f;
                    x = (x * pcDisplayScale) + ((((float) dw) * (1.0f - pcDisplayScale)) / 2.0f);
                    y = (y * pcDisplayScale) + ((((float) dh) * (1.0f - pcDisplayScale)) / 2.0f);
                    thumbCenterX *= pcDisplayScale;
                    thumbCenterY *= pcDisplayScale;
                }
            }
            this.mCurrentX = x;
            this.mCurrentY = y;
            this.mService.openSurfaceTransaction();
            try {
                if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayid)) {
                    this.mSurfaceControl.setPosition(x - thumbCenterX, y - thumbCenterY);
                } else {
                    this.mSurfaceControl.setPosition(x - this.mThumbOffsetX, y - this.mThumbOffsetY);
                }
                this.mService.closeSurfaceTransaction("notifyMoveLw");
                notifyLocationLocked(x, y);
            } catch (Throwable th) {
                this.mService.closeSurfaceTransaction("notifyMoveLw");
            }
        }
    }

    void notifyLocationLocked(float x, float y) {
        float f = x;
        float f2 = y;
        WindowState touchedWin = this.mDisplayContent.getTouchableWinAtPointLocked(f, f2);
        if (!(touchedWin == null || isWindowNotified(touchedWin))) {
            touchedWin = null;
        }
        WindowState touchedWin2 = touchedWin;
        try {
            DragEvent evt;
            int myPid = Process.myPid();
            if (!(touchedWin2 == this.mTargetWindow || this.mTargetWindow == null)) {
                evt = obtainDragEvent(this.mTargetWindow, 6, 0.0f, 0.0f, null, null, null, null, false);
                this.mTargetWindow.mClient.dispatchDragEvent(evt);
                if (myPid != this.mTargetWindow.mSession.mPid) {
                    evt.recycle();
                }
            }
            if (touchedWin2 != null) {
                evt = obtainDragEvent(touchedWin2, 2, f, f2, null, null, null, null, false);
                touchedWin2.mClient.dispatchDragEvent(evt);
                if (myPid != touchedWin2.mSession.mPid) {
                    evt.recycle();
                }
            }
        } catch (RemoteException e) {
            Slog.w("WindowManager", "can't send drag notification to windows");
        }
        this.mTargetWindow = touchedWin2;
    }

    /* JADX WARNING: Missing block: B:24:0x008e, code skipped:
            if (r11 != r13.mSession.mPid) goto L_0x0090;
     */
    /* JADX WARNING: Missing block: B:25:0x0090, code skipped:
            r2.recycle();
     */
    /* JADX WARNING: Missing block: B:31:0x00b4, code skipped:
            if (r11 == r13.mSession.mPid) goto L_0x00b7;
     */
    /* JADX WARNING: Missing block: B:32:0x00b7, code skipped:
            r1.mToken = r14;
     */
    /* JADX WARNING: Missing block: B:33:0x00b9, code skipped:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void notifyDropLocked(float x, float y) {
        float f = x;
        float f2 = y;
        if (this.mAnimator == null) {
            this.mCurrentX = f;
            this.mCurrentY = f2;
            WindowState touchedWin = this.mDisplayContent.getTouchableWinAtPointLocked(f, f2);
            if (isWindowNotified(touchedWin)) {
                DragAndDropPermissionsHandler dragAndDropPermissionsHandler;
                int targetUserId = UserHandle.getUserId(touchedWin.getOwningUid());
                if ((this.mFlags & 256) == 0 || (this.mFlags & 3) == 0 || this.mData == null) {
                    dragAndDropPermissionsHandler = null;
                } else {
                    DragAndDropPermissionsHandler dragAndDropPermissionsHandler2 = new DragAndDropPermissionsHandler(this.mData, this.mUid, touchedWin.getOwningPackage(), this.mFlags & 195, this.mSourceUserId, targetUserId);
                }
                DragAndDropPermissionsHandler dragAndDropPermissions = dragAndDropPermissionsHandler;
                if (!(this.mSourceUserId == targetUserId || this.mData == null)) {
                    this.mData.fixUris(this.mSourceUserId);
                }
                int myPid = Process.myPid();
                IBinder token = touchedWin.mClient.asBinder();
                int myPid2 = myPid;
                DragEvent evt = obtainDragEvent(touchedWin, 3, f, f2, null, null, this.mData, dragAndDropPermissions, 0);
                try {
                    touchedWin.mClient.dispatchDragEvent(evt);
                    this.mDragDropController.sendTimeoutMessage(0, token);
                } catch (RemoteException e) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("can't send drop notification to win ");
                    stringBuilder.append(touchedWin);
                    Slog.w("WindowManager", stringBuilder.toString());
                    endDragLocked();
                } catch (Throwable th) {
                    if (myPid2 != touchedWin.mSession.mPid) {
                        evt.recycle();
                    }
                }
            } else {
                this.mDragResult = false;
                endDragLocked();
            }
        }
    }

    boolean isInProgress() {
        return this.mDragInProgress;
    }

    private static DragEvent obtainDragEvent(WindowState win, int action, float x, float y, Object localState, ClipDescription description, ClipData data, IDragAndDropPermissions dragAndDropPermissions, boolean result) {
        WindowState windowState = win;
        return DragEvent.obtain(action, windowState.translateToWindowX(x), windowState.translateToWindowY(y), localState, description, data, dragAndDropPermissions, result);
    }

    private ValueAnimator createReturnAnimationLocked() {
        animator = new PropertyValuesHolder[4];
        animator[0] = PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_X, new float[]{this.mCurrentX - this.mThumbOffsetX, this.mOriginalX - this.mThumbOffsetX});
        animator[1] = PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_Y, new float[]{this.mCurrentY - this.mThumbOffsetY, this.mOriginalY - this.mThumbOffsetY});
        animator[2] = PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_SCALE, new float[]{1.0f, 1.0f});
        animator[3] = PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_ALPHA, new float[]{this.mOriginalAlpha, this.mOriginalAlpha / 2.0f});
        animator = ValueAnimator.ofPropertyValuesHolder(animator);
        float translateX = this.mOriginalX - this.mCurrentX;
        float translateY = this.mOriginalY - this.mCurrentY;
        long duration = MIN_ANIMATION_DURATION_MS + ((long) ((Math.sqrt((double) ((translateX * translateX) + (translateY * translateY))) / Math.sqrt((double) ((this.mDisplaySize.x * this.mDisplaySize.x) + (this.mDisplaySize.y * this.mDisplaySize.y)))) * 180.0d));
        AnimationListener listener = new AnimationListener();
        animator.setDuration(duration);
        animator.setInterpolator(this.mCubicEaseOutInterpolator);
        animator.addListener(listener);
        animator.addUpdateListener(listener);
        this.mService.mAnimationHandler.post(new -$$Lambda$DragState$4E4tzlfJ9AKYEiVk7F8SFlBLwPc(animator));
        return animator;
    }

    private ValueAnimator createCancelAnimationLocked() {
        animator = new PropertyValuesHolder[4];
        animator[0] = PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_X, new float[]{this.mCurrentX - this.mThumbOffsetX, this.mCurrentX});
        animator[1] = PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_Y, new float[]{this.mCurrentY - this.mThumbOffsetY, this.mCurrentY});
        animator[2] = PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_SCALE, new float[]{1.0f, 0.0f});
        animator[3] = PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_ALPHA, new float[]{this.mOriginalAlpha, 0.0f});
        animator = ValueAnimator.ofPropertyValuesHolder(animator);
        AnimationListener listener = new AnimationListener();
        animator.setDuration(MIN_ANIMATION_DURATION_MS);
        animator.setInterpolator(this.mCubicEaseOutInterpolator);
        animator.addListener(listener);
        animator.addUpdateListener(listener);
        this.mService.mAnimationHandler.post(new -$$Lambda$DragState$WVn6-eGpkutjNAUr_QLMbFLA5qw(animator));
        return animator;
    }

    private boolean isFromSource(int source) {
        return (this.mTouchSource & source) == source;
    }

    void overridePointerIconLocked(int touchSource) {
        this.mTouchSource = touchSource;
        if (isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1)) {
            int displayid = this.mDisplayContent == null ? -1 : this.mDisplayContent.getDisplayId();
            if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayid)) {
                InputManager.getInstance().setPointerIconType(1000);
            } else {
                InputManager.getInstance().setPointerIconType(1021);
            }
        }
    }
}
