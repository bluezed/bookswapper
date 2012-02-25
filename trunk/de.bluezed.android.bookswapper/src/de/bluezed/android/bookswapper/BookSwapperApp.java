package de.bluezed.android.bookswapper;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "dG9RLXI0Y29pVnRqQk5zZVd3ejF5enc6MQ")
public class BookSwapperApp extends Application {
	
	@Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        super.onCreate();
    }
}

