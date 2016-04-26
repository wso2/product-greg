var render = function(theme,data,meta,require) {
	theme('2-column-right',{
		title: data.meta.applicationTitle,
		header:[{
			partial:'header',
			context:data
		}],
		body:[{
			partial:'gc-landing',
			context:data
		}
		]
	});
};