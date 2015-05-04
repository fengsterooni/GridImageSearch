package com.codepath.gridimagesearch.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.codepath.gridimagesearch.R;

public class FilterDialog extends DialogFragment {

    private Spinner spImageSize;
    private Spinner spImageColor;
    private Spinner spImageType;
    private EditText etImageSite;
    private Button buttonSave;
    private Button buttonCancel;

    public FilterDialog() {
        // Required empty public constructor
    }

    public static FilterDialog newInstance(String title) {
        FilterDialog frag = new FilterDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle("Advanced Filter");

        View view = inflater.inflate(R.layout.fragment_filter_dialog, container);

        spImageSize = (Spinner) view.findViewById(R.id.spImageSize);
        spImageColor = (Spinner) view.findViewById(R.id.spImageColor);
        spImageType = (Spinner) view.findViewById(R.id.spImageType);
        etImageSite = (EditText) view.findViewById(R.id.etImageSite);
        buttonSave = (Button) view.findViewById(R.id.buttonSave);
        buttonCancel = (Button) view.findViewById(R.id.buttonCancel);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String imageSize = spImageSize.getSelectedItem().toString();
                String imageColor = spImageColor.getSelectedItem().toString();
                String imageType = spImageType.getSelectedItem().toString();
                String imageSite = etImageSite.getText().toString();

                Log.i("INFO", "Image Size: " + imageSize);
                Log.i("INFO", "Image Color: " + imageColor);
                Log.i("INFO", "Image Type: " + imageType);
                Log.i("INFO", "Image Site: " + imageSite);

                FilterChangedListener listener = (FilterChangedListener) getActivity();
                listener.onFinishFilterDialog(imageSize, imageColor, imageType, imageSite);
                dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        populateSpinners();

        return view;
    }

    private void populateSpinners() {
        ArrayAdapter<CharSequence> isAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.imageSizeValue, android.R.layout.simple_spinner_item);
        int spinner_dd_item = android.R.layout.simple_spinner_dropdown_item;
        isAdapter.setDropDownViewResource(spinner_dd_item);
        spImageSize.setAdapter(isAdapter);

        ArrayAdapter<CharSequence> icAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.imageColorValue, android.R.layout.simple_spinner_item);
        spinner_dd_item = android.R.layout.simple_spinner_dropdown_item;
        icAdapter.setDropDownViewResource(spinner_dd_item);
        spImageColor.setAdapter(icAdapter);

        ArrayAdapter<CharSequence> itAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.imageTypeValue, android.R.layout.simple_spinner_item);
        spinner_dd_item = android.R.layout.simple_spinner_dropdown_item;
        itAdapter.setDropDownViewResource(spinner_dd_item);
        spImageType.setAdapter(itAdapter);

    }

    public interface FilterChangedListener {
        void onFinishFilterDialog(String imgSize, String imgColor, String imgType, String imgSite);
    }
}
