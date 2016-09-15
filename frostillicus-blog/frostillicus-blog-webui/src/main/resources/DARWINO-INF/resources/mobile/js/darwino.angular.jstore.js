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

/**
 * JSON store AngularJS 1.x utilities.
 */
darwino.provide("darwino/angular/jstore",null,function() {

	var LOG_GROUP = "darwino.angular.jstore";
	darwino.log.enable(LOG_GROUP,darwino.log.DEBUG)
	
	var mod = angular.module('darwino.angular.jstore',[]);

	var REFRESHCOUNT = 15;
	var MORECOUNT = 15;

	var ngHttp;
	var ngTimeout;
	
	var jstore = darwino.jstore;
	
	function ItemList(session) {
		this.session = session;
		this.baseUrl = session.getHttpStoreClient().getHttpClient().getBaseUrl();
		this.refreshCount = REFRESHCOUNT;
		this.moreCount = MORECOUNT;
		this.onItemsLoaded = null;
	}

	ItemList.prototype.initCursor = function(params) {
		//darwino.log.d(LOG_GROUP,"Create ItemList for database {0}, store {1}",databaseId,storeId);
		this.databaseId = params.database;
		this.storeId = params.store;
		this.indexId = params.index;
		this.instanceId = params.instanceId;
		this.orderBy = params.orderBy;
		this.categoryStart = params.categoryStart;
		this.categoryCount = params.categoryCount;
		this.key = params.key;
		this.query = params.query;
		this.extract = params.extract;
		this.aggregate = params.aggregate;
		this.parentId = params.parentId;
		this.ftSearch = params.ftSearch;
		this.hierarchical = params.hierarchical;
		this.jsonTree = params.jsonTree;
		this.options = params.options;
		
		this.resetCursor();
	}

	ItemList.prototype.resetCursor = function() {
		this.state = 0;
		this.all = [];
		this.count = -1;
		this.eof = false;
		this.selectedItem = null;
		this.detailItem = null;
		this.showResponses = {};

		this.refreshTimeout = null;
		this.loading = false;
	}

	ItemList.prototype.getInstance = function() {
		return this.instanceId;
	}
	ItemList.prototype.setInstance = function(id,cb) {
		this.instanceId = id;
	}
	
	ItemList.prototype._databaseUrl = function() {
		return darwino.Utils.concatPath(this.baseUrl,"/databases/")+encodeURIComponent(this.databaseId);
	}
	ItemList.prototype._storeUrl = function(u) {
		var url = this._databaseUrl()+"/stores/"+encodeURIComponent(this.storeId);
		if(this.indexId) {
			url += "/indexes/"+encodeURIComponent(this.indexId);
		}
		return url;
	}

	ItemList.prototype.isLoading = function() {
		return this.loading;
	}
	
	ItemList.prototype.findRoot = function(unid) {
		if(unid) {
			for(var i=0; i<this.all.length; i++) {
				if(this.all[i].unid==unid) {
					return this.all[i];
				}
				if(this._hasItem(this.all[i],unid)) {
					return this.all[i];
				}
			}
		}
		return null;
	}
	
	ItemList.prototype._hasItem = function(root,unid) {
		if(root.children) {
			for(var i=0; i<root.children.length; i++) {
				if(root.children[i].unid==unid) {
					return true;
				}
				if(this._hasItem(root.children[i],unid)) {
					return true;
				}
			}
		}
		return false;
	}
	
	ItemList.prototype.getEntries = function() {
		if(this.all.length==0 && !this.eof) {
			this.reload();
		}
		return this.all;
	}
	ItemList.prototype.getEntriesCount = function() {
		// Performance on larger dataset
		// A count can be very time consuming, so we disable it
		// -> https://wiki.postgresql.org/wiki/FAQ#Why_is_.22SELECT_count.28.2A.29_FROM_bigtable.3B.22_slow.3F
		// We postpone the count to a bit later, so the data query is executed before...
		// Moreover, failing to connect to the server will avoid the call.
		var _this = this;
		if(this.count<-1) {
			this.count = -1;
			var url = this._storeUrl()+"/count";
			url += "?options="+this.options;
			if(this.ftSearch) {
				url += "&ftsearch="+encodeURIComponent(this.ftSearch);
				url += '&orderby=_ftRank'
			} else if(this.orderBy) {
				url += '&orderby='+encodeURIComponent(this.orderBy);
				if(this.categoryCount) {
					url += "&categorycount="+this.categoryCount;
				}
				if(this.categoryStart) {
					url += "&categorystart="+this.categoryStart;
				}
			}
			if(this.key) {
				url += "&key=\""+encodeURIComponent(this.key)+"\"";
			}
			if(this.parentId) {
				url += "&parentid="+encodeURIComponent(this.parentId);
			}
			if(this.query) {
				url += "&query="+encodeURIComponent(this.query);
			}
			if(this.instanceId) {
				url += '&instance=' + encodeURIComponent(this.instanceId);
			}
			setTimeout(function() {
				if(_this.count==-1) {
					ngHttp.get(url).then(function(response) {
						_this.count = response.data['count'];
						darwino.log.d(LOG_GROUP,"Calculated store entries count {0}",_this.count);
					},function(response) {
						_this.count = 0;
						darwino.log.d(LOG_GROUP,"Failing to calculate store entries count {0}",_this.count);
					});
				}
			},
			200);
		}
		return this.count>=0 ? this.count : null; 
	}
	ItemList.prototype.selectItem = function(selectedItem) {
		this.detailItem = selectedItem;
		// The item is selected if it is in the list
		if(this.all) {
			for(var i=0; i<this.all.length; i++) {
				if(this.all[i]===selectedItem) {
					this.selectedItem = selectedItem;
					break;
				}
			}
		}
	}

	ItemList.prototype.reload = function(delay,cb) {
		var _this = this;
		function doReload() {
			// If there is already an ongoing request, then ignore the new one
			if(_this.loading) {
				return;
			}
			_this.refreshTimeout = null;
			_this.eof = false;
			_this.all = [];
			_this.selectedItem = null;
			_this.detailItem = null;
			_this.count = -2; // Ask for the count
			_this.showResponses = {};
			_this.loadItems(0,_this.refreshCount,cb);
		}
		if(_this.refreshTimeout) {
			ngTimeout.cancel(_this.refreshTimeout);
			_this.refreshTimeout = null;
		}
		if(delay) {
			_this.refreshTimeout = ngTimeout(doReload,delay);
		} else {
			doReload();
		}
	}
	ItemList.prototype.isShowResponses = function(item) {
		return this.showResponses[item.unid]==true;
	}
	ItemList.prototype.toggleResponses = function(item) {
		this.showResponses[item.unid] = !this.showResponses[item.unid];
	}
	
	ItemList.prototype.hasMore = function() {
		// We cannot check the loading flag here as it changes the state and forces a loadMore()
		// So we check it in loadMore(), where we do nothing
		return !this.eof;
	}
	
	ItemList.prototype.loadMore = function(cb,err) {
		// If there is already an ongoing request, then ignore the new one
		if(!this.hasMore() || this.loading) {
			if(cb) cb({});
			return false;
		}
		darwino.log.d(LOG_GROUP,"Load more entries, count={0}",this.all.length);
		this.loadItems(this.all.length,this.moreCount,cb,err);
		return true;
	}

	/**
	 * Load a set of items from the database.
	 */
	ItemList.prototype.loadItems = function(skip,count,cb,err) {
		var _this = this;
		var url = this._storeUrl()+'/entries'
				+'?skip='+skip
				+'&limit='+count;
		if(this.options) {
			url += '&options='+this.options;
		}
		if(this.jsonTree) {
			url += '&jsontree=true'
		}
		if(this.hierarchical>0) {
			url += '&hierarchical='+this.hierarchical
		}
		if(this.ftSearch) {
			url += "&ftsearch="+encodeURIComponent(this.ftSearch);
			url += '&orderby=_ftRank'
		} else if(this.orderBy) {
			url += '&orderby='+encodeURIComponent(this.orderBy);
			if(this.categoryCount) {
				url += "&categorycount="+this.categoryCount;
			}
			if(this.categoryStart) {
				url += "&categorystart="+this.categoryStart;
			}
		}
		if(this.parentId) {
			url += "&parentid="+encodeURIComponent(this.parentId);
		}
		if(this.key) {
			url += "&key=\""+encodeURIComponent(this.key)+"\"";
		}
		if(this.query) {
			url += "&query="+encodeURIComponent(this.query);
		}
		if(this.extract) {
			url += "&extract="+encodeURIComponent(this.extract);
		}
		if(this.aggregate) {
			url += "&aggregate="+encodeURIComponent(this.aggregate);
		}
		if(this.instanceId) {
			url += '&instance=' + encodeURIComponent(this.instanceId);
		}
		
		this.loading = true;
		this._loadItems(url,function(data) {
			_this.loading = false;
			if(data.length<count) {
				_this.eof = true;
			}
			if(data.length>0) {
				for(var i=0; i<data.length; i++) {
					_this.all.push(data[i]);
				}
				if(!_this.selectedItem && _this.all.length) {
					_this.selectItem(_this.all[0]);
				}
			}
			if(cb) cb(data);
		}, function(data) {
			// In case of error, all the items are considered loaded...
			_this.loading = false;
			_this.eof = true;
			if(skip==0) {
				_this.count = 0;
			}
			if(err) err(data);
		});
	}

	/**
	 * Load a new item from the database.
	 */
	ItemList.prototype.addItem = function(unid,cb,err) {
		this.addOrReplaceItem(unid,true,cb,err);
	}
	ItemList.prototype.replaceItem = function(unid,cb,err) {
		this.addOrReplaceItem(unid,false,cb,err);
	}
	ItemList.prototype.addOrReplaceItem = function(unid,isnew,cb,err) {
		var _this = this;
		var url = this._storeUrl()+'/entries'
				+'?unid='+unid
		if(this.options) {
			url += '&options='+this.options;
		}
		if(this.jsonTree) {
			url += '&jsontree=true'
		}
		if(this.hierarchical>0) {
			url += '&hierarchical='+this.hierarchical+"&parentid=*" // The unid can have a parent...
		}
		if(this.instanceId) {
			url += '&instance=' + encodeURIComponent(this.instanceId);
		}
		this._loadItems(url,function(data) {
			if(data.length>0) {
				_this._addOrReplaceItem(data[0],isnew); 
			}
			if(cb) cb(data);
		}, function(data) {
			if(err) err(data);
		});
	}
	ItemList.prototype._addOrReplaceItem = function(item,isnew) {
		var _this = this;
		function addReplace(parent) {
			// Look in the list of children if it can be replaced, when not new
			var items = parent ? parent.children : _this.all;
			if(items!=null) {
				for(var i=0; i<items.length; i++) {
					var it = items[i]; 
					if(!isnew) {
						if(it.unid==item.unid) {
							items[i] = item;
							if(_this.detailItem===it) {
								_this.detailItem=parent||item;
							}
							if(_this.selectedItem===it) {
								_this.selectedItem=item;
							}
							return true;
						}
					}
					
					// Do the same recursively
					if(addReplace(it)) {
						return true;
					}
				}
			}
			
			// If not, then look if it belongs to the parent, or the main array
			// So it can be added as a new document
			if(parent!=null) {
				if(parent.unid==item.parentUnid) {
					if(!parent.children) {
						parent.children = [];
					}
					parent.children.unshift(item);				
					_this.detailItem=parent;
					return true;
				} 
			} else {
				_this.all.unshift(item);				
				_this.selectedItem=_this.detailItem=item;
				return true;
			}
			return false;
		}
		// Try to add or replace the item in the current list.
		addReplace(null);
	}

	/**
	 * Delete an item from the database and remove it from the in memory tree.
	 */
	ItemList.prototype.deleteItem = function(item,cb,err) {
		var _this = this;
		var url = this._storeUrl()+"/documents/"+encodeURIComponent(item.unid);
		url += '?options=' + jstore.Store.DELETE_CHILDREN;
		if(this.instanceId) {
			url += '&instance=' + encodeURIComponent(this.instanceId);
		}
		ngHttp.delete(url).then(function(data) {
			_this.count = -2; // Ask for the count
			_this._removeItem(item)
			if(cb) cb(data);
		}, function(data) {
			if(err) err(data);
		});
	}
	ItemList.prototype._removeItem = function(item) {
		var _this = this;
		function remove(parent) {
			var items = parent?parent.children:_this.all;
			if(items) {
				for(var i=0; i<items.length; i++) {
					var it = items[i]; 
					if(it===item) {
						items.splice(i, 1);
						if(_this.selectedItem==it) {
							_this.selectedItem=_this.detailItem=items.length>0 ? items[Math.min(i,items.length-1)] : null;
						} else if(_this.detailItem==it) {
							_this.detailItem=parent;
						}
						return true;
					}
					if(remove(it)) {
						return true;
					}
				}
			}
			return false;
		}
		return remove(null);
	}
	
	
	/**
	 * Internal implementation of a server call that load items.
	 * The loaded items are processed for the richtext content, and gives the implementation a chance
	 * to also process the entries. For example, one can sort the children collection in a different
	 * order.
	 */
	ItemList.prototype._loadItems = function(url,cb,cberr) {
		var _this = this;
		function absoluteURL(url) {
			var a = document.createElement('a');
			a.href = url;
			return a.href;
		}
		url = absoluteURL(url);
		var successCallback = function(response) {
			var items = [];
			function loaded(item) {
				for(var field in item.value) {
					if(darwino.Utils.isString(item.value[field])) {
						item.value[field] = darwino.jstore.richTextToDisplayFormat(_this.databaseId, item.storeId, _this.instanceId, item.unid, item.value[field]);
					}
				}
				if(item.children) {
					for(var i=0; i<item.children.length; i++) {
						loaded(item.children[i]);
					}
				}
				if(_this.onItemsLoaded) items.push(item);
			}
			var data = response.data;
			for(var i = 0; i < data.length; i++) {
				loaded(data[i]);
			}
			if(_this.onItemsLoaded && items.length) {
				_this.onItemsLoaded(items);
			}
			if(cb) {
				cb(data);
			}
			darwino.log.d(LOG_GROUP,'Entries loaded from server: '+url+', #', _this.all.length);
		};
		var errorCallback = function(response) {
			if(cberr) {
				cberr();
			}
			darwino.log.d(LOG_GROUP,'Error while loading entries from server: '+url+', Err:'+response.status);
		};
		ngHttp.get(url).then(successCallback,errorCallback);
	}

	ItemList.prototype.getAttachments = function(item) {
		var _this = this;
		if(!item || item.category) return null;
		if(!item.attachments) {
			item.attachments = [];
			var jsonfields = jstore.Document.JSON_ALLATTACHMENTS;
			var options = jstore.Store.DOCUMENT_NOREADMARK;
			var url = this._storeUrl()+"/documents/"+encodeURIComponent(item.unid)+"?jsonfields="+jsonfields+"&options="+options;
			if(this.instanceId) {
				url += '&instance=' + encodeURIComponent(this.instanceId);
			}
			ngHttp.get(url).then(function(response) {
				var atts = response.data.attachments;
				if(atts) {
					// Do some post-processing
					angular.forEach(atts, function(att) {
						var display = att.name;
						var delimIndex = display.indexOf("||");
						
						// If it's an inline image, ignore it entirely
						if(delimIndex > -1) {
							return;
						}
						
						// Otherwise, remove any field-name prefix
						delimIndex = display.indexOf("^^");
						if(delimIndex > -1) {
							display = display.substring(delimIndex+2);
						}
						
						item.attachments.push({
							name: att.name,
							display: display,
							length: att.length,
							mimeType: att.mimeType,
							url: _this.session.getUrlBuilder().getAttachmentUrl(_this.databaseId, _this.storeId, response.data.unid, att.name,_this.instanceId)
						})
					});
				}
			});
		}
		return item.attachments;
	}

	//
	// Handling attachment
	//
	// Desktop browsers will use the link normally, but hybrid mobile
	// apps should use special handling.
	//
	ItemList.prototype.openAttachment = function(thisEvent, att) {
		if(darwino.hybrid.isHybrid()) {
			thisEvent.preventDefault();
			darwino.hybrid.exec("OpenAttachment",{
				database:this.databaseId, 
				store:this.storeId,
				instance:this.instanceId,
				unid:this.detailItem.unid, 
				name:att.name,
				file:att.display,
				mimeType:att.mimeType
			});
		}
	}

	mod.factory('sessionRecoverer', ['$q', '$injector', function($q, $injector) {  
	    var sessionRecoverer = {
	        responseError: function(response) {
	        	var status = response.status; 
	            // Session has expired
	            if (status==419 || response.headers('x-dwo-auth-msg')=='authrequired' ){
	            	location.reload();
	            }
	            if(status<200 || status>299) {
		            return $q.reject(response);
	            }
				return response;
	        }
	    };
	    return sessionRecoverer;
	}]);
	mod.config(['$httpProvider', function($httpProvider) {  
	    $httpProvider.interceptors.push('sessionRecoverer');
	}]);
	
	
	mod.service('$jstore', function($http,$timeout) {
		ngHttp = $http;
		ngTimeout = $timeout;
		return {
			createItemList: function(session) {
				return new ItemList(session);
			}
		}
	});
});
