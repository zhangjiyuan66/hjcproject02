'use strict';
document.addEventListener('DOMContentLoaded',function(){

	//登录
	(function(){
		var oHd_r=document.getElementById('hd_r');
		var oLogin=document.getElementById('login');
		//var oA1=oHd_r.getElementsByTagName('a')[3];
		var oA2=oHd_r.getElementsByTagName('a')[4];
		var oS=document.getElementById('ss');
		var oI=oS.getElementsByTagName('i');
		var oAdmin=document.getElementById('admin'); 
		var date=eval();
		var num='';
		var nam='';
			oLogin.onclick=function(){
				alert('登陆成功');
				oA2.innerHTML='安全退出';	
				oA2.style.color='#999';
				oA2.style.paddingLeft='20px';
				oA2.onmouseover=function(){
					this.style.color='#ff8839';	
					this.style.textDecoration='none';
				};
				oA2.onmouseout=function(){
					this.style.color='#999';	
				};
				oS.innerHTML='系统消息'+num;
				oS.style.color='#999';
				oS.onmouseover=function(){
					oS.style.color='#ff8839';	
				};
				oS.onmouseout=function(){
					oS.style.color='#999';	
				};
				oAdmin.innerHTML=nam;
				
			};
			oA2.onclick=function(){
				alert('退出成功');
				self.location.reload(); 
				
			};
			
	})();
	
	
	var total=100;
	
	//banner轮播js
	(function(){
		var oYs=document.getElementById('ys');
		var oShow=oYs.getElementsByTagName('ul')[0];	
		var oBd = document.getElementById('bd');
		var oUl = oBd.getElementsByTagName('ul')[0];
		var aLi = oUl.getElementsByTagName('li');
		var oOl = oBd.getElementsByTagName('ol')[0];
		var aBtn = oOl.getElementsByTagName('li');
		var oPrev = oBd.getElementsByTagName('a')[3];
		var oNext = oBd.getElementsByTagName('a')[4];
		var iNow = 0;
		oYs.onmouseover=function(){
			oShow.style.display="block";	
		};
		oYs.onmouseout=function(){
			oShow.style.display="none";	
		};
		for(var i=0;i<aBtn.length;i++){
			(function(index){
				aBtn[i].onmouseover=function(){
					iNow=index;
					tab();
				};
			})(i);
		};
		function tab(){
			for(var i=0;i<aBtn.length;i++){
				aBtn[i].className='';
				aLi[i].className='';
			};
			aBtn[iNow].className='on1';
			aLi[iNow].className='on1';
		};
		oPrev.onclick=function(){
			iNow--;
			if(iNow<0){
				iNow=aBtn.length-1;
			};
			tab();
		};
		oNext.onclick=next;
		function next(){
			iNow++;
			if(iNow==aBtn.length){
				iNow=0;
			};
			tab();
		};
		
		var timer = setInterval(next,2000);
		
		oBd.onmouseover=function(){
			oPrev.style.display='block';
			oNext.style.display='block';
			clearInterval(timer);
		};
		oBd.onmouseout=function(){
			oPrev.style.display='none';
			oNext.style.display='none';
			timer = setInterval(next,2000);
		};
	})();
	//导航栏下标
	$(function(){
		$(".btn").click(function(){
			$(".btn").find("i").attr("class","");
			$(this).find("i").attr("class","on");	
		});	
	});
	//官方公告
	jQuery("#notice_r").slide({mainCell:"ul",autoPlay:true,effect:"topLoop",interTime:"1500"});
		/*var oNotice_r=document.getElementById('notice_r');
		var oUl1=oNotice_r.children[0];
		var aLi1=oUl1.children;
		var oHeight=aLi1[0].offsetHeight;
		num=0;
		timer=null;
		num2=0;
		timer=setInterval(toRun,300);
		function toRun(){
			if(num==0){
				aLi1[0].style.position='static';
				oUl1.style.top=0;
				num2=0;
			}
			num++;
			if(num==aLi1.length){
				num=0;
				aLi1[0].style.position="relative";
				aLi1[0].style.top=aLi1.length*oHeight+'px';
				//console.log(aLi1)
			}
			num2++;
			startMove(oUl1,{top:-(num2*oHeight)});
		}*/
	//进度条Ajax
	function json2url(json){
			var arr = [];
			for(var name in json){
				arr.push(name+'='+encodeURIComponent(json[name]));
			};
			return arr.join('&');
		};
        //ajax函数
        function ajax(json){
            json = json||{};
            if(!json.url)return;
            json.data = json.data||{};
            json.type = json.type||'get';
            
            
            if(window.XMLHttpRequest){
                var oAjax = new XMLHttpRequest();
            }else{
                var oAjax = new ActiveXObject('Microsoft.XMLHTTP');
            };
            
            switch(json.type.toLowerCase()){
                case 'get':
                    oAjax.open('GET',json.url+'?'+json2url(json.data),true);
                    oAjax.send();
                    break;
                case 'post':
                    oAjax.open('POST',json.url,true);
                    oAjax.setRequestHeader('Content-type','application/x-www-form-urlencoded');
                    oAjax.send(json2url(json.data));
                    break;
            };
            oAjax.onreadystatechange=function(){
                if(oAjax.readyState==4){
                    if(oAjax.status>=200&&oAjax.status<300||oAjax.status==304){
                        json.success&&json.success(oAjax.responseText);
                    }else{
                        json.error&&json.error(oAjax.status);
                    };
                };
            };
        };
        var arr = [{'id1':'bai','id2':'bar'},{'id1':'bai1','id2':'bar1'},{'id1':'bai2','id2':'bar2'},{'id1':'bai3','id2':'bar3'},{'id1':'bai4','id2':'bar4'},{'id1':'bai5','id2':'bar5'},{'id1':'bai6','id2':'bar6'},{'id1':'bai7','id2':'bar7'}];
		function show(){
                ajax({
                    url:'',
                    type:'post',
                    data:{},
                    success:function(str){
                        var json = eval('('+str+')');
                        var cur = json.curN;
                        var tota = json.total;
                        for(var i=0;i<arr.length;i++){
                            var oBai=document.getElementById(arr[i].id1);
                            var oBar=document.getElementById(arr[i].id2);
                            oBar.style.width=((cur[i]/tota[i])*185)+'px';
                            oBai.innerHTML=cur[i]+'%';
                        }
                    }
                });	
		}
        show();
	//返回顶部
	$(function(){
		var speed=2000;
		var h=document.body.scrollHeight;
		$('#top').click(function(){
			$('html,body').animate({
				scrollTop:'0px'	
			},speed)	
		})	
	});

},false)
