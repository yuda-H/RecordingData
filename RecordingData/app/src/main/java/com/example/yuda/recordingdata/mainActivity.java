package com.example.yuda.recordingdata;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


/*
    step1.  press "scan" to scanning wifi-information immediately
    step2.  press "get excel" to copying or creating excel by wifi-data
    step3.  press "start" to recording wifi-data to default excel
    step4.  if you want to  cancel or stop recording, press "stop"
    step5.  if you do not want to record wifi-data anymore, press "finish"
 */

public class mainActivity extends AppCompatActivity {
    TextView txtMessage;
    EditText edtPositionX, edtPositionY;
    Button btnScanningWifiInfo,btnReadCreate, btnStartStop, btnCloseXls;
    WifiManager mWifiManager;
    List<ScanResult> lstWifiInfoResult = null;
    String[] aryWifiInfo = new String[]{};
    String[] aryWifiBssid = new String[]{};
    String[] aryWifiName = new String[]{};
    String[][] aryChoseItemsNameAndBssid = new String[][]{};
    ListView lsvShowingInfo;
    ArrayAdapter adapter;
    jxlFile mJxlFile = new jxlFile();

/*
    @Override
    protected void onPause() {
        super.onPause();

        if (mJxlFile.onlyRead != null || mJxlFile.copyToWritable != null || mJxlFile.copyToWritableSheet != null) {


            try {
                mJxlFile.stopRecording();
                mJxlFile.closeWork();
            } catch (IOException | WriteException | BiffException e) {
                e.printStackTrace();
            }


        }

    }//*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDynamicsLayout();
        txtMessage = (TextView)findViewById(R.id.txtMessage);
        edtPositionX = (EditText)findViewById(R.id.edtPositionX);
        edtPositionY = (EditText)findViewById(R.id.edtPositionY);
        btnScanningWifiInfo = (Button)findViewById(R.id.btnScanningWifiInfo);
        btnReadCreate = (Button)findViewById(R.id.btnReadCreate);
        btnStartStop = (Button)findViewById(R.id.btnStartStop);
        btnCloseXls = (Button)findViewById(R.id.btnCloseXls);
        mWifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lsvShowingInfo = (ListView)findViewById(R.id.lsvShowingfInfo);


        // scanning wifi information
        btnScanningWifiInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aryChoseItemsNameAndBssid = new String[][]{};   // reset
                aryWifiInfo = new String[]{};
                aryWifiName = new String[]{};
                aryWifiBssid = new String[]{};
                lstWifiInfoResult = null;
                lsvShowingInfo.clearChoices();      // reset end

                txtMessage.setText("您將會紀錄如下列AP資訊，若沒有您想紀錄的AP資訊，請再重新點擊一次 SCAN 按鈕。");
                mWifiManager.startScan();
                lstWifiInfoResult = mWifiManager.getScanResults();
                aryWifiInfo = new String[lstWifiInfoResult.size()];
                aryWifiName = new String[lstWifiInfoResult.size()];
                aryWifiBssid = new String[lstWifiInfoResult.size()];
                for (int i=0; i<lstWifiInfoResult.size(); i++) {
                    aryWifiInfo[i] = lstWifiInfoResult.get(i).SSID+"\n\t"+lstWifiInfoResult.get(i).BSSID+"____"+lstWifiInfoResult.get(i).level;
                    aryWifiName[i] = lstWifiInfoResult.get(i).SSID;
                    aryWifiBssid[i] = lstWifiInfoResult.get(i).BSSID;
                }
                adapter = new ArrayAdapter(mainActivity.this,android.R.layout.simple_list_item_multiple_choice,aryWifiInfo);
                lsvShowingInfo.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                lsvShowingInfo.setAdapter(adapter);
            }
        });

        // reading already exist wifi-info or create new one
        btnReadCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnReadCreate.getText().toString().equals("excel")) {
                    btnReadCreate.setText("select all");
                    // read or create excel
                    try {
                        mJxlFile.getExcel();
                    } catch (IOException | WriteException | BiffException e) {
                        e.printStackTrace();
                    }

                }
                else if (btnReadCreate.getText().toString().equals("select all")) {
                    btnReadCreate.setText("cancel all");
                    for (int i=0; i<aryWifiInfo.length; i++) {
                        lsvShowingInfo.setItemChecked(i,true);
                    }
                }
                else {
                    btnReadCreate.setText("select all");
                    for (int i=0; i<aryWifiInfo.length; i++) {
                        lsvShowingInfo.setItemChecked(i,false);
                    }
                }
            }
        });

        // starting recoding wifi-data to Excle
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btnReadCreate.getText().toString().equals("excel")) {
                    Toast.makeText(getApplicationContext(),"Please press EXCEL before START.",Toast.LENGTH_SHORT).show();
                }
                else if (edtPositionX.getText().toString().equals("") ||
                        edtPositionY.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(),"Please make sure position have been entered.",Toast.LENGTH_SHORT).show();
                }
                else if (lstWifiInfoResult == null) {
                    Toast.makeText(getApplicationContext(),"Please make sure wifi have been scanned.",Toast.LENGTH_SHORT).show();
                }
                else if (btnStartStop.getText().toString().equals("start")) {

                    // starting recording activities
                    aryChoseItemsNameAndBssid = new String[2][lsvShowingInfo.getCheckedItemCount()];
                    int count = 0;
                    for (int i=0; i<lsvShowingInfo.getCheckedItemPositions().size(); i++) {
                        if (lsvShowingInfo.getCheckedItemPositions().valueAt(i)) {
                            aryChoseItemsNameAndBssid[0][i-count] = aryWifiName[lsvShowingInfo.getCheckedItemPositions().keyAt(i)];
                            aryChoseItemsNameAndBssid[1][i-count] = aryWifiBssid[lsvShowingInfo.getCheckedItemPositions().keyAt(i)];

                        } else {
                            count++;
                        }
                    }
                    if (aryChoseItemsNameAndBssid[0].length == 0) {
                        Toast.makeText(getApplicationContext(),"You did not choose any item.",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"You have chosen these items with: \n"+Arrays.deepToString(aryChoseItemsNameAndBssid[0])
                                ,Toast.LENGTH_SHORT).show();
                        btnStartStop.setText("stop");

                        try {
                            mJxlFile.startRecording(aryChoseItemsNameAndBssid);
                        } catch (WriteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    btnStartStop.setText("start");
                    // stopping recording activities suddenly
                    mJxlFile.stopRecording();
                }
            }
        });

        //
        btnCloseXls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btnReadCreate.getText().toString().equals("excel")) {
                    Toast.makeText(getApplicationContext(),"Please press EXCEL before FINISH.",Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        mJxlFile.stopRecording();
                        mJxlFile.closeWork();
                    } catch (IOException | WriteException | BiffException e) {
                        e.printStackTrace();
                    }
                    btnReadCreate.setText("excel");
                    btnStartStop.setText("start");

                }
            }
        });


    }

    private class jxlFile {
        File mFolder = new File(Environment.getExternalStorageDirectory().getPath()+"/RecordingData/");
        File onlyReadFile = new File(Environment.getExternalStorageDirectory().getPath()+"/RecordingData/wifiData.xls");
        Workbook onlyRead = null;
        WritableWorkbook copyToWritable = null;
        WritableSheet copyToWritableSheet = null;
        Handler handler = new Handler();
        Runnable runnable = null;
        int delayTime = 1;
        int dataRecordsLimits = 30;
        int count  = 1;

        void getExcel() throws IOException, WriteException, BiffException {
            if (!mFolder.exists()) {
                mFolder.mkdirs();
            }
            if (!onlyReadFile.exists()) {
                WritableWorkbook writable = Workbook.createWorkbook(onlyReadFile);
                WritableSheet writableSheet = writable.createSheet("wifiData",0);
                writableSheet.addCell(new Label(0,0,"SSID"));
                writableSheet.addCell(new Label(1,0,"BSSID"));
                writableSheet.addCell(new Label(2,0,"x"));
                writableSheet.addCell(new Label(3,0,"y"));
                writableSheet.addCell(new Label(4,0,"RSSI..."));
                writable.write();
                writable.close();
            }
            this.onlyRead = Workbook.getWorkbook(onlyReadFile);
            this.copyToWritable = Workbook.createWorkbook(new File(Environment.getExternalStorageDirectory().
                    getPath()+"/RecordingData/wifiDataCopy.xls"),this.onlyRead);
            this.copyToWritableSheet = this.copyToWritable.getSheet(0);
        }

        void startRecording(final String[][] choseWifiNameAndBssid) throws WriteException {
            final int getExcelRows = this.copyToWritableSheet.getRows();
            String positionX = edtPositionX.getText().toString();
            String positionY = edtPositionY.getText().toString();


            for (int i=0; i<choseWifiNameAndBssid[0].length; i++) {
                this.copyToWritableSheet.addCell(new Label(0,getExcelRows+i,choseWifiNameAndBssid[0][i]));
                this.copyToWritableSheet.addCell(new Label(1,getExcelRows+i,choseWifiNameAndBssid[1][i]));
                this.copyToWritableSheet.addCell(new Label(2,getExcelRows+i,positionX));
                this.copyToWritableSheet.addCell(new Label(3,getExcelRows+i,positionY));
            }
            this.runnable = new Runnable() {
                @Override
                public void run() {
                    // recording process
                    mWifiManager.startScan();
                    List<ScanResult> results = mWifiManager.getScanResults();
                    String[] resultWifiBSSID = new String[results.size()];
                    for (int i=0; i<results.size(); i++) {
                        resultWifiBSSID[i] = results.get(i).BSSID;
                    }
                    for (int i=0; i<choseWifiNameAndBssid[0].length; i++) {
                        int index = Arrays.asList(resultWifiBSSID).indexOf(choseWifiNameAndBssid[1][i]);
                        try {
                            if (index != -1) {
                                copyToWritableSheet.addCell(new Label(3+count,i+getExcelRows,results.get(index).level+""));
                            } else {
                                copyToWritableSheet.addCell(new Label(3+count,i+getExcelRows,""));
                            }
                        } catch (WriteException e) {
                            e.printStackTrace();
                        }
                    }

                    handler.postDelayed(this,delayTime*1000);

                    if (count <= dataRecordsLimits) {
                        txtMessage.setText("您正在紀錄第 "+count+" 筆資料，尚餘 " + (dataRecordsLimits-count) +" 筆資料");
                        count++;
                    } else {
                        stopRecording();
                        txtMessage.setText("紀錄已完成");
                    }
                }
            };
            this.handler.post(this.runnable);
        }

        void stopRecording() {
            this.handler.removeCallbacks(this.runnable);
            btnStartStop.setText("start");
            txtMessage.setText("已中斷紀錄動作，共紀錄了 "+(this.count-1)+" 筆資料");
            this.count = 1;
        }

        void closeWork() throws IOException, WriteException, BiffException {
            this.copyToWritable.write();
            this.copyToWritable.close();
            this.onlyRead.close();
            this.onlyRead = Workbook.getWorkbook(new File(Environment.getExternalStorageDirectory().
                    getPath()+"/RecordingData/wifiDataCopy.xls"));
            this.copyToWritable = Workbook.createWorkbook(this.onlyReadFile,this.onlyRead);
            this.copyToWritable.write();
            this.copyToWritable.close();
        }

    }

    private void setDynamicsLayout() {
        LinearLayout dynamicsLayout = (LinearLayout)findViewById(R.id.dinamicsLayout);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int vWidth = dm.widthPixels;
        int vHeight = dm.heightPixels;
        dynamicsLayout.getLayoutParams().width=(int)(vWidth*0.95);
        dynamicsLayout.getLayoutParams().height=(int)((vHeight-100)*0.9);
    }
}
