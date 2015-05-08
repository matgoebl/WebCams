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

package cz.yetanotherview.webcamviewer.app.helper;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.Locale;

import cz.yetanotherview.webcamviewer.app.adapter.ManualSelectionAdapter;

public class OnFilterTextChange implements TextWatcher {
    private ManualSelectionAdapter manualSelectionAdapter;

    public OnFilterTextChange(ManualSelectionAdapter manualSelectionAdapter) {
        super();
        this.manualSelectionAdapter = manualSelectionAdapter;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String text = s.toString().trim().toLowerCase(Locale.getDefault());
        manualSelectionAdapter.getFilter().filter(text);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}