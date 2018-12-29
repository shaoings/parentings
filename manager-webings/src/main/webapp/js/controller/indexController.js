app.controller('indexController',function ($scope,$controller,logService) {

    //读取当前登录人
    $scope.showLoginName=function () {
        logService.loginName().success(
            function (response) {
                $scope.loginName = response.loginName;
            }
        );
    }
})