package com.cyno.drawer.ui;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import me.panavtec.drawableview.DrawableView;
import me.panavtec.drawableview.DrawableViewConfig;

import com.cyno.drawme.R;
import com.cyno.drawer.adapters.ToolsGridAdapter;
import com.cyno.drawer.models.Tools;
import com.cynozer.drawer.colorpicker.ColorPickerClickListener;
import com.cynozer.drawer.colorpicker.ColorPickerDialogBuilder;
import com.cynozer.drawer.colorpicker.ColorPickerView;
import com.cynozer.drawer.colorpicker.LineColorPicker;
import com.cynozer.drawer.colorpicker.OnColorChangedListener;
import com.cynozer.drawer.colorpicker.OnColorSelectedListener;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnSeekBarChangeListener, OnClickListener, OnItemClickListener {


    private DrawableView mDrawableView;
    private DrawableViewConfig config;
    private int currentColor = Color.parseColor("#3F51B5");
    private LinearLayout mGridLayout;
    private int mGridSpace ;
    private String fname;
    private MediaScannerConnection msConn;
    private SeekBar mSeekbar;
    private float mStrokeSize = 20f;
    private ImageView mMenuImage;
    private View mToolsView ;
    private String[] toolsTitles;
    private TypedArray toolsIcons;
    private GridView mtoolsGrid;
    private boolean hasGrid;
    private SeekBar mGridSeekbar;
    private View gridSeekLayout;
    private AlertDialog dialog;

    private final String appPackageName = "com.cyno.drawme";
    private int backgroundColor;


    public enum navItems  {BRUSH,ERASER,GRID,COLOR,SAVE,CLEAR,SHARE,UNDO , BACKGROUND}



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        mDrawableView = (DrawableView) findViewById(R.id.paintView_mainactivity);
        mGridLayout = (LinearLayout) findViewById(R.id.linear_grid_mainactivity);
        mGridSpace = (int) getResources().getDimension(R.dimen.grid_default_height);
        mToolsView = findViewById(R.id.tools_layout);
        mToolsView.setTag(false);
        mSeekbar = (SeekBar) findViewById(R.id.seekbar_size);
        mSeekbar.setProgress((int)mStrokeSize);
        mMenuImage = (ImageView) findViewById(R.id.bottom_drawer);
        mMenuImage.setOnClickListener(this);
        setupDrawerView();

        toolsTitles = getResources().getStringArray(R.array.tools_grid_titles);
        toolsIcons = getResources().obtainTypedArray(R.array.tools_grid_icons);
        mtoolsGrid = (GridView) findViewById(R.id.mainactivity_tools_grid);
        ToolsGridAdapter adapter = new ToolsGridAdapter(getApplicationContext(),getToolsList());
        toolsIcons.recycle();
        mtoolsGrid.setOnItemClickListener(this);
        mtoolsGrid.setAdapter(adapter);

        mGridSeekbar = (SeekBar) findViewById(R.id.seekbar_grid);
        gridSeekLayout = findViewById(R.id.grid_seekbar_layout);

        mGridSeekbar.setOnSeekBarChangeListener(this);

        ImageView ivNavUp = (ImageView) findViewById(R.id.nav_up);
        ivNavUp.setOnClickListener(this);
        backgroundColor = Color.WHITE;

    }

    @SuppressLint("NewApi")
    private void setupDrawerView() {
        int width = 0;
        int height = 0;
        config = new DrawableViewConfig();
        config.setStrokeColor(currentColor);
        config.setMaxZoom(1.0f);
        config.setMinZoom(1.0f);
        config.setStrokeWidth(mStrokeSize);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        if(Build.VERSION.SDK_INT >= 13){
            display.getSize(size);
            width = size.x;
            height = size.y;
        }else{
            width = display.getWidth();
            height = display.getHeight();
        }
        config.setCanvasHeight(height);
        config.setCanvasWidth(width);
        mDrawableView.setConfig(config);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSeekbar.setOnSeekBarChangeListener(this);
        if(hasGrid)
            showGrid(mGridSpace, false);
    }




    private ArrayList<Tools> getToolsList(){
        ArrayList<Tools> itemList = new ArrayList<>();
        for(int i = 0 ; i < toolsTitles.length ; ++i){
            itemList.add(new Tools(toolsTitles[i], toolsIcons.getResourceId(i, -1) ));
        }
        return itemList;
    }




    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        navItems item = navItems.values()[position];
        boolean isEraser = false;
        switch (item) {
            case BRUSH:
                if(currentColor == backgroundColor)
                    currentColor = Color.parseColor("#3F51B5");
                config.setStrokeColor(currentColor);
                break;
            case ERASER:
                isEraser = true;
                config.setStrokeColor(backgroundColor);
                break;
            case UNDO:
//			premiumFeature();
                mDrawableView.undo();
                break;
            case GRID:
//			premiumFeature();
                showGrid(mGridSpace,true );
                break;
            case SAVE:
//			premiumFeature();
                if(saveLocally() != null)
                    Toast.makeText(this, R.string.save_success, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();

                break;
            case SHARE:
//			premiumFeature();
                shareBitmap(saveLocally(), fname);
                break;
            case CLEAR:
                mDrawableView.clear();
                mGridLayout.setVisibility(View.GONE);
                hasGrid = false;
                break;
            case COLOR:
//			premiumFeature();
                if(Build.VERSION.SDK_INT >= 16)
                    selectColorModern(false);
                else
                    selectColorOld(false);
                break;
            case BACKGROUND:
//			premiumFeature();
                if(Build.VERSION.SDK_INT >= 16)
                    selectColorModern(true);
                else
                    selectColorOld(true);
                break;


            default:
                break;
        }
        if(!hasGrid)
            animateUp();
        if(!isEraser)
            config.setStrokeColor(currentColor);
    }


    private void selectColorOld(final boolean isBackground) {
        AlertDialog.Builder builder = new Builder(this);
        View view = View.inflate(this, R.layout.color_picker_old, null);
        builder.setView(view).setTitle(R.string.pick_color_title);
        dialog = builder.create();
        dialog.show();

        LineColorPicker colorpicker = (LineColorPicker) view.findViewById(R.id.picker);
        colorpicker.setOnColorChangedListener(new OnColorChangedListener() {

            @Override
            public void onColorChanged(int c) {
                if(!isBackground) {
                    config.setStrokeColor(currentColor);
                }else{
                    mDrawableView.setBackgroundColor(c);
                }
                dialog.cancel();
            }
        });


    }

    @SuppressLint("NewApi")
    private void showGrid(int space , boolean flag){
        if(flag)
            hasGrid = !hasGrid;

        if(hasGrid){
            this.gridSeekLayout.setVisibility(View.VISIBLE);
        }else{
            this.gridSeekLayout.setVisibility(View.GONE);
        }

        if(hasGrid)
            mGridLayout.setVisibility(View.VISIBLE);
        else
            mGridLayout.setVisibility(View.GONE);

        Display mDisplay = getWindowManager().getDefaultDisplay();
        Point mPoint = new Point();
        int height;
        if(Build.VERSION.SDK_INT >= 13){
            mDisplay.getSize(mPoint);
            height = mPoint.y;
        }else{
            height = mDisplay.getHeight();
        }
        int numOfLines = height / space;
        if(mGridLayout.getChildCount() != 0)
            mGridLayout.removeAllViewsInLayout();
        for (int i = 0; i <= numOfLines; ++i) {
            View mView = new View(this);
            mView.setBackgroundColor(Color.GRAY);
            LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(MATCH_PARENT, 1);
            mParams.setMargins(0, space, 0, 0);
            mView.setLayoutParams(mParams);
            mView.invalidate();
            mGridLayout.addView(mView);
        }
    }

    public void saveLocally(View v){
        saveLocally(null);
        animateUp();
        Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show();
    }

    private Bitmap saveLocally(){
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Pictures/Drawer");
        myDir.mkdirs();
        fname = "/picture"+System.currentTimeMillis()+".png";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Bitmap bp = null;
        try {
            FileOutputStream out = new FileOutputStream(file);
            bp  = mDrawableView.obtainBitmap(backgroundColor);
            bp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            scanPhoto(file.toString());
        }
        return bp;
    }


    public void scanPhoto(final String imageFileName)
    {
        msConn = new MediaScannerConnection(this,new MediaScannerConnectionClient()
        {
            public void onMediaScannerConnected()
            {
                msConn.scanFile(imageFileName, null);
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
            }
        });
        msConn.connect();
    }

    private void shareBitmap(Bitmap bp , String title){
        String pathofBmp = Images.Media.insertImage(getContentResolver(), bp,title, null);
        if(pathofBmp == null){
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            return;
        }
        Uri bmpUri = Uri.parse(pathofBmp);
        final Intent emailIntent1 = new Intent(   android.content.Intent.ACTION_SEND);
        emailIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent1.putExtra(Intent.EXTRA_STREAM, bmpUri);
        emailIntent1.putExtra(Intent.EXTRA_TEXT, getString(R.string.caption_string) + " http://bit.ly/1KRYeKf");

        emailIntent1.setType("image/png");

        startActivity(emailIntent1);
    }

    public void selectColorModern(final boolean isBackground){
        ColorPickerDialogBuilder
                .with(this)
                .setTitle(getString(R.string.pick_color_title))
                .initialColor(currentColor )
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .lightnessSliderOnly()
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {

                    }
                })
                .setPositiveButton(getString(android.R.string.ok), new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {

                        currentColor  = selectedColor;
                        if(!isBackground)
                            config.setStrokeColor(selectedColor);
                        else{
                            mDrawableView.setBackgroundColor(selectedColor);
                            backgroundColor = selectedColor;
                        }

                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekbar_grid:
                this.mGridSpace = progress+(int) getResources().getDimension(R.dimen.grid_default_height);
                showGrid(mGridSpace , false);
                break;

            default:
                this.mStrokeSize = progress;
                this.config.setStrokeWidth(mStrokeSize);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottom_drawer:
                if((boolean) mToolsView.getTag()){
                    animateUp();

                }else{
                    animateDown();

                }
                break;
            case R.id.nav_up:
                animateUp();
                break;

            default:
                break;
        }

    }


    private void animateUp(){
        mToolsView.setVisibility(View.GONE);
        Animation slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        mToolsView.startAnimation(slideAnimation);
        mToolsView.setTag(false);

        slideAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
    }

    private void animateDown(){
        mToolsView.setVisibility(View.VISIBLE);
        Animation slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        mToolsView.startAnimation(slideAnimation);
        mToolsView.setTag(true);
    }

    @Override
    public void onBackPressed() {
        if((boolean) mToolsView.getTag())
            animateUp();
        else
            super.onBackPressed();

    }

//	private void premiumFeature(){
//		AlertDialog.Builder mBuilder = new Builder(this);
//		mBuilder.setTitle(getString(R.string.premium_title))
//		.setMessage(getString(R.string.premium_msg))
//		.setPositiveButton(getString(R.string.premium_btn), new DialogInterface.OnClickListener() {
//			
//	
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				try {
//					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
//				} catch (android.content.ActivityNotFoundException anfe) {
//					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
//				}
//				
//			}
//		}).setNegativeButton(android.R.string.cancel, null).show();
//		
//	}

}
