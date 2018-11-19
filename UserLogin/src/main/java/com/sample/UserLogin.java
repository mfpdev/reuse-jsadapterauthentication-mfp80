/**
* Copyright 2015, 2018 IBM Corp.
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
*/
package com.sample;

import com.ibm.mfp.security.checks.base.UserAuthenticationSecurityCheck;
import com.ibm.mfp.server.registration.external.model.AuthenticatedUser;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.SAXException;

public class UserLogin extends UserAuthenticationSecurityCheck {
    private String userId, displayName;
    private String errorMsg;
    private boolean rememberMe = false;
    private transient CloseableHttpClient client;
    private transient HttpHost host;
    private transient HttpResponse jsAdapterResponse;
    private transient HttpEntity httpEntity;

    @Override
    protected AuthenticatedUser createUser() {

      Map<String, Object> attrMap = new HashMap<String,Object>();
      attrMap.put("hello","world");

      return new AuthenticatedUser(userId, displayName, this.getName(),attrMap);
    }

    @Override
    protected boolean validateCredentials(Map<String, Object> credentials) {
        if(credentials!=null && credentials.containsKey("username") && credentials.containsKey("password")){

            String username = credentials.get("username").toString();
            String password = credentials.get("password").toString();
            if(!username.isEmpty() && !password.isEmpty()) {

                //Optional RememberMe
                if(credentials.containsKey("rememberMe") ){
                    rememberMe = Boolean.valueOf(credentials.get("rememberMe").toString());
                }
                errorMsg = null;

                try{
                    String respfromJSAdapter = validateCredentialswithJSAdapter(username,password);

                    if(respfromJSAdapter!=null){

                        org.json.JSONObject jsonObj = new org.json.JSONObject(respfromJSAdapter);
                        if(jsonObj.get("authStatus").equals("complete")){
                            userId = username.toUpperCase();
                            displayName = username;
                        }else{
                            return false;
                        }

                    }

                }catch(Exception e){
                  System.out.println(e.toString());
                }

                return true;
            }
            else {
                errorMsg = "Wrong Credentials";
            }
        }
        else{
            errorMsg = "Credentials not set properly";
        }
        return false;
    }

    @Override
    protected Map<String, Object> createChallenge() {
        Map<String, Object> challenge = new HashMap<String, Object>();
        challenge.put("errorMsg",errorMsg);
        challenge.put("remainingAttempts",getRemainingAttempts());
        return challenge;
    }

    @Override
    protected boolean rememberCreatedUser() {
        return rememberMe;
    }

    public String validateCredentialswithJSAdapter(String username, String password) throws IOException, IllegalStateException, SAXException, URISyntaxException {

        HttpGet jsAdapterGet = new HttpGet("/mfp/api/adapters/httpAdapter/unprotected");
        URI uri = new URIBuilder(jsAdapterGet.getURI()).addParameter("params", "['" + username + "','" + password + "']").build();

        jsAdapterGet.setURI(uri);

        return execute(jsAdapterGet);
    }

    public String execute(HttpUriRequest req) throws IOException, IllegalStateException, SAXException {

      	client = HttpClientBuilder.create().build();
      	host = new HttpHost("localhost", 9080, "http");

  		jsAdapterResponse = client.execute(host, req);
  		httpEntity = jsAdapterResponse.getEntity();
        return httpEntity != null ? EntityUtils.toString(httpEntity) : null;

  	}

}
