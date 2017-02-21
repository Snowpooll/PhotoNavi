package net.developapp.photonavi;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore.Audio.Media;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SendMail extends Activity {
	private Uri mAttachFile;
	private static final int REQ_GALLERY = 1;
	private Button btGetMailAddress;
	private EditText etAddress;
	private EditText etSubject;
	private EditText etBody;
	
	private static final int REQUEST_CODE_MAIL_ADDRESS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sendmail);
		

		etAddress = (EditText) findViewById(R.id.intputAddress);
		etSubject = (EditText) findViewById(R.id.intputSubject);
		etBody = (EditText) findViewById(R.id.intputBody);
		
		//連絡帳から取得
		btGetMailAddress = (Button)this.findViewById(R.id.get_mail_address);

		// メールアドレスの取得
		btGetMailAddress.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, REQUEST_CODE_MAIL_ADDRESS);
        	}
        });

		findViewById(R.id.selectAttach).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						/*
						 * Intent intent = new
						 * Intent(Intent.ACTION_PICK,Media.EXTERNAL_CONTENT_URI
						 * ); startActivityForResult(intent,
						 * RESULT_PICK_FILENAME);
						 */
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(intent, REQ_GALLERY);

					}
				});

		findViewById(R.id.linkMail).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 宛先と本文を取得
				String address = etAddress.getText().toString();
				String subject = etSubject.getText().toString();
				String body = etBody.getText().toString();

				// 宛先か題名か本文どちらかが空っぽなら警告
				if (TextUtils.isEmpty(address) || TextUtils.isEmpty(subject)
						|| TextUtils.isEmpty(body)) {
					Toast.makeText(SendMail.this,
							getString(R.string.ch1403_label_input_empty),
							Toast.LENGTH_SHORT).show();
					return;
				}

				// メール連携
				Uri uri = Uri.parse("mailto:" + address);
				// 引数で送信先を設定、この値はsetDataで設定しても同じ意味になる
				Intent intent = new Intent(Intent.ACTION_SEND, uri);
				// 複数の送信先がある場合は、Intent.EXTRA_EMAILを使用して設定できる
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
				// 設定するメールの本文がTextの場合はtext/plainをHTMLの場合はtext/htmlを設定
				intent.setType("text/plain");
				// 件名を設定
				intent.putExtra(Intent.EXTRA_SUBJECT, subject);
				// 本文を設定
				intent.putExtra(Intent.EXTRA_TEXT, body);

				// 添付ファイルを設定
				if (mAttachFile != null) {
					intent.putExtra(Intent.EXTRA_STREAM, mAttachFile);
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(SendMail.this,
								getString(R.string.ch1403_label_input_empty),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQ_GALLERY && resultCode == RESULT_OK
				&& null != data) {
			mAttachFile = data.getData();

			String[] filePathColumn = { Media.DATA };

			Cursor cursor = getContentResolver().query(mAttachFile,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			TextView textAttachFile = (TextView) findViewById(R.id.textAttachFile);
			textAttachFile.setText(picturePath);
		}
		if (requestCode == REQUEST_CODE_MAIL_ADDRESS && resultCode == RESULT_OK
				&& null != data){
			onMailAddressAddressBookResult(data);
		}
		
	}

	/**
     * アドレス帳（メールアドレス）の表示
     */
	private void onMailAddressAddressBookResult(Intent data) {
		String[] mailAddresses = new String[0];
		ContentResolver contentResolver = this.getContentResolver();
		Cursor c = contentResolver.query(data.getData(), null, null, null, null);
		String id = "";
		String name = "";
		if (c.moveToFirst()) {
			// 選択された人のidの取得
			id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
			//name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			
			// 選択された人のメールアドレスをすべて取得
			Cursor mailC = contentResolver.query(
						ContactsContract.CommonDataKinds.Email.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id,
						null,
						null);
			if (mailC.moveToFirst()) {
				mailAddresses = new String[mailC.getCount()];
				int count = 0;
				do {
					mailAddresses[count] = mailC.getString(
							mailC.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					count++;
				} while (mailC.moveToNext());
			}
			mailC.close();
		}
		c.close();
		
		// 選択者がメールアドレスを持っていない場合
		if (mailAddresses.length <= 0) {
			return;
		}
		
		// メールアドレスが１つの場合
		if (mailAddresses.length == 1) {
			etAddress.setText(mailAddresses[0]);
			return;
		}
		
		// シングル選択ダイアログフラグメント
		SingleSelectDialogFragment fragment = SingleSelectDialogFragment.newInstance(
				R.string.dialog_title_select_mail_address, mailAddresses, 1);
		fragment.show(getFragmentManager(), "mail_address_select");
	}
	

	/**
     * メールアドレスセレクト
     */
	public void onMailAddressSelect(String mailAddress) {
		etAddress.setText(mailAddress);
	}
	


	/**
     * シングルセレクトダイアログフラグメント
     */
	public static class SingleSelectDialogFragment extends DialogFragment {
		private String selectedItem = "";
		
		public static SingleSelectDialogFragment newInstance(int title, String[] items, int selectKind) {
			SingleSelectDialogFragment fragment = new SingleSelectDialogFragment();
			Bundle args = new Bundle();
			args.putInt("title", title);
			args.putStringArray("items", items);
			args.putInt("select_kind", selectKind);
			fragment.setArguments(args);
			
			return fragment;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle safedInstanceState) {
			int title = getArguments().getInt("title");
			final String[] items = getArguments().getStringArray("items");
			final int selectKind = getArguments().getInt("select_kind");
			selectedItem = items[0];
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(title);
			builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	selectedItem = items[item];
			    }
			})
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int whichButton) {
	        		SendMail activity = (SendMail)getActivity();
	        		//if (selectKind == 0) {
	        			//activity.onPhoneNumberSelect(selectedItem);
	        		//} else {
	        			activity.onMailAddressSelect(selectedItem);
	        		//}
	        	}
	        })
	        .setNegativeButton("Cancel", null);
			
			return builder.create();
		}
    }
}
