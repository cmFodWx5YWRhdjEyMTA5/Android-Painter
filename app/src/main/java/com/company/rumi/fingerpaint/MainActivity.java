package com.company.rumi.fingerpaint;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    DrawingView dv;
    public Paint mPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        dv = new DrawingView(this);
        dv.setDrawingCacheEnabled(true);
        setContentView(dv);
        dv.setBackgroundColor(Color.WHITE);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);
        checkPermission();
    }

    private void checkPermission() {
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 101);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_color:
                ColorPickerDialog colorPickerDialog = ColorPickerDialog.createColorPickerDialog(this, ColorPickerDialog.DARK_THEME);
                colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color, String hexVal) {
                        mPaint.setColor(color);
                    }
                });
                colorPickerDialog.show();
                return true;
            case R.id.save:
                dv.saveImage();
                return true;
            case R.id.clear_all:
                dv.clearDrawing();
                return true;
            case R.id.erase:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                mPaint.setAlpha(0x80);
                return true;
            case R.id.line_width:
                openDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openDialog() {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View subView = inflater.inflate(R.layout.progress_bar, null);
        final SeekBar widthSeekBar = (SeekBar) subView.findViewById(R.id.widthSeekBar);
        final ImageView widthImageView = (ImageView) subView.findViewById(R.id.widthImageView);
        //Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose line width");
        builder.setView(subView);
        AlertDialog alertDialog = builder.create();

        SeekBar.OnSeekBarChangeListener lineWidthChanged =
                new SeekBar.OnSeekBarChangeListener() {
                    Bitmap bitmap = Bitmap.createBitmap(
                            400, 100, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap); // associate with Canvas

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        // configure a Paint object for the current SeekBar value
                        Paint p = new Paint();
                        p.setColor(mPaint.getColor());
                        p.setStrokeCap(Paint.Cap.ROUND);
                        p.setStrokeWidth(progress);

                        // erase the bitmap and redraw the line
                        bitmap.eraseColor(
                                getResources().getColor(android.R.color.transparent));
                        canvas.drawLine(30, 50, 370, 50, p);
                        widthImageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) // required
                    {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar)  // required
                    {
                    }
                }; // end lineWidthChanged
        widthSeekBar.setOnSeekBarChangeListener(lineWidthChanged);
        widthSeekBar.setProgress((int) mPaint.getStrokeWidth());

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //textInfo.setText(subEditText.getText().toString());
                mPaint.setStrokeWidth(widthSeekBar.getProgress());
                Toast.makeText(getApplicationContext(), "Ok Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_LONG).show();
            }
        });

        builder.show();
    }

//    public static class LineWidthDialog extends DialogFragment {
//        public ImageView widthImageView;
//        MainActivity mainActivity = new MainActivity();
//
//        // create an AlertDialog and return it
//        @Override
//        public Dialog onCreateDialog(Bundle bundle) {
//            android.app.AlertDialog.Builder builder =
//                    new android.app.AlertDialog.Builder(getActivity());
//            View lineWidthDialogView = getActivity().getLayoutInflater().inflate(
//                    R.layout.progress_bar, null);
//            builder.setView(lineWidthDialogView); // add GUI to dialog
//
//            // set the AlertDialog's message
//            builder.setTitle(R.string.title_line_width_dialog);
//            builder.setCancelable(true);
//
//            // get the ImageView
//            widthImageView = (ImageView) lineWidthDialogView.findViewById(
//                    R.id.widthImageView);
//
//            // configure widthSeekBar
//            //final DoodleView doodleView = getDoodleFragment().getDoodleView();
//            final SeekBar widthSeekBar = (SeekBar)
//                    lineWidthDialogView.findViewById(R.id.widthSeekBar);
//            widthSeekBar.setOnSeekBarChangeListener(lineWidthChanged);
//            widthSeekBar.setProgress((int) mainActivity.mPaint.getStrokeWidth());
//
//            // add Set Line Width Button
//            builder.setPositiveButton(R.string.button_set_line_width,
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            mainActivity.mPaint.setStrokeWidth(widthSeekBar.getProgress());
//                        }
//                    }
//            ); // end call to setPositiveButton
//
//            return builder.create(); // return dialog
//        } // end method onCreateDialog
//
//        // OnSeekBarChangeListener for the SeekBar in the width dialog
//        private SeekBar.OnSeekBarChangeListener lineWidthChanged =
//                new SeekBar.OnSeekBarChangeListener() {
//                    Bitmap bitmap = Bitmap.createBitmap(
//                            400, 100, Bitmap.Config.ARGB_8888);
//                    Canvas canvas = new Canvas(bitmap); // associate with Canvas
//
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress,
//                                                  boolean fromUser) {
//                        // configure a Paint object for the current SeekBar value
//                        Paint p = new Paint();
//                        p.setColor(mainActivity.mPaint.getColor());
//                        p.setStrokeCap(Paint.Cap.ROUND);
//                        p.setStrokeWidth(progress);
//
//                        // erase the bitmap and redraw the line
//                        bitmap.eraseColor(
//                                getResources().getColor(android.R.color.transparent));
//                        canvas.drawLine(30, 50, 370, 50, p);
//                        widthImageView.setImageBitmap(bitmap);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) // required
//                    {
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar)  // required
//                    {
//                    }
//                }; // end lineWidthChanged
//    }

    class DrawingView extends View {

        public int width;
        public int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;

        public DrawingView(Context context) {
            super(context);
            this.context = context;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLACK);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
        }

        public void clearDrawing() {

            setDrawingCacheEnabled(false);
            // don't forget that one and the match below,
            // or you just keep getting a duplicate when you save.

            onSizeChanged(width, height, width, height);
            invalidate();

            setDrawingCacheEnabled(true);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            width = w;
            height = h;

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        public void savingImage() {
            AlertDialog.Builder editalert = new AlertDialog.Builder(MainActivity.this);
            editalert.setTitle("Please Enter the name..");
            final EditText input = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.FILL_PARENT);
            input.setLayoutParams(lp);
            editalert.setView(input);
            editalert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    String name = input.getText().toString();
                    Bitmap bitmap = getDrawingCache();

                    String path = Environment.getExternalStorageDirectory().toString();
                    File file = new File(path + "/DCIM/Camera/" + name + ".jpg");
                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileOutputStream ostream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, ostream);
                        ostream.close();
                        invalidate();
                        Log.d("Message", file.toString());
                        Toast message = Toast.makeText(getContext(),
                                R.string.message_saved, Toast.LENGTH_SHORT);
                        message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                                message.getYOffset() / 2);
                        message.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {

                        setDrawingCacheEnabled(false);
                    }
                }
            });

            editalert.show();
        }

        public void saveImage() {
            String name = "Paint" + System.currentTimeMillis() + ".jpg";

            // insert the image in the device's gallery
            String location = MediaStore.Images.Media.insertImage(
                    getContext().getContentResolver(), getDrawingCache(), name,
                    "Painting");

            if (location != null) // image was saved
            {
                // display a message indicating that the image was saved
//                Toast message = Toast.makeText(getContext(), R.string.message_saved, Toast.LENGTH_SHORT);
                Toast message = Toast.makeText(getContext(), location, Toast.LENGTH_SHORT);
                message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                        message.getYOffset() / 2);
                message.show();
            } else {
                // display a message indicating that the image was not saved
                Toast message = Toast.makeText(getContext(),
                        R.string.message_error_saving, Toast.LENGTH_SHORT);
                message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                        message.getYOffset() / 2);
                message.show();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
            canvas.drawPath(circlePath, circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }
}
