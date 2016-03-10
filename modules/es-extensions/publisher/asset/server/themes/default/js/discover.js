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

$('#list_assets_table_body #pageTab1').on('click', '.ctrl-wr-asset', function(){
    $(this).toggleClass('selected');
});

$('#parent-container').on('click', '#closeWindow', function(){
    var win = window.open('', '_self');
    window.close();
    win.close(); return false;
});

$(function () {
    var selectedServices = [];
    var url = "";
    $("#submitServices").click(function(event){
        event.preventDefault();
        $('div#pageTab1 > ul li a').each(function() {
            var selectedService = {};
            var selectedAssetType = $('.name', this).html(),
                selectedAssets = $('.tab-content ' + $(this).attr('href')).find('.selected');

            if (selectedAssets.length > 0) {
                var html = $('#list_assets_table_summary').append('<h2 ' +
                'class="sub-heading">' + selectedAssetType + '</h2><hr><div><div class="row">' +
                '<div class="col-lg-12 ctrl-wr-asset-list" data-type="' + selectedAssetType + '"></div></div></div>');

                selectedService.serviceType = $(this).attr('aria-controls');
                var selectedServiceData = [];
                $.each(selectedAssets, function (i, val) {
                    $(val).appendTo($(html).find('[data-type="' + selectedAssetType + '"]'));
                    selectedServiceData.push($(val).data('content'));
                });
                selectedService.data = selectedServiceData;
            }
            if (selectedService.serviceType != null) {
                selectedServices.push(selectedService);
            }
        });

        $('div#pageTab2 > ul li a').each(function () {
            var existingService = {};
            var existingServices = $('.tab-content ' + $(this).attr('href')).children();
            var existingServiceData = [];
            existingService.serviceType = $(this).attr('aria-controls');

            var keyExisting = null;
            for (var key in selectedServices) {
                if (selectedServices.hasOwnProperty(key)) {
                    if (selectedServices[key].serviceType == existingService.serviceType) {
                        for (var key2 in selectedServices[key].data) {
                            if (selectedServices[key].data.hasOwnProperty(key2)) {
                                existingServiceData.push(selectedServices[key].data[key2]);
                            }
                        }
                        keyExisting = key;
                        break;
                    }
                }
            }

            $.each(existingServices, function (i, val2) {
                existingServiceData.push($(val2).data('content'));
            });
            existingService.data = existingServiceData;
            if (keyExisting != null) {
                delete selectedServices[keyExisting];
            }
            selectedServices.push(existingService);
        });

        selectedServices = selectedServices.filter(Object);
        $('#list_assets_table_body').hide();
        $('#list_assets_table_summary').closest('.ctrl-wr-asset-list').show();
        url = window.location.href;
    });
    $("#backButton").click(function (event) {
        event.preventDefault();
        window.location.replace(url);
    });

    $("#saveServices").click(function (event) {
        event.preventDefault();
        var serverId = $('#serverId').attr('data-serverId');
        var existArtifactStrategy = $('#existArtifactStrategy').val();
        var orphanArtifactStrategy = $('#orphanArtifactStrategy').val();
        $.ajax({
            url: caramel.context + '/assets/server/apis/servers/save?type=server&serverId=' + serverId +
            '&existArtifactStrategy=' + existArtifactStrategy + '&orphanArtifactStrategy=' + orphanArtifactStrategy,
            type: 'POST',
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(selectedServices),
            success: function (response) {
                $('#list_assets_table_summary').hide();
                $('#saveServices').hide();
                $("#backButton").hide();
                $('#discoveryStratergy').hide();
                var html1 = '<h2 class="sub-heading">DISCOVERY SERVICES SUMMARY</h2></br>';

                var html2 = '';
                for (var key in response) {
                    if (response.hasOwnProperty(key)) {
                        if (response[key].length > 0) {
                            html2 = html2 + '<h4 class="sub-heading" style="text-transform: uppercase;">' + key + '</h4>';
                            html2 = html2 + '<ul>';
                            for (var i = 0; i < response[key].length; i++) {
                                var serviceName = response[key][i].split(":");
                                html2 = html2 + '<li>' + serviceName[1] + ' (' + serviceName[0] + ')</li></br>';
                            }
                            html2 = html2 + '</ul></br>';
                        }
                    }
                }

                var html3 = '<a href="javascript:void(0);" id="closeWindow" class="cu-btn-inner inverse">Close</a>';
                $('#parent-container').append(html1);
                $('#parent-container').append(html2);
                $('#parent-container').append(html3);
            },
            error: function () {
                //console.log("Error loading services.");
            }
        });
    });
})