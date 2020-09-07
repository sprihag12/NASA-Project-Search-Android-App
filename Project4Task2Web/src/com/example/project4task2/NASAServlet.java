/* * @author Spriha Gupta 11/10/2019
 * The welcome-file in the deployment descriptor (web.xml) points to this servlet.  The servlet acts as the controller.
 * This servlet includes the functionality of Task1 Web Service along with the integration of MongodDB and web dashboard. The
 * servlet hosts 2 urls /getProjIDs and /getDashboard.  /getDashboard renders dashboard.jsp view which displays the
 * mongoDB as a log table and 3 key metrics: the average latency, most viewed project and number of unique hits.
 * The model is provided by NASAModel.
 */

package com.example.project4task2;
//import necessary libraries
import java.io.IOException;
import com.google.gson.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/*
        * The following WebServlet annotation gives instructions to the web container.
        * It states that when the user browses to the URL path /getDashboard or /getProjIDs
        * then the servlet with the name NASAServlet should be used.
        */
@WebServlet(name = "NASAServlet",
        urlPatterns = {"/getProjIDs","/getDashboard"})
public class NASAServlet extends javax.servlet.http.HttpServlet {
    NASAModel nm = null;
    String page="/dashboard.jsp";
    @Override
    public void init() {
        nm = new NASAModel();
    }
    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        //to get requested URL
        String path = request.getServletPath();
        ArrayList<JsonObject> dblist;
        String mostViewed;
        int avgLatency;
        int uniqHits;
        switch (path) {
            case "/getProjIDs":
                //Fetch parameters(project is,phone model and android id) from request sent by android app
                String search = request.getParameter("searchWord");
                String model = request.getParameter("dev");
                String androidId=request.getParameter("id");
                PrintWriter out = response.getWriter();
                JSONObject json = null;
                //Log time when request reaches the web service
                Long starttime=System.currentTimeMillis();
                response.setContentType("application/json");
                if (search != null) {
                    try {
                        //When user selects a id from drop down, search parameter has a value. Hence, this function will be executed whenever user selects options from the dropdown.
                        json = nm.doNASASearch(androidId,search, model,starttime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        out.println("Information not found");
                    }
                } else {
                    //When android starts,search parameter is null. Hence, this function will be executed on create.
                    try {
                        json = nm.doProjSearch();
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
                // Get the printwriter object from response to write the required json object to the output strea
                //The json object returned from the functions is the JSON response from the API and is written back as response
                out.write(json.toString());
                out.flush();
                break;

            case "/getDashboard":
                //Fetch mongoDB as a list of json objects along with the key metrics
                dblist = nm.fetchmongodb();
                avgLatency=nm.getAverage();
                mostViewed=nm.getMostViewedProj();
                uniqHits=nm.getUniqueHits();
                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(page);
                if (dispatcher != null) {
                    //The returned list of json objects and metrics are passed as attributes to the servlet request which then renders the dashboard jsp page
                    // The dashboard jsp fetched these attributes and displays the list in a tabular format along with the metrics
                    request.setAttribute("db", dblist);
                    request.setAttribute("avgLatency", avgLatency);
                    request.setAttribute("mostViewed", mostViewed);
                    request.setAttribute("uniqHits", uniqHits);
                    dispatcher.forward(request, response);
                }
                break;
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        //A post HTTP method is not required for our app
        doGet(request, response);
    }

}



