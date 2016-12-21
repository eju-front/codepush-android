function #(id){
 return document.getElementById(id);
}

function val(id){
 return (#(id) || {}).value;
}

function getVersion(String info){
    var jsonInfo = JSON.parse(info);
    alert(info);
    console.log(jsonInfo);
}

function getPath(String path){
    alert(path);
    console.log(path);
}

function error(String error){
    console.log(error);
    alert(error);
}

#('checkVersion').onclick = function(){
    android.checkVersion();
}

#('download').onclick=function(){
    android.download(3);
}