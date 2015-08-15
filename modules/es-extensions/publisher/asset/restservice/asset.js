/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
asset.configure = function () {
    return {
        meta: {
            ui: {
                icon: 'fw fw-rest-service'
            }
        },
        table: {
            overview: {
                fields: {
                    name: {
                        placeholder: "WeatherService"
                    },
                    context: {
                        placeholder: "/test"
                    },
                    version: {
                        placeholder: "1.0.0"
                    },
                    description: {
                        placeholder: "This is a sample service"
                    }
                }
            },
            interface: {
                fields: {
                    transports: {
                        placeholder: "https,http"
                    },
                    wsdl: {
                    	placeholder: "htts://example.com/sample.wsdl"
                    },
                    wadl: {
                    	placeholder: "https://example.com/sample.wadl"
                    },
                    swagger: {
                    	placeholder: "https://example.com/sample-doc"
                    }
                }
            }
        }
    }
};
