package com.taller.myapplication.services.permissionService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.taller.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PermissionService {

    private static final int PERMISSION_REQUEST_CODE = 200;

    public void requestPermission(Activity activeActivity,String permissions[]){
        ActivityCompat.requestPermissions(activeActivity, permissions, PERMISSION_REQUEST_CODE);
    }

    public Map<String,Boolean> managePermissionResponse(final Activity activeActivity, int requestCode, final String permissions[], int[] grantResults){
        Map<String, Boolean> response = new HashMap<>();
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean fullPermissionAccepted = true;
                    List<String> rejectedPermissions = new ArrayList<>();
                    for (int i = 0 ; i < grantResults.length ; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            fullPermissionAccepted = false;
                            rejectedPermissions.add(permissions[i]);
                            response.put(permissions[i],false);
                        }else{
                            response.put(permissions[i],true);
                        }
                    }

                    if(!fullPermissionAccepted){
                        Toast.makeText(activeActivity, R.string.permissions_needed,Toast.LENGTH_SHORT);
                        List<String> permissionsToRequest = rejectedPermissions.stream().filter(rejectedPermission ->
                                activeActivity.shouldShowRequestPermissionRationale(rejectedPermission)).collect(Collectors.toList());
                        alertView(activeActivity,permissionsToRequest.toArray(new String[permissionsToRequest.size()]));
                    }
                }
                break;
        }
        return response;
    }


    private void alertView(final Activity activeActivity, final String[] permissions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activeActivity);
        builder.setTitle(R.string.permission_denied);
        builder.setMessage(R.string.permission_denied_explanation);
        builder.setNegativeButton(R.string.permission_sure, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.dismiss();
            }
        });

        builder.setPositiveButton(R.string.permission_retry, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.dismiss();
                requestPermission(activeActivity, permissions);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
