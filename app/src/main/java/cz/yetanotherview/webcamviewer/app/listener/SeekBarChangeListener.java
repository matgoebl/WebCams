/*
* ******************************************************************************
* Copyright (c) 2013-2015 Tomas Valenta.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* *****************************************************************************
*/

package cz.yetanotherview.webcamviewer.app.listener;

import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

    private TextView seekBarText;
    private String units;
    private int seekBarCorrection;
    private int val;

    public SeekBarChangeListener(SeekBar seekBar, TextView seekBarText, int seekBarCorrection, String units) {
        super();
        this.seekBarText = seekBarText;
        this.units = units;
        this.seekBarCorrection = seekBarCorrection;
        this.val = seekBar.getProgress();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
        val = progressValue + seekBarCorrection;
        seekBarText.setText(val + units);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}