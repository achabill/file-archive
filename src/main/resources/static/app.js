var app = angular.module('archive',  ['angular-loading-bar'])

.config(['cfpLoadingBarProvider', function(cfpLoadingBarProvider) {
    cfpLoadingBarProvider.parentSelector = '#loading-bar-container';
    cfpLoadingBarProvider.spinnerTemplate = '<div><span class="fa fa-spinner">UPLOADING...</div>';
  }]);

app.directive('fileModel', [ '$parse', function($parse) {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;

			element.bind('change', function() {
				scope.$apply(function() {
					modelSetter(scope, element[0].files[0]);
				});
			});
		}
	};
}]);

app.service('ArchiveService', [ '$http', '$rootScope', function($http, $rootScope) {
	this.search = function(contentType) {
		$http.get("http://localhost:8090/archive/documents", {
			params : {
				contentType : contentType
			}
		}).success(function(response) {
			$rootScope.metadataList = response;
		}).error(function() {
		});
	}
}]);

app.service('fileUpload', ['$http','ArchiveService', function($http, ArchiveService) {
	this.uploadFileToUrl = function(uploadUrl, file, name, date) {
		var fd = new FormData();
		fd.append('file', file);
		fd.append('person', name);
		fd.append('date', date);
		$http.post(uploadUrl, fd, {
			transformRequest : angular.identity,
			headers : {
				'Content-Type' : undefined
			}
		}).success(function() {
			ArchiveService.search(null);
		}).error(function(error) {
			console.log(error);
		});
	}
} ]);

app.controller('UploadCtrl', [ '$scope', 'fileUpload',
		function($scope, fileUpload) {
			var uploadList = [];
			$scope.uploadFile = function() {
				var file = $scope.myFile;
				var name = "skylabase";
				var date = "1111-11-11";
				console.log('file is ' + JSON.stringify(file));
				var uploadUrl = "http://localhost:8090/archive/upload";
				fileUpload.uploadFileToUrl(uploadUrl, file, name, date);
			};
		} ]);

app.controller('ArchiveCtrl', function($scope, $http) {
	$scope.search = function(name, date,contentType) {
		$http.get("http://localhost:8090/archive/documents", {
			params : {
				person : name,
				date : date,
				contentType: contentType
			}
		}).success(function(response) {
			$scope.metadataList = response;
		});
	};
});

app.run(function($rootScope, $http) {
	$http.get("http://localhost:8090/archive/documents").success(
			function(response) {
				$rootScope.metadataList = response;
			});
});

function sortByLabel(claims) {
	claims.sort(function(a, b) {
		var labelA = a.label.toLowerCase(), labelB = b.label.toLowerCase();
		if (labelA < labelB) // sort string ascending
			return -1;
		if (labelA > labelB)
			return 1;
		return 0; // default return value (no sorting)
	});
}
