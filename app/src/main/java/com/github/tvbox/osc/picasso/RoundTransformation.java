package com.github.tvbox.osc.picasso;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.annotation.IntDef;

import com.squareup.picasso.Transformation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述
 *
 * @author pj567
 * @since 2020/12/22
 */
public class RoundTransformation implements Transformation {
    private int viewWidth, viewHeight;
    @RoundType
    private int mRoundType = RoundType.NONE;
    private int diameter;
    private int radius;
    private boolean isCenterCorp = true;//垂直方向不是中间裁剪，就是顶部
    private String key = "";

    public RoundTransformation(String key) {
        this.key = key;
    }

    public RoundTransformation override(int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;
        return this;
    }

    public RoundTransformation centerCorp(boolean centerCorp) {
        this.isCenterCorp = centerCorp;
        return this;
    }

    public RoundTransformation roundRadius(int radius, @RoundType int mRoundType) {
        this.radius = radius;
        this.diameter = radius * 2;
        this.mRoundType = mRoundType;
        return this;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (viewWidth == 0 || viewHeight == 0) {
            viewWidth = width;
            viewHeight = height;
        }
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        BitmapShader mBitmapShader = new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        if (viewWidth != width || viewHeight != height) {
            //是否以宽计算
            float scale;
            if (width * 1f / viewWidth > height * 1f / viewHeight) {
                scale = viewHeight * 1f / height;
                width = (int) (width * scale);
                height = viewHeight;
            } else {
                scale = viewWidth * 1f / width;
                height = (int) (height * scale);
                width = viewWidth;
            }
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            mBitmapShader.setLocalMatrix(matrix);
        }
        Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(true);
        Canvas mCanvas = new Canvas(bitmap);
        mPaint.setShader(mBitmapShader);
        // mPaint.setAntiAlias(true);
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        drawRoundRect(mCanvas, mPaint, width, height);
        source.recycle();
        return bitmap;
    }

    private void drawRoundRect(Canvas mCanvas, Paint mPaint, float width, float height) {
        switch (mRoundType) {
            case RoundType.NONE:
                if (viewWidth == width && viewHeight == height) {
                    mCanvas.drawRect(new RectF(0, 0, width, height), mPaint);
                } else {
                    if (viewWidth == width && viewHeight != height) {
                        float dis = (height - viewHeight) / 2f;
                        if (isCenterCorp) {
                            mCanvas.translate(0, -dis);
                            mCanvas.drawRect(new RectF(0, dis, viewWidth, viewHeight + dis), mPaint);
                        } else {
                            mCanvas.drawRect(new RectF(0, 0, viewWidth, viewHeight), mPaint);
                        }
                    } else {
                        float dis = (width - viewWidth) / 2f;
                        mCanvas.translate(-dis, 0);
                        mCanvas.drawRect(new RectF(dis, 0, viewWidth + dis, viewHeight), mPaint);
                    }
                }
                break;
            case RoundType.ALL:
                if (viewWidth == width && viewHeight == height) {
                    mCanvas.drawRoundRect(new RectF(0, 0, viewWidth, viewHeight), radius, radius, mPaint);
                } else if (viewWidth == width && viewHeight != height) {
                    float dis = (height - viewHeight) / 2f;
                    if (isCenterCorp) {
                        mCanvas.translate(0, -dis);
                        mCanvas.drawRoundRect(new RectF(0, dis, viewWidth, viewHeight + dis), radius, radius, mPaint);
                    } else {
                        mCanvas.drawRoundRect(new RectF(0, 0, viewWidth, viewHeight), radius, radius, mPaint);
                    }
                } else {
                    float dis = (width - viewWidth) / 2f;
                    mCanvas.translate(-dis, 0);
                    mCanvas.drawRoundRect(new RectF(dis, 0, viewWidth + dis, viewHeight), radius, radius, mPaint);
                }
                break;
            case RoundType.TOP:
                if (viewWidth == width && viewHeight == height) {
                    mCanvas.drawRoundRect(new RectF(0, 0, viewWidth, diameter), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(0, radius, viewWidth, viewHeight), mPaint);
                } else if (viewWidth == width && viewHeight != height) {
                    float dis = (height - viewHeight) / 2f;
                    if (isCenterCorp) {
                        mCanvas.translate(0, -dis);
                        mCanvas.drawRoundRect(new RectF(0, dis, viewWidth, diameter + dis), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, dis + radius, viewWidth, viewHeight + dis), mPaint);
                    } else {
                        mCanvas.drawRoundRect(new RectF(0, 0, viewWidth, diameter), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, radius, viewWidth, viewHeight), mPaint);
                    }
                } else {
                    float dis = (width - viewWidth) / 2f;
                    mCanvas.translate(-dis, 0);
                    mCanvas.drawRoundRect(new RectF(dis, 0, viewWidth + dis, diameter), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(dis, radius, viewWidth + dis, viewHeight), mPaint);
                }
                break;
            case RoundType.RIGHT:
                if (viewWidth == width && viewHeight == height) {
                    mCanvas.drawRoundRect(new RectF(viewWidth - diameter, 0, viewWidth, viewHeight), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(0, 0, viewWidth - radius, viewHeight), mPaint);
                } else if (viewWidth == width && viewHeight != height) {
                    float dis = (height - viewHeight) / 2f;
                    if (isCenterCorp) {
                        mCanvas.translate(0, -dis);
                        mCanvas.drawRoundRect(new RectF(viewWidth - diameter, dis, viewWidth, viewHeight + dis), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, dis, viewWidth - radius, viewHeight + dis), mPaint);
                    } else {
                        mCanvas.drawRoundRect(new RectF(viewWidth - diameter, 0, viewWidth, viewHeight), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, 0, viewWidth - radius, viewHeight), mPaint);
                    }
                } else {
                    float dis = (width - viewWidth) / 2f;
                    mCanvas.translate(-dis, 0);
                    mCanvas.drawRoundRect(new RectF(viewWidth - diameter + dis, 0, viewWidth + dis, viewHeight), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(dis, 0, viewWidth - radius + dis, viewHeight), mPaint);
                }
                break;
            case RoundType.BOTTOM:
                if (viewWidth == width && viewHeight == height) {
                    mCanvas.drawRoundRect(new RectF(0, viewHeight - diameter, viewWidth, viewHeight), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(0, 0, viewWidth, viewHeight - radius), mPaint);
                } else if (viewWidth == width && viewHeight != height) {
                    float dis = (height - viewHeight) / 2f;
                    if (isCenterCorp) {
                        mCanvas.translate(0, -dis);
                        mCanvas.drawRoundRect(new RectF(0, viewHeight - diameter + dis, viewWidth, viewHeight + dis), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, dis, viewWidth, viewHeight - radius + dis), mPaint);
                    } else {
                        mCanvas.drawRoundRect(new RectF(0, viewHeight - diameter, viewWidth, viewHeight), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, 0, viewWidth, viewHeight - radius), mPaint);
                    }
                } else {
                    float dis = (width - viewWidth) / 2f;
                    mCanvas.translate(-dis, 0);
                    mCanvas.drawRoundRect(new RectF(dis, viewHeight - diameter, viewWidth + dis, viewHeight), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(dis, 0, viewWidth + dis, viewHeight - radius), mPaint);
                }
                break;
            case RoundType.LEFT:
                if (viewWidth == width && viewHeight == height) {
                    mCanvas.drawRoundRect(new RectF(0, 0, diameter, viewHeight), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(radius, 0, viewWidth, viewHeight), mPaint);
                } else if (viewWidth == width && viewHeight != height) {
                    float dis = (height - viewHeight) / 2f;
                    if (isCenterCorp) {
                        mCanvas.translate(0, -dis);
                        mCanvas.drawRoundRect(new RectF(0, dis, diameter, viewHeight + dis), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(radius, dis, viewWidth, viewHeight + dis), mPaint);
                    } else {
                        mCanvas.drawRoundRect(new RectF(0, 0, diameter, viewHeight), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(radius, 0, viewWidth, viewHeight), mPaint);
                    }
                } else {
                    float dis = (width - viewWidth) / 2f;
                    mCanvas.translate(-dis, 0);
                    mCanvas.drawRoundRect(new RectF(dis, 0, diameter + dis, viewHeight), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(radius + dis, 0, viewWidth + dis, viewHeight), mPaint);
                }
                break;
            case RoundType.LEFT_TOP:
                if (viewWidth == width && viewHeight == height) {
                    mCanvas.drawRoundRect(new RectF(0, 0, diameter, diameter), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(radius, 0, viewWidth, radius), mPaint);
                    mCanvas.drawRect(new RectF(0, radius, viewWidth, viewHeight), mPaint);
                } else if (viewWidth == width && viewHeight != height) {
                    float dis = (height - viewHeight) / 2f;
                    if (isCenterCorp) {
                        mCanvas.translate(0, -dis);
                        mCanvas.drawRoundRect(new RectF(0, dis, diameter, diameter + dis), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(radius, dis, viewWidth, radius + dis), mPaint);
                        mCanvas.drawRect(new RectF(0, radius + dis, viewWidth, viewHeight + dis), mPaint);
                    } else {
                        mCanvas.drawRoundRect(new RectF(0, 0, diameter, diameter), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(radius, 0, viewWidth, radius), mPaint);
                        mCanvas.drawRect(new RectF(0, radius, viewWidth, viewHeight), mPaint);
                    }
                } else {
                    float dis = (width - viewWidth) / 2f;
                    mCanvas.translate(-dis, 0);
                    mCanvas.drawRoundRect(new RectF(dis, 0, diameter + dis, diameter), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(radius + dis, 0, viewWidth + dis, radius), mPaint);
                    mCanvas.drawRect(new RectF(dis, radius, viewWidth + dis, viewHeight), mPaint);
                }
                break;
            case RoundType.LEFT_BOTTOM:
                if (viewWidth == width && viewHeight == height) {
                    mCanvas.drawRoundRect(new RectF(0, viewHeight - diameter, diameter, viewHeight), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(0, 0, viewWidth, viewHeight - radius), mPaint);
                    mCanvas.drawRect(new RectF(radius, viewHeight - radius, viewWidth, viewHeight), mPaint);
                } else if (viewWidth == width && viewHeight != height) {
                    float dis = (height - viewHeight) / 2f;
                    if (isCenterCorp) {
                        mCanvas.translate(0, -dis);
                        mCanvas.drawRoundRect(new RectF(0, viewHeight - diameter + dis, diameter, viewHeight + dis), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, dis, viewWidth, viewHeight - radius + dis), mPaint);
                        mCanvas.drawRect(new RectF(radius, viewHeight - radius + dis, viewWidth, viewHeight + dis), mPaint);
                    } else {
                        mCanvas.drawRoundRect(new RectF(0, viewHeight - diameter, diameter, viewHeight), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, 0, viewWidth, viewHeight - radius), mPaint);
                        mCanvas.drawRect(new RectF(radius, viewHeight - radius, viewWidth, viewHeight), mPaint);
                    }
                } else {
                    float dis = (width - viewWidth) / 2f;
                    mCanvas.translate(-dis, 0);
                    mCanvas.drawRoundRect(new RectF(dis, viewHeight - diameter, diameter + dis, viewHeight), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(dis, 0, viewWidth + dis, viewHeight - radius), mPaint);
                    mCanvas.drawRect(new RectF(radius + dis, viewHeight - radius, viewWidth + dis, viewHeight), mPaint);
                }
                break;
            case RoundType.RIGHT_TOP:
                if (viewWidth == width && viewHeight == height) {
                    mCanvas.drawRoundRect(new RectF(viewWidth - diameter, 0, viewWidth, diameter), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(0, 0, viewWidth - radius, radius), mPaint);
                    mCanvas.drawRect(new RectF(0, radius, viewWidth, viewHeight), mPaint);
                } else if (viewWidth == width && viewHeight != height) {
                    float dis = (height - viewHeight) / 2f;
                    if (isCenterCorp) {
                        mCanvas.translate(0, -dis);
                        mCanvas.drawRoundRect(new RectF(viewWidth - diameter, dis, viewWidth, diameter + dis), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, dis, viewWidth - radius, radius + dis), mPaint);
                        mCanvas.drawRect(new RectF(0, radius + dis, viewWidth, viewHeight + dis), mPaint);
                    } else {
                        mCanvas.drawRoundRect(new RectF(viewWidth - diameter, 0, viewWidth, diameter), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, 0, viewWidth - radius, radius), mPaint);
                        mCanvas.drawRect(new RectF(0, radius, viewWidth, viewHeight), mPaint);
                    }
                } else {
                    float dis = (width - viewWidth) / 2f;
                    mCanvas.translate(-dis, 0);
                    mCanvas.drawRoundRect(new RectF(viewWidth - diameter + dis, 0, viewWidth + dis, diameter), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(dis, 0, viewWidth - radius + dis, radius), mPaint);
                    mCanvas.drawRect(new RectF(dis, radius, viewWidth + dis, viewHeight), mPaint);
                }
                break;
            case RoundType.RIGHT_BOTTOM:
                if (viewWidth == width && viewHeight == height) {
                    mCanvas.drawRoundRect(new RectF(viewWidth - diameter, viewHeight - diameter, viewWidth, viewHeight), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(0, 0, viewWidth, viewHeight - radius), mPaint);
                    mCanvas.drawRect(new RectF(0, viewHeight - radius, viewWidth - radius, viewHeight), mPaint);
                } else if (viewWidth == width && viewHeight != height) {
                    float dis = (height - viewHeight) / 2f;
                    if (isCenterCorp) {
                        mCanvas.translate(0, -dis);
                        mCanvas.drawRoundRect(new RectF(viewWidth - diameter, viewHeight - diameter + dis, viewWidth, viewHeight + dis), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, dis, viewWidth, viewHeight - radius + dis), mPaint);
                        mCanvas.drawRect(new RectF(0, viewHeight - radius + dis, viewWidth - radius, viewHeight + dis), mPaint);
                    } else {
                        mCanvas.drawRoundRect(new RectF(viewWidth - diameter, viewHeight - diameter, viewWidth, viewHeight), radius, radius, mPaint);
                        mCanvas.drawRect(new RectF(0, 0, viewWidth, viewHeight - radius), mPaint);
                        mCanvas.drawRect(new RectF(0, viewHeight - radius, viewWidth - radius, viewHeight), mPaint);
                    }
                } else {
                    float dis = (width - viewWidth) / 2f;
                    mCanvas.translate(-dis, 0);
                    mCanvas.drawRoundRect(new RectF(viewWidth - diameter + dis, viewHeight - diameter, viewWidth + dis, viewHeight), radius, radius, mPaint);
                    mCanvas.drawRect(new RectF(dis, 0, viewWidth + dis, viewHeight - radius), mPaint);
                    mCanvas.drawRect(new RectF(dis, viewHeight - radius, viewWidth - radius + dis, viewHeight), mPaint);
                }
                break;
        }
    }

    @Override
    public String key() {
        return key;
    }

    @IntDef({RoundType.ALL, RoundType.TOP, RoundType.RIGHT, RoundType.BOTTOM, RoundType.LEFT, RoundType.LEFT_TOP,
            RoundType.LEFT_BOTTOM, RoundType.RIGHT_TOP, RoundType.RIGHT_BOTTOM, RoundType.NONE})
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RoundType {
        int ALL = 0;
        int TOP = 1;
        int RIGHT = 2;
        int BOTTOM = 3;
        int LEFT = 4;
        int LEFT_TOP = 5;
        int LEFT_BOTTOM = 6;
        int RIGHT_TOP = 7;
        int RIGHT_BOTTOM = 8;
        int NONE = 9;
    }
}
