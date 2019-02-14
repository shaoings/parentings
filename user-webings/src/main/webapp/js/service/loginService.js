//服务层
app.service('loginService',function ($http) {
    //获取列表数据绑定到表单中
    this.showName=function () {
        return $http.get('../login/name.do');
    }
})