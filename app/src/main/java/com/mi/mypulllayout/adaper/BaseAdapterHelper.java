package com.mi.mypulllayout.adaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.util.Linkify;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Allows an abstraction of the ViewHolder pattern.<br>
 * <br>
 * <p/>
 * <b>Usage</b>
 * <p/>
 * 
 * <pre>
 * return BaseAdapterHelper.get(context, convertView, parent, R.layout.item)
 * 		.setText(R.id.tvName, contact.getName())
 * 		.setText(R.id.tvEmails, contact.getEmails().toString())
 * 		.setText(R.id.tvNumbers, contact.getNumbers().toString()).getView();
 * </pre>
 */
public class BaseAdapterHelper {

	/** Views indexed with their IDs */
	private final SparseArray<View> views;

	private int position;

	private View convertView;

	private BaseAdapterHelper(Context context, ViewGroup parent, int layoutId,
							  int position) {
		this.position = position;
		this.views = new SparseArray<View>();
		convertView = LayoutInflater.from(context) //
				.inflate(layoutId, parent, false);
		convertView.setTag(this);
	}

	/**
	 * This method is the only entry point to get a BaseAdapterHelper.
	 * 
	 * @param context
	 *            The current context.
	 * @param convertView
	 *            The convertView arg passed to the getView() method.
	 * @param parent
	 *            The parent arg passed to the getView() method.
	 * @return A BaseAdapterHelper instance.
	 */
	public static BaseAdapterHelper get(Context context, View convertView,
										ViewGroup parent, int layoutId) {
		return get(context, convertView, parent, layoutId, -1);
	}

	/** This method is package private and should only be used by QuickAdapter. */
	static BaseAdapterHelper get(Context context, View convertView,
								 ViewGroup parent, int layoutId, int position) {
		if (convertView == null) {
			return new BaseAdapterHelper(context, parent, layoutId, position);
		}
		BaseAdapterHelper adapter = (BaseAdapterHelper) convertView.getTag();
		adapter.setPosition(position);
		return adapter;
	}

	/**
	 * This method allows you to retrieve a view and perform custom operations
	 * on it, not covered by the BaseAdapterHelper.<br/>
	 * If you think it's a common use case, please consider creating a new issue
	 * at https://github.com/JoanZapata/base-adapter-helper/issues.
	 * 
	 * @param viewId
	 *            The id of the view you want to retrieve.
	 */
	public <T extends View> T getView(int viewId) {
		return retrieveView(viewId);
	}

	/**
	 * Will set the text of a TextView.
	 * 
	 * @param viewId
	 *            The view id.
	 * @param value
	 *            The text to put in the text view.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper setText(int viewId, String value) {
		TextView view = retrieveView(viewId);
		view.setText(value);
		return this;
	}


	
	/**
	 * Will set the textColor of a TextView.
	 * 
	 * @param viewId
	 *            The view id.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper setTextColor(int viewId, int color) {
		TextView view = retrieveView(viewId);
		view.setTextColor(color);
		return this;
	}
	
	public BaseAdapterHelper setTextEnabled(int viewId,Boolean enabled){
		TextView view = retrieveView(viewId);
		view.setEnabled(enabled);
		return this;
	}
	
	public BaseAdapterHelper setTextOnClickListener(int viewId,OnClickListener listener){
		TextView view = retrieveView(viewId);
		view.setOnClickListener(listener);
		return this;
	}
	
	public BaseAdapterHelper setLayoutOnClickListener(int viewId,OnClickListener listener){
		RelativeLayout view = retrieveView(viewId);
		view.setOnClickListener(listener);
		return this;
	}
	
	public BaseAdapterHelper setImageOnClickListener(int viewId,OnClickListener listener){
		ImageView view = retrieveView(viewId);
		view.setOnClickListener(listener);
		return this;
	}
	/**
	 * Will set the view of an View from a resource id.
	 * 
	 * @param viewId
	 *            The view id.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper setView(int viewId,int resid){
		View view = retrieveView(viewId);
		view.setBackgroundResource(resid);
		return this;
	}

	/**
	 * Will set the image of an ImageView from a resource id.
	 * 
	 * @param viewId
	 *            The view id.
	 * @param imageResId
	 *            The image resource id.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper setImageResource(int viewId, int imageResId) {
		ImageView view = retrieveView(viewId);
		view.setImageResource(imageResId);
		return this;
	}

	/**
	 * Will set the image of an ImageView from a drawable.
	 * 
	 * @param viewId
	 *            The view id.
	 * @param drawable
	 *            The image drawable.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper setImageDrawable(int viewId, Drawable drawable) {
		ImageView view = retrieveView(viewId);
		view.setImageDrawable(drawable);
		return this;
	}

	/**
	 * 
	 * @param viewId
	 * @param url
	 * @param imageLoader
	 * @param width
	 * @param height
	 * @param isNeedScaled
	 * @param type
	 * @param config
	 * @return
	 */
//	public BaseAdapterHelper setImageUrl(int viewId, String url,
//			ImageLoader imageLoader, int width, int height,
//			boolean isNeedScaled, ImageType type, MCImageHandleInterface config) {
//		ImageView view = retrieveView(viewId);
//		view.setImageUrl(url, imageLoader, width, height, isNeedScaled, type,
//				config);
//		return this;
//	}

	/**
	 * Add an action to set the image of an image view. Can be called multiple
	 * times.
	 */
	public BaseAdapterHelper setImageBitmap(int viewId, Bitmap bitmap) {
		ImageView view = retrieveView(viewId);
		view.setImageBitmap(bitmap);
		return this;
	}

	/**
	 * Add an action to set the alpha of a view. Can be called multiple times.
	 * Alpha between 0-1.
	 */
	public BaseAdapterHelper setAlpha(int viewId, float value) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			retrieveView(viewId).setAlpha(value);
		} else {
			// Pre-honeycomb hack to set Alpha value
			AlphaAnimation alpha = new AlphaAnimation(value, value);
			alpha.setDuration(0);
			alpha.setFillAfter(true);
			retrieveView(viewId).startAnimation(alpha);
		}
		return this;
	}

	/**
	 * Set a view visibility to VISIBLE (true) or GONE (false).
	 * 
	 * @param viewId
	 *            The view id.
	 * @param visible
	 *            True for VISIBLE, false for GONE.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper setVisible(int viewId, boolean visible) {
		View view = retrieveView(viewId);
		view.setVisibility(visible ? View.VISIBLE : View.GONE);
		return this;
	}
	
	public BaseAdapterHelper setVisible(int viewId, int invisible) {
		View view = retrieveView(viewId);
		view.setVisibility(View.INVISIBLE);
		return this;
	}

	/**
	 * Add links into a TextView.
	 * 
	 * @param viewId
	 *            The id of the TextView to linkify.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper linkify(int viewId) {
		TextView view = retrieveView(viewId);
		Linkify.addLinks(view, Linkify.ALL);
		return this;
	}

	/** Apply the typeface to the given viewId */
	public BaseAdapterHelper setTypeface(int viewId, Typeface typeface) {
		TextView view = retrieveView(viewId);
		view.setTypeface(typeface);
		return this;
	}

	/** Apply the typeface to all the given viewIds */
	public BaseAdapterHelper setTypeface(Typeface typeface, int... viewIds) {
		for (int viewId : viewIds) {
			TextView view = retrieveView(viewId);
			view.setTypeface(typeface);
		}
		return this;
	}

	/**
	 * Sets the progress of a ProgressBar.
	 * 
	 * @param viewId
	 *            The view id.
	 * @param progress
	 *            The progress.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper setProgress(int viewId, int progress) {
		ProgressBar view = retrieveView(viewId);
		view.setProgress(progress);
		return this;
	}

	/**
	 * Sets the progress and max of a ProgressBar.
	 * 
	 * @param viewId
	 *            The view id.
	 * @param progress
	 *            The progress.
	 * @param max
	 *            The max value of a ProgressBar.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper setProgress(int viewId, int progress, int max) {
		ProgressBar view = retrieveView(viewId);
		view.setProgress(progress);
		view.setMax(max);
		return this;
	}

	/**
	 * Sets the range of a ProgressBar to 0...max.
	 * 
	 * @param viewId
	 *            The view id.
	 * @param max
	 *            The max value of a ProgressBar.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper setMax(int viewId, int max) {
		ProgressBar view = retrieveView(viewId);
		view.setMax(max);
		return this;
	}

	/**
	 * Sets the rating (the number of stars filled) of a RatingBar.
	 * 
	 * @param viewId
	 *            The view id.
	 * @param rating
	 *            The rating.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper setRating(int viewId, float rating) {
		RatingBar view = retrieveView(viewId);
		view.setRating(rating);
		return this;
	}

	/**
	 * Sets the rating (the number of stars filled) and max of a RatingBar.
	 * 
	 * @param viewId
	 *            The view id.
	 * @param rating
	 *            The rating.
	 * @param max
	 *            The range of the RatingBar to 0...max.
	 * @return The BaseAdapterHelper for chaining.
	 */
	public BaseAdapterHelper setRating(int viewId, float rating, int max) {
		RatingBar view = retrieveView(viewId);
		view.setRating(rating);
		view.setMax(max);
		return this;
	}

	/** Retrieve the convertView */
	public View getView() {
		return convertView;
	}

	/**
	 * Retrieve the overall position of the data in the list.
	 * 
	 * @throws IllegalArgumentException
	 *             If the position hasn't been set at the construction of the
	 *             this helper.
	 */
	public int getPosition() {
		if (position == -1)
			throw new IllegalStateException(
					"Use BaseAdapterHelper constructor "
							+ "with position if you need to retrieve the position.");
		return position;
	}

	/**
	 * 
	 * @param position	
	 * @return void
	 * @date 2015-6-1 下午4:27:45
	 */
	private void setPosition(int position) {
		this.position = position;
	}

	@SuppressWarnings("unchecked")
	private <T extends View> T retrieveView(int viewId) {
		View view = views.get(viewId);
		if (view == null) {
			view = convertView.findViewById(viewId);
			views.put(viewId, view);
		}
		return (T) view;
	}

}
