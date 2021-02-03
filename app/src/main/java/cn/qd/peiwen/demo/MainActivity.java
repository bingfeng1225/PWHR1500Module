package cn.qd.peiwen.demo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import cn.haier.bio.medical.hr1500.HR1500Manager;
import cn.haier.bio.medical.hr1500.IHR1500Listener;

public class MainActivity extends AppCompatActivity implements IHR1500Listener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HR1500Manager.getInstance().init("串口路径");
        HR1500Manager.getInstance().changeListener(this);
        HR1500Manager.getInstance().enable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HR1500Manager.getInstance().disable();
        HR1500Manager.getInstance().release();
    }


    @Override
    public void onHR1500Connected() {

    }

    @Override
    public void onHR1500Print(String message) {

    }

    @Override
    public void onHR1500Exception(Throwable throwable) {

    }

    @Override
    public boolean onHR1500CheckFrame(byte[] data) {
        return false;
    }

    @Override
    public void onHR1500PackageReceived(byte[] data) {

    }
}
