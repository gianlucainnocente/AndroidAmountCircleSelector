package com.gatedev.amountcircleselector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

public class CustomCircleAmountSelector extends FrameLayout {

	public String TAG = CustomCircleAmountSelector.class.getSimpleName();

	private int SELECTOR_RADIUS;

	private enum InternalState {DRAGGING, IDLE};

	private Paint mLowerBackgroundPaint;
	private Paint mUpperBackgroundPaint;
	private Paint mSelectorPaint;
	private Paint mProgressPaint;
	private Paint mUnderstrokePaint;
	private Paint mUpperTextPaint;
	private Paint mLowerTextPaint;
	private Paint mLinesPaint;
	private RectF mArcRect;
	private RectF mCircleRect;
	private Rect mUpperTextBounds;
	private Rect[] mLowerTextBounds;
	private Path mUpperBackgroundPath;
	private Path mLowerBackgroundPath;
	private Path mArcPath;
	private Path mUnderStrokePath;
	private Bitmap mHandlerBitmap;
	private Bitmap mDisabledHandlerBitmap;

	private ValueAnimator mAnimator;
	private GestureDetector mGesture;
	private OnTapListener mTapListener;
	private OnValueChangedListener mListener;
	private InternalState mState = InternalState.IDLE;
	private ArrayList<Target> mTargets = new ArrayList<Target>();
	private String mUpperText;
	private String[] mLowerText;
	private float[] mValues;
	private float mLastSelectedValue = -1;
	private float mLastDownAngle = 0;
	private float mSelectorAngle = 270;
	private float mLastAngle = 0;
	private float mLastVelocity = 0;
	private int mContainerSize;
	private int mSize;
	private boolean mShowLines = true;
	private boolean mShowUpperLine = false;
	private boolean mSkipFirst = true;
	private boolean mShowProgress = true;
	private boolean mIsEnabled = true;

	private SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {

		@Override
		public boolean onSingleTapUp(final MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			if(y > mContainerSize / 2 
					&& x > mContainerSize / 2 - mSize / 2 + Utils.dip2pixel(getContext(), 40)
					&& x < mContainerSize / 2 + mSize / 2 - Utils.dip2pixel(getContext(), 40)) {
				if(mTapListener != null) {
					mTapListener.onTap(mLastSelectedValue);
				}
			}

			return true;
		}

	};

	public CustomCircleAmountSelector(Context context) {
		super(context);	

		init();
	}

	public CustomCircleAmountSelector(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public CustomCircleAmountSelector(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		init();
	}

	private void init() {
		mGesture = new GestureDetector(getContext(), mGestureListener);

		SELECTOR_RADIUS = (int) Utils.dip2pixel(getContext(), 25);

		mUpperTextPaint = new Paint();
		mUpperTextPaint.setColor(Color.WHITE); 
		mUpperTextPaint.setAntiAlias(true);
		mUpperTextPaint.setTextSize(Utils.dip2pixel(getContext(), 40));

		mLowerTextPaint = new Paint();
		mLowerTextPaint.setColor(Color.WHITE); 
		mLowerTextPaint.setAntiAlias(true);
		mLowerTextPaint.setTextSize(Utils.dip2pixel(getContext(), 35));

		mLowerBackgroundPaint = new Paint();
		mLowerBackgroundPaint.setColor(Color.YELLOW); 
		mLowerBackgroundPaint.setAntiAlias(true);

		mUpperBackgroundPaint = new Paint();
		mUpperBackgroundPaint.setColor(Color.RED); 
		mUpperBackgroundPaint.setAntiAlias(true);

		mLinesPaint = new Paint();
		mLinesPaint.setColor(Color.RED); 
		mLinesPaint.setAntiAlias(true);

		mSelectorPaint = new Paint();
		mSelectorPaint.setColor(Color.BLUE); 
		mSelectorPaint.setAntiAlias(true);

		mProgressPaint = new Paint();
		mProgressPaint.setColor(Color.BLUE); 
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setStyle(Paint.Style.STROKE);
		mProgressPaint.setStrokeWidth(Utils.dip2pixel(getContext(), 3));

		mUnderstrokePaint = new Paint();
		mUnderstrokePaint.setColor(Color.BLUE); 
		mUnderstrokePaint.setAntiAlias(true);
		mUnderstrokePaint.setStyle(Paint.Style.STROKE);
		mUnderstrokePaint.setStrokeWidth(Utils.dip2pixel(getContext(), 3));

		mArcRect = new RectF();
		mCircleRect = new RectF();
		mUpperTextBounds = new Rect();

		mLowerBackgroundPath = new Path();
		mUpperBackgroundPath = new Path();
		mArcPath = new Path();
		mUnderStrokePath = new Path();
	}

	public void setValues(float[] values) {
		mValues = values;
		mTargets.clear();

		for(int i = 0; i < mValues.length; i++) {
			mTargets.add(new Target(mValues[i], 0));
		}

		reorderValues();

		float slice = 360 / mTargets.size();
		float angle = 270 + slice;

		for(int i = 0; i < mTargets.size(); i++) {
			mTargets.get(i).setAngle(angle % 360);

			angle += slice;
		}
	}

	public void setValues(int[] values) {
		float[] newval = new float[values.length];

		for(int i = 0; i < values.length; i++) {
			newval[i] = values[i];
		}

		setValues(newval);
	}

	private void reorderValues() {
		Collections.sort(mTargets, new Comparator<Target>() {

			@Override
			public int compare(Target lhs, Target rhs) {
				if(lhs.getValue() > rhs.getValue()) {
					return 1;
				} else if(lhs.getValue() < rhs.getValue()) {
					return -1;
				} else {
					return 0;
				}
			}

		});
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if(getMeasuredWidth() > getMeasuredHeight() || getMeasuredWidth() == 0) {
			mContainerSize = getMeasuredHeight();
		} else {
			mContainerSize = getMeasuredWidth();
		}

		mSize = mContainerSize - SELECTOR_RADIUS * 2;

		mCircleRect.set(
				mContainerSize / 2 - mSize / 2, 
				mContainerSize / 2 - mSize / 2, 
				mSize + SELECTOR_RADIUS, 
				mSize + SELECTOR_RADIUS);

		mArcRect.set(
				mContainerSize / 2 - mSize / 2 - Utils.dip2pixel(getContext(), 4), 
				mContainerSize / 2 - mSize / 2 - Utils.dip2pixel(getContext(), 4), 
				mSize + SELECTOR_RADIUS + Utils.dip2pixel(getContext(), 4), 
				mSize + SELECTOR_RADIUS + Utils.dip2pixel(getContext(), 4));

		mLowerBackgroundPath.reset();
		mUpperBackgroundPath.reset();

		mLowerBackgroundPath.addArc(mCircleRect, 0, 180);
		mUpperBackgroundPath.addArc(mCircleRect, 180, 180);

		setMeasuredDimension(mContainerSize, mContainerSize);

		//LogUtils.debug(TAG, "Width: "+getMeasuredWidth()+"   height:"+getMeasuredHeight());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawPath(mLowerBackgroundPath, mLowerBackgroundPaint);
		canvas.drawPath(mUpperBackgroundPath, mUpperBackgroundPaint);

		mUnderStrokePath.reset();
		mUnderStrokePath.addArc(mArcRect, 0, 360);
		canvas.drawPath(mUnderStrokePath, mUnderstrokePaint); 

		if(mShowProgress) {
			if(mIsEnabled) {
				float sweep = mSelectorAngle % 360;
				if(sweep < 0) {
					sweep += 360;
				}

				mArcPath.reset();
				mArcPath.addArc(mArcRect, -90, (sweep + 90 - 1) % 360);
				canvas.drawPath(mArcPath, mProgressPaint); 
			}
		}

		if(mShowLines && mIsEnabled) {
			for(Target t : mTargets) {
				float tx = computeX(t.getAngle(), mSize / 2 + SELECTOR_RADIUS / 3.5f);
				float ty = computeY(t.getAngle(), mSize / 2 + SELECTOR_RADIUS / 3.5f);

				float ttx = computeX(t.getAngle(), mSize / 2 + SELECTOR_RADIUS + Utils.dip2pixel(getContext(), 3));
				float tty = computeY(t.getAngle(), mSize / 2 + SELECTOR_RADIUS + Utils.dip2pixel(getContext(), 3));

				canvas.drawLine(tx, ty, ttx, tty, mLinesPaint);
			}
		}

		if(mShowUpperLine) {
			float tx = computeX(270, mSize / 2 + SELECTOR_RADIUS / 3.5f);
			float ty = computeY(270, mSize / 2 + SELECTOR_RADIUS / 3.5f);

			float ttx = computeX(270, mSize / 2 + SELECTOR_RADIUS + Utils.dip2pixel(getContext(), 3));
			float tty = computeY(270, mSize / 2 + SELECTOR_RADIUS + Utils.dip2pixel(getContext(), 3));

			canvas.drawLine(tx, ty, ttx, tty, mLinesPaint);
		}

		if(mHandlerBitmap != null) {
			float selectorX = computeX(mSelectorAngle, mSize / 2 + Utils.dip2pixel(getContext(), 3)) - mHandlerBitmap.getWidth() / 2;
			float selectorY = computeY(mSelectorAngle, mSize / 2 + Utils.dip2pixel(getContext(), 3)) - mHandlerBitmap.getHeight() / 2;
			canvas.drawBitmap(mIsEnabled ? mHandlerBitmap : mDisabledHandlerBitmap, selectorX, selectorY, null);
		}

		if(mUpperText != null && mUpperTextBounds != null) {
			canvas.drawText(mUpperText, mContainerSize / 2 - mUpperTextBounds.width() / 2, mContainerSize / 2 - mUpperTextBounds.height(), mUpperTextPaint);
		}

		if(mLowerText != null && mLowerTextBounds != null) {
			float yy = mContainerSize / 2 + mLowerTextBounds[0].height() * 2;

			for(int i = 0; i < mLowerText.length; i++) {
				canvas.drawText(mLowerText[i], mContainerSize / 2 - mLowerTextBounds[i].width() / 2, yy, mLowerTextPaint);
				yy += mLowerTextBounds[i].height() + Utils.dip2pixel(getContext(), 8);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);

		getParent().requestDisallowInterceptTouchEvent(true);

		if(!mIsEnabled) {
			return false;
		}

		mGesture.onTouchEvent(event);

		float tx = event.getX();
		float ty = event.getY();


		float currentAngle = (float) Math.toDegrees(Math.atan2(
				(ty - mContainerSize / 2), 
				(tx - mContainerSize / 2)));

		float velocity = currentAngle - mLastAngle;

		if(velocity != 0) {
			mLastVelocity = velocity;
		}

		mLastAngle = currentAngle;

		switch(event.getActionMasked()) {

		case MotionEvent.ACTION_DOWN:
			if(mState == InternalState.IDLE) {
				if(touchInSelector(tx, ty, mSelectorAngle, SELECTOR_RADIUS)) {
					mState = InternalState.DRAGGING;
					mLastDownAngle = currentAngle;
				}
			}

			break;

		case MotionEvent.ACTION_MOVE:
			if(mState == InternalState.DRAGGING) {
				mSelectorAngle = currentAngle;

				invalidate();

				checkAndUpdateValue();
			}

			break;

		case MotionEvent.ACTION_UP:
			if(mState == InternalState.DRAGGING) {
				mState = InternalState.IDLE;

				float diff = (float) Math.atan2(
						Math.sin(Math.toRadians(mLastDownAngle) - Math.toRadians(mSelectorAngle)), 
						Math.cos(Math.toRadians(mLastDownAngle) - Math.toRadians(mSelectorAngle)));

				if(Math.abs(mLastVelocity) > 1f && Math.abs(Math.toDegrees(diff)) > 10f) {
					animateInertia(mLastVelocity);
				} else {
					animateToTarget(getNearestTarget(mSelectorAngle));
				}
			}

			break;
		}


		return true;
	}

	private void animateToTarget(Target t) {
		float targetAngle = t.getAngle();

		float diff = (float) Math.atan2(
				Math.sin(Math.toRadians(targetAngle) - Math.toRadians(mSelectorAngle)), 
				Math.cos(Math.toRadians(targetAngle) - Math.toRadians(mSelectorAngle)));

		if(targetAngle < 0) {
			targetAngle += 360;
		}

		if(mSelectorAngle < 0) {
			mSelectorAngle += 360;
		}

		float normalDiff = Math.round((targetAngle - mSelectorAngle));

		if(normalDiff != Math.round(Math.toDegrees(diff))) {
			targetAngle = 360 - targetAngle;

			if(targetAngle != t.getAngle() % 360) {
				targetAngle = - targetAngle;
			}
		}

		if(Math.abs(Math.round(Math.toDegrees(diff))) < Math.abs(normalDiff)) {
			targetAngle = targetAngle - 360;
			targetAngle %= 360;
		}

		if(targetAngle < -180) {
			targetAngle += 720;
		}

		if(targetAngle == 0) {
			if(mSelectorAngle > 180) {
				targetAngle = 360;
			}
		}

		mAnimator = ValueAnimator.ofFloat(mSelectorAngle, targetAngle);
		mAnimator.setDuration(500);
		mAnimator.setInterpolator(new OvershootInterpolator(0));
		mAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mSelectorAngle = (Float) animation.getAnimatedValue();

				if(!mIsEnabled) {
					mSelectorAngle = 270;
					mAnimator.cancel();
				}

				invalidate();
			}
		});
		mAnimator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				checkAndUpdateValue();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				checkAndUpdateValue();
			}

		});
		mAnimator.start();
	}

	private void animateInertia(float velocity) {
		if((mLastDownAngle > 0 && mLastAngle < 0 && velocity < 0 && mLastAngle <= -90 && mLastAngle >= -180)
				|| (mLastDownAngle < 0 && mLastAngle > 0 && velocity > 0 && mLastDownAngle < -90)) {
			velocity = -velocity;
		}

		velocity *= 5;

		if(velocity > 100) {
			velocity = 100;
		} else if(velocity < -100) {
			velocity = -100;
		}

		float targetAngle = mSelectorAngle + velocity;

		mAnimator = ValueAnimator.ofFloat(mSelectorAngle, targetAngle);
		mAnimator.setDuration(600);
		mAnimator.setInterpolator(new OvershootInterpolator(0));
		mAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mSelectorAngle = (Float) animation.getAnimatedValue();

				if(!mIsEnabled) {
					mSelectorAngle = 270;
					mAnimator.cancel();
				}

				invalidate();
			}
		});
		mAnimator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				animateToTarget(getNearestTarget(mSelectorAngle));
				checkAndUpdateValue();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				animateToTarget(getNearestTarget(mSelectorAngle));
				checkAndUpdateValue();
			}

		});
		mAnimator.start();
	}

	private void checkAndUpdateValue() {
		mSelectorAngle %= 360;

		if(mSelectorAngle < 0) {
			mSelectorAngle += 360;
		}

		Target selectedTarget = getNearestTarget(mSelectorAngle);
		if(selectedTarget.getValue() != mLastSelectedValue) {
			mLastSelectedValue = selectedTarget.getValue();

			if(mListener != null) {
				mListener.onValueChanged(selectedTarget.getValue());
			}
		}

		if(!mIsEnabled) {
			mSelectorAngle = 270;
		}
	}

	private Target getNearestTarget(float angle) {
		if(angle > 360) {
			angle %= 360;
		}

		Target nearest = null;
		int nearestIndex = 0;
		float minDiff = 99999;

		for(int i = 0; i < mTargets.size(); i++) {
			Target t = mTargets.get(i);

			float diff = (float) Math.atan2(
					Math.sin(Math.toRadians(t.getAngle()) - Math.toRadians(angle)), 
					Math.cos(Math.toRadians(t.getAngle()) - Math.toRadians(angle)));

			if(Math.abs(diff) < minDiff) {
				minDiff = Math.abs(diff);
				nearest = t;
				nearestIndex = i;
			}
		}

		if(mSkipFirst) {
			if(angle < 0) {
				angle += 360;
			}

			if(nearestIndex == mTargets.size() - 1 && angle > nearest.getAngle()) {
				nearest = mTargets.get(0);
			}
		}

		return nearest;
	}

	private boolean touchInSelector(float tx, float ty, float selectorAngle, float radius) {
		float selectorX = computeX(selectorAngle, mSize / 2);
		float selectorY = computeY(selectorAngle, mSize / 2);

		if(tx >= selectorX - radius && tx <= selectorX + radius
				&& ty >= selectorY - radius && ty <= selectorY + radius) {
			return true;
		} else {
			return false;
		}
	}

	public void animateProgressTo(float progress, float total) {
		ValueAnimator mProgressBarAnimator = ValueAnimator.ofFloat(mSelectorAngle, 360 - ((progress * 360) / total) - 90 + 1);
		mProgressBarAnimator.setDuration(1000);
		mProgressBarAnimator.setInterpolator(new LinearInterpolator());
		mProgressBarAnimator.removeAllListeners();
		mProgressBarAnimator.reverse();
		mProgressBarAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(final ValueAnimator animation) {
				mSelectorAngle = (Float) animation.getAnimatedValue();

				invalidate();
			}

		});
		mProgressBarAnimator.start();
	}

	public void setProgressValue(float value) {
		mSelectorAngle = value;
		invalidate();
	}

	private float computeX(float angle, float radius) {
		return (float) (mContainerSize / 2 + radius * Math.cos(Math.toRadians(angle)));
	}

	private float computeY(float angle, float radius) {
		return (float) (mContainerSize / 2 + radius * Math.sin(Math.toRadians(angle)));
	}

	public boolean isShowLines() {
		return mShowLines;
	}

	public void setShowLines(boolean mShowLines) {
		this.mShowLines = mShowLines;
	}

	public boolean isSkipFirst() {
		return mSkipFirst;
	}

	public void setSkipFirst(boolean mSkipFirst) {
		this.mSkipFirst = mSkipFirst;
	}

	public OnValueChangedListener getListener() {
		return mListener;
	}

	public void setListener(OnValueChangedListener listener) {
		this.mListener = listener;
	}

	public OnTapListener getTapListener() {
		return mTapListener;
	}

	public void setTapListener(OnTapListener tapListener) {
		this.mTapListener = tapListener;
	}

	public boolean isShowProgress() {
		return mShowProgress;
	}

	public void setShowProgress(boolean mShowProgress) {
		this.mShowProgress = mShowProgress;
	}

	public void setProgressColor(int progressColor) {
		mProgressPaint.setColor(progressColor);
	}

	public void setUnderstrokeColor(int undestrokeColor) {
		mUnderstrokePaint.setColor(undestrokeColor);
	}

	public void setLowerBackgroundColor(int lowerBackgroundColor) {
		mLowerBackgroundPaint.setColor(lowerBackgroundColor);
	}

	public void setUpperBackgroundColor(int upperBackgroundColor) {
		mUpperBackgroundPaint.setColor(upperBackgroundColor);
	}

	public void setLinesColor(int linesColor) {
		mLinesPaint.setColor(linesColor);
	}

	public Bitmap getHandlerBitmap() {
		return mHandlerBitmap;
	}

	public void setHandlerBitmap(Bitmap mHandlerBitmap) {
		this.mHandlerBitmap = mHandlerBitmap;
	}

	public Bitmap getDisabledHandlerBitmap() {
		return mDisabledHandlerBitmap;
	}

	public void setDisabledHandlerBitmap(Bitmap mDisabledHandlerBitmap) {
		this.mDisabledHandlerBitmap = mDisabledHandlerBitmap;
	}

	public boolean isEnabled() {
		return mIsEnabled;
	}

	public boolean isShowUpperLine() {
		return mShowUpperLine;
	}

	public void setShowUpperLine(boolean mShowUpperLine) {
		this.mShowUpperLine = mShowUpperLine;
	}
	
	public void setUpperTextColor(int color) {
		if(mUpperTextPaint != null) {
			mUpperTextPaint.setColor(color);
		}
	}
	
	public void setLowerTextColor(int color) {
		if(mLowerTextPaint != null) {
			mLowerTextPaint.setColor(color);
		}
	}

	public void setUpperText(String text) {
		if(mUpperTextBounds != null && mUpperTextPaint != null) {
			mLastSelectedValue = -1;
			mUpperText = text;
			mUpperTextPaint.getTextBounds(text, 0, text.length(), mUpperTextBounds);

			invalidate();
		}
	}

	public void setLowerText(String text) {
		mLowerText = text.split("\n");
		mLowerTextBounds = new Rect[mLowerText.length];

		if(mLowerTextBounds != null && mLowerTextPaint != null) {
			for(int i = 0; i < mLowerText.length; i++) {
				mLowerTextBounds[i] = new Rect();
				mLowerTextPaint.getTextBounds(mLowerText[i], 0, mLowerText[i].length(), mLowerTextBounds[i]);
			}

			invalidate();
		}
	}

	public void setEnabled(boolean mIsEnabled) {
		this.mIsEnabled = mIsEnabled;

		if(mIsEnabled) {
			if(mTargets != null && mTargets.size() > 0) {
				animateToTarget(mTargets.get(0));
			}

			mLowerTextPaint.setAlpha(255);
		} else {
			mSelectorAngle = 270;

			mLowerTextPaint.setAlpha(120);
		}

		invalidate();
	}

	public interface OnValueChangedListener {

		void onValueChanged(float value);

	}

	public interface OnTapListener {

		void onTap(float value);

	}

	public static class Target {

		private float value;
		private float angle;

		public Target(float value, float angle) {
			this.value = value;
			this.angle = angle;
		}

		public float getValue() {
			return value;
		}

		public void setValue(float value) {
			this.value = value;
		}

		public float getAngle() {
			return angle;
		}

		public void setAngle(float angle) {
			this.angle = angle;
		}

	}

}
