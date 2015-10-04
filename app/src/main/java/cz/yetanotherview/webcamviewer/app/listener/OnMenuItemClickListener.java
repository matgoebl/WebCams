package cz.yetanotherview.webcamviewer.app.listener;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import cz.yetanotherview.webcamviewer.app.R;

public class OnMenuItemClickListener implements Toolbar.OnMenuItemClickListener {
    @Override
    public boolean onMenuItemClick(MenuItem item) {

        Log.i("Menu clicked", String.valueOf(item.getItemId()));

//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            // Your action here
//            return true;
//        }


//        int numberOfColumns = 1; //ToDo
//        boolean imagesOnOff = true; //ToDO
//        switch (item.getItemId()) {
//
//            case R.id.action_search:
//                return super.onOptionsItemSelected(item);
//
//            case R.id.action_refresh:
//                //refresh();
//                break;
//
//            case R.id.action_dashboard:
//                if (numberOfColumns == 1) {
//                    numberOfColumns = 2;
//                    item.setIcon(R.drawable.ic_action_view_day);
//                }
//                else if (numberOfColumns == 2) {
//                    numberOfColumns = 1;
//                    item.setIcon(R.drawable.ic_action_dashboard);
//                }
//                //initRecyclerView();
//                //saveToPref();
//                break;
//
//            case R.id.action_sort:
//                //showSortDialog();
//                break;
//
//            case R.id.action_image_on_off:
//                if (imagesOnOff) {
//                    imagesOnOff = false;
//                    item.setTitle(R.string.images_on);
//                    item.setIcon(R.drawable.ic_action_image_on);
//                } else {
//                    imagesOnOff = true;
//                    item.setTitle(R.string.images_off);
//                    item.setIcon(R.drawable.ic_action_image_off);
//                }
//                //initRecyclerView();
//                //saveToPref();
//                break;
//
//            case R.id.action_settings:
//                //openSettings();
//                break;
//
//            case R.id.menu_help:
//                //openHelp();
//                break;
//
//            default:
//                break;
//        }

        return false;
    }
}
