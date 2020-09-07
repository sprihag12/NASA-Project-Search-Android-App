/* * @author Spriha Gupta 11/10/2019
 * This class provides capabilities to search for projects details on nasa technport  given a project id.  The method "projectlist" is the entry to the class.
 * Network operations cannot be done from the UI thread, therefore this class makes use of an AsyncTask inner class that will do the network
 * operations in a separate worker thread.  However, any UI updates should be done in the UI thread so avoid any synchronization problems.
 * onPostExecution runs in the UI thread, and it calls the dropdownReady once dropdown is ready and textReady once text is ready to be displayed.

 * /*Sources:https://stackoverflow.com/questions/4308554/simplest-way-to-read-json-from-a-url-in-java/4308572
 * https://stackoverflow.com/questions/1995439/get-android-phone-model-programmatically
 */
//import necessary files
package com.example.project4task1;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;

public class GetDetails {
    MainActivity ip = null;
    public void projectlist( MainActivity ip) {
        this.ip = ip;
        new NASAProjSearch().execute();
    }
    public void search(String searchTerm, String ID, MainActivity ip) {
        this.ip = ip;
        new ProjDetailsSearch().execute(searchTerm,ID);
    }
    private class NASAProjSearch extends AsyncTask<String, Void, String > {
        @Override
        protected String doInBackground(String... urls) {
            //1st URL is for Task1,2nd URL is for localhost and 3rd is for Task2 and has been left commented intentionally
           // final String URL =  "https://lit-eyrie-68843.herokuapp.com/getProjIDs";
            //final String URL =  "http://10.0.2.2:8088/getProjIDs";
            final String URL =  "https://radiant-everglades-11032.herokuapp.com/getProjIDs";
            String result = "";
            try {
                //connect to webservice without any parameters and receive project list as json response
                JSONObject json=connectionToServlet(URL);
                result=parseforProjects(json);
            }
            catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return result;
        }
        protected void onPostExecute(String filtered ) {
            ip.dropdownReady(filtered);
        }
    }
    /*
     * AsyncTask provides a simple way to use a thread separate from the UI thread in which to do network operations.
     * doInBackground is run in the helper thread.
     * onPostExecute is run in the UI thread, allowing for safe UI updates.
     */
    private class ProjDetailsSearch extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            //to get device name.For emulators, it shows Google Android SDK version(if Nexus is used). For physical devices, it shows model with manufacturer.
            String dev=getDeviceName();
            //1st URL is for Task1,2nd URL is for localhost and 3rd is for Task2 and has been left commented intentionally
      // final String URL =  "https://lit-eyrie-68843.herokuapp.com/getProjIDs?searchWord="+urls[0]+"&dev="+dev+"&id="+urls[1];
       // final String URL =  "http://10.0.2.2:8088/getProjIDs?searchWord="+urls[0]+"&dev="+dev+"&id="+urls[1];
           final String URL =  "https://radiant-everglades-11032.herokuapp.com/getProjIDs?searchWord="+urls[0]+"&dev="+dev+"&id="+urls[1];

        String result = "";
            try {
                //connect to webservice with the parameters proj id,android id and device model and receive text as json response
            JSONObject json=connectionToServlet(URL);
            result=parseforDetails(json);
        }
            catch (JSONException | IOException e) {
            e.printStackTrace();
        }
            return result;}

        protected void onPostExecute(String details) {
            ip.textReady(details);
        }
    }
    public String parseforProjects(JSONObject json) throws JSONException {
        //the json response is a json object of object of arrays of which the project ids is extracted
        JSONObject proj = (JSONObject) json.get("projects");
        JSONArray ary = proj.getJSONArray("projects");
        String result = "";
        //the json response is sorted by last updated and hence the top 10 project ids are the most recent updated projects
        //the project ids are concatenated by a delimiter
        for (int i = 0; i < 10; ++i) {
            JSONObject obj = ary.getJSONObject(i);
            result = result  + obj.getString("id")+ "%";
        }
        return result;
    }
    public String parseforDetails(JSONObject json) {
        //the json response is a json object of json objects with multiple keys of which only the project name,status and leadOrganization is extracted
        //The details are sent as one concatenated string
        JSONObject id=new JSONObject();
        String title=null;
        String status=null;
        String orgname=null;
        JSONObject leadOrg=new JSONObject();
        try {
            id= (JSONObject) json.get("project");
            title=id.getString("title");
            status= id.getString("status");
            leadOrg= (JSONObject) id.get("leadOrganization");
            orgname=leadOrg.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return title+"&"+status+"&"+orgname;
    }


    private JSONObject connectionToServlet(String urlString) throws IOException, JSONException {
        //use http get request to connect to the web service
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        String response = "";
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();
          // Read all the text returned by the service
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(), "UTF-8"));
            String str;
            // Read each line of in adding each to response until end of line
            while ((str = in.readLine()) != null) {
                // str is one line of text readLine() strips newline characters
                response += str;
            }
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //return response as string format
        JSONObject object = new JSONObject(response);
return object;
    }

public String getDeviceName() {
        //gets model and manufacturer names and concatenates them unless model string already contains manufacturer name
    String manufacturer = Build.MANUFACTURER;
    String model = Build.MODEL;
    if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
        return capitalize(model);
    } else {
        return capitalize(manufacturer) + " " + model;
    }
}
    public String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

}

