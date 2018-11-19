/*
 *  Licensed Materials - Property of IBM
 *  5725-I43 (C) Copyright 2015, 2018 IBM Corp. All Rights Reserved.
 *  US Government Users Restricted Rights - Use, duplication or
 *  disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

/**
 * @param tag: a topic such as MobileFirst_Platform, Bluemix, Cordova.
 * @returns json list of items.
 */

function getFeed(tag) {
	var input = {
	    method : 'get',
	    returnedContentType : 'xml',
	    path : getPath(tag)
	};

	return MFP.Server.invokeHttp(input);
}

/**
 * Helper function to build the URL path.
 */
function getPath(tag){
    if(tag === undefined || tag === ''){
        return 'feed.xml';
    } else {
        return 'blog/atom/' + tag + '.xml';
    }
}

/**
 * @returns ok
 */
function unprotected(username,password) {

	if (username==password){

		return {
			authStatus: "complete"
		};
	}

	return onAuthRequired(null, "Invalid login credentials");

}

function onAuthRequired(headers, errorMessage){
	errorMessage = errorMessage ? errorMessage : null;

	return {
		authStatus: "credentialsRequired",
		errorMessage: errorMessage
	};
}
