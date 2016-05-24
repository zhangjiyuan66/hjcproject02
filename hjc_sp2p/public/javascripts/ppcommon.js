// JavaScript Document
$(document).ready(function(e) {
	setInterval(GetWindowWidth,10);
});
function GetWindowWidth(){
	var w=$(window).width();
	$(".bd").css("width",w+"px");
	if (w < 1920){
	   $(".bd ul").css({"left":(w-1920)/2+"px"});
	   }else{
		$(".bd ul").css({"left":0+"px"});
	}
}

//通用选项卡 xxk
$(function(){
	tab($(".p_zh_xxkul li"),$(".p_zh_tabox"),"on")
	tab($(".p_xq_xxkul li"),$(".p_xq_tabox"),"on")
	tab($(".y_zcul li"),$(".y_zctab"),"click")
	tab($(".y_zcul li"),$(".y_zctab"),"click")
})
// 文本框获取焦点
$(function(){
	$(".p_text").bind({ 
         focus:function(){ 
		    if (this.value == this.defaultValue){
				this.value="";
				}
			},
		blur:function(){
			if (this.value == ""){ 
                this.value = this.defaultValue; 
            }
        } 
    }); 
})
// 登录密码框
$(function(){
	$(".p_pass").bind({
        focus:function(){ 
			$(this).siblings("label").hide()
		},
		blur:function(){
			if (this.value == ""){ 
				$(this).siblings("label").show()        		
            } 
        } 
		
	})
})

$(function(){
	//首页头部边框处理
	
	/*
	$(".p_top_right a:first").css("border-left","0");
	$(".p_top_right a:last").css("border-right","0");*/
	//首页四大安全模块
	$(".p_sy_ul01 li:last").addClass("p_sy_sflast");
	//首页新闻最后li的边框处理
	$(".p_sy_ggul li:last").css("border","0");
	$(".p_sy_cgul li:last").css("border","0");
})
$(function(){
	$(".p_ft_icon a:last").css("margin-right","0");
	$(".p_jk_boxul>:nth-child(2n)").css("margin-right","0");
});

function tab(li,tabox,name){
	li.eq(0).addClass(name);
	tabox.eq(0).show();
	li.click(function(){
		$(this).addClass(name).siblings().removeClass(name);
		tabox.hide().eq($(this).index()).show();
	})
}
$(function(){
	$(".p_zh_navul>li>h4").click(function(){
		var that = $(this);
            $nextUl=that.next("ul");
		if($nextUl.is(":hidden")){
			that.parent("li").addClass("clicked").siblings().removeClass("clicked");
			$nextUl.slideDown().parent("li").siblings().find("ul").slideUp();
		}else{
			that.parent("li").removeClass("clicked");
			$nextUl.slideUp();
		}
	});
});

// other function
$(function(){
	$('.p_sy_newul').map(function(){ $(this).find('li:last').css('border','none') });
	$(".p_zc_table tr").each(function() {
		var ix = $(this).index();
		$(this).css('z-index',20-ix);
	});
})
//0911 select下拉菜单js
$(function(){
    $(".l_select").each(function(){
      var $select = $(this),
        $cont = $select.children(".l_text"),
        $ul = $select.children("ul"),
        $options = $ul.children("li");

      $cont.text($options.first().text());

      $select.on("click",function(e){
        $(".l_select").not($(this)).children("ul").slideUp(200);
        if($ul.is(":hidden")){
          $ul.slideDown(200);
        }else{
          $ul.slideUp(200);
        }
        e.stopPropagation();
      });

      $options.on("click",function(e){
        $cont.text($(this).text());
      });

      $("body").on("click",function(){
        $ul.slideUp(200);
      });

    });
  });

	/*1110新增js*/
	//弹窗js
	$(function(){
		
	$(".l_top_reg").click(function(){ 
		changeAll();
	    $(".popupbg").show(); 
	    popcenter($(".l_zc_pop")); 
	});
	$(".l_top_log").click(function(){ 
		changeLogin();
	    $(".popupbg").show(); 
	    popcenter($(".y_dlmain"));  
	
	});
	
	$(".p_lobtn").click(function(){ 
		changeAll();
	    $(".popupbg").show(); 
	    popcenter($(".l_zc_pop"));  
	
	});
	$(".p_un").click(function(){ 
	    changeLogin();
	    $(".popupbg").show(); 
	    popcenter($(".y_dlmain"));
	    $(".l_zc_pop").css('display','none');
	
	});
	$(".p_un1").click(function(){ 
		changeAll();
	    popcenter($(".l_zc_pop"));
	    $(".y_dlmain").css('display','none');
	});
	//弹窗居中 
	function popcenter(popup){ 
	var width = popup.width(), 
	  height = popup.height(); 
	  popup.css({ 
	  "margin-left" : -width/2, 
	  "margin-top" : -height/2 
	  }).show(); 
	}
	$(".popclose").click(function(){
	  $(".l_zc_pop").hide(); 
	  $(".y_dlmain").hide(); 
	  $(".popupbg").hide(); 
	})  
	})
//2015-12-11替换js
//标的详情---图片上传滚动
$(document).ready(function(){
  $('.l-upload-btns').hover(function(){
      $(this).fadeTo('fast',1);
    },function(){
      $(this).fadeTo('fast',0.7);
  })
})

$(function(){ 
var $scroll=$(".l-upload-picarea>ul"), 
    $li=$(".l-upload-picarea>ul>li"), 
     index=0, 
     timeId,
   len=$li.length,
   $prev=$(".l-upload-prevbtn"),
   $next=$(".l-upload-nextbtn"),
   width=$li.eq(0).width();
  function scroll(obj){
      obj.stop(true,true).animate({marginLeft:"-="+width+"px"},200,function(){ 
      obj.css("marginLeft","0").find("li:first").appendTo(obj); 
      }); 
  }    
  $scroll.hover(function(){ 
      clearInterval(timeId); 
    },function(){ 
      if (len>4) {
        timeId=setInterval(function(){ 
        scroll($scroll); 
        },3000) 
      } 
      }).trigger("mouseleave"); 
    $next.click(function(){
      if (len>4) {
        clearInterval(timeId);
        scroll($scroll);
      timeId=setInterval(function(){ 
        scroll($scroll); 
        },3000) 
    }
  })
  $prev.click(function(){
      if (len>4) {
      clearInterval(timeId);
          $scroll.find("li").prev().appendTo($scroll);
        $scroll.stop(true,true).css("marginLeft","-="+width+"px").animate({marginLeft:0},200,function(){

        });
      timeId=setInterval(function(){ 
        scroll($scroll); 
      },3000) 
    }
    })
})