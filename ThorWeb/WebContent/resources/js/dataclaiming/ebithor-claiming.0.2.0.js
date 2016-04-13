/**
 * includes autocomplete while typing
 */
var thorApplicationNamespace = {};

$(document).ready(function() {
	thorApplicationNamespace.defVariables();

});

/**
 * Declare the variables used by THOR ORCID search
 */
thorApplicationNamespace.defVariables = function defVariables() {
	// EBI-THOR Project WebService Information
	thorApplicationNamespace.Server = "http://localhost:8080/ThorWeb";
//	thorApplicationNamespace.wsIsSignedInUrl = thorApplicationNamespace.Server
//			+ "/api/claiming/orcid/issignedin";
	thorApplicationNamespace.wsClaimingUrl = thorApplicationNamespace.Server
			+ "/dataclaiming/claiming";
}

//thorApplicationNamespace.isUserSignedIn = function(callback) {
//	var serviceUrl = thorApplicationNamespace.wsIsSignedInUrl;
//	thorApplicationNamespace.callWS(serviceUrl, callback);
//}


/**
 * Function to create a javascript object with parameters that identity the current dataset being visualized.
 */
thorApplicationNamespace.createWorkOrcId = function(title, workType, publicationYear, externalIdType, externalId, url) {
	var orcIdWork = {
		    title:title,
		    workType:workType,
		    pubYear:publicationYear,
		    externalIdType:externalIdType,
		    externalId:externalId,
		    url:url
		};
	
	return orcIdWork;
}

/**
 * Loads 
 */
thorApplicationNamespace.claimingInfo = function(callback, orcIdWork) {
	var serviceUrl = thorApplicationNamespace.wsClaimingUrl;
	//thorApplicationNamespace.sendGetWS(serviceUrl, callback);
	thorApplicationNamespace.sendGetWS(serviceUrl, callback, orcIdWork);
}

thorApplicationNamespace.isSignedIn = function(data) {
	return data['isUserLoggedIn'];
}

thorApplicationNamespace.isDataClaimed = function(data) {
	return data['isDataClaimed'];
}

thorApplicationNamespace.getLoginUrl = function(data) {
	var url = data['loginUrl'];
 	//adds remember me checked to loginlink
	if($('.rememberMe').is(':checked')) {
		url = thorApplicationNamespace.buildUrl(url, "remind", "true")
	}
	return url;
}

thorApplicationNamespace.getLogoutUrl = function(data) {
	return data['logoutUrl'];
}

thorApplicationNamespace.getUserName = function(data) {
	if (data['orcIdRecord'] != null) {
		return data['orcIdRecord']['name'];
	}
}

/**
 * Function to call a Rest Web Service at the endpoint
 * and invoke the callback function to process the results
 * passing the json string returned from WS as function parameter.
 */
thorApplicationNamespace.callWS = function(endpoint, callback, myData, sendType) {
	var serviceUrl = endpoint;
	$.ajax({
		url : serviceUrl,
		type: sendType,
		data: JSON.stringify(myData),
		dataType: 'json',
		success : function(data) {
			callback(data);
		}
	});
}

/**
 * Function to call a Rest Web Service at the endpoint
 * and invoke the callback function to process the results
 * passing the json string returned from WS as function parameter.
 */
thorApplicationNamespace.sendGetWS = function(endpoint, callback, myData) {
	var jsonStr = JSON.stringify(myData);
	var serviceUrl = thorApplicationNamespace.buildUrl(endpoint, "ordIdWorkJson", jsonStr);
	thorApplicationNamespace.callWS(serviceUrl, callback, '', 'GET');
}

thorApplicationNamespace.buildUrl = function(base, key, value) {
    var sep = (base.indexOf('?') > -1) ? '&' : '?';
    return base + sep + key + '=' + value;
}
// //Reference to HTML objects related to Thor Basic Integration
// thorApplicationNamespace.thorTxtSearch = $('.thorKeyword');
// thorApplicationNamespace.thorthorDivResult =$('.thorResult');
// //JSON whole response
// thorApplicationNamespace.thorJSONResult = '';
// //JSON arrays of orcid-search-result
// thorApplicationNamespace.thorProfilesArray = [];
// //OnClick Function for the Thor Basic Integration Search Button
//	

// if (thorApplicationNamespace.thorthorDivResult.length == 0) {
// thorApplicationNamespace.thorIntegrationType="AUTOCOMPLETE";
// thorApplicationNamespace.addAutoComplete();
// }
// else {
// thorApplicationNamespace.thorIntegrationType = "BASIC";
// /**
// * Onclick implemenBasic Integration Search Button onClick
// * Passes the keyword and callback function to display the results
// */
// thorApplicationNamespace.fnBasicSearchClick = function(event){
// thorApplicationNamespace.findAllRecordsOrcIds(thorApplicationNamespace.thorTxtSearch.val(),
// thorApplicationNamespace.displayBasicResults);
// }
// //Add onClick funciont to search button
// thorApplicationNamespace.setBasicSearchClick();
// }

// thorApplicationNamespace.addAutoComplete = function addAutoComplete() {
// thorApplicationNamespace.thorTxtSearch.autocomplete({
// source: function (request, response) {
// thorApplicationNamespace.findAllRecordsOrcIds(thorApplicationNamespace.thorTxtSearch.val(),
// function() {
// var arrayesultado = [];
// $.each(thorApplicationNamespace.thorProfilesArray, function( index, value ) {
// var tmpxx =
// thorApplicationNamespace.createBasicResult(thorApplicationNamespace.thorProfilesArray[index]['orcid-profile']);
// arrayesultado.push(tmpxx)
// });
// response($.map(arrayesultado, function (value, key) {
// return {
// label: value[1],
// value: value[0]
// };
// }));
// });
// },
// minLength: 4,
// delay: 500
// });
// }
//
// /**
// * Basic Integration: Defines the event to be called when click the search
// button
// * for objects marked as class 'thorSearchButton'
// */
// thorApplicationNamespace.setBasicSearchClick = function setBasicSearchClick()
// {
// $('.thorSearchButton').on('click',
// thorApplicationNamespace.fnBasicSearchClick);
// }
//
// thorApplicationNamespace.displayBasicResults = function
// displayBasicResults(event) {
// thorApplicationNamespace.thorthorDivResult.text("");
// $.each(thorApplicationNamespace.thorProfilesArray, function( index, value ) {
// var tmpOutput =
// thorApplicationNamespace.createBasicResult(thorApplicationNamespace.thorProfilesArray[index]['orcid-profile']);
// thorApplicationNamespace.addToDropDown(tmpOutput[0], tmpOutput[1]);
// });
// }
//
// thorApplicationNamespace.addToDropDown = function addToDropDown(optValue,
// optText) {
// thorApplicationNamespace.thorthorDivResult.append($("<option
// />").val(optValue).text(optText));
// }
//
// thorApplicationNamespace.createBasicResult = function
// createBasicResult(profile) {
// var orcId = profile['orcid-identifier'].path
// var givenName =
// thorApplicationNamespace.retrieveValue(profile['orcid-bio']['personal-details']['given-names']);
// var familyName =
// thorApplicationNamespace.retrieveValue(profile['orcid-bio']['personal-details']['family-name']);
// var outLabel = orcId + " (" + givenName + " " + familyName + ")";
// return [orcId, outLabel];
// }
//
// thorApplicationNamespace.retrieveValue = function retrieveValue(element) {
// if (element != null) {
// var str = element.value;
// }
// else {
// var str = "";
// }
// return str;
// }
//
// thorApplicationNamespace.findAllRecordsOrcIds = function
// findAllRecordsOrcIds(keyword, callback) {
// var searchUrl = thorApplicationNamespace.thorUrl + keyword;
// $.ajax({
// url: searchUrl,
// success: function(data) {
// var thorJSONResult = jQuery.parseJSON( data );
// thorApplicationNamespace.thorProfilesArray =
// thorJSONResult['orcid-search-results']['orcid-search-result'];
// callback();
// }
// });
// }
//
//
