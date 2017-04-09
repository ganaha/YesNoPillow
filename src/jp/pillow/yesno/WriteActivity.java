
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
     * YesNo�摜��OnOff���
     */
    private static final String ON = "ON";
    private static final String OFF = "OFF";

    /** �C���e���g�t�B���^�[ */
    IntentFilter[] mWriteTagFilters;

    /** YesNo�摜 */
    private ImageView mImgYes;
    private ImageView mImgNo;

    /** �������[�h */
    private boolean mWriteMode = false;

    /** �_�C�A���O */
    private AlertDialog mDialog;

    /*
     * (�� Javadoc)
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
     * (�� Javadoc)
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
            // �^�O�ɏ���
            if (writeTag(getNdef(), detectedTag)) {
                // �����̏ꍇ�A�������ݑ҂��_�C�A���O�����
                mDialog.dismiss();
            }
        }
    }

    /**
     * �^�O�ɏ������ށB
     * 
     * @param message �������b�Z�[�W
     * @param tag �^�O
     * @return ��������(True:����, False:���s)
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
                    // ���t�H�[�}�b�g
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
                    // �t�H�[�}�b�g�ς�
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
     * Yes�摜���������B
     * 
     * @param v �r���[
     */
    public void onClickYes(View v) {
        Log.d(TAG, "onClickYes");

        if (OFF.equals(mImgYes.getTag().toString())) {
            // YES off �Ȃ� on
            mImgYes.setImageResource(R.drawable.yes_on);
            mImgYes.setTag(ON);

            // NO off �Ȃ� on
            mImgNo.setImageResource(R.drawable.no_off);
            mImgNo.setTag(OFF);
        }
    }

    /**
     * No�摜���������B
     * 
     * @param v �r���[
     */
    public void onClickNo(View v) {
        Log.d(TAG, "onClickNo");

        if (OFF.equals(mImgNo.getTag().toString())) {
            // NO off �Ȃ� on
            mImgNo.setImageResource(R.drawable.no_on);
            mImgNo.setTag(ON);

            // YES off �Ȃ� on
            mImgYes.setImageResource(R.drawable.yes_off);
            mImgYes.setTag(OFF);
        }
    }

    /**
     * �����݃{�^�����������B
     * 
     * @param v �r���[
     */
    public void onClick(View v) {
        Log.d("YesNoPillow", "Button Write!");

        disableNdefExchangeMode();
        enableTagWriteMode();

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("�^�O���߂Â��āI");
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
     * �I�𒆂̉摜����Yes��No�𔻒f����B
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
     * Ndef���b�Z�[�W���擾����B
     * 
     * @return Ndef���b�Z�[�W
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
     * (�� Javadoc)
     * @see jp.pillow.yesno.AbstractActivity#enableNdefExchangeMode()
     */
    protected void enableNdefExchangeMode() {
        mNfcAdapter.setNdefPushMessage(getNdef(), this);
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    }

    /*
     * (�� Javadoc)
     * @see jp.pillow.yesno.AbstractActivity#disableNdefExchangeMode()
     */
    protected void disableNdefExchangeMode() {
        if (mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessage(getNdef(), this);
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    /**
     * ������L��������B
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
     * �����𖳌�������B
     */
    private void disableTagWriteMode() {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(this);
    }
}
