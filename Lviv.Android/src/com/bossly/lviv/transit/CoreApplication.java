package com.bossly.lviv.transit;

import android.app.Application;
import android.content.Context;

public class CoreApplication extends Application {
	public static CoreApplication get(Context context) {
		return (CoreApplication) context.getApplicationContext();
	}
}
