app.controller('itemController',function ($scope) {

  $scope.num = 1;
  $scope.specificationItems={};//存储用户选择的规格
  
	//数量加减
  $scope.addNum=function(x){
     $scope.num+=x;
	 if($scope.num<1){
		  $scope.num = 1;
	 }
  }
  
  //用户选择规格
  $scope.selectSpecification=function(key,value){
	$scope.specificationItems[key]=value;
	searchSku();
  }
  
  $scope.isSelected=function(key,value){
	  
	  if($scope.specificationItems[key]== value){
		  return true;
	  }else{
		  return false;	
	  }
  }
  
  $scope.sku={};//当前选择的sku
  //加载默认的sku
  $scope.loadSku=function(){
	$scope.sku=skuList[0];
	$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ; //深克隆
  }
  
  //匹配两个对象是否相等
  matchObject=function(map1,map2){
	  
	  for(var k in map1){
		if(map1[k]!=map2[k]){
			return false;
		}
	  }
	  
	  for(var k in map2){
		if(map2[k]!=map1[k]){
			return false;
		}
	  }
	 return true;
  }
  
  //规矩规格查找sku
  searchSku=function(){
		for(var i=0;i<skuList.length;i++){
			if(matchObject(skuList[i].spec,$scope.specificationItems)){
			  $scope.sku=skuList[i];
			  return ;
			}
		}
		$scope.sku={id:0,title:'-----',price:0};
  }
  
  //添加商品的购物车
  $scope.addToCart = function(){
	  alert('skuId:'+$scope.sku.id);
  }
    
});