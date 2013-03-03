/**
 * 
 */

package jp.pillow.yesno;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * @author user1
 */
public abstract class AbstractActivity extends Activity {

    /** �Œ蕶�� */
    protected String TAG = "YesNoPillow";
    protected String YES = "YES";
    protected String NO = "NO";

    NfcAdapter mNfcAdapter;
    IntentFilter[] mNdefExchangeFilters;
    PendingIntent mNfcPendingIntent;
    String[][] techListArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // NFC�A�_�v�^�[��������
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // �C���e���g�t�B���^�[��������
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("text/plain");
        } catch (MalformedMimeTypeException e) {
        }
        mNdefExchangeFilters = new IntentFilter[] {
                ndefDetected
        };
        
        techListArray = new String[][]{
                new String[] { IsoDep.class.getName() },
                new String[] { NfcA.class.getName() },
                new String[] { NfcB.class.getName() },
                new String[] { NfcF.class.getName() },
                new String[] { NfcV.class.getName() },
                new String[] { Ndef.class.getName() },
                new String[] { NdefFormatable.class.getName() },
                new String[] { MifareClassic.class.getName() },
                new String[] { MifareUltralight.class.getName() }
        };

        // ��ʂ̃X�^�b�N���V���O���g�b�v�ɏ�����
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    /*
     * (�� Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        Log.d(TAG, "abstract onResume");
        super.onResume();
        enableNdefExchangeMode();
    }

    /*
     * (�� Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "abstract onPause");
        super.onPause();
        disableNdefExchangeMode();
    }

    /**
     * Ndef��L��������B
     */
    protected abstract void enableNdefExchangeMode();

    /**
     * Ndef�𖳌�������B
     */
    protected abstract void disableNdefExchangeMode();
    
    /**
     * �g�[�X�g��\������B
     * 
     * @param text �\�����镶����
     */
    protected void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
