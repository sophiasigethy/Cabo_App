package msp.group3.caboclient.CaboView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;

import org.json.JSONException;

import msp.group3.caboclient.CaboController.JSON_commands;
import msp.group3.caboclient.CaboModel.Player;
import msp.group3.caboclient.R;

public class SettingsFragment extends Fragment {

    com.google.android.material.slider.Slider slider;
    TextView sliderValueText;
    float currentMaxScoreOnSlider=100;
    Button updateScoreButton;

    public SettingsFragment() {
        super(R.layout.settings_fragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment

        return inflater.inflate(R.layout.settings_fragment, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        slider = view.findViewById(R.id.max_score_slider);
        sliderValueText = view.findViewById(R.id.slider_value_text);
        sliderValueText.setText("100");
        updateScoreButton = view.findViewById(R.id.update_score_button);
        updateScoreButton.setSaveEnabled(false);
        updateScoreButton.setAlpha(0.3f);

        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                sliderValueText.setText(""+value);
                currentMaxScoreOnSlider=value;
                updateScoreButton.setSaveEnabled(true);
                updateScoreButton.setAlpha(1f);
            }
        });

        updateScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateScoreButton.setSaveEnabled(false);
                updateScoreButton.setAlpha(0.3f);
                try {
                    ((WaitingRoomActivity) getActivity()).communicator.sendMessage(JSON_commands.sendMaxPoints((int) currentMaxScoreOnSlider));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }
}
