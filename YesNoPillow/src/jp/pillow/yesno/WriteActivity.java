
package jp.pillow.yesno;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * @author user1
 *
 */
/**
 * @author user1
 */
public class WriteActivity extends AbstractActivity {

    /**
     * YesNo画像のOnOff状態
     */
    private static final String ON = "ON";
    private static final String OFF = "OFF";

    /** インテントフィルター */
    IntentFilter[] mWriteTagFilters;

    /** YesNo画像 */
    private ImageView mImgYes;
    private ImageView mImgNo;

    /** 書込モード */
    private boolean mWriteMode = false;

    /** ダイアログ */
    private AlertDialog mDialog;

    /*
     * (非 Javadoc)
     * @see jp.pillow.yesno.AbstractActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        mImgYes = (ImageView) findViewById(R.id.image_yes);
        mImgYes.setTag(ON);
        mImgNo = (ImageView) findViewById(R.id.image_no);
        mImgNo.setTag(OFF);
    }

    /*
     * (非 Javadoc)
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");

        if (mWriteMode == false) {
            return;
        }

        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // タグに書込
            if (writeTag(getNdef(), detectedTag)) {
                // 成功の場合、書き込み待ちダイアログを閉じる
                mDialog.dismiss();
            }
        }
    }

    /**
     * タグに書き込む。
     * 
     * @param message 書込メッセージ
     * @param tag タグ
     * @return 書込結果(True:成功, False:失敗)
     */
    private boolean writeTag(NdefMessage message, Tag tag) {
        Log.d(TAG, "writeTag");
        int size = message.toByteArray().length;

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    toast(getString(R.string.msg_write_readonly));
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    toast("max: " + ndef.getMaxSize() + ", msg: " + size
                            + " bytes.");
                    return false;
                }

                ndef.writeNdefMessage(message);
                toast(getString(R.string.msg_write_ok));
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format == null) {
                    // 未フォーマット
                    // NfcF nfcf = NfcF.get(tag);
                    // nfcf.connect();
                    // byte[] res = nfcf.transceive(new byte[] {
                    // (byte) 0x06, (byte) 0x00, (byte) 0xFE, (byte) 0x00,
                    // (byte) 0x00,
                    // (byte) 0x0F
                    // });
                    // nfcf.transceive(res);
                    // nfcf.close();
                    toast(getString(R.string.msg_write_not_support));
                    return false;
                } else {
                    // フォーマット済み
                    try {
                        format.connect();
                        format.format(message);
                        toast(getString(R.string.msg_write_ok_format));
                        return true;
                    } catch (IOException e) {
                        toast(getString(R.string.msg_write_error));
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            toast(getString(R.string.msg_write_ng));
        }

        return false;
    }

    /**
     * Yes画像押下処理。
     * 
     * @param v ビュー
     */
    public void onClickYes(View v) {
        Log.d(TAG, "onClickYes");

        if (OFF.equals(mImgYes.getTag().toString())) {
            // YES off なら on
            mImgYes.setImageResource(R.drawable.yes_on);
            mImgYes.setTag(ON);

            // NO off なら on
            mImgNo.setImageResource(R.drawable.no_off);
            mImgNo.setTag(OFF);
        }
    }

    /**
     * No画像押下処理。
     * 
     * @param v ビュー
     */
    public void onClickNo(View v) {
        Log.d(TAG, "onClickNo");

        if (OFF.equals(mImgNo.getTag().toString())) {
            // NO off なら on
            mImgNo.setImageResource(R.drawable.no_on);
            mImgNo.setTag(ON);

            // YES off なら on
            mImgYes.setImageResource(R.drawable.yes_off);
            mImgYes.setTag(OFF);
        }
    }

    /**
     * 書込みボタン押下処理。
     * 
     * @param v ビュー
     */
    public void onClick(View v) {
        Log.d("YesNoPillow", "Button Write!");

        disableNdefExchangeMode();
        enableTagWriteMode();

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("タグを近づけて！");
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "onCancel");
                disableTagWriteMode();
                enableNdefExchangeMode();
            }
        });
        mDialog = dialog.create();
        mDialog.show();
    }

    /**
     * 選択中の画像からYesかNoを判断する。
     * 
     * @return Yes Or No
     */
    private String getAnswer() {
        String msg = NO;
        if (ON.equals(mImgYes.getTag().toString())) {
            msg = YES;
        }
        return msg;
    }

    /**
     * Ndefメッセージを取得する。
     * 
     * @return Ndefメッセージ
     */
    private NdefMessage getNdef() {
        byte[] textBytes = getAnswer().getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                new byte[] {}, textBytes);
        return new NdefMessage(new NdefRecord[] {
                textRecord
        });
    }

    /*
     * (非 Javadoc)
     * @see jp.pillow.yesno.AbstractActivity#enableNdefExchangeMode()
     */
    protected void enableNdefExchangeMode() {
        mNfcAdapter.setNdefPushMessage(getNdef(), this);
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    }

    /*
     * (非 Javadoc)
     * @see jp.pillow.yesno.AbstractActivity#disableNdefExchangeMode()
     */
    protected void disableNdefExchangeMode() {
        if (mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessage(getNdef(), this);
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    /**
     * 書込を有効化する。
     */
    private void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] {
                tagDetected
        };
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }

    /**
     * 書込を無効化する。
     */
    private void disableTagWriteMode() {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(this);
    }
}
