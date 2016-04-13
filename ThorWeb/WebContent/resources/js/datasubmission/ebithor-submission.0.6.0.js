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
	//values are BASIC, AUTOCOMPLETE, CUSTOM
	thorApplicationNamespace.thorIntegrationType = "";
	thorApplicationNamespace.thorServer = "http://localhost:8080";
	//EBI-THOR Project WebService Endpoint
	thorApplicationNamespace.thorUrl = 			thorApplicationNamespace.thorServer + "/ThorWeb/api/orcid/find/";
	//EBI-THOR Project WebService to get the login url
	thorApplicationNamespace.thorUrlLogin = 	thorApplicationNamespace.thorServer + "/ThorWeb/dataclaiming/loginUrl/";
	//EBI-THOR Project WebService to get the login url
	thorApplicationNamespace.thorOrcIdBio = 	thorApplicationNamespace.thorServer + "/ThorWeb/dataclaiming/orcIdBio/";
	
	//Reference to HTML objects related to Thor Basic Integration
	thorApplicationNamespace.thorTxtSearch =		$('.thorKeyword');
	thorApplicationNamespace.thorthorDivResult =$('.thorResult');
	thorApplicationNamespace.thorLoginOrcId = $('.thorLoginOrcId');
	thorApplicationNamespace.thorLoadingImage = $('.thorLoadingImage');
	//Text fields to autopopulate afer the search of user details at orcide
	thorApplicationNamespace.thorGivenNameTxt = $('.thorGivenNameTxt');
	thorApplicationNamespace.thorFamilyNameTxt = $('.thorFamilyNameTxt');
	thorApplicationNamespace.thorOrcIdSpan = $('.thorOrcIdSpan');
	thorApplicationNamespace.thorEmailTxt = $('.thorEmailTxt');
	thorApplicationNamespace.thorCountryTxt = $('.thorCountryTxt');
	//Elements to be displayed while User is not logged in orcid
	thorApplicationNamespace.thorOrcIdNotLogged = $('.thorOrcIdNotLogged');
	//Elements to be displayed after User is logged in orcid
	thorApplicationNamespace.thorOrcIdLogged = $('.thorOrcIdLogged');
	
	
	//JSON whole response
	thorApplicationNamespace.thorJSONResult = 	'';
	//JSON arrays of orcid-search-result
	thorApplicationNamespace.thorProfilesArray = [];
	//OnClick Function for the Thor Basic Integration Search Button
	
	//Identify the type of integration being used by client
	if (thorApplicationNamespace.thorLoginOrcId.length != 0) {
		//Integration with login thru OrcId website
		thorApplicationNamespace.thorIntegrationType="LOGINORCID";
		thorApplicationNamespace.loadLoginUrl();
		
	}
	else if (thorApplicationNamespace.thorthorDivResult.length == 0) {
		thorApplicationNamespace.thorIntegrationType="AUTOCOMPLETE";
		thorApplicationNamespace.addAutoComplete();
	}
	else {
		thorApplicationNamespace.thorIntegrationType = "BASIC";
		/**
		 * Onclick implemenBasic Integration Search Button onClick
		 * Passes the keyword and callback function to display the results
		 */
		thorApplicationNamespace.fnBasicSearchClick = function(event){
			thorApplicationNamespace.findAllRecordsOrcIds(thorApplicationNamespace.thorTxtSearch, thorApplicationNamespace.displayBasicResults);
		}
		//Add onClick funciont to search button
		thorApplicationNamespace.setBasicSearchClick();
	}
}

thorApplicationNamespace.getServerNamePort = function() {
	var url = window.location.href
	var arr = url.split("/");
	var result = arr[0] + "//" + arr[2]
	return result;
}

thorApplicationNamespace.buildUrl = function(base, key, value) {
    var sep = (base.indexOf('?') > -1) ? '&' : '?';
    return base + sep + key + '=' + value;
}

thorApplicationNamespace.addAutoComplete = function addAutoComplete() {
	thorApplicationNamespace.thorTxtSearch.autocomplete({
	    source: function (request, response) {
	    	thorApplicationNamespace.findAllRecordsOrcIds(thorApplicationNamespace.thorTxtSearch, function() {
	        	var arrayesultado = [];
	        	$.each(thorApplicationNamespace.thorProfilesArray, function( index, value ) {
	        		var tmpxx = thorApplicationNamespace.createBasicResult(thorApplicationNamespace.thorProfilesArray[index]['orcid-profile']);
	        		arrayesultado.push(tmpxx)
	        		});
	        		response($.map(arrayesultado, function (value, key) {
	        			return {
	                    label: value[1],
	                    value: value[0]
	                };		    		
	        		 }));
	    	});
	    },
	    minLength: 4,
	    delay: 500
	});
}

/**
 * Basic Integration: Defines the event to be called when click the search button 
 * for objects marked as class 'thorSearchButton'
 */
thorApplicationNamespace.setBasicSearchClick = function setBasicSearchClick() {
	$('.thorSearchButton').on('click', thorApplicationNamespace.fnBasicSearchClick);	
}

thorApplicationNamespace.displayBasicResults = function displayBasicResults(event) {
	thorApplicationNamespace.thorthorDivResult.text("");
	$.each(thorApplicationNamespace.thorProfilesArray, function( index, value ) {
		var tmpOutput =  thorApplicationNamespace.createBasicResult(thorApplicationNamespace.thorProfilesArray[index]['orcid-profile']);
		thorApplicationNamespace.addToDropDown(tmpOutput[0], tmpOutput[1]);
		});
}

thorApplicationNamespace.addToDropDown = function addToDropDown(optValue, optText) {
	thorApplicationNamespace.thorthorDivResult.append($("<option />").val(optValue).text(optText));
}

thorApplicationNamespace.createBasicResult = function createBasicResult(profile) {
	var orcId = "";
	var outLabel = "";
	var completeName = "";
	if (profile['orcid-identifier'] != null) {
		orcId=	profile['orcid-identifier'].path
	}
	
	if (profile['orcid-bio'] != null && profile['orcid-bio']['personal-details'] != null) {
		var givenName = thorApplicationNamespace.retrieveValue(profile['orcid-bio']['personal-details']['given-names']);
		var familyName = thorApplicationNamespace.retrieveValue(profile['orcid-bio']['personal-details']['family-name']);
		if ($.trim(givenName) != "" || $.trim(familyName) != "") {
			completeName = " (" + givenName + " " + familyName + ")";
		}
	}
	
	outLabel = orcId;
	if (completeName != "" ) {
		outLabel = outLabel + completeName;
	}

	return [orcId, outLabel];
}

thorApplicationNamespace.retrieveValue = function retrieveValue(element) {
	if (element != null) {
	    var str = element.value;
	}
	else {
	    var str = "";
	}
	return str;
}

thorApplicationNamespace.findAllRecordsOrcIds = function findAllRecordsOrcIds(keywordFields, callback) {
	thorApplicationNamespace.showLoadingImage();
	var keyword = thorApplicationNamespace.retrieveKeyword(keywordFields);
	var searchUrl =  thorApplicationNamespace.thorUrl + keyword;
	
	thorApplicationNamespace.callWS(
		searchUrl, 
		function(data) {
			var thorJSONResult = jQuery.parseJSON( JSON.stringify(data) );
    		thorApplicationNamespace.thorProfilesArray = thorJSONResult['orcid-search-results']['orcid-search-result'];
    		callback();
    	},
    	function() {
	    	thorApplicationNamespace.thorLoadingImage.hide();
	    },
    	'',
    	'GET');
	
	/*
	$.ajax({
	        url: searchUrl,
	        success: function(data) {
		    	var thorJSONResult = jQuery.parseJSON( data );
		    	thorApplicationNamespace.thorProfilesArray = thorJSONResult['orcid-search-results']['orcid-search-result'];
		    	callback();
		    },
		    complete:  function(data) {
		    	thorApplicationNamespace.thorLoadingImage.hide();
		    }
	    });
	    */
}

thorApplicationNamespace.retrieveKeyword = function retrieveKeyword(keywordFields) {
	var keyword = "";
	if (keywordFields != null) {
		$.each(keywordFields, function(index, value){
			if (keyword != "") {
				keyword += " "; //add spaces between keywords 
			}
			keyword = keyword + value.value;
		});
	}
	return keyword;
}


thorApplicationNamespace.showLoadingImage = function showLoadingImage() {
	if (thorApplicationNamespace.thorLoadingImage != null) {
		thorApplicationNamespace.thorLoadingImage.show();
	}
}

thorApplicationNamespace.hideLoadingImage = function hideLoadingImage() {
	if (thorApplicationNamespace.thorLoadingImage != null) {
		thorApplicationNamespace.thorLoadingImage.hide();
	}
}

/**
 * Function to call a Rest Web Service at the endpoint
 * and invoke the callback function to process the results
 * passing the json string returned from WS as function parameter.
 */
thorApplicationNamespace.callWS = function(endpoint, onSuccess, onComplete, myData, sendType) {
	var serviceUrl = endpoint;
	$.ajax({
		url : serviceUrl,
		crossDomain: true,
		xhrFields: { withCredentials:true },
		type: sendType,
		data: JSON.stringify(myData),
		dataType: 'json',
		success : function(data) {
			onSuccess(data);
		},
	    complete : function(data) {
	    	if (onComplete != null) {
	    		onComplete(data);
	    	}
		}
	});
}

thorApplicationNamespace.loadLoginUrl = function() {
	thorApplicationNamespace.callWS(
		thorApplicationNamespace.thorUrlLogin,
		null,
		function(data) {
			var url = data.responseText;
	        //Add client server name + port to login URL so the javascript from server can communicate via postMessage 
	        //with the javascript from client.
	        if (url!="") {
	        	url = thorApplicationNamespace.buildUrl(url, "clientAddress", thorApplicationNamespace.getServerNamePort())
	        }
			//Load LOGIN LINK Url
			thorApplicationNamespace.thorLoginOrcId.unbind( "click" );
			thorApplicationNamespace.thorLoginOrcId.click(function(e){
		        e.preventDefault(); // this will prevent the browser to redirect to the href
		        // if js is disabled nothing should change and the link will work normally
		        if (url!="") {
		        	var windowName = $(this).attr('id');
		        	window.open(url, windowName, "height=900,width=800");
		        }
		    });
		},
		'','GET');
}

thorApplicationNamespace.searchOrcIdBio = function(callback) {
	thorApplicationNamespace.callWS(
		thorApplicationNamespace.thorOrcIdBio,
		function(data) {
			//If callback function is informed, then invoke it
			if (callback != null) {
				callback(JSON.stringify(data))
			}
			else {
				//if no callback function is passed, 
				//we will automatically populate the
				//returned values to the fields.
				thorApplicationNamespace.populateOrcIdBiofields(JSON.stringify(data));
			}
			
		},
		null,
		'','GET');
}

thorApplicationNamespace.populateOrcIdBiofields = function(dataStr) {
	var data = jQuery.parseJSON(dataStr);
	
	if (thorApplicationNamespace.thorGivenNameTxt != null) {
		try {
			thorApplicationNamespace.thorGivenNameTxt.val(data['orcid-profile']['orcid-bio']['personal-details']['given-names']['value'])
		}
		catch(err) {
		}
	}
	if (thorApplicationNamespace.thorFamilyNameTxt != null) {
		try {
			thorApplicationNamespace.thorFamilyNameTxt.val(data['orcid-profile']['orcid-bio']['personal-details']['family-name']['value'])
		}
		catch(err) {
		}
	}
	if (thorApplicationNamespace.thorOrcIdSpan != null) {
		try {
			//!!!!!trocar esse label por span talvez...e colocar um campo para eu setar o orcid que aceito .val nele
			thorApplicationNamespace.thorOrcIdSpan.text(data['orcid-profile']['orcid-identifier']['path'])
			thorApplicationNamespace.thorOrcIdNotLogged.hide();
			thorApplicationNamespace.thorOrcIdLogged.show();
		}
		catch(err) {
		}
	}
	if (thorApplicationNamespace.thorEmailTxt != null) {
		try {
			thorApplicationNamespace.thorEmailTxt.val(data['orcid-profile']['orcid-bio']['contact-details']['email'][0]['value'])
		}
		catch(err) {
		}
	}
	if (thorApplicationNamespace.thorCountryTxt != null) {
		try {
			thorApplicationNamespace.thorCountryTxt.val(data['orcid-profile']['orcid-bio']['contact-details']['address']['country']['value'])
		}
		catch(err) {
		}
	}
}
