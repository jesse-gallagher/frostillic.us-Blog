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
	var mod = angular.module('darwino.ionic', ['ionic']);

	// Debug data binding
	mod.directive('jsonModel', function() {
		return {
			restrict : 'A',
			require : 'ngModel',
			link : function(scope, elem, attr, ctrl) {
				ctrl.$parsers.push(function(value) {
					return darwino.Utils.fromJson(value);
				});
				ctrl.$formatters.push(function(modelValue) {
					return darwino.Utils.toJson(modelValue, false);
				});
			}
		};
	});	
	
	// ShowWhen and HideWhen
	// Inpired from https://github.com/andrewmcgivery/ionic-ion-showWhen, MIT license
	function showhide($window,show) {
		return {
			restrict: 'A',
			link: function($scope, $element, $attr) {
				function checkExpose() {
					var mq = media($attr[show?'showWhenMedia':'hideWhenMedia']);
					var matches = $window.matchMedia(mq).matches;
					var visible = (show && matches) || (!show && !matches);  
					if(visible){
						$element.removeClass('ng-hide');
					} else {
						$element.addClass('ng-hide');		
					}
				}
	
				function onResize() {
					debouncedCheck();
				}
	
				var debouncedCheck = ionic.debounce(function() {
					$scope.$apply(function(){
						checkExpose();
					});
				}, 300, false);
	
				checkExpose();
	
				ionic.on('resize', onResize, $window);
	
				$scope.$on('$destroy', function(){
					ionic.off('resize', onResize, $window);
				});
			}
		};
	}
	mod.directive('showWhenMedia', ['$window', function($window) {
		return showhide($window,true);
	}]);
	mod.directive('hideWhenMedia', ['$window', function($window) {
		return showhide($window,false);
	}]);
	
	function media(m) {
		if(m=="large") return "(min-width:768px)"
		return m;
	}
	function ifmedia(ngIfDirective,$window,show) {
		var ngIf = ngIfDirective[0];
		return {
			transclude: ngIf.transclude,
			priority: ngIf.priority,
			terminal: ngIf.terminal,
			restrict: ngIf.restrict,
			link: function($scope, $element, $attr) {
				$attr.ngIf = function() {
					var mq = media($attr[show?'ifMedia':'ifNotMedia']);
					var matches = $window.matchMedia(mq).matches;
					var visible = (show && matches) || (!show && !matches);
					return visible;
				};
				ngIf.link.apply(ngIf, arguments);
			}
		};
	}
	mod.directive('ifMedia', ['ngIfDirective', '$window', function(ngIfDirective,$window) {
		return ifmedia(ngIfDirective,$window,true);
	}]);
	mod.directive('ifNotMedia', ['ngIfDirective', '$window', function(ngIfDirective,$window) {
		return ifmedia(ngIfDirective,$window,false);
	}]);

	// See if this is useful.
	mod.directive('showWhenState', ['$window','$state','$rootScope', function($window,$state,$rootScope) {
		return {
			restrict: 'A',
			link: function($scope, $element, $attr) {
				function checkExpose(){
					var state = $state.current.name;
					var statesToMatch = $attr.showWhenState.split(" || ");
					if(statesToMatch.indexOf(state) > -1){
						$element.removeClass('ng-hide');
					} else {
						$element.addClass('ng-hide');
					}
				}
			
				$rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams){
					checkExpose();
				})
			
				checkExpose();
			}
		};
	}]);
	
})();
