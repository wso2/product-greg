var render = function(theme,data,meta,require) {
	theme('single-col-fluid',{
        title: data.meta.title,
        applicationTitle: data.meta.applicationTitle,
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