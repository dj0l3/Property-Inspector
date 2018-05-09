package com.sakthi.propertyinspector;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.sakthi.propertyinspector.data.Area;
import com.sakthi.propertyinspector.data.PropertyInfo;

import io.fabric.sdk.android.Fabric;

public class NewAreaActivity extends AppCompatActivity {

    private Spinner mStatusSpinner;
    private Spinner mTypeSpinner;
    private PropertyInfo mPropertyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_new_area);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        final EditText areaName=(EditText)findViewById(R.id.editRoomAlias);
        mPropertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();
        final Area newArea=mPropertyInfo.getNewArea();

        TextView roomNoView=(TextView)findViewById(R.id.textRoomNumber);
        roomNoView.setText(""+newArea.getRoomNo());

        mStatusSpinner=(Spinner)findViewById(R.id.spinnerRoomStatus);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,mPropertyInfo.getStatusArray());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mStatusSpinner.setAdapter(arrayAdapter);

        mTypeSpinner=(Spinner)findViewById(R.id.spinnerRoomType);
        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,mPropertyInfo.getRoomTypes());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(arrayAdapter);


        Button butAdd=(Button)findViewById(R.id.but_newarea_add);

        butAdd.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {

                                          String name=areaName.getText().toString().trim();
                                          if (name.length() <= 0) {
                                              areaName.setError("Area name is empty!!!");
                                              return;
                                          }
                                          if (mPropertyInfo.isAreaAvailable(name)) {
                                              areaName.setError("Area is already available!!!");
                                              return;
                                          }
                                          if (mTypeSpinner.getSelectedItem() == null) {
                                              areaName.setError("No room type selected!!!");
                                              return;
                                          }

                                          newArea.setAlias(name);
                                          newArea.setRoomType(mPropertyInfo.getRoomTypeIndx(mTypeSpinner.getSelectedItem().toString()));
                                          newArea.setStatus(mStatusSpinner.getSelectedItemPosition());

                                          mPropertyInfo.addArea(newArea);
                                          setResult(Activity.RESULT_OK);
                                          finish();

                                      }
                                  });

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

}
