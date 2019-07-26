package com.aurora.adroid.installer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import com.aurora.adroid.Constants;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.services.IPrivilegedCallback;
import com.aurora.services.IPrivilegedService;

public class Uninstaller {

    private Context context;

    public Uninstaller(Context context) {
        this.context = context;
    }

    public void uninstall(App app) {
        String prefValue = PrefUtil.getString(context, Constants.PREFERENCE_INSTALLATION_METHOD);
        switch (prefValue) {
            case "0":
            case "2":
                uninstallByPackageManager(app);
                break;
            case "1":
                uninstallByRoot(app);
                break;
            default:
                uninstallByPackageManager(app);
        }
    }

    private void uninstallByServices(App app) {
        ServiceConnection mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                IPrivilegedService service = IPrivilegedService.Stub.asInterface(binder);
                IPrivilegedCallback callback = new IPrivilegedCallback.Stub() {
                    @Override
                    public void handleResult(String packageName, int returnCode) {
                        Log.i("Uninstallation of " + packageName + " complete with code " + returnCode);
                    }
                };
                try {
                    if (!service.hasPrivilegedPermissions()) {
                        Log.e("service.hasPrivilegedPermissions() is false");
                        return;
                    }

                    service.deletePackage(app.getPackageName(), 1, callback);

                } catch (RemoteException e) {
                    Log.e("Connecting to privileged service failed");
                }
            }

            public void onServiceDisconnected(ComponentName name) {
            }
        };
        Intent serviceIntent = new Intent(Constants.PRIVILEGED_EXTENSION_SERVICE_INTENT);
        serviceIntent.setPackage(Constants.PRIVILEGED_EXTENSION_PACKAGE_NAME);
        context.getApplicationContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void uninstallByPackageManager(App app) {
        Uri uri = Uri.fromParts("package", app.getPackageName(), null);
        Intent intent = new Intent();
        intent.setData(uri);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            intent.setAction(Intent.ACTION_DELETE);
        } else {
            intent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void uninstallByRoot(App app) {
        new AppUninstallerRooted().uninstall(app);
    }

}
