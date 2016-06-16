package yearsj.com.coolplayer.View.ui.view;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import yearsj.com.coolplayer.View.ui.R;

/**
 *
 */
public class WaitDialog {

	private static Dialog mLoadingDialog;
	

	public static void showDialogForLoading(Activity context, String msg, boolean cancelable) {
		View view = LayoutInflater.from(context).inflate(R.layout.layout_loading_dialog, null);
		TextView loadingText = (TextView)view.findViewById(R.id.id_tv_loading_dialog_text);
		loadingText.setText(msg);
		
		mLoadingDialog = new Dialog(context, R.style.loading_dialog_style);
		mLoadingDialog.setCancelable(cancelable);
		mLoadingDialog.setContentView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
		mLoadingDialog.show();		
	}

	public static void hideDialogForLoading() {
		if(mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.cancel();
		}
	}

}
