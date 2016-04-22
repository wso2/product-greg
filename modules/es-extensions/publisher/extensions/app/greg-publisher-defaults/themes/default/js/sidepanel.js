/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Setting-up global variables.
 */
var sidePanel = '.wr-side-panel',
    navHeight = $('nav.navbar').height(),
    headerHeight = $('header').height(),
    offset = (headerHeight + navHeight),
    toggleButton = 'a.wr-side-panel-toggle-btn';

/*
 * On window loaded functions.
 */
$(window).load(function(){
    setSidePanelHeight();
    $(sidePanel).css('top', offset);
    sidePanelPositionFix();

    if($(toggleButton + ' .wr-notification-bubble').html() == 0){
        $(toggleButton + ' .wr-notification-bubble').closest(toggleButton).hide();
    }
});

/*
 * On window resize functions.
 */
$(window).resize(function(){
    setSidePanelHeight();
    sidePanelPositionFix();
});

/*
 * On main div.container resize functions.
 * @required  jquery.resize.js
 */
$('.container').resize(function(){
    setSidePanelHeight();
    sidePanelPositionFix();
});

/*
 * On window scroll functions.
 */
$(function(){ // document ready
    if (!!$(sidePanel).offset()) { // make sure ".sticky" element exists
        //var stickyTop = $(sidePanel).offset().top; // returns number
        $(window).scroll(function(){ // scroll event
            sidePanelPositionFix();
        });
    }
});

/*
 * Notification panel fix positioning on window scrolling
 */
function sidePanelPositionFix(){
    var windowTop = $(window).scrollTop(); // returns number
    if (headerHeight < windowTop){
        $(sidePanel).css({ position: 'fixed', top: '0' });
        $(sidePanel).css('opacity','0.95');
    }
    else {
        $(sidePanel).css('position','absolute');
        $(sidePanel).css('top', offset);
        $(sidePanel).css('opacity','0.95');
    }
}

/*
 * Notification panel slide toggle
 * @param view: which should be visible on side panel open
 * @param button: selected button
 */
function toggleSidePanel(view,button){

    var viewElement = (sidePanel + ' #' + view);

    $(viewElement).siblings().hide();
    $(viewElement).show();

    $(button).siblings().removeClass('selected');
    $(button).toggleClass('selected');

    if($(button).hasClass('selected')){
        $('.navbar  .'+view+'  .fw-stack-1-5x').removeClass('fw-left-arrow');
        $('.navbar  .'+view+'  .fw-stack-1-5x').addClass('fw-right-arrow');
        $(sidePanel).addClass('toggled');
    }
    else {
        $('.navbar  .'+view+'  .fw-stack-1-5x').removeClass('fw-right-arrow');
        $('.navbar  .'+view+'  .fw-stack-1-5x').addClass('fw-left-arrow');
        $(sidePanel).removeClass('toggled');
    }

}

/*
 * Set notification bar height to fill window height
 */
function setSidePanelHeight(){
    var screenHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
    var minHeight = screenHeight - 144;
    $('.wr-side-panel').css('min-height', minHeight+'px');
    //document.getElementsByClassName("wr-side-panel").style.minHeight = minHeight+"px";
    $(sidePanel).height('100%');
}
