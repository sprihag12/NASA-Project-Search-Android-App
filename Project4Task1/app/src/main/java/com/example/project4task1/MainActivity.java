/* * @author Spriha Gupta 11/10/2019
 * This file is the Main file of the android app i.e the UI thread.
 * It calls the helper threads on GetDetails.java to perform the fetch
 * operations to allow a responsive UI
 */

package com.example.project4task1;
//import necessary libraries
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import java.util.Arrays;
import java.util.List;
/*Sources://https://stackoverflow.com/questions/46137234/filter-json-array
//https://stackoverflow.com/questions/13377361/how-to-create-a-drop-down-list*/

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MainActivity ma = this;
        setContentView(R.layout.activity_main);
        /*
         * The dropdown listener will need a reference to this object, so that upon successfully finding details from NASA techport, it
         * can callback to this object with the resulting details.
         */
        //instantiate getdetails.java and called projectlist to get default list of projects on app start. Once executed, helper thread calls
        //ip.dropdownReady
        GetDetails gp = new GetDetails();
        gp.projectlist(ma);
    }

    public void dropdownReady(final String filtered ) {
        final MainActivity ma = this;
        //get android id for the emulator. Each emulator will have a unique android id. getContentResolver can only be called in the UI thread,
       // hence is passed as a parameter to search function in helper thread
        final String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        //create a list to split the project ids
        final List<String> items = Arrays.asList(filtered.split("%"));
        //Find the dropdown and add a listener to it
        final Spinner dynamicSpinner=(Spinner)findViewById(R.id.spinner);

        if (items.size() !=0) {
            //Dynamically populate the dropdown with the projects
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
            dynamicSpinner.setAdapter(adapter);
            dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                boolean mSpinnerInitialized;
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    //when an item is selected,store the project id and send it as paramter to helper thread
                    String searchword = dynamicSpinner.getSelectedItem().toString();
                    GetDetails gp = new GetDetails();
                    if (!mSpinnerInitialized) {
                        //the dropdown shows the first project id on android startup
                        searchword=items.get(0);
                        mSpinnerInitialized = true;
                    }
                    gp.search(searchword,android_id, ma);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    //do nothing
                }
            });
        }
    }

    public void textReady(String details) {
        //find textview and poulate it with the text once the details have been fetched from api in the helper thread
        TextView searchView = (TextView)findViewById(R.id.textView3);
        if (details != null) {
            //The details is split into project name,status and lead organization name by a delimiter
            searchView.setText("Project Name: "+details.split("&")[0]+"\nStatus: "+details.split("&")[1]+"\nLead Organization: "+details.split("&")[2]);
            searchView.setVisibility(View.VISIBLE);
        } else {
            //If nothing is found,text is set to blank
            searchView.setText("");
            searchView.setVisibility(View.INVISIBLE);
        }
    }

}
