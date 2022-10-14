function ajax(options){
  options=options||{};

  options.type=options.type||'get';
  options.data=options.data||{};
  options.dataType=options.dataType||'text';

  //不兼容IE6
  let xhr=new XMLHttpRequest();

  //数据组装
  let arr=[];
  for(let name in options.data){
//    arr.push(`${encodeURIComponent(name)}=${encodeURIComponent(options.data[name])}`);
    arr.push(encodeURIComponent(name)+ '='+encodeURIComponent(options.data[name]));
  }
  let strData=arr.join('&');

  if(options.type=='post'){
    xhr.open('POST', options.url, true);
    xhr.setRequestHeader('content-type', 'application/json');
    xhr.send(JSON.stringify(options.data));
  }else{
    xhr.open('GET', options.url+'?'+strData, true);
    xhr.send();
  }

  //接收
  xhr.onreadystatechange=function (){
    //4——完事
    if(xhr.readyState==4){
      //成功——2xx、304
      if(xhr.status>=200 && xhr.status<300 || xhr.status==304){
        let data=xhr.responseText;

        switch(options.dataType){
          case 'json':
            if(window.JSON && JSON.parse){
              data=JSON.parse(data);
            }else{
              data=eval('('+str+')');
            }
            break;
          case 'xml':
            data=xhr.responseXML;
            break;
        }

        options.success && options.success(data);
      }else{
        options.error && options.error();
      }
    }
  };
}
