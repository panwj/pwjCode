package com.ex.simi.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.ex.simi.R;

import java.io.File;
import java.util.List;

/**
 * author : She Wenbiao
 * date   : 2020/7/14 11:14 AM
 */
public class FileUtilDialog {

    public interface DialogListener {
        void onOK();

        void onCancel();

        void onDismiss();
    }

    public interface DialogRenameListener {
        void onOK(String name);

        void onCancel();

        void onDismiss();
    }

    public static void showScreenshotDeleteDialog(Activity activity, int count, DialogListener dialogListener) {
        View view = LayoutInflater.from(activity).inflate(R.layout.similar_photo_delete_confirm_dialog, null);
        TextView title = view.findViewById(R.id.tv_title);
        TextView content = view.findViewById(R.id.tv_content);
        TextView cancel = view.findViewById(R.id.tv_no);
        TextView ok = view.findViewById(R.id.tv_ok);
        title.setText(activity.getResources().getQuantityString(R.plurals.duplicate_photos_delete_title, count, count));
        String quantityString = activity.getResources().getQuantityString(R.plurals.screenshots_photos_detele_content, count, count);
        content.setText(quantityString);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogListener != null) {
                    dialogListener.onOK();
                }
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogListener != null) {
                    dialogListener.onCancel();
                }
                dialog.dismiss();
            }
        });
        if (!isActivityDestroyed(activity)) {
            dialog.show();
        }

    }

    public static void showDeleteConfirmDialog(Activity activity, @StringRes int titleId,
                                               @StringRes int messageId, DialogListener dialogListener) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialogListener != null) {
                            dialogListener.onOK();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dlg_btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialogListener != null) {
                            dialogListener.onCancel();
                        }
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        if (!isActivityDestroyed(activity)) {
            dialog.show();
        }
    }

    public static AlertDialog showSimilarPhotosDeletingDialog(Activity activity, DialogListener dialogListener) {
        if (activity == null || activity.isFinishing()) {
            return null;
        }
        View view = LayoutInflater.from(activity).inflate(R.layout.similar_photo_deleting_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        AlertDialog deletingDialog = builder.create();
        deletingDialog.setCancelable(false);
        deletingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dialogListener != null) {
                    dialogListener.onDismiss();
                }
            }
        });
        if (!isActivityDestroyed(activity)) {
            deletingDialog.show();
        }
        return deletingDialog;
    }


    public static AlertDialog showDeletingDialog(Activity activity, DialogListener dialogListener) {
        if (activity == null || activity.isFinishing()) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.deleting);
        AlertDialog deletingDialog = builder.create();
        deletingDialog.setCancelable(false);
        deletingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dialogListener != null) {
                    dialogListener.onDismiss();
                }
            }
        });
        if (!isActivityDestroyed(activity)) {
            deletingDialog.show();
        }
        return deletingDialog;
    }

    public static AlertDialog showCopyingDialog(Activity activity, DialogListener dialogListener) {
        if (activity == null || activity.isFinishing()) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.inprogress);
        AlertDialog deletingDialog = builder.create();
        deletingDialog.setCancelable(false);
        deletingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dialogListener != null) {
                    dialogListener.onDismiss();
                }
            }
        });
        if (!isActivityDestroyed(activity)) {
            deletingDialog.show();
        }
        return deletingDialog;
    }

    public static void showFileDeletedDialog(Activity activity, int count, DialogListener dialogListener) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        final View view = LayoutInflater.from(activity).inflate(R.layout.dlg_files_deleted, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        LottieAnimationView doneAnimationView = view.findViewById(R.id.animation_done);
        doneAnimationView.playAnimation();
        TextView contentTv = view.findViewById(R.id.tv_content);
        String contentStr = activity.getResources().getQuantityString(R.plurals.file_deleted, count, count);
        contentTv.setText(contentStr);
        TextView okTv = view.findViewById(R.id.tv_ok);
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dialogListener != null) {
                    dialogListener.onDismiss();
                }
            }
        });
        if (!isActivityDestroyed(activity)) {
            dialog.show();
        }
    }

    public static void openFile(Context context, File file) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);//android 7.0以上
                intent.setDataAndType(fileUri, MimeTypes.getMimeType(file));
                grantUriPermission(context, fileUri, intent);
            } else {
                intent.setDataAndType(Uri.fromFile(file), MimeTypes.getMimeType(file));
            }
            context.startActivity(intent);
            Intent.createChooser(intent, context.getResources().getString(R.string.open_with));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getResources().getString(R.string.open_with_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private static void grantUriPermission(Context context, Uri fileUri, Intent intent) {
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    public static boolean isActivityDestroyed(Activity activity) {
        return activity == null || activity.isFinishing() ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed());
    }

}
