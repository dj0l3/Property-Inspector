package com.sakthi.propertyinspector.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sakthi.propertyinspector.R;
import com.sakthi.propertyinspector.data.InspectedFiles;
import com.sakthi.propertyinspector.data.PropertyInfo;

/**
 * Created by Satheesh on 12/06/17.
 * Sanghish IT Solutions
 * satheesh@sanghish.com
 */
public class InspectedFilesViewHolder extends RecyclerView.ViewHolder {

    private TextView lblAddress;
    private TextView lblPostalCode;
    private TextView lblUniqueKey;
    private TextView propertyId;
    private TextView picturesTaken;
    private Button btnOpen;
    private Button btnUpload;

    public InspectedFilesViewHolder(View itemView) {
        super(itemView);
        lblAddress = (TextView) itemView.findViewById(R.id.lblAddress);
        lblPostalCode = (TextView) itemView.findViewById(R.id.lblPostalCode);
        lblUniqueKey = (TextView) itemView.findViewById(R.id.lblUniqueKey);
        propertyId = (TextView) itemView.findViewById(R.id.property_id);
        picturesTaken = (TextView) itemView.findViewById(R.id.pictures_taken);
        btnOpen = (Button) itemView.findViewById(R.id.btnOpenInspectedFiles);
        btnUpload = (Button) itemView.findViewById(R.id.btnUploadToFTP);
    }

    public void onBind(InspectedFiles inspectedFiles, int position, View.OnClickListener onClickListener){
        if (inspectedFiles != null && inspectedFiles.propertyInfo != null) {
            lblAddress.setText(inspectedFiles.propertyInfo.getAddress());
            lblPostalCode.setText(inspectedFiles.propertyInfo.getPostalCode());
            lblUniqueKey.setText(String.valueOf(inspectedFiles.propertyInfo.getUniqueKey()));
            propertyId.setText(String.valueOf(inspectedFiles.propertyInfo.getPropertyId()));
            picturesTaken.setText(String.valueOf(inspectedFiles.propertyInfo.getTotalPictures()));

            btnOpen.setTag(R.id.lblAddress, inspectedFiles);
            btnOpen.setTag(R.id.lblPostalCode, position);
            btnOpen.setOnClickListener(onClickListener);

            btnUpload.setTag(R.id.lblUniqueKey, inspectedFiles);
            btnUpload.setTag(R.id.btnOpenInspectedFiles, position);
            btnUpload.setOnClickListener(onClickListener);
        }
    }
}
