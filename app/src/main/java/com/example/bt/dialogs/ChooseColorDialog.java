package com.example.bt.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.bt.MemoryConnector;
import com.example.bt.R;
import com.example.bt.list_adapters.AllAppsRowItem;
import com.example.bt.views.BrightnessSlider;
import com.example.bt.views.ColorField;
import com.example.bt.views.ColorWheel;

import org.json.JSONException;
import org.json.JSONObject;

public class ChooseColorDialog extends DialogFragment {

    private ColorWheel colorWheel;
    private BrightnessSlider brightnessSlider;
    private Button buttonChoose;
    private ColorField colorPreview;

    public String packageName;
    public JSONObject jsonObject;
    public AllAppsRowItem listItem;
    public View listItemView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_choose_color, null);
        builder.setView(view);

        setupFields(view);
        setupEventListeners();

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(jsonObject != null && packageName != null) {
            if (jsonObject.has(packageName)) {
                try {
                    // get hsv
                    int color = jsonObject.getInt(packageName);
                    float[] hsv = new float[3];
                    Color.colorToHSV(color, hsv);

                    // populate views
                    colorWheel.SetColor(color);
                    brightnessSlider.SetProgress(hsv[2]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupFields(View parent) {
        colorWheel = parent.findViewById(R.id.chooseColorWheel);
        brightnessSlider = parent.findViewById(R.id.chooseBrightnessSlider);
        buttonChoose = parent.findViewById(R.id.buttonChoose);
        colorPreview = parent.findViewById(R.id.choosePreview);

        colorWheel.BindBrightnessSlider(brightnessSlider);
        brightnessSlider.BindColorPicker(colorWheel);
    }

    private void setupEventListeners() {
        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = colorWheel.GetColor(true);
                // if chosen color is not transparent - append to JSON array
                if(color != Color.TRANSPARENT) {
                    try {
                        // if brightness is 0 - color is transparent
                        if(brightnessSlider.GetBrightness() == 0)
                            color = Color.TRANSPARENT;

                        // create object prop
                        if(jsonObject.has(packageName))
                            jsonObject.remove(packageName);
                        if(color != Color.TRANSPARENT)
                            jsonObject.put(packageName, color);

                        // save ready-to-go json object to file
                        MemoryConnector.writeJsonToFile(getContext(), getString(R.string.file_name), jsonObject);

                        // update app in list
                        listItem.setColor(color);
                        ((ColorField)listItemView.findViewById(R.id.aa_color_field)).SetColor(color);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                dismiss();
            }
        });

        colorWheel.setOnColorChangeEventListener(new ColorWheel.OnColorChangeEventListener() {
            @Override
            public void onEvent(int color) {
            if(brightnessSlider.GetBrightness() == 0)
                color = Color.TRANSPARENT;
            colorPreview.SetColor(color);
            }
        });
    }
}
