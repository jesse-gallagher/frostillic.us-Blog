/*!COPYRIGHT HEADER! 
 *
 * (c) Copyright Darwino Inc. 2014-2016.
 *
 * Licensed under The MIT License (https://opensource.org/licenses/MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

(function() {
	
var jstore = darwino.jstore;
var services = darwino.services;

var app_baseUrl = "$darwino-app";
var jstore_baseUrl = "$darwino-jstore";
var social_baseUrl = "$darwino-social";

var session = jstore.createRemoteApplication(jstore_baseUrl).createSession();
var userService = services.createUserService(social_baseUrl+"/users");

// Enable some logging
var LOG_GROUP = "frostillicus_blog.web";
darwino.log.enable(LOG_GROUP,darwino.log.DEBUG)

// Main database and store names
var DATABASE_NAME = "frostillicus_blog";
var STORE_NAME = "_default";

// Enable this flag if your application uses instances
var INSTANCE_PROP = "dwo.Contacts.instance";

var DEFAULT_STATE_URL = "/app/views/bydate";

angular.module('app', ['ngSanitize','ionic', 'darwino.ionic', 'darwino.angular.jstore', 'ngQuill' ])

.run(['$rootScope','$location','$state','$http','$window','$timeout','views',function($rootScope,$location,$state,$http,$window,$timeout,views) {
	// Storage utilities
	function storage() {
		try {
			return 'localStorage' in window && window['localStorage'] !== null ? window.localStorage : null;
		} catch(e){
			return null;
		}
	}	
	function arrayIndexOf(a, obj) {
	    var i = a.length;
	    while (i--) {
	       if (a[i] === obj) {
	           return i;
	       }
	    }
	    return -1;
	}
	var storage = storage(); 
	
	// Make the data models globally available so the navigator can access them
	// Could also be provided as a service to avoid the pollution of $rooScope
	// Use an object because of prototypical inheritance
	// http://stackoverflow.com/questions/15305900/angularjs-ng-model-input-type-number-to-rootscope-not-updating
	$rootScope.data = {
		jsonQuery: false,
		// Global information maintained
		useInstances: false,
		instances: [],
		instance: "",
	}
	
	
	// Some options
	$rootScope.infiniteScroll = true;
	$rootScope.accessUserService = true;
	
	// Make some global variables visible
	$rootScope.darwino = darwino;
	$rootScope.session = session;
	$rootScope.userService = userService;

	
	// Should this me moved to an initialization service?
	$rootScope.hasInstances = function() {
		return $rootScope.data.useInstances && $rootScope.data.instances.length>1;  
	}
	$rootScope.instanceChanged = function() {
		if($rootScope.data.useInstances) {
			var inst = $rootScope.data.instance;
			if(storage) {
				storage.setItem(INSTANCE_PROP,inst);
			}
			$rootScope.reset();
	        $state.go("app.views",{view:'bydate'});
		}
	}

	$rootScope.reset = function() {
		var inst = null;
		if($rootScope.data.useInstances) {
			inst = $rootScope.data.instance;
		}
		views.reset();
		$rootScope.database = null;
		$rootScope.nsfdata = null;
		$rootScope.dbPromise = session.getDatabase(DATABASE_NAME,inst);
		$rootScope.dbPromise.then(function(database) {
			$rootScope.database = database;
			$rootScope.nsfdata = database.getStore(STORE_NAME);
			$rootScope.apply();
		});
	}

	$rootScope.isDualPane = function() {
		if($window.matchMedia("(min-width:768px)").matches) {
			return true;
		}
		return false
	}
	

	//
	// Global user related functions
	//
	$rootScope.isAnonymous = function() {
		var u = userService.getCurrentUser();
		return !u || u.isAnonymous();
	};
	$rootScope.getUser = function(id) {
		if(id && $rootScope.accessUserService) {
			// Ensure that we get the load notification only once (the users has not been requested yet)
			// Not that the cache can be filled from a multiple user requests as well
			var u = userService.getUserFromCache(id);
			if(u) {
				return u;
			}
			var u = userService.getUser(id,function(u,loaded) {
				$rootScope.apply(); 
			});
			return u || darwino.services.User.ANONYMOUS_USER;
		}
		return userService.createUser(id);
	};
	$rootScope.getPhoto = function(id) {
		if(id && $rootScope.accessUserService) {  
			return userService.getUserPhotoUrl(id);
		}
		return darwino.services.User.ANONYMOUS_PHOTO;
	};
	
	//
	// State change helpers
	//
	$rootScope.go = function(path,monoPaneOnly) {
		if(monoPaneOnly) {
			if($rootScope.isDualPane()) {
				return;
			}
		}
		$state.go(path)
	};
	$rootScope.displayUser = function(dn) {
        $state.go('app.user',{userdn:dn});
    }	
	$rootScope.isState = function(state,params) {
        if($state.includes(state)) {
        	if(params) {
        		for(var p in params) {
        			if($state.params[p]!=params[p]) {
        				return false;
        			}
        		}
        	}
        	return true;
        }
        return false;
    }	
	
	//
	// Apply the changes later, when it comes idle
	// Make sure that this is only executed once
	//
	var pendingApply = null;
	$rootScope.apply = function() {
		if(!pendingApply) {
			pendingApply = $timeout(function(){pendingApply=null});
		}
	};
	

	$rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams){
		if(toState.name=="app.read" || toState.name=="app.edit") {
			if(!$rootScope.entries) {
				event.preventDefault();
			}
		}
	})	
	$rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
		if(toState.name=="app.views") {
			var view = toParams.view;
			views.selectEntries(view);
		} else if(toState.name=="app.author") {		
			var author = toParams.author;
			views.selectEntries('author',{author:author});
		}
	})

	//
	// Darwino hybrid notification
	// Register for a change in settings so the page gets repaint. This is how, for example,
	// the refresh icon is displayed
	darwino.hybrid.addSettingsListener(function(){
		$rootScope.apply();
	});
	
	//
	// Initialization
	//
	$http.get(app_baseUrl+"/properties").then(function(response) {
		var properties = response.data;
		$rootScope.data.jsonQuery = properties.jsonQuery;
		$rootScope.data.useInstances = properties.useInstances;
		$rootScope.data.instances = properties.instances;
		
		if($rootScope.data.useInstances) {
			var instances = properties.instances;
			if(instances && instances.length>0) {
				var inst = storage ? storage.getItem(INSTANCE_PROP) : null;
				if(!inst || arrayIndexOf(instances,inst)<0) {
					inst = instances[0];
				}
				$rootScope.data.instance = inst;
				$rootScope.instanceChanged();
				return;
			}
		}
		$rootScope.reset();
	})
}])

//
// Basic date formatter using moment JS
//
.filter('formattedDate', function() {
	return function(d) {
		return d ? moment(d).fromNow() : '';
	}
})

//
// State provider
//
.config(['$stateProvider', '$urlRouterProvider', '$ionicConfigProvider', function($stateProvider, $urlRouterProvider, $ionicConfigProvider) {
	// Abstract state used to provide the main layout
	$stateProvider.state('app', {
		url : "/app",
		abstract : true,
		templateUrl : "templates/leftmenu.html"
	// Home page describing the application
	}).state('app.home', {
		url : "/home",
		views : {
			'menuContent' : {
				templateUrl : "templates/home.html"
			}
		}
	// Views - queries.
	}).state('app.views', {
		url : "/views/:view",
		views : {
			'menuContent' : {
				templateUrl : "templates/mainview.html",
				controller : "ViewsCtrl",
			}
		},
		resolve:{
			view: ['$stateParams', function($stateParams){
				return $stateParams.view;
		    }]
		}	
	// View categorized by authors
	}).state('app.author', {
		url : "/author/:author",
		views : {
			'menuContent' : {
				templateUrl : "templates/mainview.html",
				controller : "DocsAuthor",
			}
		}	
	// Document read state, used in mono pane UI only
	}).state('app.read', {
		url : "/read",
		views : {
			'menuContent@app' : {
				templateUrl : "templates/readitem.html",
				controller : "ReadCtrl",
			}
		}
	// Document edit (new or existing)
	// The id can be of the for id:xxx for an existing document, pid:xxx for a new document belonging to a parent 
	}).state('app.edit', {
		url : "/edit/:id",
		views : {
			'menuContent@app' : {
				templateUrl : "templates/edititem.html",
				controller : "EditCtrl"
			}
		}
	// Display user related information
	}).state('app.user', {
		url : "/user/:userdn",
		views : {
			'menuContent@app' : {
				templateUrl : "templates/user.html",
				controller : "UserCtrl"
			}
		}
	// About
	}).state('app.about', {
		url : "/about",
		views : {
			'menuContent' : {
				templateUrl : "templates/about.html"
			}
		}
	});

	$urlRouterProvider.otherwise(DEFAULT_STATE_URL);
}])


//
// Service to access the documents
// Each collection is here called a 'view' and is identified by its name
//
.service('views', ['$rootScope','$state','$jstore','$ionicPopup',function($rootScope,$state,$jstore,$ionicPopup) {
	// We cache the entries share the list in memory
	// We a
	var allEntries = {};
	this.reset = function() {
		for(var k in allEntries) {
			var e = allEntries[k];
			e.setInstance($rootScope.data.instance);
			e.resetCursor();
		}
	}
	this.selectEntries = function(view,params) {
		return $rootScope.entries = this.getEntries(view,params);
	}
	this.getEntries = function(view,params) {
		// Look for the entries in the cache
		// For parameterized views, we only keep one copy in the cache
		var v = allEntries[view]
		if(v) {
			if(!params || angular.equals(params,v.params)) {
				return v;
			}
		}
		return allEntries[view] = this.createEntries(view,params);
	}
	this.createEntries = function(view,params) {
		var entries = $jstore.createItemList(session)
		entries.view = view;
		entries.params = params;

		var authField = '_cuser'; 
		var p;
		if(view=='bydate') {
			p = {
				database: DATABASE_NAME,
				store: STORE_NAME,
				orderBy: "_cdate desc",
				jsonTree: true,
				hierarchical: 99,
				options: jstore.Cursor.RANGE_ROOT+jstore.Cursor.DATA_MODDATES+jstore.Cursor.DATA_READMARK+jstore.Cursor.DATA_WRITEACC
			};
		} else if(view=='byauthor') {
			p = {
				database: DATABASE_NAME,
				store: STORE_NAME,
				orderBy: authField+", _cdate desc",
				categoryCount: 1,
				aggregate: "{ Count: {$count: '$'} }",
				options: jstore.Cursor.RANGE_ROOT+jstore.Cursor.DATA_MODDATES+jstore.Cursor.DATA_CATONLY+jstore.Cursor.DATA_WRITEACC
			};
		} else if(view=='author') {
			var ap = params.author ? ('\"'+params.author+'\"') : "null"
			p = {
				database: DATABASE_NAME,
				store: STORE_NAME,
				orderBy: authField+", _cdate desc",
				query: "{'"+authField+"':"+ap+"}",
				parentId: '*',
				jsonTree: true,
				hierarchical: 99,
				options: jstore.Cursor.RANGE_ROOT+jstore.Cursor.DATA_MODDATES+jstore.Cursor.DATA_READMARK+jstore.Cursor.DATA_WRITEACC
			};
		} else {
			// Unknown view...
			return;
		}
		entries.initCursor(p);
		
		if($rootScope.data.useInstances) {
			entries.setInstance($rootScope.data.instance);
		}
		
		// Specific methods
		entries.getUserDn = function(item) {
			if(item) {
				var a = item.cuser;
				if(darwino.Utils.isArray(a)) {
					return a.length==1 ? a[0] : null;
				}
				return a;
			}
			return null;
		}
		entries.getUser = function(item) {
			if(item) {
				return $rootScope.getUser(this.getUserDn(item));
			}
			return darwino.services.User.ANONYMOUS_USER;
		}
		entries.getPhoto =  function(item) {
			if(item) {
				return $rootScope.getPhoto(this.getUserDn(item));
			}
			return darwino.services.User.ANONYMOUS_PHOTO;
		}
		entries.isFtEnabled = function() {
			return $rootScope.nsfdata && $rootScope.nsfdata.isFtSearchEnabled();
		}
		
		entries.canCreateDocument = function() {
			var db = $rootScope.database;
			return db && db.canCreateDocument();
		}
		entries.canUpdateDocument = function(item) {
			var db = $rootScope.database;
			var item = item || entries.detailItem;
			return db && db.canUpdateDocument() && item && !item.readOnly;
		}
		entries.canDeleteDocument = function(item) {
			var db = $rootScope.database;
			var item = item || entries.detailItem;
			return db && db.canDeleteDocument() && item && !item.readOnly;
		}
		
		entries.isCategory = function(item) {
			var item = item || entries.detailItem;
			if(!item) return;
			return item.category==true;
		}
		entries.getFormattedJson = function(item) {
			return item ? darwino.Utils.toJson(item.value,false) : null;
		}
		entries.newEntry = function() {
			$state.go("app.edit",{view:entries.view});
		}
		entries.readEntry = function(item) {
			var item = item || entries.detailItem;
			if(!item) return;
			if(item.category) {
				var dn = item.key;
				$state.go('app.author',{author:dn});
			} else {
				$rootScope.go("app.read",true);
			}
		}
		entries.editEntry = function(item) {
			var item = item || entries.detailItem;
			if(!item) return;
			$state.go("app.edit",{view:entries.view,id:"id:"+item.unid});
		}
		entries.newResponse = function(item) {
			var item = item || entries.detailItem;
			if(!item) return;
			$state.go("app.edit",{view:entries.view,id:"pid:"+item.unid});
		}
		entries.deleteEntry = function(item) {
			var item = item || entries.detailItem;
			if(!item) return;
			$ionicPopup.confirm({
				title: 'Delete entry',
				template: 'Are you sure you want to delete this document?'
			}).then(function(res) {
				if(res) {
					entries.deleteItem(item);
				}
			});
		}
		
		entries.hasMoreButton = function() {
			return !$rootScope.infiniteScroll && this.hasMore() && !this.isLoading();
		}

		entries.onItemsLoaded = function(items) {
			var userDns =  [];
			// Act on each item
			for(var i=0; i<items.length; i++) {
				var item = items[i];
				// Sort the children by date
				if(item.children) {
					item.children.sort(function(i1,i2) {
						return i2.cdate-i1.cdate;
					})
				}
				if($rootScope.accessUserService) {
					// Get the DN for the document or the category
					var dn = null;
					if(item.category) {
						if(this.view=="byauthor") {
							dn = item.key;
						}
					} else {
						dn = this.getUserDn(item);
					}
					if(dn && !userService.getUserFromCache(dn)) {
						userDns.push(dn);
					}
				}
			}
			// Get all the missing users at once from the server
			if(userDns.length) {
				userService.preloadUsers(userDns,false,function() {
					$rootScope.apply();
				});
			}
		}
		$rootScope.getUser = function(id) {
			if(id && $rootScope.accessUserService) {
				// Ensure that we get the load notification only once (the users has not been requested yet)
				// Not that the cache can be filled from a multiple user requests as well
				var u = userService.getUserFromCache(id);
				if(u) {
					return u;
				}
				var u = userService.getUser(id,function(u,loaded) {
					$rootScope.apply();
				});
				return u || darwino.services.User.ANONYMOUS_USER;
			}
			return userService.createUser(id);
		};
		
		var oldLoadMore = entries.loadMore; 
		entries.loadMore = function() {
			function broadcast() {
				$rootScope.$broadcast('scroll.infiniteScrollComplete');
			}
			oldLoadMore.call(this,broadcast,broadcast);
		}

		var oldReload = entries.reload; 
		entries.reload = function(delay) {
			function broadcast() {
				$rootScope.$broadcast('scroll.refreshComplete');
			}
			oldReload.call( this, delay, function() {
				darwino.hybrid.setDirty(false);
				broadcast();
			},broadcast);
		}
		
		//
		// Handling attachments
		//
		entries.openAttachment = function(thisEvent, att) {
			entries.openAttachmentFunc(thisEvent, att);
		};
	
		//
		// Search Bar
		//
		entries.searchMode = 0;
		entries.startSearch = function() {
			this.searchMode = 1;
		};
		entries.executeSearch = function() {
			this.reload(500);
		};
		entries.cancelSearch = function() {
			this.searchMode = 0;
			if(this.ftSearch) {
				this.ftSearch = "";
				this.reload();
			}
		};
		
		return entries;
	}
}])


//
//	Main controller for the whole page
//
.controller('MainCtrl', ['$scope','$http', function($scope,$http) {
	var _appInfo = null;
	$scope.getAppInformation = function() {
		if(!_appInfo) {
			_appInfo = "<Fetching Application Information>"
			var successCallback = function(data, status, headers, config) {
				_appInfo = darwino.Utils.toJson(data,false);
			};
			var url = "$darwino-app"
			$http.get(url).success(successCallback);
		}
		return _appInfo;
	};	
}])
	

//
//	Views
//
.controller('ViewsCtrl', ['$rootScope','$scope','$state','$stateParams','views', function($rootScope,$scope,$state,$stateParams,views) {
}])

.controller('DocsAuthor', ['$rootScope','$scope','$state','$stateParams','views', function($rootScope,$scope,$state,$stateParams,views) {
}])


//
//	Reader
//
.controller('ReadCtrl', ['$scope', '$rootScope','views', function($scope,$rootScope,views) {
}])


//
//	Editor
//
.controller('EditCtrl', ['$scope', '$rootScope','$state','$stateParams','$ionicHistory','views', function($scope,$rootScope,$state,$stateParams,$ionicHistory,views) {
	var id = $stateParams.id;
	$scope.doc = null;
	$scope.json = null;
	$scope.dbPromise.then(function() {
		if(id && darwino.Utils.startsWith(id,'id:')) {
			return $scope.nsfdata.loadDocument(id.substring(3));
		} else {
			return $scope.nsfdata.newDocument();
		}
	}).then(function (doc) {
		if(id && darwino.Utils.startsWith(id,'pid:')) {
			doc.setParentUnid(id.substring(4));
		}
		doc.convertAttachmentUrlsForDisplay();
		$scope.doc = doc;
		$scope.json = doc.getJson();
		$scope.apply();
	});
	
	$scope.submit = function() {
		var entries = $rootScope.entries;
		var doc = $scope.doc;
		if(doc) {
			doc.convertAttachmentUrlsForStorage();
			var isNew = doc.isNewDocument();
			doc.save().then(function() {
				doc.convertAttachmentUrlsForDisplay();
				
				// We should go back to the previous once the item are reloaded
				// Else it will display the old data
				function back() {$ionicHistory.goBack()}
				if(isNew) {
					entries.addItem(doc.getUnid(),back);
				} else {
					entries.replaceItem(doc.getUnid(),back);
				}
			})
		}
	}

	$scope.cancel = function() {
		$ionicHistory.goBack();
	}
	
	$scope.getTitle = function() {
		var doc = $scope.doc;
		if(doc) {
			return doc.isNewDocument() ? "New Entry" : "Edit Entry";
		}
		return "";
	}
}])


//
//	User
//
.controller('UserCtrl', ['$scope','$stateParams', function($scope,$stateParams) {
	$scope.userAttr = "";
	$scope.userPayload = "";
	$scope.userConnAttrs = "";
	$scope.userConnPayload = "";

	$scope.user = userService.getUser($stateParams.userdn, function(user,read) {
		if(read) {
			$scope.user = user;
			$scope.apply();
		}
	});
}])

}());
