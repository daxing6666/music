package com.music.frame.imageviewpager.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import com.music.R;
import com.music.frame.imageviewpager.ImagePreview;
import com.music.frame.imageviewpager.bean.ImageInfo;
import com.music.frame.imageviewpager.glide.FileTarget;
import com.music.frame.imageviewpager.glide.ImageLoader;
import com.music.frame.imageviewpager.view.helper.FingerDragHelper;
import com.music.frame.imageviewpager.view.helper.SubsamplingScaleImageViewDragClose;
import com.music.frame.utils.ImageUtil;
import com.music.frame.utils.NetworkUtil;
import com.music.frame.utils.Print;
import com.music.frame.utils.ToastUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImagePreviewAdapter extends PagerAdapter {

  private static final String TAG = "ImagePreview";
  private Activity activity;
  private List<ImageInfo> imageInfo;
  private HashMap<String, SubsamplingScaleImageViewDragClose> imageHashMap = new HashMap<>();
  private String finalLoadUrl = "";// 最终加载的图片url
  private int phoneHeight = 0;

  public ImagePreviewAdapter(Activity activity, @NonNull List<ImageInfo> imageInfo) {
    super();
    this.imageInfo = imageInfo;
    this.activity = activity;
    WindowManager windowManager = (WindowManager) activity.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics metric = new DisplayMetrics();
    windowManager.getDefaultDisplay().getMetrics(metric);
    this.phoneHeight = metric.heightPixels;
  }

  public void closePage() {
    try {
      if (imageHashMap != null && imageHashMap.size() > 0) {
        for (Object o : imageHashMap.entrySet()) {
          Map.Entry entry = (Map.Entry) o;
          if (entry != null && entry.getValue() != null) {
            ((SubsamplingScaleImageViewDragClose) entry.getValue()).recycle();
          }
        }
        imageHashMap.clear();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public int getCount() {
    return imageInfo.size();
  }

  /**
   * 加载原图
   */
  public void loadOrigin(final ImageInfo imageInfo) {
    if (imageHashMap.get(imageInfo.getOriginUrl()) != null) {
      final SubsamplingScaleImageViewDragClose imageView = imageHashMap.get(imageInfo.getOriginUrl());
      File cacheFile = ImageLoader.getGlideCacheFile(activity, imageInfo.getOriginUrl());
      if (cacheFile != null && cacheFile.exists()) {
        String thumbnailUrl = imageInfo.getThumbnailUrl();
        File smallCacheFile = ImageLoader.getGlideCacheFile(activity, thumbnailUrl);
        com.music.frame.imageviewpager.view.helper.ImageSource small = null;
        if (smallCacheFile != null && smallCacheFile.exists()) {
          String smallImagePath = smallCacheFile.getAbsolutePath();
          small = com.music.frame.imageviewpager.view.helper.ImageSource.bitmap(ImageUtil.getImageBitmap(smallImagePath, ImageUtil.getBitmapDegree(smallImagePath)));
          int widSmall = ImageUtil.getWidthHeight(smallImagePath)[0];
          int heiSmall = ImageUtil.getWidthHeight(smallImagePath)[1];
          small.dimensions(widSmall, heiSmall);
        }

        String imagePath = cacheFile.getAbsolutePath();
        com.music.frame.imageviewpager.view.helper.ImageSource origin =
                com.music.frame.imageviewpager.view.helper.ImageSource.uri(imagePath);
        int widOrigin = ImageUtil.getWidthHeight(imagePath)[0];
        int heiOrigin = ImageUtil.getWidthHeight(imagePath)[1];
        origin.dimensions(widOrigin, heiOrigin);

        boolean isLongImage = ImageUtil.isLongImage(imagePath);
        Print.d(TAG, "isLongImage = " + isLongImage);
        if (isLongImage) {
          imageView.setOrientation(ImageUtil.getOrientation(imagePath));
          imageView.setMinimumScaleType(SubsamplingScaleImageViewDragClose.SCALE_TYPE_START);
        }
        imageView.setImage(origin, small);
      }
    } else {
      notifyDataSetChanged();
    }
  }

  @NonNull
  @Override
  public Object instantiateItem(@NonNull ViewGroup container, final int position) {
    if (activity == null) {
      return container;
    }
    View convertView = View.inflate(activity, R.layout.item_photoview, null);
    final ProgressBar progressBar = convertView.findViewById(R.id.progress_view);
    final SubsamplingScaleImageViewDragClose imageView = convertView.findViewById(R.id.photo_view);
    final FingerDragHelper fingerDragHelper = convertView.findViewById(R.id.fingerDragHelper);

    imageView.setMinimumScaleType(SubsamplingScaleImageViewDragClose.SCALE_TYPE_CENTER_INSIDE);
    imageView.setDoubleTapZoomStyle(SubsamplingScaleImageViewDragClose.ZOOM_FOCUS_CENTER);
    imageView.setDoubleTapZoomDuration(ImagePreview.getInstance().getZoomTransitionDuration());
    imageView.setMinScale(ImagePreview.getInstance().getMinScale());
    imageView.setMaxScale(ImagePreview.getInstance().getMaxScale());
    imageView.setDoubleTapZoomScale(ImagePreview.getInstance().getMediumScale());

    if (ImagePreview.getInstance().isEnableClickClose()) {
      imageView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          activity.finish();
        }
      });
    }

    if (ImagePreview.getInstance().isEnableDragClose()) {
      fingerDragHelper.setOnAlphaChangeListener(new FingerDragHelper.onAlphaChangedListener() {
        @Override
        public void onTranslationYChanged(MotionEvent event, float translationY) {
          float yAbs = Math.abs(translationY);
          float percent = yAbs / phoneHeight;
          float number = 1.0F - percent;

          if (activity instanceof ImagePreviewActivity) {
            ((ImagePreviewActivity) activity).setAlpha(number);
          }

          imageView.setScaleX(number);
          imageView.setScaleY(number);
        }
      });
    }

    final ImageInfo info = this.imageInfo.get(position);
    final String originPathUrl = info.getOriginUrl();
    final String thumbPathUrl = info.getThumbnailUrl();

    finalLoadUrl = thumbPathUrl;
    ImagePreview.LoadStrategy loadStrategy = ImagePreview.getInstance().getLoadStrategy();

    if (imageHashMap.containsKey(originPathUrl)) {
      imageHashMap.remove(originPathUrl);
    }
    imageHashMap.put(originPathUrl, imageView);

    // 判断原图缓存是否存在，存在的话，直接显示原图缓存，优先保证清晰。
    File cacheFile = ImageLoader.getGlideCacheFile(activity, originPathUrl);
    if (cacheFile != null && cacheFile.exists()) {
      String imagePath = cacheFile.getAbsolutePath();
      boolean isLongImage = ImageUtil.isLongImage(imagePath);
      Print.d(TAG, "isLongImage = " + isLongImage);
      if (isLongImage) {
        imageView.setOrientation(ImageUtil.getOrientation(imagePath));
        imageView.setMinimumScaleType(SubsamplingScaleImageViewDragClose.SCALE_TYPE_START);
      }
      imageView.setImage(com.music.frame.imageviewpager.view.helper.ImageSource.uri(Uri.fromFile(new File(cacheFile.getAbsolutePath()))));
      progressBar.setVisibility(View.GONE);
    } else {
      // 根据当前加载策略判断，需要加载的url是哪一个
      if (loadStrategy == ImagePreview.LoadStrategy.Default) {
        finalLoadUrl = thumbPathUrl;
      } else if (loadStrategy == ImagePreview.LoadStrategy.AlwaysOrigin) {
        finalLoadUrl = originPathUrl;
      } else if (loadStrategy == ImagePreview.LoadStrategy.AlwaysThumb) {
        finalLoadUrl = thumbPathUrl;
      } else if (loadStrategy == ImagePreview.LoadStrategy.NetworkAuto) {
        if (NetworkUtil.isWiFi(activity)) {
          finalLoadUrl = originPathUrl;
        } else {
          finalLoadUrl = thumbPathUrl;
        }
      }
      finalLoadUrl = finalLoadUrl.trim();
      Print.d(TAG, "finalLoadUrl == " + finalLoadUrl);
      final String url = finalLoadUrl;

      Glide.with(activity).downloadOnly().load(url).addListener(new RequestListener<File>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target,
                                    boolean isFirstResource) {

          Glide.with(activity).downloadOnly().load(url).addListener(new RequestListener<File>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target,
                                        boolean isFirstResource) {

              Glide.with(activity).downloadOnly().load(url).addListener(new RequestListener<File>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target,
                                            boolean isFirstResource) {

                  progressBar.setVisibility(View.GONE);
                  String errorMsg = "加载失败";
                  if (e != null) {
                    errorMsg = errorMsg.concat(":\n").concat(e.getMessage());
                  }
                  if (errorMsg.length() > 200) {
                    errorMsg = errorMsg.substring(0, 199);
                  }
                  ToastUtil.getInstance()._short(activity.getApplicationContext(), errorMsg);
                  return true;
                }

                @Override
                public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource,
                                               boolean isFirstResource) {
                  String imagePath = resource.getAbsolutePath();
                  boolean isLongImage = ImageUtil.isLongImage(imagePath);
                  Print.d(TAG, "isLongImage = " + isLongImage);
                  if (isLongImage) {
                    imageView.setOrientation(ImageUtil.getOrientation(imagePath));
                    imageView.setMinimumScaleType(SubsamplingScaleImageViewDragClose.SCALE_TYPE_START);
                  }
                  imageView.setImage(com.music.frame.imageviewpager.view.helper.ImageSource.uri(Uri.fromFile(new File(resource.getAbsolutePath()))));
                  progressBar.setVisibility(View.GONE);
                  return true;
                }
              }).into(new FileTarget() {
                @Override
                public void onLoadStarted(@Nullable Drawable placeholder) {
                  super.onLoadStarted(placeholder);
                  progressBar.setVisibility(View.VISIBLE);
                }
              });
              return true;
            }

            @Override
            public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource,
                                           boolean isFirstResource) {
              String imagePath = resource.getAbsolutePath();
              boolean isLongImage = ImageUtil.isLongImage(imagePath);
              Print.d(TAG, "isLongImage = " + isLongImage);
              if (isLongImage) {
                imageView.setOrientation(ImageUtil.getOrientation(imagePath));
                imageView.setMinimumScaleType(SubsamplingScaleImageViewDragClose.SCALE_TYPE_START);
              }
              imageView.setImage(com.music.frame.imageviewpager.view.helper.ImageSource.uri(Uri.fromFile(new File(resource.getAbsolutePath()))));
              progressBar.setVisibility(View.GONE);
              return true;
            }
          }).into(new FileTarget() {
            @Override
            public void onLoadStarted(@Nullable Drawable placeholder) {
              super.onLoadStarted(placeholder);
              progressBar.setVisibility(View.VISIBLE);
            }
          });
          return true;
        }

        @Override
        public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource,
                                       boolean isFirstResource) {
          String imagePath = resource.getAbsolutePath();
          boolean isLongImage = ImageUtil.isLongImage(imagePath);
          Print.d(TAG, "isLongImage = " + isLongImage);
          if (isLongImage) {
            imageView.setOrientation(ImageUtil.getOrientation(imagePath));
            imageView.setMinimumScaleType(SubsamplingScaleImageViewDragClose.SCALE_TYPE_START);
          }
          imageView.setImage(com.music.frame.imageviewpager.view.helper.ImageSource.uri(Uri.fromFile(new File(resource.getAbsolutePath()))));
          progressBar.setVisibility(View.GONE);
          return true;
        }
      }).into(new FileTarget() {
        @Override
        public void onLoadStarted(@Nullable Drawable placeholder) {
          super.onLoadStarted(placeholder);
          progressBar.setVisibility(View.VISIBLE);
        }
      });
    }
    container.addView(convertView);
    return convertView;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    try {
      container.removeView((View) object);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      ImageLoader.clearMemory(activity);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setPrimaryItem(ViewGroup container, int position, final Object object) {
    super.setPrimaryItem(container, position, object);
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override
  public int getItemPosition(Object object) {
    return POSITION_NONE;
  }
}