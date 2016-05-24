'use strict'
document.addEventListener('DOMContentLoaded',function(){
	function getStyle(obj,sName){
	if(obj.currentStyle){
			return obj.currentStyle[sName];
		}else{
			return getComputedStyle(obj,false)[sName];
		}
	};
	var scaleW=parseInt(document.body.clientWidth||document.documentElement.clientWidth)/320; 
	var resizes = document.querySelectorAll('.resize');
	var resizes1 = document.querySelectorAll('.resize1');
	//计算除图片元素以外的宽、高、top、left
	for (var j=0; j<resizes.length; j++) {
	   resizes[j].style.width=parseInt(getStyle(resizes[j],'width'))*scaleW+'px';
	   resizes[j].style.height=parseInt(getStyle(resizes[j],'height'))*scaleW+'px';
	   resizes[j].style.top=parseInt(getStyle(resizes[j],'top'))*scaleW+'px';
	   resizes[j].style.left=parseInt(getStyle(resizes[j],'left'))*scaleW+'px';
	};
	//图片元素的宽、top、left
	for (var i=0; i<resizes1.length;i++) {
	   resizes1[i].style.width=parseInt(getStyle(resizes1[i],'width'))*scaleW+'px';
	   //resizes1[i].style.height=parseInt(getStyle(resizes1[i],'height'))*scaleW+'px';
	   resizes1[i].style.top=parseInt(getStyle(resizes1[i],'top'))*scaleW+'px';
	   resizes1[i].style.left=parseInt(getStyle(resizes1[i],'left'))*scaleW+'px';
	};
	
	//计算某元素的字体，行高
	function lineHeight(obj){
		var objLien=parseInt(obj.style.height);
		obj.style.lineHeight=objLien+'px';
		obj.style.fontSize=parseInt(getStyle(obj,'fontSize'))*scaleW+'px';
	}
	/*var inp= document.querySelector('.pass_word1');
	var inp1= document.querySelector('.pass_word2');
	var inp2= document.querySelector('.zc');
		lineHeight(inp)
		lineHeight(inp1)
		lineHeight(inp2)*/
		var oResize=document.querySelectorAll('.resize');
		
		for(var i=0;i<oResize.length;i++){
			lineHeight(oResize[i])	
		}
},false)








