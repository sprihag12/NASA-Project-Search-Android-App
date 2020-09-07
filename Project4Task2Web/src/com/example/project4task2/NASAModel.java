/* * @author Spriha Gupta 11/10/2019
 * This file is the Model component of the MVC, and it models the business
 * logic for the web application.  In this case, the business logic involves
 * making a request to nasa tech port and then fetching the json response
 * in order to fabricate dropdown options and project information on the android app.
 * It also connects to the mongodb and logs each request submitted by android into the
 * database. It fetches the database and calculates metrics when dashboard url is requested.
 * Source: https://stackoverflow.com/questions/22989806/find-the-most-common-string-in-arraylist/22990086
 * https://stackoverflow.com/questions/10791568/calculating-average-of-an-array-list
 * */
package com.example.project4task2;
//import necessary libraries
import com.google.gson.JsonParser;
import org.json.JSONObject;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.io.IOException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;


public class NASAModel {
 int average=0;
 String most_viewed_proj="";
 int unique_hits=0;
    public JSONObject doProjSearch() throws IOException, JSONException, ParseException {
        //When android starts, hit API 1(in Task0 documentation) to fetch the json response including all project ids
        String nasaURL = "https://api.nasa.gov/techport/api/projects?api_key=lKGcHbsc2qhsdWs0lAddU2CiStsR60dHUVOihivD";
        JSONObject json = fetch(nasaURL);
        return json;
    }
    public JSONObject doNASASearch(String androidId,String searchTag,String model, Long starttime) throws IOException, JSONException {
        searchTag = URLEncoder.encode(searchTag, "UTF-8");
        //When user selects a value in the dropdown, hit API 2(in Task0 documentation) with the project id to fetch the project details as json object
        String nasaURL = "https://api.nasa.gov/techport/api/projects/"+searchTag+".json?api_key="+"lKGcHbsc2qhsdWs0lAddU2CiStsR60dHUVOihivD";
        JSONObject json = fetch(nasaURL);
        Long stoptime=System.currentTimeMillis();
        //Store the request parameters sent by android in database along with the time taken to fetch the response i.e search latency
        storeinmongodb(androidId,searchTag,model,nasaURL,stoptime-starttime);
        return json;
    }
    private JSONObject fetch(String urlString) throws JSONException {
        //use http get request to connect to the relevant api
        final String REQUEST_METHOD = "GET";
        final int READ_TIMEOUT = 15000;
        final int CONNECTION_TIMEOUT = 15000;
        String response = "";
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(REQUEST_METHOD);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.connect();
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            String str;
            // Read each line of in adding each to response until end of line
            while ((str = in.readLine()) != null) {
                response += str;
            }

            in.close();
        } catch (IOException e) {
            System.out.println("Eeek, an exception");
        }
        //return response as string format
        JSONObject object = new JSONObject(response);

        return object;
    }

   public void  storeinmongodb(String androidId,String searchTag,String device,String url,Long latency){
        //connect to mongodb,create a database, collection and document. Please note this table has a few entries loaded for reference. This function is called everytime
       //a user selects the dropdown
       MongoClientURI connectionString = new MongoClientURI("mongodb+srv://spriha:hell1234@spriha-cluster-ukpyl.mongodb.net/test?retryWrites=true&w=majority");
       MongoClient mongoClient = new MongoClient(connectionString);
        DB db = mongoClient.getDB("project4_db");
       DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
       Date date = new Date();
        DBCollection table = db.getCollection("logs");
        BasicDBObject document = new BasicDBObject();
        //store all relevent details along with the date of the request in the document and insert it in the collection
        document.put("android_id", androidId);
        document.put("proj_id", searchTag);
        document.put("device", device);
        document.put("url", url);
        document.put("createdDate", dateFormat.format(date));
        document.put("FetchTime", String.valueOf(latency));
       table.insert(document);

       }
       public ArrayList<JsonObject>  fetchmongodb(){
           //connect to mongodb,call the collection.
           // This function is called everytime dashboard is requested
           ArrayList<JsonObject> dblist=new ArrayList<JsonObject>();
           MongoClientURI connectionString = new MongoClientURI("mongodb+srv://spriha:hell1234@spriha-cluster-ukpyl.mongodb.net/test?retryWrites=true&w=majority");
           MongoClient mongoClient = new MongoClient(connectionString);
           DB db = mongoClient.getDB("project4_db");
           DBCollection table = db.getCollection("logs");
           //set thr cursor reference to the logs collection
           DBCursor cursor = table.find();
           String tableObj= new String();
           ArrayList<Integer> latency=new ArrayList<>();
           ArrayList<String> id=new ArrayList<>();
           ArrayList<String> androids=new ArrayList<>();
        while (cursor.hasNext()) {
            //read each document and parse as json object
            tableObj=cursor.next().toString();
            JsonObject tableJsonObj = new JsonParser().parse(tableObj).getAsJsonObject();
            //convert latency time from string in " to integer
            int len=String.valueOf(tableJsonObj.get("FetchTime")).length();
             int lat_ = Integer.parseInt(String.valueOf(tableJsonObj.get("FetchTime")).substring(1,len-1));
             //while reading the collection,store relevant fields in a list to be queried later
             latency.add(lat_);
             String id_ = tableJsonObj.get("proj_id").toString();
            id.add(id_);
            String and_ = tableJsonObj.get("android_id").toString();
            androids.add(and_);
            //create list of json objects
            dblist.add(tableJsonObj);
        }
        //call methods to calculate metrics
           calculateAverage(latency);
           mostViewedProj(id);
           uniqueHits(androids);
    return dblist;
    }
    public void calculateAverage( ArrayList<Integer> latency) {
        //this function calculates the average time taken to fetch the json response to the requested project id
         int sum=0;
        if(!latency.isEmpty()) {
            for (int lat : latency) {
                sum += lat;
            }
            average =sum/ latency.size();
        }
    }

    public void mostViewedProj(ArrayList<String> id)
    {
// this function converts arraylist to hashmap to count occurences of each project id in the table
        Map<String, Integer> wordMap = new HashMap<String, Integer>();

        for (String st : id) {
            String input = st.toUpperCase();
            if (wordMap.get(input) != null) {
                Integer count = wordMap.get(input) + 1;
                wordMap.put(input, count);
            } else {
                wordMap.put(input, 1);
            }
        }
        //Get the key corresponding to the maximum value. If 2 keys have same values, it returns the first key as per natural order
        Object maxEntry = Collections.max(wordMap.entrySet(), Map.Entry.comparingByValue()).getKey();
        most_viewed_proj =String.valueOf(maxEntry);

    }
    public void uniqueHits(ArrayList<String> id)
    {
        //Convert array to set to store distinct android ids
        Set<String> set = new HashSet<String>(id);
        unique_hits=set.size();
    }
    //the following getter methods are used to send values of the metrics to the controller

    public int getAverage()
    {return this.average;}
    public String getMostViewedProj()
    {return this.most_viewed_proj;}
    public int getUniqueHits()
    {return this.unique_hits;}
}