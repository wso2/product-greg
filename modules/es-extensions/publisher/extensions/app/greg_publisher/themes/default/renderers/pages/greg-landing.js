var render = function(theme,data,meta,require) {
	theme('single-col-fluid',{
		title:'Landing page test',
		header:[{
			partial:'header',
			context:data
		}],
		listassets:[{
			partial:'gc-landing',
			context:data
		}
		]
	});
};