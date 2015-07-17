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
 *
 */
$('.more-toggle-btn').click(function(){
    $(this).prev('.more-toggle-wrapper').toggleClass('expand');
    $(this).html(function(){
        if($(this).prev('.more-toggle-wrapper').hasClass('expand')){
            $(this).html('Show Less');
        }
        else{
            $(this).html('Show All');
        }
    });
});

$('#list_assets_table_body').on('click', '.ctrl-wr-asset', function(){
    $(this).toggleClass('selected');
});


$(function () {
    var selectedServices = [];
    $("#submitServices").click(function(event){
        event.preventDefault();
        $('ul#pageTab li a').each(function() {
            var selectedService = {};
            var selectedAssetType = $('.name', this).html(),
                selectedAssets = $('.tab-content ' + $(this).attr('href')).find('.selected');

            if (selectedAssets.length > 0) {
                var html = $('#list_assets_table_summary').append('<h2 ' +
                'class="sub-heading">' + selectedAssetType + '</h2><hr><div><div class="row">' +
                '<div class="col-lg-12 ctrl-wr-asset-list" data-type="' + selectedAssetType + '"></div></div></div>');

                selectedService.serviceType = selectedAssetType;
                var selectedServiceData = [];
                $.each(selectedAssets, function (i, val) {
                    $(val).appendTo($(html).find('[data-type="' + selectedAssetType + '"]'));
                    selectedServiceData.push($(val).data('content'));
                });
                selectedService.data = selectedServiceData;
            }
            selectedServices.push(selectedService);
        });

        $('#list_assets_table_body').hide();
        $('#list_assets_table_summary').closest('.ctrl-wr-asset-list').show();
    });

    $("#saveServices").click(function(event){
        event.preventDefault();
        $.ajax({
            url: caramel.context + '/assets/server/apis/servers/save?type=server',
            type: 'POST',
            contentType: "application/json",
            dataType: "json",
            data:JSON.stringify(selectedServices),
            success: function (response) {
                $('#list_assets_table_summary').hide();
                $('#saveServices').hide();
                var html = '<h2 class="sub-heading">Services saved successfully</h2>';
                $('#parent-container').append(html);
            },
            error: function () {
                console.log("Error loading services.");
            }
        });
    })
})