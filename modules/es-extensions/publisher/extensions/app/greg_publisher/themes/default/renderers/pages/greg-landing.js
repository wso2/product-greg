var render = function(theme,data,meta,require) {
	theme('single-col-fluid',{
		title:data.meta.applicationTitle,
		header:[{
			partial:'header',
			context:data
		}],
        ribbon: [{
            partial: 'ribbon',
            context: data
        }],
		listassets:[{
			partial:'gc-landing',
			context:data
		}
		]
	});
};