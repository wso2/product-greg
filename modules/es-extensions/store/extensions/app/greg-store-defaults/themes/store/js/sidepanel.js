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
    navHeight = $('#nav').height(),
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
        $(sidePanel).css({ position: 'fixed', top: '0px' });
        $(sidePanel).css('opacity','0.92');
    }
    else {
        $(sidePanel).css('position','absolute');
        $(sidePanel).css('top', offset);
        $(sidePanel).css('opacity','0.92');
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
        $('.top-menu-right-custom  .'+view+'  .fw-stack-1-5x').removeClass('fw-left-arrow');
        $('.top-menu-right-custom  .'+view+'  .fw-stack-1-5x').addClass('fw-right-arrow');
        $(sidePanel).show();
        $(sidePanel).addClass('toggled');
    }
    else {
        $('.top-menu-right-custom  .'+view+'  .fw-stack-1-5x').removeClass('fw-right-arrow');
        $('.top-menu-right-custom  .'+view+'  .fw-stack-1-5x').addClass('fw-left-arrow');
        $(sidePanel).hide();
        $(sidePanel).removeClass('toggled');
    }

}

/*
 * Set notification bar height to fill window height
 */
function setSidePanelHeight(){
    $(sidePanel).height($('html').height() - offset);
}

/**
 * Toggle collapse down arrow direction when user click on the collapsing bar(store notificaton)
 */
$('#collapseNotificationsSettings').on('shown.bs.collapse', function () {
    $("#headingNotificationsSettings .fw-down-arrow").removeClass("fw-down-arrow").addClass("fw-up-arrow");
});

$('#collapseNotificationsSettings').on('hidden.bs.collapse', function () {
    $("#headingNotificationsSettings .fw-up-arrow").removeClass("fw-up-arrow").addClass("fw-down-arrow");
});
