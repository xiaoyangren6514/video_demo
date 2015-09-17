package com.happy.mobileproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.happy.mobileproject.activity.VideoListActivity;


public class MainActivity extends Activity implements View.OnClickListener {

    private Button loadLocalData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadLocalData = (Button) this.findViewById(R.id.loadLocalData);

        loadLocalData.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.loadLocalData:
                intent = new Intent(MainActivity.this, VideoListActivity.class);
                startActivity(intent);
                break;
        }
    }
}
