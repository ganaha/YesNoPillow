
package jp.pillow.yesno;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ReadActivity extends AbstractActivity {

    /** Read�摜 */
    ImageView mImage;

    /*
     * (�� Javadoc)
     * @see jp.pillow.yesno.AbstractActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        mImage = (ImageView) findViewById(R.id.image_read);
    }

    /*
     * (�� Javadoc)
     * @see jp.pillow.yesno.AbstractActivity#onResume()
     */
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        Intent intent = getIntent();
        String action = intent.getAction();
        Log.d(TAG, "action: " + action);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            // �^�O�̏ꍇ
            NdefMessage[] messages = getNdefMessages(intent);
            byte[] payload = messages[0].getRecords()[0].getPayload();
            String msg = new String(payload);
            setImage(msg);
            setIntent(new Intent());
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            setImage("");
            setIntent(new Intent());
            // TODO:alert
        }
    }

    /*
     * (�� Javadoc)
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            NdefMessage[] msgs = getNdefMessages(intent);
            String body = new String(msgs[0].getRecords()[0].getPayload());
            promptForContent(body);
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            alert(getId(intent));
        }
    }

    /**
     * �ēǂݍ��݃_�C�A���O��\������B
     */
    private void promptForContent(final String msg) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.msg_read))
                .setPositiveButton(getString(R.string.msg_read_yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                setImage(msg);
                            }
                        })
                .setNegativeButton(getString(R.string.msg_read_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        }).show();
    }

    /**
     * �C���e���g����NFC ID ���擾����B
     * 
     * @param intent �C���e���g
     * @return ID
     */
    private String getId(Intent intent) {
        byte[] rawIds = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        if (rawIds == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < rawIds.length; i++) {
            sb.append(Integer.toHexString(rawIds[i] & 0xff));
        }
        return sb.toString();
    }

    /**
     * �_�C�A���O��\������B
     * 
     * @param msg ���b�Z�[�W
     */
    private void alert(String msg) {
        new AlertDialog.Builder(this).setTitle(msg).create().show();
    }

    /**
     * �C���e���g����Ndef���b�Z�[�W���擾����B
     * 
     * @param intent �C���e���g
     * @return Ndef���b�Z�[�W
     */
    private NdefMessage[] getNdefMessages(Intent intent) {
        NdefMessage[] msgs = null;
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
        } else {
            // Unknown tag type
            byte[] empty = new byte[] {};
            NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
            NdefMessage msg = new NdefMessage(new NdefRecord[] {
                    record
            });
            msgs = new NdefMessage[] {
                    msg
            };
        }
        return msgs;
    }

    /**
     * �Ǎ�������ɂ���ĉ摜�������ւ���B
     * 
     * @param yesno �Ǎ�������
     */
    private void setImage(String yesno) {
        if (YES.equals(yesno)) {
            Log.d(TAG, YES);
            mImage.setImageResource(R.drawable.yes_on);
            mImage.setTag(YES);
        } else if (NO.equals(yesno)) {
            Log.d(TAG, NO);
            mImage.setImageResource(R.drawable.no_on);
            mImage.setTag(NO);
        } else {
            mImage.setImageResource(R.drawable.nothing);
            mImage.setTag("");
        }
    }

    /**
     * ������ʂ֑J�ڂ���B
     * 
     * @param v �r���[
     */
    public void onClick(View v) {
        Log.d(TAG, "Button Clicked!");

        Intent intent = new Intent(this, WriteActivity.class);
        startActivity(intent);
    }

    /*
     * (�� Javadoc)
     * @see jp.pillow.yesno.AbstractActivity#disableNdefExchangeMode()
     */
    protected void disableNdefExchangeMode() {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    /*
     * (�� Javadoc)
     * @see jp.pillow.yesno.AbstractActivity#enableNdefExchangeMode()
     */
    protected void enableNdefExchangeMode() {
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters,
                techListArray);
    }
}
