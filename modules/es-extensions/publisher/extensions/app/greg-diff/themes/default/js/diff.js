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
var appBar = '.app-bar',
    viewPanel = '.view-panel',
    appBarHeight = $(appBar).height();

/*
 * On window loaded functions.
 */
//$(window).load(function(){
//    setViewPanelsHeight();
//    addTitle();
//});

/*
 * On window resize functions.
 */
$(window).resize(function(){
    setViewPanelsHeight();
});

/*
 * On main div.container resize functions.
 * @required  jquery.resize.js
 */
$('.container-fluid').resize(function(){
    setViewPanelsHeight();
});

/*
 * Set view panels height relatively to the window
 */
function setViewPanelsHeight(){
    if($(window).width() > 768){
        $(viewPanel).css('height', ($('body').height() - ($(window).height()*.1)));
    }
    else{
        $(viewPanel).css('height', 'auto');
        //$(viewPanel).css('height', ($('body').height() - ($(window).height()*.1)));
    }
    $('.CodeMirror, .CodeMirror-merge').height(($(viewPanel).height()-($(window).height()*.1)));

    /* Fix for code mirror diff view panels width unbalance issue */
    var CodeMirrorSizerMinWidth = '500px';
    $('.CodeMirror-sizer').each(function(){
        if($(this).css('min-width') < CodeMirrorSizerMinWidth){
            CodeMirrorSizerMinWidth = $(this).css('min-width');
            $('.CodeMirror-sizer').css('min-width', CodeMirrorSizerMinWidth);
        }
    });
    $('.CodeMirror-vscrollbar').scrollTop(2);
    $('.CodeMirror-hscrollbar').scrollLeft(2);

    /* Fix for code mirror scrollbar not shows on window load */
    if($('.CodeMirror').height() < $('.CodeMirror-code').height()){
        $('.CodeMirror-vscrollbar').show();
        $('.CodeMirror-vscrollbar > div').height($('.CodeMirror-sizer').height());
    }
}

/*
 * Adding code mirror pane title
 */
function addTitle(baseVersion, revisionVersion, change) {
    $('.CodeMirror-merge-pane').each(function (i) {
        if ("CONTENT_ADDITION" === change) {
            i = 1;
        }
        var title;
        switch (i) {
            case 0:
                title = 'Base ( V' + baseVersion + ' )';
                break;
            case 1:
                title = 'Revision ( V' + revisionVersion + ' )';
                break;
        }

        $('.CodeMirror', this).before('<div class="title">' + title + '</div>');
    });
}