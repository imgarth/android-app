package cn.eoe.app.ui;

import java.util.Locale;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import cn.eoe.app.R;
import cn.eoe.app.ui.base.BaseActivity;

import com.umeng.update.UmengUpdateAgent;

public class SplashActivity extends BaseActivity {

	private Handler mHandler = new Handler();
	public static final String SHORTCUTISCREATE = "createshortcut";
	public static final String ISSHORTCUTCREATED = "isshortcutcreated";
	private static final String SHORTCUTINTENT = "com.android.launcher.action.INSTALL_SHORTCUT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		View view=View.inflate(this, R.layout.start_activity, null);
		setContentView(view);
		final SharedPreferences shortCutPrefs = getSharedPreferences(SHORTCUTISCREATE,0);
		final boolean shortCut = shortCutPrefs.getBoolean(ISSHORTCUTCREATED, false);
		new Thread(){
            @Override
            public void run() {
            	if(shortCut == false){
            		if(!isInstallShortcut()){
            			addShortcut();
            		}
        		}
            }
        }.start();
		Animation animation=AnimationUtils.loadAnimation(this, R.anim.alpha);
		view.startAnimation(animation);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {}
			@Override
			public void onAnimationRepeat(Animation arg0) {}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						goHome();
					}
				}, 500);
			}
		});
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.update(this);
		
		
	}

	private boolean isInstallShortcut(){
		boolean result = false;
	    String title = null;
	    try {
	        final PackageManager pm = this.getPackageManager();
	        title = pm.getApplicationLabel(
	                pm.getApplicationInfo(this.getPackageName(),
	                        PackageManager.GET_META_DATA)).toString();
	    } catch (Exception e) {}

	    final String uriStr;
	    if (android.os.Build.VERSION.SDK_INT < 8) {
	        uriStr = "content://com.android.launcher.settings/favorites?notify=true";
	    } else {
	        uriStr = "content://com.android.launcher2.settings/favorites?notify=true";
	    }
	    
	    final Uri CONTENT_URI = Uri.parse(uriStr);
	    try{
		    final Cursor c = this.getContentResolver().query(CONTENT_URI, null,
		            "title=?", new String[] { title }, null);
		    if (c != null && c.getCount() > 0) {
		        result = true;
		    }
	    }catch(SecurityException e){
	    	result = false;
	    	e.printStackTrace();
	    }
	    
	    if(result){
	    	addShortCutTrue();
	    }
	    return result;
	}	
	
	private void addShortcut(){
		
		Intent shortCutIntent = new Intent(SHORTCUTINTENT);
		String appName = this.getString(R.string.app_name);
		shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
		//不用重复创建
		shortCutIntent.putExtra("duplicate", false);
		Intent localIntent = new Intent(Intent.ACTION_MAIN,null);
		localIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		String packageName = this.getPackageName();
		String className = packageName + "."  + this.getLocalClassName();
		ComponentName localComponentName = new ComponentName(packageName, className);
		localIntent.setComponent(localComponentName);
		shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, localIntent);
		Intent.ShortcutIconResource localShortcutIconResource = 
			Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher);
		shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, localShortcutIconResource);
		
		addShortCutTrue();

		this.sendBroadcast(shortCutIntent);
	}
	
	private void addShortCutTrue(){
		 SharedPreferences shortCutPrefs = getSharedPreferences(SHORTCUTISCREATE,0);
			//创建之后，将状态改为true
		 Editor editor = shortCutPrefs.edit();
		 editor.putBoolean(ISSHORTCUTCREATED,true);
		 editor.commit(); 
	}
	protected void onResume() {
		super.onResume();
	}

	private void goHome() {
		openActivity(MainActivity.class);
		defaultFinish();
	};

}
