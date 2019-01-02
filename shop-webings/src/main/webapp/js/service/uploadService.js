//文件上传服务器
app.service('uploadService',function ($http) {
    this.uploadFile = function () {
        var formData = new FormData();
        //html5中新增的对象，转门用于上传
        formData.append('file',file.files[0]); //key file 文件上传框的nmae
        return $http({
            method:'POST',
            url:"../upload.do",
            data:formData,
            headers: {'Content-Type':undefined},
            transformRequest:angular.identity     //二进制上传
        });
    }
})