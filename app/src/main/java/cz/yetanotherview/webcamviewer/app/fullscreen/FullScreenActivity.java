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

package cz.yetanotherview.webcamviewer.app.fullscreen;

import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.ImmersiveMode;
import cz.yetanotherview.webcamviewer.app.maps.MapsFragment;

public class FullScreenActivity extends AppCompatActivity {

    private MapsFragment mapsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_container);

        Bundle bundle = getIntent().getExtras();
        boolean map = bundle.getBoolean("map");
        boolean screenAlwaysOn = bundle.getBoolean("screenAlwaysOn");

        // Go FullScreen only on KitKat and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            new ImmersiveMode().goFullScreen(this);
        }

        // Screen Always on
        if (screenAlwaysOn){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Fragment Transaction
        if (findViewById(R.id.full_screen_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            if (!map) {
                FullScreenFragment fullScreenFragment = new FullScreenFragment();
                fullScreenFragment.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .add(R.id.full_screen_container, fullScreenFragment).commit();
            }
            else {
                mapsFragment = new MapsFragment();
                mapsFragment.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .add(R.id.full_screen_container, mapsFragment).commit();
            }
        }
    }

    public void replaceFragments() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (mapsFragment == null) {
            mapsFragment = new MapsFragment();
        }
        transaction.replace(R.id.full_screen_container, mapsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
