package com.billy.presentegram.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.billy.presentegram.R;
import com.rtugeek.android.colorseekbar.ColorSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class PreparePrompterFragment extends Fragment {

    @BindView(R.id.color_seekBar)
    ColorSeekBar colorSeekBar;

    @BindView(R.id.et_prompter_text)
    EditText prompterText;

    @BindView(R.id.rg_text_size)
    RadioGroup textSize;




    SetPrompterTextProperties properties;

    interface SetPrompterTextProperties{
        void setPrompterText(String text, float textSize, int color );
    }



    public PreparePrompterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_prepare_prompter, container, false);
        ButterKnife.bind(this, rootView);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);



        properties = (SetPrompterTextProperties) getActivity();

        prompterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                onChangeProperties();

            }
        });


        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int i, int i1, int i2) {
                prompterText.setTextColor(i2);
                onChangeProperties();
            }
        });


        textSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i) {
                    case R.id.rb_text_size_1x:
                        prompterText.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f);
                        break;
                    case R.id.rb_text_size_2x:
                        prompterText.setTextSize(TypedValue.COMPLEX_UNIT_SP,20f);
                        break;
                    case R.id.rb_text_size_3x:
                        prompterText.setTextSize(TypedValue.COMPLEX_UNIT_SP,22f);
                        break;
                    case R.id.rb_text_size_4x:
                        prompterText.setTextSize(TypedValue.COMPLEX_UNIT_SP,24f);
                        break;

                    default:
                        break;
                }
                
                onChangeProperties();
            }
        });

        return rootView;
    }


    
    private void onChangeProperties(){
        
        if (properties != null){
            properties.setPrompterText(prompterText.getText().toString()
                    ,prompterText.getPaint().getTextSize()/getResources().getDisplayMetrics().scaledDensity
                    ,prompterText.getCurrentTextColor());


        }
    }


}
