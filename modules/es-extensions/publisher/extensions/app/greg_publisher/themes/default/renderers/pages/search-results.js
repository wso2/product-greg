var render = function(theme,data,meta,require) {
	theme('single-col-fluid',{
		title:'WSO2 Governance Center - Search Results',
		header:[{
			partial:'header',
			context:data
		}],
        ribbon: [{
            partial: 'ribbon',
            context: data
        }],
        leftnav: [{
        	partial:'left-nav',
        	context:data
        }],
		listassets:[{
			partial:'search-results',
			context:data
		}
		]
	});
};