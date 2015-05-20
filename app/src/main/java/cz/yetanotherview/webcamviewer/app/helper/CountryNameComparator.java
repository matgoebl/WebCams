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

import java.util.Comparator;

import cz.yetanotherview.webcamviewer.app.model.Country;

public class CountryNameComparator implements Comparator<Country> {
    @Override
    public int compare(Country country1, Country country2) {
        String strippedCountryName1 = Utils.getNameStrippedAccents(country1.getCountryName());
        String strippedCountryName2 = Utils.getNameStrippedAccents(country2.getCountryName());

        return strippedCountryName1.compareToIgnoreCase(strippedCountryName2);
    }
}
